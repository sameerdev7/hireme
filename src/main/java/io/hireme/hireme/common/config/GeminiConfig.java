package io.hireme.hireme.common.config;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class GeminiConfig {

    @Value("${analysis.gemini.api-key}")
    private String googleApiKey;

    @Bean
    public Client geminiClient() {
        return Client.builder()
                .apiKey(googleApiKey)
                .build();
    }
}
