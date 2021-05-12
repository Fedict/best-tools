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
package be.bosa.dt.hibernate;

import java.sql.SQLException;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.ByteOrder;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.geolatte.geom.builder.DSL;
import org.geolatte.geom.crs.CoordinateReferenceSystems;

/**
 * Very minimalistic Spatialite dialect for Hibernate
 * 
 * @author Bart Hanssens
 */
public class SpatialiteGeometryDecoder {
	public Geometry from(byte[] blob) throws SQLException {
		if (blob == null || blob.length < 60) {
			throw new SQLException("Only simple points are supported");
		}
		ByteBuffer buf = ByteBuffer.from(blob);
		buf.setByteOrder(blob[1] == 0x01 ? ByteOrder.NDR : ByteOrder.XDR);

		byte start = buf.get();
		byte bom = buf.get();
		int srid = buf.getInt();
		double minX = buf.getDouble();
		double minY = buf.getDouble();
		double maxX = buf.getDouble();
		double maxY = buf.getDouble();
		byte end = buf.get();
		int ctype = buf.getInt();
		
		double x = buf.getDouble();
		double y = buf.getDouble();

		Point p = DSL.point(CoordinateReferenceSystems.WGS84, DSL.g(x, y));
		return p;
	}
}
