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
import be.bosa.dt.best.converter.dao.BestType;
import be.bosa.dt.best.converter.dao.Streetname;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
	
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * BeST XML file processor interface
 * 
 * @author Bart Hanssens
 */
public class StreetnameReader extends AbstractXMLReader<Streetname> {
	private final static QName LANGUAGE = new QName(AbstractXMLReader.ADD, "language");
	private final static QName NAMESPACE = new QName(AbstractXMLReader.ADD, "namespace");
	private final static QName OBJECTID = new QName(AbstractXMLReader.ADD, "objectIdentifier");;
	private final static QName SPELLING = new QName(AbstractXMLReader.ADD, "spelling");
	private final static QName STATUS = new QName(AbstractXMLReader.ADD, "status");
	private final static QName STREETNAME = new QName(AbstractXMLReader.TNS, "Streetname");
	
	private final Logger LOG = LoggerFactory.getLogger(StreetnameReader.class);

	
	@Override
	protected Streetname getNextObj(XMLEventReader reader) throws XMLStreamException {
		Streetname obj = null;
		
		while(reader.hasNext()) {
			XMLEvent event = reader.nextTag();
			if (event.isStartElement()) {
				QName el = event.asStartElement().getName();
				if (el.equals(STREETNAME)) {
					obj = new Streetname();
				}
				if (el.equals(NAMESPACE)) {
					
				}
			}
			if (event.isEndElement() && event.asEndElement().getName().equals(STREETNAME)) {
				return obj;
			}
		}
		throw new XMLStreamException("Was expecting next object");
	}
	
	/**
	 * Get a Java stream of streetnames
	 * 
	 * @param region region
	 * @param indir input directory
	 * @return 
	 */
	public Stream<Streetname> read(BestRegion region, Path indir) {
		return read(region, BestType.STREETNAMES, indir);
	}
}
