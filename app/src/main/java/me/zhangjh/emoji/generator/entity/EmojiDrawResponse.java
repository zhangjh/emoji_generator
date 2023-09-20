package me.zhangjh.emoji.generator.entity;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.Date;
import java.util.List;

public class EmojiDrawResponse {
    private String id;

    private String version;

    private String logs;

    private List<String> output;

    private String error;

    private String status;

    @JSONField(name = "created_at")
    private Date createdAt;

    @JSONField(name = "started_at")
    private Date startedAt;

    @JSONField(name = "completed_at")
    private Date completedAt;

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

    public List<String> getOutput() {
        return output;
    }

    public void setOutput(List<String> output) {
        this.output = output;
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

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public List<OperationUrl> getUrls() {
        return urls;
    }

    public void setUrls(List<OperationUrl> urls) {
        this.urls = urls;
    }
}
