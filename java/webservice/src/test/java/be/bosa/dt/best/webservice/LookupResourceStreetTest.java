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
	public void testStreetFindIDBxl() {
		String bxl = "BE.BRUSSELS.BRIC.ADM.STR/3974/2";
		testFindByID(LookupResource.STREETS, bxl, "street-schema.json")
			.body("name.nl", equalTo("Masuistraat"),
					"name.fr", equalTo("Rue Masui"),
					"status", "current");
	}

	@Test
	public void testStreetFindIDVl() {
		String vl = "https://data.vlaanderen.be/id/straatnaam/26089/2013-10-05T15:59:10.067";
		testFindByID(LookupResource.STREETS, vl, "street-schema.json")
			.body("name.nl", equalTo("Elbeekstraat"),
					"status", "current");
	}

	@Test
	public void testStreetFindIDWal() {
		String wal = "geodata.wallonie.be/id/Streetname/7737097/1 ";
		testFindByID(LookupResource.STREETS, wal, "street-schema.json")
			.body("name.fr", equalTo("Sur les Roches"),
					"status", "current");
	}
}
