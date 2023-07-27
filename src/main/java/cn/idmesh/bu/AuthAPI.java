package cn.idmesh.bu;


import cn.idmesh.Util;
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class AuthAPI {
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_CLIENT_SECRET = "client_secret";
    private static final String KEY_GRANT_TYPE = "grant_type";
    private static final String PATH_PROTOCOL = "protocol";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String PATH_OAUTH = "oauth";
    private static final String PATH_OAUTH2 = "oauth2";
    private static final String PATH_OIDC = "oidc";
    private static final String PATH_TOKEN = "token";

    private static final String KEY_AUDIENCE = "audience";

    private static final String PATH_USERINFO = "userinfo";

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

    public UserInfo userInfo(String accessToken) throws IOException {
        // 获取用户信息
        String url = Util.urlBuilder(baseUrl, PATH_PROTOCOL, PATH_OIDC, PATH_USERINFO);
        // 构建请求
        Request request = Request.get(url);
        request.addHeader("Authorization", "Bearer " + accessToken);

        String result = request.execute().handleResponse(new BasicHttpClientResponseHandler());
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        return gson.fromJson(result, UserInfo.class);
    }

    public TokenHolder login(String emailOrUsername, char[] password, String realm) throws IOException {
        Validate.notNull(emailOrUsername, "email or username should not be null");
        Validate.notNull(password, "password should not be null");
        Validate.notNull(realm, "realm should not be null");

        String url =Util.urlBuilder(baseUrl, PATH_PROTOCOL, PATH_OAUTH2, PATH_TOKEN);
        Request request = Request.post(url);
        Form form = Form.form()
                .add(KEY_CLIENT_ID, clientId)
                .add(KEY_CLIENT_SECRET, clientSecret)
                .add(KEY_GRANT_TYPE, "password")
                .add("username", emailOrUsername)
                .add("password", new String(password))
                .add("realm", realm)
                .add("scope", "all");
        request.bodyForm(form.build(), StandardCharsets.UTF_8);
        String result = request
                .execute()
                .handleResponse(new BasicHttpClientResponseHandler());
        return parseBody(result, TokenHolder.class);
    }


    /**
     * M2M授权，直接请求token，使用clientId 直接申请token
     * @param audience 申请的token的audience
     * @return TokenHolder
     * @throws IOException IOException
     */
    public TokenHolder requestToken(String audience) throws IOException {
        Validate.notNull(audience, "audience should not be null");

        String url = Util.urlBuilder(baseUrl, PATH_PROTOCOL, PATH_OAUTH2, PATH_TOKEN);
        // 构建请求
        Request request = Request.post(url);
        Form form = Form.form()
                .add(KEY_CLIENT_ID, clientId)
                .add(KEY_CLIENT_SECRET, clientSecret)
                .add(KEY_GRANT_TYPE, "client_credentials")
                .add(KEY_AUDIENCE, audience);
        request.bodyForm(form.build(), StandardCharsets.UTF_8);
        String result = request
                .execute()
                .handleResponse(new BasicHttpClientResponseHandler());
        return parseBody(result, TokenHolder.class);
    }

    public TokenHolder revokeToken(String refreshToken) throws IOException {
        Validate.notNull(refreshToken, "refresh token should not be null");
        String url = Util.urlBuilder(baseUrl, PATH_PROTOCOL, PATH_OAUTH2, PATH_TOKEN);
        // 构建请求
        Request request = Request.post(url);
        Form form = Form.form()
                .add(KEY_CLIENT_ID, clientId)
                .add(KEY_CLIENT_SECRET, clientSecret)
                .add(KEY_GRANT_TYPE, "refresh_token")
                .add(KEY_REFRESH_TOKEN, refreshToken);
        request.bodyForm(form.build(), StandardCharsets.UTF_8);
        String result = request
                .execute()
                .handleResponse(new BasicHttpClientResponseHandler());
        return parseBody(result, TokenHolder.class);
    }

    public TokenHolder renewAuth(String refreshToken) throws IOException {
        Validate.notNull(refreshToken, "refresh token should not be null");

        String url = Util.urlBuilder(baseUrl, PATH_PROTOCOL, PATH_OAUTH2, PATH_TOKEN);

        Request request = Request.post(url);
        Form form  = Form.form()
                .add(KEY_CLIENT_ID, clientId)
                .add(KEY_CLIENT_SECRET, clientSecret)
                .add(KEY_GRANT_TYPE, "refresh_token")
                .add(KEY_REFRESH_TOKEN, refreshToken);

        request.bodyForm(form.build(), StandardCharsets.UTF_8);

        String result = request
                .execute()
                .handleResponse(new BasicHttpClientResponseHandler());
        return parseBody(result, TokenHolder.class);
    }

    public TokenHolder exchangeCode(String code, String redirectUri, ClientOption options) throws IOException {
        String url = Util.urlBuilder(baseUrl, PATH_PROTOCOL, PATH_OIDC, PATH_TOKEN);
        // 发送请求
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
        String result = request
                .execute()
                .handleResponse(new BasicHttpClientResponseHandler());
        return parseBody(result, TokenHolder.class);
    }

    public void setTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    public <T> T parseBody(String body, Class<T> clazz) {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        return gson.fromJson(body, clazz);
    }

    public Telemetry getTelemetry() {
        return telemetry;
    }
}
