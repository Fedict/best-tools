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


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Loads XML BeST data into a GIS-enabled RDBMS, currently only PostGIS and SpatiaLite
 * Requires (flat) directory of unzipped BeST XML files
 * 
 * @author Bart Hanssens
 */
public class Main {
	private static final Logger LOG = Logger.getLogger(Main.class.getName());

	private final static Options OPTS = new Options()
		.addRequiredOption("x", "xmldir", true, "directory with unzipped BeST XML files")
		.addOption("c", "csv", true, "CSV output directory")
		.addOption("d", "db", true, "JDBC connection string (postgis, h2gis, spatialite)")
		.addOption("g", "gps", false, "store coordinates as WGS84/GPS instead of Lambert72");

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
		String csvstr = cli.getOptionValue("c");
		boolean gps = cli.hasOption("g");
		
		Path xmlPath = Paths.get(xmldir);
		if (!xmlPath.toFile().exists()) {
			LOG.severe("BEST directory does not exist");
			System.exit(-2);
		}
		
		// load to database using JDBC _or_ write to CSV (so tables can be read later)
		if (dbstr != null) {
			DbLoader loader = null;
			if (dbstr.contains("postg") || dbstr.contains("pgsql")) {
				loader = new PostGisLoader(dbstr);
			} else {
				LOG.severe("Database type not supported");
				System.exit(-3);
			}
		
			try {
				loader.loadData(xmlPath, gps);
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "Failed", e);
				System.exit(-4);
			}
		} else if (csvstr != null) {
			CsvPreparer w = new CsvPreparer();
	
			Path p = Paths.get(csvstr);
			if (!p.toFile().exists() && !p.toFile().mkdirs()) {
				LOG.severe("Output directory does not exist and could not be created");
				System.exit(-2);
			}

			try {
				w.write(xmlPath, p, gps);
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "Failed", e);
				System.exit(-4);
			}
		}

		LOG.info("Done");
	}
}
