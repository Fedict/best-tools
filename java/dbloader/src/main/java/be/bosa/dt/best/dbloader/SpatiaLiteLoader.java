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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

/**
 * Loads XML BeST data into an RDBMS, in this case SpatiaLite (SQLite extension)
 * Requires (flat) directory of unzipped XML files
 * 
 * @author Bart Hanssens
 */
public class SpatiaLiteLoader extends DbLoader {
	private final static Logger LOG = Logger.getLogger(SpatiaLiteLoader.class.getName());

	// increase performance
	private final static SQLiteConfig config = new SQLiteConfig();
	static {
		config.enableLoadExtension(true);
		config.setOpenMode(SQLiteOpenMode.NOMUTEX);
		config.setCacheSize(1024);
		config.setPageSize(8192);
		config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
		config.setJournalMode(SQLiteConfig.JournalMode.OFF);
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(getDbStr(), getDbProp());
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT load_extension('mod_spatialite')");
		return conn;
	}
	
	@Override
	public void initDb() throws SQLException {
		LOG.info("Initalizing DB");


		LOG.info("Creating table");
		// We could use an ORM tool like MyBatis or Hibernate, but let's use plain JDBC
		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();
			stmt.execute("SELECT InitSpatialMetaData()");
			
			stmt.execute("CREATE TABLE postals(" +
							"id VARCHAR(88) NOT NULL, " +
							"zipcode VARCHAR(4), " +
							"name_nl VARCHAR(240), " +
							"name_fr VARCHAR(240), " +
							"name_de VARCHAR(240))");

			stmt.execute("CREATE TABLE municipalities(" +
							"id VARCHAR(88) NOT NULL, " +
							"name_nl VARCHAR(80), " +
							"name_fr VARCHAR(80), " +
							"name_de VARCHAR(80))");

			stmt.execute("CREATE TABLE municipalityparts(" +
							"id VARCHAR(88) NOT NULL, " +
							"name_nl VARCHAR(80), " +
							"name_fr VARCHAR(80), " +
							"name_de VARCHAR(80))");

			stmt.execute("CREATE TABLE streets(" +
							"id VARCHAR(88) NOT NULL, " +
							"city_id VARCHAR(88) NOT NULL, " +
							"name_nl VARCHAR(80), " +
							"name_fr VARCHAR(80), " +
							"name_de VARCHAR(80), " +
							"status VARCHAR(10))");

			stmt.execute("CREATE TABLE addresses(" +
							"id VARCHAR(88) NOT NULL, " +
							"city_id VARCHAR(88) NOT NULL, " +
							"part_id VARCHAR(88) NOT NULL, " +
							"street_id VARCHAR(88) NOT NULL, " +
							"postal_id VARCHAR(88) NOT NULL, " +
							"houseno VARCHAR(12), " +
							"boxno VARCHAR(40), " +
							"status VARCHAR(10))");

			LOG.info("Adding geo column");
			stmt.execute("SELECT AddGeometryColumn('addresses', 'geom', 4326, 'POINT', 'XY')");
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
			stmt.execute("SELECT CreateSpatialIndex('addresses', 'geom')");
		}
	}
	
	public SpatiaLiteLoader(String dbStr) {
		super(dbStr, config.toProperties());
	}
}
