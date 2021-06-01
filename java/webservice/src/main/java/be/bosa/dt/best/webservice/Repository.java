/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.bosa.dt.best.webservice;

import be.bosa.dt.best.webservice.entities.AddressDistance;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;

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
		"ST_DISTANCE(a.geom, ST_Transform(ST_SetSRID(ST_MakePoint($1, $2), 4326), 31370)) as distance " +
		"FROM Addresses a " +
		"INNER JOIN streets s ON a.street_id = s.id " +
		"INNER JOIN municipalities m ON a.city_id = m.id " +
		"INNER JOIN postals p ON a.postal_id = p.id " +
		"WHERE ST_DWithin(a.geom, ST_Transform(ST_SetSRID(ST_MakePoint($3, $4), 4326), 31370), $5) = TRUE " + 
		"ORDER by distance";
	
	public static Multi<AddressDistance> findAddressDistance(PgPool client, double x, double y, int maxdist) {
		return client.preparedQuery(SQL_DISTANCE)
					.execute(Tuple.tuple().addDouble(x).addDouble(y).addDouble(x).addDouble(y).addInteger(maxdist))
					.onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
					.onItem().transform(Repository::toAddressDistance);
	}

	private static AddressDistance toAddressDistance(Row res) {
		return new AddressDistance(
			res.getString(1), res.getString(2), res.getString(3), res.getString(4), 
			res.getDouble(5), res.getDouble(6), null, res.getString(8),
			res.getString(9), res.getString(10), res.getString(11), res.getString(12),
			res.getString(13), res.getString(14), res.getString(15), res.getString(16), res.getString(17),
			res.getString(18), res.getString(19), res.getString(20), res.getString(21), res.getString(22),
			res.getDouble(23));
	}
}
