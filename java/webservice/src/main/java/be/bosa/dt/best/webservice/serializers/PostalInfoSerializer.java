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

import be.bosa.dt.best.webservice.LookupResource;
import be.bosa.dt.best.webservice.entities.PostalInfo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

import javax.inject.Singleton;


/**
 * Serializes BeST municipality to JSON.
 * 
 * Note that the serialization is not complete, additional processing is required to create the final JSON
 * (e.g. to embed additional objects, add links to first / next page)
 * 
 * @see <a ahref="https://www.gcloud.belgium.be/rest/">G-Cloud REST guidelines</a>
 * @author Bart Hanssens
 */
@Singleton
public class PostalInfoSerializer extends BestSerializer<PostalInfo> {
	@Override
	public void serialize(PostalInfo postalinfo, JsonGenerator jg, SerializerProvider sp) throws IOException {
		jg.writeStartObject();
        jg.writeStringField("id", postalinfo.id);
		jg.writeStringField("self", getHref(LookupResource.POSTAL, postalinfo.id));
		jg.writeStringField("postalcode", postalinfo.zipcode);
		writeLangObject(jg, postalinfo.name_nl, postalinfo.name_fr, postalinfo.name_de);
        jg.writeEndObject();
	}
}