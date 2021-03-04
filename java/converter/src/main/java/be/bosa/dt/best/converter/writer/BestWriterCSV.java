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
package be.bosa.dt.best.converter.writer;

import be.bosa.dt.best.dao.Address;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.BestType;
import be.bosa.dt.best.dao.Municipality;
import be.bosa.dt.best.dao.Postal;
import be.bosa.dt.best.dao.Street;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;

import org.locationtech.jts.geom.Coordinate;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Write BeST data to a series of CSV files.
 *
 * @author Bart Hanssens
 */
public class BestWriterCSV implements BestWriter {
	protected static MathTransform TRANS;
	private final static Logger LOG = Logger.getLogger(BestWriterCSV.class.getName());

	/**
	 * Constructor
	 */
	public BestWriterCSV() {
		try {
			TRANS = CRS.findMathTransform(CRS.decode("EPSG:31370"), CRS.decode("EPSG:4326"), false);
		} catch (FactoryException fe) {
			LOG.severe("No conversion found");
		}
	}

	/**
	 * Write a series of Best object to a file
	 *
	 * @param <T>
	 * @param file CSV file to write to
	 * @param header header as array of strings
	 * @param lines stream of lines
	 * @param func function to create a row in the CSV
	 * @param quotes
	 */
	protected <T> void write(Path file, String[] header, Stream<T> lines, Function<T, String[]> func, boolean quotes) {
		LOG.log(Level.INFO, "Writing {0}", file);
		try (CSVWriter w = new CSVWriter(Files.newBufferedWriter(file))) {
			w.writeNext(header, quotes);
			lines.forEach(s -> w.writeNext(func.apply(s), quotes));
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, "Error writing to file", ioe);
		}
	}

	/**
	 * Write municipalities (or parts of municipalities) to a file
	 *
	 * @param file output file
	 * @param cities municipalities or parts of municipalities
	 * @return cache
	 */
	private Map<String, String[]> writeMunicipalityOrParts(Path file, Stream<Municipality> cities) {
		Map<String, String[]> cache = new HashMap<>();

		String[] header = {"id", "version", "name_nl", "name_fr", "name_de"};
		Function<Municipality, String[]> func = (Municipality s) -> {
			cache.put(s.getId(), new String[]{s.getName("nl"), s.getName("fr"), s.getName("de"), s.getIDVersion()});
			return new String[]{s.getIDVersion(), s.getName("nl"), s.getName("fr"), s.getName("de")};
		};

		write(file, header, cities, func, true);

		return cache;
	}

	@Override
	public Map<String, String[]> writeMunicipalities(BestRegion region, Path outdir, Stream<Municipality> cities) {
		Path file = BestWriter.getPath(outdir, region, BestType.MUNICIPALITIES, "csv");
		return writeMunicipalityOrParts(file, cities);
	}

	@Override
	public Map<String, String[]> writeMunicipalityParts(BestRegion region, Path outdir, Stream<Municipality> cityParts) {
		Path file = BestWriter.getPath(outdir, region, BestType.MUNICIPALITY_PARTS, "csv");
		return writeMunicipalityOrParts(file, cityParts);
	}

	@Override
	public Map<String, String[]> writePostals(BestRegion region, Path outdir, Stream<Postal> postals) {
		Path file = BestWriter.getPath(outdir, region, BestType.POSTALINFO, "csv");

		Map<String, String[]> cache = new HashMap<>();

		String[] header = {"id", "name_nl", "name_fr", "name_de", "status"};
		Function<Postal, String[]> func = (Postal s) -> {
			cache.put(s.getId(), new String[]{s.getName("nl"), s.getName("fr"), s.getName("de")});
			return new String[]{s.getId(), s.getName("nl"), s.getName("fr"), s.getName("de")};
		};

		write(file, header, postals, func, true);

		return cache;
	}

	@Override
	public Map<String, String[]> writeStreets(BestRegion region, Path outdir, Stream<Street> streets,
		Map<String, String[]> cities) {
		Path file = BestWriter.getPath(outdir, region, BestType.STREETNAMES, "csv");

		Map<String, String[]> cache = new HashMap<>();

		String[] header = {"id", "name_nl", "name_fr", "name_de",
			"city_id", "city_nl", "city_fr", "city_de",
			"version", "status", "from"};

		Function<Street, String[]> func = (Street s) -> {
			cache.put(s.getId(), new String[]{s.getName("nl"), s.getName("fr"), s.getName("de"), s.getIDVersion()});
			String[] cCities = cities.getOrDefault(s.getCity().getId(), new String[3]);

			return new String[]{s.getIDVersion(), s.getName("nl"), s.getName("fr"), s.getName("de"),
				s.getCity().getIDVersion(), cCities[0], cCities[1], cCities[2],
				s.getVersion(), s.getStatus(), s.getFromDate()};
		};

		write(file, header, streets, func, true);

		return cache;
	}

	@Override
	public Map<String, Map<String, String[]>> writeAddresses(BestRegion region, Path outdir, Stream<Address> addresses,
		Map<String, String[]> streets, Map<String, String[]> cities, Map<String, String[]> cityParts,
		Map<String, String[]> postals) {

		Path file = BestWriter.getPath(outdir, region, BestType.ADDRESSES, "csv");

		Map<String, Map<String, String[]>> cache = new HashMap<>();

		String[] header = {"id",
			"street_id", "street_nl", "street_fr", "street_de",
			"number", "box",
			"city_id", "city_nl", "city_fr", "city_de",
			"citypart_id", "citypart_nl", "citypart_fr", "citypart_de",
			"postal_id", "postal_nl", "postal_fr", "postal_de",
			"lambertx", "lamberty",
			"gpsx", "gpsy",
			"status"};
		Function<Address, String[]> func = (Address s) -> {
			String[] cCities = cities.getOrDefault(s.getCity().getId(), new String[3]);
			String[] cParts = cityParts.getOrDefault(s.getCityPart().getId(), new String[3]);
			String[] cStreet = streets.getOrDefault(s.getStreet().getId(), new String[3]);
			String[] cPostal = postals.getOrDefault(s.getPostal().getId(), new String[3]);

			Coordinate src = new Coordinate(s.getPoint().getX(), s.getPoint().getY());
			Coordinate dest = new Coordinate();

			try {
				JTS.transform(src, dest, TRANS);
			} catch (TransformException ex) {
				LOG.warning("Transformation to GPS failed");
			}

			// A street can have different postal codes, so create a cache of info per street per postal code
			if (s.getStatus().equals("current")) {
				Map<String, String[]> postalStreet = cache.getOrDefault(s.getPostal().getId(), new HashMap<>());
				postalStreet.put(s.getStreet().getId(), new String[]{
					s.getPostal().getId(), cPostal[0], cPostal[1], cPostal[2],
					cStreet[0], cStreet[1], cStreet[2],
					cCities[0], cCities[1], cCities[2],
					cParts[0], cParts[1], cParts[2],
					s.getStreet().getNamespace(), s.getStreet().getId(), s.getStreet().getVersion(),
					s.getCity().getNamespace(), s.getCity().getId(), s.getCity().getVersion(),
					s.getCityPart().getNamespace(), s.getCityPart().getId(), s.getCityPart().getVersion()
				});
				cache.put(s.getPostal().getId(), postalStreet);
			}
			return new String[]{s.getIDVersion(),
				s.getStreet().getIDVersion(), cStreet[0], cStreet[1], cStreet[2],
				s.getNumber(), s.getBox(),
				s.getCity().getIDVersion(), cCities[0], cCities[1], cCities[2],
				s.getCityPart().getIDVersion(), cParts[0], cParts[1], cParts[2],
				s.getPostal().getId(), cPostal[0], cPostal[1], cPostal[2],
				String.valueOf(s.getPoint().getX()), String.valueOf(s.getPoint().getY()),
				String.format(Locale.US, "%.4f", dest.x), String.format(Locale.US, "%.4f", dest.y),
				s.getStatus()
			};
		};

		write(file, header, addresses, func, true);

		return cache;
	}
	
	@Override
	public void writePostalStreets(BestRegion region, Path outdir, Map<String, Map<String, String[]>> cache) {
		Path file = BestWriter.getPath(outdir, region, "postal_street", "csv");

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
