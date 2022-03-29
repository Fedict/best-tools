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
import org.junit.jupiter.api.Test;

/**
 *
 * @author Bart Hanssens
 */
@QuarkusTest
@QuarkusTestResource(PostgisServer.class)
public class LookupResourceMunicipalityTest extends LookupResourceTest {
	@Test
    public void testMunicipalityEndpoint() {
		testFound(LookupResource.MUNICIPALITIES);
    }
	
	@Test
    public void testMunicipalityNotFound() {
		testNotFound(LookupResource.MUNICIPALITIES);
    }

	@Test
	public void testMunicipalityIDBxl() {
		String bxl = "https://databrussels.be/id/municipality/21002/6";
		testFindByID(LookupResource.MUNICIPALITIES, bxl, "municipality-schema.json")
			.body("municipalityName.nl", equalTo("Oudergem"),
					"municipalityName.fr", equalTo("Auderghem"));
	}

	@Test
	public void testMunicipalityIDVl() {
		String vl = "https://data.vlaanderen.be/id/gemeente/71034/2002-08-13T17:32:32";
		testFindByID(LookupResource.MUNICIPALITIES, vl, "municipality-schema.json")
			.body("municipalityName.nl", equalTo("Leopoldsburg"),
					"municipalityName.fr", equalTo("Bourg-Léopold"),
					"municipalityName.de", equalTo("Leopoldsburg"));
	}

	@Test
	public void testMunicipalityIDWal() {
		String wal = "geodata.wallonie.be/id/Municipality/63067/4";
		testFindByID(LookupResource.MUNICIPALITIES, wal, "municipality-schema.json")
			.body("municipalityName.fr", equalTo("Saint-Vith"),
					"municipalityName.de", equalTo("Sankt Vith"));
	}
	
	@Test
	public void testMunicipalityNameAccent() {
		testFindByParams(LookupResource.MUNICIPALITIES, Map.of("municipalityName", "Chievres"), "municipality-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items[0].municipalityName.fr", equalTo("Chièvres"));
	}

	@Test
	public void testMunicipalityNameLower() {
		testFindByParams(LookupResource.MUNICIPALITIES, Map.of("municipalityName", "sankt vith"), "municipality-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items[0].municipalityName.de", equalTo("Sankt Vith"));
	}
}
