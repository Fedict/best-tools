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

import be.bosa.dt.best.dao.Address;
import be.bosa.dt.best.dao.BestObject;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.BestType;

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
public class AddressReader extends AbstractXMLReader<Address> {
	private final static QName ADDRESS = new QName(AbstractXMLReader.TNS, "Address");
	private final static QName MUNICIPALITY = new QName(AbstractXMLReader.ADD, "Municipality");
	private final static QName MUNICIPALITY_PART = new QName(AbstractXMLReader.ADD, "PartOfMunicipality");
	private final static QName POSTAL = new QName(AbstractXMLReader.ADD, "PostalInfo");
	private final static QName STREETNAME = new QName(AbstractXMLReader.ADD, "Streetname");
	private final static QName HOUSENUMBER = new QName(AbstractXMLReader.ADD, "houseNumber");
	private final static QName BOXNUMBER = new QName(AbstractXMLReader.ADD, "boxNumber");
	private final static QName NAMESPACE = new QName(AbstractXMLReader.ADD, "namespace");
	private final static QName OBJECTID = new QName(AbstractXMLReader.ADD, "objectIdentifier");
	private final static QName VERSIONID = new QName(AbstractXMLReader.ADD, "versionIdentifier");
	private final static QName POS = new QName(AbstractXMLReader.GML, "pos");
	private final static QName STATUS = new QName(AbstractXMLReader.ADD, "status");
	private final static QName SRSNAME = new QName("", "srsName");

	private final static Logger LOG = LoggerFactory.getLogger(AddressReader.class);
	
	private int nr = 0;
		
	
	@Override
	protected QName getRoot() {
		return ADDRESS;
	}

	@Override
	protected Address getNextObj(XMLEventReader reader) throws XMLStreamException {
		Address obj = null;
		BestObject withinObj = null;
		// catch unused elements
		BestObject dummy = new BestObject();
		
		while(reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement()) {
				QName el = event.asStartElement().getName();
				if (el.equals(ADDRESS)) {
					obj = new Address();
					withinObj = obj;
					if ((++nr % 100_000L) == 0) {
						LOG.info("Parsing address {}", nr);
					}
				} else if (obj != null) {
					if (el.equals(MUNICIPALITY)) {
						withinObj = obj.getCity();
					} else  if (el.equals(MUNICIPALITY_PART)) {
						withinObj = dummy;
					} else if (el.equals(POSTAL)) {
						withinObj = obj.getPostal();
					} else if (el.equals(STREETNAME)) {
						withinObj = obj.getStreet();
					} else if (el.equals(NAMESPACE)) {
						String txt = reader.getElementText();
						withinObj.setNamespace(txt);
					} else if (el.equals(OBJECTID)) {
						String txt = reader.getElementText();
						withinObj.setId(txt);
					} else if (el.equals(VERSIONID)) {
						String txt = reader.getElementText();
						withinObj.setVersion(txt);
					} else if (el.equals(HOUSENUMBER)) {
						String txt = reader.getElementText();
						obj.setNumber(txt);
					} else if (el.equals(BOXNUMBER)) {
						String txt = reader.getElementText();
						obj.setBox(txt);
					} else if (el.equals(POS)) {
						Object attr = event.asStartElement().getAttributeByName(SRSNAME).getValue();
						String srs = (attr == null) ? "" : attr.toString();
						String txt = reader.getElementText();
						try {
							obj.getPoint().setXY(txt, srs);
						} catch (NumberFormatException nfe) {
							LOG.warn("Error geoposition {} for {} ", nfe, obj.getId());
						}
					} else if (el.equals(STATUS)) {
						String txt = reader.getElementText();
						obj.setStatus(txt);
					}
				}
			}
			if (event.isEndElement()) {
				QName el = event.asEndElement().getName();
				if (el.equals(MUNICIPALITY)) {
					withinObj = obj;
				} else if (el.equals(MUNICIPALITY_PART)) {
					withinObj = obj;				
				} else if (el.equals(POSTAL)) {
					withinObj = obj;				
				} else if (el.equals(STREETNAME)) {
					withinObj = obj;
				} else if (el.equals(ADDRESS)) {
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
	public Stream<Address> read(BestRegion region, Path indir) {
		return read(region, BestType.ADDRESSES, indir);
	}
}