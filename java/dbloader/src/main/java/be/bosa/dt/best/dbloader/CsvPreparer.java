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

import be.bosa.dt.best.dao.Address;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.Geopoint;
import be.bosa.dt.best.dao.Municipality;
import be.bosa.dt.best.dao.Postal;
import be.bosa.dt.best.dao.Street;
import be.bosa.dt.best.xmlreader.AddressReader;
import be.bosa.dt.best.xmlreader.MunicipalityPartReader;
import be.bosa.dt.best.xmlreader.MunicipalityReader;
import be.bosa.dt.best.xmlreader.PostalReader;
import be.bosa.dt.best.xmlreader.StreetnameReader;

import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.CsvWriter.CsvWriterBuilder;
import de.siegmar.fastcsv.writer.LineDelimiter;
import de.siegmar.fastcsv.writer.QuoteStrategy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;


/**
 * Writes files into CSV suitable for Postgresql "COPY" fast import
 * 
 * @author Bart Hanssens
 */
public class CsvPreparer {
	private static final Logger LOG = Logger.getLogger(CsvPreparer.class.getName());
	
	private final CsvWriterBuilder builder;
	private final GeoCoder geoCoder;
	
	/**
	 * Write postal code info
	 * 
	 * @param xmlPath XML BeST directory
	 * @param csvPath CSV output directory
	 * @throws IOException 
	 */
	private void writePostals(Path xmlPath, Path csvPath) throws IOException {
		try(CsvWriter w = builder.build(csvPath.resolve("postals.csv"), StandardCharsets.UTF_8)) {
			for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
				LOG.log(Level.INFO, "Starting postals {0}", reg.getName());
				int cnt = 0;
				
				PostalReader reader = new PostalReader();
				Stream<Postal> postals = reader.read(reg, xmlPath);
				Iterator<Postal> iter = postals.iterator();

				while (iter.hasNext()) {
					Postal a = iter.next();
					w.writeRow(NsConverter.postalEncode(a.getIDVersion()), "1", a.getId(),
								a.getName("nl"), a.getName("fr"), a.getName("de"));
					if (++cnt % 10_000 == 0) {
						LOG.log(Level.INFO, "Wrote {0}", cnt);
					}
				}
				LOG.log(Level.INFO, "Wrote {0}", cnt);
			}
		}
	}

	/**
	 * Write municipality info
	 * 
	 * @param xmlPath XML BeST directory
	 * @param csvPath CSV output directory
	 * @throws IOException 
	 */
	private void writeMunicipalities(Path xmlPath, Path csvPath) throws IOException {
		try(CsvWriter w = builder.build(csvPath.resolve("municipalities.csv"), StandardCharsets.UTF_8)) {
			for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
				LOG.log(Level.INFO, "Starting municipalities {0}", reg.getName());
				int cnt = 0;

				MunicipalityReader reader = new MunicipalityReader();
				Stream<Municipality> municipalities = reader.read(reg, xmlPath);
				Iterator<Municipality> iter = municipalities.iterator();

				while (iter.hasNext()) {
					Municipality a = iter.next();
					w.writeRow( NsConverter.municipalityEncode(a.getIDVersion()), "1", a.getId(), 
								a.getName("nl"), a.getName("fr"), a.getName("de"));
					if (++cnt % 10_000 == 0) {
						LOG.log(Level.INFO, "Wrote {0}", cnt);
					}
				}
				LOG.log(Level.INFO, "Wrote {0}", cnt);
			}
		}
	}

	/**
	 * Write municipality part info
	 * 
	 * @param xmlPath XML BeST directory
	 * @param csvPath CSV output directory
	 * @throws IOException 
	 */
	private void writeMunicipalityParts(Path xmlPath, Path csvPath) throws IOException {
		try(CsvWriter w = builder.build(csvPath.resolve("municipalityparts.csv"), StandardCharsets.UTF_8)) {
			for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
				LOG.log(Level.INFO, "Starting municipality parts {0}", reg.getName());
				int cnt = 0;

				MunicipalityPartReader reader = new MunicipalityPartReader();
				Stream<Municipality> parts = reader.read(reg, xmlPath);
				Iterator<Municipality> iter = parts.iterator();

				while (iter.hasNext()) {
					Municipality a = iter.next();
					w.writeRow(NsConverter.municipalityPartEncode(a.getIDVersion()), "1", 
								a.getName("nl"), a.getName("fr"), a.getName("de"));
					if (++cnt % 10_000 == 0) {
						LOG.log(Level.INFO, "Wrote {0}", cnt);
					}
				}

				LOG.log(Level.INFO, "Wrote {0}", cnt);
			}
		}
	}

	/**
	 * Load streets
	 * 
	 * @param xmlPath XML BeST directory
	 * @param csvPath CSV output directory
	 * @throws IOException 
	 */
	private void writeStreets(Path xmlPath, Path csvPath) throws IOException {
		try(CsvWriter w = builder.build(csvPath.resolve("streets.csv"), StandardCharsets.UTF_8)) {
			for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
				LOG.log(Level.INFO, "Starting streets {0} ", reg.getName());
				int cnt = 0;

				StreetnameReader reader = new StreetnameReader();
				Stream<Street> streets = reader.read(reg, xmlPath);
				Iterator<Street> iter = streets.iterator();

				while (iter.hasNext()) {
					Street a = iter.next();
					w.writeRow(NsConverter.streetEncode(a.getIDVersion()),
								"1",
								NsConverter.municipalityEncode(a.getCity().getIDVersion()), 
								"1",
								a.getName("nl"), a.getName("fr"), a.getName("de"), 
								"", a.getStreetType(), a.getStatus(),
								(a.getFromDate() != null) ? a.getFromDate().toString() : "", 
								(a.getTillDate() != null) ? a.getTillDate().toString() : "", 
								(a.getBeginLife() != null) ? a.getBeginLife().toString() : "",
								(a.getEndLife() != null) ? a.getEndLife().toString() : "");
				
					if (++cnt % 10_000 == 0) {
						LOG.log(Level.INFO, "Wrote {0}", cnt);
					}
				}
				LOG.log(Level.INFO, "Wrote {0}", cnt);
			}
		}
	}

	/**
	 * Write addresses
	 * 
	 * @param xmlPath XML BeST directory
	 * @param csvPath CSV output directory
	 * @throws IOException 
	 */
	private void writeAddresses(Path xmlPath, Path csvPath, boolean gps) throws IOException {
		try(CsvWriter w = builder.build(csvPath.resolve("addresses.csv"), StandardCharsets.UTF_8)) {
			for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
				LOG.log(Level.INFO, "Starting addresses {0}", reg.getName());
				int cnt = 0;

				AddressReader reader = new AddressReader();
				Stream<Address> addresses = reader.read(reg, xmlPath);
				Iterator<Address> iter = addresses.iterator();

				while (iter.hasNext()) {
					Address a = iter.next();

					Geopoint p = a.getPoint();
					
					// if GPS flag is set, write the WKB as GPS but x,y as Lambert72 (and vice versa)
					Coordinate coordl72 = geoCoder.toCoords(p.getX(), p.getY(), false);
					Coordinate coordgps = geoCoder.toCoords(p.getX(), p.getY(), true);
					
					String x = String.valueOf(gps ? coordl72.x : coordgps.x);
					String y = String.valueOf(gps ? coordl72.y : coordgps.y);
					String c = geoCoder.toWkb(gps ? coordgps : coordl72);

					// calculate geom afterwards, using separate UPDATE statement
					w.writeRow(NsConverter.addressEncode(a.getIDVersion()), 
						"1",
						NsConverter.municipalityEncode(a.getCity().getIDVersion()),
						"1",
						NsConverter.municipalityPartEncode(a.getCityPart().getIDVersion()),
						"1",
						NsConverter.streetEncode(a.getStreet().getIDVersion()),
						"1",
						NsConverter.postalEncode(a.getPostal().getIDVersion()),
						"1",
						a.getNumber(), a.getBox(), "", 
						a.getStatus(), 
						(a.getFromDate() != null) ? a.getFromDate().toString() : "", 
						(a.getTillDate() != null) ? a.getTillDate().toString() : "", 
						(a.getBeginLife() != null) ? a.getBeginLife().toString() : "",
						(a.getEndLife() != null) ? a.getEndLife().toString() : "",
						c, "", "", "false");

					if (++cnt % 10_000 == 0) {
						LOG.log(Level.INFO, "Wrote {0}", cnt);
					}
				}
				LOG.log(Level.INFO, "Wrote {0}", cnt);
			}
		}
	}

	/**
	 * Load XML BeST data files in a database file.
	 * 
	 * @param xmlPath path to XML BeST files
	 * @param csvPath output path for CSV files
	 * @param gps store coordinates as GPS coordinates
	 * @throws IOException
	 */
	public void write(Path xmlPath, Path csvPath, boolean gps) throws IOException {
		writePostals(xmlPath, csvPath);
		writeMunicipalities(xmlPath, csvPath);
		writeMunicipalityParts(xmlPath, csvPath);
		writeStreets(xmlPath, csvPath);
		writeAddresses(xmlPath, csvPath, gps);
	}

	/**
	 * Constructor
	 * 
	 * @throws java.lang.Exception
	 */
	public CsvPreparer() throws Exception {
		builder = CsvWriter.builder()
						.fieldSeparator(';').quoteCharacter('"').quoteStrategy(QuoteStrategy.REQUIRED)
						.lineDelimiter(LineDelimiter.LF);
		geoCoder = new GeoCoder();
	}
}
