package de.uni_potsdam.hpi.openmensa;

public class Canteen {
	public String name;
	public int id;
	
	public Canteen(String name, int id) {
		this.name= name;
		this.id = id;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
