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

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;


/**
 * Address with distance (in meters).
 * 
 * @author Bart Hanssens
 */
public class AddressDistance extends PanacheEntityBase {
	public Address address;
	public double distance;
	
	public AddressDistance(Address address, double distance) {
		this.address = address;
		this.distance = distance;
	}
	
	/**
	 * Constructor, only needed for N+1 select work-around
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
					double l72x, double l72y, String status,
					String s_id, String s_name_nl, String s_name_fr, String s_name_de,
					String m_id, String m_niscode, String m_name_nl, String m_name_fr, String m_name_de,
					String p_id, String p_zipcode, String p_name_nl, String p_name_fr, String p_name_de,
					double distance) {
		this(new Address(id, part_id, houseno, boxno, l72x, l72y, status,
						s_id, s_name_nl, s_name_fr, s_name_de,
						m_id, m_niscode, m_name_nl, m_name_fr, m_name_de,
						p_id, p_zipcode, p_name_nl, p_name_fr, p_name_de), 
			distance);
	}
}
