package com.wakefern.sbdemo.batch.feeds;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "Response object containing feed execution results")
public class FeedExecutionResponse {

    @Schema(description = "ID of the executed feed", example = "1")
    private Long feedId;

    @Schema(description = "Execution status", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED", "IN_PROGRESS"})
    private String status;

    @Schema(description = "List of execution log messages")
    private List<String> logs;

    @Schema(description = "Execution start time")
    private LocalDateTime startTime;

    @Schema(description = "Execution end time")
    private LocalDateTime endTime;

    @Schema(description = "Execution duration in milliseconds", example = "1500")
    private Long durationMillis;

    @Schema(description = "Error message if execution failed")
    private String error;

    @Schema(description = "List of execution stages with their status and timing information")
    private List<StageDTO> stages;

}