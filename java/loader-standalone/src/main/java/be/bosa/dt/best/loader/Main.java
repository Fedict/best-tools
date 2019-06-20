/*
 * Copyright (c) 2018, FPS BOSA DG DT
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
package be.bosa.dt.best.loader;

import be.bosa.dt.best.dao.Address;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.Municipality;
import be.bosa.dt.best.dao.Postal;
import be.bosa.dt.best.dao.Streetname;

import be.bosa.dt.best.xmlreader.AddressReader;
import be.bosa.dt.best.xmlreader.MunicipalityReader;
import be.bosa.dt.best.xmlreader.PostalReader;
import be.bosa.dt.best.xmlreader.StreetnameReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

import java.sql.Date;

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
 * Standalone tool to load data into postgresql database
 * 
 * @author Bart Hanssens
 */
public class Main {
	private final static Logger LOG = LoggerFactory.getLogger(Main.class);
	
	private final static Options OPTS = new Options()
		.addRequiredOption("i", "indir", true, "input directory")
		.addOption("d", "database", true, "database JDBC string")
		.addOption("B", "Brussels", false, "process files for Brussels")
		.addOption("F", "Flanders", false, "process files for Flanders")
		.addOption("W", "Wallonia", false, "process files for Wallonia");
	
	private final static Date TODAY = new Date(Instant.now().toEpochMilli());
	
	/**
	 * Print help info
	 */
	private static void printHelp() {
		HelpFormatter fmt = new HelpFormatter();
		fmt.printHelp("BeST loader", OPTS);
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
	 * Write data to cities table
	 * 
	 * @param conn
	 * @param region
	 * @param inPath 
	 */
	private static void writeCities(Connection conn, BestRegion region, Path inPath) {
		LOG.info("Cities{}", region.getName());
		String code = region.getCode();

		String INS = 
			"INSERT INTO best.cities VALUES(?, ?, ?, ?, ?) "
			+ "ON CONFLICT(id) DO "
			+ "UPDATE SET region = EXCLUDED.region, "
			+ " name_nl = EXCLUDED.name_nl, "
			+ " name_fr = EXCLUDED.name_fr, "
			+ " stamp = EXCLUDED.stamp ";
		
		try(Stream<Municipality> cities = new MunicipalityReader().read(region, inPath)) {
			PreparedStatement upd = conn.prepareStatement(INS);
			cities.forEach(c -> {
				try {
					upd.setString(1, c.getId());
					upd.setString(2, code);
					upd.setString(3, c.getName("nl"));
					upd.setString(4, c.getName("fr"));
					upd.setDate(5, TODAY);
					upd.execute();
				} catch (SQLException se) {
					LOG.warn("Error inserting city", se);					
				}
			});
		} catch (SQLException ue) {
			LOG.warn("Error inserting city", ue);
		}
	}

	/**
	 * Write data to streets table
	 * 
	 * @param conn
	 * @param region
	 * @param inPath 
	 */
	private static void writeStreets(Connection conn, BestRegion region, Path inPath) {
		LOG.info("Streets {}", region.getName());
		String code = region.getCode();

		String INS = 
			"INSERT INTO best.streets VALUES(?, ?, ?, ?, ?, ?) "
			+ "ON CONFLICT(id) DO "
			+ "UPDATE SET region = EXCLUDED.region, "
			+ " name_nl = EXCLUDED.name_nl, "
			+ " name_fr = EXCLUDED.name_fr, "
			+ " stamp = EXCLUDED.stamp";
		
		try(Stream<Streetname> streets = new StreetnameReader().read(region, inPath)) {
			PreparedStatement upd = conn.prepareStatement(INS);
			streets.filter(s -> s.getStatus().equals("current")).sequential().forEach(s -> {
				try {
					upd.setString(1, s.getId());
					upd.setString(2, code);
					upd.setString(3, s.getName("nl"));
					upd.setString(4, s.getName("fr"));
					upd.setString(5, s.getCity().getId());
					upd.setDate(6, TODAY);
					upd.execute();
				} catch (SQLException ue) {
					LOG.warn("Error inserting {} street {}, {}", code, s.getId());
				}
			});
		} catch (SQLException ue) {
			LOG.warn("Error deleting streets", ue);
		}
	}

	/**
	 * Write data to addresses table
	 * 
	 * @param jdbi
	 * @param region
	 * @param inPath 
	 */
	private static void writeAddresses(Connection conn, BestRegion region, Path inPath) {
		LOG.info("Addresses {}", region.getName());
		String code = region.getCode();
		
		String INS = 
			"INSERT INTO best.addresses VALUES(?, ?, ?, ?, ?, ?, ?, ?, "
			+ " ST_Transform(ST_SetSRID(ST_MakePoint(?, ?), 31370), 4326), "
			+ " ?) "
			+ " ON CONFLICT(id) DO "
			+ "UPDATE SET region = EXCLUDED.region, "
			+ " streetno = EXCLUDED.streetno, "
			+ " boxno = EXCLUDED.boxno, "
			+ " street_id = EXCLUDED.street_id, "
			+ " city_id = EXCLUDED.city_id, "
			+ " x = EXCLUDED.x, "
			+ " y = EXCLUDED.y, "
			+ " geom = EXCLUDED.geom, "
			+ " stamp = EXCLUDED.stamp";
		
		try(Stream<Address> addresses = new AddressReader().read(region, inPath)) {
			PreparedStatement upd = conn.prepareStatement(INS);
			addresses.filter(a -> a.getStatus().equals("current")).sequential().forEach(a -> {
				try {
					upd.clearParameters();
					upd.setString(1, a.getId());
					upd.setString(2, code);
					upd.setString(3, a.getNumber());
					upd.setString(4, a.getBox());
					upd.setString(5, a.getStreet().getId());
					upd.setString(6, a.getCity().getId());
					upd.setFloat(7, a.getPoint().getX());
					upd.setFloat(8, a.getPoint().getY());
					upd.setFloat(9, a.getPoint().getX());
					upd.setFloat(10, a.getPoint().getY());
					upd.setDate(11, TODAY);
					upd.execute();
				} catch (SQLException ue) {
					LOG.warn("Error inserting {} address {}", code, a.getId(), ue);
				}
			});
		} catch (SQLException ue) {
			LOG.warn("Error deleting addresses", ue);
		}
	}

	/**
	 * Main
	 * 
	 * @param args 
	 */
	public static void main(String args[]) {
		CommandLine cli  = parse(args);
		if (cli == null) {
			System.exit(-1);
		}
		
		String indir = cli.getOptionValue("i");
		String jdbc = cli.getOptionValue("d");
		
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(jdbc);
			conn.setAutoCommit(true);
		} catch (SQLException ex) {
			System.exit(-2);
		}
		
		Path inPath = Paths.get(indir);
		
		for (BestRegion region: BestRegion.values()) {
			if (cli.hasOption(region.getCode())) {
				LOG.info("Region {}", region.getName());
				
				writeCities(conn, region, inPath);
				writeStreets(conn, region, inPath);
				writeAddresses(conn, region, inPath);
			}
		}
	}
}
