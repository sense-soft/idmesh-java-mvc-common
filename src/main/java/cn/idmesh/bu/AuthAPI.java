package cn.idmesh.bu;


import cn.idmesh.http.NamePassword;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cn.idmesh.HttpOptions;
import cn.idmesh.Telemetry;
import cn.idmesh.TokenHolder;
import cn.idmesh.http.ClientOption;
import cn.idmesh.model.UserInfo;
import org.apache.commons.lang3.Validate;
import org.apache.hc.client5.http.fluent.Form;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.net.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class AuthAPI {
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_CLIENT_SECRET = "client_secret";
    private static final String KEY_GRANT_TYPE = "grant_type";
    private static final String PATH_PROTOCOL = "protocol";
    private static final String PATH_OIDC = "oidc";
    private static final String PATH_TOKEN = "token";

    private final String clientId;
    private final String clientSecret;
    private final URI baseUrl;
    private Telemetry telemetry;

    public AuthAPI(String domain, String clientId, String clientSecret, HttpOptions options) {
        Validate.notNull(domain, "domain should not be null");
        Validate.notNull(clientId, "client id should not be null");
        Validate.notNull(clientSecret, "client secret should not be null");
        Validate.notNull(options, "client options should not be null");

        this.baseUrl = createBaseUrl(domain);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }


    public AuthAPI(String domain, String clientId, String clientSecret) {
        this(domain, clientId, clientSecret, new HttpOptions());
    }

    public URI getBaseUrl() {
        return baseUrl;
    }

    private URI createBaseUrl(String domain) {
        String url = domain;
        if (!domain.startsWith("https://") && !domain.startsWith("http://")) {
            url = "https://" + domain; // 不加schema默认为https
        }
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The domain had an invalid format and couldn't be parsed as an URL.");
        }
    }

    public AuthorizeUrlBuilder authorizeUrl(String redirectUri) {
        Validate.notNull(redirectUri, "redirect uri should not be null");

        return AuthorizeUrlBuilder.newInstance(baseUrl, clientId, redirectUri);
    }

    public UserInfo userInfo(String accessToken) { // TODO
        return null;
    }

    public Object resetPassword(String email, String connection) { // TODO
        Validate.notNull(email, "email should not be null");
        Validate.notNull(connection, "connection should not be null");

        String url = baseUrl + "/dbconnections/change_password"; // TODO

        return null;
    }


    public Object login(String emailOrUsername, char[] password, String realm) {
        return null;
    }


    public Object requestToken(String audience) {
        // TODO
        return null;
    }

    public Object revokeToken(String refreshToken) { // TODO 封装响应对象
        return null;
    }

    public Object renewAuth(String refreshToken) {
        // TODO 使用refreshToken刷新token
        return null;
    }

    public TokenHolder exchangeCode(String code, String redirectUri, ClientOption options) throws IOException {
        String url = null;
        try {
            url = new URIBuilder(baseUrl)
                    .appendPathSegments(PATH_PROTOCOL)
                    .appendPathSegments(PATH_OIDC)
                    .appendPathSegments(PATH_TOKEN)
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            // not possible
            throw new RuntimeException(e);
        }

        // 使用fluent-hc发送请求
        Request request = Request.post(url);

        Form formBuilder = Form.form();
        if (TokenValidationType.CLIENT_SECRET_BASIC.equals(options.getTokenValidationType())) {
            request.addHeader("Authorization", new NamePassword(clientId, clientSecret).getBasicAuth());
        } else if (TokenValidationType.CLIENT_SECRET_POST.equals(options.getTokenValidationType())) {
            formBuilder = formBuilder.add(KEY_CLIENT_ID, clientId).add(KEY_CLIENT_SECRET, clientSecret);
        }

        formBuilder = formBuilder
                .add(KEY_GRANT_TYPE, "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri);

        request.bodyForm(formBuilder.build(), StandardCharsets.UTF_8);

        String result = request.execute().handleResponse(new BasicHttpClientResponseHandler());
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        return gson.fromJson(result, TokenHolder.class);
    }

    public void setTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    public Telemetry getTelemetry() {
        return telemetry;
    }
}
