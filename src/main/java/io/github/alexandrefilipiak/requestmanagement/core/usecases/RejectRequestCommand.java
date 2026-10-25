package io.github.alexandrefilipiak.requestmanagement.core.usecases;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequestCommand(@NotBlank @Size(max = 1000) String reason) {}