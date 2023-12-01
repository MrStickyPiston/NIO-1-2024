package A;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class LaserCutter {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        int n = Integer.parseInt(reader.readLine());
        String cutA = reader.readLine();
        String cutB = reader.readLine();

        System.out.println(GetLargestSquare(n, cutA, cutB));
    }

    private static void CutGrid(String cut, int fillType, int[][] grid){
        int x = 0;
        int y = 0;

        for(char move : cut.toCharArray()) {
            if (move == '0'){
                x++;
            } else if (move == '1') {
                y++;
            }
            for (int i = 0; i < y; i++){
                try {
                    grid[i][x] = fillType;
                } catch (ArrayIndexOutOfBoundsException ignored) { }
            }
        }
    }

    public static int LargestSquareInGrid(int[][] grid) {

        int rows = grid.length;
        int cols = grid[0].length;
        int[][] matrix = new int[rows][cols];
        int maxSize = 0;

        for (int i = 0; i < rows; i++) {
            matrix[i][0] = grid[i][0];
            maxSize = Math.max(maxSize, matrix[i][0]);
        }
        for (int j = 0; j < cols; j++) {
            matrix[0][j] = grid[0][j];
            maxSize = Math.max(maxSize, matrix[0][j]);
        }

        for (int i = 1; i < rows; i++) {
            for (int j = 1; j < cols; j++) {
                if (grid[i][j] == 1) {
                    matrix[i][j] = Math.min(matrix[i - 1][j - 1], Math.min(matrix[i - 1][j], matrix[i][j - 1])) + 1;
                    maxSize = Math.max(maxSize, matrix[i][j]);
                }
            }
        }

        return maxSize;
    }

    private static int GetLargestSquare(int n, String cutA, String cutB){
        int[][] grid = new int[n][n];

        CutGrid(cutA, 1, grid);
        CutGrid(cutB, 0, grid);

        return LargestSquareInGrid(grid);
    }
}