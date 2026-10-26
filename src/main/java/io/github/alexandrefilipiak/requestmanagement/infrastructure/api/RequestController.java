package io.github.alexandrefilipiak.requestmanagement.infrastructure.api;

import io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(summary = "Create a request", description = "The request starts in CREATED. Name and content are mandatory.")
    @ApiResponse(responseCode = "201", description = "Created; the Location header points to the new request")
    @ApiResponse(responseCode = "400", description = "The name or the content is missing or blank; both are mandatory")
    ResponseEntity<CreatedResponse> create(@Valid @RequestBody CreateRequestCommand command) {
        long id = service.create(command);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(new CreatedResponse(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a request")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", description = "No request with that id")
    RequestDetailsResponse get(@PathVariable long id) {
        return RequestDetailsResponse.from(service.get(id));
    }

    @PutMapping("/{id}/content")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Replace the content", description = "Only allowed while the request is CREATED or VERIFIED; the state is not affected.")
    @ApiResponse(responseCode = "204")
    @ApiResponse(responseCode = "404", description = "No request with that id")
    @ApiResponse(responseCode = "409", description = "Content is not editable in the current state")
    void updateContent(@PathVariable long id, @Valid @RequestBody UpdateContentCommand command) {
        service.updateContent(id, command);
    }

    @PostMapping("/{id}/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Verify a request", description = "CREATED to VERIFIED.")
    @ApiResponse(responseCode = "204")
    @ApiResponse(responseCode = "404", description = "No request with that id")
    @ApiResponse(responseCode = "409", description = "Transition not allowed from the current state, or concurrent modification")
    void verify(@PathVariable long id) {
        service.verify(id);
    }

    @PostMapping("/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Accept a request", description = "VERIFIED to ACCEPTED.")
    @ApiResponse(responseCode = "204")
    @ApiResponse(responseCode = "404", description = "No request with that id")
    @ApiResponse(responseCode = "409", description = "Transition not allowed from the current state, or concurrent modification")
    void accept(@PathVariable long id) {
        service.accept(id);
    }

    @PostMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a request", description = "CREATED to DELETED. The request is kept with its reason. A reason is mandatory.")
    @ApiResponse(responseCode = "204")
    @ApiResponse(responseCode = "400", description = "The reason is missing or blank; a reason is mandatory to delete a request")
    @ApiResponse(responseCode = "404", description = "No request with that id")
    @ApiResponse(responseCode = "409", description = "Transition not allowed from the current state, or concurrent modification")
    void delete(@PathVariable long id, @Valid @RequestBody DeleteRequestCommand command) {
        service.delete(id, command);
    }

    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reject a request", description = "VERIFIED or ACCEPTED to REJECTED. A reason is mandatory.")
    @ApiResponse(responseCode = "204")
    @ApiResponse(responseCode = "400", description = "The reason is missing or blank; a reason is mandatory to reject a request")
    @ApiResponse(responseCode = "404", description = "No request with that id")
    @ApiResponse(responseCode = "409", description = "Transition not allowed from the current state, or concurrent modification")
    void reject(@PathVariable long id, @Valid @RequestBody RejectRequestCommand command) {
        service.reject(id, command);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a request", description = "ACCEPTED to PUBLISHED. Assigns a unique publication number.")
    @ApiResponse(responseCode = "200", description = "Published; the body carries the publication number")
    @ApiResponse(responseCode = "404", description = "No request with that id")
    @ApiResponse(responseCode = "409", description = "Transition not allowed from the current state, or concurrent modification")
    PublishedResponse publish(@PathVariable long id) {
        return new PublishedResponse(service.publish(id));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get the history of a request", description = "State changes, oldest first, starting with the creation.")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", description = "No request with that id")
    HistoryResponse history(@PathVariable long id) {
        return HistoryResponse.of(id, service.getHistory(id));
    }

    @GetMapping
    @Operation(summary = "List requests", description = "Paginated, filterable by partial case-insensitive name and by state. Newest first.")
    RequestPageResponse list(@RequestParam(required = false) String name,
                             @RequestParam(required = false) RequestState state,
                             @RequestParam(defaultValue = "0") @Min(0) int page,
                             @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return RequestPageResponse.from(service.list(new ListRequestsQuery(name, state, page, size)));
    }
}