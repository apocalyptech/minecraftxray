/**
 * Copyright (c) 2005-2006, The ParticleReality Project
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

/***
 * Class holding a pair of two integers where the order is determined
 * first by the first integer and when these are equal, by the second
 * integer. This is used for holding resolution information
 * @author Vincent Vollers
 */
@SuppressWarnings("rawtypes")
public class IntegerPair implements Comparable {
	private int valueOne;
	private int valueTwo;
	
	public IntegerPair(int valueOne, int valueTwo) {
		this.valueOne = valueOne;
		this.valueTwo = valueTwo;
	}
	
	public int getValueOne() {
		return this.valueOne;
	}
	
	public int getValueTwo() {
		return this.valueTwo;
	}

	public int compareTo(Object o) {
		if(!(o instanceof IntegerPair)) {
			return -1;
		}
		IntegerPair i = (IntegerPair) o;
		
		if(i.getValueOne()>valueOne)
			return 1;
		
		if(i.getValueOne()<valueOne)
			return -1;
		
		if(i.getValueTwo()>valueTwo)
			return 1;
		
		if(i.getValueTwo()<valueTwo)
			return -1;

		return 0;
	}
}

