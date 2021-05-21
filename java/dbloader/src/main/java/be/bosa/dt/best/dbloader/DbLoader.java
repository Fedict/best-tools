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
import be.bosa.dt.best.dao.Street;
import be.bosa.dt.best.xmlreader.AddressReader;
import be.bosa.dt.best.xmlreader.MunicipalityPartReader;
import be.bosa.dt.best.xmlreader.MunicipalityReader;
import be.bosa.dt.best.xmlreader.PostalReader;
import be.bosa.dt.best.xmlreader.StreetnameReader;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Stream;


/**
 * Loads XML BeST data into an RDBMS
 * Requires (flat) directory of unzipped XML files
 * 
 * @author Bart Hanssens
 */
public abstract class DbLoader {
	private static final Logger LOG = Logger.getLogger(DbLoader.class.getName());
	private final String dbStr;
	private final Properties dbProp;

	public String getDbStr() {
		return dbStr;
	}

	public Properties getDbProp() {
		return dbProp;
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(getDbStr(), getDbProp());
	}

	/**
	 * Create address table with geometry column
	 * 
	 * @param gps use gps coordinates instead of Lambert72
	 * @throws SQLException 
	 */
	public abstract void createSpatialTable(boolean gps) throws SQLException;

	/**
	 * Create the rest of the "normal" tables
	 * 
	 * @throws SQLException 
	 */
	public void createNonSpatialTables() throws SQLException {
		LOG.info("Create tables");

		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();

			stmt.execute("CREATE TABLE postals(" +
							"id VARCHAR(88) NOT NULL, " +
							"zipcode VARCHAR(4) NOT NULL, " +
							"name_nl VARCHAR(240), " +
							"name_fr VARCHAR(240), " +
							"name_de VARCHAR(240))");

			stmt.execute("CREATE TABLE municipalities(" +
							"id VARCHAR(88) NOT NULL, " +
							"niscode VARCHAR(5) NOT NULL, " +
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
		}
	}

