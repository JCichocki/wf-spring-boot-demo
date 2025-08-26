package com.wakefern.sbdemo.batch.feeds;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExecutionResult {
    
    public enum Status {
        SUCCESS, FAILED, IN_PROGRESS
    }
    
    private Status status;
    private List<String> logs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String error;
    private List<ExecutionStage> stages;
    private ExecutionStage currentStage;
    
    public ExecutionResult() {
        this.logs = new ArrayList<>();
        this.stages = new ArrayList<>();
        this.startTime = LocalDateTime.now();
        this.status = Status.IN_PROGRESS;
    }
    
    public void addLog(String message) {
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
        this.logs.add("[" + timestamp + "] " + message);
    }
    
    public void setSuccess() {
        this.status = Status.SUCCESS;
        this.endTime = LocalDateTime.now();
    }
    
    public void setFailed(String error) {
        this.status = Status.FAILED;
        this.error = error;
        this.endTime = LocalDateTime.now();
    }
    
    // Getters and setters
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
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getLogsAsString() {
        return String.join("\n", logs);
    }
    
    public long getDurationMillis() {
        if (endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }
    
    // Stage management methods
    public ExecutionStage addStage(String name, String description) {
        ExecutionStage stage = new ExecutionStage(name, description);
        stages.add(stage);
        return stage;
    }
    
    public ExecutionStage startStage(String name, String description, Map<String, String> parameters) {
        ExecutionStage stage = addStage(name, description);
        if (parameters != null) {
            stage.setParameters(parameters);
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                stage.addLog("Parameter: " + param.getKey() + " = " + param.getValue());
            }
        }
        this.currentStage = stage;
        stage.start();
        addLog("Started stage: " + name);
        return stage;
    }
    
    public void completeCurrentStage() {
        if (currentStage != null) {
            currentStage.complete();
            addLog("Completed stage: " + currentStage.getName());
        }
    }
    
    public void failCurrentStage(String error) {
        if (currentStage != null) {
            currentStage.fail(error);
            addLog("Failed stage: " + currentStage.getName() + " - " + error);
        }
    }
    
    public void addLogToCurrentStage(String message) {
        if (currentStage != null) {
            currentStage.addLog(message);
        }
        addLog(message);
    }
    
    public List<ExecutionStage> getStages() {
        return stages;
    }
    
    public void setStages(List<ExecutionStage> stages) {
        this.stages = stages;
    }
    
    public ExecutionStage getCurrentStage() {
        return currentStage;
    }
    
    public void setCurrentStage(ExecutionStage currentStage) {
        this.currentStage = currentStage;
    }
    
    public boolean hasFailedStages() {
        return stages.stream().anyMatch(stage -> stage.getStatus() == ExecutionStage.Status.FAILED);
    }
    
    public long getTotalStagesDurationMillis() {
        return stages.stream()
                .mapToLong(ExecutionStage::getDurationMillis)
                .sum();
    }
    
    public int getSuccessfulStagesCount() {
        return (int) stages.stream()
                .filter(stage -> stage.getStatus() == ExecutionStage.Status.SUCCESS)
                .count();
    }
    
    public int getFailedStagesCount() {
        return (int) stages.stream()
                .filter(stage -> stage.getStatus() == ExecutionStage.Status.FAILED)
                .count();
    }
    
    public int getInProgressStagesCount() {
        return (int) stages.stream()
                .filter(stage -> stage.getStatus() == ExecutionStage.Status.IN_PROGRESS)
                .count();
    }
    
    public int getPendingStagesCount() {
        return (int) stages.stream()
                .filter(stage -> stage.getStatus() == ExecutionStage.Status.PENDING)
                .count();
    }
    
    public double getAverageStageDurationMillis() {
        return stages.stream()
                .filter(stage -> stage.getDurationMillis() > 0)
                .mapToLong(ExecutionStage::getDurationMillis)
                .average()
                .orElse(0.0);
    }
    
    public int getCompletedStagesCount() {
        return getSuccessfulStagesCount() + getFailedStagesCount();
    }
    
    public double getCompletionPercentage() {
        if (stages.isEmpty()) {
            return 0.0;
        }
        return (getCompletedStagesCount() * 100.0) / stages.size();
    }
}