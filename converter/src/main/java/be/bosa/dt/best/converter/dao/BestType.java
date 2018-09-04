/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.bosa.dt.best.converter.dao;

/**
 * Helper enum for different types if data.
 * 
 * @author Bart Hanssens
 */
public enum BestType {
	ADDRESSES("Address"),
	MUNICIPALITIES("Municipality"),
	POSTALINFO("Postalinfo"),
	STREETNAMES("Streetname");
	
	private final String name;
		
	BestType(String name) {
		this.name = name;
	}

	public String getName() { return this.name; }
}
