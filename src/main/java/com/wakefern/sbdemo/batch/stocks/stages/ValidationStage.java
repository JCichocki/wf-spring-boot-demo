package com.wakefern.sbdemo.batch.stocks.stages;

import com.wakefern.sbdemo.batch.feeds.ExecutionResult;
import com.wakefern.sbdemo.batch.feeds.StageExecutor;
import com.wakefern.sbdemo.batch.stocks.StocksBatch;

import java.util.Map;

public class ValidationStage implements StageExecutor {
    
    private final StocksBatch stocksBatch;
    
    public ValidationStage(StocksBatch stocksBatch) {
        this.stocksBatch = stocksBatch;
    }
    
    @Override
    public void execute(Map<String, String> parameters, ExecutionResult result) throws Exception {
        result.addLogToCurrentStage("Starting data validation");
        
        // Get validation parameters
        boolean strictMode = parameters != null && parameters.containsKey("strictMode") 
            ? Boolean.parseBoolean(parameters.get("strictMode")) 
            : true;
        double errorThreshold = parameters != null && parameters.containsKey("errorThreshold") 
            ? Double.parseDouble(parameters.get("errorThreshold")) 
            : 0.05; // 5% error threshold
            
        result.addLogToCurrentStage("Validation configuration - Strict mode: " + strictMode + ", Error threshold: " + (errorThreshold * 100) + "%");
        
        // Simulate validation steps
        result.addLogToCurrentStage("Validating data format and structure...");
        Thread.sleep(300);
        
        result.addLogToCurrentStage("Checking required fields presence...");
        Thread.sleep(200);
        
        result.addLogToCurrentStage("Validating data types and ranges...");
        Thread.sleep(250);
        
        result.addLogToCurrentStage("Running business rule validations...");
        Thread.sleep(200);
        
        // Simulate validation results
        int totalRecords = 500;
        int errorCount = 12;
        double errorRate = (double) errorCount / totalRecords;
        
        result.addLogToCurrentStage("Validation results: " + totalRecords + " records, " + errorCount + " errors (" + String.format("%.2f%%", errorRate * 100) + ")");
        
        if (errorRate > errorThreshold) {
            if (strictMode) {
                throw new Exception("Validation failed - Error rate " + String.format("%.2f%%", errorRate * 100) + " exceeds threshold " + String.format("%.2f%%", errorThreshold * 100));
            } else {
                result.addLogToCurrentStage("WARNING: Error rate exceeds threshold but continuing due to non-strict mode");
            }
        } else {
            result.addLogToCurrentStage("Data quality validation passed - Error rate within acceptable limits");
        }
        
        result.addLogToCurrentStage("Data validation stage completed successfully");
    }
    
    @Override
    public String getName() {
        return "Data Validation";
    }
    
    @Override
    public String getDescription() {
        return "Validate data format, structure, and business rules";
    }
}