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

import static com.apocalyptech.minecraft.xray.MineCraftConstants.*;

/**
 * Data about block types, intended to be read in from a YAML file (this will
 * get kicked off via the BlockTypeCollection class, actually).
 *
 * This class suffers from extreme Dissociative Identity Disorder, probably
 * due to deficiencies in the way I'm loading the data via snakeyaml.  The
 * basic problem is that I couldn't figure out a way to get the data to import
 * in a way that's most useful to X-Ray.  So, for a bunch of datatypes, we
 * end up using a call to "normalize()" to convert them to a different set of
 * datatypes, which X-Ray will then use.
 *
 * One issue is that a few of our data structures use Bytes, and snakeyaml doesn't
 * seem to like those very much.  They'd seemingly work okay when I plug them
 * into a HashMap, but when trying to use them with methods that explicitly require
 * a byte, I'd get this:
 *
 *    java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.Byte
 *
 * ... and I never did figure out a good way around that.  Instead I just have the
 * YAML attributes read into an Integer, and do the conversion manually.  It's
 * possible that we shouldn't bother using Bytes, actually, since from what I've
 * been reading, Java might only reference data in units of words, anyway, but for
 * now we're going to continue using bytes.
 *
 * The other problem is that I couldn't get snakeyaml to use our Enums properly when
 * they were contained inside a HashMap.  You may notice that "type" is directly
 * loaded from Yaml into the Enum, properly, but I couldn't get it to do the same
 * thing when the Enum was one element of a HashMap.  I'd end up getting the same
 * sort of errors as with the integer/byte thing, above, where it was saying it
 * refused to cast a java.lang.String to whatever the Enum was.
 *
 * I assume there are solutions to both of those problems, which would let me just
 * import the Yaml directly into the structures I actually care about, but for
 * now we'll just go with this slightly crazy and un-OOesque way of doing things.
 *
 * The getUsedTextures() call will only provide meaningful information after
 * normalization.
 */
public class BlockType
{

	public enum DIRECTION_REL {
		FORWARD,
		BACKWARD,
		SIDES,
		TOP,
		BOTTOM
	}

	public enum DIRECTION_ABS {
		NORTH,
		SOUTH,
		EAST,
		WEST
	}

	// YAML Attributes
	public short id;
	public String idStr;
	public String name;
	public BLOCK_TYPE type;
	private ArrayList<Integer> mapcolor;
	private ArrayList<Integer> tex;
	private HashMap<Integer, ArrayList<Integer>> tex_data;
	private HashMap<String, ArrayList<Integer>> tex_direction;
	private HashMap<Integer, String> tex_direction_data;
	private boolean override;

	// Computed Attributes
	public Color color;
	public int tex_idx;
	public HashMap<Byte, Integer> texture_data_map;
	public HashMap<DIRECTION_REL, Integer> texture_dir_map;
	public HashMap<Byte, DIRECTION_ABS> texture_dir_data_map;

	public BlockType()
	{
		this.override = false;
		this.id = -1;
	}

	public void setId(short id)
	{
		this.id = id;
	}

	public short getId()
	{
		return this.id;
	}

	public void setIdStr(String idStr)
	{
		this.idStr = idStr;
	}

