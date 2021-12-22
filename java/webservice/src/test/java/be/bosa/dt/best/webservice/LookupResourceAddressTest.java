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
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Bart Hanssens
 */
@QuarkusTest
@QuarkusTestResource(PostgisServer.class)
public class LookupResourceAddressTest extends LookupResourceTest {
	@Test
    public void testAddressEndpoint() {
		testFound(LookupResource.ADDRESSES)
			.and().body(matchesJsonSchemaInClasspath("address-collection-schema.json"));
    }
	
	@Test
    public void testAddressNotFound() {
		testNotFound(LookupResource.ADDRESSES);
    }


    public void testAddressEmbedded() {
		testFound(LookupResource.ADDRESSES + "?embed=true").body("embed", notNullValue());
	}

	@Test
    public void testAddressNotEmbedded() {
		testNotFound(LookupResource.ADDRESSES);
    }

	@Test
	public void testAddressMunicipalityBxl() {
		String bxl = "BE.BRUSSELS.BRIC.ADM.MUNICIPALITY/21004/7";
		testFindByParams(LookupResource.ADDRESSES, Map.of("municipalityID", bxl), "address-collection-schema.json")
			.body("items.municipality.id", everyItem(equalTo(bxl)));
	}

	@Test
	public void testAddressMunicipalityVl() {
		String vl = "https://data.vlaanderen.be/id/gemeente/23027/2002-08-13T17:32:32";
		testFindByParams(LookupResource.ADDRESSES, Map.of("municipalityID", vl), "address-collection-schema.json")
			.body("items.municipality.id", everyItem(equalTo(vl)));
	}

	@Test
	public void testAddressMunicipalityWal() {
		String wal = "geodata.wallonie.be/id/Municipality/63049/4";
		testFindByParams(LookupResource.ADDRESSES, Map.of("municipalityID", wal), "address-collection-schema.json")
			.body("items.municipality.id", everyItem(equalTo(wal)));
	}
	
	@Test
	public void testAddressMunicipalityNameBxl() {
		String bxl = "BE.BRUSSELS.BRIC.ADM.MUNICIPALITY/21004/7";
		testFindByParams(LookupResource.ADDRESSES, Map.of("municipalityName", "Bruxelles"), "address-collection-schema.json")
			.body("items.municipality.id", everyItem(equalTo(bxl)));
	}

	@Test
	public void testAddressMunicipalityNameVl() {
		String vl = "https://data.vlaanderen.be/id/gemeente/23027/2002-08-13T17:32:32";
		testFindByParams(LookupResource.ADDRESSES, Map.of("municipalityName", "Halle"), "address-collection-schema.json")
			.body("items.municipality.id", everyItem(equalTo(vl)));
	}

	@Test
	public void testAddressMunicipalityNameWal() {
		String wal = "geodata.wallonie.be/id/Municipality/63049/4";
		testFindByParams(LookupResource.ADDRESSES, Map.of("municipalityName", "Malmedy"), "address-collection-schema.json")
			.body("items.municipality.id", everyItem(equalTo(wal)));
	}

	@Test
	public void testAddressNisCodeBxl() {
		String bxl = "BE.BRUSSELS.BRIC.ADM.MUNICIPALITY/21004/7";
		testFindByParams(LookupResource.ADDRESSES, Map.of("nisCode", "21004"), "address-collection-schema.json")
			.body("items.municipality.id", everyItem(equalTo(bxl)));
	}

	@Test
	public void testAddressNisCodeVl() {
		String vl = "https://data.vlaanderen.be/id/gemeente/23027/2002-08-13T17:32:32";
		testFindByParams(LookupResource.ADDRESSES, Map.of("nisCode", "23027"), "address-collection-schema.json")
			.body("items.municipality.id", everyItem(equalTo(vl)));
	}

	@Test
	public void testAddressNisCodeWal() {
		String wal = "geodata.wallonie.be/id/Municipality/63049/4";
		testFindByParams(LookupResource.ADDRESSES, Map.of("nisCode", "63049"), "address-collection-schema.json")
			.body("items.municipality.id", everyItem(equalTo(wal)));
	}

	@Test
	public void testAddressPostalinfoBxl() {
		String bxl = "BE.BRUSSELS.BRIC.ADM.PZ/1130/2";
		testFindByParams(LookupResource.ADDRESSES, Map.of("postalinfoID", bxl), "address-collection-schema.json")
			.body("items.postalinfo.id", everyItem(equalTo(bxl)));
	}

	@Test
	public void testAddressPostalinfoVl() {
		String vl = "https://data.vlaanderen.be/id/postinfo/1502/2002-08-13T16:37:33";
		testFindByParams(LookupResource.ADDRESSES, Map.of("postalinfoID", vl), "address-collection-schema.json")
			.body("items.postalinfo.id", everyItem(equalTo(vl)));
	}

