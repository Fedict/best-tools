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
package be.bosa.dt.best.converter;

import be.bosa.dt.best.converter.dao.Address;
import be.bosa.dt.best.converter.dao.BestRegion;
import be.bosa.dt.best.converter.dao.Municipality;
import be.bosa.dt.best.converter.dao.Postal;
import be.bosa.dt.best.converter.dao.Streetname;
import be.bosa.dt.best.converter.reader.AddressReader;
import be.bosa.dt.best.converter.reader.MunicipalityReader;
import be.bosa.dt.best.converter.reader.PostalReader;
import be.bosa.dt.best.converter.reader.StreetnameReader;
import be.bosa.dt.best.converter.writer.BestWriterCSV;
import be.bosa.dt.best.converter.writer.BestWriterShape;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
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
 * Small tool to unzip the various BeST zipfiles
 * 
 * @author Bart Hanssens
 */
public class Main {
	private final static Logger LOG = LoggerFactory.getLogger(Main.class);
	
	private final static Options OPTS = new Options()
		.addRequiredOption("i", "indir", true, "input directory")
		.addOption("o", "outdir", true, "output directory")
		.addOption("B", "Brussels", false, "process files for Brussels")
		.addOption("F", "Flanders", false, "process files for Flanders")
		.addOption("W", "Wallonia", false, "process files for Wallonia");
	
	/**
	 * Print help info
	 */
	private static void printHelp() {
		HelpFormatter fmt = new HelpFormatter();
		fmt.printHelp("BeST converters", OPTS);
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

	
	private static void writeRegionCSV(BestRegion region, Path inPath, Path outPath) {	
		try( Stream<Municipality> cities = new MunicipalityReader().read(region, inPath);
			Stream<Postal> postals = new PostalReader().read(region, inPath);
			Stream<Streetname> streets = new StreetnameReader().read(region, inPath);
			Stream<Address> addresses = new AddressReader().read(region, inPath)) {

			BestWriterCSV writer = new BestWriterCSV();
				
			Map<String, String[]> cacheCities = writer.writeMunicipalities(region, outPath, cities);
			Map<String, String[]> cachePostals = writer.writePostals(region, outPath, postals);
			Map<String, String[]> cacheStreets = writer.writeStreets(region, outPath, streets, cacheCities);
			
			writer.writeAddresses(region, outPath, addresses, cacheStreets, cacheCities, cachePostals);
		}
	}
	
	private static void writeRegionShape(BestRegion region, Path inPath, Path outPath) {	
		try( Stream<Municipality> cities = new MunicipalityReader().read(region, inPath);
			Stream<Postal> postals = new PostalReader().read(region, inPath);
			Stream<Streetname> streets = new StreetnameReader().read(region, inPath);
			Stream<Address> addresses = new AddressReader().read(region, inPath)) {

			BestWriterShape writer = new BestWriterShape();
				
			Map<String, String[]> cacheCities = writer.writeMunicipalities(region, outPath, cities);
			Map<String, String[]> cachePostals = writer.writePostals(region, outPath, postals);
			Map<String, String[]> cacheStreets = writer.writeStreets(region, outPath, streets, cacheCities);
			
			writer.writeAddresses(region, outPath, addresses, cacheCities, cacheCities, cachePostals);
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
		String outdir = cli.getOptionValue(indir, indir);
		
		Path inPath = Paths.get(indir);
		Path outPath = Paths.get(outdir);
		
		for (BestRegion region: BestRegion.values()) {
			if (cli.hasOption(region.getCode())) {
				LOG.info("Region {}", region.getName());
				writeRegionCSV(region, inPath, outPath);
				writeRegionShape(region, inPath, outPath);
			}
		}
	}
}
