package me.zhangjh.emoji.emoji.generator.entity;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class HttpRequest {

    @NonNull
    private final String url;

    private String method = "POST";

    private Map<String, String> bizHeaderMap = new HashMap<>();

    private String reqData;

    // 回调函数
    private Function<String, Object> cb;

    public HttpRequest(@NonNull String url) {
        this.url = url;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getBizHeaderMap() {
        return bizHeaderMap;
    }

    public void setBizHeaderMap(Map<String, String> bizHeaderMap) {
        this.bizHeaderMap = bizHeaderMap;
    }

    public String getReqData() {
        return reqData;
    }

    public void setReqData(String reqData) {
        this.reqData = reqData;
    }

    public Function<String, Object> getCb() {
        return cb;
    }

    public void setCb(Function<String, Object> cb) {
        this.cb = cb;
    }
}
