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
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
	private final String dbStr;
		
	private static final Logger LOG = Logger.getLogger(PostGisLoader.class.getName());
	private static final BestRegion[] REGIONS = new BestRegion[] { 
									BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA };

	/**
	 * Get database connection based on JDBC string
	 * 
	 * @return
	 * @throws SQLException 
	 */
	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(dbStr);
	}

	/**
	 * Truncate all tables
	 * 
	 * @throws SQLException 
	 */
	public void dropTables() throws SQLException {
		LOG.info("Drop tables");

		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();
			stmt.execute("DROP TABLE IF EXISTS Address CASCADE");			
			stmt.execute("DROP TABLE IF EXISTS PostalInfo CASCADE");
			stmt.execute("DROP TABLE IF EXISTS Street CASCADE");
			stmt.execute("DROP TABLE IF EXISTS PartOfMunicipality CASCADE");
			stmt.execute("DROP TABLE IF EXISTS Municipality CASCADE");

		}
	}
	/**
	 * Drop various enum types
	 * 
	 * @throws SQLException 
	 */
	public void dropEnums() throws SQLException {
		LOG.info("Drop enums");

		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();
			
			stmt.execute("DROP TYPE IF EXISTS enumStatus");
			stmt.execute("DROP TYPE IF EXISTS enumStreetnameType");
			stmt.execute("DROP TYPE IF EXISTS enumPositionGeometryMethodValueType");
			stmt.execute("DROP TYPE IF EXISTS  enumPositionSpecificationValueType");
		}
	}

	/**
	 * Create various enum types
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
					"	identifier VARCHAR(100) NOT NULL, " +
					"	minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1, " +
					"	mIdentifier VARCHAR(100) NOT NULL, " +
					"	mMinorVersionIdentifier SMALLINT, " +
					"	mpIdentifier VARCHAR(100), " +
					"	mpMinorVersionIdentifier SMALLINT, " +
					"	sIdentifier VARCHAR(100) NOT NULL, " +
					"	sMinorVersionIdentifier SMALLINT, " +
					"	pIdentifier VARCHAR(100) NOT NULL, " +
					"	pMinorVersionIdentifier SMALLINT, " +
					"	housenumber VARCHAR(15), " +
					"	boxnumber VARCHAR(35), " +
					"	officiallyAssigned BOOLEAN, " +
					"	status enumStatus NOT NULL, " +
					"	validFrom TIMESTAMPTZ, " +
					"	validTo TIMESTAMPTZ, " +
					"	beginLifeSpanVersion TIMESTAMPTZ, " +
					"	endLifeSpanVersion TIMESTAMPTZ, " +
					"	point GEOMETRY(POINT, 31370), " +
					"	positionGeometryMethod enumPositionGeometryMethodValueType, " +
					"	positionSpecification enumPositionSpecificationValueType) ");

			stmt.execute("CREATE TABLE Municipality(" +
					"	identifier VARCHAR(100) NOT NULL, " +
					"	minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1, " +
					"	refnisCode VARCHAR(5) NOT NULL, " +
					"	nameNL VARCHAR(100), " +
					"	nameFR VARCHAR(100), " +
					"	nameDE VARCHAR(100))");

			stmt.execute("CREATE TABLE PartOfMunicipality(" +
					"	identifier VARCHAR(100) NOT NULL, " +
					"	minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1, " +
					"	nameNL VARCHAR(100), " +
					"	nameFR VARCHAR(100), " +
					"	nameDE VARCHAR(100));");

			stmt.execute("CREATE TABLE PostalInfo( " +
					"	identifier VARCHAR(100) NOT NULL, " +
					"	minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1, " +
					"	postalCode VARCHAR(4) NOT NULL, " +
					"	nameNL VARCHAR(240), " +
					"	nameFR VARCHAR(240), " +
					"	nameDE VARCHAR(240))");
	
			stmt.execute("CREATE TABLE Street(" +
					"	identifier VARCHAR(100) NOT NULL, " +
					"	minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1, " +
					"	mIdentifier VARCHAR(100) NOT NULL, " +
					"	mMinorVersionIdentifier SMALLINT, " +
					"	nameNL VARCHAR(100), " +
					"	nameFR VARCHAR(100), " +
					"	nameDE VARCHAR(100), " +
					"	homonymAddition VARCHAR(25), " +
					"	streetnameType enumStreetnameType, " +
					"	status enumStatus NOT NULL, " +
					"	validFrom TIMESTAMPTZ, " +
					"	validTo TIMESTAMPTZ, " +
					"	beginLifeSpanVersion TIMESTAMPTZ, " +
					"	endLifeSpanVersion TIMESTAMPTZ);");
		}
	}

	/**
	 * Create primary/foreign keys and additional constraints
	 * 
	 * @throws SQLException 
	 */
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
			//stmt.execute("ALTER TABLE Event " +
			//	" ADD CONSTRAINT pkEvent PRIMARY KEY(eventIdentifier)");

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
	
	/**
	 * Create spatial index and update statistics
	 * 
	 * @throws SQLException 
	 */
	public void createIndex() throws SQLException {
		try(Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();

			LOG.info("Set spatial index");			
			stmt.execute("CREATE INDEX idxAddressPoint ON Address USING GIST(point)");

			LOG.info("Set foreign key idexes");			
			stmt.execute("CREATE INDEX idxAddressMunicipality ON Address(mIdentifier)");
			stmt.execute("CREATE INDEX idxAddressPostal ON Address(pIdentifier)");
			stmt.execute("CREATE INDEX idxStreetMunicipality ON Street(mIdentifier)");

			LOG.info("Set street text indexes");				
			stmt.execute("CREATE INDEX idxStreetNL ON Street(LOWER(nameNL) varchar_pattern_ops)");
			stmt.execute("CREATE INDEX idxStreetFR ON Street(LOWER(nameFR) varchar_pattern_ops)");
			stmt.execute("CREATE INDEX idxStreetDE ON Street(LOWER(nameDE) varchar_pattern_ops)");

			stmt.execute("CREATE INDEX idxGinStreetNL ON Street USING (nameNL gin_trgm_ops)");
			stmt.execute("CREATE INDEX idxGinStreetFR ON Street USING (nameFR gin_trgm_ops)");
			stmt.execute("CREATE INDEX idxGinStreetDE ON Street USING (nameDE gin_trgm_ops)");
			
			LOG.info("Set Municipality text indexes");				
			stmt.execute("CREATE INDEX idxMunicipalityNL ON Municipality(LOWER(nameNL) varchar_pattern_ops)");
			stmt.execute("CREATE INDEX idxMunicipalityFR ON Municipality(LOWER(nameFR) varchar_pattern_ops)");
			stmt.execute("CREATE INDEX idxMunicipalityDE ON Municipality(LOWER(nameDE) varchar_pattern_ops)");

			stmt.execute("CREATE INDEX idxGinMunicipalityNL ON Municipality USING (nameNL gin_trgm_ops)");
			stmt.execute("CREATE INDEX idxGinMunicipalityFR ON Municipality USING (nameFR gin_trgm_ops)");
			stmt.execute("CREATE INDEX idxGinMunicipalityDE ON Municipality USING (nameDE gin_trgm_ops)");
			
			LOG.info("Update statistics");
			stmt.execute("VACUUM FULL ANALYZE");
		}
	}
	
	/**
	 * Load postal code info
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @param reg region
	 * @throws SQLException 
	 */
	private void loadPostals(PreparedStatement prep, Path xmlPath, BestRegion reg) throws SQLException {
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

	/**
	 * Load municipality info
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @param reg region code
	 * @throws SQLException 
	 */
	private void loadMunicipalities(PreparedStatement prep, Path xmlPath, BestRegion reg) throws SQLException {
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
			// insert per 1000 records
			if (++cnt % 1000 == 0) {
				prep.executeBatch();
				LOG.log(Level.INFO, "Inserted {0}", cnt);
			}
		}
		prep.executeBatch();
		LOG.log(Level.INFO, "Inserted {0}", cnt);
	}

	/**
	 * Load municipality part info
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @param reg region
	 * @throws SQLException 
	 */
	private void loadMunicipalityParts(PreparedStatement prep, Path xmlPath, BestRegion reg) throws SQLException {
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
			// insert per 1000 records
			if (++cnt % 1000 == 0) {
				prep.executeBatch();
				LOG.log(Level.INFO, "Inserted {0}", cnt);
			}
		}
		prep.executeBatch();
		LOG.log(Level.INFO, "Inserted {0}", cnt);
	}

	/**
	 * Load streets
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @param reg region code
	 * @throws SQLException 
	 */
	private void loadStreets(PreparedStatement prep, Path xmlPath, BestRegion reg) throws SQLException {
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
			prep.setObject(6, a.getStatus(), Types.OTHER);
			prep.setObject(7, a.getFromDate(), Types.TIMESTAMP_WITH_TIMEZONE);
			prep.setObject(8, a.getTillDate(), Types.TIMESTAMP_WITH_TIMEZONE);
			prep.setObject(9, a.getBeginLife(), Types.TIMESTAMP_WITH_TIMEZONE);
			prep.setObject(10, a.getEndLife(), Types.TIMESTAMP_WITH_TIMEZONE);

			prep.addBatch();
			// insert per 10.000 records
			if (++cnt % 10_000 == 0) {
				prep.executeBatch();
				LOG.log(Level.INFO, "Inserted {0}", cnt);
			}
		}
		prep.executeBatch();
	}
	
	private String encodePoint(GeoCoder geoCoder, Geopoint p) {
		double x = p.getX();
		double y = p.getY();

		if (x > 1 && y > 1 && p.getSrs().equals("31370")) {
			try {
				Coordinate coord = geoCoder.toCoords(x, y, false);
				return geoCoder.toWkb(coord);
			} catch (IOException ioe) {
				LOG.log(Level.WARNING, "Could not convert coordinates", ioe);
			}
		} else {
			LOG.log(Level.WARNING, "Invalid coordinates {0} {1} ({2})", new Object[] { x, y, p.getSrs() });
		}
		return null;
	}

	/**
	 * Load addresses
	 * 
	 * @param prep prepared statement
	 * @param xmlPath XML BeST directory
	 * @param reg region code
	 * @throws SQLException 
	 */
	private void loadAddresses(PreparedStatement prep, Path xmlPath, BestRegion reg) throws SQLException, Exception {
		LOG.log(Level.INFO, "Starting addresses {0}", reg.getName());
		int cnt = 0;

		GeoCoder geoCoder = new GeoCoder();

		AddressReader reader = new AddressReader();
		Stream<Address> addresses = reader.read(reg, xmlPath);
		Iterator<Address> iter = addresses.iterator();

		while (iter.hasNext()) {
			Address a = iter.next();

			Geopoint p = a.getPoint();
			
			String geom = encodePoint(geoCoder, p);

			prep.setString(1, a.getIDVersion());
			prep.setString(2, a.getCity().getIDVersion());
			prep.setString(3, null);
			prep.setString(4, a.getStreet().getIDVersion());
			prep.setString(5, a.getPostal().getIDVersion());
			prep.setString(6, a.getNumber());
			prep.setString(7, a.getBox());
			prep.setObject(8, a.getStatus(), Types.OTHER);
			prep.setObject(9, a.getFromDate(), Types.TIMESTAMP_WITH_TIMEZONE);
			prep.setObject(10, a.getTillDate(), Types.TIMESTAMP_WITH_TIMEZONE);
			prep.setObject(11, a.getBeginLife(), Types.TIMESTAMP_WITH_TIMEZONE);
			prep.setObject(12, a.getEndLife(), Types.TIMESTAMP_WITH_TIMEZONE);
			prep.setObject(13, geom, Types.OTHER);

			prep.addBatch();
			// insert per 10_000 records
			if (++cnt % 10_000 == 0) {
				prep.executeBatch();
				LOG.log(Level.INFO, "Inserted {0}", cnt);
			}
		}
		// insert per 10_000 records
		prep.executeBatch();
		LOG.log(Level.INFO, "Inserted {0}", cnt);
	}

	/**
	 * Load XML BeST data files for a specific regionin a database file.
	 * 
	 * @param xmlPath path to XML BeST files
	 * @param reg best region
	 * @throws ClassNotFoundException
	 * @throws SQLException 
	 */
	private void loadDataRegion(Path xmlPath, BestRegion reg) throws SQLException {
		LOG.log(Level.INFO, "Loading data for region {}", reg);

		try(Connection conn = getConnection()) {
			PreparedStatement prep = conn.prepareStatement(
				"INSERT INTO PostalInfo(identifier, postalCode, nameNL, nameFR, nameDE) " +
				" VALUES (?, ?, ?, ?, ?)");
			loadPostals(prep, xmlPath, reg);
			
			prep = conn.prepareStatement(
				"INSERT INTO Municipality(identifier, refnisCode, nameNL, nameFR, nameDE) " +
				" VALUES (?, ?, ?, ?, ?)");
			loadMunicipalities(prep, xmlPath, reg);

			prep = conn.prepareStatement(
				"INSERT INTO PartOfMunicipality(identifier, nameNL, nameFR, nameDE) " +
				" VALUES (?, ?, ?, ?)");
			loadMunicipalityParts(prep, xmlPath, reg);

			prep = conn.prepareStatement(
				"INSERT INTO Street(identifier, mIdentifier, nameNL, nameFR, nameDE, " +
					" status, validFrom, validTo, beginLifeSpanVersion, endLifeSpanVersion ) " +
				" VALUES (?, ?, ?, ?, ?, " + 
						" ?, ?, ?, ?, ?)");
			loadStreets(prep, xmlPath, reg);
	
			prep = conn.prepareStatement(
				"INSERT INTO Address(identifier, mIdentifier, mpIdentifier, sIdentifier, pIdentifier, " +
					" housenumber, boxnumber, status, validFrom, " +
					" validTo, beginLifespanVersion, endLifespanVersion, point) " +
				" VALUES (?, ?, ?, ?, ?, " +
						" ?, ?, ?, ?, " +
						" ?, ?, ?, ?)");
			loadAddresses(prep, xmlPath, reg);
		}
	}

	/**
	 * Load XML BeST data files in a database.
	 * 
	 * @param xmlPath path to XML BeST files
	 * @throws ClassNotFoundException
	 * @throws SQLException 
	 */
	public void loadData(Path xmlPath) throws ClassNotFoundException, SQLException {
		dropTables();
		dropEnums();
	
		createEnums();
		createTables();
		addConstraints();

		List<Runnable> tasks = new ArrayList<>();
		for (BestRegion reg: REGIONS){
			tasks.add(() -> {
				try {
					loadDataRegion(xmlPath, reg);
				} catch (SQLException ex) {
					LOG.log(Level.SEVERE, "Loading for region failed", ex);
				}
			});
		}
		tasks.stream().parallel().forEach(Runnable::run);

		createIndex();
		//addPostalTables();
	}

	/**
	 * Constructor 
	 *
	 * @param dbstr
	 * @throws Exception 
	 */
	public PostGisLoader(String dbstr) throws Exception {
		this.dbStr = dbstr;
	}
}
