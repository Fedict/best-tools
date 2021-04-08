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
package be.bosa.dt.best.xmlreader;

import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.BestType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;

/**
 * BeST XML file processor interface
 * 
 * @author Bart Hanssens
 * @param <T>
 */
public abstract class AbstractXMLReader<T> implements BestReader {
	// XML namespaces
	public final static String TNS = "http://fsb.belgium.be/mappingservices/FullDownload/v1_00";
	public final static String GML = "http://www.opengis.net/gml/3.2";
	public final static String ADD = "http://vocab.belgif.be/ns/inspire/";
	
	private final static XMLInputFactory2 FAC = (XMLInputFactory2) XMLInputFactory.newInstance();
	private final static Logger LOG = Logger.getLogger(AbstractXMLReader.class.getName());
	
	private BufferedInputStream bis;
	private XMLEventReader reader;
	
	/**
	 * Check if there is a file for a region.
	 * File names should follow the pattern "Region""Suffix""...""ext"
	 * 
	 * @param indir input directory
	 * @param region region
	 * @param type object type
	 * @param ext file extension
	 * @return path to file
	 * @throws IOException when no matching file was found 
	 */
	protected Path checkFile(Path indir, BestRegion region, BestType type, String ext) throws IOException {
		String start = (region.getName() + type.getName()).toLowerCase();
		
		Path xml = Files.list(indir).filter(p -> {
			String name = p.getFileName().toString().toLowerCase();
			return name.startsWith(start) && name.endsWith(ext);
		}).findFirst().orElseThrow(() -> new IOException("No file starting with " + start + " found"));

		if (Files.isRegularFile(xml) && Files.isReadable(xml)) {
			return xml;
		}
		throw new IOException("File not readable");
	}

	protected abstract QName getRoot();

	/**
	 * Just read the root element
	 * 
	 * @param reader
	 * @throws XMLStreamException 
	 */
	protected void start(XMLEventReader reader) throws XMLStreamException {
		while(reader.hasNext()) {
			XMLEvent peek = reader.peek();
			if (peek.isStartElement()) {
				QName el = peek.asStartElement().getName();
				if (el.equals(getRoot())) {
					return;
				}
			}
			reader.nextEvent();
		}
	}
	
	/**
	 * Check if there is a next object
	 * 
	 * @param reader XML reader
	 * @return true if there is a next object
	 * @throws XMLStreamException 
	 */
	protected boolean hasNextObj(XMLEventReader reader) throws XMLStreamException {
		for(;;) {
			XMLEvent peek = reader.peek();
			int type = peek.getEventType();
			if (type == XMLEvent.START_ELEMENT) {
				return true;
			}
			if (type == XMLEvent.END_DOCUMENT) {
				return false;
			}
			reader.nextEvent();
		}
	}
	
	/**
	 * Get next object, if any
	 * 
	 * @param reader XML reader
	 * @return object
	 * @throws XMLStreamException 
	 */
	protected abstract T getNextObj(XMLEventReader reader) throws XMLStreamException;

	/**
	 * Turn XML stream into iterator for JAVA 8 stream API
	 * 
	 * @param reader XML stream
	 * @return iterator of objects
	 */
	protected Iterator<T> getIterator(XMLEventReader reader) {
		return new Iterator<T>() {	
			@Override
			public boolean hasNext() {
				try {
					return hasNextObj(reader);
				} catch (XMLStreamException ex) {
					LOG.log(Level.SEVERE, "Error peeking at next object", ex);
					closeReader();
				}
				return false;
			}

			@Override
			public T next() {
				try {
					return getNextObj(reader);
				} catch (XMLStreamException ex) {
					LOG.log(Level.SEVERE, "Error getting next object", ex);
					closeReader();
				}
				return null;
			}
		};
	}

	/**
	 * Gracefully close the XML reader and other input streams
	 */
	public void closeReader() {
		if (reader != null) {
			try {
				reader.close();
			} catch (XMLStreamException xse) {
				LOG.log(Level.WARNING, "Error closing reader", xse);
				// do nothing
			}
		}
		if (bis != null) {
			try {
				bis.close();
			} catch (IOException ioe) {
				LOG.log(Level.WARNING, "Error closing buffered input stream", ioe);
			}
		}
	}

	@Override
	public Stream<T> read(BestRegion region, BestType type, Path indir) {
		Path file;
		
		try {
			file = checkFile(indir, region, type, "xml");
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, "Error, no XML file found for {0} in {1}", 
									new String[] { region.getName(), indir.toFile().toString() });
			return Stream.empty();
		}	
		LOG.log(Level.INFO, "Reading {0}", file);
	
		FAC.configureForSpeed();
		
		try {
			// not in try-resourcese, input/reader must be closed in stream iterator
			bis = new BufferedInputStream(Files.newInputStream(file));
			reader = FAC.createXMLEventReader(bis);
			
			start(reader);
			// to Java 8 stream
			Iterator<T> iter = getIterator(reader);
			Spliterator<T> split = Spliterators.spliteratorUnknownSize(iter, Spliterator.IMMUTABLE);
			return StreamSupport.stream(split, true);
		} catch (XMLStreamException|IOException ex) {
			closeReader();
			LOG.log(Level.SEVERE, "Error parsing XML", ex);
			return Stream.empty();
		}
	}
}
