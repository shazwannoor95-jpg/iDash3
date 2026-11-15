#!/usr/bin/env bash
# iDash3 head unit debloat via ADB (PC). DISABLES only — reversible with: ./debloat-adb.sh restore
# Usage: adb connect <ip>:5555 (or USB), then: ./debloat-adb.sh [disable|restore|optional]
set -u
mode="${1:-disable}"
DEFAULT="com.mediatek.mtklogger com.mediatek.mtklogger.proxy com.mediatek.ygps com.mediatek.bluetooth.dtt com.xyauto.android.autotest com.bumblebee.remindeasy com.android.exchange com.mediatek.calendarimporter com.google.android.partnersetup com.google.android.apps.work.oobconfig com.google.android.feedback"
OPTIONAL="com.android.soundrecorder com.google.android.inputmethod.latin com.google.android.youtube com.google.android.apps.maps"
case "$mode" in
  disable)  for p in $DEFAULT; do echo "disable $p"; adb shell pm disable-user --user 0 "$p"; done ;;
  optional) for p in $OPTIONAL; do echo "disable $p"; adb shell pm disable-user --user 0 "$p"; done ;;
  restore)  for p in $DEFAULT $OPTIONAL; do echo "enable $p"; adb shell pm enable "$p"; done ;;
  *) echo "mode: disable | optional | restore"; exit 1 ;;
esac
# Free space and memory (safe):
adb shell pm trim-caches 999999M
adb shell rm -rf /sdcard/mtklog /sdcard/debuglogger /sdcard/mtklog_backup
echo "Selesai. Mulakan semula head unit."
