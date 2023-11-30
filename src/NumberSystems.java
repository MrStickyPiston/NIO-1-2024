import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class NumberSystems {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int sourceBase = Integer.parseInt(reader.readLine());
        String sourceNumber = reader.readLine();
        int outputBase = Integer.parseInt(reader.readLine());

        System.out.println(translate(sourceNumber, sourceBase, outputBase));
    }

    private static int toNumeric(char c){
        if (Character.isDigit(c)) {
            return Character.getNumericValue(c);
        }
        return c - 'a' + 10;
    }

    private static char toChar(int n){
        if (n >= 10 && n <= 35) {
            return (char) ('a' + (n - 10));
        } else {
            return (char)(n+'0');
        }
    }

    private static String translate(String sourceNumber, int sourceBase, int outputBase) {
        ArrayList<Integer> n = new ArrayList<Integer>();

        char[] temp = sourceNumber.toCharArray();

        for (char c : temp) {
            n.add(toNumeric(c));
        }

        Collections.reverse(n);

        int base10 = 0;

        for (int i = 0; i < n.size(); i++){
            base10 += (int) (n.get(i) * Math.pow(sourceBase, i));
        }

        StringBuilder baseXStringBuilder = new StringBuilder();

        while (base10 > 0) {
            int remainder = base10 % outputBase;
            base10 /= outputBase;

            baseXStringBuilder.append(toChar(remainder));
        }
        return baseXStringBuilder.reverse().toString();
    }
}
