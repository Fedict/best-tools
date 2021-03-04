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
package be.bosa.dt.best.automation.beans;

import be.bosa.dt.best.automation.util.Utils;
import be.bosa.dt.best.automation.util.Status;
import be.bosa.dt.best.automation.services.MailService;
import be.bosa.dt.best.automation.services.TransferService;
import be.bosa.dt.best.automation.services.ZipService;
import be.bosa.dt.best.automation.util.StatusHistory;
import be.bosa.dt.best.converter.writer.BestRegionWriter;
import be.bosa.dt.best.converter.writer.BestWriterCSV;
import be.bosa.dt.best.converter.writer.BestWriterCSVOpenAddresses;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.emptystreets.EmptyStreetWriter;

import io.quarkus.mailer.Mail;
import io.quarkus.scheduler.Scheduled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Converts BeST XML files to CSV and upload to public site via SFTP.
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
public class ConverterBean implements StatusHistory {
	@Inject
	TransferService sftp;
	
	@Inject
	ZipService zip;

	@Inject
	MailService mailer;

	@ConfigProperty(name = "copier.mft.server")
	String mftServer;

	@ConfigProperty(name = "copier.mft.port", defaultValue = "22")
	int mftPort;

	@ConfigProperty(name = "copier.mft.user")
	String mftUser;
	
	@ConfigProperty(name = "copier.mft.pass")
	String mftPass;
	
	@ConfigProperty(name = "copier.mft.file")
	String mftFile;

	@ConfigProperty(name = "copier.data.server")
	String dataServer;

	@ConfigProperty(name = "copier.data.port", defaultValue = "22")
	int dataPort;
	
	@ConfigProperty(name = "copier.data.user")
	String dataUser;
	
	@ConfigProperty(name = "copier.data.pass")
	String dataPass;

	@ConfigProperty(name = "copier.data.path")
	String dataPath;

	@ConfigProperty(name = "postalstreets.data.file")
	String dataFilePs;

	@ConfigProperty(name = "emptystreets.data.file")
	String dataFileEs;

	@ConfigProperty(name = "openaddresses.vlg.data.file")
	String dataFileOAVLG;
	@ConfigProperty(name = "openaddresses.bru.data.file")
	String dataFileOABRU;
	@ConfigProperty(name = "openaddresses.wal.data.file")
	String dataFileOAWAL;

	@ConfigProperty(name = "copier.mailto")
	String mailTo;

	private Status status = new Status();

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
			zip.zip(csvPath.toString(), zipfile, f -> f.toString().contains("postal"));
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

			EmptyStreetWriter esw = new EmptyStreetWriter();
			for(BestRegion region: BestRegion.values()) {
				esw.writeRegion(region, xmlPath, csvPath);
			}
			zip.zip(csvPath.toString(), zipfile);	
		} finally {
			Utils.recursiveDelete(xmlPath);
			Utils.recursiveDelete(csvPath);
		}
	}

	@Scheduled(cron = "{converter.cron.expr}")
	public void scheduledConverter() {
		Mail mail;

		Path tempFile = null;
		Path zipFileOAVLG = null;
		Path zipFileOABRU = null;
		Path zipFileOAWAL = null;
		Path zipFilePs = null;
		Path zipFileEs = null;

		status.clear();

		try {
			tempFile = Files.createTempFile("best", "local");
			String localFile = tempFile.toAbsolutePath().toString();
			String fileName = Utils.getFileName(mftFile);

			status.set("Downloading " + fileName);
			sftp.download(mftServer, mftPort, mftUser, mftPass, fileName, localFile);

			zipFileOAVLG = Files.createTempFile("best", "oavlg");			
			zipFileOABRU = Files.createTempFile("best", "oabru");
			zipFileOAWAL = Files.createTempFile("best", "oawal");
			status.set("Converting open addresses");
			convertOA(localFile, zipFileOAVLG.toString(), zipFileOABRU.toString(), zipFileOAWAL.toString());
			
			zipFilePs = Files.createTempFile("best", "postal");			
			status.set("Converting postal streets");
			convertRegion(localFile, zipFilePs.toString());
					
			zipFileEs = Files.createTempFile("best", "empty");			
			status.set("Converting empty streets");
			convertEmptyStreets(localFile, zipFileEs.toString());

			status.set("Uploading open addresses VLG");
			sftp.upload(dataServer, dataPort, dataUser, dataPass, dataPath + dataFileOAVLG, zipFileOAVLG.toString());
			status.set("Uploading open addresses BRU");
			sftp.upload(dataServer, dataPort, dataUser, dataPass, dataPath + dataFileOABRU, zipFileOABRU.toString());
			status.set("Uploading open addresses WAL");
			sftp.upload(dataServer, dataPort, dataUser, dataPass, dataPath + dataFileOAWAL, zipFileOAWAL.toString());

			status.set("Uploading postal streets");
			sftp.upload(dataServer, dataPort, dataUser, dataPass, dataPath + dataFilePs, zipFilePs.toString());
	
			status.set("Uploading empty streets");
			sftp.upload(dataServer, dataPort, dataUser, dataPass, dataPath + dataFileEs, zipFileEs.toString());

			status.set("Done (OK) " + fileName);
			mail = Mail.withText(mailTo, "Conversion ok", "File used: " + fileName);
		} catch (IOException ioe) {
			status.set("Failed " + ioe.getMessage());
			mail = Mail.withText(mailTo, "Conversion failed", ioe.getMessage());		
		} finally {
			for(Path p: new Path[]{ tempFile, zipFileOAVLG, zipFileOABRU, zipFileOAWAL, zipFilePs, zipFileEs}) {
				if (p != null) {
					p.toFile().delete();
				}
			}
		}
		mailer.sendMail(mail);
	}

	@Override
	public List<String> getStatusHistory() {
		return status.getHistory();
	}
}
