import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapTest {

    public static void main(String[] args) {

        Map<Integer, String> testMap = new HashMap<>();
        Set<Integer> testSet = new HashSet<>();
        testSet.add(2);
        testSet.add(4);
        testSet.add(5);

        testMap.put(1, "abc");
        testMap.put(2, "def");
        testMap.put(3, "dsdf");
        testMap.put(4, "dfsf");

        testMap.keySet().removeIf(key -> !testSet.contains(key));
        System.out.println(testMap);
    }

}
