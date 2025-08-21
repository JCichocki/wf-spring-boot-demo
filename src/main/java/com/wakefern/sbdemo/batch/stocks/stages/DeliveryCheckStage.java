package com.wakefern.sbdemo.batch.stocks.stages;

import com.wakefern.sbdemo.batch.feeds.ExecutionResult;
import com.wakefern.sbdemo.batch.feeds.StageExecutor;
import com.wakefern.sbdemo.batch.stocks.StocksBatch;

import java.util.Map;

public class DeliveryCheckStage implements StageExecutor {
    
    private final StocksBatch stocksBatch;
    
    public DeliveryCheckStage(StocksBatch stocksBatch) {
        this.stocksBatch = stocksBatch;
    }
    
    @Override
    public void execute(Map<String, String> parameters, ExecutionResult result) throws Exception {
        result.addLogToCurrentStage("Starting delivery status check");
        
        // Get retry parameters
        int retryCount = parameters != null && parameters.containsKey("retryCount") 
            ? Integer.parseInt(parameters.get("retryCount")) 
            : 3;
        int retryDelay = parameters != null && parameters.containsKey("retryDelay") 
            ? Integer.parseInt(parameters.get("retryDelay")) 
            : 1000;
            
        result.addLogToCurrentStage("Retry configuration - Count: " + retryCount + ", Delay: " + retryDelay + "ms");
        
        result.addLogToCurrentStage("Checking delivery system for file presence");
        result.addLogToCurrentStage("NOTE: API calls are currently disabled for PoC");
        
        // Simulate delivery check with retries
        for (int i = 1; i <= retryCount; i++) {
            result.addLogToCurrentStage("Attempt " + i + " of " + retryCount + " - Checking delivery status");
            Thread.sleep(retryDelay / 2); // Simulate API call
            
            if (i < retryCount) {
                result.addLogToCurrentStage("File not found, retrying in " + retryDelay + "ms");
                Thread.sleep(retryDelay / 2);
            } else {
                result.addLogToCurrentStage("Simulated delivery status check completed - Status: PENDING");
            }
        }
        
        result.addLogToCurrentStage("Delivery check stage completed successfully");
    }
    
    @Override
    public String getName() {
        return "Delivery Status Check";
    }
    
    @Override
    public String getDescription() {
        return "Check if data file is available in the delivery system";
    }
}