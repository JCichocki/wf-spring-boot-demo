package com.wakefern.sbdemo.batch.feeds;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Data transfer object for feed execution log")
public class FeedExecutionLogDTO {

    @Schema(description = "Log ID", example = "10003")
    private Long id;

    @Schema(description = "Log message")
    private String message;

    @Schema(description = "Log timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Log level", example = "INFO", allowableValues = {"INFO", "WARN", "ERROR"})
    private String logLevel;

    @Schema(description = "Stage ID if this log belongs to a specific stage")
    private Long stageId;
}