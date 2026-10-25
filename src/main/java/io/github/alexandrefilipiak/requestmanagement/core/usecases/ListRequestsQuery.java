package io.github.alexandrefilipiak.requestmanagement.core.usecases;

import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState;

public record ListRequestsQuery(String name, RequestState state, int page, int size) {

    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;

    public ListRequestsQuery {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > MAX_SIZE) throw new IllegalArgumentException("size must be between 1 and " + MAX_SIZE);
        name = (name == null || name.isBlank()) ? null : name.trim();
    }
}
