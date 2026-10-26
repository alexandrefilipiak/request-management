package io.github.alexandrefilipiak.requestmanagement.infrastructure.api;

import io.github.alexandrefilipiak.requestmanagement.core.usecases.RequestPage;

import java.util.List;

record RequestPageResponse(List<RequestDetailsResponse> items, int page, int size,
                           long totalElements, int totalPages) {

    static RequestPageResponse from(RequestPage p) {
        return new RequestPageResponse(p.items().stream().map(RequestDetailsResponse::from).toList(),
                p.page(), p.size(), p.totalElements(), p.totalPages());
    }
}