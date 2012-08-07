package de.uni_potsdam.hpi.openmensa;

public class Canteen {
	public String name = "Dummy";
	public String key = null;
	
	public Canteen(String key, String name) {
		this.name = name;
		this.key = key;
	}
	
	public Canteen() {}

	@Override
	public String toString() {
		return name;
	}
}
