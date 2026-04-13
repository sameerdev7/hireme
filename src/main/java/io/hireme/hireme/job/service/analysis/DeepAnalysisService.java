package io.hireme.hireme.job.service.analysis;

import io.hireme.hireme.observation.analysis.JobAnalysisResult;

public interface DeepAnalysisService {
    public JobAnalysisResult runDeepAnalysis(String title, String description);
}
