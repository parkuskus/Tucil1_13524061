package tucil1.aufar.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class IOHandler {

    private char[][] board;
    private int N;
    private String errorMessage = "";

    public boolean readFile(String filename) {
        List<char[]> tempBoard = new ArrayList<>();

        File file = new File(filename) ;

        if (!file.exists()) {
            file = new File("test/" + filename);
        }

        // Coba naik satu folder
        if (!file.exists()) {
            file = new File("../test/" + filename);
        }

        if (!file.exists()) {
            errorMessage = "File tidak ditemukan: " + filename;
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    tempBoard.add(line.toCharArray());
                }
            }

        } catch (IOException e) {
            errorMessage = "Gagal membaca file: " + e.getMessage();
            return false;
        }

        if (tempBoard.isEmpty()) {
            errorMessage = "File kosong.";
            return false;
        }

        N = tempBoard.size();
        board = new char[N][];

        for (int i = 0; i < N; i++) {
            board[i] = tempBoard.get(i);
        }

        if (!isSquare()) {
            errorMessage = "Papan tidak berbentuk persegi (N x N).";
            return false;
        }

        return true;
    }

    private boolean isSquare() {
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean writeToFile(String filename, String content) {
        File outputDir = new File("test");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File file = new File(outputDir, filename);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(content);
            return true;
        } catch (IOException e) {
            errorMessage = "Gagal menulis file: " + e.getMessage();
            return false;
        }
    }
}
