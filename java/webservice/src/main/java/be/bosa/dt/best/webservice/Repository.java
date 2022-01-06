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

import be.bosa.dt.best.webservice.util.NsConverter;
import be.bosa.dt.best.webservice.util.CoordConverter;
import be.bosa.dt.best.webservice.entities.Address;
import be.bosa.dt.best.webservice.entities.Municipality;
import be.bosa.dt.best.webservice.entities.MunicipalityPart;
import be.bosa.dt.best.webservice.entities.PostalInfo;
import be.bosa.dt.best.webservice.entities.Street;
import be.bosa.dt.best.webservice.entities.Version;
import be.bosa.dt.best.webservice.queries.Sql;
import be.bosa.dt.best.webservice.queries.SqlAddress;
import be.bosa.dt.best.webservice.queries.SqlGeo;
import be.bosa.dt.best.webservice.queries.SqlMunicipality;
import be.bosa.dt.best.webservice.queries.SqlMunicipalityPart;
import be.bosa.dt.best.webservice.queries.SqlPostalInfo;
import be.bosa.dt.best.webservice.queries.SqlStreet;
import be.bosa.dt.best.webservice.queries.SqlVersion;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.MultiOnItem;
import io.smallrye.mutiny.groups.UniOnItem;

import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;

import org.locationtech.proj4j.ProjCoordinate;


