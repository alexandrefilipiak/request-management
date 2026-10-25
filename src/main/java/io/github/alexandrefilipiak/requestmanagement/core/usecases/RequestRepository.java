package io.github.alexandrefilipiak.requestmanagement.core.usecases;

import io.github.alexandrefilipiak.requestmanagement.core.domain.Request;

import java.util.Optional;

public interface RequestRepository {
    Optional<Request> findById(long id);
    Request save(Request request);
    long nextPublicationNumber();
    RequestPage search(ListRequestsQuery query);
}