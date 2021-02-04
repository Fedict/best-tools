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
package be.bosa.dt.best.emptystreets;

import be.bosa.dt.best.converter.writer.BestWriter;
import be.bosa.dt.best.dao.Address;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.Municipality;
import be.bosa.dt.best.dao.Street;
import be.bosa.dt.best.xmlreader.AddressReader;
import be.bosa.dt.best.xmlreader.MunicipalityReader;
import be.bosa.dt.best.xmlreader.StreetnameReader;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
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
 * Create a list of streets without addresses (e.g. pedestrian maps, but could also be empty by mistake)
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
	
	/**
	 * Write a series of Best object to a file
	 *
	 * @param <T>
	 * @param file CSV file to write to
	 * @param header header as array of strings
	 * @param lines stream of lines
	 */
	private static void write(Path file, String[] header, Stream<String[]> lines) {
		LOG.info("Writing {}", file);
		try (CSVWriter w = new CSVWriter(Files.newBufferedWriter(file))) {
			w.writeNext(header);
			lines.forEach(s -> w.writeNext(s));
		} catch (IOException ioe) {
			LOG.error("Error writing to file", ioe);
		}
	}

	/**
	 * Write files for a specific region
	 * 
	 * @param region
	 * @param inPath input directory
	 * @param outPath output directory
	 */
	private static void writeRegion(BestRegion region, Path inPath, Path outPath) {
		Map<String, String[]> cacheCities = new HashMap<>();
		Map<String, String[]> cacheStreets = new HashMap<>();
	
		try (Stream<Municipality> cities = new MunicipalityReader().read(region, inPath);
			Stream<Street> streets = new StreetnameReader().read(region, inPath)) {
				cities.forEach(s -> cacheCities.put(s.getId(), new String[]{ 
									s.getName("nl"), s.getName("fr"), s.getName("de"), 
									s.getNamespace(), s.getId(), s.getVersion()}));
				streets.filter(s -> s.getStatus().equals("current"))
					.filter(s -> s.getTillDate() == null) // also remove Flanders streets with end date
					.forEach(s -> cacheStreets.put(s.getId(), new String[]{ 
									s.getName("nl"), s.getName("fr"), s.getName("de"), 
									s.getNamespace(), s.getId(), s.getVersion(),
									s.getCity().getId()}));
		}

		Map<String,String[]> NL = new HashMap<>();
		Map<String,String[]> FR = new HashMap<>();
		Map<String,String[]> DE = new HashMap<>();
		
		try(Stream<Address> addresses = new AddressReader().read(region, inPath)) {
			// get the street IDs of "current" (active) addresses and remove these streets from the cache
			addresses.filter(a -> a.getStatus().equals("current"))
					.peek(a -> { 		// create lookup tables for postal info based on name
						String[] city = cacheCities.get(a.getCity().getId());
						NL.put(city[0], new String[] { a.getPostal().getId(), city[0], city[1], city[2] });
						FR.put(city[1], new String[] { a.getPostal().getId(), city[0], city[1], city[2] });
						DE.put(city[2], new String[] { a.getPostal().getId(), city[0], city[1], city[2] });
					})
					.map(a -> a.getStreet().getId())
					.forEach(s -> cacheStreets.remove(s));
		}
		NL.remove("");
		FR.remove("");
		DE.remove("");

		// now the map only constains streets without any address
		LOG.info("{} empty streets", cacheStreets.size());

		Path file = BestWriter.getPath(outPath, region, "empty_street", "csv");
		
		// mimic the structure of the postalstreet CSV, even when there is no postal and no city part info
		String[] header = {
			"postal_id", "postal_nl", "postal_fr", "postal_de",
			"street_nl", "street_fr", "street_de",
			"city_nl", "city_fr", "city_de",
			"citypart_nl", "citypart_fr", "citypart_de",
			"street_prefix", "street_no", "street_version",
			"city_prefix", "city_no", "city_version",
			"citypart_prefix", "citypart_no", "citypart_version"
		};

		// order by city
		Stream<String[]> stream = cacheStreets.values().stream()
			.sorted(Comparator.comparing(s -> s[6]))
			.map(s -> {
				String[] c = cacheCities.getOrDefault(s[6], new String[6]);
				String[] p = NL.getOrDefault(c[0], 
								FR.getOrDefault(c[1],
								DE.getOrDefault(c[2], new String[4])));
				return new String[] { 
					p[0], p[1], p[2], p[3],
					s[0], s[1], s[2], 
					c[0], c[1], c[2],
					"", "", "", "",
					s[3], s[4], s[5],
					c[3], c[4], c[5],
					"", "", "", "",
				}; 
			});

		write(file, header, stream);
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
		
		String indir = cli.getOptionValue("i");
		String outdir = cli.getOptionValue(indir, indir);
		
		Path inPath = Paths.get(indir);
		Path outPath = Paths.get(outdir);
		
		for (BestRegion region: BestRegion.values()) {
			if (cli.hasOption(region.getCode())) {
				LOG.info("Region {}", region.getName());
				writeRegion(region, inPath, outPath);
			}
		}
	}
}
