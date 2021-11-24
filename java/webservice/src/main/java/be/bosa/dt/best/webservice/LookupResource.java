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
	public final static String POSTAL = "/postalInfos";
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
	 * Convert uni of address, street, ... into JSON
	 * 
	 * @param <T>
	 * @param info information about web request
	 * @param item entity
	 * @return JSON object or null when not found
	 */
	private static JsonObject toJsonEmbeddable(UriInfo info, Uni<Address> item, boolean embed) {
		Address addr = item.await().indefinitely();
		if (addr == null) {
			return null;
		}

		JsonObject parentObj = JsonObject.mapFrom(addr);
		String self = info.getAbsolutePath().toString();
	
		if (embed) {
			JsonObject embObj = new JsonObject();
	
			Set<String> embedded = new HashSet<>();

			embedded.add(addr.sIdentifier);
			embedded.add(addr.mIdentifier);
			if (addr.mpIdentifier != null) {
				embedded.add(addr.mpIdentifier);
			}
			embedded.add(addr.pIdentifier);

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

		return parentObj.put("self", self);
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
			streets.put(a.street.id, a.street);
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
			arr.add(JsonObject.mapFrom(a));
				//.put("href", href));
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
	 * @return response with single JSON result or addr not found JSON object
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

	/**
	 * Get address by ID
	 * 
	 * @param id
	 * @param embed
	 * @param info
	 * @return address or error object
	 */
	@GET
	@Path(LookupResource.ADDRESSES + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<JsonObject> getAddressById(String id, @RestQuery boolean embed, UriInfo info) {
		Uni<Address> address = repo.findAddressById(id, embed);
		return responseOrEmpty(toJsonEmbeddable(info, address, embed));
	}

	/**
	 * Search for addresses
	 * 
	 * @param municipalityID
	 * @param municipalityName
	 * @param streetID
	 * @param streetName
	 * @param postalID
	 * @param postalName
	 * @param postalCode
	 * @param houseNumber
	 * @param boxNumber
	 * @param status
	 * @param coordX
	 * @param coordY
	 * @param radius
	 * @param polygon
	 * @param crs
	 * @param embed
	 * @param after
	 * @param info
	 * @return (possibly empty) list of addresses
	 */
	@GET
	@Path(LookupResource.ADDRESSES)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getAddresses(
			@RestQuery String municipalityID, @RestQuery String municipalityName,
			@RestQuery String streetID, @RestQuery String streetName,
			@RestQuery String postalID, @RestQuery String postalName, @RestQuery String postalCode,
			@RestQuery String houseNumber, @RestQuery String boxNumber,
			@RestQuery String status,
			@RestQuery double coordX, @RestQuery double coordY, @RestQuery int radius,
			@RestQuery String polygon, @RestQuery String crs,
			@RestQuery boolean embed, @RestQuery String after, UriInfo info) {
		Multi<Address> addresses;
		if (coordX != 0 && coordY == 0) {
			addresses = repo.findByCoordinates(after, coordX, coordY, crs, radius, status, embed);
		} else if (polygon != null && !polygon.isEmpty()) {
			addresses = repo.findByPolygon(after, polygon, crs, status, embed);
		} else {
			addresses = repo.findAddresses(after, 
								municipalityID, municipalityName, streetID, streetName, 
								postalID, postalCode, postalName, houseNumber, boxNumber, 
								status, embed);
		}
		return toJsonEmbeddable(info, addresses, embed);
	}

	/**
	 * Get municipality by ID
	 * 
	 * @param id
	 * @param info
	 * @return municipality or error object
	 */
	@GET
	@Path(LookupResource.MUNICIPALITIES +"/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<JsonObject> getMunicipalityById(String id, UriInfo info) {
		Uni<Municipality> municipality = repo.findMunicipalityById(id);
		return responseOrEmpty(toJson(info, municipality));
	}

	/**
	 * Search for municipalities
	 * 
	 * @param niscode
	 * @param postalcode
	 * @param name
	 * @param nameMatch
	 * @param info
	 * @return (possibly empty) list of municipalities
	 */
	@GET
	@Path(LookupResource.MUNICIPALITIES)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getMunicipalities(
			@RestQuery String niscode,
			@RestQuery String postalcode,
			@RestQuery String name,
			@RestQuery String nameMatch, UriInfo info) {
		Multi<Municipality> municipalities = repo.findMunicipalities(niscode, postalcode, name, nameMatch);
		return toJson(info, municipalities);
	}

	/**
	 * Get part of municipality by ID
	 * 
	 * @param id
	 * @param info
	 * @return 
	 */
	@GET
	@Path(LookupResource.MUNICIPALITY_PARTS +"/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<JsonObject> getMunicipalityPartsById(String id, UriInfo info) {
		Uni<MunicipalityPart> part = repo.findMunicipalityPartById(id);
		return responseOrEmpty(toJson(info, part));		
	}

	/**
	 * Search for municipality parts
	 * 
	 * @param name
	 * @param after
	 * @param info
	 * @return (possibly empty) list of municipality parts
	 */
	@GET
	@Path(LookupResource.MUNICIPALITY_PARTS)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getMunicipalityParts(	
			@RestQuery String name,
			@RestQuery String after, UriInfo info) {
		Multi<MunicipalityPart> parts = repo.findMunicipalityParts(after, name);
		return toJson(info, parts);
	}

	/**
	 * Get postal info by ID
	 * 
	 * @param id
	 * @param info
	 * @return 
	 */
	@GET
	@Path(LookupResource.POSTAL + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<JsonObject> getPostalById(String id, UriInfo info) {
		Uni<PostalInfo> postal = repo.findPostalInfoById(id);
		return responseOrEmpty(toJson(info, postal));
	}

	/**
	 * Search for postal info
	 * 
	 * @param postalCode
	 * @param name
	 * @param after
	 * @param info
	 * @return (possibly empty) list of postal infos
	 */
	@GET
	@Path(LookupResource.POSTAL)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getPostalInfos(
			@RestQuery String postalCode,
			@RestQuery String name,
			@RestQuery String after, UriInfo info) {
		Multi<PostalInfo> postals = repo.findPostalInfos(after, postalCode, name);
		return toJson(info, postals);
	}

	/**
	 * Get street by ID
	 * 
	 * @param id
	 * @param info
	 * @return 
	 */
	@GET
	@Path(LookupResource.STREETS + "/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<JsonObject> getStreetById(String id, UriInfo info) {
		Uni<Street> street = repo.findStreetById(id);
		return responseOrEmpty(toJson(info, street));
	}

	/**
	 * Search for streets
	 * 
	 * @param municipalityID
	 * @param postalCode
	 * @param name
	 * @param after
	 * @param info
	 * @return 
	 */
	@GET
	@Path(LookupResource.STREETS)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getStreets(
			@RestQuery String municipalityID,
			@RestQuery String postalCode,
			@RestQuery String name,
			@RestQuery String after, UriInfo info) {
		Multi<Street> streets = repo.findStreets(after, municipalityID, postalCode, name);
		return toJson(info, streets);
	}

	/**
	 * Get version info
	 * 
	 * @return 
	 */
	@GET
	@Path("version")
	@Produces(MediaType.APPLICATION_JSON)
	public Multi<Version> getVersionInfo() {
		return repo.findVersionInfo();
	}

}