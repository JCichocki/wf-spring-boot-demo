package com.wakefern.sbdemo.batch.feeds;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter
@Setter
public class FeedExecutionDTO {

    private Long feedId;
    
    private String feedName;
    
    private String feedType;
    
    private String parameters;
    
    private LocalDateTime executionTime;

}