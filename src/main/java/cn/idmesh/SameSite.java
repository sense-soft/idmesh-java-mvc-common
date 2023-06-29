package cn.idmesh;

enum SameSite {
    LAX("Lax"),
    NONE("None"),
    STRICT("Strict");

    private String value;

    String getValue() {
        return this.value;
    }

    SameSite(String value) {
        this.value = value;
    }
}