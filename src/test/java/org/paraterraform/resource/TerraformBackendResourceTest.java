package org.paraterraform.resource;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertInternalServerErrorResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNotFoundResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertResponseStatusCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.base.UUIDs;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.kiwiproject.test.util.Fixtures;
import org.paraterraform.dao.StateLockDao;
import org.paraterraform.dao.TerraformStateDao;
import org.paraterraform.model.StateLock;
import org.paraterraform.model.TerraformState;

import java.util.Map;
import java.util.Optional;

@DisplayName("TerraformBackendResource")
@ExtendWith(DropwizardExtensionsSupport.class)
class TerraformBackendResourceTest {

    private static final TerraformStateDao TERRAFORM_STATE_DAO = mock(TerraformStateDao.class);
    private static final StateLockDao STATE_LOCK_DAO = mock(StateLockDao.class);
    private static final TerraformBackendResource TERRAFORM_BACKEND_RESOURCE = new TerraformBackendResource(TERRAFORM_STATE_DAO, STATE_LOCK_DAO);

    private static final ResourceExtension APP = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addProvider(MultiPartFeature.class)
            .addResource(TERRAFORM_BACKEND_RESOURCE)
            .addProvider(JaxrsExceptionMapper.class)
            .build();

    @BeforeEach
    void setUp() {
        reset(TERRAFORM_STATE_DAO, STATE_LOCK_DAO);
    }

    @Nested
    class GetState {

        @Test
        void shouldReturnTerraformStateWithGivenNameWhenFound() {
            var content = Fixtures.fixture("state.tf");
            when(TERRAFORM_STATE_DAO.findLatestStateContentByName("foo")).thenReturn(Optional.of(content));

            var response = APP.client().target("/state/{stateName}")
                    .resolveTemplate("stateName", "foo")
                    .request()
                    .get();

            assertOkResponse(response);

            assertThat(response.readEntity(String.class)).isEqualTo(content);
        }

        @Test
        void shouldReturn404WhenRequestedStateDoesNotExist() {
            when(TERRAFORM_STATE_DAO.findLatestStateContentByName("foo")).thenReturn(Optional.empty());

            var response = APP.client().target("/state/{stateName}")
                    .resolveTemplate("stateName", "foo")
                    .request()
                    .get();

            assertNotFoundResponse(response);
        }
    }

    @Nested
    class UpdateState {

        @Test
        void shouldReturn423WhenAlreadyLocked() {
            var id = UUIDs.randomUUIDString();
            var lock = StateLock.builder()
                    .id(id)
                    .build();

            when(STATE_LOCK_DAO.findLockByStateName("foo")).thenReturn(Optional.of(lock));

            var response = APP.client().target("/state/{stateName}")
                    .resolveTemplate("stateName", "foo")
                    .queryParam("ID", UUIDs.randomUUIDString())
                    .request()
                    .post(json(Map.of()));

            assertResponseStatusCode(response, 423);

            verifyNoInteractions(TERRAFORM_STATE_DAO);
        }

        @Test
        void shouldSaveStateAndReturn200() {
            when(STATE_LOCK_DAO.findLockByStateName("foo")).thenReturn(Optional.empty());

            var response = APP.client().target("/state/{stateName}")
                    .resolveTemplate("stateName", "foo")
                    .request()
                    .post(json(Map.of()));

            assertOkResponse(response);

            verify(TERRAFORM_STATE_DAO).insert(any(TerraformState.class));
        }

        @Test
        void shouldSaveStateAndReturn200WhenLockIsHeldBySameId() {
            var id = UUIDs.randomUUIDString();
            var lock = StateLock.builder()
                    .id(id)
                    .build();

            when(STATE_LOCK_DAO.findLockByStateName("foo")).thenReturn(Optional.of(lock));

            var response = APP.client().target("/state/{stateName}")
                    .resolveTemplate("stateName", "foo")
                    .queryParam("ID", id)
                    .request()
                    .post(json(Map.of()));

            assertOkResponse(response);

            verify(TERRAFORM_STATE_DAO).insert(any(TerraformState.class));
        }
    }

    @Nested
    class PurgeState {
        @Test
        void shouldReturn423WhenAlreadyLocked() {
            var lock = StateLock.builder().build();

            when(STATE_LOCK_DAO.findLockByStateName("foo")).thenReturn(Optional.of(lock));

            var response = APP.client().target("/state/{stateName}")
                    .resolveTemplate("stateName", "foo")
                    .request()
                    .delete();

            assertResponseStatusCode(response, 423);

            verifyNoInteractions(TERRAFORM_STATE_DAO);
        }

        @Test
        void shouldDeleteStateAndReturn200() {
            when(STATE_LOCK_DAO.findLockByStateName("foo")).thenReturn(Optional.empty());
            when(TERRAFORM_STATE_DAO.deleteByName("foo")).thenReturn(1);

            var response = APP.client().target("/state/{stateName}")
                    .resolveTemplate("stateName", "foo")
                    .request()
                    .delete();

            assertOkResponse(response);

            verify(TERRAFORM_STATE_DAO).deleteByName("foo");
        }

    }

    @Nested
    class Lock {
        @Test
        void shouldReturn423WhenAlreadyLocked() {
            var lock = StateLock.builder().build();

            when(STATE_LOCK_DAO.findLockByStateName("foo")).thenReturn(Optional.of(lock));

            var response = APP.client().target("/state/{stateName}/lock")
                    .resolveTemplate("stateName", "foo")
                    .request()
                    .put(json(lock));

            assertResponseStatusCode(response, 423);

            verify(STATE_LOCK_DAO).findLockByStateName("foo");
            verifyNoMoreInteractions(STATE_LOCK_DAO);
        }

        @Test
        void shouldSaveLockAndReturn200() {
            when(STATE_LOCK_DAO.findLockByStateName("foo")).thenReturn(Optional.empty());

            var lock = StateLock.builder().build();
            var response = APP.client().target("/state/{stateName}/lock")
                    .resolveTemplate("stateName", "foo")
                    .request()
                    .put(json(lock));

            assertOkResponse(response);

            verify(STATE_LOCK_DAO).findLockByStateName("foo");
            verify(STATE_LOCK_DAO).insertLock(any(StateLock.class));
        }

        @Test
        void shouldReturn500WhenErrorThrownDuringSave() {
            when(STATE_LOCK_DAO.findLockByStateName("foo")).thenReturn(Optional.empty());
            doThrow(new RuntimeException("oops")).when(STATE_LOCK_DAO).insertLock(any(StateLock.class));

            var lock = StateLock.builder().build();
            var response = APP.client().target("/state/{stateName}/lock")
                    .resolveTemplate("stateName", "foo")
                    .request()
                    .put(json(lock));

            assertInternalServerErrorResponse(response);

            verify(STATE_LOCK_DAO).findLockByStateName("foo");
            verify(STATE_LOCK_DAO).insertLock(any(StateLock.class));
        }
    }

    @Nested
    class Unlock {

        @Test
        void shouldDeleteLockAndReturn200() {
            var lock = StateLock.builder().build();
            var response = APP.client().target("/state/{stateName}/unlock")
                    .resolveTemplate("stateName", "foo")
                    .request()
                    .put(json(lock));

            assertOkResponse(response);

            verify(STATE_LOCK_DAO).deleteLock("foo");
        }
    }
}
