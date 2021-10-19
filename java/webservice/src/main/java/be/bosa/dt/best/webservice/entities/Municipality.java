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

/**
 * Municipality entity.
 * Note that each municipality has a unique REFNIS code, which is NOT a postal code.
 * Municipalities may have multiple postal codes and postal codes can be shared by different municipalities 
 * 
 * @author Bart Hanssens
 */
public class Municipality extends BestEntity {
	public String niscode;
	public String name_nl;
	public String name_fr;
	public String name_de;

	/**
	 * Convert database result to object
	 * 
	 * @param res database row
	 * @return data object
	 */
	public static Municipality from(Row res) {
		return new Municipality(res.getString(0), res.getString(1), res.getString(2), res.getString(3), res.getString(4));
	}

	/**
	 * Constructor
	 * 
	 * @param identifier
	 * @param niscode
	 * @param name_nl
	 * @param name_fr
	 * @param name_de 
	 */
	public Municipality(String identifier, String niscode, String name_nl, String name_fr, String name_de) {
		this.identifier = NsConverter.municipalityDecode(identifier);
		this.niscode = niscode;
		this.name_nl = name_nl;
		this.name_fr = name_fr;
		this.name_de = name_de;
	}
}
