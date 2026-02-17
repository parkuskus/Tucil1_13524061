package tucil1.aufar.models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class BruteForce {    
    int n ; 
    int[] queens ;
    List<String> solutions;
    List<int[]> solutionQueens;
    
    // Stats
    private long searchTimeMs;
    private long casesCheckedTotal;
    
    // Callbacks for GUI visualization
    private VisualizationCallback visualizationCallback;
    private BiConsumer<String, Integer> solutionCallback;
    
    // Functional interface for visualization callback
    @FunctionalInterface
    public interface VisualizationCallback {
        void onUpdate(int[] queens, long casesChecked, double progress);
    }

    public BruteForce(int n) {
        this.n = n;
        this.queens = new int[n];
        this.solutions = new ArrayList<>();
        this.solutionQueens = new ArrayList<>();
    }
    
    public void setVisualizationCallback(VisualizationCallback callback) {
        this.visualizationCallback = callback;
    }
    
    public void setSolutionCallback(BiConsumer<String, Integer> callback) {
        this.solutionCallback = callback;
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
        solutionQueens.clear();
        StringBuilder result = new StringBuilder();
        
        long totalCase = (long) Math.pow(n, n);
        int[] liveUpdate = {10, 20, 30, 40, 50, 60, 70, 80, 90} ;
        int update = 0 ;
        long visualizationInterval = Math.max(1, totalCase / 100); // Update every 1%
        
        do {
            casesChecked++;
            
            // Live visualization callback
            if (visualizationCallback != null && casesChecked % visualizationInterval == 0) {
                double progress = (casesChecked * 100.0) / totalCase;
                visualizationCallback.onUpdate(queens.clone(), casesChecked, progress);
            }
            
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
                solutionQueens.add(queens.clone());
                
                // Solution callback for GUI
                if (solutionCallback != null) {
                    solutionCallback.accept(solution, solutions.size());
                }
            }
        } while (nextState() && solutions.size() < 1);

        long endTime = System.nanoTime();
        searchTimeMs = (endTime - startTime) / 1_000_000;
        casesCheckedTotal = casesChecked;

        // Cetak semua solusi di akhir
        if (solutions.size() < 1){
            System.out.println("Tidak Ada Solusi!");
            result.append("Tidak Ada Solusi!") ;
        } else {
            System.out.println("\nSOLUSI AKHIR");
            result.append("\nSOLUSI AKHIR\n") ;
            System.out.print(solutions.get(0) + "\n");
            result.append(solutions.get(0)).append("\n");
        }


        String stats = "Waktu pencarian: " + searchTimeMs + " ms\n" +
                       "Banyak kasus yang ditinjau: " + casesChecked + " kasus\n";
        
        System.out.println(stats);
        result.append(stats);
        
        return result.toString();
    }
    
    // Getter methods for GUI
    public int getSolutionCount() {
        return solutions.size();
    }
    
    public long getSearchTimeMs() {
        return searchTimeMs;
    }
    
    public long getCasesChecked() {
        return casesCheckedTotal;
    }
    
    public int[] getFirstSolutionQueens() {
        if (solutionQueens.isEmpty()) {
            return null;
        }
        return solutionQueens.get(0);
    }
    
    public List<int[]> getAllSolutionQueens() {
        return solutionQueens;
    }

}
