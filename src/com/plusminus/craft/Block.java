package com.plusminus.craft;


import static com.plusminus.craft.MineCraftConstants.*;
import java.lang.Thread;
import java.lang.StackTraceElement;
/**
 * @author Vincent Vollers
 *
 * A Block in the minecraft world
 * wildly abused :( for its 'simple' integer x,y,z properties 
 */
public class Block implements Comparable<Block> {

	
	public int x;
	public int y;
	public int z;
	public int t;
	public int cx;
	public int cz;

	public Block(int x, int y, int z) {
		this.x =x;
		this.y =y;
		this.z =z;
		this.cx = -x/16;
		this.cz = -z/16;
	}
	
	public int compareTo(Block a) {
		// TODO Auto-generated method stub
		
		Block b = (Block) a;
		if(b.x > x) {
			return 1;
		}
		if(b.x < x) {
			return -1;
		}
		if(b.z > z) {
			return 1;
		}
		if(b.z < z) {
			return -1;
		}
		if(b.y > y) {
			return 1;
		}
		if(b.y < y) {
			return -1;
		}
		return 0;
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof Block)) {
			return false;
		}
		Block p = (Block) o;
		return this.x == p.x && this.y == p.y && this.z == p.z;
	}
	
	public boolean equals(Block p) {
		return this.x == p.x && this.y == p.y && this.z == p.z;
	}
	public String toString() {
		return "Point( x=" + x + ", y=" + y + ", z=" + z + ")";
	}


}
