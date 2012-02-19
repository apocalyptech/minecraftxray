/**
 * Copyright (c) 2010-2012, Christopher J. Kucera
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

import java.util.HashMap;

/**
 * Class to aid in our "explored" highlight.  Basically just a wrapper
 * around a big ol' HashMap.  Note that there's currently no API to
 * expire information in here.  Technically that means that if a user
 * were to load X-Ray, travel a ways away, then load Minecraft and make
 * some changes, then go BACK into X-Ray and travel back to the original
 * location, this registry might differ from what's being shown.
 *
 * I'm not exactly overly concerned about that.
 */
public class LightSourceRegistry
{
	private HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>> registry;
	private final int radius = 3;
	
	public LightSourceRegistry()
	{
		// Note that the order will be: x, z, y
		registry = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>();
	}

	/**
	 * Adds a new entry to the registry.  Will also add all "adjacent" blocks
	 * based on the radius, though note that we're adding a cube centered around
	 * the given coordinates, not a sphere.
	 */
	public void add(int x, int y, int z)
	{
		for (int lx = x - this.radius;
				lx <= x + this.radius;
				lx++)
		{
			for (int lz = z - this.radius;
					lz <= z + this.radius;
					lz++)
			{
				for (int ly = y - this.radius;
						ly <= y + this.radius;
						ly++)
				{
					this._add(lx, ly, lz);
				}
			}
		}
	}

	/**
	 * The private function that actually adds a single point to our structure.
	 */
	private void _add(int x, int y, int z)
	{
		if (!this.registry.containsKey(x))
		{
			this.registry.put(x, new HashMap<Integer, HashMap<Integer, Boolean>>());
		}
		HashMap<Integer, HashMap<Integer, Boolean>> hm_x = this.registry.get(x);

		if (!hm_x.containsKey(z))
		{
			hm_x.put(z, new HashMap<Integer, Boolean>());
		}
		HashMap<Integer, Boolean> hm_z = hm_x.get(z);

		hm_z.put(y, true);
	}

	/**
	 * Checks to see if the given coordinate is in our registry
	 */
	public boolean check(int x, int y, int z)
	{
		if (!this.registry.containsKey(x))
		{
			return false;
		}
		HashMap<Integer, HashMap<Integer, Boolean>> hm_x = this.registry.get(x);

		if (!hm_x.containsKey(z))
		{
			return false;
		}
		HashMap<Integer, Boolean> hm_z = hm_x.get(z);

		if (!hm_z.containsKey(y))
		{
			return false;
		}

		return true;
	}
}
