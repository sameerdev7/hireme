package io.hireme.hireme.observation.execution;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceRunLog {
    private String sourceName; // "Adzuna", "Google"
    private int jobsFound;
    private int jobsFiltered;
    private int jobsSent;
    private boolean success;   // true/false
    private String error;      // "Timeout" or null
}
