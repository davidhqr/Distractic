package tech.drivesmart.drivesmart.util;

public class Utils {
    public static boolean isEmpty(String... strs) {
        for (String str : strs) {
            if (str.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getEmpty(String... strs) {
        List<String> emptyStrList = new ArrayList<>();
        for (String str : strs) {
            if (str.isEmpty()) {
                emptyStrList.add(str);
            }
        }
        return emptyStrList;
    }

    public static boolean isInt(String str) {
        try {
            Integer.parseInt(str);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }


}
