#!/usr/bin/env bash
# Run after installing app + test APK on a disposable emulator. Separate processes are essential.
set -euo pipefail
out="${1:-qa-persistence}"
mkdir -p "$out"
for stage in write read; do
  timeout 180 adb shell am instrument -w -r -e stage "$stage" com.shazwan.idash3.test/com.shazwan.idash3.AppearancePersistenceChecks > "$out/$stage.txt" 2>&1
  adb shell am force-stop com.shazwan.idash3
  if ! grep -q 'stream=PASS' "$out/$stage.txt"; then cat "$out/$stage.txt"; exit 1; fi
  printf '%s: PASS\n' "$stage"
done
