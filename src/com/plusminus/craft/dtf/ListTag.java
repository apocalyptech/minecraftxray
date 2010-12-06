package com.plusminus.craft.dtf;

import java.util.ArrayList;

public class ListTag extends Tag {
	public ArrayList<Tag> value;
	public ListTag(String name, ArrayList<Tag> value) {
		this.name = name;
		this.value = value;
	}
	public String toString(int tab) {
		String f = tab(tab);
		f += "TAG_List(\"" + name + "\")\n";
		f += tab(tab) + "(\n";
		for(Tag t : value) {
			f += t.toString(tab+1);
		}
		f += tab(tab) + "}\n";
		
		return f;
	}
}
