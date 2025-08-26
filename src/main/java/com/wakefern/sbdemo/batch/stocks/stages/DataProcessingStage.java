package com.wakefern.sbdemo.batch.stocks.stages;

import com.wakefern.sbdemo.batch.feeds.ExecutionResult;
import com.wakefern.sbdemo.batch.feeds.StageExecutor;
import com.wakefern.sbdemo.batch.stocks.StocksBatch;

import java.util.Map;

public class DataProcessingStage implements StageExecutor {
    
    private final StocksBatch stocksBatch;
    
    public DataProcessingStage(StocksBatch stocksBatch) {
        this.stocksBatch = stocksBatch;
    }
    
    @Override
    public void execute(Map<String, String> parameters, ExecutionResult result) throws Exception {
        result.addLogToCurrentStage("Starting stocks data processing");
        
        // Get processing parameters
        int batchSize = parameters != null && parameters.containsKey("batchSize") 
            ? Integer.parseInt(parameters.get("batchSize")) 
            : 100;
        boolean parallel = parameters != null && parameters.containsKey("parallel") 
            ? Boolean.parseBoolean(parameters.get("parallel")) 
            : false;
            
        result.addLogToCurrentStage("Processing configuration - Batch size: " + batchSize + ", Parallel: " + parallel);
        
        // Simulate data processing steps
        result.addLogToCurrentStage("Reading stocks data feed...");
        Thread.sleep(300);
        
        result.addLogToCurrentStage("Parsing data format (CSV/JSON)...");
        Thread.sleep(200);
        
        result.addLogToCurrentStage("Processing " + (batchSize * 5) + " stock records in batches of " + batchSize);
        
        // Simulate batch processing
        for (int batch = 1; batch <= 5; batch++) {
            result.addLogToCurrentStage("Processing batch " + batch + "/5 (" + batchSize + " records)");
            Thread.sleep(parallel ? 150 : 200); // Parallel processing is slightly faster
            
            if (batch == 3) {
                result.addLogToCurrentStage("Applying data transformations for batch " + batch);
            }
        }
        
        result.addLogToCurrentStage("All data batches processed successfully");
        result.addLogToCurrentStage("Total records processed: " + (batchSize * 5));
        
        result.addLogToCurrentStage("Data processing stage completed successfully");
    }
    
    @Override
    public String getName() {
        return "Data Processing";
    }
    
    @Override
    public String getDescription() {
        return "Process stocks data feed and transform records";
    }
}