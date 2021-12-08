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

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgisContainerProvider;

/**
 *
 * @author Bart Hanssens
 */
public class PostgisServer implements QuarkusTestResourceLifecycleManager  {
	protected static JdbcDatabaseContainer<?> pgContainer;
	
	static {
		pgContainer = (JdbcDatabaseContainer) new PostgisContainerProvider().newInstance("14-3.1")
			.withDatabaseName("best")
			.withInitScript("data/postgis.sql")
			.withClasspathResourceMapping("data/addresses_part.csv", "/tmp/addresses.csv", BindMode.READ_ONLY)
			.withClasspathResourceMapping("data/municipalities.csv", "/tmp/municipalities.csv", BindMode.READ_ONLY)
			.withClasspathResourceMapping("data/municipalityparts.csv", "/tmp/municipalityparts.csv", BindMode.READ_ONLY)
			.withClasspathResourceMapping("data/postals.csv", "/tmp/postals.csv", BindMode.READ_ONLY)
			.withClasspathResourceMapping("data/streets_part.csv", "/tmp/streets.csv", BindMode.READ_ONLY);
	}

	@Override
    public Map<String, String> start() {	
		pgContainer.start();
		String host = pgContainer.getHost();
		int port = pgContainer.getFirstMappedPort();
		String dbName = pgContainer.getDatabaseName();
		
		System.err.println("datasource" + new String("postgresql://" + host + ":" + port + "/" + dbName));
		return Map.of("quarkus.datasource.reactive.url", "postgresql://" + host + ":" + port + "/" + dbName);
	}

	@Override
    public void stop() {
		pgContainer.stop();
	}
}
