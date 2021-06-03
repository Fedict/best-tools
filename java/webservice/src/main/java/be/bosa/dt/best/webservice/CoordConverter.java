/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.bosa.dt.best.webservice;


import javax.enterprise.context.ApplicationScoped;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Bart.Hanssens
 */
@ApplicationScoped
public class CoordConverter {
	private final CoordinateReferenceSystem l72;
	private final CoordinateReferenceSystem wgs84;
	private final MathTransform trans;
	/**
	 * Convert x y to a set of coordinates, optionally convert lambert72 to GPS coordinates
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return hex string
	 */
	public Coordinate toCoords(double x, double y) {
		Coordinate coords = new Coordinate(x, y);
		try {
			coords = JTS.transform(coords, null, trans);
		} catch (TransformException ex) {
			//
		}
		return coords;
	}

	public CoordConverter() throws FactoryException {
		l72 = CRS.decode("EPSG:31370");
		wgs84 = CRS.decode("EPSG:4326");
		trans = CRS.findMathTransform(wgs84, l72);
	}
}
