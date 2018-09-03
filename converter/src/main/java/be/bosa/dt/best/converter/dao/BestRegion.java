/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.bosa.dt.best.converter.dao;

/**
 * Helper enum for 3 regions in Belgium providing the address data.
 * 
 * @author Bart Hanssens
 */
public enum BestRegion {
	BRUSSELS("B", "Brussels"),
	FLANDERS("F", "Flanders"),
	WALLONIA("W", "Wallonia");
		
	private final String code;
	private final String name;
		
	BestRegion(String code, String name) {
		this.code = code;
		this.name = name;
	}
		
	public String getCode() { return this.code; }
	public String getName() { return this.name; }
}
