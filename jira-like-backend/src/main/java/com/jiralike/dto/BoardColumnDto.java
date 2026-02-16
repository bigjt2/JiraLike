package com.jiralike.dto;

import lombok.Data;
import java.util.List;

@Data
public class BoardColumnDto {
    private Long id;
    private String name;
    private Integer position;
    private String color;
    private Long projectId;
    private List<TicketDto> tickets;
}
