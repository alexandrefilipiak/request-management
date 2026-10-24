package io.github.alexandrefilipiak.requestmanagement.usecases;

import io.github.alexandrefilipiak.requestmanagement.domain.Request;
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
    public Request create(String name, String content) {
        return repository.save(Request.create(name, content));
    }

    @Transactional
    public Request verify(long id) {
        Request request = load(id);
        request.verify();
        return repository.save(request);
    }

    @Transactional
    public Request reject(long id, String reason) {
        Request request = load(id);
        request.reject(reason);
        return repository.save(request);
    }

    @Transactional
    public Request publish(long id) {
        Request request = load(id);
        request.publish(repository.nextPublicationNumber());
        return repository.save(request);
    }

    @Transactional
    public Request accept(long id) {
        Request request = load(id);
        request.accept();
        return repository.save(request);
    }

    @Transactional
    public Request delete(long id, String reason) {
        Request request = load(id);
        request.delete(reason);
        return repository.save(request);
    }

    @Transactional
    public Request updateContent(long id, String content) {
        Request request = load(id);
        request.updateContent(content);
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