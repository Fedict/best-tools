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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;



/**
 * Create a list of streets without addresses (e.g. pedestrian maps, but could also be empty by mistake)
 * 
 * @author Bart Hanssens
 */
public class EmptyStreetWriter {
	private final static Logger LOG = Logger.getLogger(EmptyStreetWriter.class.getName());
	
	/**
	 * Write a series of Best object to a file
	 *
	 * @param <T>
	 * @param file CSV file to write to
	 * @param header header as array of strings
	 * @param lines stream of lines
	 */
	private void write(Path file, String[] header, Stream<String[]> lines) {
		LOG.log(Level.INFO, "Writing {0}", file);
		try (CSVWriter w = new CSVWriter(Files.newBufferedWriter(file))) {
			w.writeNext(header);
			lines.forEach(s -> w.writeNext(s));
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, "Error writing to file", ioe);
		}
	}


	/**
	 * Fill postal map.
	 * If the postal code is already in the map, the replace the values if the new postal code is lower than
	 * the current postal code (since the lowest postal code often refers to the main city)
	 * E.g. 2060 Antwerpen will be replaced by 2000 Antwerpen
	 * 
	 * @param postal
	 * @param code key
	 * @param values values
	 */
	private void fillPostal(Map<String, String[]> postal, String key, String[] values) {
		if (!key.isEmpty()) {
			String[] current = postal.get(key);
			if (current == null || (current[0].compareTo(values[0]) > 0)) {
				postal.put(key, values);
			}
		}
	}

	/**
	 * Fill lookup maps for postal info.
	 * This zip code is only available via an address (i.e. with house number), not on a street level.
	 * So use addresses to collect city/postal names and postal codes, and use the city names to guess the
	 * postal code when a street has no addresses attached to it.
	 * This will not work 100% correct, because a city can have multiple postal codes
	 * 
	 * @param a address
	 * @param cacheCities cities
	 * @param postalNL map with Duth city name as key
	 * @param postalFR map with French city name as key
	 * @param postalDE map with German city name as key
	 */
	private void fillPostals(Address a, Map<String, String[]> cacheCities,
							Map<String, String[]> postalNL, Map<String, String[]> postalFR, 
							Map<String, String[]> postalDE) {
		String[] city = cacheCities.get(a.getCity().getId());
		String[] values = new String[] { a.getPostal().getId(), city[0], city[1], city[2] };
		
		fillPostal(postalNL, city[0], values);
		fillPostal(postalFR, city[1], values);
		fillPostal(postalDE, city[2], values);
	}

	/**
	 * Write files for a specific region
	 * 
	 * @param region
	 * @param inPath input directory
	 * @param outPath output directory
	 */
	public void writeRegion(BestRegion region, Path inPath, Path outPath) {
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
					.peek(a -> fillPostals(a, cacheCities, NL, FR, DE))
					.map(a -> a.getStreet().getId())
					.forEach(s -> cacheStreets.remove(s));
		}

		// now the map only constains streets without any address
		LOG.log(Level.INFO, "{0} empty streets", cacheStreets.size());

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
}