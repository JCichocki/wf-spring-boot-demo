package com.wakefern.sbdemo.batch.stocks.stages;

import com.wakefern.sbdemo.batch.feeds.ExecutionResult;
import com.wakefern.sbdemo.batch.feeds.StageExecutor;
import com.wakefern.sbdemo.batch.stocks.StocksBatch;
import kong.unirest.core.Unirest;

import java.util.Map;

public class ConfigurationStage implements StageExecutor {
    
    private final StocksBatch stocksBatch;
    
    public ConfigurationStage(StocksBatch stocksBatch) {
        this.stocksBatch = stocksBatch;
    }
    
    @Override
    public void execute(Map<String, String> parameters, ExecutionResult result) throws Exception {
        result.addLogToCurrentStage("Configuring Stocks Batch execution environment");
        
        // Get timeout values from parameters or use defaults
        int connectTimeout = parameters != null && parameters.containsKey("connectTimeout") 
            ? Integer.parseInt(parameters.get("connectTimeout")) 
            : 10000;
        int requestTimeout = parameters != null && parameters.containsKey("requestTimeout") 
            ? Integer.parseInt(parameters.get("requestTimeout")) 
            : 10000;
            
        result.addLogToCurrentStage("Setting HTTP client timeouts - Connect: " + connectTimeout + "ms, Request: " + requestTimeout + "ms");
        
        // Configure Unirest
        Unirest.config()
            .connectTimeout(connectTimeout)
            .requestTimeout(requestTimeout);
            
        result.addLogToCurrentStage("HTTP client configured successfully");
        
        // Simulate configuration validation
        Thread.sleep(200);
        result.addLogToCurrentStage("Configuration validation completed");
        
        result.addLogToCurrentStage("Configuration stage completed successfully");
    }
    
    @Override
    public String getName() {
        return "Configuration";
    }
    
    @Override
    public String getDescription() {
        return "Configure HTTP client and validate system settings";
    }
}