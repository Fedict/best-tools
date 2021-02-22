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

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;


/**
 * Copy Belgian Streets and Addresses (BeST) zipped XML file from an SFTP server to a web site
 * 
 * @author Bart Hanssens
 */
public class Main {
	private final static Logger LOG  = Logger.getLogger(Main.class.getName());

	// in case of an error, retry download / upload 
	private final static RetryPolicy<Object> policy = new RetryPolicy<>()
							.withDelay(Duration.ofSeconds(30))
							.withMaxRetries(3)
							.onRetry(e -> LOG.warning("Failure, retrying..."))
							.onRetriesExceeded(e -> LOG.severe("Max retries exceeded"));
	
	private final static Copier copier = new Copier();

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
		
		LOG.log(Level.INFO, "Downloading from {0} {1}", new String[] { downServer, downPath });

		String localFile = System.getenv("LOCAL_FILE");
		File f = new File(localFile);
				
		Failsafe.with(policy).run(() -> {
			f.delete();
			copier.download(downServer, downUser, downPass, downPath, f.getAbsolutePath());
			copier.verifyZip(f, Copier.strToLong(minSize));
		});

		LOG.log(Level.INFO, "File size {0}", f.length());

		String upServer = System.getenv("UPLOAD_SERVER");
		String upPath = System.getenv("UPLOAD_PATH");
		String upUser = System.getenv("UPLOAD_USER");
		String upPass = System.getenv("UPLOAD_PASS");		
		String upFile = System.getenv("UPLOAD_FILE");		

		LOG.log(Level.INFO, "Uploading to {0} {1}", new String[] { upServer, upPath });

		Failsafe.with(policy).run(() -> {
			copier.upload(upServer, upUser, upPass, upPath, f.getAbsolutePath());
			copier.verifyUpload(upFile, f.length());
		});
	}
}
