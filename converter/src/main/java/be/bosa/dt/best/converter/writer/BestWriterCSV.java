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
import be.bosa.dt.best.converter.dao.BestRegion;
import be.bosa.dt.best.converter.dao.BestType;
import be.bosa.dt.best.converter.dao.Municipality;
import be.bosa.dt.best.converter.dao.Postal;
import be.bosa.dt.best.converter.dao.Streetname;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BeST result file writer interface
 * 
 * @author Bart Hanssens
 */
public class BestWriterCSV implements BestWriter {
	private final static Logger LOG = LoggerFactory.getLogger(BestWriterCSV.class);
	
	@Override
	public void writeMunicipalities(BestRegion region, Path outdir, Stream<Municipality> cities) {
		String name = BestType.MUNICIPALITIES.toString().toLowerCase();
		Path p = BestWriter.getPath(outdir, region, name, "csv");
		LOG.info("Writing {}", p);
			
		try (CSVWriter w = new CSVWriter(Files.newBufferedWriter(p))) {
			String[] header = { "namespace", "id", "name_nl", "name_fr" };
			w.writeNext(header);
			cities.forEach(s -> {
				String[] line = { s.getNamespace(), s.getId(), s.getName("nl"), s.getName("fr") };
				w.writeNext(line);
			});
		} catch(IOException ioe) {
			LOG.error("Error writing to file", ioe);
		}
	}

	/**
	 * Process the input file and return a stream of BeSt objects
	 * 
	 * @param region
	 * @param outdir
	 * @param postals
	 */
	@Override
	public void writePostals(BestRegion region, Path outdir, Stream<Postal> postals) {
		String name = BestType.POSTALINFO.toString().toLowerCase();
		Path p = BestWriter.getPath(outdir, region, name, "csv");
		LOG.info("Writing {}", p);
			
		try (CSVWriter w = new CSVWriter(Files.newBufferedWriter(p))) {
			String[] header = { "namespace", "id", "name_nl", "name_fr", "status" };
			w.writeNext(header);
			postals.forEach(s -> {
				String[] line = { s.getNamespace(), s.getId(), 
									s.getName("nl"), s.getName("fr") };
				w.writeNext(line);
			});
		} catch(IOException ioe) {
			LOG.error("Error writing to file", ioe);
		}
	}
	
	/**
	 * Process the input file and return a stream of BeSt objects
	 * 
	 * @param region
	 * @param outdir
	 * @param streets
	 */
	@Override
	public void writeStreets(BestRegion region, Path outdir, Stream<Streetname> streets) {
		String name = BestType.STREETNAMES.toString().toLowerCase();
		Path p = BestWriter.getPath(outdir, region, name, "csv");
		LOG.info("Writing {}", p);
			
		try (CSVWriter w = new CSVWriter(Files.newBufferedWriter(p))) {
			String[] header = { "namespace", "id", "assigned_ns", "assigned_id", 
								"name_nl", "name_fr", "status" };
			w.writeNext(header);
			streets.forEach(s -> {
				String[] line = { s.getNamespace(), s.getId(), 
								s.getCity().getNamespace(), s.getCity().getId(),
								s.getName("nl"), s.getName("fr"), s.getStatus() };
				w.writeNext(line);
			});
		} catch(IOException ioe) {
			LOG.error("Error writing to file", ioe);
		}
	}
	
	/**
	 * 
	 * @param region
	 * @param outdir
	 * @param addresses 
	 */
	public void writeAddresses(BestRegion region, Path outdir, Stream<Address> addresses) {
		String name = BestType.ADDRESSES.toString().toLowerCase();
		Path p = BestWriter.getPath(outdir, region, name, "csv");
		LOG.info("Writing {}", p);
		
		try (CSVWriter w = new CSVWriter(Files.newBufferedWriter(p))) {
			String[] header = { "namespace", "id", "x", "y", 
//								"street_ns", "street_id", "city_ns", "city_id", "number",
								"status" };
			w.writeNext(header);
			addresses.forEach(s -> {
				String[] line = { s.getNamespace(), s.getId(),
								String.valueOf(s.getPoint().getX()), 
								String.valueOf(s.getPoint().getY()),
//								s.getCity().getNamespace(), s.getCity().getId(),
//								s.getName("nl"), s.getName("fr"), 
								s.getStatus() };
				w.writeNext(line);
			});
		} catch(IOException ioe) {
			LOG.error("Error writing to file", ioe);
		}
	}

}
