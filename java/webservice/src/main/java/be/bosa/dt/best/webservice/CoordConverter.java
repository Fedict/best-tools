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
package be.bosa.dt.best.webservice;


import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.jboss.logging.Logger;
import org.locationtech.jts.geom.Coordinate;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Helper class to convert Lambert 72 from/to GPS coordinates
 * 
 * @author Bart Hanssens
 */
public class CoordConverter {
	private static CoordinateReferenceSystem l72;
	private static CoordinateReferenceSystem wgs84;
	private static MathTransform L72ToWgs84;
	private static MathTransform Wgs84ToL72;

	private final static Logger LOG = Logger.getLogger(CoordConverter.class);

	static {
		try {
			l72 = CRS.decode("EPSG:31370");
			wgs84 = CRS.decode("EPSG:4326");
			Wgs84ToL72 = CRS.findMathTransform(wgs84, l72);
			L72ToWgs84 = CRS.findMathTransform(l72, wgs84);
		} catch (FactoryException ex) {
			LOG.fatal(ex.getMessage(), ex);
		}
	}

	/**
	 * Convert one set of coordinates to another one using a specific transformation
	 * 
	 * @param coords
	 * @param transform
	 * @return 
	 */
	private static Coordinate convert(Coordinate coords, MathTransform transform) {
		try {
			return JTS.transform(coords, null, transform);
		} catch (TransformException ex) {
			return null;
		}
	}

	/**
	 * Convert GPS coordinates to Lambert72 x y
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return coordinate
	 */
	public static Coordinate gpsToLambert(double x, double y) {
		return convert(new Coordinate(x,y), Wgs84ToL72);
	}

	/**
	 * Convert Lambert72 x y to GPS coordinates
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return coordinate
	 */
	public static Coordinate lambertToGps(double x, double y) {
		return convert(new Coordinate(x,y), L72ToWgs84);
	}	
}