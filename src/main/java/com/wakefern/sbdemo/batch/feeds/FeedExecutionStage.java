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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feed_execution_stages", indexes = {
    @Index(name = "idx_feed_execution_stage_execution_id", columnList = "execution_id"),
    @Index(name = "idx_feed_execution_stage_status", columnList = "status"),
    @Index(name = "idx_feed_execution_stage_order", columnList = "execution_id, stage_order")
})
@Getter
@Setter
public class FeedExecutionStage {

    public enum Status {
        PENDING, IN_PROGRESS, SUCCESS, FAILED
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

    @Column(name = "execution_id", nullable = false)
    private Long executionId;

    @ManyToOne
    @JoinColumn(name = "execution_id", insertable = false, updatable = false)
    private FeedExecution execution;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "CLOB")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "parameters", columnDefinition = "CLOB")
    private String parameters;

    @Column(name = "error", columnDefinition = "CLOB")
    private String error;

    @Column(name = "duration_millis")
    private Long durationMillis;

    @Column(name = "stage_order", nullable = false)
    private Integer stageOrder;

    @OneToMany(mappedBy = "stage")
    private List<FeedExecutionLog> logs = new ArrayList<>();

    public long calculateDurationMillis() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }
}