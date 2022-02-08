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

import javax.enterprise.context.ApplicationScoped;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.eclipse.microprofile.faulttolerance.Retry;

/**
 * Copies zipfile from BeST MFT to public web server via SFTP
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
public class TransferService {
	// from
	@ConfigProperty(name = "automation.download.server")
	String downloadServer;

	@ConfigProperty(name = "automation.download.port", defaultValue = "22")
	int downloadPort;

	@ConfigProperty(name = "automation.download.user")
	String downloadUser;
	
	@ConfigProperty(name = "automation.download.pass")
	String downloadPass;

	// to
	@ConfigProperty(name = "automation.upload.server")
	String uploadServer;

	@ConfigProperty(name = "automation.upload.port", defaultValue = "22")
	int uploadPort;
	
	@ConfigProperty(name = "automation.upload.user")
	String uploadUser;
	
	@ConfigProperty(name = "automation.upload.pass")
	String uploadPass;

	/**
	 * Download data file via SFTP (typically from BOSA "Managed File Transfer" service) and save it to a local file.
	 *
	 * @param remote remote location of the file
	 * @param local local name of zip file
	 * @throws IOException 
	 */
	@Retry(retryOn = Exception.class, maxRetries = 3, delay = 2000)
	public void download(String remote, String local) throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(downloadServer, downloadPort);
		client.authPassword(downloadUser, downloadPass);

		try (SFTPClient sftp = client.newSFTPClient()) {
			sftp.get(remote, local);
		} finally {
			client.disconnect();
		}
	}

	/**
	 * Upload file to a server via SFTP
	 * 
	 * @param remote remote location of the file
	 * @param local local zip file
	 * @throws IOException 
	 */
	@Retry(retryOn = Exception.class, maxRetries = 5, delay = 3000)
	public void upload(String remote, String local) throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(uploadServer, uploadPort);
		client.authPassword(uploadUser, uploadPass);

		try (SFTPClient sftp = client.newSFTPClient()) {
			sftp.put(local, remote);
		} finally {
			client.disconnect();
		}		
	}
}
