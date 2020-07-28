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
package be.bosa.dt.best.converter.writer;

import be.bosa.dt.best.dao.Address;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.dao.BestType;
import be.bosa.dt.best.dao.Municipality;
import be.bosa.dt.best.dao.Postal;
import be.bosa.dt.best.dao.Streetname;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BeST result Shapefile writer
 * 
 * @author Bart Hanssens
 */
public class BestWriterShape implements BestWriter {
	private final GeometryFactory FAC = JTSFactoryFinder.getGeometryFactory();
	
	private final static Logger LOG = LoggerFactory.getLogger(BestWriterShape.class);
	
	/**
	 * Get shapefile feature type
	 * 
	 * @return
	 * @throws FactoryException 
	 */
	private static SimpleFeatureType getFeatureType() throws FactoryException {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Addresses");
		builder.setCRS(CRS.decode("EPSG:31370"));

        // add attributes in order
        builder.add("the_geom", Point.class);
	//	builder.length(20).add("id", String.class);
		builder.length(8).add("number", String.class);
        builder.length(8).add("box", String.class);
		builder.length(10).add("street_id", String.class);
		builder.length(60).add("street_nl", String.class);
        builder.length(60).add("street_fr", String.class);
		builder.length(10).add("city_id", String.class);
		builder.length(40).add("city_nl", String.class);
        builder.length(40).add("city_fr", String.class);
		builder.length(10).add("postal_id", String.class);
		builder.length(40).add("postal_nl", String.class);
        builder.length(40).add("postal_fr", String.class);
		builder.length(15).add("status", String.class);
		
        return builder.buildFeatureType();
	}
	
	/**
	 * Get shapefile datastore
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	private ShapefileDataStore getDatastore(Path file) throws IOException {
		ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
		ShapefileDataStore store = (ShapefileDataStore) factory.createDataStore(file.toUri().toURL());
	//	store.setMemoryMapped(true);
		store.setBufferCachingEnabled(true);
		
		return store;
	}
	
	@Override
	public Map<String,String[]> writeMunicipalities(BestRegion region, Path outdir, Stream<Municipality> cities) {
		Map<String,String[]> cache = new HashMap<>();
		cities.forEach(s -> cache.put(s.getId(), new String[] { s.getName("nl"), s.getName("fr")}) );
		
		return cache;
	}

	@Override
	public Map<String,String[]> writePostals(BestRegion region, Path outdir, Stream<Postal> postals) {
		Map<String,String[]> cache = new HashMap<>();
		postals.forEach(s -> cache.put(s.getId(), new String[] { s.getName("nl"), s.getName("fr")}) );
		
		return cache;
	}
	
	@Override
	public Map<String,String[]> writeStreets(BestRegion region, Path outdir, Stream<Streetname> streets,
											Map<String,String[]> cities) {
		Map<String,String[]> cache = new HashMap<>();
		streets.forEach(s -> cache.put(s.getId(), new String[] { s.getName("nl"), s.getName("fr")}) );
		
		return cache;
	}

	/**
	 * Build a shapefile feature from an address
	 * 
	 * @param builder
	 * @param address
	 * @param cCities citie name cache
 	 * @param cStreet street name cache
	 * @param cPostal postal name cache
	 * @return shapefile feature
	 */
	private SimpleFeature buildFeature(SimpleFeatureBuilder builder, Address address, 
										String[] cCities, String[] cStreet, String[] cPostal) {
		Coordinate xy = new Coordinate(address.getPoint().getX(), address.getPoint().getY());
		Point point = FAC.createPoint(xy);
		builder.add(point);
		//		builder.add(s.getId());
		builder.add(address.getNumber());
		builder.add(address.getBox());
		builder.add(address.getStreet().getId());
		builder.add(cStreet[0]);
		builder.add(cStreet[1]);
		builder.add(address.getCity().getId());
		builder.add(cCities[0]);
		builder.add(cCities[1]);		
		builder.add(address.getPostal().getId());
		builder.add(cPostal[0]);
		builder.add(cPostal[1]);
		builder.add(address.getStatus());
				
		return builder.buildFeature(address.getId());
	}
	
	/**
	 * Store features into shapefile
	 * 
	 * @param writer
	 * @param features
	 * @throws IOException
	 */
	private void writeFeatures(FeatureWriter<SimpleFeatureType,SimpleFeature> writer, 
								List<SimpleFeature> features) throws IOException {
		for (SimpleFeature f: features) {
			SimpleFeature toWrite = writer.next();
			for (int j = 0; j < toWrite.getType().getAttributeCount(); j++) {
				String name = toWrite.getType().getDescriptor(j).getLocalName();
				toWrite.setAttribute(name, f.getAttribute(name));
			}
			writer.write();
		}
	}
	
	@Override
	public void writeAddresses(BestRegion region, Path outdir, Stream<Address> addresses,
			Map<String,String[]> streets, Map<String,String[]> cities, Map<String,String[]> postals) {
		Path file = BestWriter.getPath(outdir, region, BestType.ADDRESSES, "shp");
		LOG.info("Writing {}", file);
				
		try {
			SimpleFeatureType ftype = getFeatureType();
			
			ShapefileDataStore datastore = getDatastore(file);
			datastore.createSchema(ftype);
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(ftype);
			
			//write batches of 100_000 features to the shape file
			int i = 0;
			Iterator iterator = addresses.iterator();
			List<SimpleFeature> features = new ArrayList<>(100_000);
			String typeName = datastore.getTypeNames()[0];
			
			// low-level API is much faster for large numbers of addresses
			try( FeatureWriter<SimpleFeatureType, SimpleFeature> writer = 
				datastore.getFeatureWriter(typeName, Transaction.AUTO_COMMIT)) {
				datastore.setIndexCreationEnabled(false);
				
				while (iterator.hasNext()) {
					Address s = (Address) iterator.next();
					
					String[] cCities = cities.getOrDefault(s.getCity().getId(), new String[2]);
					String[] cStreet = streets.getOrDefault(s.getStreet().getId(), new String[2]);
					String[] cPostal = postals.getOrDefault(s.getPostal().getId(), new String[2]);
					features.add(buildFeature(builder, s, cCities, cStreet, cPostal));
					
					if (++i % 100_000 == 0) {
						writeFeatures(writer, features);
						features.clear();
					}
				}
				writeFeatures(writer, features);
				features.clear();
				
				datastore.setIndexCreationEnabled(true);
			}
		} catch (IOException ioe) {
			LOG.error("Can't write shapefile", ioe);
		} catch (FactoryException fe) {
			LOG.error("Coordinate system not supported", fe);
		}
	}
}
