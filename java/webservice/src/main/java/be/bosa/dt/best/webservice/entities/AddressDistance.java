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

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import java.util.List;
import java.util.Locale;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Address with distance (in meters)
 * 
 * @author Bart Hanssens
 */
@Entity
public class AddressDistance extends PanacheEntity {
	@Transient
	public Address address;
	@Transient
	public double distance;

	public AddressDistance() {
		
	}
	public AddressDistance(Address address, double distance) {
		this.address = address;
		this.distance = distance;
	}

	/**
	 * Find nearest address based on GPS/WGS84 coordinates
	 * 
	 * @param posx
	 * @param posy
	 * @return 
	 */
	public static List<AddressDistance> findNearestByGPS(double posx, double posy) {
		// make sure to use a '.' as decimal separator
		String point = String.format(Locale.US, 
									"ST_GeomFromText('POINT(%f %f)', 4326)", 
									posx, posy);

		String qry = String.format("SELECT NEW AddressDistance(a, " +
				"DISTANCE(a.geom, %s) as distance) " +
				"FROM Addresses a " +
				"WHERE DWITHIN(a.geom, %s, 100) = TRUE " +
				"ORDER BY distance", point, point);

		return find(qry).list();
	}
}
