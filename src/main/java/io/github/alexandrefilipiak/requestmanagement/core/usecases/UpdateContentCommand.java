package io.github.alexandrefilipiak.requestmanagement.core.usecases;

import jakarta.validation.constraints.NotBlank;

public record UpdateContentCommand(@NotBlank String content) {}