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
public class LookupResourceMunicipalitypartTest extends LookupResourceTest {
	@Test
    public void testPartOfMunicipalityEndpoint() {
		testFound(LookupResource.MUNICIPALITY_PARTS);
    }
	
	@Test
    public void testParOfMunicipalityNotFound() {
		testNotFound(LookupResource.MUNICIPALITY_PARTS);
    }

	@Test
	public void testMunicipalitypartsNameAccent() {
		testFindByParams(LookupResource.MUNICIPALITY_PARTS, Map.of("partOfMunicipalityName", "Moderscheid"), "municipalitypart-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items[0].partOfMunicipalityName.de", equalTo("Möderscheid"));
	}

	@Test
	public void testMunicipalitypartsNameLower() {
		testFindByParams(LookupResource.MUNICIPALITY_PARTS, Map.of("partOfMunicipalityName", "ath"), "municipalitypart-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items[0].partOfMunicipalityName.fr", equalTo("Ath"));
	}
	
	@Test
	public void testMunicipalitypartsNameMultiple() {
		testFindByParams(LookupResource.MUNICIPALITY_PARTS, Map.of("partOfMunicipalityName", "Momalle"), "municipalitypart-collection-schema.json")
			.body("items.size()", equalTo(2),
					"items[0].partOfMunicipalityName.fr", equalTo("Momalle"));
	}
}
