#!/usr/bin/env bash
# Read-only Maps/WebView baseline. No logcat, account, location, settings writes or package changes.
set -euo pipefail
out="${1:-idash-runtime.txt}"
adb_cmd=(adb)
if [ -n "${ANDROID_SERIAL:-}" ]; then adb_cmd+=(-s "$ANDROID_SERIAL"); fi
"${adb_cmd[@]}" get-state >/dev/null
{
  printf 'iDash4 runtime baseline (read-only)\n'
  for property in ro.product.model ro.build.version.release ro.build.version.sdk ro.product.cpu.abilist dalvik.vm.heapgrowthlimit dalvik.vm.heapsize ro.config.low_ram; do
    printf '\n%s: ' "$property"
    "${adb_cmd[@]}" shell getprop "$property"
  done
  for package in com.shazwan.idash3 com.google.android.apps.maps com.waze com.google.android.gms com.google.android.webview com.android.webview com.android.chrome; do
    printf '\nPackage: %s\n' "$package"
    "${adb_cmd[@]}" shell dumpsys package "$package" | grep -E 'versionCode=|versionName=' || true
  done
  printf '\nActive WebView provider\n'
  "${adb_cmd[@]}" shell dumpsys webviewupdate
  for package in com.shazwan.idash3 com.google.android.apps.maps; do
    printf '\nMemory: %s\n' "$package"
    "${adb_cmd[@]}" shell dumpsys meminfo "$package"
  done
} > "$out"
printf 'Saved: %s\n' "$out"
