package com.jiralike.dto;

import com.jiralike.entity.Ticket.Priority;
import com.jiralike.entity.Ticket.TicketType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TicketCreateDto {
    @NotBlank
    @Size(min = 1, max = 200)
    private String title;

    private String description;

    private Priority priority = Priority.MEDIUM;

    private TicketType ticketType = TicketType.TASK;

    private Integer storyPoints;

    private LocalDate dueDate;

    @NotNull
    private Long projectId;

    @NotNull
    private Long columnId;

    private Long assigneeId;

    private Long reporterId;
}
