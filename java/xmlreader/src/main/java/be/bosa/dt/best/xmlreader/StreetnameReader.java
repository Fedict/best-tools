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

import be.bosa.dt.best.dao.BestObject;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.BestType;
import be.bosa.dt.best.dao.Street;

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
public class StreetnameReader extends AbstractXMLReader<Street> {
	private final static QName STREETNAME = new QName(AbstractXMLReader.TNS, "Streetname");
	//private final static QName STREETNAME_CODE = new QName(AbstractXMLReader.ADD, "streetnameCode");
	private final static QName STREETNAME_NAME = new QName(AbstractXMLReader.ADD, "streetname");
	private final static QName MUNICIPALITY = new QName(AbstractXMLReader.ADD, "Municipality");
	private final static QName NAMESPACE = new QName(AbstractXMLReader.ADD, "namespace");
	private final static QName OBJECTID = new QName(AbstractXMLReader.ADD, "objectIdentifier");
	private final static QName VERSIONID = new QName(AbstractXMLReader.ADD, "versionIdentifier");
	private final static QName LANGUAGE = new QName(AbstractXMLReader.ADD, "language");
	private final static QName SPELLING = new QName(AbstractXMLReader.ADD, "spelling");
	private final static QName STATUS = new QName(AbstractXMLReader.ADD, "status");
	private final static QName VALID_FROM = new QName(AbstractXMLReader.ADD, "validFrom");
	private final static QName VALID_TO = new QName(AbstractXMLReader.ADD, "validTo");
	
	@Override
	protected QName getRoot() {
		return STREETNAME;
	}

	@Override
	protected Street getNextObj(XMLEventReader reader) throws XMLStreamException {
		Street obj = null;
		BestObject withinObj = null;
		String lang = "";
		String spelling = "";
		
		while(reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement()) {
				QName el = event.asStartElement().getName();
				if (el.equals(STREETNAME)) {
					obj = new Street();
					withinObj = obj;
				} else if (obj != null) {
					if (el.equals(MUNICIPALITY)) {
						withinObj = obj.getCity();
					} else if (el.equals(NAMESPACE)) {
						String txt = reader.getElementText();
						withinObj.setNamespace(txt);
					} else if (el.equals(OBJECTID)) {
						String txt = reader.getElementText();
						withinObj.setId(txt);
					} else if (el.equals(VERSIONID)) {
						String txt = reader.getElementText();
						withinObj.setVersion(txt);
					} else if (el.equals(STREETNAME_NAME)) {
						lang = "";
						spelling = "";
					} else if (el.equals(LANGUAGE)) {
						lang = reader.getElementText();
					} else if (el.equals(SPELLING)) {
						spelling = reader.getElementText();
					} else if (el.equals(STATUS)) {
						String txt = reader.getElementText();
						obj.setStatus(txt);
					} else if (el.equals(VALID_FROM)) {
						String txt = reader.getElementText();
						obj.setFromDate(txt);
					} else if (el.equals(VALID_TO)) {
						String txt = reader.getElementText();
						obj.setTillDate(txt);
					}
				}
			}
			if (event.isEndElement()) {
				QName el = event.asEndElement().getName();
				if (el.equals(MUNICIPALITY)) {
					withinObj = obj;
				} else if (el.equals(STREETNAME_NAME)) {
					obj.setName(spelling, lang);
				} else if (el.equals(STREETNAME)) {
					return obj;
				}
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
	public Stream<Street> read(BestRegion region, Path indir) {
		return read(region, BestType.STREETNAMES, indir);
	}
}
