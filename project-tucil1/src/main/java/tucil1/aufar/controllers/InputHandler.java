package tucil1.aufar.controllers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputHandler {

    private char[][] board;
    private int N;

    public void readFile(String filename) throws IOException {
    List<char[]> tempBoard = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    tempBoard.add(line.toCharArray());
                }
            }
        }

        N = tempBoard.size();
        board = new char[N][];

        for (int i = 0; i < N; i++) {
            board[i] = tempBoard.get(i);
        }
    }

    public boolean isSquare() {
        if (board == null) return false;

        for (int i = 0; i < N; i++) {
            if (board[i].length != N) {
                return false;
            }
        }

        return true;
    }

    public char[][] getBoard() {
        return board;
    }

    public int getSize() {
        return N;
    }

    public void printBoard() {
        if (board == null) {
            System.out.println("Board kosong.");
            return;
        }

        for (int i = 0; i < N; i++) {
            System.out.println(new String(board[i]));
        }
    }
}
