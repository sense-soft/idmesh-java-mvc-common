package cn.idmesh;

import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class Util {

    static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static String urlBuilder(String url, String... paths) {
        try {
            return new URIBuilder(url).appendPathSegments(paths).build().toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The domain had an invalid format and couldn't be parsed as an URL.");
        }
    }

    public static String urlBuilder(URI url, String... paths) {
        try {
            return new URIBuilder(url).appendPathSegments(paths).build().toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The domain had an invalid format and couldn't be parsed as an URL.");
        }
    }
}
