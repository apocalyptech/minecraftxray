package com.plusminus.craft.dtf;

public class ShortTag extends Tag {
	public short value;
	public ShortTag(String name, short value) {
		this.name = name;
		this.value = value;
	}
	public String toString(int tab) {
		return tab(tab) + "TAG_Short(\"" + this.name + "\"): " + this.value + "\n";
	}
}
