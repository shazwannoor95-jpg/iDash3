#!/usr/bin/env bash
# Capture one iDash3 UI screenshot from a connected head unit into docs/images/.
# Usage: tools/screenshots.sh <home|app-library|control-center|settings|drive|drive-split>
# The screen list, what each one should show and the commit steps are in docs/SCREENSHOTS.md.
set -euo pipefail
name="${1:-}"
case "$name" in
  home|app-library|control-center|settings|drive|drive-split) ;;
  *) echo "usage: tools/screenshots.sh <home|app-library|control-center|settings|drive|drive-split>" >&2; exit 2 ;;
esac
command -v adb >/dev/null 2>&1 || { echo "adb not found: install Android platform-tools and put adb on PATH" >&2; exit 1; }
if [ "$(adb get-state 2>/dev/null)" != "device" ]; then
  echo "no authorised device: connect the head unit, enable USB debugging and accept the prompt" >&2
  exit 1
fi
mkdir -p docs/images
adb exec-out screencap -p > "docs/images/$name.png"
size=$(wc -c < "docs/images/$name.png")
if [ "$size" -lt 5000 ]; then
  echo "capture looks empty ($size bytes): is the head unit screen on?" >&2
  exit 1
fi
echo "saved docs/images/$name.png ($size bytes)"
echo "read docs/SCREENSHOTS.md, then commit the PNGs and add them to the README gallery"
