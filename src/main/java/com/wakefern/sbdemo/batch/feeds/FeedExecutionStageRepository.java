package com.wakefern.sbdemo.batch.feeds;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FeedExecutionStageRepository extends JpaRepository<FeedExecutionStage, Long> {

    List<FeedExecutionStage> findByExecutionIdOrderByStageOrder(Long executionId);

    List<FeedExecutionStage> findByExecutionIdAndStatusOrderByStageOrder(Long executionId, FeedExecutionStage.Status status);

    List<FeedExecutionStage> findByStatusOrderByStartTimeDesc(FeedExecutionStage.Status status);

    @Query("SELECT fes FROM FeedExecutionStage fes WHERE fes.executionId = :executionId AND fes.name = :name")
    FeedExecutionStage findByExecutionIdAndName(@Param("executionId") Long executionId, @Param("name") String name);

    @Query("SELECT COUNT(fes) FROM FeedExecutionStage fes WHERE fes.executionId = :executionId AND fes.status = :status")
    long countByExecutionIdAndStatus(@Param("executionId") Long executionId, @Param("status") FeedExecutionStage.Status status);

    @Query("SELECT AVG(fes.durationMillis) FROM FeedExecutionStage fes WHERE fes.executionId = :executionId AND fes.status = 'SUCCESS' AND fes.durationMillis IS NOT NULL")
    Double getAverageDurationByExecutionId(@Param("executionId") Long executionId);

    @Query("SELECT SUM(fes.durationMillis) FROM FeedExecutionStage fes WHERE fes.executionId = :executionId AND fes.durationMillis IS NOT NULL")
    Long getTotalDurationByExecutionId(@Param("executionId") Long executionId);

    // Analytics for stage performance across executions
    @Query("SELECT AVG(fes.durationMillis) FROM FeedExecutionStage fes WHERE fes.name = :stageName AND fes.status = 'SUCCESS' AND fes.durationMillis IS NOT NULL")
    Double getAverageDurationByStageNameAcrossExecutions(@Param("stageName") String stageName);

    @Query("SELECT fes FROM FeedExecutionStage fes WHERE fes.name = :stageName AND fes.status = 'FAILED' ORDER BY fes.startTime DESC")
    List<FeedExecutionStage> findFailedStagesByName(@Param("stageName") String stageName);
}