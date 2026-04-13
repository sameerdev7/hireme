package io.hireme.hireme.api.dto.response;

import java.util.List;

public record CandidateProfileDto(
        String id,
        String bio,
        String rawResume,
        List<String> topSkills,
        String experienceSummary
) {
}
