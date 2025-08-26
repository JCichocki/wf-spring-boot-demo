package com.wakefern.sbdemo.batch.feeds;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "feed_execution_logs", indexes = {
    @Index(name = "idx_feed_execution_log_execution_id", columnList = "execution_id"),
    @Index(name = "idx_feed_execution_log_stage_id", columnList = "stage_id"),
    @Index(name = "idx_feed_execution_log_timestamp", columnList = "timestamp"),
    @Index(name = "idx_feed_execution_log_level", columnList = "log_level")
})
@Getter
@Setter
public class FeedExecutionLog {

    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1,
            initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Long id;

    @Column(name = "execution_id", nullable = false)
    private Long executionId;

    @ManyToOne
    @JoinColumn(name = "execution_id", insertable = false, updatable = false)
    private FeedExecution execution;

    @Column(name = "stage_id")
    private Long stageId;

    @ManyToOne
    @JoinColumn(name = "stage_id", insertable = false, updatable = false)
    private FeedExecutionStage stage;

    @Column(name = "message", nullable = false, columnDefinition = "CLOB")
    private String message;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "log_level")
    private String logLevel;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (logLevel == null) {
            logLevel = "INFO";
        }
    }
}