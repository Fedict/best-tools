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


/**
 * Address with distance (in meters).
 * 
 * @author Bart Hanssens
 */
public class AddressDistance {
	public Address address;
	public double distance;

	/**
	 * Convert database result to object
	 * 
	 * @param res database row
	 * @return data object
	 */
	public static AddressDistance from(Row res) {
		return new AddressDistance(
			res.getString(0), res.getString(1), res.getString(2), res.getString(3), 
			res.getDouble(4), res.getDouble(5), null, res.getString(7),
			res.getString(8), res.getString(9), res.getString(10), res.getString(11),
			res.getString(12), res.getString(13), res.getString(14), res.getString(15), res.getString(16),
			res.getString(17), res.getString(18), res.getString(19), res.getString(20), res.getString(21),
			res.getDouble(22));
	}

	/**
	 * Constructor
	 * 
	 * @param address
	 * @param distance 
	 */
	public AddressDistance(Address address, double distance) {
		this.address = address;
		this.distance = distance;
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param part_id
	 * @param houseno
	 * @param boxno
	 * @param l72x
	 * @param l72y
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
	 * @param distance 
	 */
	public AddressDistance(String id, String part_id, String houseno, String boxno, 
					double l72x, double l72y, Object geom, String status,
					String s_id, String s_name_nl, String s_name_fr, String s_name_de,
					String m_id, String m_niscode, String m_name_nl, String m_name_fr, String m_name_de,
					String p_id, String p_zipcode, String p_name_nl, String p_name_fr, String p_name_de,
					double distance) {
		this.address = new Address(id, part_id, houseno, boxno, l72x, l72y, geom, status,
						s_id, s_name_nl, s_name_fr, s_name_de,
						m_id, m_niscode, m_name_nl, m_name_fr, m_name_de,
						p_id, p_zipcode, p_name_nl, p_name_fr, p_name_de);
		this.distance = distance;
	}
}
