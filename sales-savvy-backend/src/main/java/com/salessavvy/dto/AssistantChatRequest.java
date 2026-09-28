package com.salessavvy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AssistantChatRequest(
        @NotBlank
        @Size(max = 1000)
        String message,
        @Size(max = 12)
        List<@NotNull @Valid AssistantChatTurn> history) {
}
