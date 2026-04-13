package io.hireme.hireme.job.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AIAnalysisConfigRepository extends JpaRepository<AIAnalysisConfig, Long> {
    @Query("select c from AIAnalysisConfig c where c.id = 1")
    Optional<AIAnalysisConfig> getSingleton();
}
