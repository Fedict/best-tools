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


import java.io.IOException;


import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBWriter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;


/**
 * Convert coordinates
 * 
 * @author Bart Hanssens
 */
public class GeoCoder {
	// geo transformations
	private final static GeometryFactory fac = JTSFactoryFinder.getGeometryFactory();
	private final static WKBWriter wkb = new WKBWriter();
	private CoordinateReferenceSystem l72;
	private CoordinateReferenceSystem wgs84;
	private MathTransform trans;

	
	/**
	 * Convert x y to a set of coordinates, optionally convert lambert72 to GPS coordinates
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param gps convert to gps or not
	 * @return hex string
	 */
	public Coordinate toCoords(double x, double y, boolean gps) throws IOException {
		Coordinate coords = new Coordinate(x, y);
		if (gps) {
			try {
				coords = JTS.transform(coords, null, trans);
			} catch (TransformException ex) {
				throw new IOException("Could not convert coordinates");
			}
		}
		return coords;
	}

	/**
	 * Convert geo coordinates to a hex representation of "well-known binary" format
	 * 
	 * @param coords coordinates
	 * @return hex string
	 */
	public String toWkb(Coordinate coords) {
		return WKBWriter.toHex(wkb.write(fac.createPoint(coords)));
	}

	/**
	 * Constructor
	 * 
	 * @throws java.lang.Exception
	 */
	public GeoCoder() throws Exception {
		try {
			l72 = CRS.decode("EPSG:31370");
			wgs84 = CRS.decode("EPSG:4326");
			trans = CRS.findMathTransform(l72, wgs84);
		} catch (FactoryException e) {
			throw new Exception("Could not initialize geo", e);
		}
	}
}
