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
import be.bosa.dt.best.webservice.entities.BestEntity;
import be.bosa.dt.best.webservice.entities.Municipality;
import be.bosa.dt.best.webservice.entities.Street;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import org.jboss.resteasy.reactive.RestQuery;


/**
 * End points for web service
 * 
 * @author Bart Hanssens
 */
@OpenAPIDefinition(
	 info = @Info(
        title="v2 Belgian Streets and Addresses REST API Demo",
        version = "2.0.0",
		description = " OpenAPI specification of the v2 Belgian Streets and Addresses (BeSt) REST API. \n" +
			"By relying on a consolidated dataset, this new version delivers even greater performance and more features than ever.\n" +
			"\n" +
			"An address is an identification of the fixed location of a property.\n" +
			"The full address is a hierarchy consisting of components such as geographic names, with an increasing level of detail (e.g.,town, then street name, then house number or name).\n" +
			"It may also include a post code or other postal descriptors.",
		contact = @Contact(
			name = "BOSA DG DT servicedesk",
			email = "servicedesk.DTO@bosa.fgov.be"
		)
	),
	tags = {
		@Tag(name = "addresses", description = "Everything about addresses" ),
		@Tag(name = "municipalities", description = "Everything about municipalities" ),
		@Tag(name = "streets", description = "Everything about streets" )
		}
	)

@ApplicationScoped
@Path("api/belgianAddress/v2")
public class LookupResource {
	@Inject
	Repository repo;

	/**
	 * Convert uni of address, street, ... into JSON
	 * 
	 * @param <T>
	 * @param info information about web request
	 * @param item entity
	 * @return JSON object
	 */
	private static <T> JsonObject toJson(UriInfo info, Uni<T> item) {
		String self = info.getAbsolutePath().toString();
		return JsonObject.mapFrom(item.await().indefinitely()).put("self", self);
	}

	/**
	 * Convert multi of address, street, ... into JSON
	 * 
	 * @param <T>
	 * @param info information about web request
	 * @param items entities
	 * @return JSON object
	 */
	private static <T extends BestEntity> JsonObject toJson(UriInfo info, Multi<T> items) {
		String self = info.getAbsolutePath().toString();

		JsonArray arr = new JsonArray();
		items.subscribe().asStream().forEach(a -> {
			String href = self + "/" + a.identifier.replaceAll("/", "%2F");
			arr.add(JsonObject.mapFrom(a).put("href", href));
		});

		JsonObject obj = new JsonObject();		
		obj.put("self", self);
		obj.put("items", arr); 

		//pagination
		int size = arr.size();
		if (size >= Repository.LIMIT) {
			JsonObject lastObj = arr.getJsonObject(size - 1);
			String first = info.getRequestUriBuilder()
								.replaceQueryParam("after", null)
								.queryParam("pageSize", Repository.LIMIT)
								.build().toString();
			String next = info.getRequestUriBuilder()
								.replaceQueryParam("after", lastObj.getString("identifier"))
								.queryParam("pageSize", Repository.LIMIT)
								.build().toString();
			obj.put("pageSize", Repository.LIMIT);
			obj.put("first", first);
			obj.put("next", next);
		}

		return obj;
	}

	@GET
	@Path("addresses/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "The external identifier of the address",
			description = "This is a concatenation of the address namespace, objectIdentifier, and versionIdentifier")
	public JsonObject getAddressById(
			@Parameter(description = "Address ID", 
						required = true, 
						example = "https://data.vlaanderen.be/id/adres/205001/2014-03-19T16:59:54.467")
			String id,
			UriInfo info) {
		Uni<Address> address = repo.findAddressById(id);
		return toJson(info, address);
	}

	@GET
	@Path("addresses")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "The external identifier of the address",
			description = "This is a concatenation of the address namespace, objectIdentifier, and versionIdentifier")
	public JsonObject getAddresses(
			@Parameter(description = "After address", 
						required = false, 
						example = "https://data.vlaanderen.be/id/adres/205001/2014-03-19T16:59:54.467")
			@RestQuery String after,
			@Parameter(description = "Municipality identifier", 
						required = false)
			@RestQuery String municipalityID,
			@Parameter(description = "Street identifier", 
						required = false)
			@RestQuery String streetID,
			@Parameter(description = "House number", 
						required = false)
			@RestQuery String houseNumber,
			@Parameter(description = "Box number", 
						required = false)
			@RestQuery String boxNumber,
			UriInfo info) {
		Multi<Address> addresses = repo.findAddresses(after, municipalityID, streetID, houseNumber, boxNumber);
		return toJson(info, addresses);
	}

	@GET
	@Path("municipalities/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get a municipality by full ID")
	public JsonObject getMunicipalityById(
			@Parameter(description = "Municipality ID", required = true, example = "BE.BRUSSELS.BRIC.ADM.ADDR/1299/2")
			String id,
			UriInfo info) {
		Uni<Municipality> municipality = repo.findMunicipalityById(id);
		return toJson(info, municipality);
	}

	@GET
	@Path("streets/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get a street by full ID")
	public JsonObject getStreetById(
			@Parameter(description = "Street ID", required = true, example = "https://data.vlaanderen.be/id/straatnaam/1/2013-04-12T20:06:58.583'")
			String id,
			UriInfo info) {
		Uni<Street> street = repo.findStreetById(id);
		return toJson(info, street);
	}
}
	/*
	@Operation(summary = "Search for addresses")
	@Path("/addresses")
	@GET
	public Multi<Address> getAddresses(
			@RestQuery Optional<String> municipalityId,
			@RestQuery Optional<String> municipalityName,
			@RestQuery Optional<String> partOfMunicipalityId,
			@RestQuery Optional<String> partOfMunicipalityName,
			@RestQuery Optional<String> streetId,
			@RestQuery Optional<String> streetName,
			@RestQuery Optional<String> housenumber,
			@RestQuery Optional<String> boxnumber,
			@RestQuery Optional<String> status) {
		return null;
	} */

