package com.wakefern.sbdemo.batch.stocks.stages;

import com.wakefern.sbdemo.batch.feeds.ExecutionResult;
import com.wakefern.sbdemo.batch.feeds.StageExecutor;
import com.wakefern.sbdemo.batch.stocks.StocksBatch;

import java.util.Map;

public class CompletionStage implements StageExecutor {
    
    private final StocksBatch stocksBatch;
    
    public CompletionStage(StocksBatch stocksBatch) {
        this.stocksBatch = stocksBatch;
    }
    
    @Override
    public void execute(Map<String, String> parameters, ExecutionResult result) throws Exception {
        result.addLogToCurrentStage("Starting finalization process");
        
        // Get cleanup parameters
        boolean cleanup = parameters != null && parameters.containsKey("cleanup") 
            ? Boolean.parseBoolean(parameters.get("cleanup")) 
            : true;
        boolean generateReport = parameters != null && parameters.containsKey("generateReport") 
            ? Boolean.parseBoolean(parameters.get("generateReport")) 
            : true;
            
        result.addLogToCurrentStage("Finalization configuration - Cleanup: " + cleanup + ", Generate report: " + generateReport);
        
        // Simulate completion tasks
        result.addLogToCurrentStage("Updating database timestamps...");
        Thread.sleep(200);
        
        result.addLogToCurrentStage("Writing execution metadata...");
        Thread.sleep(150);
        
        if (generateReport) {
            result.addLogToCurrentStage("Generating execution summary report...");
            Thread.sleep(300);
            result.addLogToCurrentStage("Report generated: stocks_batch_" + System.currentTimeMillis() + ".json");
        }
        
        if (cleanup) {
            result.addLogToCurrentStage("Cleaning up temporary files...");
            Thread.sleep(100);
            result.addLogToCurrentStage("Releasing system resources...");
            Thread.sleep(100);
        }
        
        result.addLogToCurrentStage("Sending notification to monitoring systems...");
        Thread.sleep(150);
        
        result.addLogToCurrentStage("All completion tasks finished successfully");
        result.addLogToCurrentStage("Stocks batch processing completed successfully");
    }
    
    @Override
    public String getName() {
        return "Finalization";
    }
    
    @Override
    public String getDescription() {
        return "Finalize processing, cleanup resources, and generate reports";
    }
}