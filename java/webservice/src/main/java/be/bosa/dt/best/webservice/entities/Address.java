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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.mutiny.sqlclient.Row;


import org.geolatte.geom.Point;

/**
 * Full address entity
 *
 * @author Bart Hanssens
 */
public class Address {
	public String id;

	public Municipality municipality;
	public String part_id;
	public Street street;
	public Postal postal;

	@JsonProperty("houseNumber")
	public String houseno;
	@JsonProperty("poBox")
	public String boxno;

	public double x;
	public double y;

	public Point geom;
	
	public String status;

	/**
	 * Convert database result to object
	 * 
	 * @param res database row
	 * @return data object
	 */
	public static Address from(Row res) {
		return new Address(
			res.getString(0), res.getString(1), res.getString(2), res.getString(3), 
			res.getDouble(4), res.getDouble(5), null, res.getString(7),
			res.getString(8), res.getString(9), res.getString(10), res.getString(11),
			res.getString(12), res.getString(13), res.getString(14), res.getString(15), res.getString(16),
			res.getString(17), res.getString(18), res.getString(19), res.getString(20), res.getString(21));
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param part_id
	 * @param houseno
	 * @param boxno
	 * @param x
	 * @param y
	 * @param geom
	 * @param status 
	 * @param s_id 
	 * @param s_name_nl 
	 * @param s_name_fr 
	 * @param s_name_de 
	 * @param m_id 
	 * @param m_niscode 
	 * @param m_name_nl 
	 * @param m_name_fr 
	 * @param m_name_de 
	 * @param p_id 
	 * @param p_zipcode 
	 * @param p_name_fr 
	 * @param p_name_nl 
	 * @param p_name_de 
	 */
	public Address(String id, String part_id, String houseno, String boxno, 
					double x, double y, Object geom, String status,
					String s_id, String s_name_nl, String s_name_fr, String s_name_de,
					String m_id, String m_niscode, String m_name_nl, String m_name_fr, String m_name_de,
					String p_id, String p_zipcode, String p_name_nl, String p_name_fr, String p_name_de) {
		this.id = id;
		this.part_id = part_id;
		this.houseno = houseno;
		this.boxno = boxno;
		this.x = x;
		this.y = y;
		this.geom = (Point) geom;
		this.status = status;
		this.municipality = new Municipality(m_id, m_niscode, m_name_nl, m_name_fr, m_name_de);
		this.street = new Street(s_id, s_name_nl, s_name_fr, s_name_de);
		this.postal = new Postal(p_id, p_zipcode, p_name_nl, p_name_fr, p_name_de);
	}
}
