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
package be.bosa.dt.best.webservice.entities;

import io.vertx.mutiny.sqlclient.Row;
import java.time.OffsetDateTime;


import org.geolatte.geom.Point;

/**
 * Full address entity
 *
 * @author Bart Hanssens
 */
public class Address {
	public final static String BY_ID = 
		"SELECT a.identifier, a.sIdentifier, a.mIdentifier, a.pIdentifier, a.mpIdentifier, " +
				"a.housenumber, a.boxnumber, " +
				"a.validFrom, a.validTo, a.status, a.point " +
		"FROM addresses a " +
		"WHERE a.identifier = $1";

	public String identifier;
	public String sIdentifier;
	public String mIdentifier;
	public String pIdentifier;
	public String mpIdentifier;
	public String housenumber;
	public String boxnumber;
	public String status;
	public OffsetDateTime validFrom;
	public OffsetDateTime validTo;
	public Point point;

	/**
	 * Convert database result to object
	 * 
	 * @param res database row
	 * @return data object
	 */
	public static Address from(Row res) {
		return new Address(
			res.getString(0), 
			res.getString(1), res.getString(2), res.getString(3), res.getString(4), 
			res.getString(5), res.getString(6),
			res.getOffsetDateTime(7), res.getOffsetDateTime(8), res.getValue(9), res.getString(10));
	}

	/**
	 * Constructor
	 * 
	 * @param identifier
	 * @param sIdentifier
	 * @param mIdentifier
	 * @param pIdentifier
	 * @param mpIdentifier
	 * @param housenumber
	 * @param boxnumber
	 * @param validFrom
	 * @param validTo
	 * @param point
	 * @param status 
	 */
	public Address(String identifier, 
					String sIdentifier, String mIdentifier, String pIdentifier, String mpIdentifier,
					String housenumber, String boxnumber, 
					OffsetDateTime validFrom, OffsetDateTime validTo, Object point, String status) {
		this.identifier = identifier;
		this.sIdentifier = sIdentifier;
		this.mIdentifier = mIdentifier;
		this.pIdentifier = pIdentifier;
		this.mpIdentifier = mpIdentifier;
		this.housenumber = housenumber;
		this.boxnumber = boxnumber;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.point = (Point) point;
		this.status = status;
	}
}
