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
package be.bosa.dt.best.copier;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import java.util.Enumeration;
import java.util.zip.ZipFile;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;


/**
 * Copy Belgian Streets and Addresses (BeST) zipped XML file from an SFTP server to a web site
 * 
 * @author Bart Hanssens
 */
public class Copier {
	/**
	 * Return size as long value
	 * 
	 * @param size size as string
	 * @return size as long value or -1 on error
	 */
	public static long strToLong(String size) {
		long val = -1;
		try {
			val = Long.valueOf(size);
		} catch (NumberFormatException nfe) {
			//
		}
		return val;
	}

	/**
	 * Download data file via SFTP (typically from BOSA "Managed File Transfer" service) and save it to a local file.
	 * 
	 * @param server remote host
	 * @param user user name
	 * @param pass password
	 * @param path remote location of the file
	 * @param zip local name of zip file
	 * @throws IOException 
	 */
	public void download(String server, String user, String pass, String path, String zip) throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(server);
		client.authPassword(user, pass);

		try (SFTPClient sftp = client.newSFTPClient()) {
			sftp.get(path, zip);
		}

		client.disconnect();
	}

	/**
	 * Quick verification of the zip file containing BeST XML files
	 * 
	 * @param f file name
	 * @param minLen minimum size 
	 * @throws IOException 
	 */
	public void verifyZip(File f, long minLen) throws IOException {
		long len = f.length();
		if (len < minLen) {
			throw new IOException("File to small: " + len);
		}
		
		ZipFile zip = new ZipFile(f);
		Enumeration entries = zip.entries();
	}

	/**
	 * Upload file to a server via SFTP
	 * 
	 * @param server remote host
	 * @param user user name
	 * @param pass password
	 * @param path remote location of the file
	 * @param zip local zip file
	 * @throws IOException 
	 */
	public void upload(String server, String user, String pass, String path, String zip) throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(server);
		client.authPassword(user, pass);

		try (SFTPClient sftp = client.newSFTPClient()) {
			sftp.put(zip, path);
		}

		client.disconnect();		
	}

	/**
	 * Verify if the file was uploaded correctly, using HTTP HEAD to obtain the file size
	 * 
	 * @param url HTTP(s) location of the uploaded file
	 * @param len expected length
	 * @throws IOException when there is no content-Length header or the size does not match
	 * @throws InterruptedException 
	 */
	public void verifyUpload(String url, long len) throws IOException, InterruptedException {
		URI uri = URI.create(url);
		HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
		HttpRequest req = HttpRequest.newBuilder(uri).method("HEAD", BodyPublishers.noBody()).build();
		
		HttpResponse<Void> resp = client.send(req, BodyHandlers.discarding());
		String val = resp.headers().firstValue("Content-Length").orElseThrow(IOException::new);
		
		long uploaded = strToLong(val);

		if (uploaded != len) {
			throw new IOException("Uploaded side does not match local size " + uploaded);
		}
	}
}
