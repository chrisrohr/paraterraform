package com.fortitudetec.foreground.resource;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertCreatedResponseWithLocationEndingWith;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNoContentResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNotFoundResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertResponseStatusCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fortitudetec.foreground.dao.TerraformStateDao;
import com.fortitudetec.foreground.model.TerraformState;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.GenericType;

@DisplayName("StateResource")
@ExtendWith(DropwizardExtensionsSupport.class)
class StateResourceTest {

    private static final TerraformStateDao TERRAFORM_STATE_DAO = mock(TerraformStateDao.class);
    private static final StateResource STATE_RESOURCE = new StateResource(TERRAFORM_STATE_DAO);

    private static final ResourceExtension APP = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(STATE_RESOURCE)
            .addProvider(JaxrsExceptionMapper.class)
            .build();

    @BeforeEach
    void setUp() {
        reset(TERRAFORM_STATE_DAO);
    }

    @Nested
    class GetList {

        @Test
        void shouldReturnAListOfTerraformStates() {
            var state = TerraformState.builder()
                    .id(1L)
                    .name("Dev")
                    .content("{}")
                    .uploadedAt(Instant.now())
                    .build();

            when(TERRAFORM_STATE_DAO.list()).thenReturn(List.of(state));

            var response = APP.client().target("/states")
                    .request()
                    .get();

            assertOkResponse(response);

            var listOfStates = response.readEntity(new GenericType<List<TerraformState>>(){});
            var returnedState = first(listOfStates);
            assertThat(returnedState)
                    .usingRecursiveComparison()
                    .isEqualTo(state);
        }
    }

    @Nested
    class Get {

        @Test
        void shouldReturnTerraformStateWithGivenId() {
            var state = TerraformState.builder()
                    .id(1L)
                    .name("Dev")
                    .content("{}")
                    .uploadedAt(Instant.now())
                    .build();

            when(TERRAFORM_STATE_DAO.findById(1L)).thenReturn(Optional.of(state));

            var response = APP.client().target("/states/{id}")
                    .resolveTemplate("id", 1L)
                    .request()
                    .get();

            assertOkResponse(response);

            var returnedState = response.readEntity(TerraformState.class);
            assertThat(returnedState)
                    .usingRecursiveComparison()
                    .isEqualTo(state);
        }

        @Test
        void shouldReturn404WhenTerraformStateCannotBeFound() {
            when(TERRAFORM_STATE_DAO.findById(1L)).thenReturn(Optional.empty());

            var response = APP.client().target("/states/{id}")
                    .resolveTemplate("id", 1L)
                    .request()
                    .get();

            assertNotFoundResponse(response);
        }
    }

    @Nested
    class Add {

        @Test
        void shouldInsertGivenValidTerraformState() {
            var state = TerraformState.builder()
                    .name("Dev")
                    .content("{}")
                    .build();

            when(TERRAFORM_STATE_DAO.insert(any(TerraformState.class))).thenReturn(1L);
            when(TERRAFORM_STATE_DAO.findById(1L)).thenReturn(Optional.of(state));

            var response = APP.client().target("/states")
                    .request()
                    .post(json(state));

            assertCreatedResponseWithLocationEndingWith(response, "/states/1");
        }

        @Test
        void shouldReturn422WhenNameIsMissing() {
            var state = TerraformState.builder()
                    .content("{}")
                    .build();

            var response = APP.client().target("/states")
                    .request()
                    .post(json(state));

            assertResponseStatusCode(response, 422);

            verifyNoInteractions(TERRAFORM_STATE_DAO);
        }

        @Test
        void shouldReturn422WhenContentIsMissing() {
            var state = TerraformState.builder()
                    .name("Dev")
                    .build();

            var response = APP.client().target("/states")
                    .request()
                    .post(json(state));

            assertResponseStatusCode(response, 422);

            verifyNoInteractions(TERRAFORM_STATE_DAO);
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldDeleteTerraformStateWithGivenId() {
            when(TERRAFORM_STATE_DAO.deleteById(1L)).thenReturn(1);

            var response = APP.client().target("/states/{id}")
                    .resolveTemplate("id", 1L)
                    .request()
                    .delete();

            assertNoContentResponse(response);

            verify(TERRAFORM_STATE_DAO).deleteById(1L);

        }
    }
}
