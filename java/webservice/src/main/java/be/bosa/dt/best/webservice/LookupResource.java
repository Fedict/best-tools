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
import be.bosa.dt.best.webservice.entities.MunicipalityPart;
import be.bosa.dt.best.webservice.entities.PostalInfo;
import be.bosa.dt.best.webservice.entities.Street;
import be.bosa.dt.best.webservice.entities.Version;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;


/**
 * End points for web service
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
@Path(LookupResource.API)
public class LookupResource {
	public final static String API = "/api/belgianAddress/v2";
	public final static String ADDRESSES = "/addresses";	
	public final static String MUNICIPALITIES = "/municipalities";
	public final static String MUNICIPALITY_PARTS = "/municipalityParts";
	public final static String POSTAL = "/postal";
	public final static String STREETS = "/streets";

	private final static Map<String,JsonObject> cache = new HashMap<>(5000);
	
	@Inject
	Repository repo;

	/**
	 * Cache the JSON rendering of all municipalities
	 * 
	 * @param ev 
	 */
	void onStart(@Observes StartupEvent ev) {               
        Log.info("Caching");
	
		Multi<Municipality> municipalities = repo.findMunicipalitiesAll();
		municipalities.subscribe().asStream().forEach(m -> {
			cache.put(m.id, JsonObject.mapFrom(m));
		});
		int size = cache.size();
		Log.infof("%d municipalities", size);
	
		Multi<MunicipalityPart> parts = repo.findMunicipalityPartsAll();
		parts.subscribe().asStream().forEach(mp -> {
			cache.put(mp.id, JsonObject.mapFrom(mp));
		});
		Log.infof("%d municipality parts", cache.size() - size);
		size = cache.size();
		
		Multi<PostalInfo> postals = repo.findPostalInfosAll();
		postals.subscribe().asStream().forEach(p -> {			
			cache.put(p.id, JsonObject.mapFrom(p));
		});
		Log.infof("%d postal info", cache.size() - size);
    }

	/**
	 * Convert uni of address, street, ... into JSON
	 * 
	 * @param <T>
	 * @param info information about web request
	 * @param item entity
	 * @return JSON object or null when not found
	 */
	private static <T extends BestEntity> JsonObject toJson(UriInfo info, Uni<T> item) {
		BestEntity entity = item.await().indefinitely();
		if (entity == null) {
			return null;
		}
		String self = info.getAbsolutePath().toString();
		return JsonObject.mapFrom(entity).put("self", self);
	}

	
	/**
	 * Add pagination links to parent object
	 * 
	 * @param info URI info
	 * @param parentObj parent JSON object
	 * @param arr result array
	 * @return JSON object with pagination
	 */
	private static JsonObject paginate(UriInfo info, JsonObject parentObj, JsonArray arr) {
		//pagination
		int size = arr.size();
		if (size >= Repository.PAGE_LIMIT) {
			JsonObject lastObj = arr.getJsonObject(size - 1);
			String first = info.getRequestUriBuilder()
								.replaceQueryParam("after", null)
								.queryParam("pageSize", Repository.PAGE_LIMIT)
								.build().toString();
			String next = info.getRequestUriBuilder()
								.replaceQueryParam("after", lastObj.getString("id"))
								.queryParam("pageSize", Repository.PAGE_LIMIT)
								.build().toString();
			parentObj.put("pageSize", Repository.PAGE_LIMIT);
			parentObj.put("first", first);
			parentObj.put("next", next);
		}
		return parentObj;
	}

	/**
	 * Convert multi(ple results) of addresses into JSON
	 * 
	 * @param info information about web request
	 * @param items entities
	 * @param embed add embedded part to JSON or not
	 * @return JSON object
	 */
	private static JsonObject toJsonEmbeddable(UriInfo info, Multi<Address> items, boolean embed) {
		String self = info.getAbsolutePath().toString();
	
		Map<String,Street> streets = new HashMap<>();
		Set<String> embedded = new HashSet<>();

		JsonArray arr = new JsonArray();
		items.subscribe().asStream().forEach(a -> {
			String href = self + "/" + a.id.replace("/", "%2F");
			if (a.street != null) {
				streets.put(a.street.id, a.street);
			}
			embedded.add(a.mIdentifier);
			if (a.mpIdentifier != null) {
				embedded.add(a.mpIdentifier);
			}
			embedded.add(a.pIdentifier);
			arr.add(JsonObject.mapFrom(a).put("href", href));
		});

		JsonObject parentObj = new JsonObject();		
		parentObj.put("self", self);
		parentObj.put("items", arr); 

		if (embed) {
			JsonObject embObj = new JsonObject();
			streets.forEach((k,v) -> {
				JsonObject obj = JsonObject.mapFrom(v);
				embObj.put(obj.getString("self"), obj);
			});

			embedded.forEach(e -> { 
				JsonObject obj = cache.get(e);
				if (obj == null) {
					Log.errorf("%s not found in cache", e);
				} else {
					embObj.put(obj.getString("self"), obj);
				}
			});
			parentObj.put("embedded", embObj);
		}	
		return paginate(info, parentObj, arr);
	}

	/**
	 * Convert multi(ple results) of streets, municipalities, ... into JSON
	 * 
	 * @param info information about web request
	 * @param items entities
	 * @return JSON object
	 */
	private static <T extends BestEntity> JsonObject toJson(UriInfo info, Multi<T> items) {
		String self = info.getAbsolutePath().toString();
	
		JsonArray arr = new JsonArray();
		items.subscribe().asStream().forEach(a -> {
			String href = self + "/" + a.id.replace("/", "%2F");
			arr.add(JsonObject.mapFrom(a).put("href", href));
		});

		JsonObject parentObj = new JsonObject();		
		parentObj.put("self", self);
		parentObj.put("items", arr); 

		return paginate(info, parentObj, arr);
	}

	/**
	 * Return single result or "not found" JSON object
	 * 
	 * @param json
	 * @return response with single JSON result or a not found JSON object
	 */
	private static RestResponse<JsonObject> responseOrEmpty(JsonObject json) {
		if (json != null) { 
			return RestResponse.ok(json);
		}

		JsonObject obj = new JsonObject();
		obj.put("type", "urn:problem-type:belgif:resourceNotFound");
		obj.put("href", "https://www.gcloud.belgium.be/rest/problems/resourceNotFound.html");
		obj.put("status", RestResponse.Status.NOT_FOUND.getStatusCode());

		return ResponseBuilder.create(RestResponse.Status.NOT_FOUND, obj).build();
	}

	@GET
	@Path(LookupResource.ADDRESSES + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get address by id",
			description = "The ID is a concatenation of the address namespace, objectIdentifier, and versionIdentifier")
	@Tag(name = "addresses")
	@APIResponses({
		@APIResponse(
		   responseCode = "200",
			name = "ok",
			description = "Requested address",
			content = @Content(
				schema = @Schema(
				type = SchemaType.OBJECT))),
		@APIResponse(
			responseCode = "404",
			name = "not found",
			description = "Address not found")	
	})
	public RestResponse<JsonObject> getAddressById(
			@Parameter(description = "Address ID ('/' must be URL-encoded to '%2f')", 
						required = true, 
						example = "https://data.vlaanderen.be/id/adres/205001/2014-03-19T16:59:54.467")
			String id,
			@Parameter(description = "Embed referenced streets, municipalities ...",
						example = "true",
						required = false)
			@RestQuery boolean embed,
			UriInfo info) {
		Uni<Address> address = repo.findAddressById(id, embed);
		return responseOrEmpty(toJson(info, address));
	}

	@GET
	@Path(LookupResource.ADDRESSES)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Search for addresses",
			description = "A series of parameters can be used to search for addresses")
	@Tag(name = "addresses")
	@APIResponse(
		responseCode = "200",
		name = "ok",
		description = "A (possibly empty) list of addresses",
		content = @Content(
			schema = @Schema(
				type = SchemaType.OBJECT)))
	public JsonObject getAddresses(
			@Parameter(description = "Municipality identifier", 
						required = false)
			@RestQuery String municipalityID,
			@Parameter(description = "Street identifier", 
						required = false)
			@RestQuery String streetID,
			@Parameter(description = "Postal identifier", 
						required = false)
			@RestQuery String postalID,
			@Parameter(description = "House number",
						example = "71",
						required = false)
			@RestQuery String houseNumber,
			@Parameter(description = "Box number",
						example = "12",
						required = false)
			@RestQuery String boxNumber,
			@Parameter(description = "X coordinate",
						example = "4.23",
						required = false)
			@RestQuery double gpsx,
			@Parameter(description = "Y coordinate",
						example = "50.699",
						required = false)
			@RestQuery double gpsy,
			@Parameter(description = "Maximum distance (in meters)",
						example = "100",
						required = false)
			@RestQuery int meters,
			@Parameter(description = "Maximum numbers of results",
						example = "200",
						required = false)	
			@RestQuery int limit,
			@Parameter(description = "Embed referenced streets, municipalities ...",
						example = "true",
						required = false)
			@RestQuery boolean embed,
			@Parameter(description = "After this address (used in pagination), ('/' must be URL-encoded to '%2f')", 
						required = false, 
						example = "https://data.vlaanderen.be/id/adres/205001/2014-03-19T16:59:54.467")
			@RestQuery String after,
			UriInfo info) {
		Multi<Address> addresses = (gpsx == 0 || gpsy == 0)
			? repo.findAddresses(after, municipalityID, streetID, postalID, houseNumber, boxNumber, limit, embed)
			: repo.findByCoordinates(after, gpsx, gpsy, meters, limit, embed);
		return toJsonEmbeddable(info, addresses, embed);
	}

	@GET
	@Path(LookupResource.MUNICIPALITIES +"/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get a municipality by ID",
			description = "The ID is a concatenation of the address namespace, objectIdentifier, and versionIdentifier")
	@Tag(name = "municipalities")
	@APIResponses({
		@APIResponse(
		   responseCode = "200",
			name = "ok",
			description = "Requested municipality",
			content = @Content(
				schema = @Schema(
                type = SchemaType.OBJECT))),
		@APIResponse(
			responseCode = "404",
			name = "not found",
			description = "Municipality not found")	
	})
	public RestResponse<JsonObject> getMunicipalityById(
			@Parameter(description = "Municipality ID", 
						required = true)
			String id,
			UriInfo info) {
		Uni<Municipality> municipality = repo.findMunicipalityById(id);
		return responseOrEmpty(toJson(info, municipality));
	}

	@GET
	@Path(LookupResource.MUNICIPALITIES)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Search for municipalities",
			description = "This is a concatenation of the address namespace, objectIdentifier, and versionIdentifier")
	@Tag(name = "municipalities")
	@APIResponse(
		responseCode = "200",
		name = "ok",
		description = "A (possibly empty) list of municipalities",
		content = @Content(
			schema = @Schema(
				type = SchemaType.OBJECT)))
	public JsonObject getMunicipalities(
			@Parameter(description = "REFNIS code (assigned by Statbel)",
						example = "23027",
						required = false)
			@RestQuery String niscode,
			@Parameter(description = "Postal code (assigned by bPost)",
						example = "1500",
						required = false)
			@RestQuery String postalcode,
			UriInfo info) {
		Multi<Municipality> municipalities = null;
		if (niscode == null && postalcode == null) {
			municipalities = repo.findMunicipalitiesAll();
		} else {
			municipalities = (niscode != null) ? repo.findMunicipalitiesByNis(niscode)
												: repo.findMunicipalitiesByPostal(postalcode);
		}
		return toJson(info, municipalities);
	}

	@GET
	@Path(LookupResource.MUNICIPALITY_PARTS +"/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get a municipality part by full ID",
				description = "Note: currently only the Walloon Region provides info about municipality parts")
	@Tag(name = "municipalities")
	public RestResponse<JsonObject> getMunicipalityPartsById(
			@Parameter(description = "Municipality part ID", 
						required = true)
			String id,
			UriInfo info) {
		Uni<MunicipalityPart> part = repo.findMunicipalityPartById(id);
		return responseOrEmpty(toJson(info, part));		
	}

	@GET
	@Path(LookupResource.MUNICIPALITY_PARTS)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Search for municipality parts",
			description = "Note: currently only the Walloon Region provides info about municipality parts")
	@Tag(name = "municipalities")
	public JsonObject getMunicipalityParts(
			@Parameter(description = "After municipality part (used in pagination)", 
						required = false)
			@RestQuery String after,
			@RestQuery String embedded,
			UriInfo info) {
		Multi<MunicipalityPart> parts = repo.findMunicipalityParts(after);
		return toJson(info, parts);
	}

	@GET
	@Path(LookupResource.POSTAL + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get a postal info by ID",
			description = "The ID is a concatenation of the address namespace, objectIdentifier, and versionIdentifier")
	@Tag(name = "postals")
	public RestResponse<JsonObject> getPostalById(
			@Parameter(description = "Postal ID", 
						required = true)
			String id,
			UriInfo info) {
		Uni<PostalInfo> postal = repo.findPostalInfoById(id);
		return responseOrEmpty(toJson(info, postal));
	}

	@GET
	@Path(LookupResource.POSTAL)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Search for postal info")
	@Tag(name = "postals")
	public JsonObject getPostalInfos(
			@Parameter(description = "After postal info (used in pagination)", 
						required = false)
			@RestQuery String after,
			UriInfo info) {
		Multi<PostalInfo> postals = repo.findPostalInfos(after);
		return toJson(info, postals);
	}

	@GET
	@Path(LookupResource.STREETS + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get a street by ID",
			description = "The ID is a concatenation of the address namespace, objectIdentifier, and versionIdentifier")
	@Tag(name = "streets")
	@APIResponses({
		@APIResponse(
		   responseCode = "200",
			name = "ok",
			description = "Requested street",
			content = @Content(
				schema = @Schema(
                type = SchemaType.OBJECT))),
		@APIResponse(
			responseCode = "404",
			name = "not found",
			description = "Street not found")	
	})
	public RestResponse<JsonObject> getStreetById(
			@Parameter(description = "Street ID", 
						required = true,
						example = "https://data.vlaanderen.be/id/straatnaam/1/2013-04-12T20:06:58.583'")
			String id,
			UriInfo info) {
		Uni<Street> street = repo.findStreetById(id);
		return responseOrEmpty(toJson(info, street));
	}

	@GET
	@Path(LookupResource.STREETS)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Search for streets")
	@Tag(name = "streets")
	@APIResponse(
	   responseCode = "200",
		name = "ok",
		description = "A (possibly empty) list of streets",
		content = @Content(
		schema = @Schema(
			type = SchemaType.OBJECT)))
	public JsonObject getStreets(
			@Parameter(description = "Municipality identifier", 
				required = false)
			@RestQuery String municipalityID,
			@Parameter(description = "After street (used in pagination)", 
				required = false)
			@RestQuery String after,
			UriInfo info) {
		Multi<Street> streets = repo.findStreets(after);
		return toJson(info, streets);
	}
	
	@GET
	@Path("version")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Display version info",
			description = "For debug / support purposes only, displays version info of source data and API")
	public Multi<Version> getVersionInfo() {
		return repo.findVersionInfo();
	}

}