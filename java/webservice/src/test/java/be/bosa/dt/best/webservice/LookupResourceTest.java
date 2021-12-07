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
import io.restassured.response.ValidatableResponse;
import org.junit.BeforeClass;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 *
 * @author Bart Hanssens
 */
@Testcontainers
public abstract class LookupResourceTest {
	@Container
	public PostgreSQLContainer pgContainer = 
		new PostgreSQLContainer(
				DockerImageName.parse("postgis/postgis:14-3.1").asCompatibleSubstituteFor("postgres"))
			.withDatabaseName("best")
            .withUsername("sa");

	@BeforeClass
	public void runBefore() {
		pgContainer.withInitScript("postgis.sql");
	}

	protected ValidatableResponse testFound(String part) {
		return when().get(LookupResource.API + part)
			.then().statusCode(200).contentType(ContentType.JSON);
	}

	protected ValidatableResponse testNotFound(String part) {
		return when().get(LookupResource.API + part + "/foobar")
			.then().statusCode(404).contentType(ContentType.JSON);
	}
}
