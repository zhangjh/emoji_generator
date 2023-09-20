package me.zhangjh.emoji.generator.entity;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.Date;
import java.util.List;

public class EmojiDrawSubmitted {
    private String id;

    private String version;

    private String logs;

    private String error;

    private String status;

    @JSONField(name = "created_at")
    private Date createdAt;

    private List<OperationUrl> urls;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<OperationUrl> getUrls() {
        return urls;
    }

    public void setUrls(List<OperationUrl> urls) {
        this.urls = urls;
    }
}
