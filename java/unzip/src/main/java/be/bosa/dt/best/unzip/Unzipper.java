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
package be.bosa.dt.best.unzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

/**
 * Unzip class
 * 
 * @author Bart Hanssens
 */
public class Unzipper {
	private final static Logger LOG = Logger.getLogger(Unzipper.class.getName());
	
	/**
	 * Unzip a zip file into a directory
	 * 
	 * @param pin input file
	 * @param pout output directory
	 * @return false in case of error
	 */
	public boolean unzip(Path pin, Path pout) {
		boolean ok = true;
		try (ZipFile zip = new ZipFile(pin.toFile())) {
			for (ZipEntry f: zip.stream().toArray(ZipEntry[]::new)) {
				String name = f.getName();
				Path p = Paths.get(pout.toString(), name);
				LOG.log(Level.INFO, "Unzipping {}", p);
				
				try (InputStream is = zip.getInputStream(f);
					OutputStream os = Files.newOutputStream(p)) {
					IOUtils.copyLarge(is, os);
					if (name.endsWith("zip")) { // zip within zip
						unzip(p, pout);
						Files.delete(p);
					}
				} catch (IOException e) {
					ok = false;
					LOG.log(Level.SEVERE, "Error extracting {}", p);
				}
			}
		} catch (IOException ioe) {
			ok = false;
			LOG.log(Level.SEVERE, "Error extracting {}", pin.toFile());
		}
		return ok;
	}

	/**
	 * Perform some quick checks and unzip, return on error
	 * 
	 * @param infile zip input file
	 * @param outdir output directory
	 * @return false on error
	 */
	public boolean checkAndUnzip(String infile, String outdir) {
		if (! infile.toLowerCase().endsWith("zip")) {
			LOG.severe("Not a zip file");
			return false;
		}
		Path pin = Paths.get(infile);
		if (! (Files.exists(pin) && Files.isRegularFile(pin))) {
			LOG.severe("Could not find input file");
			return false;
		}
		
		Path pout = Paths.get(outdir);
		if (! (Files.exists(pout) && Files.isDirectory(pout))) {
			LOG.severe("Could not find output directory");
			return false;
		}
		
		try {
			Path[] files = Files.walk(pout).filter(f -> !f.equals(pout)).toArray(Path[]::new);
			for (Path f: files) {
				LOG.log(Level.INFO, "Deleting {}", f);
				Files.delete(f);
			}
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, "Could not delete files in directory", ioe);
			return false;
		}
		
		return unzip(pin, pout);
	}
}
