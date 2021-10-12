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
package be.bosa.dt.best.dbloader;

import be.bosa.dt.best.dao.BestRegion;


/**
 * Convert namespaces into abbreviated form
 * 
 * @author Bart Hanssens
 */
public class NamespaceCoder {
	/**
	 * Abbreviate address
	 * 
	 * @param id
	 * @param reg
	 * @return 
	 */
	public static String address(String id, BestRegion reg) {
		if (id == null || id.isBlank()) {
			return id;
		}

		switch(reg) {
			case BRUSSELS:
				return id.replace("BE.BRUSSELS.BRIC.ADM.ADDR", "BA");
			case FLANDERS:
				return id.replace("https://data.vlaanderen.be/id/adres", "VA");
			case WALLONIA:
				return id.replace("geodata.wallonie.be/id/Address", "WA");
		}
		return id;
	}

	/**
	 * Abbreviate municipality
	 * 
	 * @param id
	 * @param reg
	 * @return 
	 */
	public static String municipality(String id, BestRegion reg) {
		if (id == null || id.isBlank()) {
			return id;
		}

		switch(reg) {
			case BRUSSELS:
				return id.replace("BE.BRUSSELS.BRIC.ADM.MUNICIPALITY", "BM");
			case FLANDERS:
				return id.replace("https://data.vlaanderen.be/id/gemeente", "VM");
			case WALLONIA:
				return id.replace("geodata.wallonie.be/id/Municipality", "WM");
		}
		return id;
	}
	/**
	 * Abbreviate municipality part (only used by Wallonia)
	 * 
	 * @param id
	 * @param reg
	 * @return 
	 */
	public static String municipalityPart(String id, BestRegion reg) {
		if (id == null || id.isBlank()) {
			return id;
		}

		switch(reg) {
			case WALLONIA:
				return id.replace("geodata.wallonie.be/id/PartOfMunicipality", "WPM");
		}
		return id;
	}

	/**
	 * Abbreviate postal info
	 * 
	 * @param id
	 * @param reg
	 * @return 
	 */
	public static String postal(String id, BestRegion reg) {
		if (id == null || id.isBlank()) {
			return id;
		}

		switch(reg) {
			case BRUSSELS:
				return id.replace("BE.BRUSSELS.BRIC.ADM.PZ", "BP");
			case FLANDERS:
				return id.replace("https://data.vlaanderen.be/id/postinfo", "VP");
			case WALLONIA:
				return id.replace("geodata.wallonie.be/id/PostalInfo", "WP");
		}
		return id;
	}

	/**
	 * Abbreviate street name
	 * 
	 * @param id
	 * @param reg
	 * @return 
	 */
	public static String street(String id, BestRegion reg) {
		if (id == null || id.isBlank()) {
			return id;
		}

		switch(reg) {
			case BRUSSELS:
				return id.replace("BE.BRUSSELS.BRIC.ADM.STR", "BS");
			case FLANDERS:
				return id.replace("https://data.vlaanderen.be/id/straatnaam", "VS");
			case WALLONIA:
				return id.replace("geodata.wallonie.be/id/StreetName", "WS");
		}
		return id;
	}
}
