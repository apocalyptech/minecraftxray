package com.plusminus.craft;


import static com.plusminus.craft.MineCraftConstants.*;
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
	public boolean isMineral;
	
	public Block(int x, int y, int z) {
		this(x,y,z,0);
	}
	public Block(int x, int y, int z, int t) {
		this.x =x;
		this.y =y;
		this.z =z;
		this.t =t;
		testMineral();
	}
	
	public void testMineral() {
		this.isMineral = false;
		for(int i=0;i<TEXTURE_ORES.length; i++) {
			if(TEXTURE_ORES[i] == t) {
				this.isMineral = true;
				break;
			}
		}
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
		return "Point( x=" + x + ", y=" + y + ", z=" + z + ", t=" +t + ", isMineral=" + isMineral + ")";
	}


}
