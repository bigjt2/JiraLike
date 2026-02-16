package com.jiralike.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private Instant createdAt;
}
