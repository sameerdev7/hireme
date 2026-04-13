package io.hireme.hireme.job.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "ai_analysis_config")
public class AIAnalysisConfig {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String modelName;

    @Column(columnDefinition = "TEXT")
    private String systemInstruction;

    @Column(columnDefinition = "TEXT")
    private String userPromptTemplate;
}

