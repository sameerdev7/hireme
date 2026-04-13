package io.hireme.hireme.api.controller;

import io.hireme.hireme.api.dto.response.CandidateProfileDto;
import io.hireme.hireme.common.AppMapper;
import io.hireme.hireme.job.config.AIAnalysisConfig;
import io.hireme.hireme.job.config.AIAnalysisConfigRepository;
import io.hireme.hireme.job.config.SearchCriteria;
import io.hireme.hireme.job.config.SearchCriteriaRepository;
import io.hireme.hireme.user.CandidateProfile;
import io.hireme.hireme.user.CandidateProfileRepository;
import io.hireme.hireme.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/settings")
@Profile("api")
@RequiredArgsConstructor
public class SettingsController {

    private final SearchCriteriaRepository searchCriteriaRepository;
    private final CandidateProfileRepository  profileRepository;
    private final AIAnalysisConfigRepository aiConfigRepository;
    private final AppMapper mapper;

    @GetMapping("/search")
    public ResponseEntity<List<SearchCriteria>> getConfigSearches(
            @RequestParam(value = "active", required = false) Boolean activeStatus
    ) {

        if  (activeStatus != null && activeStatus) {

            return ResponseEntity.ok(searchCriteriaRepository.findByIsActiveTrue());
        }
        return ResponseEntity.ok(searchCriteriaRepository.findAll());
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SearchCriteria> createSearchCriteria(@RequestBody SearchCriteria searchCriteria) {
        return ResponseEntity.ok(searchCriteriaRepository.save(searchCriteria));
    }

    @PutMapping("/search/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SearchCriteria> updateSearchCriteria(@RequestBody SearchCriteria searchCriteria, @PathVariable Long id) {
        if (searchCriteriaRepository.findById(id).isPresent()) {
            return ResponseEntity.ok(searchCriteriaRepository.save(searchCriteria));
        } else  {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/search/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        return searchCriteriaRepository.findById(id)
                .map(search -> {
                    search.setIsActive(!search.getIsActive());
                    searchCriteriaRepository.save(search);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());

    }

    @DeleteMapping("/search/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSearchCriteria(@PathVariable Long id) {
        searchCriteriaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

//    Profile Endpoints

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CandidateProfileDto> getProfile(
            @AuthenticationPrincipal User user
    ) {
        CandidateProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Profile not found"));

        return ResponseEntity.ok(mapper.mapToCandidateProfileDto(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<CandidateProfileDto> updateProfile(
            @RequestBody CandidateProfile profileDto,
            @AuthenticationPrincipal User user
    ) {
        // 1. Try to find existing
        Optional<CandidateProfile> existing = profileRepository.findByUserId(user.getId());

        if (existing.isPresent()) {
            // UPDATE MODE
            CandidateProfile profile = existing.get();
            profile.setBio(profileDto.getBio());
            profile.setRawResume(profileDto.getRawResume());
            profile.setTopSkills(profileDto.getTopSkills());
            profile.setExperienceSummary(profileDto.getExperienceSummary());
            return ResponseEntity.ok(mapper.mapToCandidateProfileDto(profileRepository.save(profile)));
        } else {
            // CREATE MODE (First time)
            profileDto.setUser(user);
            return ResponseEntity.ok(mapper.mapToCandidateProfileDto(profileRepository.save(profileDto)));
        }
    }

    // AI Config Endpoints

    @GetMapping("/ai-config")
    public AIAnalysisConfig getAIConfig() {
        return aiConfigRepository.getSingleton()
                .orElseGet(this::createDefaultConfig);
    }

    @PutMapping("/ai-config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AIAnalysisConfig> updateAIConfig(
            @RequestBody AIAnalysisConfig aiConfigDto
    ) {
        Optional<AIAnalysisConfig> existingConfig = aiConfigRepository.getSingleton();
        if (existingConfig.isPresent()) {

            AIAnalysisConfig aiConfig = existingConfig.get();
            aiConfig.setModelName(aiConfigDto.getModelName());
            aiConfig.setSystemInstruction(aiConfigDto.getSystemInstruction());
            aiConfig.setUserPromptTemplate(aiConfigDto.getUserPromptTemplate());
            return ResponseEntity.ok(aiConfigRepository.save(aiConfig));
        } else {
            return ResponseEntity.ok(aiConfigRepository.save(aiConfigDto));
        }
    }

    private AIAnalysisConfig createDefaultConfig() {
        AIAnalysisConfig config = new AIAnalysisConfig();
        config.setId(1L); // Force ID 1
        config.setModelName("gemini-1.5-flash"); // Default to cheaper model

        // robust default system instruction
        config.setSystemInstruction(
                "You are a strict technical recruiter assisting a candidate. " +
                        "Your goal is to evaluate job descriptions against the candidate's profile " +
                        "and reject anything that is a poor fit based on skills, seniority, or location."
        );

        // robust default template
        config.setUserPromptTemplate(
                """
                        Candidate Profile:
                        {{BIO}}
                        {{RESUME}}
                        
                        Job Description:
                        {{JOB_TITLE}}
                        {{JOB_DESCRIPTION}}
                        
                        Task: Compare the candidate to the job. \
                        If the job requires specific skills listed in {{EXCLUDED_SKILLS}}, reject it immediately."""
        );

        return aiConfigRepository.save(config);
    }

}
