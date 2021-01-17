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

import be.bosa.dt.best.dao.Address;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.Geopoint;
import be.bosa.dt.best.dao.Municipality;
import be.bosa.dt.best.dao.Postal;
import be.bosa.dt.best.dao.Streetname;
import be.bosa.dt.best.xmlreader.AddressReader;
import be.bosa.dt.best.xmlreader.MunicipalityPartReader;
import be.bosa.dt.best.xmlreader.MunicipalityReader;
import be.bosa.dt.best.xmlreader.PostalReader;
import be.bosa.dt.best.xmlreader.StreetnameReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads XML BeST data into an RDBMS, in this case H2GIS
 * Requires (flat) directory of unzipped XML files
 * 
 * @author Bart Hanssens
 */
public class Main {
	private static Logger LOG = LoggerFactory.getLogger(Main.class);

	private final static Options OPTS = new Options()
		.addRequiredOption("x", "xmldir", true, "directory with unzipped BeST XML files")
		.addRequiredOption("d", "db", true, "database connection string");

	/**
	 * Print help info
	 */
	private static void printHelp() {
		HelpFormatter fmt = new HelpFormatter();
		fmt.printHelp("Load unzipped BeST files to a database", OPTS);
	}
	
	/**
	 * Parse command line arguments
	 * 
	 * @param args
	 * @return 
	 */
	private static CommandLine parse(String[] args) {
		CommandLineParser cli = new DefaultParser();
		try {
			return cli.parse(OPTS, args);
		} catch (ParseException ex) {
			printHelp();
		}
		return null;
	}

