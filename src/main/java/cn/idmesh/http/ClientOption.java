package cn.idmesh.http;

import cn.idmesh.bu.TokenValidationType;

public class ClientOption {

    private TokenValidationType tokenValidationType = TokenValidationType.CLIENT_SECRET_BASIC; // 默认使用Client_Secret_Basic

    public TokenValidationType getTokenValidationType() {
        return tokenValidationType;
    }

    public ClientOption setTokenValidationType(TokenValidationType tokenValidationType) {
        this.tokenValidationType = tokenValidationType;
        return this;
    }
}
