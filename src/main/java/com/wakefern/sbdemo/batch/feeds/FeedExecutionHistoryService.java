package com.wakefern.sbdemo.batch.feeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class FeedExecutionHistoryService {

    private final FeedExecutionRepository feedExecutionRepository;
    private final FeedExecutionStageRepository feedExecutionStageRepository;
    private final FeedExecutionLogRepository feedExecutionLogRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public FeedExecutionHistoryService(FeedExecutionRepository feedExecutionRepository,
                                       FeedExecutionStageRepository feedExecutionStageRepository,
                                       FeedExecutionLogRepository feedExecutionLogRepository,
                                       ObjectMapper objectMapper) {
        this.feedExecutionRepository = feedExecutionRepository;
        this.feedExecutionStageRepository = feedExecutionStageRepository;
        this.feedExecutionLogRepository = feedExecutionLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Persist an ExecutionResult to the database
     */
    public FeedExecution persistExecution(Long feedId, String parameters, ExecutionResult executionResult) {
        // Create and save FeedExecution
        FeedExecution feedExecution = new FeedExecution();
        feedExecution.setFeedId(feedId);
        feedExecution.setStatus(mapExecutionStatus(executionResult.getStatus()));
        feedExecution.setStartTime(executionResult.getStartTime());
        feedExecution.setEndTime(executionResult.getEndTime());
        feedExecution.setParameters(parameters);
        feedExecution.setError(executionResult.getError());
        feedExecution.setDurationMillis(executionResult.getDurationMillis());

        feedExecution = feedExecutionRepository.save(feedExecution);

        // Persist stages
        if (executionResult.getStages() != null && !executionResult.getStages().isEmpty()) {
            persistStages(feedExecution.getId(), executionResult.getStages());
        }

        // Persist logs
        if (executionResult.getLogs() != null && !executionResult.getLogs().isEmpty()) {
            persistLogs(feedExecution.getId(), executionResult.getLogs());
        }

        return feedExecution;
    }

    /**
     * Persist execution stages
     */
    private void persistStages(Long executionId, List<ExecutionStage> stages) {
        for (int i = 0; i < stages.size(); i++) {
            ExecutionStage stage = stages.get(i);
            
            FeedExecutionStage feedExecutionStage = new FeedExecutionStage();
            feedExecutionStage.setExecutionId(executionId);
            feedExecutionStage.setName(stage.getName());
            feedExecutionStage.setDescription(stage.getDescription());
            feedExecutionStage.setStatus(mapStageStatus(stage.getStatus()));
            feedExecutionStage.setStartTime(stage.getStartTime());
            feedExecutionStage.setEndTime(stage.getEndTime());
            feedExecutionStage.setError(stage.getError());
            feedExecutionStage.setDurationMillis(stage.getDurationMillis());
            feedExecutionStage.setStageOrder(i + 1);

            // Convert parameters map to JSON string
            if (stage.getParameters() != null && !stage.getParameters().isEmpty()) {
                try {
                    feedExecutionStage.setParameters(objectMapper.writeValueAsString(stage.getParameters()));
                } catch (Exception e) {
                    // If JSON conversion fails, store as string representation
                    feedExecutionStage.setParameters(stage.getParameters().toString());
                }
            }

            FeedExecutionStage savedStage = feedExecutionStageRepository.save(feedExecutionStage);

            // Persist stage-specific logs
            if (stage.getLogs() != null && !stage.getLogs().isEmpty()) {
                persistStageLogs(executionId, savedStage.getId(), stage.getLogs());
            }
        }
    }

    /**
     * Persist execution logs (overall logs)
     */
    private void persistLogs(Long executionId, List<String> logs) {
        for (String logMessage : logs) {
            FeedExecutionLog feedExecutionLog = new FeedExecutionLog();
            feedExecutionLog.setExecutionId(executionId);
            feedExecutionLog.setMessage(logMessage);
            feedExecutionLog.setLogLevel(determineLogLevel(logMessage));
            
            feedExecutionLogRepository.save(feedExecutionLog);
        }
    }

    /**
     * Persist stage-specific logs
     */
    private void persistStageLogs(Long executionId, Long stageId, List<String> logs) {
        for (String logMessage : logs) {
            FeedExecutionLog feedExecutionLog = new FeedExecutionLog();
            feedExecutionLog.setExecutionId(executionId);
            feedExecutionLog.setStageId(stageId);
            feedExecutionLog.setMessage(logMessage);
            feedExecutionLog.setLogLevel(determineLogLevel(logMessage));
            
            feedExecutionLogRepository.save(feedExecutionLog);
        }
    }

    /**
     * Determine log level based on message content
     */
    private String determineLogLevel(String message) {
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("error") || lowerMessage.contains("failed") || lowerMessage.contains("exception")) {
            return "ERROR";
        } else if (lowerMessage.contains("warn") || lowerMessage.contains("warning")) {
            return "WARN";
        } else {
            return "INFO";
        }
    }

    /**
     * Map ExecutionResult.Status to FeedExecution.Status
     */
    private FeedExecution.Status mapExecutionStatus(ExecutionResult.Status status) {
        return switch (status) {
            case SUCCESS -> FeedExecution.Status.SUCCESS;
            case FAILED -> FeedExecution.Status.FAILED;
            case IN_PROGRESS -> FeedExecution.Status.IN_PROGRESS;
        };
    }

    /**
     * Map ExecutionStage.Status to FeedExecutionStage.Status
     */
    private FeedExecutionStage.Status mapStageStatus(ExecutionStage.Status status) {
        return switch (status) {
            case SUCCESS -> FeedExecutionStage.Status.SUCCESS;
            case FAILED -> FeedExecutionStage.Status.FAILED;
            case IN_PROGRESS -> FeedExecutionStage.Status.IN_PROGRESS;
            case PENDING -> FeedExecutionStage.Status.PENDING;
        };
    }

    // Query methods for retrieving execution history

    /**
     * Get execution history for a specific feed
     */
    @Transactional(readOnly = true)
    public List<FeedExecution> getExecutionHistory(Long feedId) {
        return feedExecutionRepository.findByFeedIdOrderByStartTimeDesc(feedId);
    }

    /**
     * Get paginated execution history for a specific feed
     */
    @Transactional(readOnly = true)
    public Page<FeedExecution> getExecutionHistory(Long feedId, Pageable pageable) {
        return feedExecutionRepository.findByFeedIdOrderByStartTimeDesc(feedId, pageable);
    }

    /**
     * Get execution history by status
     */
    @Transactional(readOnly = true)
    public List<FeedExecution> getExecutionHistoryByStatus(FeedExecution.Status status) {
        return feedExecutionRepository.findByStatusOrderByStartTimeDesc(status);
    }

    /**
     * Get execution history by date range
     */
    @Transactional(readOnly = true)
    public List<FeedExecution> getExecutionHistoryByDateRange(Long feedId, LocalDateTime startTime, LocalDateTime endTime) {
        return feedExecutionRepository.findByFeedIdAndStartTimeBetween(feedId, startTime, endTime);
    }

    /**
     * Get a specific execution with stages and logs
     */
    @Transactional(readOnly = true)
    public Optional<FeedExecution> getExecutionWithDetails(Long executionId) {
        Optional<FeedExecution> execution = feedExecutionRepository.findById(executionId);
        if (execution.isPresent()) {
            FeedExecution feedExecution = execution.get();
            // Load stages
            List<FeedExecutionStage> stages = feedExecutionStageRepository.findByExecutionIdOrderByStageOrder(executionId);
            feedExecution.setStages(stages);
            
            // Load logs
            List<FeedExecutionLog> logs = feedExecutionLogRepository.findByExecutionIdOrderByTimestamp(executionId);
            feedExecution.setLogs(logs);
        }
        return execution;
    }

    /**
     * Get recent executions for a feed (last N executions)
     */
    @Transactional(readOnly = true)
    public List<FeedExecution> getRecentExecutions(Long feedId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return feedExecutionRepository.findMostRecentByFeedId(feedId, pageable);
    }

    /**
     * Get execution statistics for a feed
     */
    @Transactional(readOnly = true)
    public FeedExecutionStats getExecutionStats(Long feedId) {
        FeedExecutionStats stats = new FeedExecutionStats();
        stats.setFeedId(feedId);
        stats.setTotalExecutions(feedExecutionRepository.countByFeedId(feedId));
        stats.setSuccessfulExecutions(feedExecutionRepository.countByFeedIdAndStatus(feedId, FeedExecution.Status.SUCCESS));
        stats.setFailedExecutions(feedExecutionRepository.countByFeedIdAndStatus(feedId, FeedExecution.Status.FAILED));
        stats.setAverageDurationMillis(feedExecutionRepository.getAverageDurationByFeedId(feedId));
        
        return stats;
    }

    /**
     * Delete old execution history (cleanup)
     */
    public void deleteOldExecutions(LocalDateTime cutoffDate) {
        List<FeedExecution> oldExecutions = feedExecutionRepository.findByStartTimeBetween(LocalDateTime.of(2000, 1, 1, 0, 0), cutoffDate);
        for (FeedExecution execution : oldExecutions) {
            feedExecutionLogRepository.deleteOldLogsBefore(cutoffDate);
            feedExecutionStageRepository.deleteAll(execution.getStages());
            feedExecutionRepository.delete(execution);
        }
    }

    /**
     * Statistics container class
     */
    public static class FeedExecutionStats {
        private Long feedId;
        private Long totalExecutions;
        private Long successfulExecutions;
        private Long failedExecutions;
        private Double averageDurationMillis;

        // Getters and setters
        public Long getFeedId() { return feedId; }
        public void setFeedId(Long feedId) { this.feedId = feedId; }
        
        public Long getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(Long totalExecutions) { this.totalExecutions = totalExecutions; }
        
        public Long getSuccessfulExecutions() { return successfulExecutions; }
        public void setSuccessfulExecutions(Long successfulExecutions) { this.successfulExecutions = successfulExecutions; }
        
        public Long getFailedExecutions() { return failedExecutions; }
        public void setFailedExecutions(Long failedExecutions) { this.failedExecutions = failedExecutions; }
        
        public Double getAverageDurationMillis() { return averageDurationMillis; }
        public void setAverageDurationMillis(Double averageDurationMillis) { this.averageDurationMillis = averageDurationMillis; }
        
        public double getSuccessRate() {
            if (totalExecutions == null || totalExecutions == 0) return 0.0;
            return (successfulExecutions != null ? successfulExecutions : 0) * 100.0 / totalExecutions;
        }
    }
}