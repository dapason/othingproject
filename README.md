# Analisis Proyek Stopwatch Compose

Dokumen ini berisi analisis mendalam dan blueprint dari proyek Stopwatch yang dibangun menggunakan Jetpack Compose. Proyek ini berfungsi sebagai aplikasi stopwatch fungsional dan juga sebagai sarana pembelajaran konsep-konsep fundamental dalam pengembangan Android modern.

##  Teknologi yang Digunakan

- **Bahasa:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Modern Declarative UI)
- **Desain:** [Material 3](https://m3.material.io/)
- **Asynchronous:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) untuk manajemen timer di latar belakang.
- **Arsitektur:** Berbasis UI State Management sederhana yang dikelola di dalam Composable.

---

## Blueprint & Arsitektur Aplikasi

Aplikasi ini dirancang dengan 3 lapisan utama:

### 1. Lapisan Data & State (Sumber Kebenaran)

State adalah data yang perlu diingat dan dapat berubah seiring waktu, yang akan memengaruhi UI.

- **`timeMs: Long`**: Menyimpan waktu utama stopwatch dalam milidetik.
- **`isRunning: Boolean`**: Melacak status stopwatch (berjalan/dijeda).
- **`lapTimes: List<Long>`**: Daftar untuk menyimpan semua catatan waktu putaran.
- **`elapsedPauseTime: Long`**: State internal untuk menyimpan waktu yang telah berlalu saat tombol PAUSE ditekan.
- **`startTime: Long`**: State internal untuk menyimpan timestamp sistem saat timer dimulai.

### 2. Lapisan Logika (Otak Aplikasi)

Fungsi-fungsi yang memanipulasi state berdasarkan interaksi pengguna.

- **`startTimer()`**: Memulai/melanjutkan loop coroutine untuk menghitung `timeMs`.
- **`pauseTimer()`**: Menghentikan loop coroutine dan menyimpan waktu terakhir ke `elapsedPauseTime`.
- **`resetTimer()`**: Menghentikan timer dan mengatur ulang semua state ke nilai default.
- **`recordLap()`**: Menambahkan nilai `timeMs` saat ini ke dalam daftar `lapTimes`.

### 3. Lapisan UI (Tampilan Visual)

Komponen Jetpack Compose yang bereaksi terhadap perubahan state.

- **Tampilan Waktu (`Text`)**: Menampilkan `timeMs` yang telah diformat. Otomatis update saat `timeMs` berubah.
- **Tombol Kontrol (`Button`)**: Teks dan aksinya (`onClick`) berubah secara dinamis berdasarkan state `isRunning`.
- **Daftar Putaran (`LazyColumn`)**: Secara efisien menampilkan setiap item dari `lapTimes`. Otomatis menambah/menghapus baris saat daftar `lapTimes` berubah.

---

## Konsep Kunci & Penjelasan Kode

Berikut adalah beberapa konsep fundamental yang digunakan dalam proyek ini:

### Keyword Dasar Kotlin
- **`class`**: Cetak biru untuk membuat objek (misal: `class MainActivity`).
- **`fun`**: Mendefinisikan sebuah fungsi/perilaku (misal: `fun startTimer()`).
- **`var` (Variable)**: "Papan tulis", wadah data yang nilainya bisa diubah.
- **`val` (Value)**: "Prasasti batu", wadah data yang nilainya tidak bisa diubah setelah ditentukan.
- **`super`**: Merujuk pada implementasi dari kelas induk (parent class).

### Konsep Android & Compose
- **`Job`**: Representasi sebuah tugas yang berjalan di Coroutine. Bisa digunakan untuk membatalkan tugas tersebut.
- **`Bundle`**: "Koper" data yang digunakan Android untuk menyimpan state Activity atau mengirim data antar layar.
- **`?` (Nullable Type)**: Menandakan bahwa sebuah variabel diizinkan untuk bernilai `null` (kosong), membuat kode lebih aman dari `NullPointerException`.
- **`Toast`**: Notifikasi pop-up sederhana untuk memberi feedback singkat kepada pengguna.
- **`LazyColumn`**: Komponen daftar yang efisien, karena hanya merender item yang terlihat di layar (prinsip daur ulang/recycling).
- **`remember { mutableStateOf(...) }`**: Perintah inti di Compose untuk menciptakan "State" yang reaktif. `remember` membuat nilainya bertahan saat UI digambar ulang, dan `mutableStateOf` memastikan UI akan otomatis update saat nilainya berubah.

### Fokus pada Android Activity Lifecycle
Proyek ini secara eksplisit meng-override fungsi-fungsi lifecycle (`onCreate`, `onResume`, `onPause`, `onDestroy`, dll.) dan menambahkan `Log` di dalamnya. Ini adalah teknik fundamental untuk mempelajari dan men-debug bagaimana sebuah Activity berperilaku saat dibuat, dijeda, dilanjutkan, atau dihancurkan oleh sistem Android.

---

## Langkah Pembangunan dari Awal

1.  **Definisikan State**: Tentukan semua data yang perlu diingat menggunakan `remember { mutableStateOf(...) }`.
2.  **Bangun UI Statis**: Rancang layout visual menggunakan Composable (`Column`, `Row`, `Text`, `Button`).
3.  **Implementasikan Logika**: Tulis fungsi-fungsi yang memanipulasi state (`startTimer`, `pauseTimer`, dll.).
4.  **Hubungkan Logika ke UI**: Sambungkan fungsi logika ke `onClick` pada `Button` dan tampilkan nilai state di `Text`.
5.  **Integrasi & Poles**: Panggil Composable utama dari `MainActivity` dan tambahkan sentuhan akhir seperti `Toast`.
