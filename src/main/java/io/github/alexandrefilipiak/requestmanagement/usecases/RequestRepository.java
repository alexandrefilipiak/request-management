package io.github.alexandrefilipiak.requestmanagement.usecases;

import io.github.alexandrefilipiak.requestmanagement.domain.Request;

import java.util.Optional;

public interface RequestRepository {
    Optional<Request> findById(long id);
    Request save(Request request);
    long nextPublicationNumber();
}