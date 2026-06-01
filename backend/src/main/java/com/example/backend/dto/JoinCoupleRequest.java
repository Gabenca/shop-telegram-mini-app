package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinCoupleRequest {

    @NotBlank(message = "Код приглашения обязателен")
    @Size(min = 6, max = 6, message = "Код должен содержать 6 символов")
    private String inviteCode;
}
