package com.wakefern.sbdemo.batch.feeds;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/feeds", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Feed Management", description = "REST API for managing and executing data feeds")
public class FeedResource {

    private final FeedService feedService;
    private final FeedExecutionHistoryService executionHistoryService;

    public FeedResource(final FeedService feedService, final FeedExecutionHistoryService executionHistoryService) {
        this.feedService = feedService;
        this.executionHistoryService = executionHistoryService;
    }

    @GetMapping
    @Operation(summary = "Get all feeds", description = "Retrieve a list of all configured feeds")
    public ResponseEntity<List<FeedDTO>> getAllFeeds() {
        return ResponseEntity.ok(feedService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feed by ID", description = "Retrieve a specific feed by its ID")
    public ResponseEntity<FeedDTO> getFeed(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(feedService.get(id));
    }

    @PostMapping
    @Operation(summary = "Create new feed", description = "Create a new feed configuration")
    @ApiResponse(responseCode = "201", description = "Feed created successfully")
    public ResponseEntity<Long> createFeed(@RequestBody @Valid final FeedDTO feedDTO) {
        final Long createdId = feedService.create(feedDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update feed", description = "Update an existing feed configuration")
    public ResponseEntity<Long> updateFeed(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final FeedDTO feedDTO) {
        feedService.update(id, feedDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete feed", description = "Delete a feed configuration")
    @ApiResponse(responseCode = "204", description = "Feed deleted successfully")
    public ResponseEntity<Void> deleteFeed(@PathVariable(name = "id") final Long id) {
        feedService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute feed", description = "Execute a specific feed with optional parameters")
    @ApiResponse(responseCode = "200", description = "Feed execution completed")
    public ResponseEntity<FeedExecutionResponse> executeFeed(
            @PathVariable(name = "id") final Long id,
            @RequestBody(required = false) final FeedExecutionRequest request) {
        
        String parameters = request != null ? request.getParameters() : null;
        ExecutionResult result = feedService.executeFeed(id, parameters);
        
        FeedExecutionResponse response = new FeedExecutionResponse();
        response.setFeedId(id);
        response.setStatus(result.getStatus().name());
        response.setLogs(result.getLogs());
        response.setStartTime(result.getStartTime());
        response.setEndTime(result.getEndTime());
        response.setDurationMillis(result.getDurationMillis());
        response.setError(result.getError());
        
        // Map execution stages to DTOs
        List<StageDTO> stageDTOs = result.getStages().stream()
                .map(StageDTO::fromExecutionStage)
                .collect(Collectors.toList());
        response.setStages(stageDTOs);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/executions")
    @Operation(summary = "Get execution history", description = "Retrieve execution history for a specific feed")
    public ResponseEntity<List<FeedExecutionHistoryDTO>> getExecutionHistory(
            @PathVariable(name = "id") final Long id) {
        
        List<FeedExecution> executions = executionHistoryService.getExecutionHistory(id);
        List<FeedExecutionHistoryDTO> executionDTOs = executions.stream()
                .map(this::mapToExecutionHistoryDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(executionDTOs);
    }

    @GetMapping("/{id}/executions/{executionId}")
    @Operation(summary = "Get execution details", description = "Retrieve detailed information about a specific execution including stages and logs")
    public ResponseEntity<FeedExecutionDetailDTO> getExecutionDetails(
            @PathVariable(name = "id") final Long feedId,
            @PathVariable(name = "executionId") final Long executionId) {
        
        return executionHistoryService.getExecutionWithDetails(executionId)
                .map(execution -> {
                    if (!execution.getFeedId().equals(feedId)) {
                        throw new RuntimeException("Execution does not belong to this feed");
                    }
                    FeedExecutionDetailDTO detailDTO = mapToExecutionDetailDTO(execution);
                    return ResponseEntity.ok(detailDTO);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/executions/recent")
    @Operation(summary = "Get recent executions", description = "Retrieve the most recent executions for a feed")
    public ResponseEntity<List<FeedExecutionHistoryDTO>> getRecentExecutions(
            @PathVariable(name = "id") final Long id,
            @RequestParam(defaultValue = "10") final int limit) {
        
        List<FeedExecution> executions = executionHistoryService.getRecentExecutions(id, limit);
        List<FeedExecutionHistoryDTO> executionDTOs = executions.stream()
                .map(this::mapToExecutionHistoryDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(executionDTOs);
    }

    @GetMapping("/{id}/executions/stats")
    @Operation(summary = "Get execution statistics", description = "Retrieve execution statistics for a feed")
    public ResponseEntity<FeedExecutionHistoryService.FeedExecutionStats> getExecutionStats(
            @PathVariable(name = "id") final Long id) {
        
        FeedExecutionHistoryService.FeedExecutionStats stats = executionHistoryService.getExecutionStats(id);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/executions/filter")
    @Operation(summary = "Filter execution history", description = "Retrieve execution history filtered by status and date range")
    public ResponseEntity<List<FeedExecutionHistoryDTO>> getFilteredExecutionHistory(
            @PathVariable(name = "id") final Long id,
            @RequestParam(required = false) final String status,
            @RequestParam(required = false) final String startDate,
            @RequestParam(required = false) final String endDate) {
        
        List<FeedExecution> executions;
        
        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            executions = executionHistoryService.getExecutionHistoryByDateRange(id, start, end);
        } else if (status != null) {
            FeedExecution.Status executionStatus = FeedExecution.Status.valueOf(status.toUpperCase());
            executions = executionHistoryService.getExecutionHistoryByStatus(executionStatus);
            executions = executions.stream()
                    .filter(execution -> execution.getFeedId().equals(id))
                    .collect(Collectors.toList());
        } else {
            executions = executionHistoryService.getExecutionHistory(id);
        }
        
        List<FeedExecutionHistoryDTO> executionDTOs = executions.stream()
                .map(this::mapToExecutionHistoryDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(executionDTOs);
    }

    private FeedExecutionHistoryDTO mapToExecutionHistoryDTO(FeedExecution execution) {
        FeedExecutionHistoryDTO dto = new FeedExecutionHistoryDTO();
        dto.setId(execution.getId());
        dto.setFeedId(execution.getFeedId());
        dto.setStatus(execution.getStatus().name());
        dto.setStartTime(execution.getStartTime());
        dto.setEndTime(execution.getEndTime());
        dto.setDurationMillis(execution.getDurationMillis());
        dto.setParameters(execution.getParameters());
        dto.setError(execution.getError());
        dto.setCreatedAt(execution.getCreatedAt());
        return dto;
    }

    private FeedExecutionDetailDTO mapToExecutionDetailDTO(FeedExecution execution) {
        FeedExecutionDetailDTO dto = new FeedExecutionDetailDTO();
        dto.setId(execution.getId());
        dto.setFeedId(execution.getFeedId());
        dto.setStatus(execution.getStatus().name());
        dto.setStartTime(execution.getStartTime());
        dto.setEndTime(execution.getEndTime());
        dto.setDurationMillis(execution.getDurationMillis());
        dto.setParameters(execution.getParameters());
        dto.setError(execution.getError());
        dto.setCreatedAt(execution.getCreatedAt());
        
        // Map stages
        if (execution.getStages() != null) {
            List<FeedExecutionStageDTO> stageDTOs = execution.getStages().stream()
                    .map(this::mapToStageDTO)
                    .collect(Collectors.toList());
            dto.setStages(stageDTOs);
        }
        
        // Map logs
        if (execution.getLogs() != null) {
            List<FeedExecutionLogDTO> logDTOs = execution.getLogs().stream()
                    .map(this::mapToLogDTO)
                    .collect(Collectors.toList());
            dto.setLogs(logDTOs);
        }
        
        return dto;
    }

    private FeedExecutionStageDTO mapToStageDTO(FeedExecutionStage stage) {
        FeedExecutionStageDTO dto = new FeedExecutionStageDTO();
        dto.setId(stage.getId());
        dto.setName(stage.getName());
        dto.setDescription(stage.getDescription());
        dto.setStatus(stage.getStatus().name());
        dto.setStartTime(stage.getStartTime());
        dto.setEndTime(stage.getEndTime());
        dto.setDurationMillis(stage.getDurationMillis());
        dto.setParameters(stage.getParameters());
        dto.setError(stage.getError());
        dto.setStageOrder(stage.getStageOrder());
        return dto;
    }

    private FeedExecutionLogDTO mapToLogDTO(FeedExecutionLog log) {
        FeedExecutionLogDTO dto = new FeedExecutionLogDTO();
        dto.setId(log.getId());
        dto.setMessage(log.getMessage());
        dto.setTimestamp(log.getTimestamp());
        dto.setLogLevel(log.getLogLevel());
        dto.setStageId(log.getStageId());
        return dto;
    }

}
