package io.hireme.hireme.job.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchCriteriaRepository extends JpaRepository<SearchCriteria, Long> {

    List<SearchCriteria> findByIsActiveTrue();

    SearchCriteria findFirstByIsActiveTrue();
}
