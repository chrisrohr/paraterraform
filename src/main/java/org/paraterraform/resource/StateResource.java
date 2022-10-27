package org.paraterraform.resource;

import static org.kiwiproject.jaxrs.KiwiStandardResponses.standardDeleteResponse;
import static org.kiwiproject.jaxrs.KiwiStandardResponses.standardGetResponse;
import static org.kiwiproject.jaxrs.KiwiStandardResponses.standardPostResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.kiwiproject.json.JsonHelper;
import org.paraterraform.dao.TerraformStateDao;
import org.paraterraform.model.TerraformState;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/states")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class StateResource {

    private static final String NOT_FOUND_MESSAGE = "Unable to find terraform state";

    private final TerraformStateDao terraformStateDao;
    private final JsonHelper jsonHelper;

    public StateResource(TerraformStateDao terraformStateDao, JsonHelper jsonHelper) {
        this.terraformStateDao = terraformStateDao;
        this.jsonHelper = jsonHelper;
    }

    @GET
    public Response list() {
        var states = terraformStateDao.find();
        return Response.ok(states).build();
    }

    @GET
    @Path("/latest")
    public Response listLatestStates() {
        var states = terraformStateDao.findLatestStates();
        return Response.ok(states).build();
    }

    @GET
    @Path("/{name}/history")
    public Response listStateHistoryByName(@PathParam("name") String name) {
        var state = terraformStateDao.findStateHistoryByName(name);
        return standardGetResponse(state, NOT_FOUND_MESSAGE);
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        var state = terraformStateDao.findById(id);
        return standardGetResponse(state, NOT_FOUND_MESSAGE);
    }

    @GET
    @Path("/{id}/content")
    public Response getContentById(@PathParam("id") Long id) {
        var content = terraformStateDao.findContentById(id);
        return standardGetResponse(content, NOT_FOUND_MESSAGE);
    }

    @GET
    @Path("/{a}/diff/{b}")
    public Response diffTwoContentsByIds(@PathParam("a") Long a, @PathParam("b") Long b) {
        var uploadedAtA = terraformStateDao.findById(a).orElseThrow().getUploadedAt();
        var uploadedAtB = terraformStateDao.findById(b).orElseThrow().getUploadedAt();
        var contentA = terraformStateDao.findContentById(a).orElseThrow();
        var contentB = terraformStateDao.findContentById(b).orElseThrow();

        var stringListMap = uploadedAtA.isBefore(uploadedAtB)
                ? jsonHelper.jsonDiff(contentA, contentB)
                : jsonHelper.jsonDiff(contentB, contentA);

        return standardGetResponse(stringListMap, "can't happen");
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadStateFile(@FormDataParam("file") InputStream stateFileInputStream,
                                    @FormDataParam("name") String name,
                                    @Context UriInfo uriInfo) throws IOException {
        String dataContent = IOUtils.toString(stateFileInputStream, StandardCharsets.UTF_8);
        var state = TerraformState.builder()
                .name(name)
                .content(dataContent)
                .build();
        var id = terraformStateDao.insert(state);
        var uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path(Long.toString(id));

        var savedState = terraformStateDao.findById(id)
                .orElseThrow(() -> new IllegalStateException(String.format("Unable to find inserted terraform state for id: %d", id)));

        return standardPostResponse(uriBuilder.build(), savedState);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteById(@PathParam("id") Long id) {
        var affectedCount = terraformStateDao.deleteById(id);
        LOG.debug("Deleted {} terraform state records with id {}", affectedCount, id);
        return standardDeleteResponse();
    }

}
