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
 * Download BeST XML full download via SFTP, convert, and upload XML and CSV files to public website
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

	@ConfigProperty(name = "automation.tempdata.path")
	Path tempData;
	
	@ConfigProperty(name = "automation.web.bestfull.file")
	String fullFile;

	@ConfigProperty(name = "automation.web.postalstreets.file")
	String postalstreetFile;

	@ConfigProperty(name = "automation.web.emptystreets.file")
	String emptystreetFile;

	@ConfigProperty(name = "automation.web.oa.vlg.file")
	String oaVlgFile;
	@ConfigProperty(name = "automation.web.oa.bru.file")
	String oaBruFile;
	@ConfigProperty(name = "automation.web.oa.wal.file")
	String oaWalFile;

	/**
	 * Convert BeST XML into CSV files per Region.
	 * Streets per postal code
	 * 
	 * @param file 
	 */
	private void convertStreets(String file, String zipfile) throws IOException {
		Path xmlPath = null;
		Path csvPath = null;
		try {
			xmlPath = Files.createTempDirectory(tempData, "region-in");
			csvPath = Files.createTempDirectory(tempData, "region-out");
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
			xmlPath = Files.createTempDirectory(tempData, "oa-in");
			csvPath = Files.createTempDirectory(tempData, "oa-out");
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
	 * Empty streets i.e. streets without any house number like country roads, forest tracks... 
	 * 
	 * @param file 
	 */
	private void convertEmptyStreets(String file, String zipfile) throws IOException {
		Path xmlPath = null;
		Path csvPath = null;
		try {
			xmlPath = Files.createTempDirectory(tempData, "emptystreets-in");
			csvPath = Files.createTempDirectory(tempData, "emptystreets-out");
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
			if (!tempData.toFile().exists()) {
				Log.infof("Creating directory %s", tempData);
				Files.createDirectory(tempData);
			}

			tempFile = Files.createTempFile(tempData, "bestfull", "local");
			String localFile = tempFile.toAbsolutePath().toString();
			String fileName = Utils.getFileName(downloadFile);

			sftp.download(fileName, localFile);
			
			verifier.verify(localFile);

			zipFileOAVLG = Files.createTempFile(tempData, "oa", "vlg");			
			zipFileOABRU = Files.createTempFile(tempData, "oa", "bru");
			zipFileOAWAL = Files.createTempFile(tempData, "oa", "wal");
			Log.info("Converting to OpenAddresses format");
			convertOA(localFile, zipFileOAVLG.toString(), zipFileOABRU.toString(), zipFileOAWAL.toString());
			
			zipFilePs = Files.createTempFile(tempData, "street", "postal");			
			Log.info("Converting postal streets");
			convertStreets(localFile, zipFilePs.toString());
					
			zipFileEs = Files.createTempFile(tempData, "street", "empty");			
			Log.info("Converting empty streets");
			convertEmptyStreets(localFile, zipFileEs.toString());
			
			Log.info("Uploading BeST Full XML");
			sftp.upload(localFile, uploadPath + fullFile);

			Log.info("Uploading OpenAddresses VLG");
			sftp.upload(zipFileOAVLG.toString(), uploadPath + oaVlgFile);
			Log.info("Uploading OpenAddresses BRU");
			sftp.upload(zipFileOABRU.toString(), uploadPath + oaBruFile);
			Log.info("Uploading OpenAddresses WAL");
			sftp.upload( zipFileOAWAL.toString(), uploadPath + oaWalFile);

			Log.info("Uploading postal streets");
			sftp.upload(zipFilePs.toString(), uploadPath + postalstreetFile);
	
			Log.info("Uploading empty streets");
			sftp.upload(zipFileEs.toString(), uploadPath + emptystreetFile);
			
			Log.infof("Done (OK) %s", fileName);
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
