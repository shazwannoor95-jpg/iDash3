# SOURCE-INVENTORY — Fasa 0

Tarikh: 2026-11-07. Semua rujukan dibaca sahaja (clone cetek di luar working tree iDash3; `.git` tidak disalin). Bukti = commit, diff, fail dan log Actions yang benar-benar dibaca dalam sesi ini.

## Akses
| Repo | Akses | Keputusan |
|---|---|---|
| iDash3 | tulis | OK, branch kerja `claude/hopeful-cannon-r401pi` (hanya `Initial commit`, README + .gitignore) |
| iDash (awam) | baca | OK, clone `--depth 1 --no-single-branch` kemudian fetch kedalaman 200 |
| iDash2 (PRIVATE) | baca | OK, `add_repo` berjaya dan clone berjaya |

## Branch (disahkan wujud)
**iDash** — hanya 3 branch; TIADA branch "stabil/release lain" selain yang di bawah.
| Branch | Tip | Versi | Java | Catatan |
|---|---|---|---|---|
| `main` | 6f88d5a (2026-11-02) | 0.1.7 / code 8 | 24 | Pratonton awal; tiada Fasa 6A–6C |
| `recovery/fasa-6d-a1-20261103` | e574a3f | **0.1.8 / code 9 (Fasa 6C)** | 99 | Source penuh; launcher = 6C, 6D/A1 hanya dalam `experiments/` |
| `delivery/idash-0.1.8-build9-apk` | 384d715 | 0.1.8 / code 9 | 99 | Sama dengan recovery + 1 APK debug 6.5 MB (SHA256 85b613f2…1a9e, sepadan dengan dokumen) |

**iDash2**
| Branch | Tip | Keadaan |
|---|---|---|
| `main` | 490f60c | KOSONG: hanya `README.md` ("# iDash2") |
| `claude/hopeful-pasteur-vn9f83` | 1f34d9a | Import 6C + buang panel native (VirtualDisplay) + APK debug |
| `ci/fasa-8a-github-actions` | fa58865 | Branch di atas + CI + pembetulan `panel_mode=3` + ujian regresi |

Branch lain: tiada. Nama branch yang anda ingat semuanya wujud.

## Perbezaan utama
- recovery ↔ delivery: beza satu fail sahaja (APK).
- recovery → iDash2 (`claude/…`): 4 fail dibuang (`DashNativeFrame/Proxy/Geometry.java`, `PanelChecks.java`), 7 fail diubah (2 manifest, `DashSettings`, `LauncherActivity`, 3 `Settings*Checks`), `experiments/` dan `dist/` lama tidak dibawa. Dokumen migrasi sendiri menyebut angka ini; saya sahkan senarai fail dengan `diff` (empat fail dibuang, `docs/MIGRASI-IDASH2-MS.md` ditambah).
- `ci/…` ↔ `claude/…`: +CI workflow, +`tools/ci/*`, +`PanelMigrationChecks`, +2 baris `LauncherActivity` (normalisasi `panel_mode` di luar 0..2 → 0).

## Status CI iDash2 Fasa 8A (run 42312986901, commit c9b8b2d, ditamatkan: **failure**)
| Job | Keputusan |
|---|---|
| Build, lint & guard | **LULUS** — guard statik, assembleDebug + assembleDebugAndroidTest, lintDebug, guard APK, checksum |
| Ujian unit | `NO-SOURCE` — langkah "lulus" tetapi tiada ujian unit wujud (bukan bukti) |
| Instrumentation emulator API27 (x86_64) | **GAGAL** |

Perincian instrumentation (log job 128219613057): LULUS = Quality, Stability, DeviceReadiness, Wallpaper, GlassRender, GlassMaterial, Settings (7 kelas). **GAGAL = SmokeChecks** (`stream=FAIL`, punca tidak kelihatan dalam log; ada dalam artifact `instrumentation-logs`, belum saya muat turun). **TAMAT_MASA = SystemControlsChecks** (±15 min), selepas itu emulator "device offline" → 13 kelas gate TIDAK_DIJALANKAN, termasuk `PanelMigrationChecks` (ujian bagi pembetulan `panel_mode=3`) dan 2 kelas persistence yang dilapor GAGAL akibat emulator mati, bukan akibat kod. Maka: **pembetulan `panel_mode=3` hanya disahkan oleh kod, belum oleh ujian yang dijalankan.**
Run 2 (fa58865, penambahbaikan log/penjagaan emulator) masih `in_progress` ketika saya semak; keputusannya belum diketahui. Jangan anggap hijau.

## Cadangan baseline (menunggu kelulusan anda)
**Baseline = iDash2 `ci/fasa-8a-github-actions` @ fa58865** (turunan terus dari iDash recovery e574a3f / Fasa 6C), dengan syarat:
1. Fungsi Fasa 6C dikekalkan (bukti: 126 fail `app/` identik dengan sumber asal).
2. Pembuangan panel native dikekalkan (anda sudah batalkan embedding).
3. Pembetulan `panel_mode` diambil (logik jelas betul; ujian belum dijalankan).
4. `main` iDash2 tidak boleh dijadikan baseline (kosong); `main` iDash (0.1.7) terlalu awal.
5. `experiments/phase6d-a1` diketepikan (A1 tidak berjaya: proxy muncul pada display0).
6. Source 0.1.9 Build 10 (Codex) TIDAK wujud di GitHub; tidak direka semula.
