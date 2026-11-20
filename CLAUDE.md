# TK An Najah — catatan pemeliharaan

Aplikasi guru TK (absensi, kalender, to-do, hafalan, perkembangan & raport, doa & cerita Islami, kuis),
multi-guru lewat Firebase (proyek `tk-an-najah`). Pemilik: qois2020.

## Struktur
- `src/index.html` — **SUMBER UTAMA**. Satu file HTML (CSS+JS). Edit di sini, bukan di hasil build.
  Tanpa `window.FB_CONFIG` ia berjalan mode lokal (dipakai juga sebagai versi pribadi di Claude).
- `tools/build.py` — menyuntikkan font offline + Firebase (`tools/fb.js`) → `app/src/main/assets/index.html` & `web/index.html`.
  `--mock` memakai `tools/mock.js` (Firebase tiruan, data di localStorage) untuk uji Playwright.
- `app/` — proyek Android (WebView + WebViewAssetLoader, jembatan JS: AndroidVoice/AndroidNotify/AndroidPrint).
- `web/` — GitHub Pages (untuk iPhone/laptop): https://qois2020.github.io/TK-An-Najah/
- `firestore.rules` — aturan keamanan (dipasang manual di Firebase console → Firestore → Rules → Publish).
- `.github/workflows/build-apk.yml` — tiap push ke `main`: build APK → Release `TKAnNajah.apk`,
  terbitkan `web/` + `version.json` ke branch `gh-pages`. Nomor build = run_number+100 (mengganti `__BUILD__`);
  aplikasi membandingkan dengan `version.json` dan menampilkan bar "Versi baru tersedia".
  Pesan commit terakhir tampil ke guru sebagai catatan update — tulis dalam bahasa Indonesia yang ramah.

## Cara update
1. Edit `src/index.html`.
2. `cd tools && npm install && cd .. && python3 tools/build.py`
3. Commit `src/`, `app/src/main/assets/index.html`, `web/index.html`, lalu push ke `main`.

## Model data Firestore
- `school/main` {name, head, admins:[uid]} · `school/agenda` {events:{id:…}}
- `users/{uid}` {name,email,approved,classId} · `users/{uid}/priv/main` {todos, settings pribadi}
- `classes/{id}` {name, students:{id:…}, schedule[]} · `classes/{id}/data/progres` {hafalan, notes, custom}
- `classes/{id}/data/jurnal` {notes:{tanggal:teks}} · `classes/{id}/absen/{YYYY-MM}` {days:{tgl:{sid:H|I|S|A}}}
Penulisan memakai diff per-field (setDoc merge) agar guru di kelas yang sama tidak saling menimpa.

## Aturan konten
- Doa harus dari Al-Qur'an atau hadits shahih, cantumkan sumbernya. Cerita juga dari Al-Qur'an/hadits shahih.
- Karakter Ahmad & Aisyah **tanpa wajah** (dibedakan peci & kerudung); hindari emoji berwajah.
