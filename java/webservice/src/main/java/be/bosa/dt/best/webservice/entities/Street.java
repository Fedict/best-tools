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
import io.vertx.mutiny.sqlclient.Row;
import java.time.OffsetDateTime;

/**
 * Street entity
 * 
 * @author Bart Hanssens
 */
public class Street extends BestEntity {
	public String mIdentifier;
	public String nameNL;
	public String nameFR;
	public String nameDE;
	public OffsetDateTime validFrom;
	public OffsetDateTime validTo;
	public String status;

	/**
	 * Convert database result to object
	 * 
	 * @param res database row
	 * @return data object
	 */
	public static Street from(Row res) {
		return new Street(res.getString(0), 
			res.getString(1), res.getString(2), res.getString(3), res.getString(4), 
			res.getOffsetDateTime(5), res.getOffsetDateTime(6), res.getString(7));
	}

	/**
	 * Constructor
	 * 
	 * @param identifier 
	 */
	public Street(String identifier) {
		this.id = NsConverter.streetDecode(identifier);
	}
	
	/**
	* Constructor
	* 
	 * @param identifier
	 * @param mIdentifier
	 * @param nameNL
	 * @param nameFR
	 * @param nameDE
	 * @param validFrom
	 * @param validTo
	 * @param status
	*/
	public Street(String identifier, 
				String mIdentifier, String nameNL, String nameFR, String nameDE,
				OffsetDateTime validFrom, OffsetDateTime validTo, String status) {
		this.id = NsConverter.streetDecode(identifier);
		this.mIdentifier = NsConverter.municipalityDecode(mIdentifier);
		this.nameNL = nameNL;
		this.nameFR = nameFR;
		this.nameDE = nameDE;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.status = status;
	}
}
