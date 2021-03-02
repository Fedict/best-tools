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
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.geotools.geometry.jts.JTS;

import org.locationtech.jts.geom.Coordinate;

import org.opengis.referencing.operation.TransformException;

/**
 * Write addresses to a legacy format (same output as OSOC19 python tools) served to OpenAddresses.io.
 * Only the CSV with addresses gets written, streets etc are not used by OpenAddresses.io
 *
 * @author Bart Hanssens
 */
public class BestWriterCSVOpenAddresses extends BestWriterCSV {
	private final static Logger LOG = Logger.getLogger(BestWriterCSVOpenAddresses.class.getName());

	@Override
	public Map<String, String[]> writeMunicipalities(BestRegion region, Path outdir, Stream<Municipality> cities) {
		return cities.collect(Collectors.toMap(
			s -> s.getId(), 
			s -> new String[]{s.getName("nl"), s.getName("fr"), s.getName("de"), s.getIDVersion()},
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
		return streets.collect(Collectors.toMap(
			s -> s.getId(), 
			s -> new String[]{s.getName("nl"), s.getName("fr"), s.getName("de"), s.getIDVersion()},
			(s1,s2) -> { return s2; }));
	}

	@Override
	public Map<String, Map<String, String[]>> writeAddresses(BestRegion region, Path outdir, Stream<Address> addresses,
		Map<String, String[]> streets, Map<String, String[]> cities, Map<String, String[]> cityParts,
		Map<String, String[]> postals) {

		Path file = Paths.get(outdir.toString(), "openaddress-be" + region.getAbbr() + ".csv");

		Map<String, Map<String, String[]>> cache = new HashMap<>();
		String[] header = { 
			"EPSG:31370_x","EPSG:31370_y",
			"EPSG:4326_lat","EPSG:4326_lon",
			"address_id","box_number","house_number",
			"municipality_id","municipality_name_de","municipality_name_fr","municipality_name_nl",
			"postcode","postname_fr","postname_nl",
			"street_id","streetname_de","streetname_fr","streetname_nl",
			"region_code","status"
		};
	
		Function<Address, String[]> func = (Address s) -> {
			String[] cCities = cities.getOrDefault(s.getCity().getId(), new String[3]);
			String[] cStreet = streets.getOrDefault(s.getStreet().getId(), new String[3]);
			String[] cPostal = postals.getOrDefault(s.getPostal().getId(), new String[3]);

			Coordinate src = new Coordinate(s.getPoint().getX(), s.getPoint().getY());
			Coordinate dest = new Coordinate();

			try {
				JTS.transform(src, dest, TRANS);
			} catch (TransformException ex) {
				LOG.warning("Transformation to GPS failed");
			}

			return new String[]{
				String.format(Locale.US, "%.4f", dest.x), String.format(Locale.US, "%.4f", dest.y),
				String.valueOf(s.getPoint().getX()), String.valueOf(s.getPoint().getY()),
				s.getId(), s.getBox(), s.getNumber(),
				s.getPostal().getId(), cPostal[1], cPostal[0],
				s.getCity().getId(), cCities[2], cCities[1], cCities[1],						
				s.getStreet().getId(), cStreet[2], cStreet[1], cStreet[0],
				String.format("BE-%s", region.getAbbr().toUpperCase()), s.getStatus()
			};
		};

		write(file, header, addresses, func, false);
		
		return cache;
	}
	
	@Override
	public void writePostalStreets(BestRegion region, Path outdir, Map<String, Map<String, String[]>> cache) {
		//
	}
}
