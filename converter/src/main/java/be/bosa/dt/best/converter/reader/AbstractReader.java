/*
 * Copyright (c) 2018, FPS BOSA DG DT
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
package be.bosa.dt.best.converter.reader;

import be.bosa.dt.best.converter.dao.BestRegion;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * BeST XML file processor interface
 * 
 * @author Bart Hanssens
 * @param <T>
 */
public abstract class AbstractReader<T> implements BestReader {
	public final static String TNS = "http://fsb.belgium.be/mappingservices/FullDownload/v1_00";
	public final static String ADD = "http://vocab.belgif.be/ns/inspire/";
	
	private final static XMLInputFactory2 FAC = (XMLInputFactory2) XMLInputFactory.newInstance();  
	
	private final static Logger LOG = LoggerFactory.getLogger(AbstractReader.class);
	
	/**
	 * Check if there is a file for a region.
	 * File names should follow the pattern "Region""Suffix""...""ext"
	 * 
	 * @param indir input directory
	 * @param region region
	 * @param suffix 
	 * @param ext file extension
	 * @return path to file
	 * @throws IOException when no matching file was found 
	 */
	protected Path checkFile(Path indir, BestRegion region, String suffix, String ext) throws IOException {
		String start = (region.getName() + suffix).toLowerCase();
		
		Path xml = Files.list(indir).filter(p -> {
			String name = p.getFileName().toString().toLowerCase();
			return name.startsWith(start) && name.endsWith(ext);
		}).findFirst().orElseThrow(() -> new IOException("No file starting with " + start + " found"));

		if (Files.isRegularFile(xml) && Files.isReadable(xml)) {
			return xml;
		}
		throw new IOException("File not readable");
	}
	
	/**
	 * Gracefully close the XML reader
	 * 
	 * @param reader 
	 */
	public static void closeReader(XMLEventReader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (XMLStreamException xse) {
				LOG.warn("Error closing reader", xse);
				// do nothing
			}
		}
	}
	
	protected abstract boolean hasNext(XMLEventReader reader) throws XMLStreamException;
	
	protected abstract T getNext(XMLEventReader reader) throws XMLStreamException;

	
	protected Iterator<T> getIterator(XMLEventReader reader) {
		return new Iterator<T>() {	
			@Override
			public boolean hasNext() {
				try {
					return hasNext(reader);
				} catch (XMLStreamException ex) {
					LOG.error("Error peeking");
					closeReader(reader);
				}
				return false;
			}

			@Override
			public T next() {
				try {
					return getNext(reader);
				} catch (XMLStreamException ex) {
					LOG.error("Error parsing");
					closeReader(reader);
				}
				return null;
			}
		};
	}
	
	@Override
	public Stream<T> read(BestRegion region, Path indir) {
		Path file;
		
		try {
			file = checkFile(indir, region, getSuffix(), "xml");
		} catch (IOException ex) {
			LOG.error("Error, no XML file found for {} in {}", region.getName(), indir);
			return Stream.empty();
		}	
		LOG.info("Reading {}", file);
		
		FAC.configureForSpeed();
		XMLEventReader reader = null;
		
		try (	InputStream is = Files.newInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(is)) {
			reader = FAC.createXMLEventReader(bis);
  
			Iterator<T> iter = getIterator(reader);
			Spliterator<T> split = Spliterators.spliteratorUnknownSize(iter, Spliterator.IMMUTABLE);
			return StreamSupport.stream(split, true);
		} catch (IOException|XMLStreamException ex) {
			closeReader(reader);
			LOG.error("Error parsing XML", ex);
			return Stream.empty();
		}
	}
}
