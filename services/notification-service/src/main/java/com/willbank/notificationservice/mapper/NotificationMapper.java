package com.willbank.notificationservice.mapper;

import com.willbank.notificationservice.domain.Notification;
import com.willbank.notificationservice.dto.NotificationResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Notification entity and DTOs
 */
public class NotificationMapper {

    /**
     * Convert Notification entity to NotificationResponse DTO
     */
    public static NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .customerId(notification.getCustomerId())
                .email(notification.getEmail())
                .type(notification.getType())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .status(notification.getStatus())
                .errorMessage(notification.getErrorMessage())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .build();
    }

    /**
     * Convert list of Notification entities to list of NotificationResponse DTOs
     */
    public static List<NotificationResponse> toResponseList(List<Notification> notifications) {
        if (notifications == null) {
            return null;
        }

        return notifications.stream()
                .map(NotificationMapper::toResponse)
                .collect(Collectors.toList());
    }
}
