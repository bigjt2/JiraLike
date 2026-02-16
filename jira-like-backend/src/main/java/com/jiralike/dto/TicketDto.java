package com.jiralike.dto;

import com.jiralike.entity.Ticket.Priority;
import com.jiralike.entity.Ticket.TicketType;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class TicketDto {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private TicketType ticketType;
    private Integer position;
    private Integer storyPoints;
    private LocalDate dueDate;
    private Long projectId;
    private String projectKey;
    private Long columnId;
    private String columnName;
    private UserDto assignee;
    private UserDto reporter;
    private List<CommentDto> comments;
    private Instant createdAt;
    private Instant updatedAt;
}
