package io.github.alexandrefilipiak.requestmanagement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RequestManagementApplicationTests {

	@Autowired
    JdbcClient jdbc;

	@Test
	@DisplayName("starts against PostgreSQL and applies Flyway migrations")
	void startsAndMigrates() {
		boolean exists = jdbc.sql("""
            SELECT EXISTS (
                SELECT 1 FROM information_schema.sequences
                WHERE sequence_name = 'publication_number_seq'
            )
            """)
				.query(Boolean.class)
				.single();

		assertThat(exists).isTrue();
	}

}
