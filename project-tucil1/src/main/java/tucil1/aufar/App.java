package tucil1.aufar;

import java.util.Scanner;

import tucil1.aufar.controllers.InputHandler;

public class App {
    public static void main(String[] args) {
        InputHandler inputHandler = new InputHandler() ;

        try (Scanner scan = new Scanner(System.in)) {
            System.out.print("Masukkan nama file test case (.txt): ");
            String filename = scan.nextLine() ;


            if (!inputHandler.readFile(filename)){
                System.out.println(inputHandler.getErrorMessage());
                return;
            }

            System.out.println("Board berhasil dibaca!") ;
            System.out.println("Ukuran papan: " + inputHandler.getSize());

        } 

        char[][] board = inputHandler.getBoard() ;
        int n = inputHandler.getSize();

        System.out.println("\n Konfigurasi Awal Papan: ");
        for (int i = 0 ; i < n ; i++){
            for (int j = 0 ; j < n ; j++){
                System.out.print(board[i][j]) ;
            }
            System.out.println() ;
        }
    }
}
