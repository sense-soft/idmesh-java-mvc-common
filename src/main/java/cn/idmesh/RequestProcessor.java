package cn.idmesh;

import cn.idmesh.bu.AuthAPI;
import cn.idmesh.exception.IdentityVerificationException;
import cn.idmesh.exception.InvalidRequestException;
import cn.idmesh.http.ClientOption;
import cn.idmesh.bu.TokenValidationType;
import org.apache.commons.lang3.Validate;
import org.apache.hc.core5.net.URIBuilder;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;


class RequestProcessor {

    private static final String KEY_STATE = "state";
    private static final String KEY_ERROR = "error";
    private static final String KEY_ERROR_DESCRIPTION = "error_description";
    private static final String KEY_EXPIRES_IN = "expires_in";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_ID_TOKEN = "id_token";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_CODE = "code";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_RESPONSE_MODE = "response_mode";
    private static final String KEY_FORM_POST = "form_post";
    private static final String KEY_MAX_AGE = "max_age";

    // Visible for testing
//    final IdTokenVerifier.Options verifyOptions;
    final boolean useLegacySameSiteCookie;

    private final String responseType;
    private final AuthAPI client;
//    private final IdTokenVerifier tokenVerifier;
    private final String organization;
    private final String invitation;

    private JwtConsumerBuilder jwtConsumerBuilder;


    static class Builder {
        private final AuthAPI client;
        private final String responseType;
//        private final IdTokenVerifier.Options verifyOptions;
        private boolean useLegacySameSiteCookie = true;
//        private IdTokenVerifier tokenVerifier;
        private String organization;
        private String invitation;

        private JwtConsumerBuilder jwtConsumerBuilder;

        Builder(AuthAPI client, String responseType) {
            Validate.notNull(client);
            Validate.notNull(responseType);
            this.client = client;
            this.responseType = responseType;
        }



//        Builder(AuthAPI client, String responseType, IdTokenVerifier.Options verifyOptions) {
//            Validate.notNull(client);
//            Validate.notNull(responseType);
//            Validate.notNull(verifyOptions);
//            this.client = client;
//            this.responseType = responseType;
//            this.verifyOptions = verifyOptions;
//        }

        Builder withLegacySameSiteCookie(boolean useLegacySameSiteCookie) {
            this.useLegacySameSiteCookie = useLegacySameSiteCookie;
            return this;
        }

//        Builder withIdTokenVerifier(IdTokenVerifier verifier) {
//            this.tokenVerifier = verifier;
//            return this;
//        }

        Builder withOrganization(String organization) {
            this.organization = organization;
            return this;
        }

        Builder withInvitation(String invitation) {
            this.invitation = invitation;
            return this;
        }

        Builder withJwtConsumerBuilder(JwtConsumerBuilder jwtConsumerBuilder) {
            this.jwtConsumerBuilder = jwtConsumerBuilder;
            return this;
        }
        JwtConsumerBuilder createJwtConsumerBuilder(JwtConsumerBuilderOptions options) {
            JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder().setRequireIssuedAt()
                    .setRequireExpirationTime();
            // TODO - add support for other options 例如是否验证某些信息
            return jwtConsumerBuilder;
        }

        RequestProcessor build() {
            if (this.jwtConsumerBuilder == null) {
                this.jwtConsumerBuilder = createJwtConsumerBuilder(new JwtConsumerBuilderOptions());
            }
            return new RequestProcessor(client, responseType,
                    useLegacySameSiteCookie, organization, invitation, jwtConsumerBuilder);
        }
    }

//    private RequestProcessor(AuthAPI client, String responseType, IdTokenVerifier.Options verifyOptions, IdTokenVerifier tokenVerifier, boolean useLegacySameSiteCookie, String organization, String invitation) {
//        Validate.notNull(client);
//        Validate.notNull(responseType);
//        Validate.notNull(verifyOptions);
//        this.client = client;
//        this.responseType = responseType;
//        this.verifyOptions = verifyOptions;
//        this.tokenVerifier = tokenVerifier;
//        this.useLegacySameSiteCookie = useLegacySameSiteCookie;
//        this.organization = organization;
//        this.invitation = invitation;
//    }

