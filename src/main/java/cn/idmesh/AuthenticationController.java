package cn.idmesh;

import cn.idmesh.bu.AuthAPI;
import cn.idmesh.exception.IdentityVerificationException;
import org.apache.commons.lang3.Validate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationController {

    private final RequestProcessor requestProcessor;

    AuthenticationController(RequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    RequestProcessor getRequestProcessor() {
        return requestProcessor;
    }

    public static Builder newBuilder(String domain, String clientId, String clientSecret) {
        return new Builder(domain, clientId, clientSecret);
    }


    public static class Builder {
        private static final String RESPONSE_TYPE_CODE = "code";

        private final String domain;
        private final String clientId;
        private final String clientSecret;
        private String responseType;
        private Integer clockSkew;
        private Integer authenticationMaxAge;
        private boolean useLegacySameSiteCookie;
        private String organization;
        private String invitation;
        private HttpOptions httpOptions;

        Builder(String domain, String clientId, String clientSecret) {
            Validate.notNull(domain);
            Validate.notNull(clientId);
            Validate.notNull(clientSecret);

            this.domain = domain;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.responseType = RESPONSE_TYPE_CODE;
            this.useLegacySameSiteCookie = true;
        }

        public Builder withHttpOptions(HttpOptions httpOptions) {
            Validate.notNull(httpOptions);
            this.httpOptions = httpOptions;
            return this;
        }
        public Builder withResponseType(String responseType) {
            Validate.notNull(responseType);
            this.responseType = responseType.trim().toLowerCase();
            return this;
        }

        public Builder withClockSkew(Integer clockSkew) {
            Validate.notNull(clockSkew);
            this.clockSkew = clockSkew;
            return this;
        }

        public Builder withAuthenticationMaxAge(Integer maxAge) {
            Validate.notNull(maxAge);
            this.authenticationMaxAge = maxAge;
            return this;
        }

        public Builder withLegacySameSiteCookie(boolean useLegacySameSiteCookie) {
            this.useLegacySameSiteCookie = useLegacySameSiteCookie;
            return this;
        }

        public Builder withOrganization(String organization) {
            Validate.notNull(organization);
            this.organization = organization;
            return this;
        }

        public Builder withInvitation(String invitation) {
            Validate.notNull(invitation);
            this.invitation = invitation;
            return this;
        }

        public AuthenticationController build() throws UnsupportedOperationException {
            AuthAPI apiClient = createAPIClient(domain, clientId, clientSecret, httpOptions);
            setupTelemetry(apiClient);

            RequestProcessor processor = new RequestProcessor.Builder(apiClient, responseType)
                    .withLegacySameSiteCookie(useLegacySameSiteCookie)
                    .withOrganization(organization)
                    .withInvitation(invitation)
                    .build();

            return new AuthenticationController(processor);
        }

        AuthAPI createAPIClient(String domain, String clientId, String clientSecret, HttpOptions httpOptions) {
            if (httpOptions != null) {
                return new AuthAPI(domain, clientId, clientSecret, httpOptions);
            }
            return new AuthAPI(domain, clientId, clientSecret);
        }

        void setupTelemetry(AuthAPI client) {
            Telemetry telemetry = new Telemetry("idmesh-java-mvc-common", "1.0.0");
            client.setTelemetry(telemetry);
        }

        private String getIssuer(String domain) {
            if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
                domain = "https://" + domain;
            }
            if (!domain.endsWith("/")) {
                domain = domain + "/";
            }
            return domain;
        }
    }

    public Tokens handle(HttpServletRequest request, HttpServletResponse response) throws IdentityVerificationException {
        Validate.notNull(request, "request must not be null");
        Validate.notNull(response, "response must not be null");

        return requestProcessor.process(request, response);
    }

    public AuthorizeUrl buildAuthorizeUrl(HttpServletRequest request, HttpServletResponse response, String redirectUri) {
        Validate.notNull(request, "request must not be null");
        Validate.notNull(response, "response must not be null");
        Validate.notNull(redirectUri, "redirectUri must not be null");

        String state = StorageUtils.secureRandomString();
        String nonce = StorageUtils.secureRandomString();

        return requestProcessor.buildAuthorizeUrl(request, response, redirectUri, state, nonce);
    }

    public boolean isDefaultPort(String scheme, int port) {
        return ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
    }

}
