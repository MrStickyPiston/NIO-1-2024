package A;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class HippoDiner {
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static int MaxPlates(int K, int[] plates) {
        Arrays.sort(plates);
        int maxPlates = 0;
        int lastPlate = -1;

        for (int plate : plates) {
            if (lastPlate == -1 || plate - lastPlate >= K) {
                maxPlates++;
                lastPlate = plate;
            }
        }

        return maxPlates;
    }

    public static void main(String[] args) throws IOException {

        int N = Integer.parseInt(reader.readLine());
        int K = Integer.parseInt(reader.readLine());
        int[] plates = new int[N];

        for (int i = 0; i < N; i++){
            plates[i] = Integer.parseInt(reader.readLine());
        }

        System.out.println(MaxPlates(K, plates));
    }
}
