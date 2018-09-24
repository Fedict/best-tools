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
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToCreateStatementException;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.core.statement.Update;

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
	 * @param jdbi
	 * @param region
	 * @param inPath 
	 */
	private static void writeCities(Jdbi jdbi, BestRegion region, Path inPath) {
		String code = region.getCode();
		String DEL = "DELETE FROM best.cities WHERE region = ?";
		String INS = 
			"INSERT INTO best.cities VALUES(:id, :region, :nl, :fr) "
			+ "ON CONFLICT(id) DO "
			+ "UPDATE SET region = EXCLUDED.region, "
			+ " name_nl = EXCLUDED.name_nl, "
			+ " name_fr = EXCLUDED.name_fr";
		
		try(Handle handle = jdbi.open();
			Stream<Municipality> cities = new MunicipalityReader().read(region, inPath)) {
			
			handle.execute(DEL, code);
			Update upd = handle.createUpdate(INS);
			cities.forEach(c -> {
					upd.bind("id", c.getId())
						.bind("region", code)
						.bind("nl", c.getName("nl"))
						.bind("fr", c.getName("fr")).execute();
			});
		} catch (UnableToExecuteStatementException ue) {
			LOG.warn("Error inserting city", ue);
		}
	}

	/**
	 * Write data to streets table
	 * 
	 * @param jdbi
	 * @param region
	 * @param inPath 
	 */
	private static void writeStreets(Jdbi jdbi, BestRegion region, Path inPath) {
		String code = region.getCode();
		String DEL = "DELETE FROM best.streets WHERE region = ?";
		String INS = 
			"INSERT INTO best.streets VALUES(:id, :region, :nl, :fr, :city_id) "
			+ "ON CONFLICT(id) DO "
			+ "UPDATE SET region = EXCLUDED.region, "
			+ " name_nl = EXCLUDED.name_nl, "
			+ " name_fr = EXCLUDED.name_fr";
		
		try(Handle handle = jdbi.open();
			Stream<Streetname> streets = new StreetnameReader().read(region, inPath)) {
			
			handle.execute(DEL, code);
			Update upd = handle.createUpdate(INS);
			streets.forEach(s -> {
				try {
					upd.bind("id", s.getId())
						.bind("region", code)
						.bind("nl", s.getName("nl"))
						.bind("fr", s.getName("fr"))
						.bind("city_id", s.getCity().getId()).execute();
				} catch (UnableToExecuteStatementException|UnableToCreateStatementException ue) {
					LOG.warn("Error inserting {} street {}", code, s.getId());
				}
			});
		} catch (UnableToExecuteStatementException ue) {
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
	private static void writeAddresses(Jdbi jdbi, BestRegion region, Path inPath) {
		String code = region.getCode();
		
		String DEL = "DELETE FROM best.addresses WHERE region = ?";
		String INS = 
			"INSERT INTO addresses VALUES(:id, :region, :streetno, :boxno, "
			+ " :street_id, :city_id, :x, :y, "
			+ "ST_Transform(ST_SetSRID(ST_MakePoint(:x2, :y2), 31370), 4326)) "
			+ " ON CONFLICT(id) DO "
			+ "UPDATE SET region = EXCLUDED.region, "
			+ " streetno = EXCLUDED.streetno, "
			+ " boxno = EXCLUDED.boxno, "
			+ " street_id = EXCLUDED.street_id, "
			+ " city_id = EXCLUDED.city_id,"
			+ " x = EXCLUDED.x,"
			+ " y = EXCLUDED.y,"
			+ " geom = ST_Transform(ST_SetSRID(ST_MakePoint(EXCLUDED.x2, EXCLUDED.y2), 31370), 4326))";
		
		try(Handle handle = jdbi.open();
			Stream<Address> addresses = new AddressReader().read(region, inPath)) {
			
			handle.execute(DEL, code);
			Update upd = handle.createUpdate(INS);
			addresses.forEach(a -> {
				try {
					upd.bind("id", a.getId())
						.bind("region", code)
						.bind("streetno", a.getNumber())
						.bind("boxno", a.getBox())
						.bind("street_id", a.getStreet().getId())
						.bind("city_id", a.getCity().getId())
						.bind("x", a.getPoint().getX())
						.bind("y", a.getPoint().getY())
						.bind("x2", a.getPoint().getX())
						.bind("y2", a.getPoint().getY())
						.execute();
				} catch (UnableToExecuteStatementException ue) {
					LOG.warn("Error inserting address {}", a.getId());
				}
			});
		} catch (UnableToExecuteStatementException ue) {
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
		
		Jdbi jdbi = Jdbi.create(jdbc);		
		Path inPath = Paths.get(indir);
		
		for (BestRegion region: BestRegion.values()) {
			if (cli.hasOption(region.getCode())) {
				LOG.info("Region {}", region.getName());
				writeCities(jdbi, region, inPath);
				writeStreets(jdbi, region, inPath);
				writeAddresses(jdbi, region, inPath);
			}
		}
	}
}
