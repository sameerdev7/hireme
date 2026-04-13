package io.hireme.hireme.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, String> {
    // Find the profile linked to a specific user
    Optional<CandidateProfile> findByUserId(Long userId);

    List<CandidateProfile> findByUser(User user);
}
