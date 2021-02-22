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
package be.bosa.dt.best.converter.writer;

import be.bosa.dt.best.dao.Address;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.Municipality;
import be.bosa.dt.best.dao.Postal;
import be.bosa.dt.best.dao.Street;
import be.bosa.dt.best.xmlreader.AddressReader;
import be.bosa.dt.best.xmlreader.MunicipalityPartReader;
import be.bosa.dt.best.xmlreader.MunicipalityReader;
import be.bosa.dt.best.xmlreader.PostalReader;
import be.bosa.dt.best.xmlreader.StreetnameReader;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * @author Bart.Hanssens
 */
public class BestRegionWriter {
	
	/**
	 * Write files for a specific region
	 * 
	 * @param writer
	 * @param region
	 * @param inPath input directory
	 * @param outPath output directory
	 */
	public void writeRegion(BestWriter writer, BestRegion region, Path inPath, Path outPath) {
		try( Stream<Municipality> cities = new MunicipalityReader().read(region, inPath);
			Stream<Postal> postals = new PostalReader().read(region, inPath);
			Stream<Street> streets = new StreetnameReader().read(region, inPath);
			Stream<Address> addresses = new AddressReader().read(region, inPath)) {
				
			Map<String, String[]> cacheCities = writer.writeMunicipalities(region, outPath, cities);

			Map<String, String[]> cacheCityParts = Collections.EMPTY_MAP;
			if (region.equals(BestRegion.WALLONIA)) { // only Walloon Region provides "municipality parts"
				try ( Stream<Municipality> cityParts = new MunicipalityPartReader().read(region, inPath)) {
					cacheCityParts = writer.writeMunicipalityParts(region, outPath, cityParts);
				}
			}
			Map<String, String[]> cachePostals = writer.writePostals(region, outPath, postals);
			Map<String, String[]> cacheStreets = writer.writeStreets(region, outPath, streets, cacheCities);
			
			Map<String, Map<String, String[]>> cachePostalStreets = 
				writer.writeAddresses(region, outPath, addresses, cacheStreets, cacheCities, cacheCityParts, cachePostals);
			
			writer.writePostalStreets(region, outPath, cachePostalStreets);
		}
	}

}
