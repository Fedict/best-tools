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
package be.bosa.dt.best.webservice.serializers;

import be.bosa.dt.best.webservice.CoordConverter;
import be.bosa.dt.best.webservice.LookupResource;
import be.bosa.dt.best.webservice.entities.Address;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import javax.inject.Singleton;

import org.locationtech.proj4j.ProjCoordinate;

/**
 * Serializes BeST address to JSON.
 * 
 * Note that the serialization is not complete, additional processing is required to create the final JSON
 * (e.g. to embed additional objects, add links to first / next page)
 * 
 * @see <a ahref="https://www.gcloud.belgium.be/rest/">G-Cloud REST guidelines</a>
 * @author Bart.Hanssens
 */
@Singleton
public class AddressSerializer extends BestSerializer<Address> {
	/**
	 * Write best entity as object with ID and HTTP URI (used to point to embedded object)
	 * 
	 * @param jg JSON writer
	 * @param field field name
	 * @param id full identifier
	 * @param type entity type
	 * @throws IOException 
	 */
	private void writeObject(JsonGenerator jg, String field, String id, String type) throws IOException {
		jg.writeObjectFieldStart(field);
		jg.writeStringField("id", id);
		jg.writeStringField("href", getHref(type, id));
		jg.writeEndObject();
	}

	@Override
	public void serialize(Address address, JsonGenerator jg, SerializerProvider sp) throws IOException {
		jg.writeStartObject();
        jg.writeStringField("id", address.id);
		jg.writeStringField("self", getHref(LookupResource.ADDRESSES, address.id));
		writeObject(jg, "street", address.sIdentifier, LookupResource.STREETS);
		writeObject(jg, "municipality",address.mIdentifier, LookupResource.MUNICIPALITIES);
		writeObject(jg, "postal",address.pIdentifier, LookupResource.POSTAL);
		jg.writeStringField("housenumber", address.housenumber);
		if (address.boxnumber != null) {
			jg.writeStringField("boxnumber", address.boxnumber);
		}
		if (address.point.x > 0) {
			jg.writeObjectFieldStart("lambert72");
			jg.writeNumberField("x", address.point.x);
			jg.writeNumberField("y", address.point.y);
			jg.writeEndObject();
			ProjCoordinate coord = CoordConverter.l72ToGps(address.point.x, address.point.y);
			jg.writeObjectFieldStart("gps");
			jg.writeNumberField("x", coord.x);
			jg.writeNumberField("y", coord.y);
			jg.writeEndObject();
		}
		jg.writeStringField("status", address.status);
		if (address.validFrom != null) {
			jg.writeStringField("validFrom", address.validFrom.toString());
		}
		if (address.validTo != null) {
			jg.writeStringField("validTo", address.validTo.toString());			
		}
        jg.writeEndObject();
	}
}
