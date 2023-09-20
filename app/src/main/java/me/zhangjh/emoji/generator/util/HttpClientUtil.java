package me.zhangjh.emoji.generator.util;

import android.util.Log;

import com.alibaba.fastjson2.JSONObject;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import me.zhangjh.emoji.generator.entity.HttpRequest;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public class HttpClientUtil {

    private static final OkHttpClient HTTP_CLIENT;

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(60, TimeUnit.SECONDS);
        builder.callTimeout(60, TimeUnit.SECONDS);
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.writeTimeout(60, TimeUnit.SECONDS);
        builder.connectionPool(new ConnectionPool(32,
                5,TimeUnit.MINUTES));
        HTTP_CLIENT = builder.build();
    }

    public static String getSync(String url) {
        HttpRequest httpRequest = new HttpRequest(url);
        return getSync(httpRequest);
    }

    public static String getSync(HttpRequest httpRequest) {
        Request.Builder builder = new Request.Builder().get().url(httpRequest.getUrl());
        for (Map.Entry<String, String> entry : httpRequest.getBizHeaderMap().entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        Request request = builder.build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String ret = handleResponse(Objects.requireNonNull(response.body()));
            if(httpRequest.getCb() != null) {
                httpRequest.getCb().apply(ret);
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String postSync(String url, String data) {
        HttpRequest request = new HttpRequest(url);
        request.setReqData(data);
        return postSync(request);
    }

    public static String postSync(HttpRequest httpRequest) {
        try {
            Log.d("httpRequest", "httpRequest: " + httpRequest.getReqData());
            JSONObject.parseObject(httpRequest.getReqData());
        } catch (Exception e) {
            throw new IllegalArgumentException("reqData isn't json format");
        }
        RequestBody requestBody = RequestBody.create(httpRequest.getReqData(), MediaType.get("application/json"));
        Request.Builder builder = new Request.Builder();
        for (Map.Entry<String, String> entry : httpRequest.getBizHeaderMap().entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        builder.url(httpRequest.getUrl())
                .method(httpRequest.getMethod(), requestBody);
        Request request = builder.build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String ret = handleResponse(Objects.requireNonNull(response.body()));
            if(httpRequest.getCb() != null) {
                httpRequest.getCb().apply(ret);
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String handleResponse(ResponseBody responseBody) {
        StringBuilder sb = new StringBuilder();
        try (responseBody) {
            BufferedSource source = responseBody.source();
            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if(StringUtils.isNotEmpty(line)) {
                    sb.append(line);
                }
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e("handleResponse error", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
