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

import be.bosa.dt.best.webservice.NsConverter;
import be.bosa.dt.best.webservice.serializers.AddressSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.pgclient.data.Point;

import java.time.OffsetDateTime;



/**
 * Full address entity
 *
 * @author Bart Hanssens
 */
@JsonSerialize(using = AddressSerializer.class)
public class Address extends BestEntity {
	public String mIdentifier;
	public String sIdentifier;
	public String pIdentifier;
	public String mpIdentifier;
	public String housenumber;
	public String boxnumber;
	public String status;
	public OffsetDateTime validFrom;
	public OffsetDateTime validTo;
	public Point point;
	public Street street;

	/**
	 * Convert database result to object
	 * 
	 * @param res database row
	 * @return data object
	 */
	public static Address from(Row res) {
		Address addr = new Address(
				res.getString(0), 
				res.getString(1), res.getString(2), res.getString(3), res.getString(4), 
				res.getString(5), res.getString(6),
				res.getOffsetDateTime(7), res.getOffsetDateTime(8), res.getString(9), 
				res.get(Point.class, 10));
		if (res.size() > 11) {
			addr.street = new Street(res.getString(4), res.getString(11),
							res.getString(12), res.getString(13), res.getString(14),
							res.getOffsetDateTime(15), res.getOffsetDateTime(16), res.getString(17));
		}
		return addr;	
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
					String mIdentifier, String pIdentifier, String mpIdentifier,
					String sIdentifier, String housenumber, String boxnumber, 
					OffsetDateTime validFrom, OffsetDateTime validTo, String status,
					Point point) {
		this.id = NsConverter.addressDecode(identifier);
		this.mIdentifier = NsConverter.municipalityDecode(mIdentifier);
		this.sIdentifier = NsConverter.streetDecode(sIdentifier);
		this.pIdentifier = NsConverter.postalDecode(pIdentifier);
		this.mpIdentifier = mpIdentifier;
		this.housenumber = housenumber;
		this.boxnumber = boxnumber;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.point = point;
		this.status = status;
	}
}
