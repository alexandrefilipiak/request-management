package io.github.alexandrefilipiak.requestmanagement.core.usecases;

import io.github.alexandrefilipiak.requestmanagement.core.domain.Request;

import java.util.List;

public record RequestPage(List<Request> items, int page, int size, long totalElements) {

    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }
}