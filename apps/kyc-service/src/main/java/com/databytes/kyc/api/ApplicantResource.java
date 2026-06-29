package com.databytes.kyc.api;

import com.databytes.kyc.application.ApplicantService;
import com.databytes.kyc.domain.Applicant;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

@Path("/applicants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicantResource {

    @Inject
    ApplicantService service;

    @POST
    public Response register(@Valid RegisterApplicantRequest request, @Context UriInfo uriInfo) {
        Applicant draft = new Applicant();
        draft.fullName = request.fullName();
        draft.nationalId = request.nationalId();
        draft.dateOfBirth = request.dateOfBirth();
        draft.country = request.country();
        draft.email = request.email();
        if (request.attributes() != null) {
            draft.attributes = new HashMap<>(request.attributes());
        }
        Applicant saved = service.register(draft);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(saved.id)).build();
        return Response.created(location).entity(ApplicantResponse.from(saved)).build();
    }

    @GET
    @Path("/{id}")
    public ApplicantResponse get(@PathParam("id") Long id) {
        return ApplicantResponse.from(service.findById(id));
    }

    @GET
    public List<ApplicantResponse> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return service.list(page, size).stream().map(ApplicantResponse::from).toList();
    }
}
