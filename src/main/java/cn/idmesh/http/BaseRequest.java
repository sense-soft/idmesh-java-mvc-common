package cn.idmesh.http;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseRequest extends HashMap<String, Object> {

    // 创建抽象sign方法
    public abstract String sign(String secret);
    
}
