package io.hireme.hireme.job;

import io.hireme.hireme.observation.analysis.AnalysisLog;
import io.hireme.hireme.observation.execution.JobExecution;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "job_listings")
public class JobListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- JOB DATA ---
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    private String location;
    private String source;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RecruitmentStatus recruitmentStatus = RecruitmentStatus.INCOMING;

    private int priorityScore;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // --- PARENT LINK ---
    // The only link up the chain you need.
    // Access criteria via: this.getFoundInExecution().getSearchCriteria()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id")
    @ToString.Exclude
    private JobExecution foundInExecution;

    // --- CHILD LINK ---

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AnalysisStatus analysisStatus = AnalysisStatus.PENDING;

    @OneToOne(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private AnalysisLog currentAnalysis;

    // Helper to link Analysis
    public void setAnalysis(AnalysisLog analysis) {
        this.currentAnalysis = analysis;
        if (analysis != null) {
            analysis.setJob(this);
        }
    }
}