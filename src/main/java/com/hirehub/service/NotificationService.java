package com.hirehub.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.hirehub.model.Notification;
import com.hirehub.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(String recipientEmail, String title, String message, String type, String link) {
        if (recipientEmail == null || recipientEmail.isBlank()) return;
        Notification n = new Notification(recipientEmail, title, message, type, link);
        notificationRepository.save(n);
    }

    public List<Notification> getUserNotifications(String email) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    public long getUnreadCount(String email) {
        return notificationRepository.countByRecipientEmailAndReadFalse(email);
    }

    public void markAllAsRead(String email) {
        List<Notification> list = notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
        for (Notification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
    }
}
