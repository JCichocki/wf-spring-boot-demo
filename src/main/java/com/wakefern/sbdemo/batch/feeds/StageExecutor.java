package com.wakefern.sbdemo.batch.feeds;

import java.util.Map;

public interface StageExecutor {
    
    /**
     * Execute the stage with the provided parameters
     * @param parameters Stage-specific parameters
     * @param result The execution result to log to and update
     * @throws Exception if the stage execution fails
     */
    void execute(Map<String, String> parameters, ExecutionResult result) throws Exception;
    
    /**
     * Get the name of this stage
     * @return The stage name
     */
    String getName();
    
    /**
     * Get the description of this stage
     * @return The stage description
     */
    String getDescription();
}