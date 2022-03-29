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

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Bart Hanssens
 */
@QuarkusTest
@QuarkusTestResource(PostgisServer.class)
public class LookupResourceStreetTest extends LookupResourceTest {
	@Test
    public void testStreetEndpoint() {
		testFound(LookupResource.STREETS);
    }
	
	@Test
    public void testStreetNotFound() {
		testNotFound(LookupResource.STREETS);
    }

	@Test
	public void testStreetIDBxl() {
		String bxl = "•	https://databrussels.be/id/streetname/3974/2";
		testFindByID(LookupResource.STREETS, bxl, "street-schema.json")
			.body("streetName.nl", equalTo("Masuistraat"),
					"streetName.fr", equalTo("Rue Masui"),
					"streetNameStatus", equalTo("current"));
	}

	@Test
	public void testStreetIDVl() {
		String vl = "https://data.vlaanderen.be/id/straatnaam/26089/2013-10-05T15:59:10.067";
		testFindByID(LookupResource.STREETS, vl, "street-schema.json")
			.body("streetName.nl", equalTo("Elbeekstraat"),
					"streetNameStatus", equalTo("current"));
	}

	@Test
	public void testStreetIDWal() {
		String wal = "geodata.wallonie.be/id/Streetname/7737097/1";
		testFindByID(LookupResource.STREETS, wal, "street-schema.json")
			.body("streetName.fr", equalTo("Sur les Roches"),
					"streetNameStatus", equalTo("current"));
	}
	
	@Test
	public void testStreetNameAccent() {
		testFindByParams(LookupResource.STREETS, Map.of("streetName", "Rue de l'aerodrome"), "street-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items[0].streetName.fr", equalTo("Rue de l'Aérodrome"));
	}

	@Test
	public void testStreetNameLower() {
		testFindByParams(LookupResource.STREETS, Map.of("streetName", "abdijstraat"), "street-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items[0].streetName.nl", equalTo("Abdijstraat"));
	}

	@Test
	public void testStreetNameMultiple() {
		testFindByParams(LookupResource.STREETS, Map.of("streetName", "Rue de la Chapelle"), "street-collection-schema.json")
			.body("items.size()", equalTo(2),
					"items[0].streetName.fr", equalTo("Rue de la Chapelle"));
	}
	
	@Test
	public void testStreetStatusRetired() {
		testFindByParams(LookupResource.STREETS, Map.of("streetNameStatus", "retired"), "street-collection-schema.json")
			.body("items.size()", equalTo(10));
	}

	@Test
	public void testStreetPostalID() {
		testFindByParams(LookupResource.STREETS, 
			Map.of("postalinfoID", "https://data.vlaanderen.be/id/postinfo/1502/2002-08-13T16:37:33"),
			"street-collection-schema.json")
			.body("items.size()", equalTo(72),
				"items.streetName.nl", hasItem("Claesplein"));
	}

	@Test
	public void testStreetPostalCode() {
		testFindByParams(LookupResource.STREETS, Map.of("postalCode", "1502"), "street-collection-schema.json")
			.body("items.size()", equalTo(72),
					"items.streetName.nl", hasItem("Claesplein"));
	}

	@Test
	public void testStreetPostalName() {
		testFindByParams(LookupResource.STREETS, Map.of("postalName", "Lembeek"), "street-collection-schema.json")
			.body("items.size()", equalTo(72),
					"items.streetName.nl", hasItem("Claesplein"));
	}

	@Test
	public void testStreetMunicipalityID() {
		testFindByParams(LookupResource.STREETS, Map.of("municipalityID", 
			"https://data.vlaanderen.be/id/gemeente/23027/2002-08-13T17:32:32"), "street-collection-schema.json")
			.body("items.size()", equalTo(250),
					"items.streetName.nl", hasItem("Claesplein"));
	}

	@Test
	public void testStreetMunicipalityIDNotFound() {
		testNotFoundByParams(LookupResource.STREETS, Map.of("municipalityID", "https://data.vlaanderen.be/id/gemeente/99999"));
	}

	@Test
	public void testStreetNiscode() {
		testFindByParams(LookupResource.STREETS, Map.of("municipalityCode", "21004"), "street-collection-schema.json")
			.body("items.size()", equalTo(250),
					"items.municipalityName.fr", hasItem("Rue du Camp"));
	}
	
	@Test
	public void testStreetNiscodeNotFound() {
		testNotFoundByParams(LookupResource.STREETS, Map.of("municipalityCode", "9999"));
	}

	@Test
	public void testStreetMunicipalityName() {
		testFindByParams(LookupResource.STREETS, Map.of("municipalityName", "Brussel"), "street-collection-schema.json")
			.body("items.size()", equalTo(250),
					"items.municipalityName.fr", hasItem("Rue du Camp"));
	}
	
	@Test
	public void testStreetMunicipalityNameNotFound() {
		testNotFoundByParams(LookupResource.STREETS, Map.of("municipalityName", "Washington"));
	}

	@Test
	public void testStreetNameMunicipalityName() {
		testFindByParams(LookupResource.STREETS, Map.of("municipalityName", "Brussel", "name", "Rue du Camp"), 
			"street-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items.streetName.fr", hasItem("Rue du Camp"));
	}

	@Test
	public void testStreetPagination() {
		testFound(LookupResource.STREETS)
			.body("items.size()", equalTo(250),
					"first", notNullValue(),
					"next", notNullValue());
	}
}
