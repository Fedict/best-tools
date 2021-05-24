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
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.geolatte.geom.Geometry;
import org.hibernate.annotations.Filter;


/**
 * Full address entity
 * 
 * @author Bart Hanssens
 */
@Entity(name = "Addresses")
@Filter(name="status", condition="status = :status") 
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
}
