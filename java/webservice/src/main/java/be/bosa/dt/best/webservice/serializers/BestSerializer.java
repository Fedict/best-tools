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
import be.bosa.dt.best.webservice.entities.BestEntity;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.eclipse.microprofile.config.ConfigProvider;


/**
 * Abstract serializer, helper class
 * 
 * @author Bart Hanssens
 * @param <T>
 */
public abstract class BestSerializer<T extends BestEntity> extends StdSerializer<T> {
	// can't use ConfigProperty annotation here
	protected final static String BASEURL = 
			ConfigProvider.getConfig().getValue("be.bosa.dt.best.webservice.url", String.class);

	protected static String getHref(String type, String id) {
		return BestSerializer.BASEURL + LookupResource.API + type + "/" + id.replace("/", "%2F");
	}

	/**
	 * Write language object
	 * 
	 * @param jg JSON writer
	 * @param nl Dutch name
	 * @param fr French name
	 * @param de German name
	 * @throws IOException 
	 */
	protected void writeLangObject(JsonGenerator jg, String nl, String fr, String de) throws IOException {
		jg.writeObjectFieldStart("name");
		if (nl != null && !nl.isEmpty()) {
			jg.writeStringField("nl", nl);
		}
		if (fr != null && !fr.isEmpty()) {
			jg.writeStringField("fr", fr);
		}
		if (de != null && !de.isEmpty()) {
			jg.writeStringField("de", de);
		}
		jg.writeEndObject();
	}

    public BestSerializer() {
        this(null);
    }
 
    public BestSerializer(Class<T> t) {
        super(t);
    }
}