	/**
	 * Initialize database and create tables
	 * 
	 * @param jdbc database string
	 * @throws SQLException
	 */
	private static void initDb(String jdbc) throws SQLException {
		// Spatial features
		try(Connection conn = DriverManager.getConnection(jdbc)) {
			Statement stmt = conn.createStatement();
			stmt.execute("CREATE EXTENSION postgis");
		}

		// We could use an ORM tool like MyBatis or Hibernate, but let's use plain JDBC
		try(Connection conn = DriverManager.getConnection(jdbc)) {
			Statement stmt = conn.createStatement();

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
							"status VARCHAR(10), " + 
							"geom GEOMETRY)");
		}
	}

	/**
	 * Add additional constraints and indices
	 * 
	 * @param jdbc database string
	 * @throws SQLException
	 */
	private static void addConstraints(String jdbc) throws SQLException {
		// add primary keys, indices and constraints after loading data, for performance
		try(Connection conn = DriverManager.getConnection(jdbc)) {
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

	/**
	 * Load postal code info
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @throws SQLException 
	 */
	private static void loadPostals(PreparedStatement prep, Path xmlPath) throws SQLException {
		for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
			System.out.println("Starting postals " + reg.getName());
			int cnt = 0;

			PostalReader reader = new PostalReader();
			Stream<Postal> postals = reader.read(reg, xmlPath);
			Iterator<Postal> iter = postals.iterator();

			while (iter.hasNext()) {
				Postal a = iter.next();
				prep.setString(1, a.getIDVersion());
				prep.setString(2, a.getId());
				prep.setString(3, a.getName("nl"));
				prep.setString(4, a.getName("fr"));
				prep.setString(5, a.getName("de"));

				prep.addBatch();
				// insert per 10000 records
				if (++cnt % 10_000 == 0) {
					prep.executeBatch();
					LOG.info("Inserted {}", cnt);
				}
			}
			prep.executeBatch();
			LOG.info("Inserted {}", cnt);
		}
	}

	/**
	 * Load municipality info
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @throws SQLException 
	 */
	private static void loadMunicipalities(PreparedStatement prep, Path xmlPath) throws SQLException {
		for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
			LOG.info("Starting municipalities {}", reg.getName());
			int cnt = 0;

			MunicipalityReader reader = new MunicipalityReader();
			Stream<Municipality> municipalities = reader.read(reg, xmlPath);
			Iterator<Municipality> iter = municipalities.iterator();

			while (iter.hasNext()) {
				Municipality a = iter.next();
				prep.setString(1, a.getIDVersion());
				prep.setString(2, a.getName("nl"));
				prep.setString(3, a.getName("fr"));
				prep.setString(4, a.getName("de"));

				prep.addBatch();
				// insert per 10000 records
				if (++cnt % 10_000 == 0) {
					prep.executeBatch();
					LOG.info("Inserted {}", cnt);
				}
			}
			prep.executeBatch();
			LOG.info("Inserted {}", cnt);
		}
	}
	/**
	 * Load municipality part info
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @throws SQLException 
	 */
	private static void loadMunicipalityParts(PreparedStatement prep, Path xmlPath) throws SQLException {
		for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
			LOG.info("Starting municipality parts {}", reg.getName());
			int cnt = 0;

			MunicipalityPartReader reader = new MunicipalityPartReader();
			Stream<Municipality> parts = reader.read(reg, xmlPath);
			Iterator<Municipality> iter = parts.iterator();

			while (iter.hasNext()) {
				Municipality a = iter.next();
				prep.setString(1, a.getIDVersion());
				prep.setString(2, a.getName("nl"));
				prep.setString(3, a.getName("fr"));
				prep.setString(4, a.getName("de"));

				prep.addBatch();
				// insert per 10000 records
				if (++cnt % 10_000 == 0) {
					prep.executeBatch();
					LOG.info("Inserted {}", cnt);
				}
			}
			prep.executeBatch();
			LOG.info("Inserted {}", cnt);
		}
	}

	/**
	 * Load streets
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @throws SQLException 
	 */
	private static void loadStreets(PreparedStatement prep, Path xmlPath) throws SQLException {
		for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
			LOG.info("Starting streets {} ", reg.getName());
			int cnt = 0;

			StreetnameReader reader = new StreetnameReader();
			Stream<Streetname> streets = reader.read(reg, xmlPath);
			Iterator<Streetname> iter = streets.iterator();

			while (iter.hasNext()) {
				Streetname a = iter.next();

				prep.setString(1, a.getIDVersion());
				prep.setString(2, a.getCity().getIDVersion());
				prep.setString(3, a.getName("nl"));
				prep.setString(4, a.getName("fr"));
				prep.setString(5, a.getName("de"));
				prep.setString(6, a.getStatus());
				
				prep.addBatch();
				// insert per 10000 records
				if (++cnt % 10_000 == 0) {
					prep.executeBatch();
					LOG.info("Inserted {}", cnt);
				}
			}
			prep.executeBatch();
			LOG.info("Inserted {}", cnt);
		}
	}

	/**
	 * Load addresses
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @throws SQLException 
	 */
	private static void loadAddresses(PreparedStatement prep, Path xmlPath) throws SQLException {
		for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
			LOG.info("Starting addresses {}", reg.getName());
			int cnt = 0;

			AddressReader reader = new AddressReader();
			Stream<Address> addresses = reader.read(reg, xmlPath);
			Iterator<Address> iter = addresses.iterator();

			while (iter.hasNext()) {
				Address a = iter.next();

				Geopoint p = a.getPoint();
				String geom = "POINT(%s %s)".formatted(p.getX(), p.getY());
				prep.setString(1, a.getIDVersion());
				prep.setString(2, a.getCity().getIDVersion());
				prep.setString(3, a.getCityPart().getIDVersion());
				prep.setString(4, a.getStreet().getIDVersion());
				prep.setString(5, a.getPostal().getIDVersion());
				prep.setString(6, a.getNumber());
				prep.setString(7, a.getBox());
				prep.setString(8, a.getStatus());
				prep.setString(9, geom);

				prep.addBatch();
				// insert per 10000 records
				if (++cnt % 10_000 == 0) {
					prep.executeBatch();
					LOG.info("Inserted {}", cnt);
				}
			}
			prep.executeBatch();
			LOG.info("Inserted {}", cnt);
		}
	}

	/**
	 * Load XML BeST data files in a database file.
	 * 
	 * @param dbstr JDBC connection string to database
	 * @param xmlPath path to XML BeST files
	 * @throws ClassNotFoundException
	 * @throws SQLException 
	 */
	private static void loadData(String dbstr, Path xmlPath) throws ClassNotFoundException, SQLException {
		// init db
		initDb(dbstr);

		try(Connection conn = DriverManager.getConnection(dbstr)) {
			PreparedStatement prep = conn.prepareStatement("INSERT INTO postals VALUES (?, ?, ?, ?, ?)");
			loadPostals(prep, xmlPath);

			prep = conn.prepareStatement("INSERT INTO municipalities VALUES (?, ?, ?, ?)");
			loadMunicipalities(prep, xmlPath);

			prep = conn.prepareStatement("INSERT INTO municipalityparts VALUES (?, ?, ?, ?)");
			loadMunicipalityParts(prep, xmlPath);

			prep = conn.prepareStatement("INSERT INTO streets VALUES (?, ?, ?, ?, ?, ?)");
			loadStreets(prep, xmlPath);

			prep = conn.prepareStatement("INSERT INTO addresses VALUES (?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?, 31370))");
			loadAddresses(prep, xmlPath);
		}

		addConstraints(dbstr);
	}

	/**
	 * Main
	 * 
	 * @param args 
	 */
	public static void main(String[] args) {
		CommandLine cli  = parse(args);
		if (cli == null) {
			System.exit(-1);
		}

		String xmldir = cli.getOptionValue("x");
		String dbstr = cli.getOptionValue("d");
		
		Path xmlPath = Paths.get(xmldir);
		if (!xmlPath.toFile().exists()) {
			LOG.error("BEST directory does not exist");
			System.exit(-2);
		}

		try {
			loadData(dbstr, xmlPath);
		} catch (Exception e) {
			LOG.error("Failed: " + e.getMessage());
			System.exit(-3);
		}
		LOG.info("Done");
	}
}
