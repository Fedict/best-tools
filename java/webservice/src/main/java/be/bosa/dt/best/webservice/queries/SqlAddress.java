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
package be.bosa.dt.best.webservice.queries;

/**
 * Select from address table
 * 
 * @author Bart Hanssens
 */
public class SqlAddress extends Sql {
	/**
	 * Join with street table
	 */
	public void joinStreet() {
		this.join += " INNER JOIN street s ON a.sIdentifier = s.identifier";
	}
	
	/**
	 * Join with municipality table
	 */
	public void joinMunicipality() {
		this.join += " INNER JOIN municipality m ON a.mIdentifier = m.identifier";
	}

	/**
	 * Join with postal info table
	 */
	public void joinPostal() {
		this.join += " INNER JOIN postalinfo p ON a.pIdentifier = p.identifier";
	}

	/**
	 * Adapt query to search in a radius from a specific point
	 */
	public void point() {
		this.where = "ST_DWITHIN(a.point, ST_SetSRID(ST_Point($1, $2), 31370), $3)";
		this.vars = 3;	
	}

	/**
	 * Adapt query to search within a polygon
	 */
	public void polygon() {
		this.where = "ST_WITHIN(a.point, ST_SetSRID(ST_GeomFromText($1), 31370))";
		this.vars = 1;	
	}

	/**
	 * Constructor
	 */
	public SqlAddress() {
		this.select = "a.identifier, a.mIdentifier, a.pIdentifier, a.mpIdentifier, a.sIdentifier, " +
						" a.housenumber, a.boxnumber, a.validFrom, a.validTo, a.status::text, a.point::point";
		this.from = "address";
		this.alias = "a";
	}
}
