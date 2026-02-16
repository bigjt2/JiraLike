package com.jiralike.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class CommentDto {
    private Long id;
    private String content;
    private Long ticketId;
    private UserDto author;
    private Instant createdAt;
    private Instant updatedAt;
}
