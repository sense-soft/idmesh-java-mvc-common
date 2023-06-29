package cn.idmesh.http;

public class SimpleBaseRequest extends BaseRequest{

    public SimpleBaseRequest() {
    }

    public SimpleBaseRequest put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public static SimpleBaseRequest create(KeyValue... entries) {
        SimpleBaseRequest request = new SimpleBaseRequest();
        for (KeyValue entry : entries) {
            request.put(entry.key, entry.value);
        }
        return request;
    }

    @Override
    public String sign(String secret) {
        return null;
    }

    public static class KeyValue {
        private String key;
        private Object value;

        public KeyValue(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }
}
