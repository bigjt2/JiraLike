package com.jiralike.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentCreateDto {
    @NotBlank
    private String content;

    @NotNull
    private Long authorId;
}
