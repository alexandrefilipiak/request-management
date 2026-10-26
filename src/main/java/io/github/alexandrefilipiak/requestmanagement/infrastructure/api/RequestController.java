package io.github.alexandrefilipiak.requestmanagement.infrastructure.api;

import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/requests")
public class RequestController {

    RequestService service;

    RequestController(RequestService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<CreatedResponse> create(@Valid @RequestBody CreateRequestCommand command) {
        long id = service.create(command);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(new CreatedResponse(id));
    }

    @GetMapping("/{id}")
    RequestDetailsResponse get(@PathVariable long id) {
        return RequestDetailsResponse.from(service.get(id));
    }

    @PutMapping("/{id}/content")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void content(@PathVariable long id, @Valid @RequestBody UpdateContentCommand command) {
        service.updateContent(id, command);
    }

    @PostMapping("/{id}/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void verify(@PathVariable long id) {
        service.verify(id);
    }

    @PostMapping("/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void accept(@PathVariable long id) {
        service.accept(id);
    }

    @PostMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable long id, @Valid @RequestBody DeleteRequestCommand command) {
        service.delete(id, command);
    }

    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reject(@PathVariable long id, @Valid @RequestBody RejectRequestCommand command) {
        service.reject(id, command);
    }

    @PostMapping("/{id}/publish")
    PublishedResponse publish(@PathVariable long id) {
        return new PublishedResponse(service.publish(id));
    }

    @GetMapping("/{id}/history")
    HistoryResponse history(@PathVariable long id) {
        return HistoryResponse.of(id, service.getHistory(id));
    }

    @GetMapping
    RequestPageResponse list(@RequestParam(required = false) String name,
                             @RequestParam(required = false) RequestState state,
                             @RequestParam(defaultValue = "0") @Min(0) int page,
                             @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return RequestPageResponse.from(service.list(new ListRequestsQuery(name, state, page, size)));
    }
}