package com.pcveldi.notification.dto;

import com.pcveldi.notification.model.NotificationEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "body is required")
    @Size(max = 2000)
    private String body;

    @NotEmpty(message = "at least one channel must be specified")
    private List<NotificationEvent.Channel> channels;

    private NotificationEvent.Priority priority = NotificationEvent.Priority.NORMAL;
}
