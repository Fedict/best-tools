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

import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Bart Hanssens
 */
@QuarkusTest
@QuarkusTestResource(PostgisServer.class)
public class LookupResourcePostalnfoTest extends LookupResourceTest {
	@Test
    public void testPostalinfoEndpoint() {
		testFound(LookupResource.POSTAL);
    }

	@Test
    public void testPostalinfoNotFound() {
		testNotFound(LookupResource.POSTAL);
    }

	@Test
	public void testPostalinfoFindIDBxl() {
		String bxl = "BE.BRUSSELS.BRIC.ADM.PZ/1000/2";
		testFindByID(LookupResource.POSTAL, bxl, "postal-schema.json")
			.body("name.nl", equalTo("Brussel (Centrum)"),
					"name.fr", equalTo("Bruxelles (Centre)"));
	}

	@Test
	public void testPostalinfoFindIDVl() {
		String vl = "https://data.vlaanderen.be/id/postinfo/2323/2002-08-13T16:37:33";
		testFindByID(LookupResource.POSTAL, vl, "postal-schema.json")
			.body("name.nl", equalTo("Wortel"));
	}

	@Test
	public void testPostalinfoFindIDWal() {
		String wal = "geodata.wallonie.be/id/PostalInfo/7973/1";
		testFindByID(LookupResource.POSTAL, wal, "postal-schema.json");
	}

	@Test
	public void testPostalinfoFindParamPostalcode() {
		testFindByParam(LookupResource.POSTAL, "postalCode", "2000", "postal-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items[0].name.nl", equalTo("Antwerpen"));
	}

	@Test
	public void testPostalinfoFindParamNameBXLNoAccent() {
		testFindByParam(LookupResource.POSTAL, "name", "Parlement Europeen", "postal-collection-schema.json")
			.body("items.size()", equalTo(1),
					"items[1].name.fr", equalTo("Parlement Européen"));
	}

	@Test
	public void testPostalinfoFindParamNameVLLower() {
		testFindByParam(LookupResource.POSTAL, "name", "antwerpen", "postal-collection-schema.json")
			.body("items.size()", equalTo(6),
					"items[0].name.nl", equalTo("Antwerpen"));
	}

	public void testPostalinfoFindParamNameWal() {
		// Walloon Region does not provide names
	}

}
