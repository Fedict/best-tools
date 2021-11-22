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


import io.quarkus.logging.Log;

import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.InvalidValueException;
import org.locationtech.proj4j.ProjCoordinate;
import org.locationtech.proj4j.UnknownAuthorityCodeException;
import org.locationtech.proj4j.UnsupportedParameterException;


/**
 * Helper class to convert Lambert 72 from/to GPS coordinates
 * 
 * @author Bart Hanssens
 */
public class CoordConverter {
	private static CoordinateReferenceSystem l72;
	private static CoordinateReferenceSystem gps;
	private static BasicCoordinateTransform L72toGPS;
	private static BasicCoordinateTransform GPStoL72;
	
	static {
		try {
			CRSFactory factory = new CRSFactory();
			l72 = factory.createFromName("EPSG:31370");
			gps = factory.createFromName("EPSG:4258");
			L72toGPS = new BasicCoordinateTransform(l72, gps);
			GPStoL72 = new BasicCoordinateTransform(gps, l72);
		} catch (InvalidValueException | UnknownAuthorityCodeException | UnsupportedParameterException ex) {
			Log.fatal(ex.getMessage());
		}
	}

	/**
	 * Convert GPS coordinates to Lambert72 x y
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return coordinate
	 */
	public static ProjCoordinate gpsToL72(double x, double y) {
		ProjCoordinate src = new ProjCoordinate(x, y);
		ProjCoordinate dest = new ProjCoordinate();
		return GPStoL72.transform(src, dest);
	}

	/**
	 * Convert Lambert72 x y to GPS coordinates
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return coordinate
	 */
	public static ProjCoordinate l72ToGps(double x, double y) {
		ProjCoordinate src = new ProjCoordinate(x, y);
		ProjCoordinate dest = new ProjCoordinate();
		return L72toGPS.transform(src, dest);
	}

/**
	 * Build coordinates and convert to Lambert72
	 * 
	 * @param coordX X-coordinate
	 * @param coordY Y-coordinate
	 * @param crs gps or l72 coordinate reference system
	 * @return 
	 */
	public static ProjCoordinate makeL72Coordinate(double coordX, double coordY, String crs) {
		return (crs == null || crs.toLowerCase().equals("gps"))
									? CoordConverter.gpsToL72(coordX, coordY) 
									: new ProjCoordinate(coordX, coordY);
	}

	/**
	 * Build "Well-Known Text" polygon from string
	 * 
	 * @param polygon polygon string
	 * @param crs gps or l72 coordinate reference system
	 * @return 
	 */
	public static String makeWktPolygon(String polygon, String crs) {
		StringBuilder builder = new StringBuilder(80);
		String[] points = polygon.split("~");
		builder.append("'POLYGON((");

		for (String p: points) {
			String[] coords = p.split(",");
			double coordX = Double.valueOf(coords[0]);
			double coordY = Double.valueOf(coords[1]);

			ProjCoordinate point = makeL72Coordinate(coordX, coordY, crs);
			builder.append(point.x).append(' ').append(point.y).append(',');
		}
		builder.deleteCharAt(builder.length()-1); // remove last ','
		builder.append("'))");
		
		return builder.toString();
	}
}
