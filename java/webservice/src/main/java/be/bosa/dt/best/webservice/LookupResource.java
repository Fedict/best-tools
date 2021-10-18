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

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;

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
		Address add = repo.findAddressById(id).await().indefinitely();
		return JsonObject.mapFrom(add).put("self", info.getAbsolutePath());
	}

	@GET
	@Path("addresses")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "The external identifier of the address",
			description = "This is a concatenation of the address namespace, objectIdentifier, and versionIdentifier")
	@Blocking
	public JsonObject getAddresses(
			@Parameter(description = "After address", 
						required = false, 
						example = "https://data.vlaanderen.be/id/adres/205001/2014-03-19T16:59:54.467")
			@RestQuery String afterAddress,
			UriInfo info) {
		Multi<Address> adds = repo.findAddresses(afterAddress);

		JsonObject obj = new JsonObject();
		String url = info.getAbsolutePath().toString();
		obj.put("self", url);
		JsonArray arr = new JsonArray();
		adds.subscribe().asStream().forEach(a -> arr.add(JsonObject.mapFrom(a)));
	
		JsonObject last = arr.getJsonObject(arr.size() -1);
		String next = last.getString("identifier");
		obj.put("items", arr); 
		obj.put("next", url + "?afterAddress=" + next);
		return obj;
	}
}
/*
	@Route(path = "municipalities/:id", methods = HttpMethod.GET, produces = "application/json")
	@Operation(summary = "Get a municipality by full ID")
	public void getMunicipalityById(
			@Parameter(description = "Municipality ID", required = true, example = "BE.BRUSSELS.BRIC.ADM.ADDR/1299/2")
			@Param("id") String id,
			RoutingContext rc) {
		Uni<Municipality> city = repo.findMunicipalityById(id);
		city.subscribe().with(m ->  {
			JsonObject obj = JsonObject.mapFrom(m).put("self", rc.request().absoluteURI());
			rc.response().send(obj.toBuffer());
		});
	}

	@Route(path = "streets/:id", methods = HttpMethod.GET, produces = "application/json")
	@Operation(summary = "Get a street by full ID")
	public void getStreetById(
			@Parameter(description = "Street ID", required = true, example = "https://data.vlaanderen.be/id/straatnaam/1/2013-04-12T20:06:58.583'")
			@Param("id") String id,
			RoutingContext rc) {
		Uni<Street> street = repo.findStreetById(id);
		street.subscribe().with(s ->  {
			JsonObject obj = JsonObject.mapFrom(s).put("self", rc.request().absoluteURI());
			rc.response().send(obj.toBuffer());
		});
	}
}
*/	/*
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

