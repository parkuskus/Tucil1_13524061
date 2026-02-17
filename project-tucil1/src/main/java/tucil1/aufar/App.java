package tucil1.aufar;

import java.util.Scanner;

import tucil1.aufar.controllers.IOHandler;
import tucil1.aufar.models.BruteForce;
import tucil1.aufar.views.MainGUI;

public class App {
    public static void main(String[] args) {
        // Check for GUI mode
        if (args.length > 0 && (args[0].equals("--gui") || args[0].equals("-g"))) {
            MainGUI.main(args);
            return;
        }
        
        // CLI mode
        IOHandler ioHandler = new IOHandler();
        Scanner scan = new Scanner(System.in);

        System.out.println("=== Queens Game Solver ===");
        System.out.println("Pilih mode:");
        System.out.println("1. CLI (Command Line)");
        System.out.println("2. GUI (Graphical Interface)");
        System.out.print("Pilihan (1/2): ");
        
        String modeChoice = scan.nextLine().trim();
        
        if (modeChoice.equals("2")) {
            scan.close();
            MainGUI.main(args);
            return;
        }

        System.out.print("\nMasukkan nama file test case (.txt): ");
        String filename = scan.nextLine();

        if (!ioHandler.readFile(filename)) {
            System.out.println(ioHandler.getErrorMessage());
            scan.close();
            return;
        }

        System.out.println("Board berhasil dibaca!");
        System.out.println("Ukuran papan: " + ioHandler.getSize());

        char[][] board = ioHandler.getBoard();
        int n = ioHandler.getSize();

        System.out.println("\nKonfigurasi Awal Papan: ");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(board[i][j]);
            }
            System.out.println();
        }

        System.out.println("\nSolusi (Brute-force):");
        BruteForce bf = new BruteForce(n);
        String result = bf.solve(board);

        // Opsi simpan ke file
        System.out.print("\nApakah ingin menyimpan solusi ke file? (y/n): ");
        String saveChoice = scan.nextLine().trim().toLowerCase();
        
        if (saveChoice.equals("y") || saveChoice.equals("yes")) {
            System.out.print("Masukkan nama file output (contoh: output.txt): ");
            String outputFilename = scan.nextLine().trim();
            
            if (ioHandler.writeToFile(outputFilename, result)) {
                System.out.println("Solusi berhasil disimpan ke results/" + outputFilename);
            } else {
                System.out.println(ioHandler.getErrorMessage());
            }
        }

        scan.close();
    }
}
