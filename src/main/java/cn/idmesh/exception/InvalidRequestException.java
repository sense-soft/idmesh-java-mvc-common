package cn.idmesh.exception;

public class InvalidRequestException extends IdentityVerificationException {
    static final String INVALID_STATE_ERROR = "idmesh.invalid_state";
    static final String MISSING_ID_TOKEN = "idmesh.missing_id_token";
    static final String MISSING_ACCESS_TOKEN = "idmesh.missing_access_token";
    static final String DEFAULT_DESCRIPTION = "The request contains an error";

    public InvalidRequestException(String code, String description) {
        super(code, description != null ? description : DEFAULT_DESCRIPTION, null);
    }
}
