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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * End point for additional (license) info
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
@Path("/")
public class AboutResource {
	private static String about = "";

	/**
	 * Read file from resource JAR
	 * 
	 * @param path
	 * @return
	 * @throws IOException 
	 */
	private String getResource(String path) throws IOException {
		try(InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
	
	@GET
	@Path("about")
	@Produces(MediaType.TEXT_HTML)
	public String getAbout() throws IOException {
		if (about.isEmpty()) {
			about = getResource("licenses/about.html");
			// replace placeholders by respective licenses
			String[] files = { "bosa-bsd", 
				"openjdk-gplv2ce", "postgis-gplv2", "postgres", "proj4j-apache", "quarkus-apache" };
			for (String f: files) {
				String license = getResource("licenses/" + f + ".txt");
				about = about.replaceFirst("\\{" + f + "\\}", license);
			}
		}
		return about;
	}



}