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
import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.locationtech.jts.geom.Point;

/**
 *
 * @author Bart Hanssens
 */
@Entity(name = "Addresses")
public class Address extends PanacheEntityBase {
	@Id public String id;
	
	@OneToOne(optional = true)
	@JoinColumn(name = "city_id", referencedColumnName = "id")
	@NotFound(action = NotFoundAction.IGNORE)
	public Municipality municipality;
	public String part_id;
	
	@OneToOne(optional = true)
	@JoinColumn(name = "street_id", referencedColumnName = "id")
	@NotFound(action = NotFoundAction.IGNORE)
	public Street street;

	@OneToOne(optional = true)
	@JoinColumn(name = "postal_id", referencedColumnName = "id")
	@NotFound(action = NotFoundAction.IGNORE)
	public Postal postal;

	public String houseno;
	public String boxno;

	@Column(columnDefinition = "geometry")
	public Point geom;
}
