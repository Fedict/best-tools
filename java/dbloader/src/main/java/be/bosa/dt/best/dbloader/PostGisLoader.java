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
package be.bosa.dt.best.dbloader;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads XML BeST data into an RDBMS, in this case PostGIS (based on Postgres)
 * Requires (flat) directory of unzipped XML files
 * 
 * @author Bart Hanssens
 */
public class PostGisLoader extends DbLoader {
	private static final Logger LOG = Logger.getLogger(PostGisLoader.class.getName());

	@Override
	public void createSpatialTable(boolean gps) throws SQLException {
		LOG.info("Address table");

		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();
			stmt.execute("CREATE EXTENSION IF NOT EXISTS postgis");

			stmt.execute("CREATE TABLE addresses(" +
							"id VARCHAR(88) NOT NULL, " +
							"city_id VARCHAR(88) NOT NULL, " +
							"part_id VARCHAR(88), " +
							"street_id VARCHAR(88) NOT NULL, " +
							"postal_id VARCHAR(88) NOT NULL, " +
							"houseno VARCHAR(12), " +
							"boxno VARCHAR(40), " +
							"status VARCHAR(10), " + 
							"l72x DOUBLE PRECISION NOT NULL, " +
							"l72y DOUBLE PRECISION NOT NULL, " +
							"geom POINT NOT NULL)");
		}
	}

	@Override
	public void addConstraints() throws SQLException {
		// add primary keys, indices and constraints after loading data, for performance
		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();

			LOG.info("Constraints and indices");
			// unfortunately not guaranteed to be unique in files / constraints issues
			stmt.execute("CREATE INDEX ON streets(city_id)");
			stmt.execute("CREATE INDEX ON addresses(postal_id)");
			stmt.execute("CREATE INDEX ON addresses(city_id)");
			stmt.execute("CREATE INDEX ON addresses(part_id)");
			
			LOG.info("Set primary keys");
			stmt.execute("ALTER TABLE municipalities ADD PRIMARY KEY(id)");
			stmt.execute("ALTER TABLE municipalityparts ADD PRIMARY KEY(id)");
			stmt.execute("ALTER TABLE streets ADD PRIMARY KEY(id)");
			stmt.execute("ALTER TABLE addresses ADD PRIMARY KEY(id)");

			LOG.info("Set spatial index");			
			stmt.execute("CREATE INDEX ON addresses USING GIST(geom)");
			
			LOG.info("Update statistics");
			stmt.execute("VACUUM FULL ANALYZE");
		}
	}
	
	public PostGisLoader(String dbStr) {
		super(dbStr, new Properties());
	}
}