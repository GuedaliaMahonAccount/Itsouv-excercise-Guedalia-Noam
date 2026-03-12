import java.util.Arrays;

public class SeasonUtil {
    public static final Season[] SEASONS = Season.values();
    public static void sortSeasonable(Comparable[] seasonables) {
        Arrays.sort(seasonables);
    }


    static String reportAll(Seasonable[] seasonables) {
        String result = "";

        for (int i = 0; i < seasonables.length; i++) {
            result += seasonables[i].toString();

            if (i < seasonables.length - 1) {
                result += "\n";
            }
        }
        return result;
    }

}
