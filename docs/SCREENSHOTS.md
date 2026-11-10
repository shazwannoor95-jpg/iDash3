# Screenshots / Tangkapan skrin

The README gallery uses six PNG files in `docs/images/`. Nothing is committed yet: the maintainer's
build environment has no ARM32 head unit and no KVM-capable emulator, so a real screenshot has to be
taken on the device (or taken by CI and copied over).

Galeri README menggunakan enam fail PNG dalam `docs/images/`. Belum ada yang dimasukkan: persekitaran
bina penyelenggara tiada head unit ARM32 dan tiada emulator ber-KVM, jadi tangkapan skrin sebenar
perlu diambil pada peranti (atau diambil oleh CI dan disalin).

## What to capture / Apa yang perlu dirakam

Take everything in **landscape at 1024×600** (the target head unit). An 800×480 set is optional.
Ambil semuanya dalam **landskap 1024×600** (head unit sasaran). Set 800×480 adalah pilihan.

| File | Screen / Skrin | What should be visible / Apa yang patut kelihatan |
|---|---|---|
| `home.png` | Home | Large clock, calendar, JAKIM widget, floating dock, panel on the right |
| `app-library.png` | App Library | App grid with search field and icons |
| `control-center.png` | Control Center | Wi-Fi, Bluetooth, brightness, volume and media tiles |
| `settings.png` | Settings | Category list (Appearance, Home & Dock, Prayer Times, Panel, About) |
| `drive.png` | Drive page | PETA/MEDIA cards, ⇄ layout button, requirement chips, Start Drive |
| `drive-split.png` | Drive in split-screen | Two apps docked plus the Android Auto-style bar (launcher, map, media, phone, Settings, battery, clock) |

## How to capture / Cara merakam

Enable USB debugging, connect the head unit and confirm `adb devices` lists it, then either:

```bash
# one screen at a time (name from the table above)
tools/screenshots.sh home

# or capture directly
adb exec-out screencap -p > docs/images/home.png
```

For `drive-split.png`, start Drive first (Drive page → **Mula Drive**), wait until both apps are
docked, then capture. The floating bar needs the "display over other apps" permission — without it the
screenshot shows the split pair only, which is still useful.

Untuk `drive-split.png`, mulakan Drive dahulu (halaman Drive → **Mula Drive**), tunggu sehingga kedua-dua
app didok, kemudian rakam. Bar terapung memerlukan kebenaran "paparan di atas app lain" — tanpanya
tangkapan skrin hanya menunjukkan pasangan split, yang masih berguna.

## Commit them / Masukkan ke repo

```bash
git add docs/images/*.png
git commit -m "docs: add UI screenshots"
git push
```

Then replace the "no screenshots yet" paragraph in `README.md` with a gallery, for example:

```markdown
| Home | Drive (split-screen) |
|---|---|
| ![Home](docs/images/home.png) | ![Drive](docs/images/drive-split.png) |
```

Kemudian gantikan perenggan "belum ada tangkapan skrin" dalam `README.md` dengan galeri seperti di atas.

## CI alternative / Alternatif CI

`.github/workflows/android-ci.yml` runs an API 27 x86_64 emulator that already captures
`qa-device/screens/*.png` through [`../tools/ci/device-qa.sh`](../tools/ci/device-qa.sh). Download the
**emulator-qa** artifact from the latest Actions run and copy `screens/home-1024x600.png` into
`docs/images/home.png`. Emulator renders are x86_64 software rendering: fine for a preview, not proof
of ARM32 head-unit behaviour, so label them as emulator output when used.

CI menjalankan emulator API 27 x86_64 yang sudah merakam `qa-device/screens/*.png` melalui
[`../tools/ci/device-qa.sh`](../tools/ci/device-qa.sh). Muat turun artifak **emulator-qa** dari run
Actions terakhir dan salin `screens/home-1024x600.png` ke `docs/images/home.png`. Render emulator ialah
x86_64 software rendering: sesuai untuk pratonton, bukan bukti kelakuan ARM32, jadi labelkannya sebagai
output emulator jika digunakan.
