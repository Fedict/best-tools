/*
 * Copyright (c) 2021, FPS BOSA DG DT
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package be.bosa.dt.best.webservice;


import be.bosa.dt.best.webservice.entities.AddressDistance;

import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.ReactiveRoutes;
import io.quarkus.vertx.web.Route;
import io.smallrye.mutiny.Multi;
import io.vertx.core.http.HttpMethod;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

/**
 *
 * @author Bart Hanssens
 */
@OpenAPIDefinition(
	 info = @Info(
        title="Demo BeST API application",
        version = "1.0.0")
)
@ApplicationScoped
public class LookupResource {

	@Inject
	Repository repo;

	@Route(path = "/best/api/v2/near", methods = HttpMethod.GET, produces = "application/json")
	@Operation(summary = "Get addresses within (maximal) 100 meters")
	public Multi<AddressDistance> nearestAddress(
			@Parameter(description = "X coordinate (longitude)", required = true, example = "4.23")
			@Param("x") Double x, 
			@Parameter(description = "Y coordinate (latitude)", required = true, example = "50.73")	
			@Param("y") Double y,
			@Parameter(description = "Maximum distance (meters)", required = false, example = "100")	
			@Param("dist") Optional<Integer> maxdist,
			@Parameter(description = "status", example = "current")
			@Param("status") Optional<String> status,
			@Parameter(description = "calculate distance", example = "true")
			@Param("calc") Optional<Boolean> calc) {	
		return ReactiveRoutes.asJsonArray(repo.findAddressDistance(x, y, maxdist.orElse(100)));
	}
/*
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/address")
	@Operation(summary = "Get an address by full, optionally filter by status")
	public Address getAddressById(
			@Parameter(description = "Address ID", required = true, example = "BE.BRUSSELS.BRIC.ADM.ADDR/1299/2")
			@QueryParam("id") String id,
			@Parameter(description = "Status", example = "current")
			@QueryParam("status") Optional<String> status) {
		return Address.findByIdAndStatus(id, status);
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/street")
	@Operation(summary = "Get a street by full id, optionally filter by status")
	public Street getStreetById(
			@Parameter(description = "Street ID", required = true, example = "https://data.vlaanderen.be/doc/straatnaam/178908")
			@QueryParam("id") String id,
			@Parameter(description = "Status", example = "current")
			@QueryParam("status") Optional<String> status) {
		return Street.findById(id);
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/municipality")
	@Operation(summary = "Get a municipality by full id")
	public Municipality getMunicipalityById(
			@Parameter(description = "Municipality ID", required = true, example = "https://data.vlaanderen.be/id/gemeente/23027/2002-08-13T17:32:32")
			@QueryParam("id") Optional<String> id) {
		return Municipality.findById(id);
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/municipalities")
	@Operation(summary = "Get a list of all municipalities or search by (part of) name")
	public List<Municipality> allMunicipalities(
			@Parameter(description = "Part of the name (at least 2 characters)", example = "Halle")
			@QueryParam("name") Optional<String> name) {
		if (name.isPresent()) {
			return Municipality.findByName(name.get()).list();
		}
		return Municipality.findAll().list();
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/streets")
	@Operation(summary = "Search for streets by postal or nis code, optionally by (part of) name ")
	public List<Street> streetsByCode(
			@Parameter(description = "postal code", example = "1500")	
			@QueryParam("zipcode") Optional<String> zipcode,
			@Parameter(description = "REFNIS code", example = "23027")	
			@QueryParam("niscode") Optional<String> niscode,
			@Parameter(description = "Part of the name (at least 2 characters)", example = "Markt")
			@QueryParam("name") Optional<String> name) {
		if (zipcode.isPresent()) {
			return Street.findByZipcodeAndName(zipcode.get(), name).list();
		}

		if (niscode.isPresent()) {
			return Street.findByNiscodeAndName(niscode.get(), name).list();
		}
		return Collections.EMPTY_LIST;
	}
*/
}
