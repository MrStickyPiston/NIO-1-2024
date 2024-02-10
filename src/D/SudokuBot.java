package D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.exit;

public class SudokuBot {
    private static final Solver solver = new Solver();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args){

        long start = currentTimeMillis();

        gameLoop(scanner.nextLine());

        System.err.println(Utils.toMove(0, 0, 1));
        System.err.println(Arrays.toString(Utils.parseMove(Utils.toMove(0, 0, 1))));

        exit(0);


        System.out.println(solver.nextMove());

        System.err.println(Arrays.deepToString(solver.grid));
        System.err.println(Arrays.deepToString(solver.solutions.toArray()));

        System.err.println(currentTimeMillis()-start);
    }

    public static void gameLoop(String move) {
        int[] moveData;


        if (Objects.equals(move, "start")){
            System.out.println("Fb2");
            solver.grid[5][1] = 2;
        } else {
            moveData = Utils.parseMove(move);

            solver.grid[moveData[0]][moveData[1]] = moveData[2];
        }

        while (true){
            System.err.println(Arrays.deepToString(solver.grid));
            move = scanner.nextLine();
            moveData = Utils.parseMove(move);
            solver.grid[moveData[0]][moveData[1]] = moveData[2];

            move = solver.nextMove();

            System.out.println(move);
        }
    }
}

class Solver {

    public int[][] grid = new int[9][9];

    public ArrayList<int[][]> solutions = new ArrayList<>();

    public Solver(){}

    private static boolean check(int[][] grid, int row, int col, int num){

        for (int x = 0; x <= 8; x++)
            if (grid[row][x] == num)
                return false;

        for (int x = 0; x <= 8; x++)
            if (grid[x][col] == num)
                return false;

        int startRow = row - row % 3, startCol = col - col % 3;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (grid[i + startRow][j + startCol] == num)
                    return false;

        return true;
    }

    private boolean solveSudoku(int[][] grid, int row, int col){

        if (row == 9 - 1 && col ==  9)
            return true;

        if (col ==  9) {
            row++;
            col = 0;
        }

        if (grid[row][col] != 0)
            return solveSudoku(grid, row, col + 1);

        for (int num = 1; num < 10; num++) {
            if (check(grid, row, col, num)) {
                grid[row][col] = num;
                if (solveSudoku(grid, row, col + 1)){

                    if (solutions.size() >= 2) {
                        return true;
                    } else {
                        solutions.add(Utils.deepCopy(grid));
                    }
                }

            }

            grid[row][col] = 0;
        }

        return false;
    }

    public String nextMove(){
        solutions.clear();
        solveSudoku(Utils.deepCopy(grid), 0, 0);

        if (solutions.size() <= 1){
            return "!";
        }

        int[][] solutionA = solutions.get(0);
        int[][] solutionB = solutions.get(1);

        for (int row = 0; row < 9; row++){
            for (int col = 0; col < 9; col++){
                if (solutionA[row][col] != solutionB[row][col]){
                    System.err.printf("r%d c%d a=%s b=%s %n", row, col, solutionA[row][col], solutionB[row][col]);
                    grid[row][col] = solutionA[row][col];
                    return Utils.toMove(row, col, solutionA[row][col]);
                }
            }
        }

        return "!";
    }

}

class Utils {
    public static int[][] deepCopy(int[][] matrix) {
        return Arrays.stream(matrix)
                .map(int[]::clone)
                .toArray(s -> matrix.clone());
    }

    public static String toMove(int row, int col, int n){
        return "%C%c%c".formatted((char) ('A' + row), (char) ('a' + col), (char) ('0' + n));
    }

    public static int[] parseMove(String move){
        return new int[]{(move.charAt(0) - 'A'), (move.charAt(1) - 'a'), (move.charAt(2) - '0')};
    }

    private static int toNumeric(char c){
        if (Character.isDigit(c)) {
            return Character.getNumericValue(c);
        }
        return c - 'a' + 10;
    }

    private static char toChar(int n){
        return (char) ('a' + n);
    }
}