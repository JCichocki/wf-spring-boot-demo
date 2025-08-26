package com.wakefern.sbdemo.batch.feeds;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feed_executions", indexes = {
    @Index(name = "idx_feed_execution_feed_id", columnList = "feed_id"),
    @Index(name = "idx_feed_execution_status", columnList = "status"),
    @Index(name = "idx_feed_execution_start_time", columnList = "start_time"),
    @Index(name = "idx_feed_execution_created_at", columnList = "created_at")
})
@Getter
@Setter
public class FeedExecution {

    public enum Status {
        SUCCESS, FAILED, IN_PROGRESS
    }

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

    @Column(name = "feed_id", nullable = false)
    private Long feedId;

    @ManyToOne
    @JoinColumn(name = "feed_id", insertable = false, updatable = false)
    private Feed feed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "parameters", columnDefinition = "CLOB")
    private String parameters;

    @Column(name = "error", columnDefinition = "CLOB")
    private String error;

    @Column(name = "duration_millis")
    private Long durationMillis;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "execution")
    private List<FeedExecutionStage> stages = new ArrayList<>();

    @OneToMany(mappedBy = "execution")
    private List<FeedExecutionLog> logs = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public long calculateDurationMillis() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }
}