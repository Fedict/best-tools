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
package be.bosa.dt.best.dao;

/**
 * Helper class for geo point
 * 
 * @author Bart Hanssens
 */
public class Geopoint {
	private double x;
	private double y;
	private String srs;

	/**
	 * Get X coordinate
	 * 
	 * @return x
	 */
	public double getX() {
		return x;
	}

	/**
	 * Set X coordinate
	 * @param x 
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * Get Y coordinate
	 * 
	 * @return y
	 */
	public double getY() {
		return y;
	}

	/**
	 * Set Y coordinate
	 * 
	 * @param y 
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Get GML srsName (ID of coordinate system being used)
	 * 
	 * @return 
	 */
	public String getSrs() {
		return srs;
	}

	/**
	 * Set GML srsName (ID of coordinate system being used)
	 * 
	 * @param srs 
	 */
	public void setSrs(String srs) {
		this.srs = srs;
	}
	
	/**
	 * Convenience method for setting X,Y and system at once
	 * 
	 * @param xy
	 * @param srs
	 * @throws NumberFormatException
	 */
	public void setXY(String xy, String srs) throws NumberFormatException {
		if (xy == null || !xy.contains(" ")) {
			throw new NumberFormatException("Not a valid X Y coordinate string");
		}
		if (!srs.endsWith("31370")) {
			throw new NumberFormatException("Not Lambert 72 projection");
		}
		String[] coords = xy.split(" ");
		this.x = Double.valueOf(coords[0]);
		this.y = Double.valueOf(coords[1]);
		this.srs = "31370";
	}		
	
	/**
	 * Constructor
	 */
	public Geopoint() {
	}
}