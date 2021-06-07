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

import be.bosa.dt.best.webservice.entities.Address;
import be.bosa.dt.best.webservice.entities.AddressDistance;
import be.bosa.dt.best.webservice.entities.Municipality;
import be.bosa.dt.best.webservice.entities.Street;

import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.ReactiveRoutes;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HandlerType;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.quarkus.vertx.web.RouteBase;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.vertx.mutiny.core.http.HttpServerResponse;

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
@RouteBase(path = "best/api/v2")
public class LookupResource {
	@Inject
	Repository repo;

	@Route(type = HandlerType.FAILURE)
	public void invalidArgException(IllegalArgumentException e, HttpServerResponse response) {
		response.setStatusCode(400).end(e.getMessage());
	}

	@Route(path = "near", methods = HttpMethod.GET, produces = "application/json")
	@Operation(summary = "Get (maximum) 200 addresses within (maximum) 100 meters + calculate distance")
	@Blocking
	public Multi<AddressDistance> nearestAddress(
			@Parameter(description = "X coordinate (longitude)", required = true, example = "4.23")
			@Param("x") Double x, 
			@Parameter(description = "Y coordinate (latitude)", required = true, example = "50.73")	
			@Param("y") Double y,
			@Parameter(description = "Maximum distance (meters)", required = false, example = "100")	
			@Param("dist") Optional<Integer> maxdist,
			@Parameter(description = "Maximum number of results", example = "200")
			@Param("status") Optional<Integer> limit,
			@Parameter(description = "Calculate distance", example = "true")
			@Param("calc") Optional<Boolean> calc) {
	
		int dist = maxdist.orElse(100);
		int res = limit.orElse(200);
		if (dist > 100 || res > 200) {
			throw new IllegalArgumentException("Invalid parameters");
		}	
		return ReactiveRoutes.asJsonArray(repo.findAddressDistance(x, y, dist, res));
	}

	@Route(path = "address", methods = HttpMethod.GET, produces = "application/json")
	@Operation(summary = "Get an address by full ID")
	public Uni<Address> getAddressById(
			@Parameter(description = "Address ID", required = true, example = "BE.BRUSSELS.BRIC.ADM.ADDR/1299/2")
			@Param("id") String id) {
		return repo.findAddressById(id);
	}

	@Route(path = "street", methods = HttpMethod.GET, produces = "application/json")
	@Operation(summary = "Get a street by full id")
	public Uni<Street> getStreetById(
			@Parameter(description = "Street ID", required = true, example = "https://data.vlaanderen.be/doc/straatnaam/178908")
			@Param("id") String id) {
		return repo.findStreetById(id);
	}

	@Route(path = "municipality", methods = HttpMethod.GET, produces = "application/json")
	@Operation(summary = "Get a municipality by full id")
	public Uni<Municipality> getMunicipalityById(
			@Parameter(description = "Municipality ID", required = true, example = "https://data.vlaanderen.be/id/gemeente/23027/2002-08-13T17:32:32")
			@Param("id") String id) {
		return repo.findMunicipalityById(id);
	}

	@Route(path = "municipalities", methods = HttpMethod.GET, produces = "application/json")
	@Operation(summary = "Get a list of all municipalities, or search by (part of) name or zipcode")
	public Multi<Municipality> allMunicipalities(
			@Parameter(description = "Part of the name", example = "Halle")
			@Param("name") Optional<String> name,
			@Parameter(description = "Postal code", example = "1500")
			@Param("name") Optional<String> zipcode) {
		Multi<Municipality> res = null;
		if (name.isPresent()) {
			res = ReactiveRoutes.asJsonArray(repo.findMunicipalitiesByName(name.get()));
		} else if (zipcode.isPresent()) {
			res = repo.findMunicipalitiesByZipcode(zipcode.get());
		} else {
			res = repo.findMunicipalities();
		}
		return ReactiveRoutes.asJsonArray(res);
	}

	@Route(path = "streets", methods = HttpMethod.GET, produces = "application/json")
	@Operation(summary = "Search for streets by postal or nis code, optionally by (part of) name ")
	public Multi<Street> streetsByCode(
			@Parameter(description = "postal code", example = "1500")	
			@Param("zipcode") Optional<String> zipcode,
			@Parameter(description = "REFNIS code", example = "23027")	
			@Param("niscode") Optional<String> niscode,
			@Parameter(description = "Part of the name (at least 2 characters)", example = "Markt")
			@Param("name") Optional<String> name) {
		if (zipcode.isEmpty() && niscode.isEmpty()) {
			throw new IllegalArgumentException("Invalid parameters");
		}
		
		Multi<Street> res = null;
		
		if (zipcode.isPresent()) {
			String zipstr = zipcode.get();
			res = name.isPresent() ? repo.findStreetsByZipcodeAndName(zipstr, name.get()) 
									: repo.findStreetsByZipcode(zipstr);
		} else if (niscode.isPresent()) {
			String nisstr = niscode.get();
			res = name.isPresent() ? repo.findStreetsByNiscodeAndName(nisstr, name.get()) 
									: repo.findStreetsByNiscode(nisstr);
		}
		return ReactiveRoutes.asJsonArray(res);
	}
}
