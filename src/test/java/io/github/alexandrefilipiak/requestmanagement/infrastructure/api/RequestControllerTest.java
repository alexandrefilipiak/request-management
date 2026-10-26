package io.github.alexandrefilipiak.requestmanagement.infrastructure.api;

import io.github.alexandrefilipiak.requestmanagement.core.domain.ContentNotEditableException;
import io.github.alexandrefilipiak.requestmanagement.core.domain.InvalidTransitionException;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.ListRequestsQuery;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.RequestNotFoundException;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.RequestPage;
import io.github.alexandrefilipiak.requestmanagement.core.usecases.RequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestEvent.PUBLISH;
import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestFixtures.aRequestIn;
import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    RequestService service;

    @Test
    @DisplayName("POST /requests returns 201 with the id and a Location header")
    void create() throws Exception {
        when(service.create(any())).thenReturn(42L);

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "My request", "content": "Some content"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/requests/42")))
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    @DisplayName("POST /requests with a blank name returns 400")
    void createWithBlankName() throws Exception {
        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "  ", "content": "Some content"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("GET /requests/{id} returns the request details")
    void getRequest() throws Exception {
        when(service.get(42L)).thenReturn(aRequestIn(VERIFIED));

        mvc.perform(get("/requests/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.state").value("VERIFIED"))
                .andExpect(jsonPath("$.publicationNumber").doesNotExist());
    }

    @Test
    @DisplayName("GET /requests/{id} returns 404 for an unknown id")
    void getUnknownRequest() throws Exception {
        when(service.get(42L)).thenThrow(new RequestNotFoundException(42L));

        mvc.perform(get("/requests/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("42")));
    }

    @Test
    @DisplayName("PUT /requests/{id}/content returns 204")
    void updateContent() throws Exception {
        mvc.perform(put("/requests/42/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "New content"}
                                """))
                .andExpect(status().isNoContent());

        verify(service).updateContent(eq(42L), any());
    }

    @Test
    @DisplayName("editing content in a non-editable state returns 409")
    void updateContentNotEditable() throws Exception {
        doThrow(new ContentNotEditableException(ACCEPTED)).when(service).updateContent(eq(42L), any());

        mvc.perform(put("/requests/42/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "New content"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /requests/{id}/verify returns 204")
    void verifyRequest() throws Exception {
        mvc.perform(post("/requests/42/verify"))
                .andExpect(status().isNoContent());

        verify(service).verify(42L);
    }

    @Test
    @DisplayName("POST /requests/{id}/accept returns 204")
    void acceptRequest() throws Exception {
        mvc.perform(post("/requests/42/accept"))
                .andExpect(status().isNoContent());

        verify(service).accept(42L);
    }

    @Test
    @DisplayName("POST /requests/{id}/reject with a reason returns 204")
    void rejectRequest() throws Exception {
        mvc.perform(post("/requests/42/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason": "Incomplete"}
                                """))
                .andExpect(status().isNoContent());

        verify(service).reject(eq(42L), any());
    }

    @Test
    @DisplayName("POST /requests/{id}/reject without a reason returns 400")
    void rejectWithoutReason() throws Exception {
        mvc.perform(post("/requests/42/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST /requests/{id}/delete with a reason returns 204")
    void deleteRequest() throws Exception {
        mvc.perform(post("/requests/42/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason": "Duplicate"}
                                """))
                .andExpect(status().isNoContent());

        verify(service).delete(eq(42L), any());
    }

    @Test
    @DisplayName("POST /requests/{id}/publish returns the publication number")
    void publishRequest() throws Exception {
        when(service.publish(42L)).thenReturn(7L);

        mvc.perform(post("/requests/42/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicationNumber").value(7));
    }

    @Test
    @DisplayName("an invalid transition returns 409")
    void invalidTransition() throws Exception {
        when(service.publish(42L)).thenThrow(new InvalidTransitionException(CREATED, PUBLISH));

        mvc.perform(post("/requests/42/publish"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("PUBLISH")));
    }

    @Test
    @DisplayName("a concurrent modification returns 409")
    void concurrentModification() throws Exception {
        doThrow(new ObjectOptimisticLockingFailureException("Request", 42L)).when(service).verify(42L);

        mvc.perform(post("/requests/42/verify"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(not(containsString("Request"))));
    }

    @Test
    @DisplayName("an unexpected error returns 500 without details")
    void unexpectedError() throws Exception {
        doThrow(new IllegalStateException("Simulated failure for testing the 500 handler (this exception is normal, it means the test passes)")).when(service).verify(42L);

        mvc.perform(post("/requests/42/verify"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value(not(containsString("Simulated failure"))));
    }

    @Test
    @DisplayName("GET /requests/{id}/history returns the entries oldest first")
    void history() throws Exception {
        when(service.getHistory(42L)).thenReturn(aRequestIn(VERIFIED).getHistory());

        mvc.perform(get("/requests/42/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(42))
                .andExpect(jsonPath("$.entries.length()").value(2))
                .andExpect(jsonPath("$.entries[0].event").value("CREATE"))
                .andExpect(jsonPath("$.entries[0].fromState").value(nullValue()))
                .andExpect(jsonPath("$.entries[1].fromState").value("CREATED"))
                .andExpect(jsonPath("$.entries[1].toState").value("VERIFIED"));
    }

    @Test
    @DisplayName("GET /requests/{id}/history returns 404 for an unknown id")
    void historyUnknown() throws Exception {
        when(service.getHistory(42L)).thenThrow(new RequestNotFoundException(42L));

        mvc.perform(get("/requests/42/history"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /requests applies the default page and size")
    void listDefaults() throws Exception {
        when(service.list(any())).thenReturn(new RequestPage(List.of(), 0, 10, 0));

        mvc.perform(get("/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        ArgumentCaptor<ListRequestsQuery> captor = ArgumentCaptor.forClass(ListRequestsQuery.class);
        verify(service).list(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new ListRequestsQuery(null, null, 0, 10));
    }

    @Test
    @DisplayName("GET /requests passes the filters and paging to the use case")
    void listWithFilters() throws Exception {
        when(service.list(any())).thenReturn(new RequestPage(List.of(aRequestIn(VERIFIED)), 1, 5, 6));

        mvc.perform(get("/requests").param("name", "budget").param("state", "VERIFIED")
                        .param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].state").value("VERIFIED"))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(service).list(new ListRequestsQuery("budget", VERIFIED, 1, 5));
    }

    @Test
    @DisplayName("GET /requests with a size above 100 returns 400")
    void listSizeTooLarge() throws Exception {
        mvc.perform(get("/requests").param("size", "200"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("GET /requests with an unknown state returns 400")
    void listUnknownState() throws Exception {
        mvc.perform(get("/requests").param("state", "WHATEVER"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}