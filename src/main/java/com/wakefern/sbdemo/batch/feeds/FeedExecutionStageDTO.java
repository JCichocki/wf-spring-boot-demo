package com.wakefern.sbdemo.batch.feeds;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Data transfer object for feed execution stage")
public class FeedExecutionStageDTO {

    @Schema(description = "Stage ID", example = "10002")
    private Long id;

    @Schema(description = "Stage name", example = "Configuration")
    private String name;

    @Schema(description = "Stage description", example = "Configure HTTP client and validate system settings")
    private String description;

    @Schema(description = "Stage status", example = "SUCCESS", allowableValues = {"PENDING", "IN_PROGRESS", "SUCCESS", "FAILED"})
    private String status;

    @Schema(description = "Stage start time")
    private LocalDateTime startTime;

    @Schema(description = "Stage end time")
    private LocalDateTime endTime;

    @Schema(description = "Stage duration in milliseconds", example = "1250")
    private Long durationMillis;

    @Schema(description = "Stage parameters in JSON format")
    private String parameters;

    @Schema(description = "Error message if stage failed")
    private String error;

    @Schema(description = "Stage execution order", example = "1")
    private Integer stageOrder;
}