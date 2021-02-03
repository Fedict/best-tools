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

/**
 * Helper class for addresses
 * 
 * @author Bart Hanssens
 */
public class Address extends BestObject {
	private final Geopoint point = new Geopoint();
	private final Municipality city = new Municipality();
	private final Municipality cityPart = new Municipality();
	private final Postal postal = new Postal();
	private final Street street = new Street();
	private String number = "";
	private String box = "";
	private String status = "";

	/**
	 * Get the geo-position
	 * 
	 * @return geopoint object
	 */
	public Geopoint getPoint() {
		return point;
	}
	
	/**
	 * Get the municipality / city of the address
	 * 
	 * @return municipality object
	 */
	public Municipality getCity() {
		return city;
	}

	/**
	 * Get the municipality part of the address
	 * 
	 * @return municipality object
	 */
	public Municipality getCityPart() {
		return cityPart;
	}
	
	/**
	 * Get the postal info of the address
	 * 
	 * @return postal object
	 */
	public Postal getPostal() {
		return postal;
	}
	
	/**
	 * Get the street of the address
	 * 
	 * @return street object
	 */
	public Street getStreet() {
		return street;
	}
	
	/**
	 * Get the house number of the address as string
	 * 
	 * @return house number
	 */
	public String getNumber() {
		return number;
	}
	
	/**
	 * Set the house number of the address
	 * 
	 * @param number house number
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Get the box number, if any
	 * 
	 * @return box number
	 */
	public String getBox() {
		return box;
	}
	
	/**
	 * Set the box number, if any
	 * 
	 * @param box box number
	 */
	public void setBox(String box) {
		this.box = box;
	}
	
	/**
	 * Get the status of this address
	 * 
	 * @return string
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Set the status of thie address
	 * 
	 * @param status 
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Constructor
	 */
	public Address() {
	}
}