	/**
	 * Add auxiliary tables to speed up searches on postal codes
	 * 
	 * @throws SQLException
	 */
	public void addPostalTables() throws SQLException {
		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();

			LOG.info("Postal tables");
			stmt.execute("CREATE TABLE postal_municipalities AS " +
				"SELECT DISTINCT city_id, zipcode " +
				"FROM addresses a, postals p " +
				"WHERE a.postal_id = p.id");

			stmt.execute("CREATE TABLE postal_streets AS " +
				"SELECT DISTINCT street_id, zipcode " +
				"FROM addresses a, postals p " +
				"WHERE a.postal_id = p.id");
		}
	}

	/**
	 * Add additional constraints and indices
	 * 
	 * @throws SQLException
	 */
	public abstract void addConstraints() throws SQLException;
	
	/**
	 * Load postal code info
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @throws SQLException 
	 */
	private void loadPostals(PreparedStatement prep, Path xmlPath) throws SQLException {
		for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
			LOG.log(Level.INFO, "Starting postals {0}", reg.getName());
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
					LOG.log(Level.INFO, "Inserted {0}", cnt);
				}
			}
			prep.executeBatch();
			LOG.log(Level.INFO, "Inserted {0}", cnt);
		}
	}

	/**
	 * Load municipality info
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @throws SQLException 
	 */
	private void loadMunicipalities(PreparedStatement prep, Path xmlPath) throws SQLException {
		for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
			LOG.log(Level.INFO, "Starting municipalities {0}", reg.getName());
			int cnt = 0;

			MunicipalityReader reader = new MunicipalityReader();
			Stream<Municipality> municipalities = reader.read(reg, xmlPath);
			Iterator<Municipality> iter = municipalities.iterator();

			while (iter.hasNext()) {
				Municipality a = iter.next();
				prep.setString(1, a.getIDVersion());
				prep.setString(2, a.getId());
				prep.setString(3, a.getName("nl"));
				prep.setString(4, a.getName("fr"));
				prep.setString(5, a.getName("de"));

				prep.addBatch();
				// insert per 10000 records
				if (++cnt % 10_000 == 0) {
					prep.executeBatch();
					LOG.log(Level.INFO, "Inserted {0}", cnt);
				}
			}
			prep.executeBatch();
			LOG.log(Level.INFO, "Inserted {0}", cnt);
		}
	}
	/**
	 * Load municipality part info
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @throws SQLException 
	 */
	private void loadMunicipalityParts(PreparedStatement prep, Path xmlPath) throws SQLException {
		for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
			LOG.log(Level.INFO, "Starting municipality parts {0}", reg.getName());
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
					LOG.log(Level.INFO, "Inserted {0}", cnt);
				}
			}
			prep.executeBatch();
			LOG.log(Level.INFO, "Inserted {0}", cnt);
		}
	}

	/**
	 * Load streets
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @throws SQLException 
	 */
	private void loadStreets(PreparedStatement prep, Path xmlPath) throws SQLException {
		for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
			LOG.log(Level.INFO, "Starting streets {0} ", reg.getName());
			int cnt = 0;

			StreetnameReader reader = new StreetnameReader();
			Stream<Street> streets = reader.read(reg, xmlPath);
			Iterator<Street> iter = streets.iterator();

			while (iter.hasNext()) {
				Street a = iter.next();

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
					LOG.log(Level.INFO, "Inserted {0}", cnt);
				}
			}
			prep.executeBatch();
			LOG.log(Level.INFO, "Inserted {0}", cnt);
		}
	}

	/**
	 * Load addresses
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @throws SQLException 
	 */
	private void loadAddresses(PreparedStatement prep, Path xmlPath) throws SQLException {
		for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
			LOG.log(Level.INFO, "Starting addresses {0}", reg.getName());
			int cnt = 0;

			AddressReader reader = new AddressReader();
			Stream<Address> addresses = reader.read(reg, xmlPath);
			Iterator<Address> iter = addresses.iterator();

			while (iter.hasNext()) {
				Address a = iter.next();

				Geopoint p = a.getPoint();
				String geom = String.format("POINT(%s %s)", p.getX(), p.getY());
				prep.setString(1, a.getIDVersion());
				prep.setString(2, a.getCity().getIDVersion());
				prep.setString(3, a.getCityPart().getIDVersion());
				prep.setString(4, a.getStreet().getIDVersion());
				prep.setString(5, a.getPostal().getIDVersion());
				prep.setString(6, a.getNumber());
				prep.setString(7, a.getBox());
				prep.setString(8, a.getStatus());
				prep.setDouble(9, p.getX());
				prep.setDouble(10, p.getY());
				prep.setString(11, geom);

				prep.addBatch();
				// insert per 10000 records
				if (++cnt % 10_000 == 0) {
					prep.executeBatch();
					LOG.log(Level.INFO, "Inserted {0}", cnt);
				}
			}
			prep.executeBatch();
			LOG.log(Level.INFO, "Inserted {0}", cnt);
		}
	}

	/**
	 * Load XML BeST data files in a database file.
	 * 
	 * @param xmlPath path to XML BeST files
	 * @param gps store coordinates as GPS coordinates
	 * @throws ClassNotFoundException
	 * @throws SQLException 
	 */
	public void loadData(Path xmlPath, boolean gps) throws ClassNotFoundException, SQLException {
		createSpatialTable(gps);
		createNonSpatialTables();

		try(Connection conn = getConnection()) {
			
			PreparedStatement prep = conn.prepareStatement("INSERT INTO postals VALUES (?, ?, ?, ?, ?)");
			loadPostals(prep, xmlPath);

			prep = conn.prepareStatement("INSERT INTO municipalities VALUES (?, ?, ?, ?, ?)");
			loadMunicipalities(prep, xmlPath);

			prep = conn.prepareStatement("INSERT INTO municipalityparts VALUES (?, ?, ?, ?)");
			loadMunicipalityParts(prep, xmlPath);

			prep = conn.prepareStatement("INSERT INTO streets VALUES (?, ?, ?, ?, ?, ?)");
			loadStreets(prep, xmlPath);

			prep = conn.prepareStatement(gps
				? "INSERT INTO addresses VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_Transform(ST_GeomFromText(?, 31370), 4326))"
				: "INSERT INTO addresses VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?, 31370))"
			);
			loadAddresses(prep, xmlPath);
		}
		addConstraints();
		addPostalTables();
	}

	/**
	 * Constructor
	 * 
	 * @param dbstr 
	 * @param prop 
	 */
	public DbLoader(String dbstr, Properties prop) {
		this.dbStr = dbstr;
		this.dbProp = prop;
	}
}
