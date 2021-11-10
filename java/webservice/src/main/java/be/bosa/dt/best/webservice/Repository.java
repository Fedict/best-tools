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

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.MultiOnItem;
import io.smallrye.mutiny.groups.UniOnItem;

import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;

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
	protected static int PAGE_LIMIT = 100;

	@Inject
	PgPool pg;

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
	}

	/**
	 * Add non-empty fields to SQL where clause
	 * 
	 * @param lst list of SQL parameter values
	 * @param qry SQL query
	 * @param field field name (+ " = " operator for equal to value)
	 * @param value value
	 */
	private static void where(List lst, Sql qry, String field, String value) {
		if (value != null && !value.isEmpty()) {
			qry.where(field);
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
		SqlAddress qry = new SqlAddress(embed);
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
	 * @param sIdentifier street identifier
	 * @param pIdentifier postal identifier
	 * @param houseNumber house number
	 * @param boxNumber box number
	 * @param limit maximum number of results
	 * @param embed embed street, postal etc or not
	 * @return 
	 */
	public Multi<Address> findAddresses(String afterId, String mIdentifier, String sIdentifier, 
										String pIdentifier,
										String houseNumber, String boxNumber, int limit,
										boolean embed) {
		List lst = new ArrayList(8);
		SqlAddress qry = new SqlAddress(embed);

		paginate(lst, qry, NsConverter.addressEncode(afterId));
		where(lst, qry, "a.mIdentifier =", NsConverter.municipalityEncode(mIdentifier));
		where(lst, qry, "a.sIdentifier =", NsConverter.streetEncode(sIdentifier));
		where(lst, qry, "a.pIdentifier =", NsConverter.postalEncode(pIdentifier));
		where(lst, qry, "a.houseNumber =", houseNumber);
		where(lst, qry, "a.boxNumber =", boxNumber);

		if (limit > 0) {
			qry.limit();
			lst.add(limit);
		}
		qry.orderById();

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.from(lst))
		).transform(Address::from);
	}

	/**
	 * Find addresses by GPS coordinates
	 * 
	 * @param afterId search after ID (paginated results)
	 * @param gpsx
	 * @param gpsy
	 * @param meters
	 * @param limit
	 * @param embed embed street, postal etc or not
	 * @return 
	 */
	public Multi<Address> findByCoordinates(String afterId, double gpsx, double gpsy, int meters, int limit, boolean embed) {
		Coordinate l72 = CoordConverter.gpsToL72(gpsx, gpsy);
		List lst = new ArrayList<>(6); 
		lst.add(l72.x);
		lst.add(l72.y);
		lst.add(meters);

		SqlGeo qry = new SqlGeo(embed);
		paginate(lst, qry, NsConverter.addressEncode(afterId));

		if (limit > 0) {
			qry.limit();
			lst.add(limit);
		}
		qry.orderById();

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
	 * @param niscode REFNIS code
	 * @return municipalities or null
	 */
	public Multi<Municipality> findMunicipalitiesByNis(String niscode) {
		SqlMunicipality qry = new SqlMunicipality(false);
		qry.where("m.refniscode =");

		qry.orderById();

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.of(niscode))
		).transform(Municipality::from);
	}

	/**
	 * Find municipalities by postal code (bPost)
	 * 
	 * @param postalcode postal code
	 * @return municipalities or null
	 */
	public Multi<Municipality> findMunicipalitiesByPostal(String postalcode) {
		SqlMunicipality qry = new SqlMunicipality(true);
		qry.where("p.postalcode =");

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.of(postalcode))
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
	 * @return 
	 */
	public Multi<MunicipalityPart> findMunicipalityParts(String afterId) {
		List lst = new ArrayList(2);
		SqlMunicipalityPart qry = new SqlMunicipalityPart();

		paginate(lst, qry, NsConverter.municipalityPartEncode(afterId));
		qry.orderById();

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
	 * @return 
	 */
	public Multi<PostalInfo> findPostalInfos(String afterId) {
		List lst = new ArrayList(2);
		SqlPostalInfo qry = new SqlPostalInfo();

		paginate(lst, qry, NsConverter.postalEncode(afterId));
		qry.orderById();

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
		SqlStreet qry = new SqlStreet();
		qry.where("s.identifier =");

		return uni(
			pg.preparedQuery(qry.build()).execute(tuple)
		).transform(row -> row.hasNext() ? Street.from(row.next()) : null);
	}

	/**
	 * Find streets
	 * 
	 * @param afterId search after ID (paginated results)
	 * @return 
	 */
	public Multi<Street> findStreets(String afterId) {
		List lst = new ArrayList(2);
		SqlStreet qry = new SqlStreet();

		paginate(lst, qry, NsConverter.streetEncode(afterId));
		qry.orderById();

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.from(lst))
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
