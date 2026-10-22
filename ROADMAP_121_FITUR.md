# Neo-Launcher — Rencana 121 Fitur

Dokumen ini adalah backlog bertahap, bukan klaim bahwa semua fitur sudah tersedia. Fitur yang membutuhkan izin, API eksternal, atau dukungan versi Android akan diimplementasikan dengan fallback dan pengaturan privasi.

## Fitur yang sudah diterapkan pada batch 0.3.0–0.5.0
- [x] Filter alfabet aplikasi termasuk kategori `#` untuk nama yang tidak diawali huruf.
- [x] Hapus cepat pada kolom pencarian.
- [x] Indikator visual huruf alfabet yang dipilih dan ketuk ulang untuk membatalkan filter.
- [x] Aksi hapus filter pada hasil kosong.
- [x] Panel pengaturan dapat digulir di layar kecil.
- [x] Beranda dapat digulir pada layar pendek.
- [x] Pesan fallback untuk aplikasi jam, kalender, dan Pengaturan Android yang tidak tersedia.
- [x] Preferensi aksen dan ukuran ikon tetap disimpan.
- [x] Pilihan wajah jam analog (5 desain) disimpan.
- [x] Format jam 12/24 jam.
- [x] Tampilkan/sembunyikan detik.

## Perbaikan tambahan v0.7.1
- [x] Jumlah aplikasi pada drawer mengikuti filter aplikasi sistem.
- [x] Pesan kosong muncul ketika filter menyisakan nol aplikasi.
- [x] Chip filter dapat digulir horizontal agar tidak memaksa semua kontrol muat di layar kecil.
- [x] Tombol RESET membersihkan pencarian, huruf alfabet, dan filter favorit.
- [ ] BUG-001: tombol COPOT belum membuka konfirmasi pencopotan pada perangkat penguji; sengaja ditunda untuk batch berikutnya.

## Perbaikan tambahan v0.7.2
- [x] Widget baterai menampilkan status sedang mengisi atau penuh.
- [x] Status pengisian dibaca langsung dari status baterai Android tanpa izin tambahan.

## Batch v0.8.0
- [x] Empat preset latar beranda: Neo gelap, Violet, Teal, dan Amber.
- [x] Pilihan latar disimpan otomatis dan dipulihkan saat aplikasi dibuka kembali.
- [x] Label build beranda diselaraskan dengan versi aplikasi.
- [ ] BUG-001 tombol COPOT tetap ditunda sesuai keputusan pengujian.

## Batch v0.8.1
- [x] Pilihan warna label aplikasi mengikuti warna aksen.
- [x] Preferensi warna label disimpan lokal.
- [x] Label build beranda diperbarui ke v0.8.1.

## Batch v0.9.0
- [x] Ukuran teks label aplikasi dapat diatur dari 80% hingga 140%.
- [x] Ukuran teks label disimpan lokal dan dipulihkan saat launcher dibuka.
- [x] Label build beranda diselaraskan ke v0.9.0.

## Batch v0.9.1
- [x] Salam beranda dapat disembunyikan untuk tampilan lebih minimal.
- [x] Pilihan salam beranda disimpan secara lokal.
- [x] Label build beranda diselaraskan ke v0.9.1.

## Batch v0.9.2
- [x] Warna angka jam digital dapat mengikuti warna aksen.
- [x] Pilihan warna jam digital tersimpan otomatis.
- [x] Label build beranda diselaraskan ke v0.9.2.

## Batch v0.9.3
- [x] Tanggal pada widget jam beranda dapat ditampilkan atau disembunyikan.
- [x] Preferensi tampilan tanggal disimpan otomatis dan dipulihkan saat aplikasi dibuka.
- [x] Label build beranda diselaraskan ke v0.9.3.

## Batch v0.9.4
- [x] Ukuran jam digital di beranda dapat disesuaikan dari 80% hingga 140%.
- [x] Preferensi ukuran jam disimpan lokal dan dipulihkan saat launcher dibuka.
- [x] Label build beranda diselaraskan ke v0.9.4.

## Backlog fitur

### Home & personalisasi
1. [ ] Pilihan layout beranda
2. [ ] Grid ikon yang dapat diatur
3. [ ] Ukuran ikon terpisah untuk beranda
4. [x] Label ikon dapat disembunyikan (v0.7.0)
5. [ ] Warna teks ikon
6. [ ] Transparansi kartu
7. [ ] Efek glow aksen
8. [ ] Wallpaper dari galeri
9. [x] Wallpaper gradien bawaan (4 preset latar beranda, v0.8.0)
10. [ ] Wallpaper berubah terjadwal
11. [ ] Mode minimalis

### App drawer
12. [x] Favorit aplikasi
13. [ ] Bagian aplikasi terbaru
14. [x] Urutkan A–Z/Z–A
15. [ ] Urutkan berdasarkan terakhir dipakai
16. [x] Filter aplikasi sistem (v0.7.0)
17. [ ] Sembunyikan aplikasi pilihan
18. [ ] Kunci aplikasi dengan autentikasi
19. [ ] Grup aplikasi berdasarkan kategori
20. [ ] Grid/daftar aplikasi
21. [ ] Animasi drawer
22. [ ] Pencarian fuzzy

