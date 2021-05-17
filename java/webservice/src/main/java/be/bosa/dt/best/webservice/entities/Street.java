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
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Bart Hanssens
 */
@Entity(name = "Streets")
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Street extends PanacheEntityBase {
	@Id public String id;
	public String city_id;
	public String name_nl;
	public String name_fr;
	public String name_de;
	
	/**
	 * Find by postal code, using auxiliary table
	 * 
	 * @param postal zip code
	 * @param name (part of)
	 * @return street query
	 */
	public static PanacheQuery<Street> findByZipcode(String postal, Optional<String> name) {
		if (!name.isPresent()) {
			return find("SELECT s " + 
				"FROM PostalStreets AS ps " + 
				"INNER JOIN ps.street as s " +
				"WHERE ps.zipcode = ?1", postal);
		}

		String str = '%' + name.get() + '%';
		return find("SELECT s " + 
				"FROM PostalStreets AS ps " + 
				"INNER JOIN ps.street as s " +
				"WHERE ps.zipcode = ?1 " +
				"AND (name_nl LIKE ?2 OR name_fr LIKE ?2 OR name_de LIKE ?2)", postal, str);
	}

	/**
	 * Find by REFNIS code
	 * 
	 * @param niscode REFNIS code
	 * @param name (part of) name
	 * @return street query
	 */
	public static PanacheQuery<Street> findByNiscode(String niscode, Optional<String> name) {
		if (!name.isPresent()) {
			return find("nis", niscode);
		}
		String str = '%' + name.get() + '%';
		return find("FROM Streets WHERE nis = ?1 "
					+ "WHERE (name_nl LIKE ?2 OR name_fr LIKE ?2 OR name_de LIKE ?2)", niscode, str);
	}
}
