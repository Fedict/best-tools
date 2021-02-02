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

import java.nio.file.Paths;
import java.time.Duration;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

/**
 * Copy Belgian Streets and Addresses (BeST) zipped XML file from an SFTP server to a web site
 * 
 * @author Bart Hanssens
 */
public class Main {
	private final static Logger LOG  = Logger.getLogger(Main.class.getName());

	private final static Set<String> FILE_STARTS = Set.of("Flanders");
	private final static File BEST_FILE = Paths.get("best.zip").toFile();
	
	// in case of an error, retry download / upload 
	private final static RetryPolicy<Object> policy = new RetryPolicy<>()
							.handle(IOException.class,InterruptedException.class, MessagingException.class)
							.withDelay(Duration.ofSeconds(30))
							.withMaxRetries(3);


	/**
	 * Return size as long value
	 * 
	 * @param size size as string
	 * @return size as long value or -1 on error
	 */
	private static long strToLong(String size) {
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
	private static void download(String server, String user, String pass, String path, File zip) throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(server);
		client.authPassword(user, pass);

		try (SFTPClient sftp = client.newSFTPClient()) {
			sftp.get(path, zip.getAbsolutePath());
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
	private static void verifyZip(File f, long minLen) throws IOException {
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
	private static void upload(String server, String user, String pass, String path, File zip) throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(server);
		client.authPassword(user, pass);

		try (SFTPClient sftp = client.newSFTPClient()) {
			sftp.put(zip.getAbsolutePath(), path);
		}

		client.disconnect();		
	}

	/**
	 * Verify if the file was uploaded correctly, using HTTP HEAD to obtain the file size
	 * 
	 * @param url HTTP(s) location of the uploaded file
	 * @param f local file
	 * @throws IOException when there is no content-Length header or the size does not match
	 * @throws InterruptedException 
	 */
	private static void verifyUpload(String url, File f) throws IOException, InterruptedException {
		URI uri = URI.create(url);
		HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
		HttpRequest req = HttpRequest.newBuilder(uri).method("HEAD", BodyPublishers.noBody()).build();
		
		HttpResponse<Void> resp = client.send(req, BodyHandlers.discarding());
		String val = resp.headers().firstValue("Content-Length").orElseThrow(IOException::new);
		
		long uploaded = strToLong(val);

		if (uploaded != f.length()) {
			throw new IOException("Uploaded side does not match local size " + uploaded);
		}
	}

	/**
	 * Send a report by email
	 * 
	 * @param server mail server
	 * @param port mail port
	 * @param mailto destination
	 * @throws MessagingException 
	 */
	private static void mail(String server, String port, String mailto) throws MessagingException {
		Properties prop = new Properties();
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", server);
		prop.put("mail.smtp.port", port);
		
		Session session = Session.getInstance(prop);

		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress("opendata@belgium.be"));
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailto));
		msg.setSubject("Mail Subject");

		Transport.send(msg);
	}

	/**
	 * Main
	 * 
	 * @param args not used
	 */
	public static void main(String args[]) {
		// get all data from environment variables (so this app can be used in e.g. openshift)

		String downServer = System.getenv("DOWNLOAD_SERVER");
		String downPath = System.getenv("DOWNLOAD_PATH");
		String downUser = System.getenv("DOWNLOAD_USER");
		String downPass = System.getenv("DOWNLOAD_PASS");
		String minSize = System.getenv("MIN_SIZE_M");
		
		LOG.log(Level.INFO, "Downloading from {0} {1}", new String[] { downServer, downPath});

		Failsafe.with(policy).run(() -> {
			BEST_FILE.delete();
			download(downServer, downUser, downPass, downPath, BEST_FILE);
			verifyZip(BEST_FILE, strToLong(minSize));
		});

		LOG.log(Level.INFO, "File size {0}", BEST_FILE.length());

		String upServer = System.getenv("UPLOAD_SERVER");
		String upPath = System.getenv("UPLOAD_PATH");
		String upUser = System.getenv("UPLOAD_USER");
		String upPass = System.getenv("UPLOAD_PASS");		
		String webfile = System.getenv("WEBSITE_FILE");		

		LOG.log(Level.INFO, "Uploading to {0} {1}", new String[] { upServer, upPath});

		Failsafe.with(policy).run(() -> {
			upload(upServer, upUser, upPass, upPath, BEST_FILE);
			verifyUpload(webfile, BEST_FILE);
		});

		String mailServer = System.getenv("MAIL_SERVER");
		String mailPort = System.getenv("MAIL_PORT");
		String mailTo = System.getenv("MAIL_TO");
		
		Failsafe.with(policy).run(() -> {
			mail(mailServer, mailPort, mailTo);
		});
	}
}
