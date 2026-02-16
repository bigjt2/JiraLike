package com.jiralike.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketMoveDto {
    @NotNull
    private Long columnId;

    @NotNull
    private Integer position;
}
