package com.fortitudetec.foreground.resource;

import com.fortitudetec.foreground.dao.TerraformStateDao;
import com.fortitudetec.foreground.model.TerraformState;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        var state = terraformStateDao.get(id);
        if (state != null) {
            return Response.ok(state).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    public Response add(TerraformState state) {
        var id = terraformStateDao.add(state);
        state = terraformStateDao.get(id);
        return Response.ok(state).build();
    }
}
