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

import java.nio.ByteOrder;
import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;


/**
 * Very minimalistic Spatialite dialect for Hibernate
 * 
 * @author Bart Hanssens
 */
public class SpatialiteGeometryEncoder {
	public byte[] to(Geometry geometry) {
		System.err.println("QQQ encode to");
		ByteOrder nativeOrder = ByteOrder.nativeOrder();
		Envelope envelope = geometry.getEnvelope();
		
		Point p = (Point) geometry;
		p.getPosition().getCoordinate(0);
		p.getPosition().getCoordinate(0);
		
		ByteBuffer buf = ByteBuffer.allocate(84);
		buf.put((byte) 0x00);
		buf.put(nativeOrder.equals(ByteOrder.BIG_ENDIAN) ? (byte) 0x00 : (byte) 0x01);
		buf.putInt(geometry.getSRID());
		buf.putDouble(envelope.lowerLeft().getCoordinate(0));
		buf.putDouble(envelope.lowerLeft().getCoordinate(1));
		buf.putDouble(envelope.upperRight().getCoordinate(0));
		buf.putDouble(envelope.upperRight().getCoordinate(1));
		buf.put((byte) 0x7C);
		buf.putInt(0x01);
		buf.putDouble(p.getPosition().getCoordinate(0));
		buf.putDouble(p.getPosition().getCoordinate(1));
		buf.put((byte) 0xFE);
		
		return buf.toByteArray();
	}
}
