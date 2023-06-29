package cn.idmesh;


import cn.idmesh.bu.AuthAPI;
import cn.idmesh.bu.AuthorizeUrlBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AuthorizeUrl {

    private static final String SCOPE_OPENID = "openid";

    private HttpServletResponse response;
    private HttpServletRequest request;
    private final AuthorizeUrlBuilder builder;
    private final String responseType;
    private boolean useLegacySameSiteCookie = true;
    private boolean setSecureCookie = false;
    private String nonce;
    private String state;

    private boolean used;

    AuthorizeUrl(AuthAPI client, HttpServletRequest request, HttpServletResponse response, String redirectUrl, String responseType) {
        this.request = request;
        this.response = response;
        this.responseType = responseType;
        this.builder = client.authorizeUrl(redirectUrl)
                .withResponseType(responseType)
                .withScope("openid profile email offline_access");
    }

    public AuthorizeUrl withOrganization(String organization) {
        builder.withOrganization(organization);
        return this;
    }

    public AuthorizeUrl withInvitation(String invitation) {
        builder.withInvitation(invitation);
        return this;
    }

    public AuthorizeUrl withConnection(String connection) {
        builder.withConnection(connection);
        return this;
    }

    public AuthorizeUrl withSecureCookie(boolean secureCookie) {
        this.setSecureCookie = secureCookie;
        return this;
    }

    AuthorizeUrl withLegacySameSiteCookie(boolean useLegacySameSiteCookie) {
        this.useLegacySameSiteCookie = useLegacySameSiteCookie;
        return this;
    }

    public AuthorizeUrl withAudience(String audience) {
        builder.withAudience(audience);
        return this;
    }

    public AuthorizeUrl withState(String state) {
        this.state = state;
        builder.withState(state);
        return this;
    }

    public AuthorizeUrl withNonce(String nonce) {
        this.nonce = nonce;
        builder.withParameter("nonce", nonce);
        return this;
    }

    public AuthorizeUrl withScope(String scope) {
        builder.withScope(scope);
        return this;
    }

    public AuthorizeUrl withParameter(String name, String value) {
        if ("state".equals(name) || "nonce".equals(name)) {
            throw new IllegalArgumentException("Please, use the dedicated methods for setting the 'nonce' and 'state' parameters.");
        }
        if ("response_type".equals(name)) {
            throw new IllegalArgumentException("Response type cannot be changed once set.");
        }
        if ("redirect_uri".equals(name)) {
            throw new IllegalArgumentException("Redirect URI cannot be changed once set.");
        }
        builder.withParameter(name, value);
        return this;
    }

    public String build() throws IllegalStateException {
        if (used) {
            throw new IllegalStateException("The AuthorizeUrl instance must not be reused.");
        }

        if (response != null) {
            SameSite sameSiteValue = containsFormPost() ? SameSite.NONE : SameSite.LAX;

            TransientCookieStore.storeState(response, state, sameSiteValue, useLegacySameSiteCookie, setSecureCookie);
            TransientCookieStore.storeNonce(response, nonce, sameSiteValue, useLegacySameSiteCookie, setSecureCookie);
        }
        RandomStorage.setSessionState(request, state);
        RandomStorage.setSessionNonce(request, nonce);

        used = true;
        return builder.build();
    }

    private boolean containsFormPost() {
        String[] splitResponseTypes = responseType.trim().split("\\s+");
        List<String> responseTypes = Collections.unmodifiableList(Arrays.asList(splitResponseTypes));
        return RequestProcessor.requiresFormPostResponseMode(responseTypes);
    }

}
