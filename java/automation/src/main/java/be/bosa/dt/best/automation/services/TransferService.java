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

import org.eclipse.microprofile.faulttolerance.Retry;

/**
 * Copies zipfile from BeST MFT to public web server via SFTP
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
public class TransferService {
	/**
	 * Download data file via SFTP (typically from BOSA "Managed File Transfer" service) and save it to a local file.
	 * 
	 * @param server remote host
	 * @param port port
	 * @param user user name
	 * @param pass password
	 * @param path remote location of the file
	 * @param zip local name of zip file
	 * @throws IOException 
	 */
	@Retry(retryOn = Exception.class, maxRetries = 3, delay = 2000)
	public void download(String server, int port, String user, String pass, String path, String zip) throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(server, port);
		client.authPassword(user, pass);

		try (SFTPClient sftp = client.newSFTPClient()) {
			sftp.get(path, zip);
		} finally {
			client.disconnect();
		}
	}

	/**
	 * Upload file to a server via SFTP
	 * 
	 * @param server remote host
	 * @param port remote port
	 * @param user user name
	 * @param pass password
	 * @param path remote location of the file
	 * @param zip local zip file
	 * @throws IOException 
	 */
	@Retry(retryOn = Exception.class, maxRetries = 3, delay = 2000)
	public void upload(String server, int port, String user, String pass, String path, String zip) throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(server, port);
		client.authPassword(user, pass);

		try (SFTPClient sftp = client.newSFTPClient()) {
			sftp.put(zip, path);
		} finally {
			client.disconnect();
		}		
	}
}
