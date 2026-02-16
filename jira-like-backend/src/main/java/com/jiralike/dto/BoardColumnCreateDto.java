package com.jiralike.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BoardColumnCreateDto {
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    private String color;

    @NotNull
    private Long projectId;
}
