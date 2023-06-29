package cn.idmesh;

import java.util.HashMap;
import java.util.Map;

public class Telemetry {

    public static String HEADER_NAME = "X-Idmesh-Telemetry";

    private Map<String, Object> info;

    public Telemetry() {
        this.info = new HashMap<>();
    }

    public Telemetry(String name, Object value) {
        this();
        this.info.put(name, value);
    }

    public Telemetry add(String name, Object value) {
        this.info.put(name, value);
        return this;
    }
}
