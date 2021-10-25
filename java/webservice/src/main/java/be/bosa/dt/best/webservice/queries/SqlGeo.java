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
 * Select from addresses based on geo-location
 * 
 * @author Bart Hanssens
 */
public class SqlGeo extends Sql {
	
	public SqlGeo(boolean embed) {
		this.select = "a.identifier, a.mIdentifier, a.pIdentifier, a.mpIdentifier, a.sIdentifier, " +
						" a.housenumber, a.boxnumber, a.validFrom, a.validTo, a.status::text, a.point::point";
		this.from = "address";
		this.alias = "a";
		this.where = "ST_DWITHIN(a.point, ST_SetSRID(ST_MakePoint($1, $2), 31370), $3) = TRUE";
		this.vars = 3;
		
		if (embed) {
			this.select += ", s.mIdentifier, s.nameNL, s.nameFR, s.nameDE, s.validFrom, s.validTo, s.status::text";
			this.join = " INNER JOIN street s ON a.sIdentifier = s.identifier";			
		}
	}
}
