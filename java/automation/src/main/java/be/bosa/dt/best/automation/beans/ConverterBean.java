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

import be.bosa.dt.best.automation.services.MailService;
import be.bosa.dt.best.automation.services.TransferService;
import be.bosa.dt.best.automation.services.ZipService;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.emptystreets.EmptyStreetWriter;

import io.quarkus.mailer.Mail;
import io.quarkus.scheduler.Scheduled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Converts BeST XML files to CSV and upload to public site via SFTP.
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
public class ConverterBean extends StatusBean {
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

	@ConfigProperty(name = "emptystreets.data.file")
	String dataFile;

	@ConfigProperty(name = "emptystreets.weburl")
	String webUrl;

	@ConfigProperty(name = "copier.mailto")
	String mailTo;


	/**
	 * Convert XML into CSV
	 * 
	 * @param file 
	 */
	private void convert(String file, String zipfile) {
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
		} catch(IOException ioe) {
			
		} finally {
			Utils.recursiveDelete(xmlPath);
			Utils.recursiveDelete(csvPath);
		}
	}

	@Scheduled(cron = "{emptystreets.cron.expr}")
	public void scheduledEmptyStreets() {
		Mail mail;
		Path tempFile = null;
		Path zipFile = null;

		try {
			tempFile = Files.createTempFile("best", "local");
			String localFile = tempFile.toAbsolutePath().toString();
			String fileName = Utils.getFileName(mftFile);

			setStatus("Downloading " + fileName);
			sftp.download(mftServer, mftPort, mftUser, mftPass, fileName, localFile);

			zipFile = Files.createTempFile("best", "empty");			
			setStatus("Converting");
			convert(localFile, zipFile.toString());
			
			setStatus("Uploading");
			sftp.upload(dataServer, dataPort, dataUser, dataPass, dataFile, zipFile.toString());

			setStatus("Done (OK) " + fileName);
			mail = Mail.withText(mailTo, "Copy ok", "File copied: " + fileName);
		} catch (IOException ioe) {
			setStatus("Failed " + ioe.getMessage());
			mail = Mail.withText(mailTo, "Copy failed", ioe.getMessage());		
		} finally {
			if (tempFile != null) {
				tempFile.toFile().delete();
			}
			if (zipFile != null) {
				zipFile.toFile().delete();
			}
		}
		mailer.sendMail(mail);
	}
}
