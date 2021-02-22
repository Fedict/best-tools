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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author Bart Hanssens
 */
@ApplicationScoped
public class CopyBean {
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

	private String getFileName(String fmt) {
		LocalDate yesterday = LocalDate.now().minus(1, ChronoUnit.DAYS);
		DateFormat df = new SimpleDateFormat("%Y%m%d");
		return fmt.replace("%Y%m%d", df.format(yesterday));
	}

	@Scheduled(cron = "{copier.cron.expr}")
	void scheduledCopy() {
		Mail mail;

		try {
			Path p = Files.createTempFile("best", "local");
			String localFile = p.toAbsolutePath().toString();
	
			copier.download(mftServer, mftPort, mftUser, mftPass, getFileName(mftFile), localFile);

			copier.verifyZip(localFile, minSize);
			
			copier.upload(dataServer, dataPort, dataUser, dataPass, dataFile, localFile);
			copier.verifyUpload(webUrl, p.toFile().length());

			mail = Mail.withText(mailTo, "Copy ok", "File copied");
		} catch (IOException | InterruptedException e) {
			mail = Mail.withText(mailTo, "Copy failed", e.getMessage());			
		}
		mailer.send(mail);
	}
}
