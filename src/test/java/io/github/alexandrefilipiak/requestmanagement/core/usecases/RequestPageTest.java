package io.github.alexandrefilipiak.requestmanagement.core.usecases;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestPageTest {

    @ParameterizedTest(name = "{0} elements with size {1} give {2} pages")
    @CsvSource({
            "0, 10, 0",
            "1, 10, 1",
            "10, 10, 1",
            "11, 10, 2",
            "25, 10, 3"
    })
    @DisplayName("total pages is the ceiling of elements over size")
    void totalPages(long totalElements, int size, int expected) {
        RequestPage page = new RequestPage(List.of(), 0, size, totalElements);

        assertThat(page.totalPages()).isEqualTo(expected);
    }

}