    private RequestProcessor(AuthAPI client, String responseType, boolean useLegacySameSiteCookie, String organization, String invitation, JwtConsumerBuilder jwtConsumerBuilder) {
        Validate.notNull(client);
        Validate.notNull(responseType);
        this.client = client;
        this.responseType = responseType;
        this.useLegacySameSiteCookie = useLegacySameSiteCookie;
        this.organization = organization;
        this.invitation = invitation;
        this.jwtConsumerBuilder = jwtConsumerBuilder;
    }

    AuthAPI getClient() {
        return client;
    }

    AuthorizeUrl buildAuthorizeUrl(HttpServletRequest request, HttpServletResponse response, String redirectUri,
                                   String state, String nonce) {

        AuthorizeUrl creator = new AuthorizeUrl(client, request, response, redirectUri, responseType)
                .withState(state);

        if (this.organization != null) {
            creator.withOrganization(organization);
        }
        if (this.invitation != null) {
            creator.withInvitation(invitation);
        }

        if (response != null) {
            creator.withLegacySameSiteCookie(useLegacySameSiteCookie);
        }

        return getAuthorizeUrl(nonce, creator);
    }

    Tokens process(HttpServletRequest request, HttpServletResponse response) throws IdentityVerificationException {
        assertNoError(request);
        assertValidState(request, response);

        Tokens frontChannelTokens = getFrontChannelTokens(request);
        List<String> responseTypeList = getResponseType();

        if (responseTypeList.contains(KEY_ID_TOKEN) && frontChannelTokens.getIdToken() == null) {
            throw new InvalidRequestException("", "ID Token is missing from the response.");
        }
        if (responseTypeList.contains(KEY_TOKEN) && frontChannelTokens.getAccessToken() == null) {
            throw new InvalidRequestException("", "Access Token is missing from the response.");
        }

        String nonce; // TODO
        if (response != null) {
            nonce = TransientCookieStore.getNonce(request, response, useLegacySameSiteCookie);

            if (nonce == null) {
                nonce = RandomStorage.removeSessionNonce(request);
            }
        } else {
            nonce = RandomStorage.removeSessionNonce(request);
        }

//        verifyOptions.setNonce(nonce);

        return getVerifiedTokens(request, frontChannelTokens, responseTypeList);
    }

    static boolean requiresFormPostResponseMode(List<String> responseType) {
        return responseType != null &&
                (responseType.contains(KEY_TOKEN) || responseType.contains(KEY_ID_TOKEN));
    }


