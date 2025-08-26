package com.wakefern.sbdemo.batch.feeds;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter
@Setter
@Schema(description = "Feed configuration data transfer object")
public class FeedDTO {

    @Schema(description = "Unique identifier of the feed", example = "1")
    private Long id;

    @Size(max = 255)
    @Schema(description = "Name of the feed", example = "Stocks Batch Feed")
    private String name;

    @Size(max = 255)
    @Schema(description = "Type of the feed", example = "BATCH")
    private String type;

    @Size(max = 255)
    @Schema(description = "Current status of the feed", example = "ACTIVE")
    private String status;

    @Schema(description = "Last execution timestamp")
    private LocalDateTime lastRun;

    @Schema(description = "Next scheduled execution timestamp")
    private LocalDateTime nextRun;

}
