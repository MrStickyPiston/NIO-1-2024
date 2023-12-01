package A;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CalcDistance {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int k = Integer.parseInt(reader.readLine());
        String moves = reader.readLine();

        System.out.println(Calculate(k, moves));
    }

    public static int Calculate(int k, String moves){
        int x = 0;
        int y = 0;

        for (int i = 0; i < k; i++) {
            switch (moves.charAt(i)){
                case 'N':
                    y++;
                    break;
                case 'O':
                    x++;
                    break;
                case 'Z':
                    y--;
                    break;
                case 'W':
                    x--;
                    break;
            }
        }

        return Math.abs(x) + Math.abs(y);
    }
}