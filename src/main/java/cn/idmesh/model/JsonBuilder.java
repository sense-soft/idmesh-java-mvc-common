package cn.idmesh.model;

import com.google.gson.Gson;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.util.HashMap;
import java.util.Map;

public class JsonBuilder {
    private Map<String, Object> container;

    public static JsonBuilder create() {
        return new JsonBuilder();
    }

    public JsonBuilder() {
        this.container = new HashMap<>();
    }

    public JsonBuilder put(String key, Object value) {
        this.container.put(key, value);
        return this;
    }

    public String build() {
        Gson gson = new Gson();
        return gson.toJson(this.container);
    }

    public StringEntity buildEntity() {
        return new StringEntity(build());
    }
}
