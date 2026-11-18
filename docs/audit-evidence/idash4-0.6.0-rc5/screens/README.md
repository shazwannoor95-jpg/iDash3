# Preview provenance

- `apps-native-1024x600.png`: actual settled emulator screen capture, 1024×600 viewport/display.
- `apps-native-viewport800x480.png`: actual settled emulator screen capture after `wm size 800x480`; Android compositor retains a 1024×600 physical output and scales the 800×480 logical app viewport into it.
- `drive-1024x600.png` and `drive-800x480.png`: native app-view Canvas fixtures at those exact pixel dimensions. Map data connection is disabled in the fixture, so the blank tile grid is intentional.

No images here are physical head-unit captures. Early Apps Canvas fixtures were omitted because they captured the grid before deferred icon resizing settled; use the actual native screenshots above for appearance review.
