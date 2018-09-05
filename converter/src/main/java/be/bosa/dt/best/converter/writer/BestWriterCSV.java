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

import be.bosa.dt.best.converter.dao.Address;
import be.bosa.dt.best.converter.dao.BestObject;
import be.bosa.dt.best.converter.dao.BestRegion;
import be.bosa.dt.best.converter.dao.BestType;
import be.bosa.dt.best.converter.dao.Municipality;
import be.bosa.dt.best.converter.dao.Postal;
import be.bosa.dt.best.converter.dao.Streetname;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BeST result CSV file writer
 * 
 * @author Bart Hanssens
 */
public class BestWriterCSV implements BestWriter {
	private final static Logger LOG = LoggerFactory.getLogger(BestWriterCSV.class);
	
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
		
		String[] header = { "namespace", "id", "version", "name_nl", "name_fr" };
		Function<Municipality,String[]> func = (Municipality s) -> { 
			cache.put(s.getIDVersion(), new String[] { s.getName("nl"), s.getName("fr")});
			return new String[] 
				{ s.getNamespace(), s.getId(), s.getVersion(), s.getName("nl"), s.getName("fr") };
		};
		
		write(file, header, cities, func);
		
		return cache;
	}

	/**
	 * Process the input file and return a stream of BeSt objects
	 * 
	 * @param region
	 * @param outdir
	 * @param postals
	 */
	@Override
	public Map<String,String[]> writePostals(BestRegion region, Path outdir, Stream<Postal> postals) {
		Path file = BestWriter.getPath(outdir, region, BestType.POSTALINFO, "csv");
		
		Map<String,String[]> cache = new HashMap<>();
		
		String[] header = { "namespace", "id", "name_nl", "name_fr", "status" };
		Function<Postal,String[]> func = (Postal s) -> { 
			cache.put(s.getIDVersion(), new String[] { s.getName("nl"), s.getName("fr")});
			return new String[] 
				{ s.getNamespace(), s.getId(), s.getName("nl"), s.getName("fr") };
		};
				
		write(file, header, postals, func);
		
		return cache;
	}
	
	
	@Override
	public Map<String,String[]> writeStreets(BestRegion region, Path outdir, Stream<Streetname> streets,
											Map<String,String[]> cities) {
		Path file = BestWriter.getPath(outdir, region, BestType.STREETNAMES, "csv");
	
		Map<String,String[]> cache = new HashMap<>();
		
		String[] header = { "namespace", "id", "name_nl", "name_fr", 
							"city_ns", "city_id", "city_nl", "city_fr",
							"status", "from" };
		Function<Streetname,String[]> func = (Streetname s) -> { 
			cache.put(s.getIDVersion(), new String[] { s.getName("nl"), s.getName("fr")});
			String[] found = cities.getOrDefault(s.getCity().getIDVersion(), new String[2]);
			
			return new String[] 
				{ s.getNamespace(), s.getId(), s.getName("nl"), s.getName("fr"),
				s.getCity().getNamespace(), s.getCity().getId(), found[0], found[1],
				s.getStatus(), s.getDate() };
		};
		
		write(file, header, streets, func);
		
		return cache;
	}
	
	@Override
	public void writeAddresses(BestRegion region, Path outdir, Stream<Address> addresses,
			Map<String,String[]> streets, Map<String,String[]> postals) {
		Path file = BestWriter.getPath(outdir, region, BestType.ADDRESSES, "csv");

		String[] header = { "namespace", "id", "x", "y", 
//								"street_ns", "street_id", "city_ns", "city_id", "number",
							"status" };
		Function<Address,String[]> func = (Address s) -> {

			return new String[] 
				{ s.getNamespace(), s.getId(),
				String.valueOf(s.getPoint().getX()), String.valueOf(s.getPoint().getY()),
//								s.getCity().getNamespace(), s.getCity().getId(),
//								s.getName("nl"), s.getName("fr"), 
				s.getStatus() };
		};
		
		write(file, header, addresses, func);
	}

}
