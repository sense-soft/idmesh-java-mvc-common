package cn.idmesh.http;

import java.util.Base64;

public class NamePassword {
    private String name;
    private String password;

    public NamePassword(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getBasicAuth() {
        return Base64.getEncoder().encodeToString((name + ":" + password).getBytes());
    }
}