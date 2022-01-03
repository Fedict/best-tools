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
import be.bosa.dt.best.webservice.entities.Street;

import io.quarkus.logging.Log;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;


/**
 * Utility class
 * 
 * @author Bart Hanssens
 */
public class Util {	
	/**
	 * Convert uni of address, street, ... into JSON
	 * 
	 * @param <T>
	 * @param info information about web request
	 * @param item entity
	 * @return JSON object or null when not found
	 */
	protected static <T extends BestEntity> JsonObject toJson(UriInfo info, Uni<T> item) {
		BestEntity entity = item.await().indefinitely();
		if (entity == null) {
			return null;
		}
		String self = info.getAbsolutePath().toString();
		return JsonObject.mapFrom(entity).put("self", self);
	}

	/**
	 * Convert uni of address, street, ...into JSON
	 * 
	 * @param info information about web request
	 * @param embed objects or not
	 * @param item entity
	 * @param cache cache with JSON version of municipalities / postal info
	 * @return JSON object or null when not found
	 */
	protected static JsonObject toJson(UriInfo info, Uni<Address> item, boolean embed, Map<String,JsonObject> cache) {
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
	 * Convert multi(ple results) of addresses into JSON
	 * 
	 * @param info information about web request
	 * @param items entities
	 * @param embed add embedded part to JSON or not
	 * @param cache cache with JSON version of municipalities / postal info
	 * @return JSON object
	 */
	protected static JsonObject toJson(UriInfo info, Multi<Address> items, boolean embed, Map<String,JsonObject> cache) {
		String self = info.getAbsolutePath().toString();
	
		Map<String,Street> streets = new HashMap<>();
		Set<String> embedded = new HashSet<>();

		JsonArray arr = new JsonArray();
		items.subscribe().asStream().forEach(a -> {
			//String href = self + "/" + a.id.replace("/", "%2F");
			if (embed) {
				streets.put(a.street.id, a.street);
			}
			embedded.add(a.mIdentifier);
			if (a.mpIdentifier != null) {
				embedded.add(a.mpIdentifier);
			}
			embedded.add(a.pIdentifier);
			arr.add(JsonObject.mapFrom(a));
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
	 * Convert multi(ple results) of streets, municipalities, ...into JSON
	 * 
	 * @param <T>
	 * @param info information about web request
	 * @param items entities
	 * @return JSON object
	 */
	protected static <T extends BestEntity> JsonObject toJson(UriInfo info, Multi<T> items) {
		String self = info.getRequestUriBuilder().toString();
		System.err.println(info.getRequestUri().toString());
		JsonArray arr = new JsonArray();
		items.subscribe().asStream().forEach(a -> {
			//String href = self + "/" + a.id.replace("/", "%2F");
			arr.add(JsonObject.mapFrom(a));
				//.put("href", href));
		});

		JsonObject parentObj = new JsonObject();		
		parentObj.put("self", self);
		parentObj.put("items", arr); 

		return paginate(info, parentObj, arr);
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
	 * Return single result or "not found" JSON object
	 * 
	 * @param json
	 * @return response with single JSON result or not found JSON object
	 */
	protected static RestResponse<JsonObject> responseOrEmpty(JsonObject json) {
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
	 * Check if at least one value is not null and not empty
	 * 
	 * @param values
	 * @return true if at least one value os not empty
	 */
	protected static boolean oneNotEmpty(String... values) {
		for (String s: values) {
			if (s != null && !s.isEmpty()) {
				return true;
			}
		}
		return false;
	}
}