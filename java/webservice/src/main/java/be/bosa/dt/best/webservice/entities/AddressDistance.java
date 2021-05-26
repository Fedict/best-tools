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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import java.util.Map;
import java.util.Optional;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hibernate.annotations.Filter;

/**
 * Address with distance (in meters).
 * 
 * Spatialite requires some special querying,
 * to increase performance a "search frame" buffer with a 0.075 degree radius is created
 * (meters are not directly supported in buffer)
 * 
 * Also, don't use the spheroid for calculating distance. 
 * It will be 1-2% less accurate, but given the accuracy of GPS coordinates this is not a issue
 * 
 * @author Bart Hanssens
 */
@Entity
@JsonIgnoreProperties({"id"})
@NamedQuery(name = "spatialite", 
			query = "SELECT NEW AddressDistance(a, " +
				"DISTANCE(a.geom, MakePoint(:posx, :posy, 4326), 0) as distance) " +
				"FROM Addresses AS a " +
				"WHERE PtDistWithin(a.geom, MakePoint(:posx, :posy, 4326), :maxdist, 0) = TRUE " + 
				"AND a.rowid IN ( " +
					"SELECT s.rowid " +
					"FROM SpatialIndex AS s " +
					"WHERE f_table_name = 'addresses' " + 
					"AND search_frame = Buffer(MakePoint(:posx, :posy, 4326), 0.075) )" +
				"ORDER by distance")
@NamedQuery(name = "postgis", 
			query = "SELECT NEW AddressDistance(a, " +
				"DISTANCE(a.geom, ST_SetSRID(ST_MakePoint(:posx, :posy), 4326)) as distance) " +
				"FROM Addresses a " +
				"WHERE DWITHIN(a.geom, ST_SetSRID(ST_MakePoint(:posx, :posy), 4326), :maxdist) = TRUE " +
				"ORDER by distance")
@Filter(name="status", condition="status = :status") 
public class AddressDistance extends PanacheEntity {
	private final static String db = ConfigProvider.getConfig().getValue("quarkus.datasource.db-kind", String.class);

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
	 * Find all addresses within a range of X meters, based on GPS/WGS84 coordinates
	 * 
	 * @param posx
	 * @param posy
	 * @param maxdist maximum distance (in meters)
	 * @param status
	 * @return 
	 */
	public static PanacheQuery<AddressDistance> findNearestByGPS(double posx, double posy, int maxdist, 
			Optional<String> status) {
		String qry = db.equals("other") ? "#spatialite" : "#postgis";
		PanacheQuery<AddressDistance> res = find(qry, Map.of("posx", posx, "posy", posy, "maxdist", maxdist));
		return status.isPresent() ? res.filter("#status", Map.of("status", status)) : res;
	}
}
