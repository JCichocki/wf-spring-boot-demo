package com.wakefern.sbdemo.config;

import com.wakefern.sbdemo.batch.feeds.Feed;
import com.wakefern.sbdemo.batch.feeds.FeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final FeedRepository feedRepository;
    private final boolean initializeData;

    public DataInitializer(final FeedRepository feedRepository,
                          @Value("${app.data.initialize:true}") final boolean initializeData) {
        this.feedRepository = feedRepository;
        this.initializeData = initializeData;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!initializeData) {
            logger.info("Data initialization is disabled");
            return;
        }

        logger.info("Starting data initialization...");
        
        try {
            initializeFeeds();
            logger.info("Data initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during data initialization", e);
            throw new RuntimeException("Failed to initialize data", e);
        }
    }

    private void initializeFeeds() {
        List<FeedData> defaultFeeds = Arrays.asList(
            new FeedData("Stocks Batch Feed", "BATCH", "ACTIVE", "Primary stocks data processing feed"),
            new FeedData("Sales Feed", "BATCH", "ACTIVE", "Sales data processing feed"),
            new FeedData("Category Feed", "BATCH", "ACTIVE", "Category data processing feed"),
            new FeedData("Inventory Feed", "BATCH", "INACTIVE", "Inventory data processing feed")
        );

        for (FeedData feedData : defaultFeeds) {
            createFeedIfNotExists(feedData);
        }
    }

    private void createFeedIfNotExists(FeedData feedData) {
        if (!feedExists(feedData.name)) {
            Feed feed = new Feed();
            feed.setName(feedData.name);
            feed.setType(feedData.type);
            feed.setStatus(feedData.status);
            
            // Set next run to tomorrow at midnight for active feeds
            if ("ACTIVE".equals(feedData.status)) {
                feed.setNextRun(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            }
            
            feedRepository.save(feed);
            logger.info("Created default feed: {} (Type: {}, Status: {})", 
                       feedData.name, feedData.type, feedData.status);
        } else {
            logger.debug("Feed already exists: {}", feedData.name);
        }
    }

    private boolean feedExists(String name) {
        return feedRepository.findAll().stream()
                .anyMatch(feed -> name.equals(feed.getName()));
    }

    private static class FeedData {
        final String name;
        final String type;
        final String status;
        final String description;

        FeedData(String name, String type, String status, String description) {
            this.name = name;
            this.type = type;
            this.status = status;
            this.description = description;
        }
    }
}