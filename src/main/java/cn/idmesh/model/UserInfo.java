package cn.idmesh.model;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {

    private final Map<String, Object> values;

    public UserInfo() {
        this.values = new HashMap<>();
    }

    public void setValue(String key, Object value) {
        this.values.put(key, value);
    }

    public Map<String, Object> getValues() {
        return this.values;
    }

}
