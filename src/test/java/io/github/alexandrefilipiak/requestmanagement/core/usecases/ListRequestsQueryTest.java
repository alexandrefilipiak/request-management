package io.github.alexandrefilipiak.requestmanagement.core.usecases;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.github.alexandrefilipiak.requestmanagement.core.domain.RequestState.VERIFIED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ListRequestsQueryTest {

    @Test
    @DisplayName("valid values are stored as given")
    void validValuesAreStored() {
        ListRequestsQuery query = new ListRequestsQuery("budget", VERIFIED, 2, 25);

        assertThat(query.name()).isEqualTo("budget");
        assertThat(query.state()).isEqualTo(VERIFIED);
        assertThat(query.page()).isEqualTo(2);
        assertThat(query.size()).isEqualTo(25);
    }

    @Test
    @DisplayName("the name is trimmed")
    void nameIsTrimmed() {
        ListRequestsQuery query = new ListRequestsQuery("  budget ", null, 0, 10);

        assertThat(query.name()).isEqualTo("budget");
    }

    @ParameterizedTest(name = "name [{0}] becomes null")
    @ValueSource(strings = {"", "   "})
    @DisplayName("a blank name means no name filter")
    void blankNameIsNoFilter(String name) {
        ListRequestsQuery query = new ListRequestsQuery(name, null, 0, 10);

        assertThat(query.name()).isNull();
    }

    @Test
    @DisplayName("a null name means no name filter")
    void nullNameIsNoFilter() {
        ListRequestsQuery query = new ListRequestsQuery(null, null, 0, 10);

        assertThat(query.name()).isNull();
    }

    @Test
    @DisplayName("a negative page is rejected")
    void negativePage() {
        Throwable thrown = catchThrowable(() -> new ListRequestsQuery(null, null, -1, 10));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "size {0} is rejected")
    @ValueSource(ints = {0, -1, 101})
    @DisplayName("a size outside 1..100 is rejected")
    void sizeOutOfRange(int size) {
        Throwable thrown = catchThrowable(() -> new ListRequestsQuery(null, null, 0, size));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "size {0} is accepted")
    @ValueSource(ints = {1, 10, 100})
    @DisplayName("the bounds of the size range are accepted")
    void sizeBounds(int size) {
        ListRequestsQuery query = new ListRequestsQuery(null, null, 0, size);

        assertThat(query.size()).isEqualTo(size);
    }
}