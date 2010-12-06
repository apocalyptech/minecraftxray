package com.plusminus.craft.dtf;

public class FloatTag extends Tag {
	public float value;
	public FloatTag(String name, float value) {
		this.name = name;
		this.value = value;
	}
	public String toString(int tab) {
		return tab(tab) + "TAG_Float(\"" + name + "\"): " + value + "\n";
	}
}
