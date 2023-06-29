package cn.idmesh;

import org.apache.commons.lang3.Validate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

class TransientCookieStore {

    // Prevent instantiation
    private TransientCookieStore() {}


    static void storeState(HttpServletResponse response, String state, SameSite sameSite, boolean useLegacySameSiteCookie, boolean isSecureCookie) {
        store(response, StorageUtils.STATE_KEY, state, sameSite, useLegacySameSiteCookie, isSecureCookie);
    }

    static void storeNonce(HttpServletResponse response, String nonce, SameSite sameSite, boolean useLegacySameSiteCookie, boolean isSecureCookie) {
        store(response, StorageUtils.NONCE_KEY, nonce, sameSite, useLegacySameSiteCookie, isSecureCookie);
    }
    static String getState(HttpServletRequest request, HttpServletResponse response, boolean useLegacySameSiteCookie) {
        return getOnce(StorageUtils.STATE_KEY, request, response, useLegacySameSiteCookie);
    }

    static String getNonce(HttpServletRequest request, HttpServletResponse response, boolean useLegacySameSiteCookie) {
        return getOnce(StorageUtils.NONCE_KEY, request, response, useLegacySameSiteCookie);
    }

    private static void store(HttpServletResponse response, String key, String value, SameSite sameSite, boolean useLegacySameSiteCookie, boolean isSecureCookie) {
        Validate.notNull(response, "response must not be null");
        Validate.notNull(key, "key must not be null");
        Validate.notNull(sameSite, "sameSite must not be null");

        if (value == null) {
            return;
        }

        boolean isSameSiteNone = SameSite.NONE == sameSite;

        AuthCookie sameSiteCookie = new AuthCookie(key, value);
        sameSiteCookie.setSameSite(sameSite);
        sameSiteCookie.setSecure(isSameSiteNone || isSecureCookie);

        // Servlet Cookie API does not yet support setting the SameSite attribute, so just set cookie on header
        response.addHeader("Set-Cookie", sameSiteCookie.buildHeaderString());

        // set legacy fallback cookie (if configured) for clients that won't accept SameSite=None
        if (isSameSiteNone && useLegacySameSiteCookie) {
            AuthCookie legacyCookie = new AuthCookie("_" + key, value);
            legacyCookie.setSecure(isSecureCookie);
            response.addHeader("Set-Cookie", legacyCookie.buildHeaderString());
        }

    }

    private static String getOnce(String cookieName, HttpServletRequest request, HttpServletResponse response, boolean useLegacySameSiteCookie) {
        Cookie[] requestCookies = request.getCookies();
        if (requestCookies == null) {
            return null;
        }

        Cookie foundCookie = null;
        for (Cookie c : requestCookies) {
            if (cookieName.equals(c.getName())) {
                foundCookie = c;
                break;
            }
        }

        String foundCookieVal = null;
        if (foundCookie != null) {
            foundCookieVal = decode(foundCookie.getValue());
            delete(foundCookie, response);
        }

        Cookie foundLegacyCookie = null;
        for (Cookie c : requestCookies) {
            if (("_" + cookieName).equals(c.getName())) {
                foundLegacyCookie = c;
                break;
            }
        }

        String foundLegacyCookieVal = null;
        if (foundLegacyCookie != null) {
            foundLegacyCookieVal = decode(foundLegacyCookie.getValue());
            delete(foundLegacyCookie, response);
        }

        return foundCookieVal != null ? foundCookieVal : foundLegacyCookieVal;
    }

    private static void delete(Cookie cookie, HttpServletResponse response) {
        cookie.setMaxAge(0);
        cookie.setValue("");
        response.addCookie(cookie);
    }

    private static String decode(String valueToDecode) {
        try {
            return URLDecoder.decode(valueToDecode, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 character set not supported", e.getCause());
        }
    }
}
