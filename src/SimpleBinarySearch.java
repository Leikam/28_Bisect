import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SimpleBinarySearch {

    public static void main(String[] args) {
        // test
        List<String> strings = List.of("a", "b", "c", "d", "e");
        System.out.println("b - " + searchRight("b", strings, (a) -> a.equals("b")));
        System.out.println("e - " + searchRight("e", strings, (a) -> a.equals("e")));
//        System.out.println("a - " + searchRight("a", strings, (a) -> a.equals("a")));
//        System.out.println("null - " + searchRight("a", strings, (a) -> a.equals("f")));
//        System.out.println("l: 3 - " + searchLeft(6, strings));
    }

    public static String searchRight(String x, List<String> range, Function<String, Boolean> predicate) {
        int le = 0;
        int ri = range.size();

        while (ri > le + 1) {
            int mid = (le + ri) / 2;
            if (predicate.apply(range.get(mid))) {
                 le = mid;
            } else {
                ri = mid;
            }
        }


        if (range.get(le).equals(x)) {
            return x;
        }

        return null;
    }

    public static int searchLeft(int x, int[] range) {
        int le = 0;
        int ri = range.length;

        while (ri > le + 1) {
            int mid = (le + ri) / 2;
            if (range[mid] >= x) {
                ri = mid;
            } else {
                le = mid;
            }
        }

        if (range[ri] == x) {
            return ri;
        }

        return -1;
    }

}
