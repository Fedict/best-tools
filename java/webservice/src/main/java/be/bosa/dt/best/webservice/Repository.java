/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.bosa.dt.best.webservice;

import be.bosa.dt.best.webservice.entities.Address;
import be.bosa.dt.best.webservice.entities.AddressDistance;
import be.bosa.dt.best.webservice.entities.Municipality;
import be.bosa.dt.best.webservice.entities.Street;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.MultiOnItem;
import io.smallrye.mutiny.groups.UniOnItem;

import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.locationtech.jts.geom.Coordinate;

/**
 * Connect to database
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
public class Repository {
	@Inject
	PgPool pg;

	// queries
	private final static String SQL_DISTANCE = 
		"SELECT ST_DISTANCE(a.geom, b.geom) as distance " +
		"FROM addresses a, addresses b " +
		"WHERE a.id = $1 AND b.id = $2";

	private final static String SQL_NEAR_DISTANCE = 
		"SELECT a.id, a.part_id, a.houseno, a.boxno, " +
				"a.x, a.y, a.geom, a.status, " +
				"s.id, s.city_id, s.name_nl, s.name_fr, s.name_de, " +
				"m.id, m.niscode, m.name_nl, m.name_fr, m.name_de, " +
				"p.id, p.zipcode, p.name_nl, p.name_fr, p.name_de, " +
		" a.geom <-> ST_SetSRID(ST_MakePoint($1, $2), 31370) as distance " +
		"FROM Addresses a " +
		"INNER JOIN streets s ON a.street_id = s.id " +
		"INNER JOIN municipalities m ON a.city_id = m.id " +
		"INNER JOIN postals p ON a.postal_id = p.id " +
		"WHERE ST_DWithin(a.geom, ST_SetSRID(ST_MakePoint($3, $4), 31370), $5) = TRUE " + 
		"ORDER by distance " + 
		"LIMIT $6";

	private final static String SQL_ADDRESS_ID = 
		"SELECT a.id, a.part_id, a.houseno, a.boxno, " +
				"a.x, a.y, a.geom, a.status, " +
				"s.id, s.name_nl, s.name_fr, s.name_de, " +
				"m.id, m.niscode, m.name_nl, m.name_fr, m.name_de, " +
				"p.id, p.zipcode, p.name_nl, p.name_fr, p.name_de " +
		"FROM addresses a " +
		"INNER JOIN streets s ON a.street_id = s.id " +
		"INNER JOIN municipalities m ON a.city_id = m.id " +
		"INNER JOIN postals p ON a.postal_id = p.id " +
		"WHERE a.id = $1";

	private final static String SQL_MUNICIPALITIES = 
		"SELECT m.id, m.niscode, m.name_nl, m.name_fr, m.name_de " +
		"FROM municipalities m";

	private final static String SQL_MUNICIPALITY_ID = 
		"SELECT m.id, m.niscode, m.name_nl, m.name_fr, m.name_de " +
		"FROM municipalities m " +
		"WHERE m.id = $1";

	private final static String SQL_MUNICIPALITY_NIS = 
		"SELECT m.id, m.niscode, m.name_nl, m.name_fr, m.name_de " +
		"FROM municipalities m " +
		"WHERE m.niscode = $1";
		
	private final static String SQL_MUNICIPALITY_ZIP = 
		"SELECT m.id, m.niscode, m.name_nl, m.name_fr, m.name_de " +
		"FROM municipalities m " +
		"INNER JOIN postal_municipalities p ON p.city_id = m.id " +
		"WHERE p.zipcode = $1";

	private final static String SQL_MUNICIPALITY_NAME = 
		"SELECT m.id, m.niscode, m.name_nl, m.name_fr, m.name_de " +
		"FROM municipalities m " +
		"WHERE (m.name_nl LIKE '$1' or m.name_fr LIKE '$2' or m.name_de LIKE '$3')";
	
	private final static String SQL_STREET_ID = 
		"SELECT s.id, s.name_nl, s.name_fr, s.name_de " +
		"FROM streets s " +
		"WHERE s.id = $1";

	private final static String SQL_STREET_ZIP = 
		"SELECT s.id, s.city_id, s.name_nl, s.name_fr, s.name_de " +
		"FROM streets s " +
		"INNER JOIN postal_municipalities p ON p.city_id = s.city_id " +
		"WHERE p.zipcode = $1 ";

	private final static String SQL_STREET_ZIP_NAME = 
		"SELECT s.id, s.city_id, s.name_nl, s.name_fr, s.name_de " +
		"FROM streets s " +
		"INNER JOIN postal_municipalities p ON p.city_id = s.city_id " +
		"WHERE p.zipcode = $1 " +
		"AND (s.name_nl LIKE '$1' or s.name_fr LIKE '$2' or s.name_de LIKE '$3')";

	private final static String SQL_STREET_NIS = 
		"SELECT s.id, s.city_id, s.name_nl, s.name_fr, s.name_de " +
		"FROM streets s " +
		"INNER JOIN municipalities m ON s.city_id = m.id " +
		"WHERE m.niscode = $1 ";

	private final static String SQL_STREET_NIS_NAME = 
		"SELECT s.id, s.city_id, s.name_nl, s.name_fr, s.name_de " +
		"FROM streets s " +
		"INNER JOIN municipalities m ON s.city_id = m.id " +
		"WHERE m.niscode = $1 " +
		"AND (s.name_nl LIKE '$1' or s.name_fr LIKE '$2' or s.name_de LIKE '$3')";

	/**
	 * Convert rows to a multi result
	 * 
	 * @param res row set
	 * @param mapper entity mapper
	 * @return multi of data objects
	 */
	private MultiOnItem<Row> multi(Uni<RowSet<Row>> res) {
		return res.onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows)).onItem();
	}
	
	/**
	 * Convert rows to a uni result
	 * 
	 * @param res row set
	 * @param mapper entity mapper
	 * @return multi of data objects
	 */
	private UniOnItem<RowIterator<Row>> uni(Uni<RowSet<Row>> res) {
		return res.onItem().transform(RowSet::iterator).onItem();
	}

	/**
	 * Find the list of addresses within a range of 100m, and calculate distance to GPS location
	 * 
	 * @param x GPS x coordinate
	 * @param y GPS y coordinat
	 * @param maxdist maximum distance (in meters)
	 * @param maxres maximum number of results
	 * @return address with distance to location
	 */
	public Multi<AddressDistance> findAddressDistance(double x, double y, int maxdist, int maxres) {
		Coordinate l72 = CoordConverter.gpsToLambert(x, y);
		return multi(pg.preparedQuery(SQL_NEAR_DISTANCE).execute(Tuple.of(l72.x, l72.y, l72.x, l72.y, maxdist, maxres))
		).transform(AddressDistance::from);
	}

	/**
	 * Calculate the approximate distance between two addresses in meters
	 * 
	 * @param a ID first address 
	 * @param b ID second address
	 * @return address
	 */
	public Uni<Double> findDistance(String a, String b) {
		return uni(
			pg.preparedQuery(SQL_DISTANCE).execute(Tuple.of(a, b))
		).transform(row -> row.hasNext() ? row.next().getDouble(0): null);
	}
	
	/**
	 * Find the address by ID
	 * 
	 * @param id full address id
	 * @return address
	 */
	public Uni<Address> findAddressById(String id) {
		return uni(
			pg.preparedQuery(SQL_ADDRESS_ID).execute(Tuple.of(id))
		).transform(row -> row.hasNext() ? Address.from(row.next()) : null);
	}

	/**
	 * Find all municipalities
	 * 
	 * @return municipalities
	 */
	public Multi<Municipality> findMunicipalities() {
		return multi(
			pg.preparedQuery(SQL_MUNICIPALITIES).execute())
		.transform(Municipality::from);
	}
	
	/**
	 * Find the municipality by ID
	 * 
	 * @param id municipality id
	 * @return municipality
	 */
	public Uni<Municipality> findMunicipalityById(String id) {
		return uni(
			pg.preparedQuery(SQL_MUNICIPALITY_ID).execute(Tuple.of(id))
		).transform(row -> row.hasNext() ? Municipality.from(row.next()) : null);
	}

	/**
	 * Find municipalities by part of name
	 * 
	 * @param name part of name
	 * @return municipalities
	 */
	public Multi<Municipality> findMunicipalitiesByName(String name) {
		String str = name + '%';
		return multi(
			pg.preparedQuery(SQL_MUNICIPALITY_NAME).execute(Tuple.of(str, str, str)))
		.transform(Municipality::from);
	}

	/**
	 * Find municipalities by REFNIS code
	 * 
	 * @param niscode nis code
	 * @return municipalities
	 */
	public Multi<Municipality> findMunicipalitiesByNiscode(String niscode) {
		return multi(
			pg.preparedQuery(SQL_MUNICIPALITY_NIS).execute(Tuple.of(niscode)))
		.transform(Municipality::from);
	}

	/**
	 * Find municipalities by postal code
	 * 
	 * @param zipcode postal code
	 * @return municipalities
	 */
	public Multi<Municipality> findMunicipalitiesByZipcode(String zipcode) {
		return multi(
			pg.preparedQuery(SQL_MUNICIPALITY_ZIP).execute(Tuple.of(zipcode)))
		.transform(Municipality::from);
	}

	/**
	 * Find the street by ID
	 * 
	 * @param id street id
	 * @return street
	 */
	public Uni<Street> findStreetById(String id) {
		return uni(
			pg.preparedQuery(SQL_STREET_ID).execute(Tuple.of(id))
		).transform(row -> row.hasNext() ? Street.from(row.next()) : null);
	}

	/**
	 * Find the streets by postal code
	 * 
	 * @param niscode
	 * @return streets
	 */
	public Multi<Street> findStreetsByNiscode(String niscode) {
		return multi(
			pg.preparedQuery(SQL_STREET_NIS).execute(Tuple.of(niscode))
		).transform(Street::from);
	}

	/**
	 * Find the streets by part of name
	 * 
	 * @param niscode
	 * @param name part of name
	 * @return streets
	 */
	public Multi<Street> findStreetsByNiscodeAndName(String niscode, String name) {
		String str = name + '%';
		return multi(
			pg.preparedQuery(SQL_STREET_NIS_NAME).execute(Tuple.of(niscode, str, str, str))
		).transform(Street::from);
	}

	/**
	 * Find the streets by postal code
	 * 
	 * @param zipcode
	 * @return streets
	 */
	public Multi<Street> findStreetsByZipcode(String zipcode) {
		return multi(
			pg.preparedQuery(SQL_STREET_ZIP).execute(Tuple.of(zipcode))
		).transform(Street::from);
	}

	/**
	 * Find the streets by part of name
	 * 
	 * @param zipcode
	 * @param name part of name
	 * @return streets
	 */
	public Multi<Street> findStreetsByZipcodeAndName(String zipcode, String name) {
		String str = name + '%';
		return multi(
			pg.preparedQuery(SQL_STREET_ZIP_NAME).execute(Tuple.of(zipcode, str, str, str))
		).transform(Street::from);
	}
}