	public String getIdStr()
	{
		return this.idStr;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public void setMapcolor(ArrayList<Integer> mapcolor)
	{
		this.mapcolor = mapcolor;
	}

	public ArrayList<Integer> getMapcolor()
	{
		return this.mapcolor;
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

	public void setTexIdxCoords(int x, int y)
	{
		ArrayList<Integer> coords = new ArrayList<Integer>();
		coords.add(x);
		coords.add(y);
		this.tex_idx = getTexReal(coords);
	}

	public static int getTexReal(ArrayList<Integer> coords)
	{
		return coords.get(0) + (16 * coords.get(1));
	}

	public int getTexReal()
	{
		return getTexReal(this.getTex());
	}

	public static ArrayList<Integer> getTexCoords(int tex)
	{
		ArrayList<Integer> tex_coords = new ArrayList<Integer>();
		tex_coords.add(tex%16);
		tex_coords.add(tex/16);
		return tex_coords;
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

	public void setTex_direction_data(HashMap<Integer, String> tex_direction_data)
	{
		this.tex_direction_data = tex_direction_data;
	}

	public HashMap<Integer, String> getTex_direction_data()
	{
		return this.tex_direction_data;
	}

	public void setType(BLOCK_TYPE type)
	{
		this.type = type;
	}

	public BLOCK_TYPE getType()
	{
		return this.type;
	}

	public void setOverride(boolean override)
	{
		this.override = override;
	}

	public boolean getOverride()
	{
		return this.override;
	}

	public boolean isSolid()
	{
		return (this.type == BLOCK_TYPE.NORMAL);
	}

	/**
	 * Normalizes our data from what we get from YAML, to a format that's
	 * easier to deal with in X-Ray.
	 */
	public void normalizeData()
		throws BlockTypeLoadException
	{
		// First check for some required attributes
		if (this.id < -1)
		{
			throw new BlockTypeLoadException("id is a required attribute");
		}
		if (this.idStr == null)
		{
			throw new BlockTypeLoadException("idStr is a required attribute");
		}
		if (this.name == null)
		{
			throw new BlockTypeLoadException("name is a required attribute");
		}
		if (this.mapcolor == null)
		{
			throw new BlockTypeLoadException("mapcolor is a required attribute");
		}
		if (this.mapcolor.size() != 3)
		{
			throw new BlockTypeLoadException("mapcolor requires three elements (RGB)");
		}
		if (this.tex == null)
		{
			throw new BlockTypeLoadException("tex is a required attribute");
		}
		if (this.tex.size() != 2)
		{
			throw new BlockTypeLoadException("tex coordinates require two elements (X, Y)");
		}

		// Now do the actual normalizing
		this.color = new Color(this.mapcolor.get(0), this.mapcolor.get(1), this.mapcolor.get(2));
		this.tex_idx = this.getTexReal();
		if (this.getType() == null)
		{
			this.setType(BLOCK_TYPE.NORMAL);
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
		if (this.tex_direction_data != null)
		{
			this.texture_dir_data_map = new HashMap<Byte, DIRECTION_ABS>();
			for (Map.Entry<Integer, String> entry : this.tex_direction_data.entrySet())
			{
				DIRECTION_ABS dir;
				try
				{
					dir = DIRECTION_ABS.valueOf(entry.getValue());
				}
				catch (IllegalArgumentException e)
				{
					throw new BlockTypeLoadException("Invalid absolute direction: " + entry.getValue());
				}
				this.texture_dir_data_map.put(entry.getKey().byteValue(), dir);
			}
		}
	}

	/**
	 * Returns a list of all textures that this block type uses
	 */
	public ArrayList<Integer> getUsedTextures()
	{
		HashMap<Integer, Boolean> tempMap = new HashMap<Integer, Boolean>();
		tempMap.put(this.tex_idx, true);
		if (this.texture_data_map != null)
		{
			for (Integer tex : this.texture_data_map.values())
			{
				if (!tempMap.containsKey(tex))
				{
					tempMap.put(tex, true);
				}
			}
		}
		if (this.texture_dir_map != null)
		{
			for (Integer tex : this.texture_dir_map.values())
			{
				if (!tempMap.containsKey(tex))
				{
					tempMap.put(tex, true);
				}
			}
		}

		// Be sure to include extra implicitly-used blocks
		if (blockTypeExtraTextures.containsKey(this.type))
		{
			for (int tex_offset : blockTypeExtraTextures.get(this.type))
			{
				if (!tempMap.containsKey(this.tex_idx + tex_offset))
				{
					tempMap.put(this.tex_idx + tex_offset, true);
				}
			}
		}

		// Now convert to a list
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (Integer tex : tempMap.keySet())
		{
			list.add(tex);
		}
		return list;
	}

}
