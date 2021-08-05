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
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;

/**
 * Loads XML BeST data into an RDBMS, in this case PostGIS (based on Postgres)
 * Requires (flat) directory of unzipped XML files
 * 
 * @author Bart Hanssens
 */
public class PostGisLoader {
	private String dbStr;
	private Properties dbProp;
	private final GeoCoder geoCoder;
		
	private static final Logger LOG = Logger.getLogger(PostGisLoader.class.getName());

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
	 * Create enums
	 * 
	 * @throws SQLException 
	 */
	public void createEnums() throws SQLException {
		LOG.info("Create enums");

		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();
			
			stmt.execute("CREATE TYPE enumStatus " +
				" AS ENUM('current', 'proposed', 'retired', 'reserved')");
			stmt.execute("CREATE TYPE enumStreetnameType " +
				" AS ENUM('hamlet', 'streetname')");
			stmt.execute("CREATE TYPE enumPositionGeometryMethodValueType " +
				" AS ENUM('assignedByAdministrator', 'derivedFromObject')");
			stmt.execute("CREATE TYPE enumPositionSpecificationValueType " +
				" AS ENUM('building','buildingUnit', 'entrance', 'mooringPlace', 'municipality', 'parcel', 'plot', 'stand', 'street');");
		}
	}
	
	/**
	 * Create the tables
	 * 
	 * @throws SQLException 
	 */
	public void createTables() throws SQLException {
		LOG.info("Create tables");

		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();

			stmt.execute("CREATE TABLE Address(" +
					"	identifier VARCHAR(40) NOT NULL, " +
					"	minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1, " +
					"	mIdentifier VARCHAR(40) NOT NULL, " +
					"	mMinorVersionIdentifier SMALLINT, " +
					"	mpIdentifier VARCHAR(40), " +
					"	mpMinorVersionIdentifier SMALLINT, " +
					"	sIdentifier VARCHAR(4) NOT NULL, " +
					"	sMinorVersionIdentifier SMALLINT, " +
					"	pIdentifier VARCHAR(40) NOT NULL, " +
					"	pMinorVersionIdentifier SMALLINT, " +
					"	housenumber VARCHAR(15), " +
					"	boxnumber VARCHAR(35), " +
					"	officiallyAssigned BOOLEAN, " +
					"	status enumStatus NOT NULL, " +
					"	validFrom TIMESTAMP NOT NULL, " +
					"	validTo TIMESTAMP, " +
					"	beginLifeSpanVersion TIMESTAMP NOT NULL," +
					"	endLifeSpanVersion TIMESTAMP, " +
					"	point GEOMETRY(POINT, 31370) NOT NULL, " +
					"	positionGeometryMethod enumPositionGeometryMethodValueType, " +
					"	positionSpecification enumPositionSpecificationValueType) ");

			stmt.execute("CREATE TABLE Municipality(" +
					"	identifier VARCHAR(40) NOT NULL, " +
					"	minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1, " +
					"	refnisCode VARCHAR(5) NOT NULL, " +
					"	nameNL VARCHAR(100), " +
					"	nameFR VARCHAR(100), " +
					"	nameDE VARCHAR(100))");

			stmt.execute("CREATE TABLE PartOfMunicipality(" +
					"	identifier VARCHAR(40) NOT NULL, " +
					"	minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1, " +
					"	nameNL VARCHAR(100), " +
					"	nameFR VARCHAR(100), " +
					"	nameDE VARCHAR(100));");

			stmt.execute("CREATE TABLE PostalInfo( " +
					"	identifier VARCHAR(40) NOT NULL, " +
					"	minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1, " +
					"	postalCode VARCHAR(4) NOT NULL, " +
					"	nameNL VARCHAR(240), " +
					"	nameFR VARCHAR(240), " +
					"	nameDE VARCHAR(240))");
	
			stmt.execute("CREATE TABLE Street(" +
					"	identifier VARCHAR(40) NOT NULL, " +
					"	minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1, " +
					"	mIdentifier VARCHAR(40) NOT NULL, " +
					"	mMinorVersionIdentifier SMALLINT, " +
					"	nameNL VARCHAR(100), " +
					"	nameFR VARCHAR(100), " +
					"	nameDE VARCHAR(100), " +
					"	homonymAddition VARCHAR(25), " +
					"	streetnameType enumStreetnameType, " +
					"	status enumStatus NOT NULL, " +
					"	validFrom TIMESTAMP NOT NULL, " +
					"	validTo TIMESTAMP, " +
					"	beginLifeSpanVersion TIMESTAMP NOT NULL, " +
					"	endLifeSpanVersion TIMESTAMP);");
		}
	}

	public void addConstraints() throws SQLException {
		// add primary keys, indices and constraints after loading data, for performance
		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();

			LOG.info("Set primary keys");
			stmt.execute("ALTER TABLE Address " +
				" ADD CONSTRAINT pkAddress PRIMARY KEY(identifier, minorVersionIdentifier)");
			stmt.execute("ALTER TABLE Municipality " +
				" ADD CONSTRAINT pkMunicipality PRIMARY KEY(identifier, minorVersionIdentifier)");
			stmt.execute("ALTER TABLE PartOfMunicipality " +
				" ADD CONSTRAINT pkPartOfMunicipality PRIMARY KEY(identifier, minorVersionIdentifier)");
			stmt.execute("ALTER TABLE PostalInfo " +
				" ADD CONSTRAINT pkPostalInfo PRIMARY KEY(identifier, minorVersionIdentifier)");
			stmt.execute("ALTER TABLE Street " +
				" ADD CONSTRAINT pkStreet PRIMARY KEY(identifier, minorVersionIdentifier)");
			stmt.execute("ALTER TABLE Event " +
				" ADD CONSTRAINT pkEvent PRIMARY KEY(eventIdentifier)");

			LOG.info("Set foreign keys");
			stmt.execute("ALTER TABLE Address ADD CONSTRAINT fkAddressMunicipality "+
				" FOREIGN KEY (mIdentifier, mMinorVersionIdentifier) "+
				" REFERENCES Municipality(identifier, minorVersionIdentifier)");
			stmt.execute("ALTER TABLE Address ADD CONSTRAINT fkAddressPartOfMunicipality " +
				" FOREIGN KEY (mpIdentifier, mpMinorVersionIdentifier) "+
				" REFERENCES PartOfMunicipality(identifier, minorVersionIdentifier)");
			stmt.execute("ALTER TABLE Address ADD CONSTRAINT fkAddressPostalInfo " +
				" FOREIGN KEY (pIdentifier, pMinorVersionIdentifier) " +
				" REFERENCES PostalInfo(identifier, minorVersionIdentifier)");
			stmt.execute("ALTER TABLE Address ADD CONSTRAINT fkAddressStreet " +
				" FOREIGN KEY (sIdentifier, sMinorVersionIdentifier) "+
				" REFERENCES Street(identifier, minorVersionIdentifier)");
		}
	}
	
	public void updateIndex() throws SQLException {
		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();

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
				prep.setString(1, a.getId() + "/" + a.getId());
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
				prep.setString(7, a.getFromDate());
				prep.setString(8, a.getTillDate());
				prep.setString(9, a.getBeginLife());
				prep.setString(10, a.getEndLife());
				
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
				String geom = "";
				try {
					Coordinate coord = geoCoder.toCoords(p.getX(), p.getY(), false);
					geom = geoCoder.toWkb(coord);
				} catch(IOException ioe) {
					LOG.warning("Could not convert coordinates");
				}

				prep.setString(1, a.getIDVersion());
				prep.setString(2, a.getCity().getIDVersion());
				prep.setString(3, a.getCityPart().getIDVersion());
				prep.setString(4, a.getStreet().getIDVersion());
				prep.setString(5, a.getPostal().getIDVersion());
				prep.setString(6, a.getNumber());
				prep.setString(7, a.getBox());
				prep.setString(8, a.getStatus());
				prep.setString(9, a.getFromDate());
				prep.setString(10, a.getTillDate());
				prep.setString(11, a.getBeginLife());
				prep.setString(12, a.getEndLife());
				prep.setString(13, geom);

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
		createTables();
		addConstraints();
	
		try(Connection conn = getConnection()) {		
			PreparedStatement prep = conn.prepareStatement(
				"INSERT INTO PostalInfo(identifier, postalCode, nameNL, nameFR, nameDE) " +
				" VALUES (?, ?, ?, ?, ?)");
			loadPostals(prep, xmlPath);

			prep = conn.prepareStatement(
				"INSERT INTO Municipality(identifier, refnisCode, nameNL, nameFR, nameDE) " +
				" VALUES (?, ?, ?, ?, ?)");
			loadMunicipalities(prep, xmlPath);

			prep = conn.prepareStatement(
				"INSERT INTO PartOfMunicipality(identifier, refnisCode, nameNL, nameFR, nameDE) " +
				" VALUES (?, ?, ?, ?)");
			loadMunicipalityParts(prep, xmlPath);
			prep = conn.prepareStatement(
				"INSERT INTO Street(identifier, nameNL, nameFR, nameDE, " +
					" status, validFrom, validTo, beginLifeSpanVersion, endLifeSpanVersion ) " +
				" VALUES (?, ?, ?, ?, " + 
						" ?, ?, ?, ?, ?)");
			loadStreets(prep, xmlPath);

			prep = conn.prepareStatement(
				"INSERT INTO Address(identifier, mIdentifier, mpIdentifier, sIdentifier, pIdentifier, " +
					" housenumber, boxnumber, status, validFrom, " +
					" validTo, beginLifespanVersion, endLifespanVersion, point) " +
				" VALUES (?, ?, ?, ?, ? " +
						" ?, ?, ?, ?, " +
						" ?, ?, ?, ?)");
			loadAddresses(prep, xmlPath);
		}
		updateIndex();
		//addPostalTables();
	}

	/**
	 * Constructor
	 * 
	 * @param dbstr 
	 * @param prop 
	 * @throws java.lang.Exception 
	 */
	public PostGisLoader(String dbstr, Properties prop) throws Exception {
		this.dbStr = dbstr;
		this.dbProp = prop;
		geoCoder = new GeoCoder();
	}

	public PostGisLoader(String dbStr) throws Exception {
		this(dbStr, new Properties());
	}
}
