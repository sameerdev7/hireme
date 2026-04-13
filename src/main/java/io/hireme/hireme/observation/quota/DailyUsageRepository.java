package io.hireme.hireme.observation.quota;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyUsageRepository extends JpaRepository<DailyUsage, LocalDate> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from daily_usage d where d.date = :date")
    Optional<DailyUsage> findByIdWithLock(@Param("date")  LocalDate date);
}
