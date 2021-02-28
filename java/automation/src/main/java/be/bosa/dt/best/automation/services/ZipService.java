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
package be.bosa.dt.best.automation.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;


/**
 * Zips and (recursively) unzips files
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
public class ZipService {
	private static final Logger LOG = Logger.getLogger(ZipService.class);

	/**
	 * Unzip a zip file into a directory
	 * 
	 * @param pin input file
	 * @param pout output directory
	 * @throws IOException
	 */
	private void unzip(Path pin, Path pout) throws IOException {
		try (ZipFile zip = new ZipFile(pin.toFile())) {
			for (ZipEntry f: zip.stream().toArray(ZipEntry[]::new)) {
				String name = f.getName();
				Path p = Paths.get(pout.toString(), name);
				LOG.infof("Unzipping %s", p);
				
				try (InputStream is = zip.getInputStream(f)) {
					Files.copy(is, p);
					if (name.endsWith("zip")) { // zip within zip
						unzip(p, pout);
					}
				}
			}
		}
	}

	/**
	 * Perform some quick checks and unzip into an existing directory
	 * 
	 * @param infile zip input file
	 * @param outdir output directory
	 * @return false on error
	 */
	public boolean unzip(String infile, String outdir) {
		Path pin = Paths.get(infile);
		if (! (Files.exists(pin) && Files.isRegularFile(pin))) {
			LOG.errorf("Could not find input file %s", pin);
			return false;
		}
		
		Path pout = Paths.get(outdir);
		if (! (Files.exists(pout) && Files.isDirectory(pout))) {
			LOG.errorf("Could not find output directory %s", pout);
			return false;
		}
		
		try {
			unzip(pin, pout);
		} catch (IOException ioe) {
			LOG.error("Error unzipping", ioe);
			return false;
		}
		return true;
	}
	
	/**
	 * Add a file to zip
	 * 
	 * @param pin input file
	 * @param zos output stream
	 * @throws IOException
	 */
	private void zip(Path pin, ZipOutputStream zos) throws IOException {
		ZipEntry zipEntry = new ZipEntry(pin.getFileName().toString());
		
		try(InputStream fis = Files.newInputStream(pin)) {
			LOG.infof("Zipping %s", pin);
			zos.putNextEntry(zipEntry);
					
			byte[] buffer = new byte[32*1024];
			int len;
			while ((len = fis.read(buffer)) >= 0) {
				zos.write(buffer, 0, len);
			}
		}
	}
		
	/**
	 * Move files to a zip file
	 * 
	 * @param indir input directory containing files to be zipped
	 * @param outfile output zip file
	 * @return false on error
	 */
	public boolean zip(String indir, String outfile) {
		Path pin = Paths.get(indir);
		if (! (Files.exists(pin) && Files.isDirectory(pin))) {
			LOG.errorf("Could not find input directory %s", pin);
			return false;
		}

		Path pout = Paths.get(outfile);
		try (OutputStream os = Files.newOutputStream(pout);
			ZipOutputStream zos = new ZipOutputStream(os)) {
	
			Path[] files = Files.walk(pin).filter(f -> f.toFile().isFile()).toArray(Path[]::new);
			for (Path f: files) {
				zip(f, zos);
			}
		} catch (IOException ioe) {
			LOG.error("Could not zip files", ioe);
			return false;
		}

		return true;
	}
}
