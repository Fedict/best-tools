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

import be.bosa.dt.best.converter.dao.BestRegion;
import be.bosa.dt.best.converter.dao.Municipality;
import be.bosa.dt.best.converter.dao.Postal;
import be.bosa.dt.best.converter.dao.Streetname;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * BeST result file writer interface
 * 
 * @author Bart Hanssens
 */
public interface BestWriter {
	/**
	 * Process the input file and return a stream of BeSt municipalities objects
	 * 
	 * @param region
	 * @param outdir output directory
	 * @param cities 
	 */
	public void writeMunicipalities(BestRegion region, Path outdir, Stream<Municipality> cities);

	/**
	 * Process the input file and return a stream of BeSt postcode objects
	 * 
	 * @param region
	 * @param outdir output directory
	 * @param postals 
	 */
	public void writePostals(BestRegion region, Path outdir, Stream<Postal> postals);

	/**
	 * Process the input file and return a stream of BeSt streetname objects
	 * 
	 * @param region
	 * @param outdir output directory
	 * @param streetnames
	 */
	public void writeStreets(BestRegion region, Path outdir, Stream<Streetname> streetnames);
	//public void writeAddresses(Region region, Path outdir, Stream<Address> addresses);
	
	/**
	 * 
	 * @param outdir
	 * @param region
	 * @param suffix
	 * @param ext
	 * @return 
	 */
	public static Path getPath(Path outdir, BestRegion region, String suffix, String ext) {
		return Paths.get(outdir.toString(), region.getName() + suffix + "." + ext);
	}
}