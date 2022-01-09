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
import be.bosa.dt.best.webservice.entities.Municipality;
import be.bosa.dt.best.webservice.entities.MunicipalityPart;
import be.bosa.dt.best.webservice.entities.PostalInfo;
import be.bosa.dt.best.webservice.entities.Street;
import be.bosa.dt.best.webservice.entities.Version;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.vertx.core.json.JsonObject;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

/**
 * Declare REST end points for web service
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

	@Inject
	Repository repo;

	private final static Map<String, JsonObject> cache = Repository.getCache();

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
		String s = URLDecoder.decode(id, StandardCharsets.UTF_8);
		Uni<Address> address = repo.findAddressById(s);
		return Util.responseOrEmpty(Util.toJson(info, address, embed, cache));
	}

	/**
	 * Search for addresses
	 * 
	 * @param municipalityID
	 * @param municipalityName
	 * @param nisCode
	 * @param streetID
	 * @param streetName
	 * @param postalID
	 * @param postalName
	 * @param postalCode
	 * @param houseNumber
	 * @param boxNumber
	 * @param status
	 * @param coordX x-coordinate
	 * @param coordY y-coordinate
	 * @param radius max radius in meters
	 * @param polygon
	 * @param crs coordinate reference system: gps or lambert72
	 * @param embed
	 * @param after
	 * @param info
	 * @return (possibly empty) list of addresses
	 */
	@GET
	@Path(LookupResource.ADDRESSES)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getAddresses(
			@RestQuery String municipalityID, @RestQuery String municipalityName, @RestQuery String nisCode,
			@RestQuery String streetID, @RestQuery String streetName,
			@RestQuery String postalID, @RestQuery String postalName, @RestQuery String postalCode,
			@RestQuery String houseNumber, @RestQuery String boxNumber,
			@RestQuery String status,
			@RestQuery double coordX, @RestQuery double coordY, @RestQuery int radius,
			@RestQuery String polygon, @RestQuery String crs,
			@RestQuery boolean embed, @RestQuery String after, UriInfo info) {
		Multi<Address> addresses;
		if (coordX > 0 && coordY > 0) {
			addresses = repo.findByCoordinates(after, coordX, coordY, crs, radius, status);
		} else if (polygon != null && !polygon.isEmpty()) {
			addresses = repo.findByPolygon(after, polygon, crs, status);
		} else {
			addresses = repo.findAddresses(after, 
								municipalityID, nisCode, municipalityName, streetID, streetName, 
								postalID, postalCode, postalName, houseNumber, boxNumber, 
								status);
		}
		return Util.toJson(info, addresses, embed, cache);
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
		String s = URLDecoder.decode(id, StandardCharsets.UTF_8);
		Uni<Municipality> municipality = repo.findMunicipalityById(s);
		return Util.responseOrEmpty(Util.toJson(info, municipality));
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
		return Util.toJson(info, municipalities);
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
		String s = URLDecoder.decode(id, StandardCharsets.UTF_8);
		Uni<MunicipalityPart> part = repo.findMunicipalityPartById(s);
		return Util.responseOrEmpty(Util.toJson(info, part));		
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
		return Util.toJson(info, parts);
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
		String s = URLDecoder.decode(id, StandardCharsets.UTF_8);
		Uni<PostalInfo> postal = repo.findPostalInfoById(s);
		return Util.responseOrEmpty(Util.toJson(info, postal));
	}

	/**
	 * Search for postal info
	 * 
	 * @param postalCode
	 * @param name
	 * @param after
	 * @param nameMatch
	 * @param info
	 * @return (possibly empty) list of postal infos
	 */
	@GET
	@Path(LookupResource.POSTAL)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getPostalInfos(
			@RestQuery String postalCode,
			@RestQuery String name,
			@RestQuery String after, 
			@RestQuery String nameMatch, UriInfo info) {
		Multi<PostalInfo> postals = repo.findPostalInfos(after, postalCode, name, nameMatch);
		return Util.toJson(info, postals);
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
		String s = URLDecoder.decode(id, StandardCharsets.UTF_8);
		Uni<Street> street = repo.findStreetById(s);
		return Util.responseOrEmpty(Util.toJson(info, street));
	}

	/**
	 * Search for streets
	 * 
	 * @param municipalityID
	 * @param nisCode
	 * @param municipalityName
	 * @param postalinfoID
	 * @param postalCode
	 * @param postalName
	 * @param name
	 * @param status
	 * @param after
	 * @param info
	 * @return 
	 */
	@GET
	@Path(LookupResource.STREETS)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getStreets(
			@RestQuery String municipalityID,
			@RestQuery String nisCode,
			@RestQuery String municipalityName,
			@RestQuery String postalinfoID,
			@RestQuery String postalCode,
			@RestQuery String postalName,
			@RestQuery String name,
			@RestQuery String status,
			@RestQuery String after, UriInfo info) {
		Multi<Street> streets = repo.findStreets(after, municipalityID, nisCode, municipalityName, 
			postalinfoID, postalCode, postalName, name, status);
		return Util.toJson(info, streets);
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