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

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

/**
 *
 * @author Bart Hanssens
 */
@OpenAPIDefinition(
	 info = @Info(
        title="v2 Belgian Streets and Addresses REST API Demo",
        version = "2.0.0",
		description = " OpenAPI specification of the v2 Belgian Streets and Addresses (BeSt) REST API. \n" +
			"By relying on a consolidated dataset, this new version delivers even greater performance and more features than ever.\n" +
			"\n" +
			"An address is an identification of the fixed location of a property.\n" +
			"The full address is a hierarchy consisting of components such as geographic names, with an increasing level of detail (e.g.,town, then street name, then house number or name).\n" +
			"It may also include a post code or other postal descriptors.",
		contact = @Contact(
			name = "BOSA DG DT servicedesk",
			email = "servicedesk.DTO@bosa.fgov.be"
		)
	)
)

@ApplicationScoped
@Path("best/api/v2")
public class LookupResource {
	@Inject
	Repository repo;

	@Operation(summary = "Get an address by full ID")
	@Path("/addresses/{identifier}")
	public Uni<Address> getAddress(@RestPath String identifier) {
		return repo.findAddressById(identifier);
	}
	
	@Operation(summary = "Get an address by full ID")
	@Path("/addresses")
	public Multi<Address> getAddresses(
			@RestQuery Optional<String> municipalityId,
			@RestQuery Optional<String> municipalityName,
			@RestQuery Optional<String> partOfMunicipalityId,
			@RestQuery Optional<String> partOfMunicipalityName,
			@RestQuery Optional<String> streetId,
			@RestQuery Optional<String> streetName,
			@RestQuery Optional<String> housenumber,
			@RestQuery Optional<String> boxnumber,
			@RestQuery Optional<String> status) {
		return null;
	}
}