/**
 * Connect to database
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
public class Repository {
	@Inject
	PgPool pg;

	// can't use ConfigProperty annotation here
	protected final static int PAGE_LIMIT = 
			ConfigProvider.getConfig()
				.getOptionalValue("be.bosa.dt.best.webservice.page_limit", Integer.class).orElse(250);

	public static final String CRS_GPS = "gps";
	public static final String CRS_L72 = "lambert72";

	public static final String STATUS_RESERVED = "reserved";
	public static final String STATUS_CURRENT = "current";
	public static final String STATUS_RETIRED = "retired";

	public static final String SEARCH_EXACT = "exact";
	public static final String SEARCH_FUZZY = "fuzzy";
	public static final String SEARCH_STARTWITH = "startwith";

	private final static Map<String,JsonObject> cache = new HashMap<>(225000);

	/**
	 * Get municipality / street cache
	 * 
	 * @return cache as map
	 */
	public static Map<String,JsonObject> getCache() {
		return cache;
	}

	/**
	 * Cache the JSON rendering of all municipalities
	 * 
	 * @param ev 
	 */
	void onStart(@Observes StartupEvent ev) {               
        Log.info("Caching");
	
		Multi<Municipality> municipalities = findMunicipalitiesAll();
		municipalities.subscribe().asStream().forEach(m -> {
			cache.put(m.id, JsonObject.mapFrom(m));
		});
		int size = cache.size();
		Log.infof("%d municipalities", size);
	
		Multi<MunicipalityPart> parts = findMunicipalityPartsAll();
		parts.subscribe().asStream().forEach(mp -> {
			cache.put(mp.id, JsonObject.mapFrom(mp));
		});
		Log.infof("%d municipality parts", cache.size() - size);
		size = cache.size();
		
		Multi<PostalInfo> postals = findPostalInfosAll();
		postals.subscribe().asStream().forEach(p -> {			
			cache.put(p.id, JsonObject.mapFrom(p));
		});
		Log.infof("%d postal info", cache.size() - size);
		
		Multi<Street> streets = findStreetsAll();
		streets.subscribe().asStream().forEach(p -> {			
			cache.put(p.id, JsonObject.mapFrom(p));
		});
		Log.infof("%d streets", cache.size() - size);
    }

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
		return res.map(RowSet::iterator).onItem();
	}


	/**
	 * Add paginate / limit clause to SQL query, optionally start after last ID of previous page
	 * 
	 * @param lst list of SQL parameter values
	 * @param qry SQL query
	 * @param afterID last ID on previous result page
	 */
	private static void paginate(List lst, Sql qry, String afterId) {
		if (afterId != null) {
			qry.paginate();
			lst.add(afterId);
		} else {
			qry.limit();
		}
		lst.add(PAGE_LIMIT);
		qry.orderById();
	}

	/**
	 * Add non-empty fields to SQL where clause
	 * 
	 * @param lst list of SQL parameter values
	 * @param qry SQL query
	 * @param value value
	 */
	private static void where(List lst, Sql qry, String field, String value) {
		if (value != null && !value.isEmpty()) {
			qry.where(field + " =");
			lst.add(value);
		}
	}
	
	/**
	 * Add non-empty fields to SQL where clause
	 * 
	 * @param lst list of SQL parameter values
	 * @param qry SQL query
	 * @param value value
	 */
	private static void whereNames(List lst, Sql qry, String nl, String fr, String de, String value, String matchType) {
		if (value != null && !value.isEmpty()) {
			qry.whereNames(nl, fr, de, matchType);
			lst.add(value);
		}
	}

	/**
	 * Find an address by ID
	 * 
	 * @param id full address id
	 * @param embed embed street, postal etc or not
	 * @return address or null
	 */
	public Uni<Address> findAddressById(String id, boolean embed) {
		Tuple tuple = Tuple.of(NsConverter.addressEncode(id));
		SqlAddress qry = new SqlAddress(embed, false, false, false);
		qry.where("a.identifier =");

		return uni(
			pg.preparedQuery(qry.build()).execute(tuple)
		).transform(row -> row.hasNext() ? Address.from(row.next()) : null);
	}

	/**
	 * Find address by different parameters
	 * 
	 * @param afterId search after ID (paginated results)
	 * @param mIdentifier municipality identifier
	 * @param nisCode
	 * @param mName municipality name
	 * @param sIdentifier street identifier
	 * @param sName street name
	 * @param postalCode postal code
	 * @param pName postal name
	 * @param pIdentifier postal identifier
	 * @param houseNumber house number
	 * @param boxNumber box number
	 * @param status status
	 * @param embed embed street, postal etc or not
	 * @return 
	 */
	public Multi<Address> findAddresses(String afterId, String mIdentifier, String nisCode, String mName,
										String sIdentifier, String sName,
										String pIdentifier, String postalCode, String pName,
										String houseNumber, String boxNumber, String status, boolean embed) {
		boolean joinMunicipality = Util.oneNotEmpty(mName, nisCode);
		boolean joinStreet = Util.oneNotEmpty(sName);
		boolean joinPostal = Util.oneNotEmpty(postalCode);

		List lst = new ArrayList(12);
		SqlAddress qry = new SqlAddress(embed, joinStreet, joinMunicipality,  joinPostal);

		where(lst, qry, "a.mIdentifier", NsConverter.municipalityEncode(mIdentifier));
		where(lst, qry, "m.refnisCode", nisCode);
		whereNames(lst, qry, "m.nameNL", "m.nameFR", "m.nameDE", mName, Repository.SEARCH_EXACT);
		where(lst, qry, "a.sIdentifier", NsConverter.streetEncode(sIdentifier));
		whereNames(lst, qry, "s.nameNL", "s.nameFR", "s.nameDE", sName, Repository.SEARCH_EXACT);
		where(lst, qry, "p.postalCode", postalCode);
		where(lst, qry, "a.pIdentifier", NsConverter.postalEncode(pIdentifier));
		where(lst, qry, "a.houseNumber", houseNumber);
		where(lst, qry, "a.boxNumber", boxNumber);
		where(lst, qry, "a.status::text", status);
		paginate(lst, qry, NsConverter.addressEncode(afterId));

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.from(lst))
		).transform(Address::from);
	}

	/**
	 * Find addresses by coordinates
	 * 
	 * @param afterId search after ID (paginated results)
	 * @param coordX X-coordinate
	 * @param coordY Y-coordinate
	 * @param crs coordinate reference system
	 * @param radius in meters
	 * @param status
	 * @param embed embed street, postal etc or not
	 * @return 
	 */
	public Multi<Address> findByCoordinates(String afterId, double coordX, double coordY, String crs, int radius, 
											String status, boolean embed) {	
		ProjCoordinate point = CoordConverter.makeL72Coordinate(coordX, coordY, crs);

		List lst = new ArrayList(7); 
		lst.add(point.x);
		lst.add(point.y);
		lst.add(radius);

		SqlGeo qry = new SqlGeo(embed, true);
		where(lst, qry, "a.status", status);
		paginate(lst, qry, NsConverter.addressEncode(afterId));

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.from(lst))
		).transform(Address::from);
	}

	/**
	 * Find addresses by polygon
	 * 
	 * @param afterId search after ID (paginated results)
	 * @param polygon polygon points
	 * @param crs coordinate reference system
	 * @param status
	 * @param embed embed street, postal etc or not
	 * @return 
	 */
	public Multi<Address> findByPolygon(String afterId, String polygon, String crs, String status, boolean embed) {
		String coords = CoordConverter.makeWktPolygon(polygon, crs);
		
		List lst = new ArrayList(4); 
		lst.add(coords);

		SqlGeo qry = new SqlGeo(embed, false);
		where(lst, qry, "a.status", status);
		paginate(lst, qry, NsConverter.addressEncode(afterId));

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.from(lst))
		).transform(Address::from);
	}

	/**
	 * Find a municipality by ID
	 * 
	 * @param id municipality id
	 * @return municipality or null
	 */
	public Uni<Municipality> findMunicipalityById(String id) {
		Tuple tuple = Tuple.of(NsConverter.municipalityEncode(id));
		SqlMunicipality qry = new SqlMunicipality(false);
		qry.where("m.identifier =");

		return uni(
			pg.preparedQuery(qry.build()).execute(tuple)
		).transform(row -> row.hasNext() ? Municipality.from(row.next()) : null);
	}

	/**
	 * Find municipalities by REFNIS code (Statbel)
	 * 
	 * @param nisCode
	 * @param postalCode
	 * @param name
	 * @param nameMatch
	 * @return municipalities or null
	 */
	public Multi<Municipality> findMunicipalities(String nisCode, String postalCode, String name, String nameMatch) {
		boolean joinPostal = Util.oneNotEmpty(postalCode);
		List lst = new ArrayList(4);
		
		SqlMunicipality qry = new SqlMunicipality(joinPostal);
		where(lst, qry, "m.refniscode", nisCode);
		where(lst, qry, "p.postalcode", postalCode);
		whereNames(lst, qry, "m.nameNL", "m.nameFR", "m.nameDE", name, nameMatch);

		qry.orderById();
		qry.unlimited();

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.from(lst))
		).transform(Municipality::from);
	}

	/**
	 * Get all municipalities
	 * 
	 * @return 
	 */
	public Multi<Municipality> findMunicipalitiesAll() {
		SqlMunicipality qry = new SqlMunicipality(false);
		qry.orderById();
		qry.unlimited();

		return multi(
			pg.preparedQuery(qry.build()).execute()
		).transform(Municipality::from);
	}

	/**
	 * Find a municipality by ID
	 * 
	 * @param id municipality part id
	 * @return municipality part or null
	 */
	public Uni<MunicipalityPart> findMunicipalityPartById(String id) {
		Tuple tuple = Tuple.of(NsConverter.municipalityPartEncode(id));
		SqlMunicipalityPart qry = new SqlMunicipalityPart();
		qry.where("mp.identifier =");

		return uni(
			pg.preparedQuery(qry.build()).execute(tuple)
		).transform(row -> row.hasNext() ? MunicipalityPart.from(row.next()) : null);
	}

	/**
	 * Find municipality parts
	 * 
	 * @param afterId search after ID (paginated results)
	 * @param name
	 * @return 
	 */
	public Multi<MunicipalityPart> findMunicipalityParts(String afterId, String name) {
		List lst = new ArrayList(3);
		SqlMunicipalityPart qry = new SqlMunicipalityPart();

		whereNames(lst, qry, "mp.nameNL", "mp.nameFR", "mp.nameDE", name, Repository.SEARCH_EXACT);
		paginate(lst, qry, NsConverter.municipalityPartEncode(afterId));

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.from(lst))
		).transform(MunicipalityPart::from);
	}
	
	/**
	 * Get all municipality parts
	 * 
	 * @return 
	 */
	public Multi<MunicipalityPart> findMunicipalityPartsAll() {
		SqlMunicipalityPart qry = new SqlMunicipalityPart();
		qry.orderById();
		qry.unlimited();

		return multi(
			pg.preparedQuery(qry.build()).execute()
		).transform(MunicipalityPart::from);
	}

	/**
	 * Find a postal info by ID
	 * 
	 * @param id full postalinfo id
	 * @return postalinfo or null
	 */
	public Uni<PostalInfo> findPostalInfoById(String id) {
		Tuple tuple = Tuple.of(NsConverter.postalEncode(id));
		SqlPostalInfo qry = new SqlPostalInfo();
		qry.where("p.identifier =");

		return uni(
			pg.preparedQuery(qry.build()).execute(tuple)
		).transform(row -> row.hasNext() ? PostalInfo.from(row.next()) : null);
	}

	/**
	 * Find postal info
	 * 
	 * @param afterId search after ID (paginated results)
	 * @param postalCode postal code
	 * @param name postal name
	 * @param nameMatch
	 * @return 
	 */
	public Multi<PostalInfo> findPostalInfos(String afterId, String postalCode, String name, String nameMatch) {
		List lst = new ArrayList(4);
		SqlPostalInfo qry = new SqlPostalInfo();

		where(lst, qry, "p.postalcode", postalCode);
		
		whereNames(lst, qry, "p.nameNL", "p.nameFR", "p.nameDE", name, nameMatch);
		paginate(lst, qry, NsConverter.postalEncode(afterId));

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.from(lst))
		).transform(PostalInfo::from);
	}
	
	/**
	 * Get all postal infos
	 * 
	 * @return 
	 */
	public Multi<PostalInfo> findPostalInfosAll() {
		SqlPostalInfo qry = new SqlPostalInfo();
		qry.orderById();
		qry.unlimited();

		return multi(
			pg.preparedQuery(qry.build()).execute()
		).transform(PostalInfo::from);
	}

	/**
	 * Find a street by ID
	 * 
	 * @param id full street id
	 * @return street or null
	 */
	public Uni<Street> findStreetById(String id) {
		Tuple tuple = Tuple.of(NsConverter.streetEncode(id));
		SqlStreet qry = new SqlStreet(false, false);
		qry.where("s.identifier =");

		return uni(
			pg.preparedQuery(qry.build()).execute(tuple)
		).transform(row -> row.hasNext() ? Street.from(row.next()) : null);
	}

	/**
	 * Find streets
	 * 
	 * @param afterId search after ID (paginated results)
	 * @param mIdentifier municipality ID
	 * @param nisCode REFNIS code
	 * @param municipalityName
	 * @param pIdentifier postalinfo ID
	 * @param postalCode postal code
	 * @param postalName
	 * @param name (part of) municipality name, searches in NL/FR/DE
	 * @param status
	 * @return 
	 */
	public Multi<Street> findStreets(String afterId, String mIdentifier, String nisCode, String municipalityName,
									String pIdentifier, String postalCode, String postalName,
									String name, String status) {
		boolean joinPostal = Util.oneNotEmpty(postalCode, pIdentifier, postalName);
		boolean joinMunicipality = Util.oneNotEmpty(nisCode, mIdentifier, municipalityName);

		List lst = new ArrayList(15);
		SqlStreet qry = new SqlStreet(joinPostal, joinMunicipality);

		where(lst, qry, "pm.mIdentifier", NsConverter.municipalityEncode(mIdentifier));
		where(lst, qry, "m.refniscode", nisCode);
		where(lst, qry, "ps.pidentifier", NsConverter.postalEncode(pIdentifier));
		where(lst, qry, "p.postalcode", postalCode);
		whereNames(lst, qry, "m.nameNL", "m.nameFR", "m.nameDE", municipalityName, Repository.SEARCH_EXACT);
		whereNames(lst, qry, "p.nameNL", "p.nameFR", "p.nameDE", postalName, Repository.SEARCH_EXACT);
		whereNames(lst, qry, "s.nameNL", "s.nameFR", "s.nameDE", name, Repository.SEARCH_EXACT);

		where(lst, qry, "s.status", status);

		paginate(lst, qry, NsConverter.streetEncode(afterId));

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.from(lst))
		).transform(Street::from);
	}

	/**
	 * Get all streets
	 * 
	 * @return 
	 */
	public Multi<Street> findStreetsAll() {
		SqlStreet qry = new SqlStreet(false, false);
		qry.orderById();
		qry.unlimited();

		return multi(
			pg.preparedQuery(qry.build()).execute()
		).transform(Street::from);
	}
	/**
	 * Find version info
	 * 
	 * @return 
	 */
	public Multi<Version> findVersionInfo() {
		SqlVersion qry = new SqlVersion();
	
		return multi(
			pg.preparedQuery(qry.build()).execute()
		).transform(Version::from);
	}
}
