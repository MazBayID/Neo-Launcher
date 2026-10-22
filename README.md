# Neo-Launcher

**Neo-Launcher** adalah prototipe launcher Android bertema futuristik gelap, dibuat dengan **Kotlin** dan **Jetpack Compose**. Proyek ini dikembangkan bertahap; daftar fitur yang sudah selesai dan yang masih direncanakan dicatat di [ROADMAP_121_FITUR.md](ROADMAP_121_FITUR.md).

> **Versi saat ini:** v0.11.8 · **versionCode:** 43  
> **Status:** build debug melalui GitHub Actions berhasil pada versi terakhir yang tercatat. Tetap uji di perangkat Android sebelum dipakai sebagai launcher harian.

## Fitur yang tersedia

### Beranda dan widget
- Jam digital dan jam analog yang diperbarui secara langsung.
- Pilihan desain wajah jam analog dan gaya jarum, termasuk warna aksen.
- Pengaturan tampilan jarum detik, panjang dan ketebalan jarum.
- Pengaturan ukuran jam digital/analog, ketebalan angka, dan warna angka.
- Tanggal dapat ditampilkan atau disembunyikan; format jam 12/24 jam.
- Kartu widget jam dengan pengaturan transparansi.
- Widget baterai dengan status pengisian/penuh dan pintasan ke pengaturan baterai Android.
- Pilihan preset latar beranda dan aksen warna.
- Preferensi tampilan utama disimpan secara lokal.

### Laci aplikasi
- Menampilkan aplikasi terpasang, membuka aplikasi dengan ketukan, serta pencarian.
- Filter berdasarkan huruf A–Z dan kategori `#`.
- Urutan A–Z/Z–A serta opsi mengurutkan aplikasi yang baru digunakan.
- Filter aplikasi favorit dan aplikasi terbaru.
- Mode daftar atau grid, dengan pilihan jumlah kolom.
- Opsi menampilkan/menyembunyikan indeks alfabet dan label aplikasi.
- Ukuran teks label dapat disesuaikan.
- Aplikasi tertentu dapat disembunyikan lalu dipulihkan melalui mode pengelolaan aplikasi tersembunyi.
- Preferensi utama laci aplikasi disimpan secara lokal.

## Aset visual

Aset PNG jam/tanggal tersedia di `app/src/main/res/drawable-nodpi/`. Folder ini berisi 50 aset PNG yang digunakan sebagai sumber visual proyek. Tidak semua aset harus tampil pada setiap gaya atau konfigurasi.

## Persyaratan pengembangan

- Android SDK dengan **compileSdk 36**
- **JDK 17**
- Gradle melalui Android Gradle Plugin yang dikonfigurasi di proyek
- Perangkat Android **API 26 (Android 8.0) atau lebih baru** untuk instalasi

Package aplikasi: `com.neo.launcher`

## Membuat APK debug

Cara termudah adalah melalui GitHub Actions:

1. Buka tab [Actions](https://github.com/MazBayID/Neo-Launcher/actions).
2. Pilih workflow build yang tersedia dan jalankan ulang workflow jika diperlukan.
3. Buka run yang selesai dengan status berhasil.
4. Unduh artifact bernama `neo-launcher-debug-apk`.
5. Ekstrak ZIP, lalu pasang APK di perangkat Android untuk pengujian.

Jika membangun secara lokal, dari direktori proyek jalankan:

```bash
./gradlew assembleDebug
```

APK debug biasanya dihasilkan di:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Catatan pengujian dan batasan

- Proyek masih dalam pengembangan. Keberhasilan kompilasi tidak menjamin semua interaksi berfungsi di setiap perangkat.
- Integrasi cuaca langsung belum tersedia; jangan menganggap tampilan atau rencana cuaca sebagai data cuaca real-time.
- Isu lama **BUG-001**: aksi **COPOT** dari menu tekan-lama belum berhasil membuka dialog konfirmasi pencopotan pada perangkat penguji. Masalah ini sengaja ditunda dan belum boleh dianggap selesai.
- APK debug dari Actions dapat ditandatangani berbeda dari APK yang sudah terpasang. Jika Android menolak pembaruan karena konflik tanda tangan, jangan menghapus data penting tanpa cadangan; proses penandatanganan rilis akan ditangani menjelang versi final.
- Sebagian pintasan Android dapat berbeda perilakunya bergantung pada versi sistem dan aplikasi yang tersedia.

## Struktur penting

```
app/src/main/java/com/neo/launcher/MainActivity.kt
app/src/main/res/drawable-nodpi/
app/build.gradle.kts
ROADMAP_121_FITUR.md
```

## Kontribusi dan pelaporan bug

Untuk laporan bug, sertakan:
- versi Neo-Launcher;
- versi Android/perangkat;
- langkah untuk mengulang masalah;
- hasil yang diharapkan dan hasil yang terjadi.

Lihat [daftar rencana 121 fitur](ROADMAP_121_FITUR.md) untuk status pengembangan. Daftar tersebut adalah backlog bertahap, bukan pernyataan bahwa seluruh fitur telah selesai.

---

Dikembangkan sebagai proyek **Neo-Launcher** oleh [MazBayID](https://github.com/MazBayID).
