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
package be.bosa.dt.best.unzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.zip.ZipFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Small tool to unzip the various BeST zipfiles
 * 
 * @author Bart Hanssens
 */
public class Main {
	private final static Logger LOG = LoggerFactory.getLogger(Main.class);
		
	private final static Options OPTS = new Options()
		.addRequiredOption("i", "infile", true, "zipped file")
		.addRequiredOption("o", "outdir", true, "output directory");

	/**
	 * Print help info
	 */
	private static void printHelp() {
		HelpFormatter fmt = new HelpFormatter();
		fmt.printHelp("unzip BeST zip files", OPTS);
	}
	
	/**
	 * Parse command line arguments
	 * 
	 * @param args
	 * @return 
	 */
	private static CommandLine parse(String[] args) {
		CommandLineParser cli = new DefaultParser();
		try {
			return cli.parse(OPTS, args);
		} catch (ParseException ex) {
			printHelp();
		}
		return null;
	}
	
	/**
	 * Unzip a zip file into a directory
	 * 
	 * @param pin input file
	 * @param pout output directory
	 */
	private static void unzip(Path pin, Path pout) {
		try (ZipFile zip = new ZipFile(pin.toFile())) {
			zip.stream().forEach(f -> {
				String name = f.getName();
				Path p = Paths.get(pout.toString(), name);
				LOG.info("Unzipping {}", p);
				
				try (InputStream is = zip.getInputStream(f);
					OutputStream os = Files.newOutputStream(p)) {
					IOUtils.copyLarge(is, os);
					if (name.endsWith("zip")) { // zip within zip
						unzip(p, pout);
						Files.delete(p);
					}
				} catch (IOException e) {
					LOG.error("Error extracting {}", p);
				}
			});
		} catch (IOException ioe) {
			LOG.error("Error extracting {}", pin.toFile());
		}
	}
	
	/**
	 * Perform some quick checks and unzip, exit on error
	 * 
	 * @param infile zip input file
	 * @param outdir output directory
	 */
	private static void checkUnzip(String infile, String outdir) {
		if (! infile.toLowerCase().endsWith("zip")) {
			LOG.error("Not a zip file");
			System.exit(-2);
		}
		Path pin = Paths.get(infile);
		if (! (Files.exists(pin) && Files.isRegularFile(pin))) {
			LOG.error("Could not find input file");
			System.exit(-3);
		}
		
		Path pout = Paths.get(outdir);
		if (! (Files.exists(pout) && Files.isDirectory(pout))) {
			LOG.error("Could not find output directory");
			System.exit(-4);
		}
		unzip(pin, pout);
	}
	
	/**
	 * Main
	 * 
	 * @param args 
	 */
	public static void main(String args[]) {
		CommandLine cli  = parse(args);
		if (cli == null) {
			System.exit(-1);
		}

		String infile = cli.getOptionValue("i");
		String outdir = cli.getOptionValue("o");

		checkUnzip(infile, outdir);	
	}
}
