#!/usr/bin/env bash
# Device-level QA on the emulator: install/launch, HOME/Back, external app + return, config change,
# screenshots at 1024x600 and 800x480, meminfo and gfxinfo with perf mode FULL vs LOW.
# Emulator x86_64 numbers are NOT head-unit (ARM32) performance.
set -u
APK="$1"; OUT="${2:-qa-device}"; PKG=com.shazwan.idash3; ACT=$PKG/.LauncherActivity
mkdir -p "$OUT/screens"; : > "$OUT/results.tsv"
res(){ printf '%s\t%s\t%s\n' "$1" "$2" "${3:-}" >> "$OUT/results.tsv"; echo "$1: $2 ${3:-}"; }
focus(){ adb shell dumpsys window windows 2>/dev/null | grep -m1 -E "mCurrentFocus" | tr -d '\r'; }
crashed(){ grep -qE "FATAL EXCEPTION.*|Process: $PKG" "$OUT/logcat.txt" && grep -A3 "FATAL EXCEPTION" "$OUT/logcat.txt" | grep -q "$PKG"; }
adb install -r -t "$APK" > "$OUT/install.txt" 2>&1 && res install PASS || { res install FAIL "$(tail -1 "$OUT/install.txt")"; exit 1; }
adb logcat -c; adb logcat -v threadtime > "$OUT/logcat.txt" 2>&1 & LOGCAT=$!
adb shell wm size 1024x600; adb shell wm density 160
adb shell am start -W -n "$ACT" > "$OUT/launch.txt" 2>&1; sleep 6
grep -q "Status: ok" "$OUT/launch.txt" && res launch PASS "$(grep -E 'TotalTime' "$OUT/launch.txt" | tr -d '\r')" || res launch FAIL
adb exec-out screencap -p > "$OUT/screens/home-1024x600.png"
# HOME role
adb shell cmd package set-home-activity "$ACT" > "$OUT/set-home.txt" 2>&1
adb shell input keyevent KEYCODE_HOME; sleep 3
focus | grep -q "$PKG" && res home_key PASS || res home_key FAIL "$(focus)"
adb shell input keyevent KEYCODE_BACK; sleep 2
focus | grep -q "$PKG" && res back_on_home_stays PASS || res back_on_home_stays FAIL "$(focus)"
# External app and return
adb shell am start -a android.settings.SETTINGS >/dev/null 2>&1; sleep 4
focus | grep -qi "settings" && res external_app_open PASS || res external_app_open FAIL "$(focus)"
adb shell input keyevent KEYCODE_HOME; sleep 3
focus | grep -q "$PKG" && res return_home PASS || res return_home FAIL "$(focus)"
# Page swipe on the home grid (right part of the screen)
sw(){ adb shell input swipe "$1" 300 "$2" 300 220; sleep 1; }
# gfxinfo: FULL vs LOW (pref edited through run-as on the debug build)
setperf(){ # 0 auto, 1 low, 2 full — edits the debug build's prefs via run-as and verifies it
  adb shell am force-stop $PKG; sleep 1
  adb shell run-as $PKG cat shared_prefs/idash.xml > "$OUT/prefs.xml" 2>/dev/null
  grep -v 'name="perf_mode"' "$OUT/prefs.xml" | sed "s#</map>#    <int name=\"perf_mode\" value=\"$1\" />\n</map>#" > "$OUT/prefs-new.xml"
  adb shell "run-as $PKG sh -c 'cat > shared_prefs/idash.xml'" < "$OUT/prefs-new.xml"
  got=$(adb shell run-as $PKG cat shared_prefs/idash.xml | grep -o 'name="perf_mode" value="[0-9]"' | grep -o '[0-9]"$' | tr -d '"')
  [ "$got" = "$1" ] && res "perf_mode_$1_applied" PASS || res "perf_mode_$1_applied" FAIL "got=$got"
  adb shell am start -W -n "$ACT" >/dev/null 2>&1; sleep 6
}
measure(){ # label
  adb shell dumpsys gfxinfo $PKG reset >/dev/null
  for i in 1 2 3 4; do sw 900 450; sw 450 900; done
  adb shell input tap 700 300; sleep 1; adb shell input keyevent KEYCODE_BACK; sleep 1
  adb shell dumpsys gfxinfo $PKG > "$OUT/gfxinfo-$1.txt"
  adb shell dumpsys meminfo $PKG > "$OUT/meminfo-$1.txt"
  local frames jank p90 pss
  frames=$(grep -m1 "Total frames rendered" "$OUT/gfxinfo-$1.txt" | awk -F: '{print $2}' | tr -d ' \r')
  jank=$(grep -m1 "Janky frames" "$OUT/gfxinfo-$1.txt" | awk -F: '{print $2}' | tr -d '\r' | xargs)
  p90=$(grep -m1 "90th percentile" "$OUT/gfxinfo-$1.txt" | awk -F: '{print $2}' | tr -d ' \r')
  pss=$(grep -m1 -E "TOTAL( PSS)?:? " "$OUT/meminfo-$1.txt" | awk '{print $2}' | tr -d '\r')
  res "gfx_$1" MEASURED "frames=$frames janky=$jank p90=$p90 pssKB=$pss"
}
setperf 2; adb exec-out screencap -p > "$OUT/screens/home-full-1024x600.png"; measure full
setperf 1; adb exec-out screencap -p > "$OUT/screens/home-low-1024x600.png"; measure low
setperf 0
# Configuration change while running: 800x480 then back.
adb shell wm size 800x480; sleep 5
adb exec-out screencap -p > "$OUT/screens/home-800x480.png"
adb shell pidof $PKG >/dev/null && res config_800x480_alive PASS || res config_800x480_alive FAIL
adb shell wm size 1024x600; sleep 4
adb shell pidof $PKG >/dev/null && res config_back_alive PASS || res config_back_alive FAIL
kill $LOGCAT 2>/dev/null
if grep -A4 "FATAL EXCEPTION" "$OUT/logcat.txt" | grep -q "$PKG"; then res no_crash FAIL "lihat logcat.txt"; else res no_crash PASS; fi
grep -E "ANR in $PKG" "$OUT/logcat.txt" >/dev/null && res no_anr FAIL || res no_anr PASS
adb shell wm size reset; adb shell wm density reset
{ echo "### QA peranti (emulator API27 x86_64 — bukan prestasi head unit ARM32)"; echo; echo "| Semakan | Keputusan | Butiran |"; echo "|---|---|---|"
  while IFS=$'\t' read -r n r d; do echo "| $n | $r | ${d:-} |"; done < "$OUT/results.tsv"; } > "$OUT/summary.md"
cat "$OUT/summary.md"
awk -F'\t' '$2=="FAIL"{bad=1} END{exit bad}' "$OUT/results.tsv"
