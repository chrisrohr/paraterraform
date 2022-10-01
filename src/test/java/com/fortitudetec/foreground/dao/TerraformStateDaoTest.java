package com.fortitudetec.foreground.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.collect.KiwiLists.first;

import com.fortitudetec.foreground.model.TerraformState;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;
import org.postgresql.util.PSQLException;

@DisplayName("TerraformStateDao")
class TerraformStateDaoTest {

    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<TerraformStateDao> daoExtension = Jdbi3DaoExtension.<TerraformStateDao>builder()
            .daoType(TerraformStateDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private TerraformStateDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();

        handle.execute("truncate table terraform_states");
    }

    private long saveTestTerraformStateRecord(String name, String content) {
        return handle.createUpdate("insert into terraform_states (name, content) values (:name, :content)")
                .bind("name", name)
                .bind("content", content)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .first();
    }

    @Nested
    class List {

        @Test
        void shouldReturnTheEntireListOfTerraformStates() {
            saveTestTerraformStateRecord("Sample state", "{}");

            var states = dao.list();

            var state = first(states);
            assertThat(state.getName()).isEqualTo("Sample state");
            assertThat(state.getContent()).isBlank();
            assertThat(first(states).getUploadedAt()).isNotNull();
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnTerraformStateWithGivenId() {
            var id = saveTestTerraformStateRecord("To retrieve", "{}");

            var state = dao.findById(id).orElseThrow();

            assertThat(state.getId()).isEqualTo(id);
            assertThat(state.getName()).isEqualTo("To retrieve");
            assertThat(state.getContent()).isBlank();
            assertThat(state.getUploadedAt()).isNotNull();
        }

        @Test
        void shouldReturnOptionalEmptyWhenNoRecordFound() {
            var state = dao.findById(1L);

            assertThat(state).isEmpty();
        }
    }

    @Nested
    class Insert {

        @Test
        void shouldInsertGivenTerraformState() {
            var state = TerraformState.builder()
                    .name("The state")
                    .content("{}")
                    .build();

            var generatedId = dao.insert(state);

            assertThat(generatedId).isPositive();

            var savedState = handle.createQuery("select * from terraform_states where id = :id")
                    .bind("id", generatedId)
                    .mapToMap()
                    .first();

            assertThat(savedState)
                    .containsEntry("id", generatedId)
                    .containsEntry("name", "The state")
                    .containsEntry("content", "{}");

            assertThat(savedState.get("uploaded_at")).isNotNull();
        }

        @Test
        void shouldThrowErrorWhenNameIsNull() {
            var state = TerraformState.builder()
                    .content("{}")
                    .build();

            assertThatThrownBy(() -> dao.insert(state))
                    .isInstanceOf(UnableToExecuteStatementException.class)
                    .cause()
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("null value in column \"name\" of relation \"terraform_states\" violates not-null constraint");
        }

        @Test
        void shouldThrowErrorWhenContentIsNull() {
            var state = TerraformState.builder()
                    .name("State without a state")
                    .build();

            assertThatThrownBy(() -> dao.insert(state))
                    .isInstanceOf(UnableToExecuteStatementException.class)
                    .cause()
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("null value in column \"content\" of relation \"terraform_states\" violates not-null constraint");
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldDeleteTerraformStateWithGivenIdAndReturnCountOfOne() {
            var id = saveTestTerraformStateRecord("To delete", "{}");

            var deletedCount = dao.deleteById(id);

            assertThat(deletedCount).isOne();

            var countOfRecordsWithId = handle.createQuery("select count(*) from terraform_states where id = :id")
                    .bind("id", id)
                    .mapTo(Integer.class)
                    .first();

            assertThat(countOfRecordsWithId).isZero();
        }
    }
}
