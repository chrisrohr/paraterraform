package org.paraterraform.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.base.UUIDs;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;
import org.paraterraform.model.StateLock;

import java.time.Instant;

@DisplayName("StateLockDao")
class StateLockDaoTest {

    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<StateLockDao> daoExtension = Jdbi3DaoExtension.<StateLockDao>builder()
            .daoType(StateLockDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private StateLockDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();

        handle.execute("truncate table state_locks");
    }

    @Nested
    class FindLockByStateName {

        @Test
        void shouldReturnTheStateLockWithTheGivenNameWhenFound() {
            handle.createUpdate("insert into state_locks (id, state_name, operation, locked_by, version, created_at) " +
                    "values (:id, :stateName, :operation, :lockedBy, :version, :createdAt)")
                    .bind("id", UUIDs.randomUUIDString())
                    .bind("stateName", "foo")
                    .bind("operation", "DoIt")
                    .bind("lockedBy", "me")
                    .bind("version", "1.2")
                    .bind("createdAt", Instant.now())
                    .execute();

            var lock = dao.findLockByStateName("foo");
            assertThat(lock).isPresent();
        }

        @Test
        void shouldReturnOptionalEmptyWhenLockIsNotFound() {
            var lock = dao.findLockByStateName("foo");
            assertThat(lock).isEmpty();
        }
    }

    @Nested
    class InsertLock {

        @Test
        void shouldInsertGivenLock() {
            var lock = StateLock.builder()
                    .id(UUIDs.randomUUIDString())
                    .stateName("foo")
                    .operation("DoIt")
                    .lockedBy("me")
                    .createdAt(Instant.now())
                    .version("1.2")
                    .build();

            dao.insertLock(lock);

            var count = handle.createQuery("select count(*) from state_locks where state_name = :name")
                    .bind("name", "foo")
                    .mapTo(Integer.class)
                    .first();

            assertThat(count).isOne();
        }
    }

    @Nested
    class DeleteLock {

        @Test
        void shouldDeleteLockAndReturnOneWhenLockExistsAndDeletes() {
            handle.createUpdate("insert into state_locks (id, state_name, operation, locked_by, version, created_at) " +
                            "values (:id, :stateName, :operation, :lockedBy, :version, :createdAt)")
                    .bind("id", UUIDs.randomUUIDString())
                    .bind("stateName", "foo")
                    .bind("operation", "DoIt")
                    .bind("lockedBy", "me")
                    .bind("version", "1.2")
                    .bind("createdAt", Instant.now())
                    .execute();

            var count = dao.deleteLock("foo");
            assertThat(count).isOne();
        }

        @Test
        void shouldReturnZeroWhenGivenStateLockIsNotFound() {
            var count = dao.deleteLock("foo");
            assertThat(count).isZero();
        }
    }
}
