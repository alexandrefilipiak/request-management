package io.github.alexandrefilipiak.requestmanagement;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RequestLifecycleIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("a request goes from creation to publication and gets a publication number")
    void happyPath() throws Exception {
        //given
        String id = create("My request", "Some content");

        //when
        mvc.perform(post("/requests/{id}/verify", id)).andExpect(status().isNoContent());
        mvc.perform(post("/requests/{id}/accept", id)).andExpect(status().isNoContent());
        mvc.perform(post("/requests/{id}/publish", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicationNumber").isNumber());

        //then
        mvc.perform(get("/requests/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PUBLISHED"))
                .andExpect(jsonPath("$.publicationNumber").isNumber());
    }

    @Test
    @DisplayName("a rejected request keeps its reason and refuses further transitions")
    void rejection() throws Exception {
        //given
        String id = create("Another request", "Content");
        mvc.perform(post("/requests/{id}/verify", id)).andExpect(status().isNoContent());

        //when
        mvc.perform(post("/requests/{id}/reject", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason": "Incomplete"}
                                """))
                .andExpect(status().isNoContent());

        //then
        mvc.perform(get("/requests/{id}", id))
                .andExpect(jsonPath("$.state").value("REJECTED"))
                .andExpect(jsonPath("$.reason").value("Incomplete"));
        mvc.perform(post("/requests/{id}/accept", id))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("two published requests get distinct, increasing publication numbers")
    void publicationNumbersAreUnique() throws Exception {
        //given
        String first = create("First", "Content");
        String second = create("Second", "Content");

        //when
        long n1 = publish(first);
        long n2 = publish(second);

        //then
        assertThat(n2).isGreaterThan(n1);
    }

    private String create(String name, String content) throws Exception {
        String body = mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s", "content": "%s"}
                                """.formatted(name, content)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id").toString();
    }

    private long publish(String id) throws Exception {
        mvc.perform(post("/requests/{id}/verify", id)).andExpect(status().isNoContent());
        mvc.perform(post("/requests/{id}/accept", id)).andExpect(status().isNoContent());
        String body = mvc.perform(post("/requests/{id}/publish", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.publicationNumber")).longValue();
    }
}