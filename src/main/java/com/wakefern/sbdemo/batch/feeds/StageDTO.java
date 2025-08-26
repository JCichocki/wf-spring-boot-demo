package com.wakefern.sbdemo.batch.feeds;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Data transfer object representing a single execution stage")
public class StageDTO {

    @Schema(description = "Stage name", example = "Configuration")
    private String name;

    @Schema(description = "Stage description", example = "Configure HTTP client and validate system settings")
    private String description;

    @Schema(description = "Stage execution status", example = "SUCCESS", allowableValues = {"PENDING", "IN_PROGRESS", "SUCCESS", "FAILED"})
    private String status;

    @Schema(description = "Stage execution duration in milliseconds", example = "1250")
    private Long durationMillis;

    @Schema(description = "Stage start time")
    private LocalDateTime startTime;

    @Schema(description = "Stage end time")
    private LocalDateTime endTime;

    @Schema(description = "Error message if stage failed")
    private String error;

    public static StageDTO fromExecutionStage(ExecutionStage stage) {
        StageDTO dto = new StageDTO();
        dto.setName(stage.getName());
        dto.setDescription(stage.getDescription());
        dto.setStatus(stage.getStatus().name());
        dto.setDurationMillis(stage.getDurationMillis());
        dto.setStartTime(stage.getStartTime());
        dto.setEndTime(stage.getEndTime());
        dto.setError(stage.getError());
        return dto;
    }
}