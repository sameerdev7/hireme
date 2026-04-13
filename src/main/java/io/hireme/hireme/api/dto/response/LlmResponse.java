package io.hireme.hireme.api.dto.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class LlmResponse {

    @SerializedName("relevance_score")
    private Integer relevanceScore;

    @SerializedName("match_rationale")
    private String matchRationale;

    @SerializedName("extracted_skills")
    private List<String> extractedSkills;
}
