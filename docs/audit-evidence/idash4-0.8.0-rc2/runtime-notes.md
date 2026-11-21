# Runtime verification notes

The first runner stopped before instrumentation because its script was edited while Bash was still reading it during the long install. The resulting `db: command not found` is a test-driver failure; it is not a product pass. The runner is now kept stable for each execution and accepts an explicitly empty INFO list.

The first real full-UI runtime run reached the upstream Compose tree, then crashed when Android's weighted horizontal layout issued an unbounded provisional measure to a LazyColumn. Original stack and instrumentation output are preserved in `runtime/`. Host fix: disable baseline measurement on the weighted Drive row and ignore unbounded provisional passes in BitChordPanel until the parent's finite pass. Subsequent runs are recorded separately rather than overwriting the failure.

The emulator is API 27/x86_64, 1024x600/mdpi, 1024 MB RAM, software CPU emulation without KVM. APK installation/dex verification and instrumentation startup are particularly slow. Timing and RAM on this emulator are not measurements of the physical ARM32 head unit.

A system/app ANR dialog remaining after the failed run was visually inspected and dismissed before the next run; that screen is not published as a successful UI preview. Launcher3 temporarily owns HOME during crash recovery so Android does not repeatedly relaunch the failed build. Product HOME behavior is checked separately after installing the fixed build.

Live internet in this emulator currently fails DNS. Synthetic local PCM is explicitly labelled and is used only to check the actual upstream Media3 service, session, queue and audio clock. No fake online catalogue is supplied to the product.

The bounded run no longer showed the unbounded-scroll crash, but system UiAutomation connection was requested during heavy first composition and timed out. The UI test now establishes that system connection before opening the native panel. A later actual ANR trace put the main thread in MediaWidget.<clinit>, called by PlaybackService.onCreate. The host excludes standalone BitChord AppWidget providers and guards their refresh calls; its own existing media card continues to use the same owned player session. R8/resource shrinking is enabled with upstream reflection/JNI rules and preserved host/media API boundaries. The target-app main-thread excerpt is saved separately. Only this owned emulator's adbd was elevated to read the ANR file; product code requests no root or Activity-embedding privileges.
