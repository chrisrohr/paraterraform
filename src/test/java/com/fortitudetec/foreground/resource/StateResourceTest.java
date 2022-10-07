package com.fortitudetec.foreground.resource;

import static javax.ws.rs.client.Entity.entity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.constants.KiwiTestConstants.JSON_HELPER;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertCreatedResponseWithLocationEndingWith;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertInternalServerErrorResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNoContentResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNotFoundResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fortitudetec.foreground.dao.TerraformStateDao;
import com.fortitudetec.foreground.model.TerraformState;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.kiwiproject.test.util.Fixtures;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.GenericType;

@DisplayName("StateResource")
@ExtendWith(DropwizardExtensionsSupport.class)
class StateResourceTest {

    private static final TerraformStateDao TERRAFORM_STATE_DAO = mock(TerraformStateDao.class);
    private static final StateResource STATE_RESOURCE = new StateResource(TERRAFORM_STATE_DAO, JSON_HELPER);

    private static final ResourceExtension APP = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addProvider(MultiPartFeature.class)
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
    class GetListRecent {

        @Test
        void shouldReturnAListOfRecentTerraformStates() {
            var state = TerraformState.builder()
                    .id(1L)
                    .name("Dev")
                    .content("{}")
                    .uploadedAt(Instant.now())
                    .build();

            when(TERRAFORM_STATE_DAO.listMostRecentOfEachFile()).thenReturn(List.of(state));

            var response = APP.client().target("/states/recent")
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
    class UploadStateFile {

        @Test
        void shouldInsertGivenValidTerraformStateFile() throws IOException {
            var state = TerraformState.builder()
                    .name("state.tf")
                    .build();

            when(TERRAFORM_STATE_DAO.insert(any(TerraformState.class))).thenReturn(1L);
            when(TERRAFORM_STATE_DAO.findById(1L)).thenReturn(Optional.of(state));

            var file = Fixtures.fixtureFile("state.tf");
            var fileDataBodyPart = new FileDataBodyPart("file", file);

            try (var multiPart = (FormDataMultiPart) new FormDataMultiPart()
                    .field("name", "state.tf")
                    .bodyPart(fileDataBodyPart)
            ) {
                var response = APP.target("/states")
                        .register(MultiPartFeature.class)
                        .request()
                        .post(entity(multiPart, multiPart.getMediaType()));

                assertCreatedResponseWithLocationEndingWith(response, "/states/1");

                var argCaptor = ArgumentCaptor.forClass(TerraformState.class);
                verify(TERRAFORM_STATE_DAO).insert(argCaptor.capture());

                var createdState = argCaptor.getValue();
                assertThat(createdState.getName()).isEqualTo("state.tf");

                var stateContents = Fixtures.fixture("state.tf");
                assertThat(createdState.getContent()).isEqualTo(stateContents);
            }
        }

        @Test
        void shouldReturn500IfRetrievingSavedStateFails() throws IOException {

            when(TERRAFORM_STATE_DAO.insert(any(TerraformState.class))).thenReturn(1L);
            when(TERRAFORM_STATE_DAO.findById(1L)).thenReturn(Optional.empty());

            var file = Fixtures.fixtureFile("state.tf");
            var fileDataBodyPart = new FileDataBodyPart("file", file);

            try (var multiPart = (FormDataMultiPart) new FormDataMultiPart()
                    .field("name", "state.tf")
                    .bodyPart(fileDataBodyPart)
            ) {
                var response = APP.target("/states")
                        .register(MultiPartFeature.class)
                        .request()
                        .post(entity(multiPart, multiPart.getMediaType()));

                assertInternalServerErrorResponse(response);
            }
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

    @Nested
    class Diff {
        private static final TerraformState STATE_1 = TerraformState.builder()
                .id(1L)
                .content("{\"hello\": \"world\"}")
                .uploadedAt(Instant.parse("2022-10-05T00:00:00.00Z"))
                .build();
        private static final TerraformState STATE_2 = TerraformState.builder()
                .id(1L)
                .content("{\"hello\": \"Chris\", \"color\": \"purple\"}")
                .uploadedAt(Instant.parse("2022-10-05T00:01:00.00Z"))
                .build();

        @Test
        void shouldShowDifferencesGivenOrderedIds() {
            when(TERRAFORM_STATE_DAO.findById(1L)).thenReturn(Optional.of(STATE_1));
            when(TERRAFORM_STATE_DAO.findById(2L)).thenReturn(Optional.of(STATE_2));
            when(TERRAFORM_STATE_DAO.findContentById(1L)).thenReturn(Optional.of(STATE_1.getContent()));
            when(TERRAFORM_STATE_DAO.findContentById(2L)).thenReturn(Optional.of(STATE_2.getContent()));

            var response = APP.client().target("/states/diff/{a}/{b}")
                    .resolveTemplate("a", 1L)
                    .resolveTemplate("b", 2L)
                    .request()
                    .get();

            assertOkResponse(response);

            var stringListMap = response.readEntity(new GenericType<Map<String, List<String>>>(){});

            assertThat(stringListMap).size().isEqualTo(2);
            assertThat(stringListMap.get("hello").get(0)).isEqualTo("world");
            assertThat(stringListMap.get("hello").get(1)).isEqualTo("Chris");
            assertThat(stringListMap.get("color").get(0)).isNull();
            assertThat(stringListMap.get("color").get(1)).isEqualTo("purple");
        }

        @Test
        void shouldShowDifferencesGivenUnorderedIds() {
            when(TERRAFORM_STATE_DAO.findById(1L)).thenReturn(Optional.of(STATE_1));
            when(TERRAFORM_STATE_DAO.findById(2L)).thenReturn(Optional.of(STATE_2));
            when(TERRAFORM_STATE_DAO.findContentById(1L)).thenReturn(Optional.of(STATE_1.getContent()));
            when(TERRAFORM_STATE_DAO.findContentById(2L)).thenReturn(Optional.of(STATE_2.getContent()));

            var response = APP.client().target("/states/diff/{a}/{b}")
                    .resolveTemplate("a", 2L)
                    .resolveTemplate("b", 1L)
                    .request()
                    .get();

            assertOkResponse(response);

            var stringListMap = response.readEntity(new GenericType<Map<String, List<String>>>(){});

            assertThat(stringListMap).size().isEqualTo(2);
            assertThat(stringListMap.get("hello").get(0)).isEqualTo("world");
            assertThat(stringListMap.get("hello").get(1)).isEqualTo("Chris");
            assertThat(stringListMap.get("color").get(0)).isNull();
            assertThat(stringListMap.get("color").get(1)).isEqualTo("purple");
        }

        @Test
        void shouldReturn500IfStateNotFound() {
            when(TERRAFORM_STATE_DAO.findById(1L)).thenReturn(Optional.empty());

            var response = APP.client().target("/states/diff/{a}/{b}")
                    .resolveTemplate("a", 1L)
                    .resolveTemplate("b", 2L)
                    .request()
                    .get();

            assertInternalServerErrorResponse(response);
        }
    }
}
