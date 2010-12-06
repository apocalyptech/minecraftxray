package com.plusminus.craft.dtf;

public class ByteArrayTag extends Tag {
	public byte[] value;
	public ByteArrayTag(String name, byte[] value) {
		this.name = name;
		this.value = value;
	}
	
	public String toString(int tab) {
		String f = tab(tab) + "TAG_ByteArray(\"" + name + "\"): [";
	/*	for(byte b : value) {
			f += Integer.toHexString(b) + ", ";
		}*/
		
		//f = f.substring(0,f.length()-2);
		f += "" + value.length + " bytes";
		f += "]\n";
		return f;
	}
}
