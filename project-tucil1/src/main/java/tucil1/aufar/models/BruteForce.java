package tucil1.aufar.models;

public class BruteForce {    
    int n ; 
    int[] queens ;

    public BruteForce(int n) {
        this.n = n;
        this.queens = new int[n]; 
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
        int count = 0;
        StringBuilder result = new StringBuilder();
        
        do {
            casesChecked++;
            if (isValid(board)){
                String solution = getSolutionString(board);
                System.out.print(solution);
                result.append(solution).append("\n");
                count++;
            }
        } while (nextState());

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        String stats = "Total solusi: " + count + "\n" +
                       "Waktu pencarian: " + durationMs + " ms\n" +
                       "Banyak kasus yang ditinjau: " + casesChecked + " kasus\n";
        
        System.out.println(stats);
        result.append(stats);
        
        return result.toString();
    }

}
