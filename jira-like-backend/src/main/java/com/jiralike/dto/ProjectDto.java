package com.jiralike.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class ProjectDto {
    private Long id;
    private String name;
    private String key;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private List<BoardColumnDto> columns;
}
