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
import be.bosa.dt.best.converter.dao.Municipality;

import java.nio.file.Path;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * BeST XML file processor interface
 * 
 * @author Bart Hanssens
 */
public class MunicipalityReader extends AbstractXMLReader<Municipality> {
	private final static QName LANGUAGE = new QName(AbstractXMLReader.ADD, "language");
	private final static QName MUNICIPALITY = new QName(AbstractXMLReader.TNS, "Municipality");
	private final static QName NAMESPACE = new QName(AbstractXMLReader.ADD, "namespace");
	private final static QName OBJECTID = new QName(AbstractXMLReader.ADD, "objectIdentifier");;
	private final static QName SPELLING = new QName(AbstractXMLReader.ADD, "spelling");

	@Override
	protected QName getRoot() {
		return MUNICIPALITY;
	}
	
	@Override
	protected Municipality getNextObj(XMLEventReader reader) throws XMLStreamException {
		Municipality obj = null;
		String lang = "";
		
		while(reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement()) {
				QName el = event.asStartElement().getName();
				if (el.equals(MUNICIPALITY)) {
					obj = new Municipality();
				} else if (obj != null) {
					if (el.equals(NAMESPACE)) {
						String txt = reader.getElementText();
						obj.setNamespace(txt);
					} else if (el.equals(OBJECTID)) {
						String txt = reader.getElementText();
						obj.setId(txt);
					} else if (el.equals(LANGUAGE)) {
						lang = reader.getElementText();
					} else if (el.equals(SPELLING)) {
						String txt = reader.getElementText();
						obj.setName(txt, lang);
					}
				}
			}
			if (event.isEndElement() && event.asEndElement().getName().equals(MUNICIPALITY)) {
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
	public Stream<Municipality> read(BestRegion region, Path indir) {
		return read(region, BestType.MUNICIPALITIES, indir);
	}
}
