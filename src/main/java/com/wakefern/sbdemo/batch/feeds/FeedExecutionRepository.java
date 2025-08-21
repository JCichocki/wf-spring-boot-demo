package com.wakefern.sbdemo.batch.feeds;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface FeedExecutionRepository extends JpaRepository<FeedExecution, Long> {

    List<FeedExecution> findByFeedIdOrderByStartTimeDesc(Long feedId);

    Page<FeedExecution> findByFeedIdOrderByStartTimeDesc(Long feedId, Pageable pageable);

    List<FeedExecution> findByStatusOrderByStartTimeDesc(FeedExecution.Status status);

    Page<FeedExecution> findByStatusOrderByStartTimeDesc(FeedExecution.Status status, Pageable pageable);

    List<FeedExecution> findByFeedIdAndStatusOrderByStartTimeDesc(Long feedId, FeedExecution.Status status);

    Page<FeedExecution> findByFeedIdAndStatusOrderByStartTimeDesc(Long feedId, FeedExecution.Status status, Pageable pageable);

    @Query("SELECT fe FROM FeedExecution fe WHERE fe.feedId = :feedId AND fe.startTime >= :startTime AND fe.startTime <= :endTime ORDER BY fe.startTime DESC")
    List<FeedExecution> findByFeedIdAndStartTimeBetween(@Param("feedId") Long feedId, 
                                                        @Param("startTime") LocalDateTime startTime, 
                                                        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT fe FROM FeedExecution fe WHERE fe.startTime >= :startTime AND fe.startTime <= :endTime ORDER BY fe.startTime DESC")
    List<FeedExecution> findByStartTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);

    @Query("SELECT fe FROM FeedExecution fe WHERE fe.feedId = :feedId AND fe.startTime >= :startTime AND fe.startTime <= :endTime ORDER BY fe.startTime DESC")
    Page<FeedExecution> findByFeedIdAndStartTimeBetween(@Param("feedId") Long feedId, 
                                                        @Param("startTime") LocalDateTime startTime, 
                                                        @Param("endTime") LocalDateTime endTime, 
                                                        Pageable pageable);

    // Analytics queries
    long countByFeedId(Long feedId);
    
    @Query("SELECT COUNT(fe) FROM FeedExecution fe WHERE fe.feedId = :feedId AND fe.status = :status")
    long countByFeedIdAndStatus(@Param("feedId") Long feedId, @Param("status") FeedExecution.Status status);

    @Query("SELECT AVG(fe.durationMillis) FROM FeedExecution fe WHERE fe.feedId = :feedId AND fe.status = 'SUCCESS' AND fe.durationMillis IS NOT NULL")
    Double getAverageDurationByFeedId(@Param("feedId") Long feedId);

    @Query("SELECT fe FROM FeedExecution fe WHERE fe.feedId = :feedId ORDER BY fe.startTime DESC")
    List<FeedExecution> findTop10ByFeedIdOrderByStartTimeDesc(@Param("feedId") Long feedId, Pageable pageable);

    // Find the most recent execution for a feed
    @Query("SELECT fe FROM FeedExecution fe WHERE fe.feedId = :feedId ORDER BY fe.startTime DESC")
    List<FeedExecution> findMostRecentByFeedId(@Param("feedId") Long feedId, Pageable pageable);
}