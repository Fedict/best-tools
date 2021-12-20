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

import static io.restassured.RestAssured.when;
import io.restassured.http.ContentType;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import io.restassured.response.ValidatableResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Helper class
 * 
 * @author Bart Hanssens
 */
public abstract class LookupResourceTest  {
	/**
	 * Test which should return at least one result
	 * 
	 * @param part
	 * @return 
	 */
	public ValidatableResponse testFound(String part) {
		return when().get(LookupResource.API + part)
				.then().statusCode(200).contentType(ContentType.JSON);
	}

	/**
	 * Test for something that's definitively not in the database
	 * 
	 * @param part
	 * @return 
	 */
	public ValidatableResponse testNotFound(String part) {
		return when().get(LookupResource.API + part + "/foobar")
				.then().statusCode(404).contentType(ContentType.JSON);
	}
	
	/**
	 * Test which should return one result
	 * 
	 * @param part
	 * @param id  ID
	 * @param schema
	 * @return 
	 */
	public ValidatableResponse testFindByID(String part, String id, String schema) {
		return when().get(LookupResource.API + part + "/" + URLEncoder.encode(id, StandardCharsets.UTF_8))
				.then().statusCode(200).contentType(ContentType.JSON)
					.body(matchesJsonSchemaInClasspath(schema));
	}
	
	/**
	 * Test which should return one ore more result
	 * 
	 * @param part
	 * @param param
	 * @param value
	 * @param schema
	 * @return 
	 */
	public ValidatableResponse testFindByParams(String part, Map<String,String> paramValues, String schema) {
		String q = "";
		for(Entry e: paramValues.entrySet()) {
			q += e.getKey() + "=" + e.getValue();
		}
		return when().get(LookupResource.API + part + "?" + q)
				.then().statusCode(200).contentType(ContentType.JSON)
					.body(matchesJsonSchemaInClasspath(schema));
	}
}
