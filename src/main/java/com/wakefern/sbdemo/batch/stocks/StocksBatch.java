package com.wakefern.sbdemo.batch.stocks;

import com.wakefern.sbdemo.batch.feeds.ExecutionResult;
import com.wakefern.sbdemo.batch.feeds.StageExecutor;
import com.wakefern.sbdemo.batch.stocks.stages.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StocksBatch {
    private static final Logger logger = LoggerFactory.getLogger(StocksBatch.class);

    @Value("${by.api.url}")
    private String byApiUrl;

    @Value("${by.api.user}")
    private String byApiUser;

    @Value("${by.api.pass}")
    private String byApiPass;

    @Value("${by.api.timeout.connect}")
    private int connectTimeout;

    @Value("${by.api.timeout.request}")
    private int requestTimeout;

    @Value("${by.api.category}")
    private String category;

    public void execute() {
        // Keep the original method for backward compatibility
        logger.info("Executing stock feed processing...");
        // Original logic here (can be expanded later)
    }

    public ExecutionResult executeWithResult(String parameters) {
        ExecutionResult result = new ExecutionResult();
        
        try {
            result.addLog("Starting Stocks Batch execution with stage-based processing");
            
            // Parse parameters into stage-specific maps
            java.util.Map<String, java.util.Map<String, String>> stageParams = parseParameters(parameters);
            
            // Define stages in execution order
            java.util.List<StageExecutor> stages = java.util.Arrays.asList(
                new ConfigurationStage(this),
                new DeliveryCheckStage(this),
                new DataProcessingStage(this),
                new ValidationStage(this),
                new CompletionStage(this)
            );
            
            result.addLog("Initialized " + stages.size() + " execution stages");
            
            // Execute stages sequentially
            for (StageExecutor stage : stages) {
                result.startStage(
                    stage.getName(), 
                    stage.getDescription(),
                    stageParams.get(stage.getName())
                );
                
                try {
                    stage.execute(stageParams.get(stage.getName()), result);
                    result.completeCurrentStage();
                } catch (Exception e) {
                    result.failCurrentStage(e.getMessage());
                    logger.error("Stage '{}' failed", stage.getName(), e);
                    break; // Stop execution on stage failure
                }
            }
            
            // Set overall execution status based on stage results
            if (result.hasFailedStages()) {
                result.setFailed("One or more stages failed during execution");
            } else {
                result.setSuccess();
                result.addLog("All stages completed successfully");
            }
            
        } catch (Exception e) {
            result.setFailed("Stocks batch execution failed: " + e.getMessage());
            result.addLog("ERROR: " + e.getMessage());
            logger.error("StocksBatch execution failed", e);
        }
        
        return result;
    }
    
    private java.util.Map<String, java.util.Map<String, String>> parseParameters(String parameters) {
        java.util.Map<String, java.util.Map<String, String>> stageParams = new java.util.HashMap<>();
        
        if (parameters == null || parameters.trim().isEmpty()) {
            // Return default parameters for each stage
            stageParams.put("Configuration", createDefaultConfigParams());
            stageParams.put("Delivery Status Check", createDefaultDeliveryParams());
            stageParams.put("Data Processing", createDefaultProcessingParams());
            stageParams.put("Data Validation", createDefaultValidationParams());
            stageParams.put("Finalization", createDefaultCompletionParams());
            return stageParams;
        }
        
        try {
            // Try to parse as JSON first
            if (parameters.trim().startsWith("{")) {
                ObjectMapper mapper = new ObjectMapper();
                TypeReference<java.util.Map<String, java.util.Map<String, String>>> typeRef = new TypeReference<java.util.Map<String, java.util.Map<String, String>>>() {};
                stageParams = mapper.readValue(parameters, typeRef);
            } else {
                // Parse as simple key-value pairs and apply to all stages
                stageParams = parseSimpleParameters(parameters);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse parameters, using defaults: " + e.getMessage());
            stageParams.put("Configuration", createDefaultConfigParams());
            stageParams.put("Delivery Status Check", createDefaultDeliveryParams());
            stageParams.put("Data Processing", createDefaultProcessingParams());
            stageParams.put("Data Validation", createDefaultValidationParams());
            stageParams.put("Finalization", createDefaultCompletionParams());
        }
        
        return stageParams;
    }
    
    private java.util.Map<String, String> createDefaultConfigParams() {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("connectTimeout", String.valueOf(connectTimeout));
        params.put("requestTimeout", String.valueOf(requestTimeout));
        return params;
    }
    
    private java.util.Map<String, String> createDefaultDeliveryParams() {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("retryCount", "3");
        params.put("retryDelay", "1000");
        return params;
    }
    
    private java.util.Map<String, String> createDefaultProcessingParams() {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("batchSize", "100");
        params.put("parallel", "false");
        return params;
    }
    
    private java.util.Map<String, String> createDefaultValidationParams() {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("strictMode", "true");
        params.put("errorThreshold", "0.05");
        return params;
    }
    
    private java.util.Map<String, String> createDefaultCompletionParams() {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("cleanup", "true");
        params.put("generateReport", "true");
        return params;
    }
    
    private java.util.Map<String, java.util.Map<String, String>> parseSimpleParameters(String parameters) {
        java.util.Map<String, java.util.Map<String, String>> stageParams = new java.util.HashMap<>();
        java.util.Map<String, String> commonParams = new java.util.HashMap<>();
        
        // Parse simple key=value pairs
        String[] pairs = parameters.split("[,;]");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                commonParams.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        
        // Apply common parameters to all stages (they can override defaults)
        stageParams.put("Configuration", new java.util.HashMap<>(createDefaultConfigParams()));
        stageParams.put("Delivery Status Check", new java.util.HashMap<>(createDefaultDeliveryParams()));
        stageParams.put("Data Processing", new java.util.HashMap<>(createDefaultProcessingParams()));
        stageParams.put("Data Validation", new java.util.HashMap<>(createDefaultValidationParams()));
        stageParams.put("Finalization", new java.util.HashMap<>(createDefaultCompletionParams()));
        
        // Override with common parameters
        for (java.util.Map<String, String> stageParam : stageParams.values()) {
            stageParam.putAll(commonParams);
        }
        
        return stageParams;
    }

}
