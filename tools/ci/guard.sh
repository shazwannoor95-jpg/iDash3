#!/usr/bin/env bash
# Static regression guard for iDash3 (read-only). Optional arg: path to the debug APK.
set -u
APPID="com.shazwan.idash3"
fail=0; bad(){ echo "GAGAL: $*"; fail=1; }; ok(){ echo "OK: $*"; }
cd "$(dirname "$0")/../.."
grep -q "applicationId '$APPID'" app/build.gradle && ok "applicationId $APPID" || bad "applicationId berubah"
grep -qi "signingConfig" app/build.gradle build.gradle && bad "signingConfig dalam Gradle (keputusan signing belum dibuat)" || ok "tiada signingConfig"
# Drive Dashboard uses native map/player views. External activity embedding stays out.
pat='VirtualDisplay|allowEmbedded|ActivityView|SurfaceControlViewHost|setLaunchDisplayId|a1lab|DashNative|createVirtualDisplay'
grep -rnE "$pat" app/src/main && bad "ciri eksperimen embedding/VirtualDisplay ditemui" || ok "tiada embedding/VirtualDisplay"
# Third-party assets whose licence is not established must not come back.
if find app/src -iname '*mrios*' -o -iname 'sf_pro*' -o -iname '*smarttube*' | grep -q .; then bad "aset MRiOS/SF Pro/SmartTube ditemui"; else ok "tiada aset MRiOS/SF Pro/SmartTube"; fi
grep -rqi "mrios\|sf_pro" app/src/main && bad "rujukan MRiOS/SF Pro dalam source" || ok "tiada rujukan MRiOS/SF Pro"
grep -q 'permission android:name="com.shazwan.idash3.permission.CONTROL"' app/src/main/AndroidManifest.xml && ok "permission signature dinamakan untuk iDash3" || bad "permission signature bertembung dengan iDash lama"
for c in DriveDashboard DrivePlan HomeWidgets HomeGrid HomeLayout JakimCalendar QuickControls GlassSurface GlassBackdrop GlassPerf Spring IconStyler MorphOverlay DashSettings; do
  [ -f app/src/main/java/com/shazwan/idash3/$c.java ] && ok "wujud: $c" || bad "hilang: $c"; done
for z in SGR01 SGR02 SGR03 WLY01; do [ -f app/src/main/assets/jakim/$z.json ] && ok "jadual JAKIM $z" || bad "hilang jadual $z"; done
if [ "${1:-}" ]; then
  apk="$1"; [ -f "$apk" ] || { bad "APK tiada: $apk"; exit 1; }
  aapt2="$(ls "${ANDROID_HOME:-/nonexistent}"/build-tools/*/aapt2 2>/dev/null | sort -V | tail -1)"
  if [ -n "$aapt2" ]; then
    b="$("$aapt2" dump badging "$apk")"
    echo "$b" | grep -q "package: name='$APPID'" && ok "APK: pakej $APPID" || bad "APK: pakej salah"
    echo "$b" | grep -q "native-code" && bad "APK: ada pustaka native" || ok "APK: tiada pustaka native (neutral ABI)"
  else bad "aapt2 tidak ditemui"; fi
  size=$(stat -c %s "$apk"); [ "$size" -lt 8000000 ] && ok "APK ${size} bait (<8 MB)" || bad "APK terlalu besar: $size"
fi
exit $fail
