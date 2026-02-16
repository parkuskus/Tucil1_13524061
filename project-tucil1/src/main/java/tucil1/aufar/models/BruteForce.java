package tucil1.aufar.models;

import java.util.ArrayList;
import java.util.List;

public class BruteForce {    
    int n ; 
    int[] queens ;
    List<String> solutions;

    public BruteForce(int n) {
        this.n = n;
        this.queens = new int[n];
        this.solutions = new ArrayList<>();
    }

    private boolean nextState(){
        for (int i = n-1 ; i >= 0 ; i--){
            if (queens[i] < n-1){
                queens[i]++ ;
                return true ;
            } else {
                queens[i] = 0 ;
            }

        }
        return false ;
    }

    private boolean isValid(char[][] board){
        for(int i = 0 ; i < n ; i++){
            for (int j = i+1 ; j < n ; j++){
                // Same row or same color/region -> invalid
                if (this.queens[i] == this.queens[j] 
                        || board[queens[i]][i] == board[queens[j]][j]){
                    return false;
                }
                // Adjacent check: kolom bersebelahan (|i-j|==1) dan baris berbeda maksimal 1
                // Ini mencakup horizontal adjacent (baris sama) dan diagonal adjacent (baris ±1)
                if (Math.abs(i - j) == 1 && Math.abs(queens[i] - queens[j]) <= 1){
                    return false;
                }
            }
        }
        return true ;
    }

    private String getSolutionString(char[][] board){
        StringBuilder sb = new StringBuilder();
        for (int row = 0 ; row < n ; row++){
            for (int col = 0 ; col < n ; col++){
                if (queens[col] == row){
                    sb.append('#');
                } else {
                    sb.append(board[row][col]);
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String solve(char[][] board){
        long startTime = System.nanoTime();
        long casesChecked = 0;
        solutions.clear();
        StringBuilder result = new StringBuilder();
        
        long totalCase = (long) Math.pow(n, n);
        int[] liveUpdate = {10, 20, 30, 40, 50, 60, 70, 80, 90} ;
        int update = 0 ;
        do {
            casesChecked++;
            if (update < liveUpdate.length){
                if ((casesChecked * 100 / totalCase) > liveUpdate[update] ){
                    System.out.println("Update Ke-" + (update+1)) ;
                    result.append("Update Ke-").append(update+1).append("\n") ;
                    String boardState = getSolutionString(board);
                    System.out.print(boardState);
                    result.append(boardState).append("\n");
                    System.out.println() ;
                    update++;
                }
            }
            if (isValid(board)){
                String solution = getSolutionString(board);
                solutions.add(solution);
            }
        } while (nextState());

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        // Cetak semua solusi di akhir
        System.out.println("\nSOLUSI AKHIR YANG MUNGKIN\n");
        result.append("\nSOLUSI AKHIR YANG MUNGKIN\n") ;

        for (int i = 0; i < solutions.size(); i++) {
            System.out.println("Solusi " + (i + 1) + ":");
            result.append("Solusi ").append(i + 1).append(":").append("\n") ;
            System.out.print(solutions.get(i));
            result.append(solutions.get(i)).append("\n");
            System.out.println();
        }

        String stats = "Total solusi: " + solutions.size() + "\n" +
                       "Waktu pencarian: " + durationMs + " ms\n" +
                       "Banyak kasus yang ditinjau: " + casesChecked + " kasus\n";
        
        System.out.println(stats);
        result.append(stats);
        
        return result.toString();
    }

}
