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
package be.bosa.dt.best.converter.writer;

import be.bosa.dt.best.dao.Address;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.Municipality;
import be.bosa.dt.best.dao.Postal;
import be.bosa.dt.best.dao.Street;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Write the list of streets without any address / housenumber (could be a parc, rural road...)
 *
 * @author Bart Hanssens
 */
public class BestWriterCSVEmptyStreets extends BestWriterCSV {
	private final static Logger LOG = Logger.getLogger(BestWriterCSVEmptyStreets.class.getName());

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
		LOG.info("Getting addr " + a.getId());
		LOG.info("Getting city " + a.getCity().getId());
		
		String[] city = cacheCities.get(a.getCity().getId());
		String[] values = new String[] { a.getPostal().getId(), city[0], city[1], city[2] };
		
		fillPostal(postalNL, city[0], values);
		fillPostal(postalFR, city[1], values);
		fillPostal(postalDE, city[2], values);
	}

	@Override
	public Map<String, String[]> writeMunicipalities(BestRegion region, Path outdir, Stream<Municipality> cities) {
		return cities.collect(Collectors.toMap(
			s -> s.getId(), 
			s -> new String[]{	s.getName("nl"), s.getName("fr"), s.getName("de"), 
								s.getNamespace(), s.getId(), s.getVersion()},
			(s1,s2) -> { return s2; }));
	}

	@Override
	public Map<String, String[]> writeMunicipalityParts(BestRegion region, Path outdir, Stream<Municipality> cityParts) {
		return Collections.EMPTY_MAP;
	}

	@Override
	public Map<String, String[]> writePostals(BestRegion region, Path outdir, Stream<Postal> postals) {
		return postals.collect(Collectors.toMap(
			s -> s.getId(), 
			s -> new String[]{s.getName("nl"), s.getName("fr"), s.getName("de")},
			(s1,s2) -> { return s2; }));
	}

	@Override
	public Map<String, String[]> writeStreets(BestRegion region, Path outdir, Stream<Street> streets,
			Map<String, String[]> cities) {
	
		return streets
			.filter(s -> s.getStatus().equals("current") && s.getTillDate() == null) // remove streets with end date, even if status is current 
			.collect(Collectors.toMap(
				s -> s.getId(), 
				s -> new String[]{ s.getName("nl"), s.getName("fr"), s.getName("de"), 
									s.getNamespace(), s.getId(), s.getVersion(), s.getCity().getId()},
				(s1,s2) -> { return s2; }));
	}

	@Override
	public Map<String, Map<String, String[]>> writeAddresses(BestRegion region, Path outdir, Stream<Address> addresses,
		Map<String, String[]> streets, Map<String, String[]> cities, Map<String, String[]> cityParts,
		Map<String, String[]> postals) {

		Map<String,String[]> NL = new HashMap<>();
		Map<String,String[]> FR = new HashMap<>();
		Map<String,String[]> DE = new HashMap<>();

		Map<String, String[]> emptyStreets = new HashMap<>(streets);
		Map<String, Map<String, String[]>> cache = new HashMap<>();

		LOG.log(Level.INFO, "Streets {0}, cities {1}, postals {2}", 
			new Object[] { streets.size(), cities.size(), postals.size() });
	
		// remove streets that have at least one address
		addresses.filter(a -> (a.getStatus().equals("current") && a.getTillDate() == null) || 
											a.getStatus().equals("reserved"))
					.peek(a -> fillPostals(a, cities, NL, FR, DE))
					.map(a -> a.getStreet().getId())
					.forEach(s -> emptyStreets.remove(s));

		emptyStreets.forEach((k,s) -> {
			String[] c = cities.getOrDefault(s[6], new String[6]);
			// "guess" postal code based on NL/FR/DE name of the municipality
			String[] p = NL.getOrDefault(c[0],
						FR.getOrDefault(c[1],
							DE.getOrDefault(c[2], new String[4])));

			// mimic structure of the other postalstreets file (streets with an address)
			Map<String, String[]> postalStreet = cache.getOrDefault(p[0], new HashMap<>());
			postalStreet.put(s[4], new String[]{ 
				p[0], p[1], p[2], p[3],
				s[0], s[1], s[2], 
				c[0], c[1], c[2],
				"", "", "",
				s[3], s[4], s[5],
				c[3], c[4], c[5],
				"", "", "",
			});
			cache.put(p[0], postalStreet);
		});

		return cache;
	}
	
	@Override
	public void writePostalStreets(BestRegion region, Path outdir, Map<String, Map<String, String[]>> cache) {
		LOG.log(Level.INFO, "{0} empty streets", cache.size());
		Path file = BestWriter.getPath(outdir, region, "empty_street", "csv");
		
		String[] header = {
			"postal_id", "postal_nl", "postal_fr", "postal_de",
			"street_nl", "street_fr", "street_de",
			"city_nl", "city_fr", "city_de",
			"citypart_nl", "citypart_fr", "citypart_de",
			"street_prefix", "street_no", "street_version",
			"city_prefix", "city_no", "city_version",
			"citypart_prefix", "citypart_no", "citypart_version"
		};

		Stream<String[]> stream = cache.values().stream().flatMap(k -> k.values().stream());

		write(file, header, stream, Function.identity(), true);
	}
}
