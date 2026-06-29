package com.databytes.screening;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/** Read-only view of recent screening results — handy for demos and the end-to-end test. */
@Path("/screenings")
@Produces(MediaType.APPLICATION_JSON)
public class ScreeningResource {

    @Inject
    ScreeningStore store;

    @GET
    public List<Screening> recent() {
        return store.recent();
    }
}
