package io.hireme.hireme.observation.execution;

import io.hireme.hireme.job.JobListing;
import io.hireme.hireme.job.config.SearchCriteria;
import io.hireme.hireme.job.RecruitmentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@Builder
@Entity
@Table(name = "job_executions")
@NoArgsConstructor
@AllArgsConstructor
public class JobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_criteria_id")
    @ToString.Exclude
    private SearchCriteria searchCriteria;

    @OneToMany(mappedBy = "foundInExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<JobListing> foundJobs = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "job_execution_source_logs",
            joinColumns = @JoinColumn(name = "execution_id")
    )
    @Builder.Default
    private List<SourceRunLog> sourceLogs = new ArrayList<>();
    private Integer expectedTotal;

    @Builder.Default private int jobsSkipped = 0;   // Idempotency hits
    @Builder.Default private int jobsAnalysed = 0;  // Total sent to Gemini
    @Builder.Default private int jobsAccepted = 0;  // Result: NEW_MATCH
    @Builder.Default private int jobsRejected = 0;  // Result: AUTO_REJECTED
    @Builder.Default private int jobsFailed = 0;    // Result: Crash/Error
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int jobsDropped;

    @Builder.Default private int totalTokenUsed = 0;


    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @CreationTimestamp
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public int totalJobsFound() {
        return sourceLogs.stream().mapToInt(SourceRunLog::getJobsFound).sum();
    }

    public int totalJobsFiltered() {
        return sourceLogs.stream().mapToInt(SourceRunLog::getJobsFiltered).sum();
    }

    public void completeIngestion(int totalJobsFound, List<SourceRunLog> runLogs) {
        this.expectedTotal = totalJobsFound;
        if (runLogs != null) {
            this.sourceLogs.addAll(runLogs);
        }

        if (this.status != ExecutionStatus.COMPLETED) {
            this.status = ExecutionStatus.INGESTION_COMPLETE;
        }

        checkCompletion();
    }

    public void recordAnalysisOutcome(RecruitmentStatus status, int tokens, boolean isSuccess) {
        this.jobsAnalysed++; // Tracks total attempts
        this.totalTokenUsed += tokens;

        if (isSuccess) {
            if (status == RecruitmentStatus.NEW_MATCH) {
                this.jobsAccepted++;
            } else if (status == RecruitmentStatus.AUTO_REJECTED) {
                this.jobsRejected++;
            }
        } else {
            this.jobsFailed++;
        }

        checkCompletion();
    }

    public void recordSkip() {
        this.jobsSkipped++;
        checkCompletion();
    }

    public void recordDrop() {
        this.jobsDropped++;
        checkCompletion();
    }

    private void checkCompletion() {
        if (this.expectedTotal == null) {
            return;
        }

        if (this.status == ExecutionStatus.COMPLETED) {
            return;
        }

        int totalProcessed = this.jobsAnalysed + this.jobsSkipped + this.jobsDropped;

        log.debug("Progress: {}/{}", totalProcessed, this.expectedTotal);

        if (totalProcessed >= this.expectedTotal) {
            this.status = ExecutionStatus.COMPLETED;
            this.finishedAt = LocalDateTime.now();
        }
    }

    public boolean isCompleted() {
        return this.status == ExecutionStatus.COMPLETED;
    }

    public enum ExecutionStatus {
        RUNNING,
        INGESTION_COMPLETE,
        COMPLETED,
        FAILED
    }
}
