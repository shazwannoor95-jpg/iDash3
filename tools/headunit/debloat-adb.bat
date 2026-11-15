@echo off
REM iDash3 head unit debloat via ADB (Windows). DISABLES only. Undo: debloat-adb.bat restore
set MODE=%1
if "%MODE%"=="" set MODE=disable
set DEFAULT=com.mediatek.mtklogger com.mediatek.mtklogger.proxy com.mediatek.ygps com.mediatek.bluetooth.dtt com.xyauto.android.autotest com.bumblebee.remindeasy com.android.exchange com.mediatek.calendarimporter com.google.android.partnersetup com.google.android.apps.work.oobconfig com.google.android.feedback
set OPTIONAL=com.android.soundrecorder com.google.android.inputmethod.latin com.google.android.youtube com.google.android.apps.maps
if "%MODE%"=="disable" for %%p in (%DEFAULT%) do adb shell pm disable-user --user 0 %%p
if "%MODE%"=="optional" for %%p in (%OPTIONAL%) do adb shell pm disable-user --user 0 %%p
if "%MODE%"=="restore" for %%p in (%DEFAULT% %OPTIONAL%) do adb shell pm enable %%p
adb shell pm trim-caches 999999M
adb shell rm -rf /sdcard/mtklog /sdcard/debuglogger /sdcard/mtklog_backup
echo Selesai. Mulakan semula head unit.