### Widget waktu
23. [x] Pilihan format tanggal
24. [ ] Zona waktu kedua
25. [ ] Jam dunia beberapa kota
26. [ ] Alarm berikutnya
27. [ ] Hitung mundur
28. [ ] Stopwatch ringkas
29. [x] Ukuran jam digital dapat disesuaikan (v0.9.4)
30. [ ] Warna jarum jam
31. [ ] Ketebalan jarum
32. [x] Tampilkan/sembunyikan tanggal di widget (v0.9.3)
33. [ ] Mode jam layar penuh

### Baterai & perangkat
34. [x] Status pengisian (v0.7.2)
35. [ ] Perkiraan waktu hingga penuh
36. [ ] Riwayat level baterai lokal
37. [ ] Peringatan baterai rendah
38. [ ] Widget penyimpanan
39. [ ] Ringkasan RAM jika diizinkan
40. [ ] Shortcut penggunaan baterai
41. [ ] Shortcut info perangkat
42. [ ] Status koneksi
43. [ ] Shortcut Bluetooth
44. [ ] Shortcut mode pesawat melalui sistem

### Cuaca
45. [ ] Cuaca live berbasis lokasi pilihan
46. [ ] Cuaca berdasarkan kota manual
47. [ ] Suhu terasa
48. [ ] Kelembapan
49. [ ] Kecepatan angin
50. [ ] Indeks UV
51. [ ] Peluang hujan
52. [ ] Prakiraan per jam
53. [ ] Prakiraan 7 hari
54. [ ] Penyegaran data manual
55. [ ] Cache cuaca offline

### Kalender & produktivitas
56. [ ] Agenda hari ini
57. [ ] Acara berikutnya
58. [ ] Hitung mundur tanggal penting
59. [ ] Hari libur Indonesia
60. [ ] Shortcut tambah acara
61. [ ] Daftar tugas sederhana
62. [ ] Checklist harian
63. [ ] Catatan cepat lokal
64. [ ] Pengingat berbasis notifikasi
65. [ ] Ringkasan agenda mingguan
66. [ ] Widget kalender mini

### Gestur & navigasi
67. [ ] Geser ke bawah untuk pencarian
68. [ ] Geser ke atas untuk drawer
69. [ ] Ketuk dua kali untuk layar/aksi yang didukung
70. [ ] Gestur beranda yang dapat dipetakan
71. [x] Tombol kembali konsisten
72. [ ] Dukungan keyboard fisik
73. [ ] Navigasi aksesibel
74. [ ] Umpan balik haptik opsional
75. [ ] Animasi transisi yang dapat dimatikan
76. [ ] Mode satu tangan
77. [ ] Shortcut aplikasi lewat gestur

### Keamanan & privasi
78. [ ] Kunci pengaturan launcher
79. [ ] PIN untuk fitur privat
80. [ ] Folder aplikasi privat
81. [ ] Konfirmasi reset pengaturan
82. [ ] Ekspor konfigurasi lokal
83. [ ] Impor konfigurasi lokal
84. [ ] Laporan izin yang dipakai
85. [ ] Tanpa telemetri default
86. [ ] Hapus data personal lokal
87. [ ] Sembunyikan konten notifikasi
88. [ ] Kunci aplikasi berbasis biometrik jika tersedia

### Backup & pemulihan
89. [ ] Backup preferensi ke berkas
90. [ ] Restore preferensi dari berkas
91. [ ] Reset tema ke default
92. [ ] Profil tema tersimpan
93. [ ] Ganti nama profil tema
94. [ ] Duplikasi profil tema
95. [ ] Ekspor daftar favorit
96. [ ] Impor daftar favorit
97. [ ] Riwayat perubahan konfigurasi
98. [ ] Peringatan sebelum menghapus profil
99. [ ] Validasi berkas impor

### Performa & aksesibilitas
100. [ ] Pengaturan animasi rendah
101. [ ] Kurangi efek transparansi
102. [ ] Kontras tinggi
103. [ ] Ukuran teks UI
104. [ ] Dukungan font skala sistem
105. [ ] Label aksesibilitas ikon
106. [ ] Target sentuh lebih besar
107. [ ] Mode hemat sumber daya
108. [ ] Muat ikon secara efisien
109. [x] Penanganan daftar aplikasi kosong
110. [x] Penanganan aplikasi yang tidak bisa dibuka

### Integrasi & pemeliharaan
111. [ ] Shortcut halaman sistem Android
112. [ ] Shortcut detail aplikasi
113. [ ] Shortcut izin aplikasi
114. [ ] Pemeriksaan versi launcher
115. [ ] Catatan rilis di dalam aplikasi
116. [ ] Halaman diagnostik lokal
117. [ ] Log error lokal opsional
118. [ ] Pemeriksaan konfigurasi rusak
119. [ ] Pemeriksaan update melalui sumber resmi
120. [ ] Pengelolaan izin cuaca
121. [ ] Paket tema tambahan

---

## Gerbang kualitas sebelum rilis
1. Build debug berhasil di GitHub Actions.
2. Periksa crash dan referensi resource.
3. Pastikan preferensi bertahan setelah aplikasi ditutup dan dibuka kembali.
4. Uji drawer, pencarian, indeks alfabet, jam, kalender, pengaturan Android, dan navigasi.
5. Jangan menganggap cuaca/notifikasi/fitur sistem aktif sebelum integrasi dan izin terkait benar-benar ada.
6. Signing APK final dikerjakan mendekati rilis agar update tidak bentrok dengan APK debug.
