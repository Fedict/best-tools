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
package be.bosa.dt.best.automation;

import be.bosa.dt.best.automation.util.Utils;
import be.bosa.dt.best.automation.services.TransferService;
import be.bosa.dt.best.automation.services.VerifyService;
import be.bosa.dt.best.automation.services.ZipService;
import be.bosa.dt.best.converter.writer.BestRegionWriter;
import be.bosa.dt.best.converter.writer.BestWriterCSV;
import be.bosa.dt.best.converter.writer.BestWriterCSVEmptyStreets;
import be.bosa.dt.best.converter.writer.BestWriterCSVOpenAddresses;
import be.bosa.dt.best.dao.BestRegion;

import io.quarkus.logging.Log;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Converts BeST XML files to CSV and upload to public site via SFTP.
 * 
 * @author Bart Hanssens
 */
@QuarkusMain
public class Main implements QuarkusApplication {
	@Inject
	TransferService sftp;
	
	@Inject
	ZipService zip;

	@Inject
	VerifyService verifier;
	
	@ConfigProperty(name = "automation.download.file")
	String downloadFile;

	@ConfigProperty(name = "automation.upload.path")
	String uploadPath;

	@ConfigProperty(name = "automation.web.bestfull.file")
	String dataFile;

	@ConfigProperty(name = "automation.web.postalstreets.file")
	String dataFilePs;

	@ConfigProperty(name = "automation.web.emptystreets.file")
	String dataFileEs;

	@ConfigProperty(name = "automation.web.oa.vlg.file")
	String dataFileOAVLG;
	@ConfigProperty(name = "automation.web.oa.bru.file")
	String dataFileOABRU;
	@ConfigProperty(name = "automation.web.oa.wal.file")
	String dataFileOAWAL;

	/**
	 * Convert BeST XML into CSV files per Region.
	 * Streets per postal code
	 * 
	 * @param file 
	 */
	private void convertRegion(String file, String zipfile) throws IOException {
		Path xmlPath = null;
		Path csvPath = null;
		try {
			xmlPath = Files.createTempDirectory("region-in");
			csvPath = Files.createTempDirectory("region-out");
			zip.unzip(file, xmlPath.toString());

			BestRegionWriter brw = new BestRegionWriter();
			for(BestRegion region: BestRegion.values()) {
				brw.writeRegion(new BestWriterCSV(), region, xmlPath, csvPath);
			}
			zip.zip(csvPath.toString(), zipfile, f -> f.toString().contains("postal_street"));
		} finally {
			Utils.recursiveDelete(xmlPath);
			Utils.recursiveDelete(csvPath);
		}
	}

	/**
	 * Convert BeST XML into CSV files per Region.
	 * OpenAddresses.io files
	 * 
	 * @param file 
	 */
	private void convertOA(String file, String zipFileVL, String zipFileBRU, String zipFileWAL) throws IOException {
		Path xmlPath = null;
		Path csvPath = null;
		try {
			xmlPath = Files.createTempDirectory("oa-in");
			csvPath = Files.createTempDirectory("oa-out");
			zip.unzip(file, xmlPath.toString());

			BestRegionWriter brw = new BestRegionWriter();
			for(BestRegion region: BestRegion.values()) {
				brw.writeRegion(new BestWriterCSVOpenAddresses(), region, xmlPath, csvPath);
			}
			zip.zip(csvPath.toString(), zipFileVL, f -> f.toString().contains("bevlg"));
			zip.zip(csvPath.toString(), zipFileBRU, f -> f.toString().contains("bebru"));
			zip.zip(csvPath.toString(), zipFileWAL, f -> f.toString().contains("bewal"));
		} finally {
			Utils.recursiveDelete(xmlPath);
			Utils.recursiveDelete(csvPath);
		}
	}

	/**
	 * Convert BeST XML into CSV files per Region.
	 * Empty streets i.e. streets without any address.
	 * 
	 * @param file 
	 */
	private void convertEmptyStreets(String file, String zipfile) throws IOException {
		Path xmlPath = null;
		Path csvPath = null;
		try {
			xmlPath = Files.createTempDirectory("emptystreets-in");
			csvPath = Files.createTempDirectory("emptystreets-out");
			zip.unzip(file, xmlPath.toString());

			BestRegionWriter brw = new BestRegionWriter();
			for(BestRegion region: BestRegion.values()) {
				brw.writeRegion(new BestWriterCSVEmptyStreets(), region, xmlPath, csvPath);
			}
			zip.zip(csvPath.toString(), zipfile);	
		} finally {
			Utils.recursiveDelete(xmlPath);
			Utils.recursiveDelete(csvPath);
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		int exitCode = 0;

		Path tempFile = null;
		Path zipFileOAVLG = null;
		Path zipFileOABRU = null;
		Path zipFileOAWAL = null;
		Path zipFilePs = null;
		Path zipFileEs = null;

		Log.info("Start");

		try {
			tempFile = Files.createTempFile("best", "local");
			String localFile = tempFile.toAbsolutePath().toString();
			String fileName = Utils.getFileName(downloadFile);

			Log.infof("Downloading %s", fileName);
			sftp.download(fileName, localFile);
			
			verifier.verify(localFile);

			zipFileOAVLG = Files.createTempFile("best", "oavlg");			
			zipFileOABRU = Files.createTempFile("best", "oabru");
			zipFileOAWAL = Files.createTempFile("best", "oawal");
			Log.info("Converting open addresses");
			convertOA(localFile, zipFileOAVLG.toString(), zipFileOABRU.toString(), zipFileOAWAL.toString());
			
			zipFilePs = Files.createTempFile("best", "postal");			
			Log.info("Converting postal streets");
			convertRegion(localFile, zipFilePs.toString());
					
			zipFileEs = Files.createTempFile("best", "empty");			
			Log.info("Converting empty streets");
			convertEmptyStreets(localFile, zipFileEs.toString());

			Log.info("Uploading open addresses VLG");
			sftp.upload(uploadPath + dataFileOAVLG, zipFileOAVLG.toString());
			Log.info("Uploading open addresses BRU");
			sftp.upload(uploadPath + dataFileOABRU, zipFileOABRU.toString());
			Log.info("Uploading open addresses WAL");
			sftp.upload(uploadPath + dataFileOAWAL, zipFileOAWAL.toString());

			Log.info("Uploading postal streets");
			sftp.upload(uploadPath + dataFilePs, zipFilePs.toString());
	
			Log.info("Uploading empty streets");
			sftp.upload(uploadPath + dataFileEs, zipFileEs.toString());
			
			Log.info("Uploading BeST Full");
			sftp.upload(uploadPath + dataFile, localFile);
			
			Log.info("Done (OK) " + fileName);
		} catch (IOException ioe) {
			exitCode = -1;
			Log.error("Failed", ioe);	
		} finally {
			for(Path p: new Path[]{ tempFile, zipFileOAVLG, zipFileOABRU, zipFileOAWAL, zipFilePs, zipFileEs}) {
				if (p != null) {
					p.toFile().delete();
				}
			}
		}
		return exitCode;
	}
}
