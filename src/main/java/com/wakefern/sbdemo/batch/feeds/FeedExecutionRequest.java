package com.wakefern.sbdemo.batch.feeds;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request object for executing a feed")
public class FeedExecutionRequest {

    @Schema(description = "Optional parameters for feed execution", example = "param1=value1,param2=value2")
    private String parameters;

}