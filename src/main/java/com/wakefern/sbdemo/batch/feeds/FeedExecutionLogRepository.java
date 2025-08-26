package com.wakefern.sbdemo.batch.feeds;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface FeedExecutionLogRepository extends JpaRepository<FeedExecutionLog, Long> {

    List<FeedExecutionLog> findByExecutionIdOrderByTimestamp(Long executionId);

    Page<FeedExecutionLog> findByExecutionIdOrderByTimestamp(Long executionId, Pageable pageable);

    List<FeedExecutionLog> findByStageIdOrderByTimestamp(Long stageId);

    List<FeedExecutionLog> findByExecutionIdAndStageIdOrderByTimestamp(Long executionId, Long stageId);

    List<FeedExecutionLog> findByLogLevelOrderByTimestampDesc(String logLevel);

    @Query("SELECT fel FROM FeedExecutionLog fel WHERE fel.executionId = :executionId AND fel.logLevel = :logLevel ORDER BY fel.timestamp")
    List<FeedExecutionLog> findByExecutionIdAndLogLevel(@Param("executionId") Long executionId, @Param("logLevel") String logLevel);

    @Query("SELECT fel FROM FeedExecutionLog fel WHERE fel.executionId = :executionId AND fel.timestamp >= :startTime AND fel.timestamp <= :endTime ORDER BY fel.timestamp")
    List<FeedExecutionLog> findByExecutionIdAndTimestampBetween(@Param("executionId") Long executionId, 
                                                                @Param("startTime") LocalDateTime startTime, 
                                                                @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(fel) FROM FeedExecutionLog fel WHERE fel.executionId = :executionId")
    long countByExecutionId(@Param("executionId") Long executionId);

    @Query("SELECT COUNT(fel) FROM FeedExecutionLog fel WHERE fel.stageId = :stageId")
    long countByStageId(@Param("stageId") Long stageId);

    @Query("SELECT COUNT(fel) FROM FeedExecutionLog fel WHERE fel.executionId = :executionId AND fel.logLevel = :logLevel")
    long countByExecutionIdAndLogLevel(@Param("executionId") Long executionId, @Param("logLevel") String logLevel);

    // Find logs containing specific message patterns (for error analysis)
    @Query("SELECT fel FROM FeedExecutionLog fel WHERE fel.executionId = :executionId AND LOWER(fel.message) LIKE LOWER(CONCAT('%', :messagePattern, '%')) ORDER BY fel.timestamp")
    List<FeedExecutionLog> findByExecutionIdAndMessageContaining(@Param("executionId") Long executionId, @Param("messagePattern") String messagePattern);

    // Find recent error logs across all executions
    @Query("SELECT fel FROM FeedExecutionLog fel WHERE fel.logLevel = 'ERROR' ORDER BY fel.timestamp DESC")
    List<FeedExecutionLog> findRecentErrorLogs(Pageable pageable);

    // Delete old logs (for cleanup purposes)
    @Query("DELETE FROM FeedExecutionLog fel WHERE fel.timestamp < :cutoffDate")
    void deleteOldLogsBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}