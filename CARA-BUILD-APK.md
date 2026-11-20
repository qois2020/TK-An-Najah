# TK An Najah – proyek Android

Aplikasi guru TK: absensi, kalender & agenda, to-do list guru, jadwal kegiatan,
doa harian (Al-Qur'an & hadits shahih), dan cerita Islami. Berjalan offline di HP.

## Cara 1 – Lewat GitHub (tanpa install apa pun, ±5 menit)

1. Di https://github.com klik **New repository**, beri nama `tk-an-najah`,
   pilih **Public** atau **Private**, klik **Create repository**.
2. Di halaman repo baru, klik **uploading an existing file**.
3. Ekstrak file ZIP ini, lalu seret **semua isi folder `TKAnNajah`**
   (termasuk folder `.github`) ke halaman upload. Klik **Commit changes**.
   - Kalau folder `.github` tidak ikut terseret (folder tersembunyi), buat file baru lewat
     **Add file > Create new file**, beri nama `.github/workflows/build-apk.yml`,
     lalu tempel isi file `build-apk.yml` dari ZIP ini.
4. Buka tab **Actions**. Proses "Build APK" berjalan otomatis (±4 menit).
5. Setelah centang hijau, buka bagian **Releases** di halaman repo (atau branch `apk`)
   dan unduh **TKAnNajah.apk**. Kirim file itu ke HP.

## Cara 2 – Lewat Android Studio di laptop

1. Install Android Studio (https://developer.android.com/studio).
2. **File > Open**, pilih folder `TKAnNajah`. Tunggu *Gradle Sync* selesai.
3. **Build > Build App Bundle(s) / APK(s) > Build APK(s)**.

## Memasang di HP

1. Buka file `.apk` di HP. Jika muncul peringatan, izinkan **Instal aplikasi tidak dikenal**
   untuk aplikasi pembuka file (Files/Chrome/WhatsApp).
2. Fitur **Bacakan** memakai mesin suara HP. Jika tidak bersuara, pasang/aktifkan
   "Layanan Ucapan Google" dengan bahasa Indonesia.

## Catatan

- Data tersimpan di HP. Menghapus aplikasi akan menghapus datanya —
  gunakan **Pengaturan ⚙️ > Salin cadangan** secara berkala.
- Data versi web dan versi HP terpisah. Untuk memindahkan: Salin cadangan di satu versi,
  lalu **Pulihkan dari cadangan** di versi lainnya.
- Update APK berikutnya bisa langsung dipasang menimpa versi lama (kunci tanda tangan sama),
  data tidak hilang.
