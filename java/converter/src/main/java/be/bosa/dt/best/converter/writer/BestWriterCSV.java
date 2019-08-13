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
import be.bosa.dt.best.dao.BestObject;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.BestType;
import be.bosa.dt.best.dao.Municipality;
import be.bosa.dt.best.dao.Postal;
import be.bosa.dt.best.dao.Streetname;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;

import org.locationtech.jts.geom.Coordinate;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BeST result CSV file writer
 * 
 * @author Bart Hanssens
 */
public class BestWriterCSV implements BestWriter {
	private static MathTransform TRANS;
	private final static Logger LOG = LoggerFactory.getLogger(BestWriterCSV.class);

	/**
	 * Constructor
	 */
	public BestWriterCSV() {
		try {
			TRANS = CRS.findMathTransform(CRS.decode("EPSG:31370"), CRS.decode("EPSG:4326"), false);
		} catch (FactoryException fe) {
			LOG.error("No conversion found");
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
	 */
	private <T extends BestObject> void write(Path file, String[] header, Stream<T> lines, 
													Function<T,String[]> func) {
		LOG.info("Writing {}", file);
		try (CSVWriter w = new CSVWriter(Files.newBufferedWriter(file))) {
			w.writeNext(header);
			lines.forEach(s -> w.writeNext(func.apply(s)));
		} catch(IOException ioe) {
			LOG.error("Error writing to file", ioe);
		}
	}
	
	@Override
	public Map<String,String[]> writeMunicipalities(BestRegion region, Path outdir, Stream<Municipality> cities) {
		Path file = BestWriter.getPath(outdir, region, BestType.MUNICIPALITIES, "csv");
		
		Map<String,String[]> cache = new HashMap<>();
		
		String[] header = { "namespace", "id", "version", "name_nl", "name_fr", "name_de" };
		Function<Municipality,String[]> func = (Municipality s) -> { 
			cache.put(s.getId(), new String[] { s.getName("nl"), s.getName("fr"), s.getName("de")});
			return new String[] 
				{ s.getNamespace(), s.getId(), s.getVersion(), s.getName("nl"), s.getName("fr"), s.getName("de") };
		};
		
		write(file, header, cities, func);
		
		return cache;
	}

	@Override
	public Map<String,String[]> writePostals(BestRegion region, Path outdir, Stream<Postal> postals) {
		Path file = BestWriter.getPath(outdir, region, BestType.POSTALINFO, "csv");
		
		Map<String,String[]> cache = new HashMap<>();
		
		String[] header = { "namespace", "id", "name_nl", "name_fr", "name_de", "status" };
		Function<Postal,String[]> func = (Postal s) -> { 
			cache.put(s.getId(), new String[] { s.getName("nl"), s.getName("fr"), s.getName("de")});
			return new String[] 
				{ s.getNamespace(), s.getId(), s.getName("nl"), s.getName("fr"), s.getName("de") };
		};
				
		write(file, header, postals, func);
		
		return cache;
	}
		
	@Override
	public Map<String,String[]> writeStreets(BestRegion region, Path outdir, Stream<Streetname> streets,
											Map<String,String[]> cities) {
		Path file = BestWriter.getPath(outdir, region, BestType.STREETNAMES, "csv");
	
		Map<String,String[]> cache = new HashMap<>();
		
		String[] header = { "namespace", "id", "name_nl", "name_fr", "name_de",
							"city_ns", "city_id", "city_nl", "city_fr", "city_de",
							"version", "status", "from" };
		Function<Streetname,String[]> func = (Streetname s) -> { 
			cache.put(s.getId(), new String[] { s.getName("nl"), s.getName("fr"), s.getName("de")});
			String[] cCities = cities.getOrDefault(s.getCity().getId(), new String[2]);
			
			return new String[] 
				{ s.getNamespace(), s.getId(), s.getName("nl"), s.getName("fr"), s.getName("de"),
				s.getCity().getNamespace(), s.getCity().getId(), cCities[0], cCities[1],
				s.getVersion(), s.getStatus(), s.getDate() };
		};
		
		write(file, header, streets, func);
		
		return cache;
	}
	
	@Override
	public void writeAddresses(BestRegion region, Path outdir, Stream<Address> addresses,
			Map<String,String[]> streets, Map<String,String[]> cities, Map<String,String[]> postals) {
		Path file = BestWriter.getPath(outdir, region, BestType.ADDRESSES, "csv");

		String[] header = { "id", 
							"street_id", "street_nl", "street_fr", "street_de",
							"city_id", "city_nl", "city_fr", "city_de",
							"number", "box",
							"postal_id", "postal_nl", "postal_fr",
							"version", "status",
							"lambertx", "lamberty",
							"gpsx", "gpsy" };
		Function<Address,String[]> func = (Address s) -> {
			String[] cCities = cities.getOrDefault(s.getCity().getId(), new String[3]);
			String[] cStreet = streets.getOrDefault(s.getStreet().getId(), new String[3]);
			String[] cPostal = postals.getOrDefault(s.getPostal().getId(), new String[2]);
			
			Coordinate src = new Coordinate(s.getPoint().getX(), s.getPoint().getY());
			Coordinate dest = new Coordinate();
			
			try {
				JTS.transform(src, dest, TRANS);
			} catch (TransformException ex) {
				LOG.warn("Transformation to GPS failed");
			}

			return new String[] 
				{ s.getId(),
				s.getStreet().getId(), cStreet[0], cStreet[1], cStreet[2],
				s.getNumber(), s.getBox(),
				s.getCity().getId(), cCities[0], cCities[1], cCities[2],
				s.getPostal().getId(), cPostal[0], cPostal[1],
				s.getVersion(), s.getStatus(),
				String.valueOf(s.getPoint().getX()), String.valueOf(s.getPoint().getY()),
				String.format(Locale.US, "%.4f", dest.x), String.format(Locale.US, "%.4f", dest.y)
			};
		};
		
		write(file, header, addresses, func);
	}
}
