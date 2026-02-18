# Queens Game Solver

Program solver untuk permainan **Queens Game** dari LinkedIn, dibuat menggunakan bahasa Java dengan pendekatan algoritma **Brute Force**.

## Deskripsi Program

Queens Game adalah permainan puzzle di mana pemain harus menempatkan ratu (queen) pada papan berukuran N×N dengan aturan:
- Setiap baris harus memiliki tepat satu ratu
- Setiap kolom harus memiliki tepat satu ratu  
- Setiap region (area berwarna) harus memiliki tepat satu ratu
- Ratu tidak boleh bersebelahan (termasuk diagonal)

Program ini menyelesaikan puzzle tersebut menggunakan algoritma brute force dengan kompleksitas O(N^N), dan menyediakan visualisasi langsung proses pencarian solusi.

### Fitur
- **Mode CLI**: Input dari file teks, output ke terminal
- **Mode GUI**: Antarmuka grafis dengan JavaFX
  - Input dari file TXT atau gambar (screenshot)
  - Visualisasi live proses brute force
  - Ekspor solusi ke TXT atau gambar (PNG/JPG)
  - Deteksi warna otomatis dari gambar menggunakan HSB color space

## Requirements

| Komponen | Versi |
|----------|-------|
| Java JDK | 17+ |
| Apache Maven | 3.6+ |
| JavaFX | 17.0.8 (otomatis via Maven) |

## Instalasi

1. Clone repository:
   ```bash
   git clone https://github.com/username/tucil1-queens-game-simulation.git
   cd tucil1-queens-game-simulation
   ```

2. Pastikan Java 17+ terinstall:
   ```bash
   java -version
   ```

3. Pastikan Maven terinstall:
   ```bash
   mvn -version
   ```

## Kompilasi

Masuk ke direktori project dan compile:

```bash
cd project-tucil1
mvn clean compile
```

## Cara Menjalankan

### Mode Interaktif (CLI/GUI Selection)
```bash
cd project-tucil1
mvn exec:java
```
Program akan menampilkan menu untuk memilih mode CLI atau GUI.

### Langsung ke GUI Mode
```bash
mvn exec:java -Dexec.args="--gui"
```

### Mode CLI
1. Pilih opsi 1 (CLI) saat menu muncul
2. Masukkan path file test case (contoh: `../test/test1.txt`)
3. Program akan menampilkan solusi
4. Pilih untuk menyimpan hasil ke file jika diinginkan

### Mode GUI
1. Pilih opsi 2 (GUI) atau jalankan dengan `--gui`
2. Klik "Load TXT" untuk input dari file teks, atau "Load Image" untuk input dari screenshot
3. Untuk input gambar, atur grid overlay agar sesuai dengan papan
4. Klik "Solve" untuk menjalankan solver
5. Gunakan "Save TXT" atau "Save Image" untuk menyimpan hasil

## Format Input File TXT

```
<ukuran_papan>
<baris_1>
<baris_2>
...
<baris_n>
```

**Contoh (5×5):**
```
5
AAABB
ACCCB
DCCCE
DDDEE
DDDEE
```

Setiap huruf merepresentasikan region berbeda.

## Struktur Project

```
project-tucil1/
├── src/main/java/tucil1/aufar/
│   ├── App.java                 # Entry point
│   ├── controllers/
│   │   └── IOHandler.java       # File I/O handling
│   ├── models/
│   │   ├── BruteForce.java      # Algoritma solver
│   │   └── ColorRegionExtractor.java  # Ekstraksi region dari gambar
│   └── views/
│       ├── MainGUI.java         # GUI utama
│       └── ImageConfigDialog.java  # Dialog konfigurasi gambar
└── pom.xml
```

## Author

| Nama | NIM |
|------|-----|
| Muhammad Aufar Rizqi Kusuma | 13524061 |

**Institut Teknologi Bandung - Informatika**  
Tugas Kecil 1 - IF2211 Strategi Algoritma
