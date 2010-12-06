package com.plusminus.craft.dtf;

public abstract class Tag {
	public Tag nextTag;
	public String name;
	
	public String tab(int t) {
		String f = "";
		for(int i=0;i<t;i++) {
			f += "  ";
		}
		return f;
	}
	public String toString() {
		return toString(0);
	}
	public abstract String toString(int tab);
}
