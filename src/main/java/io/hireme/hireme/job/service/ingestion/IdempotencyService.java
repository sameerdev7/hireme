package io.hireme.hireme.job.service.ingestion;

import io.hireme.hireme.job.JobListing;
import io.hireme.hireme.job.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final JobRepository  jobRepository;

    public boolean isJobDuplicate(String title, String company) {
        Optional<JobListing> existing = jobRepository.findByTitleIgnoreCaseAndCompanyIgnoreCase(title,  company);
        return existing.isPresent();
    }
}
