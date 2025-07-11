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
package be.bosa.dt.best.converter.writer;

import be.bosa.dt.best.dao.Address;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.BestType;
import be.bosa.dt.best.dao.Municipality;
import be.bosa.dt.best.dao.Postal;
import be.bosa.dt.best.dao.Street;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

/**
 * BeST result file writer interface
 *
 * @author Bart Hanssens
 */
public interface BestWriter {

	/**
	 * Construct the name for an output file
	 *
	 * @param outdir output directory
	 * @param region BeST region
	 * @param type BeST type
	 * @param ext file extension
	 * @return full path to file
	 */
	public static Path getPath(Path outdir, BestRegion region, BestType type, String ext) {
		String suffix = type.toString().toLowerCase();
		return Paths.get(outdir.toString(), region.getName() + "_" + suffix + "." + ext);
	}
	/**
	 * Construct the name for an output file
	 *
	 * @param outdir output directory
	 * @param region BeST region
	 * @param suffix
	 * @param ext file extension
	 * @return full path to file
	 */
	public static Path getPath(Path outdir, BestRegion region, String suffix, String ext) {
		return Paths.get(outdir.toString(), region.getName() + "_" + suffix + "." + ext);
	}

	/**
	 * Write a stream of BeSt municipalities objects to a file and return a cache
	 *
	 * @param region
	 * @param outdir output directory
	 * @param cities
	 * @return cache
	 */
	public Map<String, String[]> writeMunicipalities(BestRegion region, Path outdir, Stream<Municipality> cities);

	/**
	 * Write a stream of BeSt municipality part objects to a file and return a cache
	 *
	 * @param region
	 * @param outdir output directory
	 * @param cityParts
	 * @return cache
	 */
	public Map<String, String[]> writeMunicipalityParts(BestRegion region, Path outdir, Stream<Municipality> cityParts);

	/**
	 * Write a stream of stream of BeSt postcode objects to a file and return a cache
	 *
	 * @param region
	 * @param outdir output directory
	 * @param postals
	 * @return cache
	 */
	public Map<String, String[]> writePostals(BestRegion region, Path outdir, Stream<Postal> postals);

	/**
	 * Write a stream of BeSt streetname objects to a file and return a cache
	 *
	 * @param region
	 * @param outdir output directory
	 * @param streetnames street names cache
	 * @param cities city names cache
	 * @return cache
	 */
	public Map<String, String[]> writeStreets(BestRegion region, Path outdir, Stream<Street> streetnames,
		Map<String, String[]> cities);

	/**
	 * Write a stream of BeSt addresses to a file and return a cache
	 *
	 * @param region
	 * @param outdir output directory
	 * @param addresses
	 * @param streets street name cache
	 * @param cities city names cache
	 * @param cityParts city parts cache
	 * @param postals postal info cache
	 * @return cache
	 */
	public Map<String, Map<String, String[]>> writeAddresses(BestRegion region, Path outdir, Stream<Address> addresses,
		Map<String, String[]> streets, Map<String, String[]> cities, Map<String, String[]> cityParts,
		Map<String, String[]> postals);
	
	/**
	 * Write a list of streets and postal codes + additional city / postal names. 
	 * A street can have multiple postal codes.
	 * 
	 * @param region
	 * @param outdir output directory
	 * @param postalStreetCache
	 */
	public void writePostalStreets(BestRegion region, Path outdir, Map<String, Map<String, String[]>> postalStreetCache);
}
