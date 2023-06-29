package cn.idmesh;

import java.io.Serializable;

public class Tokens implements Serializable {

    private static final long serialVersionUID = 2371882820082543721L;

    private final String accessToken;
    private final String idToken;
    private final String refreshToken;
    private final String type;
    private final Long expiresIn;


    public Tokens(String accessToken, String idToken, String refreshToken, String type, Long expiresIn) {
        this.accessToken = accessToken;
        this.idToken = idToken;
        this.refreshToken = refreshToken;
        this.type = type;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getType() {
        return type;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }
}
