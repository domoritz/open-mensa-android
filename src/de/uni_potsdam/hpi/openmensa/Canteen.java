package de.uni_potsdam.hpi.openmensa;

public class Canteen {
	public String name = "Dummy";
	public int id = -1;
	
	public Canteen(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	public Canteen() {}

	@Override
	public String toString() {
		return name;
	}
}
