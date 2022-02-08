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

import io.quarkus.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Perform some basic verification on BeST zip file
 * 
 * @author Bart Hanssens
 */
@ApplicationScoped
public class VerifyService {
	@ConfigProperty(name = "copier.mft.minsize")
	long minSize;

	@ConfigProperty(name = "copier.mft.files")
	List<String> expected;

	@Inject
	ZipService zip;

	/**
	 * Check BeST ZIP file
	 * 
	 * @param file file to check
	 * @throws java.io.IOException
	 */
	public void verify(String file) throws IOException {
		long fileSize = new File(file).length();
		if (fileSize < minSize) {
			throw new IOException("File too small: " + fileSize);
		}
		Log.infof("File size %s OK", fileSize);

		List<String> files = zip.listFiles(Paths.get(file));
		if (files.size() != expected.size()) {
			throw new IOException("Number of files is different: " + files.size());
		}
		for(String e: expected) {
			long count = files.stream().filter(f -> f.startsWith(e)).count();
			if (count == 0) {
				throw new IOException("Expected not found: " + e);
			}
			if (count > 1) {
				throw new IOException("Expected found multiple times: " + e);
			}
			Log.infof("Expected %s OK", e);			
		}
	}
}
