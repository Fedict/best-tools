/*
 * Copyright (c) 2018, FPS BOSA DG DT
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
package be.bosa.dt.best.dao;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 * Helper class for street names
 * 
 * @author Bart Hanssens
 */
public class Street extends BestNamedObject {
	private final Municipality city = new Municipality();
	private String status = "";
	private OffsetDateTime fromDate;
	private OffsetDateTime tillDate;
	private OffsetDateTime beginLife;
	private OffsetDateTime endLife;

	public Municipality getCity() {
		return city;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	private OffsetDateTime parseDate(String str) {
		try {
			return OffsetDateTime.parse(str);
		} catch (DateTimeParseException dte) {
			return null;
		}
	}
	
	public void setFromDate(String date) {
		this.fromDate = parseDate(date);
	}
	
	public OffsetDateTime getFromDate() {
		return fromDate;
	}

	public void setTillDate(String date) {
		this.tillDate = parseDate(date);
	}

	public OffsetDateTime getTillDate() {
		return tillDate;
	}

	public void setBeginLife(String date) {
		this.beginLife = parseDate(date);
	}

	public OffsetDateTime getBeginLife() {
		return beginLife;
	}

	public void setEndLife(String date) {
		this.endLife = parseDate(date);
	}

	public OffsetDateTime getEndLife() {
		return endLife;
	}

	public Street() {
	}
}