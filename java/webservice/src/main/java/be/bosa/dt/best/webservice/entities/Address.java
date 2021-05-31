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
import javax.persistence.Column;

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
			query = "SELECT NEW be.bosa.dt.best.webservice.entities.AddressDistance( " +
					"a.rowid, a.id, a.part_id, a.houseno, a.boxno, a.x, a.y, a.geom, a.status, " +
					"s.id, s.name_nl, s.name_fr, s.name_de, " +
					"m.id, m.niscode, m.name_nl, m.name_fr, m.name_de, " +
					"p.id, p.zipcode, p.name_nl, p.name_fr, p.name_de, " +
				"ST_DISTANCE(a.geom, ST_Transform(ST_SetSRID(ST_MakePoint(:posx, :posy), 4326), 31370)) as distance) " +
				"FROM Addresses a " +
				"INNER JOIN a.street s " +
				"INNER JOIN a.municipality m " +
				"INNER JOIN a.postal p " +
				"WHERE ST_DWithin(a.geom, ST_Transform(ST_SetSRID(ST_MakePoint(:posx, :posy), 4326), 31370), :maxdist) = TRUE " + 
				"ORDER by distance",
			hints = { 
				@QueryHint(name = "org.hibernate.readOnly", value="true")
			}),
@NamedQuery(name = "withoutdistance", 
			query = "SELECT a " +
				"FROM Addresses AS a " +
				"WHERE ST_DWithin(a.geom, ST_Transform(ST_SetSRID(ST_MakePoint(:posx, :posy), 4326), 31370), :maxdist) = TRUE ",
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

	public double x;
	public double y;

	@Column(columnDefinition = "geometry(Geometry,4326)")
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
			Map.of("posx", posx, "posy", posy, "maxdist", maxdist, "degrees", (double) maxdist / 70100));
		return status.isPresent() ? res.filter("#status", Map.of("status", status)).list() : res.list();
	}

	public Address() {
	}

	/**
	 * Constructor, only needed for N+1 select work-around
	 * 
	 * @param rowid
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
	public Address(long rowid, String id, String part_id, String houseno, String boxno, 
					double x, double y, Geometry geom, String status,
					String s_id, String s_name_nl, String s_name_fr, String s_name_de,
					String m_id, String m_niscode, String m_name_nl, String m_name_fr, String m_name_de,
					String p_id, String p_zipcode, String p_name_nl, String p_name_fr, String p_name_de) {
		this.rowid = rowid;
		this.id = id;
		this.part_id = part_id;
		this.houseno = houseno;
		this.boxno = boxno;
		this.x = x;
		this.y = y;
		this.geom = geom;
		this.status = status;
		this.municipality = new Municipality(m_id, m_niscode, m_name_nl, m_name_fr, m_name_de);
		this.street = new Street(s_id, s_name_nl, s_name_fr, s_name_de);
		this.postal = new Postal(p_id, p_zipcode, p_name_nl, p_name_fr, p_name_de);
	}
}
