package cn.idmesh;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;

import java.security.SecureRandom;

class StorageUtils {

    private StorageUtils() {}

    static final String STATE_KEY = "com.idmesh.state";
    static final String NONCE_KEY = "com.idmesh.nonce";

    static String secureRandomString() {
        final SecureRandom sr = new SecureRandom();
        final byte[] randomBytes = new byte[32];
        sr.nextBytes(randomBytes);
        return Base64.encodeBase64URLSafeString(randomBytes);
    }
}
