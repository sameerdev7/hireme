package io.hireme.hireme.job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JobRepository extends JpaRepository<JobListing, Long> {

    Page<JobListing> findByRecruitmentStatus(RecruitmentStatus status, @NonNull Pageable pageable);

    Page<JobListing> findAll(@NonNull Pageable pageable);

    Optional<JobListing> findByTitleIgnoreCaseAndCompanyIgnoreCase(String title, String company);

    @Transactional
    void deleteByRecruitmentStatusAndCreatedAtBefore(RecruitmentStatus status, LocalDateTime cutofDate);

    long countByRecruitmentStatus(RecruitmentStatus recruitmentStatus);
}