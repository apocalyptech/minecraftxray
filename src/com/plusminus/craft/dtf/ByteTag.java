package com.plusminus.craft.dtf;

public class ByteTag extends Tag {
	public byte value;
	public ByteTag(String name, byte b) {
		this.name = name;
		this.value = b;
	}
	
	public String toString(int tab) {
		return tab(tab) + "TAG_Byte(\"" + name + "\"): " + value + "\n";
	}
}
