package com.plusminus.craft.dtf;

public class IntTag extends Tag  {
	public int value;
	public IntTag(String name, int value) {
		this.name = name;
		this.value = value;
	}
	public String toString(int tab) {
		return tab(tab) + "TAG_Int(\"" + name + "\"): " + value + "\n";
	}
}
