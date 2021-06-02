/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.bosa.dt.best.webservice;

import be.bosa.dt.best.webservice.entities.AddressDistance;
import io.agroal.api.AgroalDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Bart.Hanssens
 */
@ApplicationScoped
public class Repository {
	private final static GeometryFactory fac = JTSFactoryFinder.getGeometryFactory();
	private CoordinateReferenceSystem l72;
	private CoordinateReferenceSystem wgs84;
	private MathTransform trans;

	private final static String SQL_DISTANCE = 
		"SELECT a.id, a.part_id, a.houseno, a.boxno, " +
				"a.x, a.y, a.geom, a.status, " +
				"s.id, s.name_nl, s.name_fr, s.name_de, " +
				"m.id, m.niscode, m.name_nl, m.name_fr, m.name_de, " +
				"p.id, p.zipcode, p.name_nl, p.name_fr, p.name_de, " +
		"ST_DISTANCE(a.geom, ST_SetSRID(ST_MakePoint(?, ?), 31370)) as distance " +
		"FROM Addresses a " +
		"INNER JOIN streets s ON a.street_id = s.id " +
		"INNER JOIN municipalities m ON a.city_id = m.id " +
		"INNER JOIN postals p ON a.postal_id = p.id " +
		"WHERE ST_DWithin(a.geom, ST_SetSRID(ST_MakePoint(?, ?), 31370), ?) = TRUE " + 
		"ORDER by distance";
	
	@Inject
	AgroalDataSource ds;
	
	public List<AddressDistance> findAddressDistance(double x, double y, int maxdist) {
		List<AddressDistance> list = new ArrayList<>();

		try(Connection conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement(SQL_DISTANCE)) {
				stmt.setDouble(1, x);
				stmt.setDouble(2, y);
				Coordinate coordl72 = toCoords(x, y);
				stmt.setDouble(3, coordl72.x);
				stmt.setDouble(4, coordl72.y);			
				stmt.setInt(5, maxdist);
		
				try(ResultSet res = stmt.executeQuery()) {
					while (res.next()) {
						list.add(
							new AddressDistance(
						res.getString(1), res.getString(2), res.getString(3), res.getString(4), 
						res.getDouble(5), res.getDouble(6), null, res.getString(8),
						res.getString(9), res.getString(10), res.getString(11), res.getString(12),
						res.getString(13), res.getString(14), res.getString(15), res.getString(16), res.getString(17),
						res.getString(18), res.getString(19), res.getString(20), res.getString(21), res.getString(22),
						res.getDouble(23)));
				}
			}
		} catch (SQLException ex) {
			throw new WebApplicationException(ex);
		}
		return list;
	}

	/**
	 * Convert x y to a set of coordinates, optionally convert lambert72 to GPS coordinates
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param gps convert to gps or not
	 * @return hex string
	 */
	private Coordinate toCoords(double x, double y) {
		Coordinate coords = new Coordinate(x, y);
		try {
			coords = JTS.transform(coords, null, trans);
		} catch (TransformException ex) {
			//
		}
		return coords;
	}

	public Repository() throws Exception {
		l72 = CRS.decode("EPSG:31370");
		wgs84 = CRS.decode("EPSG:4326");
		trans = CRS.findMathTransform(wgs84, l72);
	}
}
