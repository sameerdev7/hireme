package io.hireme.hireme.observation.analysis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalysisLogRepository extends JpaRepository<AnalysisLog,String> {
    List<AnalysisLog> findByJobIdOrderByAnalysedAtDesc(Long jobId);

    List<AnalysisLog> findByAnalysedAtAfter(LocalDateTime startOfDay);
}
