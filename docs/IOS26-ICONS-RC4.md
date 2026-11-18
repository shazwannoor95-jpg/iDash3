# iOS26 icon pack — rc4

Source: https://github.com/SysAdminDoc/iOSIconPack, pinned commit `db0ccf25ff695a076eee16833fd868a1a454cda6`. Only the 57 `ios26_lg` PNGs are imported, byte-for-byte. Upstream LICENSE and per-image provenance are bundled under `app/src/main/assets/icons/ios26/`. Upstream notes that artwork/trademark ownership remains with its owners; the repository's MIT license must not be represented as a separate artwork permission grant.

617 exact component mappings and 435 unambiguous package fallbacks are derived from upstream appfilter.xml. Known stock categories use suitable pack artwork. OEM radio, CAN bus, car tools and other unmatched apps retain the prior renderer. Home has no artwork in this set and keeps the launcher glyph. Calendar/clock PNGs are static pack artwork; actual clock/calendar widgets are unchanged.

Matched app icons, favorite dock icons, Settings icon and Drive icons use the pack when the existing ios_icons switch is enabled. Unmatched icons keep the legacy style controls. Rail Map/Media/Phone are fixed UI artwork, with accessible labels, tooltips and the same 56dp targets. No app labels, launch intents, glass panel dimensions or shaders change in rc4.

Images are decoded to 96×96 and shared through a 1 MiB LRU cache. This is a cache budget, not a cap on all bitmap memory; views/catalogs can retain their own references. Memory trim evicts cache references without recycling bitmaps still used by views. No icon-pack APK, Kotlin runtime or network is required on the head unit.

47 unit tests and 310 emulator assertions passed (193 icon/hash/cache/rail, 33 workspace, 10 isolation, 17 App Library, 57 smoke). Build/lint/guard passed; raw outputs and a real emulator screenshot are under `audit-evidence/0.2.1-rc4/`. Physical ARM32 testing remains outstanding.

This branch preserves the classic glass renderer. The subsequently requested optional panel material and three-branch integration belong to the iDash4 integration branch.
