import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class HippoDiner {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int lines = Integer.parseInt(reader.readLine());
        int distance = Integer.parseInt(reader.readLine());

        ArrayList<Integer> plates = new ArrayList<>();

        for (int i = 0; i < lines; i++){
            plates.add(Integer.parseInt(reader.readLine()));
        }

        System.out.println(GetHippos(distance, plates));
    }

    private static List<Integer> GetNeighbours(ArrayList<Integer> plates, int distance, int from){
        Set<Integer> resultSet = new HashSet<>();

        for (int plate: plates){
            if(from - distance < plate && plate < from + distance && plate != from){
                resultSet.add(plate);
            }
        }

        Integer[] result = new Integer[resultSet.size()];
        resultSet.toArray(result);

        return new ArrayList<>(Arrays.asList(result));
    }

    private static <K, V> Map.Entry<K, List<V>> LongestEntry(HashMap<K, List<V>> map){
        Map.Entry<K, List<V>> longestEntry = null;
        for (Map.Entry<K, List<V>> entry : map.entrySet()) {
            if (longestEntry == null || entry.getValue().size() > longestEntry.getValue().size()) {
                longestEntry = entry;
            }
        }

        return longestEntry;
    }

    private static int GetHippos(int distance, ArrayList<Integer> plates) {
        HashMap<Integer, List<Integer>> platesMap = new HashMap<>();

        for(int plate: plates){
            platesMap.put(plate, GetNeighbours(plates, distance, plate));
        }

        Map.Entry<Integer, List<Integer>> longestEntry = LongestEntry(platesMap);

        while (!longestEntry.getValue().isEmpty()){
            int plate = longestEntry.getKey();

            for (Map.Entry<Integer, List<Integer>> entry : platesMap.entrySet()){
                List<Integer> neighbours = entry.getValue();
                neighbours.removeIf(e -> e.equals(plate));
            }

            platesMap.remove(plate);

            longestEntry = LongestEntry(platesMap);
            if (longestEntry == null){
                break;
            }
        }

        return platesMap.size();
    }
}
