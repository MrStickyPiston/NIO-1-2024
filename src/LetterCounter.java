import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LetterCounter {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String letters = reader.readLine();

        System.out.println(Count(letters));
    }

    private static String Count(String letters) {

        int l = letters.length();
        Map<Character, Integer> counts = new HashMap<Character, Integer>();

        for (int i = 0; i < l; i++) {
            counts.put(letters.charAt(i), counts.getOrDefault(letters.charAt(i), 0) + 1);
        }

        int maxCount = Integer.MIN_VALUE;
        List<Character> results = new ArrayList<>();

        for (Map.Entry<Character, Integer> letter : counts.entrySet()) {
            int count = letter.getValue();

            if (count > maxCount){
                maxCount = count;
                results.clear();
                results.add(letter.getKey());
            } else if (count == maxCount){
                results.add(letter.getKey());
            }

        }

        Collections.sort(results);

        StringBuilder result = new StringBuilder();
        for (char c : results) {
            result.append(c);
        }

        return result.toString();
    }
}
