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

/**
 *
 * @author Bart.Hanssens
 */
@ApplicationScoped
public class Repository {
	private final static String SQL_DISTANCE = 
		"SELECT a.id, a.part_id, a.houseno, a.boxno, " +
				"a.x, a.y, a.geom, a.status, " +
				"s.id, s.name_nl, s.name_fr, s.name_de, " +
				"m.id, m.niscode, m.name_nl, m.name_fr, m.name_de, " +
				"p.id, p.zipcode, p.name_nl, p.name_fr, p.name_de, " +
		"ST_DISTANCE(a.geom, ST_Transform(ST_SetSRID(ST_MakePoint(?, ?), 4326), 31370)) as distance " +
		"FROM Addresses a " +
		"INNER JOIN a.street s " +
		"INNER JOIN a.municipality m " +
		"INNER JOIN a.postal p " +
		"WHERE ST_DWithin(a.geom, ST_Transform(ST_SetSRID(ST_MakePoint(?, ?), 4326), 31370), ?) = TRUE " + 
		"ORDER by distance";
	
	@Inject
	AgroalDataSource ds;
	
	public List<AddressDistance> findAddressDistance(double x, double y, int maxdist) {
		List<AddressDistance> list = new ArrayList<>();
		String params[] = new String[] { String.valueOf(x), String.valueOf(y), 
										String.valueOf(x), String.valueOf(y), String.valueOf(maxdist) };
		
		try(Connection conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement(SQL_DISTANCE)) {
				stmt.setDouble(1, x);
				stmt.setDouble(2, y);
				stmt.setDouble(3, x);
				stmt.setDouble(4, y);
				stmt.setDouble(5, maxdist);
		
				try(ResultSet res = stmt.executeQuery()) {
					while (res.next()) {
						list.add(
							new AddressDistance(
						res.getString(1), res.getString(2), res.getString(3), res.getString(4), 
						res.getDouble(5), res.getDouble(5), res.getObject(6), res.getString(7),
						res.getString(8), res.getString(9), res.getString(10), res.getString(11),
						res.getString(12), res.getString(13), res.getString(14), res.getString(15), res.getString(16),
						res.getString(17), res.getString(18), res.getString(19), res.getString(20), res.getString(21),
						res.getDouble(22)));
				}
			}
		} catch (SQLException ex) {
			throw new WebApplicationException(ex);
		}
		return list;
	}
}
