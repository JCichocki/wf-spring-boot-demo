package com.wakefern.sbdemo.batch.feeds;

import com.wakefern.sbdemo.util.NotFoundException;
import com.wakefern.sbdemo.batch.stocks.StocksBatch;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class FeedService {

    private final FeedRepository feedRepository;
    private final StocksBatch stocksBatch;
    private final FeedExecutionHistoryService executionHistoryService;

    public FeedService(final FeedRepository feedRepository, 
                      final StocksBatch stocksBatch,
                      final FeedExecutionHistoryService executionHistoryService) {
        this.feedRepository = feedRepository;
        this.stocksBatch = stocksBatch;
        this.executionHistoryService = executionHistoryService;
    }

    public List<FeedDTO> findAll() {
        final List<Feed> feeds = feedRepository.findAll(Sort.by("id"));
        return feeds.stream()
                .map(feed -> mapToDTO(feed, new FeedDTO()))
                .toList();
    }

    public FeedDTO get(final Long id) {
        return feedRepository.findById(id)
                .map(feed -> mapToDTO(feed, new FeedDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final FeedDTO feedDTO) {
        final Feed feed = new Feed();
        mapToEntity(feedDTO, feed);
        return feedRepository.save(feed).getId();
    }

    public void update(final Long id, final FeedDTO feedDTO) {
        final Feed feed = feedRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(feedDTO, feed);
        feedRepository.save(feed);
    }

    public void delete(final Long id) {
        feedRepository.deleteById(id);
    }

    public ExecutionResult executeFeed(final Long id, final String parameters) {
        final Feed feed = feedRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        
        ExecutionResult result = new ExecutionResult();
        result.addLog("Starting execution of feed: " + feed.getName() + " (ID: " + feed.getId() + ")");
        
        if (parameters != null && !parameters.trim().isEmpty()) {
            result.addLog("Parameters: " + parameters);
        } else {
            result.addLog("No parameters provided");
        }
        
        try {
            // Check if this is the Stocks Batch Feed
            if ("Stocks Batch Feed".equals(feed.getName())) {
                result.addLog("Detected Stocks Batch Feed - calling StocksBatch.execute()");
                ExecutionResult stocksResult = stocksBatch.executeWithResult(parameters);
                result.getLogs().addAll(stocksResult.getLogs());
                result.setStatus(stocksResult.getStatus());
                if (stocksResult.getError() != null) {
                    result.setError(stocksResult.getError());
                }
                // Copy stages from the stocks batch execution result
                result.setStages(stocksResult.getStages());
            } else {
                // For other feeds, just simulate execution
                result.addLog("Simulating execution for feed type: " + feed.getType());
                Thread.sleep(1000); // Simulate some processing time
                result.addLog("Feed execution completed successfully");
                result.setSuccess();
            }
            
            // Update lastRun timestamp
            feed.setLastRun(java.time.LocalDateTime.now());
            feedRepository.save(feed);
            result.addLog("Updated feed lastRun timestamp");
            
        } catch (Exception e) {
            result.setFailed("Execution failed: " + e.getMessage());
            result.addLog("ERROR: " + e.getMessage());
        }
        
        if (result.getStatus() == ExecutionResult.Status.SUCCESS) {
            result.addLog("Feed execution completed successfully in " + result.getDurationMillis() + "ms");
        }
        
        // Persist execution history to database
        try {
            executionHistoryService.persistExecution(id, parameters, result);
            result.addLog("Execution history saved to database");
        } catch (Exception e) {
            result.addLog("WARNING: Failed to save execution history - " + e.getMessage());
            // Don't fail the entire execution if history persistence fails
        }
        
        return result;
    }

    private FeedDTO mapToDTO(final Feed feed, final FeedDTO feedDTO) {
        feedDTO.setId(feed.getId());
        feedDTO.setName(feed.getName());
        feedDTO.setType(feed.getType());
        feedDTO.setStatus(feed.getStatus());
        feedDTO.setLastRun(feed.getLastRun());
        feedDTO.setNextRun(feed.getNextRun());
        return feedDTO;
    }

    private Feed mapToEntity(final FeedDTO feedDTO, final Feed feed) {
        feed.setName(feedDTO.getName());
        feed.setType(feedDTO.getType());
        feed.setStatus(feedDTO.getStatus());
        feed.setLastRun(feedDTO.getLastRun());
        feed.setNextRun(feedDTO.getNextRun());
        return feed;
    }

}
