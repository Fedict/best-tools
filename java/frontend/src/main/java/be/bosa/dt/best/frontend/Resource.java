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
package be.bosa.dt.best.frontend;

import be.bosa.dt.best.frontend.entities.AddressDistance;
import be.bosa.dt.best.frontend.entities.Municipality;
import be.bosa.dt.best.frontend.entities.Street;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

/**
 *
 * @author Bart Hanssens
 */
@Path("/best")
public class Resource {
	@GET
	@Path("/nearest/gps/{x}/{y}")
	@Operation(summary = "Get nearest addresses by coordinates")
	public List<AddressDistance> nearestAddress(
			@Parameter(description = "X coordinate (longitude)", required = true, example = "4.23")
			@PathParam("x") double x, 
			@Parameter(description = "Y coordinate (latitude)", required = true, example = "50.73")	
			@PathParam("y") double y) {
		return AddressDistance.findNearestByGPS(x, y);
	}

	@GET
	@Path("/municipalities")
	@Operation(summary = "Get all municipalities")
	public List<Municipality> allMunicipalities() {
		return null;
	}

	@GET
	@Path("/streets/by-postal/{code}")
	@Operation(summary = "Get all streets by postal code")
	public List<Street> streetsByPostal(
			@Parameter(description = "postal code", required = true, example = "1500")	
			@PathParam("code") String code) {
		return Street.findByPostal(code);
	}

/*	@GET
	@Path("/municipality")
	public List<Municipality> find(@QueryParam("id") String id, @QueryParam("postal") String postal) {
		
	}

	@GET
	@Path("/street")
*/	
}
