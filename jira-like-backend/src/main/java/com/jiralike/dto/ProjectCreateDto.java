package com.jiralike.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectCreateDto {
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Size(min = 2, max = 10)
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Project key must be uppercase letters and numbers only")
    private String key;

    private String description;
}
