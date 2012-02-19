/**
 * Copyright (c) 2010-2012, Vincent Vollers and Christopher J. Kucera
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Minecraft X-Ray team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL VINCENT VOLLERS OR CJ KUCERA BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.apocalyptech.minecraft.xray;

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
