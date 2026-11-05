# Fuad Accounting Android

Aplikasi Android native sederhana untuk belajar siklus akuntansi. Data disimpan lokal pada SQLite di perangkat (offline).

## Fitur
- Daftar akun / COA bawaan dan tambah akun
- Input jurnal umum (satu debit dan satu kredit per transaksi)
- Riwayat jurnal
- Buku besar per akun
- Neraca saldo
- Laporan laba rugi, posisi keuangan, ringkasan kas/bank

## Cara membuka dan membangun APK
1. Instal Android Studio di komputer.
2. Pilih **Open** lalu pilih folder `FuadAccountingAndroid`.
3. Tunggu Gradle Sync selesai (butuh internet untuk mengunduh Android Gradle Plugin).
4. Pastikan Android SDK Platform 35 terpasang.
5. Pilih **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. APK debug biasanya tersedia di `app/build/outputs/apk/debug/app-debug.apk`.
7. Pindahkan APK ke ponsel Android dan instal. Izinkan instalasi dari sumber tersebut jika diminta.

## Catatan
- Proyek ini adalah starter project dan belum dikompilasi menjadi APK di lingkungan ini.
- Database lokal tidak tersinkron ke cloud dan tidak otomatis dicadangkan.
- Jurnal majemuk, jurnal penyesuaian/penutup, pajak, persediaan perpetual, multi-perusahaan, ekspor PDF/Excel, autentikasi, dan backup belum termasuk.
- Buat salinan cadangan sebelum menghapus aplikasi karena data lokal dapat ikut terhapus.
