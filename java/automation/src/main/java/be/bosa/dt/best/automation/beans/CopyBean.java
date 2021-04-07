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
import be.bosa.dt.best.automation.services.VerifyService;
import be.bosa.dt.best.automation.util.StatusHistory;

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
 * Copies zipfile from BeST MFT to public web server via SFTP
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
public class CopyBean implements StatusHistory {
	@Inject
	TransferService sftp;

	@Inject
	MailService mailer;

	@Inject
	VerifyService verifier;

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

	@ConfigProperty(name = "bestfull.data.file")
	String dataFile;

	@ConfigProperty(name = "copier.mailto")
	String mailTo;

	private final Status status = new Status();

	/**
	 * Copy ZIP file from MFT to public website via SFTP
	 */
	@Scheduled(cron = "{copier.cron.expr}")
	public void scheduledCopy() {
		Mail mail;
		Path tmpFile = null;

		status.clear();

		try {
			tmpFile = Files.createTempFile("best", "local");
			String localFile = tmpFile.toAbsolutePath().toString();
			String fileName = Utils.getFileName(mftFile);
	
			status.set("Downloading " + fileName);
			sftp.download(fileName, localFile);

			verifier.verify(localFile);

			status.set("Uploading");
			sftp.upload(dataPath + dataFile, localFile);

			status.set("Done (OK) " + fileName);
			mail = Mail.withText(mailTo, "Copy ok", "File copied: " + fileName);
		} catch (IOException ioe) {
			status.set("Failed " + ioe.getMessage());
			mail = Mail.withText(mailTo, "Copy failed", ioe.getMessage());		
		} finally {
			if (tmpFile != null) {
				tmpFile.toFile().delete();
			}
		}

		mailer.sendMail(mail);
	}
	
	@Override
	public List<String> getStatusHistory() {
		return status.getHistory();
	}
}
