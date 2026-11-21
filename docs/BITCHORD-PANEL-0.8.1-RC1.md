# BitChord panel and emulator launcher — 0.8.1-rc1

Changes are isolated on `feat/idash4-0.8.1-rc1-panel-emulator`, based on f1dfc20. The previous version remains on `feat/idash4-0.8.0-rc2-full-bitchord-ui`; main is not a deployment target.

The BitChord Compose root now provides a `LocalWindowInfo.containerSize` matching its measured panel, including the toolbar height. Player sheet travel previously consulted the Activity window despite the embedded root receiving panel width/height. The remaining WindowInfo properties delegate to the host. The new **Besarkan BitChord** button hides the left cards, giving music the Drive workspace beside the rail; **Pulihkan kad** restores them. Expansion keeps the same ComposeView and playback session. Leaving music restores the cards. Back restores the cards when the host handles Back; upstream music navigation can consume Back first.

A separate exported `DashboardEntry` alias advertises MAIN/LAUNCHER as **iDash4**. The target Activity retains MAIN/HOME/DEFAULT. The internal BitChord alias remains non-exported. Previous rc2 already declared LAUNCHER on the HOME activity, so the original cause of the user's missing icon is not established merely from source inspection. ABI-specific builds now support x86_64 (most Intel/AMD Android Studio emulators), arm64-v8a (ARM64 emulators/head units), and armeabi-v7a (the photographed API27 head unit). Choose the APK matching the emulator's `ro.product.cpu.abilist`; the displayed app name is iDash4.

## Use

Install the matching APK, open **iDash4**, enter Drive and select **B**, then tap **Besarkan BitChord**. It remains native code inside iDash; no external BitChord installation, split-screen, root or accessibility service is required. Android Studio can launch the app with:

```
adb shell am start -n com.shazwan.idash3/.DashboardEntry
```

## Revert

The prior code remains preserved in its branch and tags. This build uses versionCode42. The older published code41 APK cannot be installed as a normal update over code42. Use the supplied `iDash4-0.7.0-rc5-rollback-code42.apk` to restore the pre-integration rc5 UI without uninstalling; it has the same package, certificate and versionCode42, and no native libraries. This rollback is built from `backup/pre-native-music` with only versionCode42 and versionName `0.7.0-rc5-rollback-full` changed. It preserves application data, rather than backing up personal account data. Alternatively developers can use `adb install -r -d` with a prior debug APK.

The licensing/source notes in [the native integration report](NATIVE-BITCHORD-0.8.0-RC2.md) continue to apply. Live streaming/login and physical head-unit performance are not established by emulator tests.

## Reproduce ABI builds

Use the configured JDK21/Android37 toolchain. Run `./gradlew -PidashAbi=x86_64 :app:assembleDebug` for Intel/AMD emulators, replace the property with `arm64-v8a` for ARM64, or `armeabi-v7a` for the photographed head unit. Omitting the property includes all three ABIs. The supplied three single-ABI APKs have identical DEX and manifests; native library payloads differ. All use versionCode42 and the same debug certificate as rc2. SHA256, ABI inventories and exact sizes are recorded in `audit-evidence/idash4-0.8.1-rc1/artifacts.json`.

## Validation

- 78 JVM tests pass (zero failures/errors/skips); lint completes with zero errors and 105 warnings.
- The final x86_64 APK installs on the API27 emulator and resolves `MAIN/LAUNCHER` to `DashboardEntry`. Starting that alias succeeds.
- The final x86_64 APK passes 17 native UI assertions, including expanded width, left-card visibility, composition identity, restore, actual Search/Library navigation and disposal/reopening.
- Initial software-only emulator runs encountered application TIME_TICK ANRs during first Compose load; the final run also needed the emulator's System UI boot ANR dialog closed before completing. This is not evidence of smooth cold startup on the physical 1GB head unit. An earlier universal candidate passed ten real synthetic-PCM playback assertions, but its UI run failed and is not counted as final-APK validation.
- Android's actual Launcher3 app drawer displays the **iDash4** icon; selecting the icon opens the host dashboard. See [app drawer evidence](audit-evidence/idash4-0.8.1-rc1/emulator-app-drawer.png).

Actual final-APK previews: [expanded Library](audit-evidence/idash4-0.8.1-rc1/bitchord-expanded-library.png) and [expanded Home](audit-evidence/idash4-0.8.1-rc1/bitchord-expanded-home.png). The Library's `Native audio B / Test PCM` history is synthetic playback-test data, not a live catalogue result. All four public raw download URLs return HTTP200 with expected lengths; downloading the complete x86_64 APK also reproduces its local SHA256.

An actual API27 install round trip of the supplied code42 APKs succeeds: final x86_64 rc1 → rc5 rollback → final x86_64 rc1, without uninstalling. All 17 existing iDash preference entries retain their values through both replacements. The verified installation ends on rc1. See `rollback-roundtrip.json`; no private preference contents are published.
