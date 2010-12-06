package com.plusminus.craft.dtf;

public class StringTag extends Tag {
	public String value;
	public StringTag(String name, String value) {
		this.name = name;
		this.value = value;
	}
	public String toString(int tab) {
		return tab(tab) + "TAG_String(\"" + name + "\"): " + value + "\n";
	}
}
