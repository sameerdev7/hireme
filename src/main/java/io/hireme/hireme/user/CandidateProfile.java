package io.hireme.hireme.user;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Data
@Entity
@Table(name = "candidate_profile")
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio; // "I am a backend engineer with 5 years exp..."

    @Column(columnDefinition = "TEXT")
    private String rawResume; // Paste your parsed CV text here

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> topSkills; // "Java", "Spring Boot", "AWS"

    @Column(columnDefinition = "TEXT")
    private String experienceSummary;
}
