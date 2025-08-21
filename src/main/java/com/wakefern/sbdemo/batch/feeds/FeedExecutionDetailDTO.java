package com.wakefern.sbdemo.batch.feeds;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "Detailed data transfer object for feed execution with stages and logs")
public class FeedExecutionDetailDTO {

    @Schema(description = "Execution ID", example = "10001")
    private Long id;

    @Schema(description = "Feed ID", example = "10000")
    private Long feedId;

    @Schema(description = "Execution status", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED", "IN_PROGRESS"})
    private String status;

    @Schema(description = "Execution start time")
    private LocalDateTime startTime;

    @Schema(description = "Execution end time")
    private LocalDateTime endTime;

    @Schema(description = "Execution duration in milliseconds", example = "5230")
    private Long durationMillis;

    @Schema(description = "Execution parameters")
    private String parameters;

    @Schema(description = "Error message if execution failed")
    private String error;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "List of execution stages")
    private List<FeedExecutionStageDTO> stages;

    @Schema(description = "List of execution logs")
    private List<FeedExecutionLogDTO> logs;
}