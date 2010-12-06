package com.plusminus.craft.dtf;

public class LongTag extends Tag {
	public long value;
	public LongTag(String name, long value) {
		this.name = name;
		this.value = value;
	}
	public String toString(int tab) {
		return tab(tab) + "TAG_Long(\"" + name + "\"): " + value + "\n";
	}
}
