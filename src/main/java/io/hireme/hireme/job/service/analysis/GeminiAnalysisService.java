package io.hireme.hireme.job.service.analysis;

import com.google.genai.Client;
import com.google.genai.types.*;
import com.google.gson.Gson;
import io.hireme.hireme.api.dto.response.LlmResponse;
import io.hireme.hireme.job.config.AIAnalysisConfig;
import io.hireme.hireme.job.config.AIAnalysisConfigRepository;
import io.hireme.hireme.job.config.SearchCriteria;
import io.hireme.hireme.job.config.SearchCriteriaRepository;
import io.hireme.hireme.observation.analysis.JobAnalysisResult;
import io.hireme.hireme.user.CandidateProfile;
import io.hireme.hireme.user.CandidateProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class GeminiAnalysisService implements DeepAnalysisService{

    private final Client client;
    private final Gson gson =  new Gson();
    private final SearchCriteriaRepository  searchCriteriaRepository;
    private final AIAnalysisConfigRepository aiConfigRepository;
    private final CandidateProfileRepository profileRepository;

    @Value("${analysis.gemini.model-version}")
    private String modelVersion;

    private AIAnalysisConfig getAIConfig() {
        return aiConfigRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Ai config is missing"));
    }

    private CandidateProfile getCandidateProfile() {
        return profileRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Candidate profile is missing"));
    }

    private SearchCriteria getSearchCriteria() {
        return searchCriteriaRepository.findFirstByIsActiveTrue();
    }

    @Override
    public JobAnalysisResult runDeepAnalysis(String title, String description) {
        AIAnalysisConfig aiConfig = getAIConfig();
        CandidateProfile profile = getCandidateProfile();
        SearchCriteria searchCriteria = getSearchCriteria();

        long startTime = System.currentTimeMillis();

        String instruction = aiConfig.getSystemInstruction();
        String prompt = aiConfig.getUserPromptTemplate()
                .replace("{{JOB_TITLE}}", title)
                .replace("{{JOB_DESCRIPTION}}", description)
                .replace("{{BIO}}", profile.getBio())
                .replace("{{RESUME}}", profile.getRawResume())
                .replace("{{EXCLUDED_SKILLS}}", searchCriteria.getExcludedSkills().toString());

        Content content = Content.fromParts(Part.fromText(prompt));
        Content systemInstruction =  Content.fromParts(Part.fromText(instruction));

        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(systemInstruction)
                .responseMimeType("application/json")
                .build();

        GenerateContentResponse response;
        try {
            log.info("Calling GEMINI with JSON mode...");
             response = client.models.generateContent(modelVersion, content, config);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return JobAnalysisResult.failure(e.getMessage(), duration);
        }
        long duration = System.currentTimeMillis() - startTime;

        int tokens = response.usageMetadata()
                .flatMap(GenerateContentResponseUsageMetadata::totalTokenCount)
                .orElse(0);

        String jsonResponseText = Objects.requireNonNull(response.text()).trim();

        try {
            LlmResponse json =  gson.fromJson(jsonResponseText, LlmResponse.class);
            return new JobAnalysisResult(
                    json.getRelevanceScore(),
                    json.getMatchRationale(),
                    json.getExtractedSkills(),
                    tokens,
                    duration,
                    modelVersion,
                    true
            );
        } catch (Exception e) {
            log.error("Failed to parse JSON response: {}", jsonResponseText);
            return JobAnalysisResult.failure("JSON Parsing Error: " + e.getMessage(), duration);
        }
    }

}
