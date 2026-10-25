package io.github.alexandrefilipiak.requestmanagement.core.usecases;

import io.github.alexandrefilipiak.requestmanagement.core.domain.Request;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases on requests: creation, content edition and lifecycle transitions.
 * <p>
 * Each method loads the request by id, applies the operation on the domain object
 * and persists it, within one transaction. Business rules are enforced by
 * {@link Request}; the exceptions it throws propagate unchanged.
 *
 * @throws RequestNotFoundException from every method taking an id, if no request has that id
 */
@Service
public class RequestService {

    private final RequestRepository repository;

    public RequestService(RequestRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Long create(CreateRequestCommand command) {
        Request request = repository.save(Request.create(command.name(), command.content()));
        return request.getId();
    }

    @Transactional
    public void verify(long id) {
        Request request = load(id);
        request.verify();
    }

    @Transactional
    public void reject(long id, RejectRequestCommand command) {
        Request request = load(id);
        request.reject(command.reason());
        repository.save(request);
    }

    @Transactional
    public Long publish(long id) {
        Request request = load(id);
        request.publish(repository.nextPublicationNumber());
        request = repository.save(request);
        return request.getPublicationNumber();
    }

    @Transactional
    public void accept(long id) {
        Request request = load(id);
        request.accept();
        repository.save(request);
    }

    @Transactional
    public void delete(long id, DeleteRequestCommand command) {
        Request request = load(id);
        request.delete(command.reason());
        repository.save(request);
    }

    @Transactional
    public Request updateContent(long id, UpdateContentCommand command) {
        Request request = load(id);
        request.updateContent(command.content());
        return repository.save(request);
    }

    @Transactional(readOnly = true)
    public Request get(long id) {
        return load(id);
    }

    private Request load(long id) {
        return repository.findById(id).orElseThrow(() -> new RequestNotFoundException(id));
    }
}