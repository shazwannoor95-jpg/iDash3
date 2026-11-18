#!/usr/bin/env bash
# Runs iDash3 instrumentation classes on the emulator, one at a time with a timeout and an
# emulator health check (lesson from iDash2 run 42312986901, where one hanging class took the
# emulator down and 13 classes never ran). Result per class: LULUS / GAGAL / TAMAT_MASA /
# TIDAK_DIJALANKAN / INFRA_EMULATOR_MATI. Only LULUS counts as a pass.
set -u
APP_APK="$1"; TEST_APK="$2"; OUT="${3:-qa-out}"
PKG=com.shazwan.idash3; TEST_PKG=$PKG.test
mkdir -p "$OUT"; : > "$OUT/results.tsv"
GATE="${GATE:-PrayerJourneyChecks PanelLayoutChecks DashboardLayoutChecks DriveDashboardChecks PremiumFmChecks Idash3Checks SmokeChecks Rc2Checks CrashSafetyChecks AppLibraryChecks DrivingWorkspaceChecks Ios26IconChecks MaterialSwitchChecks MaterialContrastChecks BakeQueueChecks PanelFullscreenChecks QualityChecks StabilityChecks WallpaperChecks GlassRenderChecks GlassMaterialChecks SettingsChecks PanelMigrationChecks}"
INFO="${INFO:-DeviceReadinessChecks SettingsCompleteChecks SettingsAccessChecks SettingsResetChecks MediaUtilitiesChecks ControlLayoutChecks ControlGlassChecks AppearanceChecks AppearanceRenderChecks AppearanceGradientChecks CompactChecks Glass6aChecks ControlVisualChecks PerformanceAuditChecks SystemControlsChecks}"
adb install -r -t "$APP_APK" > "$OUT/install-app.txt" 2>&1 || { cat "$OUT/install-app.txt"; exit 2; }
adb install -r -t "$TEST_APK" > "$OUT/install-test.txt" 2>&1 || { cat "$OUT/install-test.txt"; exit 2; }
adb shell settings put secure immersive_mode_confirmations confirmed
adb shell wm size 1024x600; adb shell wm density 160
adb logcat -c; adb logcat -v threadtime > "$OUT/logcat-instrumentation.txt" 2>&1 & LOGCAT=$!
healthy(){ [ "$(timeout 20 adb get-state 2>/dev/null)" = device ] && [ "$(timeout 20 adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; }
CLASS_TIMEOUT="${CLASS_TIMEOUT:-300}"
run_one(){
  local name="$1"
  if ! healthy; then sleep 20; healthy || { echo INFRA_EMULATOR_MATI; return; }; fi
  adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1
  timeout "$CLASS_TIMEOUT" adb shell am instrument -w -r "$TEST_PKG/$PKG.$name" > "$OUT/$name.txt" 2>&1; local rc=$?
  adb shell am force-stop "$PKG" >/dev/null 2>&1
  if [ $rc -eq 124 ]; then adb shell am force-stop "$TEST_PKG" >/dev/null 2>&1; healthy && echo TAMAT_MASA || echo INFRA_EMULATOR_MATI; return; fi
  if grep -q "INSTRUMENTATION_CODE: -1" "$OUT/$name.txt" && grep -q "stream=PASS" "$OUT/$name.txt"; then echo LULUS
  elif grep -q "INSTRUMENTATION_CODE:" "$OUT/$name.txt"; then echo GAGAL
  elif healthy; then echo TIDAK_DIJALANKAN; else echo INFRA_EMULATOR_MATI; fi
}
detail(){ grep -m1 -o "stream=[A-Z]*[^\r]*" "$OUT/$1.txt" 2>/dev/null | head -1 | cut -c1-220; }
show(){ [ "$2" = LULUS ] && return; echo "::group::BUTIRAN $1 => $2"; head -c 2500 "$OUT/$1.txt" 2>/dev/null; echo; grep -E "FATAL EXCEPTION|AndroidRuntime|ANR in" "$OUT/logcat-instrumentation.txt" | tail -30; echo "::endgroup::"; }
for c in $GATE; do r=$(run_one "$c"); printf '%s\tgate\t%s\t%s\n' "$c" "$r" "$(detail "$c")" >> "$OUT/results.tsv"; echo "$c: $r"; show "$c" "$r"; done
for c in $INFO; do r=$(run_one "$c"); printf '%s\tmaklumat\t%s\t%s\n' "$c" "$r" "$(detail "$c")" >> "$OUT/results.tsv"; echo "$c: $r"; show "$c" "$r"; done
kill $LOGCAT 2>/dev/null
{ echo "### Instrumentation (emulator API27 x86_64, benar-benar dijalankan)"; echo; echo "| Kelas | Jenis | Keputusan | Butiran |"; echo "|---|---|---|---|"
  while IFS=$'\t' read -r n t r d; do echo "| $n | $t | $r | ${d:-} |"; done < "$OUT/results.tsv"; } > "$OUT/summary.md"
cat "$OUT/summary.md"
awk -F'\t' '$2=="gate" && $3!="LULUS"{bad=1} END{exit bad}' "$OUT/results.tsv"
