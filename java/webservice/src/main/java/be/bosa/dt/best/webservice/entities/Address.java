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
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.QueryHint;

import org.geolatte.geom.Geometry;
import org.hibernate.annotations.Filter;


/**
 * Full address entity
 * 
 * @author Bart Hanssens
 */
@Entity(name = "Addresses")
@NamedQueries({
@NamedQuery(name = "withdistance", 
			query = "SELECT NEW be.bosa.dt.best.webservice.entities.AddressDistance(a, " +
				"DISTANCE(a.geom, MakePoint(:posx, :posy, 4326), 0) as distance) " +
				"FROM Addresses AS a " +
				"WHERE PtDistWithin(a.geom, MakePoint(:posx, :posy, 4326), :maxdist, 0) = TRUE " + 
				"AND a.rowid IN ( " +
					"SELECT s.rowid " +
					"FROM SpatialIndex AS s " +
					"WHERE f_table_name = 'addresses' " + 
					"AND search_frame = Buffer(MakePoint(:posx, :posy, 4326), 0.055) )",
			hints = { 
				@QueryHint(name = "org.hibernate.readOnly", value="true"),
				@QueryHint(name = "org.hibernate.batchSize ", value="10")
			}),
@NamedQuery(name = "withoutdistance", 
			query = "SELECT a " +
				"FROM Addresses AS a " +
				"WHERE PtDistWithin(a.geom, MakePoint(:posx, :posy, 4326), :maxdist, 0) = TRUE " + 
				"AND a.rowid IN ( " +
					"SELECT s.rowid " +
					"FROM SpatialIndex AS s " +
					"WHERE f_table_name = 'addresses' " + 
					"AND search_frame = Buffer(MakePoint(:posx, :posy, 4326), 0.055) )",
			hints = { 
				@QueryHint(name = "org.hibernate.readOnly", value="true")
			}),
})
@Filter(name="status", condition="status = :status")
@JsonIgnoreProperties("rowid")
public class Address extends PanacheEntityBase {
	public long rowid;

	@Id public String id;
	
	@OneToOne
	@JoinColumn(name = "city_id", referencedColumnName = "id")
	public Municipality municipality;
	public String part_id;
	
	@OneToOne
	@JoinColumn(name = "street_id", referencedColumnName = "id")
	public Street street;

	@OneToOne
	@JoinColumn(name = "postal_id", referencedColumnName = "id")
	public Postal postal;

	@JsonProperty("houseNumber")
	public String houseno;
	@JsonProperty("poBox")
	public String boxno;

	public double l72x;
	public double l72y;

	public Geometry geom;
	
	public String status;
	
	/**
	 * Find an address by ID and optionally filter on status
	 * 
	 * @param id id
	 * @param status
	 * @return 
	 */
	public static Address findByIdAndStatus(String id, Optional<String> status) {
		Address adr = findById(id);
		if (status.isPresent() && adr != null) {
			return adr.status.equals(status.get()) ? adr : null;
		}
		return adr;
	}
	
	/**
	 * Find all addresses within a range of X meters, based on GPS/WGS84 coordinates + calculate distance
	 * 
	 * @param posx
	 * @param posy
	 * @param maxdist maximum distance (in meters)
	 * @param status
	 * @return 
	 */
	public static List<AddressDistance> findNearestWithDistance(double posx, double posy, int maxdist, 
			Optional<String> status) {
		PanacheQuery<PanacheEntityBase> res = find("#withdistance", 
			Map.of("posx", posx, "posy", posy, "maxdist", maxdist));
		return status.isPresent() ? res.filter("#status", Map.of("status", status)).list() : res.list();
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
	public static List<Address> findNearest(double posx, double posy, int maxdist, Optional<String> status) {
		PanacheQuery<Address> res = find("#withoutdistance", 
			Map.of("posx", posx, "posy", posy, "maxdist", maxdist));
		return status.isPresent() ? res.filter("#status", Map.of("status", status)).list() : res.list();
	}
}
