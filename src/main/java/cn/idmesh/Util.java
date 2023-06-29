package cn.idmesh;

public class Util {

    static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
