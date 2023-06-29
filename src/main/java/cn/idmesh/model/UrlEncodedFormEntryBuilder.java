package cn.idmesh.model;

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class UrlEncodedFormEntryBuilder {

    private List<BasicNameValuePair> container;

    public static UrlEncodedFormEntryBuilder create() {
        return new UrlEncodedFormEntryBuilder();
    }

    public UrlEncodedFormEntryBuilder() {
        this.container = new ArrayList<>();
    }

    public UrlEncodedFormEntryBuilder add(String key, String value) {
        this.container.add(new BasicNameValuePair(key, value));
        return this;
    }

    public UrlEncodedFormEntity build() {
        return this.build(null);
    }

    public UrlEncodedFormEntity build(Charset charset) {
        return new UrlEncodedFormEntity(this.container, charset);

    }

}
