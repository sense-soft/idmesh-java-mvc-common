package cn.idmesh.exception;

@SuppressWarnings("WeakerAccess")
public class IdentityVerificationException extends Exception {

    static final String API_ERROR = "idmesh.api_error";
    static final String JWT_MISSING_PUBLIC_KEY_ERROR = "idmesh.missing_jwt_public_key_error";
    static final String JWT_VERIFICATION_ERROR = "idmesh.invalid_jwt_error";
    private final String code;

    public IdentityVerificationException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean isAPIError() {
        return API_ERROR.equals(code);
    }

    public boolean isJWTError() {
        return JWT_MISSING_PUBLIC_KEY_ERROR.equals(code) || JWT_VERIFICATION_ERROR.equals(code);
    }
}
