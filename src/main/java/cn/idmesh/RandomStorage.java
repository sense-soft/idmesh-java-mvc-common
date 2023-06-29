package cn.idmesh;

import javax.servlet.http.HttpServletRequest;

class RandomStorage extends SessionUtils {

    static boolean checkSessionState(HttpServletRequest req, String state) {
        String currentState = (String) remove(req, StorageUtils.STATE_KEY);
        return (currentState == null && state == null) || currentState != null && currentState.equals(state);
    }

    static void setSessionState(HttpServletRequest req, String state) {
        set(req, StorageUtils.STATE_KEY, state);
    }

    static void setSessionNonce(HttpServletRequest req, String nonce) {
        set(req, StorageUtils.NONCE_KEY, nonce);
    }

    static String removeSessionNonce(HttpServletRequest req) {
        return (String) remove(req, StorageUtils.NONCE_KEY);
    }
}