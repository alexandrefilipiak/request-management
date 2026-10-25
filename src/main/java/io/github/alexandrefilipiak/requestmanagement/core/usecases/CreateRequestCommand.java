package io.github.alexandrefilipiak.requestmanagement.core.usecases;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRequestCommand(
        @NotBlank @Size(max = 255) String name,
        @NotBlank String content) {}