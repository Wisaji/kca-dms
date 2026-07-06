package ke.ac.kca.dms.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification service for sending alerts to system users.
 */
public class NotificationService {

    private final List<Notification> sentNotifications = new ArrayList<>();

    public void sendNotification(String recipientId, String message) {
        sentNotifications.add(new Notification(recipientId, message));
    }

    public List<Notification> getSentNotifications() {
        return sentNotifications;
    }

    public boolean wasNotified(String recipientId) {
        return sentNotifications.stream()
            .anyMatch(n -> n.recipientId().equals(recipientId));
    }

    public record Notification(String recipientId, String message) {}
}
