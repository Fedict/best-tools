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

import be.bosa.dt.best.copier.Copier;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Retry;

/**
 * Copies zipfile from BeST MFT to public web server via SFTP
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
public class CopyBean extends StatusBean {
	@Inject
	Mailer mailer;
	
	private final Copier copier = new Copier();
	
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

	@ConfigProperty(name = "copier.mft.size")
	long minSize;

	@ConfigProperty(name = "copier.data.server")
	String dataServer;

	@ConfigProperty(name = "copier.data.port", defaultValue = "22")
	int dataPort;
	
	@ConfigProperty(name = "copier.data.user")
	String dataUser;
	
	@ConfigProperty(name = "copier.data.pass")
	String dataPass;

	@ConfigProperty(name = "copier.data.file")
	String dataFile;

	@ConfigProperty(name = "copier.weburl")
	String webUrl;

	@ConfigProperty(name = "copier.mailto")
	String mailTo;

	private String getFileName(String str) {
		LocalDate yesterday = LocalDate.now().minus(1, ChronoUnit.DAYS);
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
		return str.replace("%Y%m%d", fmt.format(yesterday));
	}

	/**
	 * Try to download a file from MFT
	 * 
	 * @param remote remote file name
	 * @param local local file name
	 * @throws IOException 
	 */
	@Retry(maxRetries = 3, delay = 2000)
	private void download(String remote, String local) throws IOException {
		setStatus("Downloading " + remote);
		copier.download(mftServer, mftPort, mftUser, mftPass, remote, local);
		setStatus("Verifying " + remote);
		copier.verifyZip(local, minSize);
	}

	/**
	 * Try to upload the file to SFTP / public site
	 * 
	 * @param local local file name
	 * @param expected expected file size
	 * @throws IOException 
	 */
	private void upload(String local, long expected) throws IOException, InterruptedException {
		setStatus("Uploading");
		copier.upload(dataServer, dataPort, dataUser, dataPass, dataFile, local);
		copier.verifyUpload(webUrl, expected);
	}

	@Scheduled(cron = "{copier.cron.expr}")
	void scheduledCopy() {
		Mail mail;
		Path p = null;

		try {
			p = Files.createTempFile("best", "local");
			String localFile = p.toAbsolutePath().toString();
			String fileName = getFileName(mftFile);
	
			download(fileName, localFile);
			
			upload(localFile, p.toFile().length());

			setStatus("Done (OK) " + fileName);
			mail = Mail.withText(mailTo, "Copy ok", "File copied: " + fileName);
		} catch (IOException | InterruptedException e) {
			setStatus("Failed");
			mail = Mail.withText(mailTo, "Copy failed", e.getMessage());		
		} finally {
			if (p != null) {
				p.toFile().delete();
			}
		}
		try {
			mailer.send(mail);
		} catch (Exception e) {
			//
		}
	}
}
