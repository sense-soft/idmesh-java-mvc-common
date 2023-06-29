package cn.idmesh;

import org.apache.commons.lang3.Validate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

class AuthCookie {

    private final static int MAX_AGE_SECONDS = 600; // 10 minutes

    private final String key;
    private final String value;
    private boolean secure;
    private SameSite sameSite;

    AuthCookie(String key, String value) {
        Validate.notNull(key, "Key must not be null");
        Validate.notNull(value, "Value must not be null");

        this.key = key;
        this.value = value;
    }

    void setSecure(boolean secure) {
        this.secure = secure;
    }

    void setSameSite(SameSite sameSite) {
        this.sameSite = sameSite;
    }

    String buildHeaderString() {
        String baseCookieString = String.format("%s=%s; HttpOnly; Max-Age=%d", encode(key), encode(value), MAX_AGE_SECONDS);
        if (sameSite != null) {
            baseCookieString = baseCookieString.concat(String.format("; SameSite=%s", encode(sameSite.getValue())));
        }
        if (secure) {
            baseCookieString = baseCookieString.concat("; Secure");
        }
        return baseCookieString;
    }

    private static String encode(String valueToEncode) {
        try {
            return URLEncoder.encode(valueToEncode, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 character set not supported", e.getCause());
        }
    }
}
