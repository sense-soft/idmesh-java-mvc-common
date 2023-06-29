package cn.idmesh.bu;


import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class AuthorizeUrlBuilder {

    private URIBuilder uriBuilder;

    private Map<String, String> params = new HashMap<>();


    static AuthorizeUrlBuilder newInstance(URI bashUrl, String clientId, String redirectUri) {
        return new AuthorizeUrlBuilder(bashUrl, clientId, redirectUri);
    }

    public AuthorizeUrlBuilder(URI bashUrl, String clientId, String redirectUri) {
        this.uriBuilder = new URIBuilder(bashUrl).setPathSegments("protocol", "oidc","authorize").addParameter("client_id", clientId).addParameter("redirect_uri", redirectUri);
    }

    public AuthorizeUrlBuilder withOrganization(String organization) {
        this.params.put("organization", organization);
        return this;
    }

    public AuthorizeUrlBuilder withInvitation(String invitation) {
        this.params.put("invitation", invitation);
        return this;
    }

    public AuthorizeUrlBuilder withConnection(String connection) {
        this.params.put("connection", connection);
        return this;
    }

    public AuthorizeUrlBuilder withAudience(String audience) {
        this.params.put("audience", audience);
        return this;
    }

    public AuthorizeUrlBuilder withState(String state) {
        this.params.put("state", state);
        return this;
    }

    public AuthorizeUrlBuilder withParameter(String name, String value) {
        this.params.put(name, value);
        return this;
    }

    public AuthorizeUrlBuilder withScope(String scope) {
        this.params.put("scope", scope);
        return this;
    }

    public String build() {
        try {
            this.params.forEach((key, value) -> this.uriBuilder.addParameter(key, value));

            return this.uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthorizeUrlBuilder withResponseType(String responseType) {
        this.params.put("response_type", responseType);
        return this;
    }
}
