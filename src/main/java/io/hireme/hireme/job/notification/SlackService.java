package io.hireme.hireme.job.notification;

import java.util.Map;
import java.util.Objects;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Log4j2
public class SlackService implements NotificationService {

    private final String webhookUrl;
    private final RestClient restClient;

    public SlackService(@Value("${slack.webhook.url}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.restClient = RestClient.create();
    }

    @Override
    public void sendNotification(String jobTitle, String jobUrl, int score, String source) {
        String message = """
                \ud83d\udd25 *New High Priority Job Found!*
                Title:\s""" + jobTitle + "\n" +
                "Score: " + score + "/100 \n" +
                "Source: " + source + "\n" +
                "Apply here: " + jobUrl;

        Map<String, String> payload = Map.of("text", message);

        try {
            restClient.post()
                    .uri(Objects.requireNonNull(webhookUrl))
                    .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                    .body(Objects.requireNonNull(payload))
                    .retrieve()
                    .toBodilessEntity();

            log.info("✅ Slack notification sent!");
        } catch (Exception e) {
            log.error("❌ Failed to send Slack notification: {}", e.getMessage());
        }
    }
}