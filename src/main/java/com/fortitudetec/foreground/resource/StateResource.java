package com.fortitudetec.foreground.resource;

import com.fortitudetec.foreground.dao.TerraformStateDao;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

}
