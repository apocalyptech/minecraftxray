package com.plusminus.craft.dtf;

public class DoubleTag extends Tag {
	public double value;
	public DoubleTag(String name, double value) {
		this.name = name;
		this.value = value;
	}
	
	public String toString(int tab) {
		return tab(tab) + "TAG_Double(\"" + name + "\"): " + value + "\n";
	}
}
