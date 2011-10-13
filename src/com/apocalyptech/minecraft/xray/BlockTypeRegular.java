/**
 * Copyright (c) 2010-2011, Christopher J. Kucera
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

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.awt.Color;

import static com.apocalyptech.minecraft.xray.MinecraftConstants.*;

/**
 */
public class BlockTypeRegular extends BlockType
{

	// YAML Attributes
	private ArrayList<Integer> tex;
	private HashMap<Integer, ArrayList<Integer>> tex_data;
	private HashMap<String, ArrayList<Integer>> tex_direction;
	private HashMap<String, ArrayList<Integer>> tex_extra;

	// Other attributes
	private boolean generated_texture;

	public BlockTypeRegular()
	{
		super();
		this.generated_texture = false;
	}

	public void setTex(ArrayList<Integer> tex)
	{
		this.tex = tex;
	}

	public ArrayList<Integer> getTex()
	{
		return this.tex;
	}

	public void setTexReal(int tex)
	{
		this.setTex(BlockType.getTexCoords(tex));
	}

	public int getTexReal()
	{
		return getTexReal(this.getTex());
	}

	public void setTex_data(HashMap<Integer, ArrayList<Integer>> tex_data)
	{
		this.tex_data = tex_data;
	}

	public HashMap<Integer, ArrayList<Integer>> getTex_data()
	{
		return this.tex_data;
	}

	public void setTex_direction(HashMap<String, ArrayList<Integer>> tex_direction)
	{
		this.tex_direction = tex_direction;
	}

	public HashMap<String, ArrayList<Integer>> getTex_direction()
	{
		return this.tex_direction;
	}

	public void setTex_extra(HashMap<String, ArrayList<Integer>> tex_extra)
	{
		this.tex_extra = tex_extra;
	}

	public HashMap<String, ArrayList<Integer>> getTex_extra()
	{
		return this.tex_extra;
	}

	/**
	 * Normalizes our data from what we get from YAML, to a format that's
	 * easier to deal with in X-Ray.
	 */
	public void normalizeData()
		throws BlockTypeLoadException
	{
		// First call our parent
		super.normalizeData();

		// Now check for some required attributes
		if (this.tex == null)
		{
			if (this.idStr.equals("WATER") || this.idStr.equals("STATIONARY_WATER") ||
					this.idStr.equals("FIRE") || this.idStr.equals("PORTAL") || 
					this.idStr.equals("ENDER_PORTAL"))
			{
				this.generated_texture = true;
			}
			else
			{
				throw new BlockTypeLoadException("tex is a required attribute");
			}
		}
		else if (this.tex.size() != 2)
		{
			throw new BlockTypeLoadException("tex coordinates require two elements (X, Y)");
		}

		// Now do the actual normalizing
		if (!this.generated_texture)
		{
			this.tex_idx = this.getTexReal();
		}

		if (this.tex_data != null && this.tex_data.size() > 0)
		{
			this.texture_data_map = new HashMap<Byte, Integer>();
			for (Map.Entry<Integer, ArrayList<Integer>> entry : this.tex_data.entrySet())
			{
				this.texture_data_map.put(entry.getKey().byteValue(), BlockType.getTexReal(entry.getValue()));
			}
		}
		if (this.tex_direction != null)
		{
			this.texture_dir_map = new HashMap<DIRECTION_REL, Integer>();
			for (Map.Entry<String, ArrayList<Integer>> entry: this.tex_direction.entrySet())
			{
				DIRECTION_REL dir;
				try
				{
					dir = DIRECTION_REL.valueOf(entry.getKey());
				}
				catch (IllegalArgumentException e)
				{
					throw new BlockTypeLoadException("Invalid relative direction: " + entry.getKey());
				}
				this.texture_dir_map.put(dir, BlockType.getTexReal(entry.getValue()));
			}
		}
		if (this.tex_extra != null)
		{
			this.texture_extra_map = new HashMap<String, Integer>();
			for (Map.Entry<String, ArrayList<Integer>> entry : this.tex_extra.entrySet())
			{
				this.texture_extra_map.put(entry.getKey(), BlockType.getTexReal(entry.getValue()));
			}
		}
	}

}
