package io.hireme.hireme.job.notification;

public interface NotificationService {
    void sendNotification(String jobTitle, String jobUrl, int score, String source);
}
