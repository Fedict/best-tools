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

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKBWriter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;


/**
 * Writes files into CSV suitable for Postgresql "COPY" fast import
 * 
 * @author Bart Hanssens
 */
public class CsvPreparer {
	private static final Logger LOG = Logger.getLogger(CsvPreparer.class.getName());
	
	private final CsvWriterBuilder builder;

	// geo transformations
	private final static GeometryFactory fac = JTSFactoryFinder.getGeometryFactory();
	private final static WKBWriter wkb = new WKBWriter();
	private CoordinateReferenceSystem l72;
	private CoordinateReferenceSystem wgs84;
	private MathTransform trans;

	
	/**
	 * Convert geo coordinates to a hex representation of "well-known binary" format
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param gps convert to gps or not
	 * @return hex string
	 */
	private String toWkb(double x, double y, boolean gps) throws IOException {
		Coordinate coords = new Coordinate(x, y);
		if (gps) {
			try {
				coords = JTS.transform(coords, null, trans);
			} catch (TransformException ex) {
				throw new IOException("Could not convert coordinates");
			}
		}
		Point point = fac.createPoint(coords);
		return WKBWriter.toHex(wkb.write(point));
	}

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
					w.writeRow(a.getIDVersion(), a.getId(), a.getName("nl"), a.getName("fr"), a.getName("de"));
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
					w.writeRow(a.getIDVersion(), a.getId(), a.getName("nl"), a.getName("fr"), a.getName("de"));
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
					w.writeRow(a.getIDVersion(), a.getName("nl"), a.getName("fr"), a.getName("de"));
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
					w.writeRow(a.getIDVersion(), a.getCity().getIDVersion(), 
								a.getName("nl"), a.getName("fr"), a.getName("de"), a.getStatus());
				
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
					String s = toWkb(p.getX(), p.getY(), gps);

					// calculate geom afterwards, using separate UPDATE statement
					w.writeRow(a.getIDVersion(), a.getCity().getIDVersion(), a.getCityPart().getIDVersion(),
							a.getStreet().getIDVersion(), a.getPostal().getIDVersion(), a.getNumber(),
							a.getBox(), a.getStatus(), 
							String.valueOf(p.getX()), String.valueOf(p.getY()), s);

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
	 */
	public CsvPreparer() {
		builder = CsvWriter.builder()
						.fieldSeparator(';').quoteCharacter('"').quoteStrategy(QuoteStrategy.REQUIRED)
						.lineDelimiter(LineDelimiter.LF);
		try {
			l72 = CRS.decode("EPSG:31370");
			wgs84 = CRS.decode("EPSG:4326");
			trans = CRS.findMathTransform(l72, wgs84);
		} catch (FactoryException e) {
			LOG.log(Level.SEVERE, "Could not initialize geo{0}", e.getMessage());
		}
	}
}
