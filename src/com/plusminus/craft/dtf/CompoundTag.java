package com.plusminus.craft.dtf;

import java.util.ArrayList;

public class CompoundTag extends Tag {
	public ArrayList<Tag> value;
	public CompoundTag(String name, ArrayList<Tag> value) {
		this.name = name;
		this.value = value;
	}
	public Tag getTagWithName(String name) {
		for(Tag t : value) {
			if(t.name != null && t.name.equals(name)) {
				return t;
			}
		}
		return null;
	}
	public String toString(int tab) {
		String f = tab(tab);
		f += "TAG_Compound(\"" + name + "\")\n";
		f += tab(tab) + "(\n";
		for(Tag t : value) {
			f += t.toString(tab+1);
		}
		f += tab(tab) + "}\n";
		
		return f;
	}
}
