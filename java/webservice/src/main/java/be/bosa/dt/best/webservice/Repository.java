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
import be.bosa.dt.best.webservice.entities.Street;
import be.bosa.dt.best.webservice.queries.Sql;
import be.bosa.dt.best.webservice.queries.SqlAddress;
import be.bosa.dt.best.webservice.queries.SqlGeo;
import be.bosa.dt.best.webservice.queries.SqlStreet;

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


/**
 * Connect to database
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
public class Repository {
	protected static int LIMIT = 100;

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
		lst.add(LIMIT);
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
	 * @return address or null
	 */
	public Uni<Address> findAddressById(String id) {
		Tuple tuple = Tuple.of(NsConverter.addressEncode(id));
		SqlAddress qry = new SqlAddress();
		qry.where("identifier");

		return uni(
			pg.preparedQuery(qry.build()).execute(tuple)
		).transform(row -> row.hasNext() ? Address.from(row.next()) : null);
	}
	
	/**
	 * Find a municipality by ID
	 * 
	 * @param id municipality id
	 * @return municipality or null
	 */
	public Uni<Municipality> findMunicipalityById(String id) {
		Tuple tuple = Tuple.of(NsConverter.municipalityEncode(id));
		SqlStreet qry = new SqlStreet();
		qry.where("identifier");

		return uni(
			pg.preparedQuery(qry.build()).execute(tuple)
		).transform(row -> row.hasNext() ? Municipality.from(row.next()) : null);
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
		qry.where("identifier");

		return uni(
			pg.preparedQuery(qry.build()).execute(tuple)
		).transform(row -> row.hasNext() ? Street.from(row.next()) : null);
	}

	/**
	 * Find address by different parameters
	 * 
	 * @param startId
	 * @param mIdentifier
	 * @param sIdentifier
	 * @param houseNumber
	 * @param boxNumber
	 * @return 
	 */
	public Multi<Address> findAddresses(String startId, String mIdentifier, String sIdentifier, 
										String houseNumber, String boxNumber) {
		List lst = new ArrayList();
		SqlAddress qry = new SqlAddress();

		paginate(lst, qry, NsConverter.addressEncode(startId));
		where(lst, qry, "mIdentifier =", NsConverter.municipalityEncode(mIdentifier));
		where(lst, qry, "sIdentifier =", NsConverter.streetEncode(sIdentifier));
		where(lst, qry, "houseNumber =", houseNumber);
		where(lst, qry, "boxNumber =", boxNumber);

		qry.order();

		return multi(
			pg.preparedQuery(qry.build()).execute(Tuple.from(lst))
		).transform(Address::from);
	}

	public Multi<Address> findByCoordinates(int x, int y, int meters, int limit, String startId) {
		Tuple tuple;
		SqlGeo qry = new SqlGeo();
		if (startId != null) {
			qry.paginate();
			tuple = Tuple.of(x, y, meters, limit, startId);
		} else {
			qry.limit();
			tuple = Tuple.of(x, y, meters, limit);
		}

		return multi(
			pg.preparedQuery(qry.build()).execute(tuple)
		).transform(Address::from);
	}
	
	/**
	 * Find the address by ID
	 * 
	 * @param id full address id
	 * @return address
	 *//*
	public Multi<Address> findAddressByCriteria(Map maps, int limit, String startIdentifier) {
		return multi(
			pg.preparedQuery(Address.SQL_ADDRESS_ID).execute()
		).transform(Address::from);
	}*/
}
