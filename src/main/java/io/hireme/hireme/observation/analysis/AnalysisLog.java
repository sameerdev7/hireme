package io.hireme.hireme.observation.analysis;

import io.hireme.hireme.job.JobListing;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "analysis_logs")
public class AnalysisLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // --- PARENT LINK (The Foreign Key) ---
    // This is the OWNER of the relationship.
    // The "job_id" column lives in this table.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @ToString.Exclude
    private JobListing job;

    // --- AUDIT TRAIL (What did we send?) ---
    // Critical for debugging if the AI starts acting weird.
    // We store the specific prompt used for THIS run, in case the config changes later.
    @Column(columnDefinition = "TEXT")
    private String promptUsed;

    // --- METRICS (The Cost/Perf) ---
    private Integer tokenCount;     // To track API costs
    private Long latencyMs;         // How long Gemini took
    private String modelVersion;    // e.g., "gemini-1.5-flash"

    // --- THE OPINION (The Result) ---
    private Integer compatibilityScore; // 0 to 100

    @Column(columnDefinition = "TEXT")
    private String matchRationale;      // "Matches because..."

    // Storing a simple list of strings (Skills)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "analysis_extracted_skills",
            joinColumns = @JoinColumn(name = "analysis_id")
    )
    @Column(name = "skill")
    private List<String> extractedSkills;

    @CreationTimestamp
    private LocalDateTime analysedAt;

    // --- Standard Equality ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnalysisLog that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}