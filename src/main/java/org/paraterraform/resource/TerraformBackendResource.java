package org.paraterraform.resource;

import static javax.ws.rs.client.Entity.json;
import static org.kiwiproject.jaxrs.KiwiStandardResponses.standardGetResponse;

import lombok.extern.slf4j.Slf4j;
import org.paraterraform.dao.StateLockDao;
import org.paraterraform.dao.TerraformStateDao;
import org.paraterraform.model.StateLock;
import org.paraterraform.model.TerraformState;

import java.time.Instant;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/state")
@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TerraformBackendResource {

    private final TerraformStateDao terraformStateDao;
    private final StateLockDao stateLockDao;

    public TerraformBackendResource(TerraformStateDao terraformStateDao, StateLockDao stateLockDao) {
        this.terraformStateDao = terraformStateDao;
        this.stateLockDao = stateLockDao;
    }

    @GET
    @Path("/{stateName}")
    public Response getState(@PathParam("stateName") String stateName) {
        LOG.info("Attempting to find a state file for {}", stateName);

        var content = terraformStateDao.findLatestStateContentByName(stateName);

        return standardGetResponse(content, "Unable to find content");
    }

    @POST
    @Path("/{stateName}")
    public Response updateState(@PathParam("stateName") String stateName, @QueryParam("ID") String lockId, String content) {
        var lock = stateLockDao.findLockByStateName(stateName);
        if (lock.isPresent() && !lockId.equalsIgnoreCase(lock.get().getId())) {
            return Response.status(423).entity(json(lock)).build();
        }

        LOG.info("Received a state file for {}", stateName);
        LOG.debug("New state file content: \n{}", content);

        var state = TerraformState.builder()
                .name(stateName)
                .content(content)
                .uploadedAt(Instant.now())
                .updatedBy(lock.map(StateLock::getLockedBy).orElse(null))
                .build();

        terraformStateDao.insert(state);

        return Response.ok().build();
    }

    @DELETE
    @Path("/{stateName}")
    public Response purgeState(@PathParam("stateName") String stateName) {
        var lock = stateLockDao.findLockByStateName(stateName);
        if (lock.isPresent()) {
            return Response.status(423).entity(json(lock)).build();
        }

        LOG.info("Purging state for {}", stateName);

        int recordsDeleted = terraformStateDao.deleteByName(stateName);
        LOG.info("{} state records were purged for {}", recordsDeleted, stateName);

        return Response.ok().build();
    }

    @PUT
    @Path("/{stateName}/lock")
    public Response lock(@PathParam("stateName") String stateName, StateLock lockRequest) {
        LOG.info("Requesting lock for {}", stateName);

        return stateLockDao.findLockByStateName(stateName)
                .map(lock -> Response.status(423).entity(json(lock)).build())
                .orElseGet(() -> {
                    var lock = lockRequest.withStateName(stateName);

                    try {
                        stateLockDao.insertLock(lock);
                        return Response.ok().build();
                    } catch (Exception e) {
                        LOG.warn("Unable to save lock", e);
                        return Response.serverError().build();
                    }
                });
    }

    @PUT
    @Path("/{stateName}/unlock")
    public Response unlock(@PathParam("stateName") String stateName, String content) {
        LOG.info("Releasing lock for {}", stateName);

        stateLockDao.deleteLock(stateName);

        return Response.ok().build();
    }
}