    private Tokens getVerifiedTokens(HttpServletRequest request, Tokens frontChannelTokens, List<String> responseTypeList)
            throws IdentityVerificationException {

        String authorizationCode = request.getParameter(KEY_CODE);
        Tokens codeExchangeTokens = null;

        try {
            HttpsJwks httpsJkws = new HttpsJwks(buildJwkUrl());
            HttpsJwksVerificationKeyResolver verificationKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJkws);

            this.jwtConsumerBuilder.setVerificationKeyResolver(verificationKeyResolver);
            this.jwtConsumerBuilder.setSkipDefaultAudienceValidation();
            JwtConsumer jwtConsumer = this.jwtConsumerBuilder.build();

            if (responseTypeList.contains(KEY_ID_TOKEN)) {
//                tokenVerifier.verify(frontChannelTokens.getIdToken(), verifyOptions);
                // 使用jose4j验证
                jwtConsumer.process(frontChannelTokens.getIdToken());
            }
            if (responseTypeList.contains(KEY_CODE)) {
                String redirectUri = request.getRequestURL().toString();
                codeExchangeTokens = exchangeCodeForTokens(authorizationCode, redirectUri);
                if (!responseTypeList.contains(KEY_ID_TOKEN)) {
                    String idTokenFromCodeExchange = codeExchangeTokens.getIdToken();
                    if (idTokenFromCodeExchange != null) {
                        jwtConsumer.process(idTokenFromCodeExchange);
                    }
                }
            }
        } catch (TokenValidationException | InvalidJwtException e) {
            throw new IdentityVerificationException("", "An error occurred while trying to verify the ID Token.", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Keep the front-channel ID Token and the code-exchange Access Token.
        return mergeTokens(frontChannelTokens, codeExchangeTokens);
    }

    String buildJwkUrl() {
        try {
            URIBuilder builder = new URIBuilder(client.getBaseUrl());
            builder.setPathSegments("protocol", "oidc", "keys");
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    List<String> getResponseType() {
        return Arrays.asList(responseType.split(" "));
    }

    private AuthorizeUrl getAuthorizeUrl(String nonce, AuthorizeUrl creator) {
        List<String> responseTypeList = getResponseType();
        if (responseTypeList.contains(KEY_ID_TOKEN) && nonce != null) {
            creator.withNonce(nonce);
        }
        if (requiresFormPostResponseMode(responseTypeList)) {
            creator.withParameter(KEY_RESPONSE_MODE, KEY_FORM_POST);
        }
//        if (verifyOptions.getMaxAge() != null) {
//            creator.withParameter(KEY_MAX_AGE, verifyOptions.getMaxAge().toString());
//        }
        return creator;
    }

    private Tokens getFrontChannelTokens(HttpServletRequest request) {
        Long expiresIn = request.getParameter(KEY_EXPIRES_IN) == null ? null : Long.parseLong(request.getParameter(KEY_EXPIRES_IN));
        return new Tokens(request.getParameter(KEY_ACCESS_TOKEN), request.getParameter(KEY_ID_TOKEN), null, request.getParameter(KEY_TOKEN_TYPE), expiresIn);
    }

    private void assertNoError(HttpServletRequest request) throws InvalidRequestException {
        String error = request.getParameter(KEY_ERROR);
        if (error != null) {
            String errorDescription = request.getParameter(KEY_ERROR_DESCRIPTION);
            throw new InvalidRequestException(error, errorDescription);
        }
    }

    private void assertValidState(HttpServletRequest request, HttpServletResponse response) throws InvalidRequestException {
        String stateFromRequest = request.getParameter(KEY_STATE);

        if (response == null) {
            checkSessionState(request, stateFromRequest);
            return;
        }

        String cookieState = TransientCookieStore.getState(request, response, useLegacySameSiteCookie);

        if (cookieState == null) {
            checkSessionState(request, stateFromRequest);
            return;
        }

        if (!cookieState.equals(stateFromRequest)) {
            throw new InvalidRequestException("", "The received state doesn't match the expected one.");
        }
    }

    private void checkSessionState(HttpServletRequest request, String stateFromRequest) throws InvalidRequestException {
        boolean valid = RandomStorage.checkSessionState(request, stateFromRequest);
        if (!valid) {
            throw new InvalidRequestException("", "The received state doesn't match the expected one.");
        }
    }

    private Tokens exchangeCodeForTokens(String authorizationCode, String redirectUri) throws IOException {
        TokenHolder holder = client
                .exchangeCode(authorizationCode, redirectUri, new ClientOption().setTokenValidationType(TokenValidationType.CLIENT_SECRET_POST));
        return new Tokens(holder.getAccessToken(), holder.getIdToken(), holder.getRefreshToken(), holder.getTokenType(), holder.getExpiresIn());
    }

    private Tokens mergeTokens(Tokens frontChannelTokens, Tokens codeExchangeTokens) {
        if (codeExchangeTokens == null) {
            return frontChannelTokens;
        }

        // Prefer access token from the code exchange
        String accessToken;
        String type;
        Long expiresIn;

        if (codeExchangeTokens.getAccessToken() != null) {
            accessToken = codeExchangeTokens.getAccessToken();
            type = codeExchangeTokens.getType();
            expiresIn = codeExchangeTokens.getExpiresIn();
        } else {
            accessToken = frontChannelTokens.getAccessToken();
            type = frontChannelTokens.getType();
            expiresIn = frontChannelTokens.getExpiresIn();
        }

        // Prefer ID token from the front-channel
        String idToken = frontChannelTokens.getIdToken() != null ? frontChannelTokens.getIdToken() : codeExchangeTokens.getIdToken();

        // Refresh token only available from the code exchange
        String refreshToken = codeExchangeTokens.getRefreshToken();

        return new Tokens(accessToken, idToken, refreshToken, type, expiresIn);
    }

}