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

/**
 * Convert namespaces into abbreviated form
 * 
 * @author Bart Hanssens
 */
public class NsConverter {
private final static String BA = "BE.BRUSSELS.BRIC.ADM.ADDR";
	private final static String VA = "https://data.vlaanderen.be/id/adres";
	private final static String WA = "geodata.wallonie.be/id/Address";
	private final static String[][] ENC_A = { { BA, "BA" }, { VA, "VA" }, { WA, "WA" } };
	private final static String[][] DEC_A = { { "BA", BA }, { "VA", VA }, { "WA", WA } };

	private final static String BM = "BE.BRUSSELS.BRIC.ADM.MUNICIPALITY";
	private final static String VM = "https://data.vlaanderen.be/id/gemeente";
	private final static String WM = "geodata.wallonie.be/id/Municipality";
	private final static String[][] ENC_M = { { BM, "BM" }, { VM, "VM" }, { WM, "WM" } };
	private final static String[][] DEC_M = { { "BM", BM }, { "VM", VM }, { "WM", WM } };

	private final static String WMP = "geodata.wallonie.be/id/PartOfMunicipality";
	
	private final static String BP = "BE.BRUSSELS.BRIC.ADM.PZ";
	private final static String VP = "https://data.vlaanderen.be/id/postinfo";
	private final static String WP = "geodata.wallonie.be/id/PostalInfo";
	private final static String[][] ENC_P = { { BP, "BP" }, { VP, "VP" }, { WP, "WP" } };
	private final static String[][] DEC_P = { { "BP", BP }, { "VP", VP }, { "WP", WP } };
	
	private final static String BS = "BE.BRUSSELS.BRIC.ADM.STR";
	private final static String VS = "https://data.vlaanderen.be/id/straatnaam";
	private final static String WS = "geodata.wallonie.be/id/Streetname";
	private final static String[][] ENC_S = { { BS, "BS" }, { VS, "VS" }, { WS, "WS" } };
	private final static String[][] DEC_S = { { "BS", BS }, { "VS", VS }, { "WS", WS } };


	/**
	 * Encode/decode a namespace to a short version and vice versa
	 * 
	 * @param id ID to be encoded/decoded
	 * @param map encoding/decoding map
	 * @return ID with shorter or longer namespace
	 */
	private static String encode(String id, String[][] map) {
		if (id == null || id.isBlank()) {
			return id;
		}
		for (int i=0; i<3; i++) {
			if (id.startsWith(map[i][0])) {
				return id.replace(map[i][0], map[i][1]);
			}
		}
		return id;
		
	}

	/**
	 * Encode address
	 * 
	 * @param id full address ID
	 * @return address ID with short namespace
	 */
	public static String addressEncode(String id) {
		return encode(id, ENC_A);
	}

	/**
	 * Decode address
	 * 
	 * @param id address ID with short namespace
	 * @return full address ID
	 */
	public static String addressDecode(String id) {
		return encode(id, DEC_A);
	}

	/**
	 * Encode municipality
	 * 
	 * @param id full municipality ID
	 * @return municipality ID with short namespace
	 */
	public static String municipalityEncode(String id) {
		return encode(id, ENC_M);
	}

	/**
	 * Decode municipality
	 * 
	 * @param id municipality ID with short namespace
	 * @return full municipality ID
	 */
	public static String municipalityDecode(String id) {
		return encode(id, DEC_M);
	}

	/**
	 * Encode municipality part (only used by Wallonia)
	 * 
	 * @param id full municipality part ID
	 * @return municipality part ID with short namespace
	 */
	public static String municipalityPartEncode(String id) {
		if (id.startsWith(WMP)) {
			return id.replace(WMP, "WMP");
		}
		return id;
	}

	/**
	 * Decode municipality part
	 * 
	 * @param id municipality part ID with short namespace
	 * @return full municipality part ID
	 */
	public static String municipalityPartDecode(String id) {
		if (id.startsWith("WMP")) {
			return id.replace("WMP", WMP);
		}
		return id;
	}

	/**
	 * Encode postal info
	 * 
	 * @param id full postal info ID
	 * @return postal info ID with short namespace
	 */
	public static String postalEncode(String id) {
		return encode(id, ENC_P);
	}

	/**
	 * Decode postal info
	 * 
	 * @param id postal ID with short namespace
	 * @return full postal info ID
	 */
	public static String postalDecode(String id) {
		return encode(id, DEC_P);
	}

	/**
	 * Encode street
	 * 
	 * @param id full street ID
	 * @return street ID with short namespace
	 */
	public static String streetEncode(String id) {
		return encode(id, ENC_S);
	}

	/**
	 * Decode street
	 * 
	 * @param id ID with short namespace
	 * @return full street ID
	 */
	public static String streetDecode(String id) {
		return encode(id, DEC_S);
	}
}
