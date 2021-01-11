/*
 * Copyright (c) 2020, FPS BOSA DG DT
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
package be.bosa.dt.best.dbloader;

import be.bosa.dt.best.dao.Address;
import be.bosa.dt.best.dao.BestRegion;
import be.bosa.dt.best.xmlreader.AddressReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.stream.Stream;

import org.h2gis.ext.H2GISExtension;

/**
 * Loads XML BeST data into an RDBMS, in this case H2GIS
 * Requires directory of unzipped
 * 
 * @author Bart Hanssens
 */
public class Main {

	private static void loadAddresses(PreparedStatement prep, Path xmlPath) throws SQLException {
		for (BestRegion reg: new BestRegion[] { BestRegion.BRUSSELS, BestRegion.FLANDERS, BestRegion.WALLONIA }) {
			System.out.println("Starting addresses " + reg.getName());
			int cnt = 0;

			AddressReader reader = new AddressReader();
			Stream<Address> addresses = reader.read(reg, xmlPath);
			Iterator<Address> iter = addresses.iterator();

			while (iter.hasNext()) {
				Address a = iter.next();
				prep.setString(1, a.getIDVersion());
				prep.setString(2, a.getCity().getIDVersion());
				prep.setString(3, a.getCityPart().getIDVersion());
				prep.setString(4, a.getStreet().getIDVersion());
				prep.setString(5, a.getNumber());
				prep.setString(6, a.getBox());
				//prep.setString(7, a.getPoint());
			
				prep.addBatch();
				if (++cnt % 10_000 == 0) {
					prep.executeBatch();
					System.out.println("Inserted " + cnt);
				}
			}
			prep.executeBatch();
			System.out.println("Inserted " + cnt);
		}
	}

	private static void loadData(Path dbPath, Path xmlPath) throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		String str = "jdbc:h2:" + dbPath.toString();

		try(Connection conn = DriverManager.getConnection(str, "sa", "sa")) {
			H2GISExtension.load(conn);
		}

		try(Connection conn = DriverManager.getConnection(str, "sa", "sa")) {
			Statement stmt = conn.createStatement();
			stmt.execute("CREATE TABLE addresses(" +
							"id VARCHAR(96) NOT NULL, " +
							"city_id VARCHAR(96), " +
							"part_id VARCHAR(96), " +
							"street_id VARCHAR(96), " +
							"houseno VARCHAR(12), " +
							"boxno VARCHAR(40))");
		}
			
		try(Connection conn = DriverManager.getConnection(str, "sa", "sa")) {
			PreparedStatement prep = conn.prepareStatement("INSERT INTO addresses"
				+ " VALUES (?, ?, ?, ?, ?, ?)");
			loadAddresses(prep, xmlPath);
		}
		
		try(Connection conn = DriverManager.getConnection(str, "sa", "sa")) {
			Statement stmt = conn.createStatement();
			stmt.execute("CREATE PRIMARY KEY ON addresses(id)");
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: dbloader xml-directory db-directory");
			System.exit(-1);
		}
		
		Path xmlPath = Paths.get(args[0]);
		if (!xmlPath.toFile().exists()) {
			System.err.println("BEST directory does not exist");
			System.exit(-2);
		}
		Path dbPath = Paths.get(args[1]);
		
		try {
			loadData(dbPath, xmlPath);
		} catch (Exception e) {
			System.err.println("Failed: " + e.getMessage());
			e.printStackTrace();
			System.exit(-3);
		}
		System.out.println("Done");
	}
}
