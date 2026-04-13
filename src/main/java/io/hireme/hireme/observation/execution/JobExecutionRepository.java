package io.hireme.hireme.observation.execution;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, String> {
    Optional<JobExecution> findTopByOrderByStartedAtDesc();
    List<JobExecution> findTop5ByOrderByStartedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM JobExecution j WHERE j.id = :id")
    Optional<JobExecution> findByIdWithLock(@Param("id") String id);
}
