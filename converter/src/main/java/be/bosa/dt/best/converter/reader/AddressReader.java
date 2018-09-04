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

import be.bosa.dt.best.converter.dao.Address;
import be.bosa.dt.best.converter.dao.BestRegion;
import be.bosa.dt.best.converter.dao.BestType;

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
public class AddressReader extends AbstractXMLReader<Address> {
	private final static QName ADDRESS = new QName(AbstractXMLReader.TNS, "Address");
	private final static QName MUNICIPALITY = new QName(AbstractXMLReader.ADD, "Municipality");
	private final static QName NAMESPACE = new QName(AbstractXMLReader.ADD, "namespace");
	private final static QName OBJECTID = new QName(AbstractXMLReader.ADD, "objectIdentifier");
	private final static QName POS = new QName(AbstractXMLReader.GML, "pos");
	private final static QName SRSNAME = new QName(AbstractXMLReader.GML, "srsName");
	private final static QName STATUS = new QName(AbstractXMLReader.ADD, "status");

	@Override
	protected QName getRoot() {
		return ADDRESS;
	}

	@Override
	protected Address getNextObj(XMLEventReader reader) throws XMLStreamException {
		Address obj = null;
		boolean inAssigned = false;
			
		while(reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement()) {
				QName el = event.asStartElement().getName();
				if (el.equals(ADDRESS)) {
					obj = new Address();
				} else if (obj != null) {
					if (el.equals(MUNICIPALITY)) {
						inAssigned = true;
					} else if (el.equals(NAMESPACE)) {
						String txt = reader.getElementText();
						if (inAssigned) { 
							obj.getCity().setNamespace(txt);
						} else {
							 obj.setNamespace(txt);
						}
					} else if (el.equals(OBJECTID)) {
						String txt = reader.getElementText();
						System.err.println(txt);
						if (inAssigned) {
							obj.getCity().setId(txt);
						} else {
							obj.setId(txt);
						}
					} else if (el.equals(POS)) {
						String srs = reader.getProperty(SRSNAME.getLocalPart()).toString();
						String txt = reader.getElementText();
						if (txt.contains(" ")) {
							String[] coords = txt.split(" ");
							int x = Integer.valueOf(coords[0]);
							int y = Integer.valueOf(coords[1]);
							obj.getPoint().set(x, y, srs);
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
					inAssigned = false;
				}
				if (el.equals(ADDRESS)) {
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
