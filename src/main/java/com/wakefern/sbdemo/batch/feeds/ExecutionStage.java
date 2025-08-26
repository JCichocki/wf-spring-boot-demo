package com.wakefern.sbdemo.batch.feeds;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionStage {
    
    public enum Status {
        PENDING, IN_PROGRESS, SUCCESS, FAILED
    }
    
    private String name;
    private String description;
    private Status status;
    private List<String> logs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, String> parameters;
    private String error;
    
    public ExecutionStage(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.PENDING;
        this.logs = new ArrayList<>();
        this.parameters = new HashMap<>();
    }
    
    public void start() {
        this.status = Status.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        addLog("Stage started: " + name);
    }
    
    public void complete() {
        this.status = Status.SUCCESS;
        this.endTime = LocalDateTime.now();
        addLog("Stage completed successfully in " + getDurationMillis() + "ms");
    }
    
    public void fail(String error) {
        this.status = Status.FAILED;
        this.error = error;
        this.endTime = LocalDateTime.now();
        addLog("Stage failed: " + error);
    }
    
    public void addLog(String message) {
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
        this.logs.add("[" + timestamp + "] " + message);
    }
    
    public long getDurationMillis() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }
    
    public String getLogsAsString() {
        return String.join("\n", logs);
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public List<String> getLogs() {
        return logs;
    }
    
    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
}