	@Test
	public void testAddresPostalinfoWal() {
		String wal = "geodata.wallonie.be/id/PostalInfo/4960/1";
		testFindByParams(LookupResource.ADDRESSES, Map.of("postalinfoID", wal), "address-collection-schema.json")
			.body("items.postalinfo.id", everyItem(equalTo(wal)));
	}

	@Test
	public void testAddressPostalcodeBxl() {
		String bxl = "BE.BRUSSELS.BRIC.ADM.PZ/1130/2";
		testFindByParams(LookupResource.ADDRESSES, Map.of("postalCode", "1130"), "address-collection-schema.json")
			.body("items.postalinfo.id", everyItem(equalTo(bxl)));
	}

	@Test
	public void testAddressPostalcodeVl() {
		String vl = "https://data.vlaanderen.be/id/postinfo/1502/2002-08-13T16:37:33";
		testFindByParams(LookupResource.ADDRESSES, Map.of("postalCode", "1502"), "address-collection-schema.json")
			.body("items.postalinfo.id", everyItem(equalTo(vl)));
	}

	@Test
	public void testAddresPostalcodeWal() {
		String wal = "geodata.wallonie.be/id/PostalInfo/4960/1";
		testFindByParams(LookupResource.ADDRESSES, Map.of("postalCode", "4960"), "address-collection-schema.json")
			.body("items.postalinfo.id", everyItem(equalTo(wal)));
	}
	
	@Test
	public void testAddressPostalcodeStreetnameHousenumber() {
		String vl = "https://data.vlaanderen.be/id/straatnaam/26057/2013-10-05T15:59:10.067";
		testFindByParams(LookupResource.ADDRESSES, 
				Map.of("postalCode", "1502", "streetName", "Claesplein", "houseNumber", "2"), "address-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items.street.id", everyItem(equalTo(vl)),
					"items.street.href", everyItem(notNullValue()),
					"items.housenumber[0]", equalTo("2"));
	}
	
	@Test
	public void testAddressStreetIDBxl() {
		String bxl = "BE.BRUSSELS.BRIC.ADM.STR/4003/2";
		testFindByParams(LookupResource.ADDRESSES, Map.of("streetID", bxl), "address-collection-schema.json")
			.body("items.size()", equalTo(119),
					"items.street.id", everyItem(equalTo(bxl)),
					"items.street.href", everyItem(notNullValue()));
	}
	
	@Test
	public void testAddressStreetIDBxlEmbed() {
		String bxl = "BE.BRUSSELS.BRIC.ADM.STR/4003/2";
		testFindByParams(LookupResource.ADDRESSES, Map.of("streetID", bxl, "embed", "true"), "address-collection-schema.json")
			.body("items.size()", equalTo(119),
					"items.street.href", everyItem(notNullValue()),
					"embedded", notNullValue());
	}

	@Test
	public void testAddressStreetIDVl() {
		String vl = "https://data.vlaanderen.be/id/straatnaam/26081/2013-10-05T15:59:10.067";
		testFindByParams(LookupResource.ADDRESSES, Map.of("streetID", vl), "address-collection-schema.json")
			.body("items.size()", equalTo(242),
					"items.street.id", everyItem(equalTo(vl)),
					"items.street.href", everyItem(notNullValue()));
	}

	@Test
	public void testAddressStreetIDWal() {
		String wal = "geodata.wallonie.be/id/Streetname/7737097/1";
		testFindByParams(LookupResource.ADDRESSES, Map.of("streetID", wal), "address-collection-schema.json")
			.body("items.size()", equalTo(11),
					"items.street.id", everyItem(equalTo(wal)),
					"items.street.href", everyItem(notNullValue()));
	}
	
	@Test
	public void testAddressStreetHousenumber() {
		String vl = "https://data.vlaanderen.be/id/straatnaam/26057/2013-10-05T15:59:10.067";
		testFindByParams(LookupResource.ADDRESSES, Map.of("streetID", vl, "houseNumber", "2"), "address-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items.street.id", everyItem(equalTo(vl)),
					"items.street.href", everyItem(notNullValue()),
					"items.housenumber[0]", equalTo("2"));
	}
	
	@Test
	public void testAddressStreetnameHousenumber() {
		String vl = "https://data.vlaanderen.be/id/straatnaam/26057/2013-10-05T15:59:10.067";
		testFindByParams(LookupResource.ADDRESSES, Map.of("streetName", "Claesplein", "houseNumber", "2"), "address-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items.street.id", everyItem(equalTo(vl)),
					"items.street.href", everyItem(notNullValue()),
					"items.housenumber[0]", equalTo("2"));
	}
}
