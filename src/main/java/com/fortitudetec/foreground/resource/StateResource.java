package com.fortitudetec.foreground.resource;

import static org.kiwiproject.jaxrs.KiwiStandardResponses.standardGetResponse;
import static org.kiwiproject.jaxrs.KiwiStandardResponses.standardPostResponse;

import com.fortitudetec.foreground.dao.TerraformStateDao;
import com.fortitudetec.foreground.model.TerraformState;

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
public class StateResource {

    private final TerraformStateDao terraformStateDao;

    public StateResource(TerraformStateDao terraformStateDao) {
        this.terraformStateDao = terraformStateDao;
    }

    @GET
    public Response list() {
        var states = terraformStateDao.list();
        return Response.ok(states).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        var state = terraformStateDao.findById(id);
        return standardGetResponse(state, "Unable to find terraform state");
    }

    @POST
    public Response add(TerraformState state, @Context UriInfo uriInfo) {
        var id = terraformStateDao.insert(state);

        var uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path(Long.toString(id));

        var savedState = terraformStateDao.findById(id)
                .orElseThrow(() -> new IllegalStateException("Unable to find inserted terraform state"));

        return standardPostResponse(uriBuilder.build(), savedState);
    }
}
