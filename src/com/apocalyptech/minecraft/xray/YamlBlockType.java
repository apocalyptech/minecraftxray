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

import java.util.HashMap;
import java.util.ArrayList;
import java.awt.Color;

import static com.apocalyptech.minecraft.xray.MineCraftConstants.*;

/**
 * Class to load in data from our YAML file.  Really we should probably be just
 * reading this stuff into an object and keeping it there (and thus should ditch
 * the "Yaml" part of this name) but for now we're keeping it the way it is.
 *
 * Note that the key part of the HashMaps for tex_data and tex_direction_data
 * SHOULD be Byte, not Integer.  snakeyaml appears to have big problems with
 * that, though, and attempts to work with the data often yield one of these:
 *
 *    java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.Byte
 * 
 * So, instead, we're using Integer and casting as we use it.  Alas.
 */
public class YamlBlockType
{
	private short id;
	private String idStr;
	private String name;
	private ArrayList<Integer> mapcolor;
	private ArrayList<Integer> tex;
	private HashMap<Integer, ArrayList<Integer>> tex_data;
	private HashMap<String, ArrayList<Integer>> tex_direction;
	private HashMap<Integer, String> tex_direction_data;
	private BLOCK_TYPE type;

	public YamlBlockType()
	{
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

	public Color getColor()
	{
		return new Color(this.mapcolor.get(0), this.mapcolor.get(1), this.mapcolor.get(2));
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
		this.setTex(YamlBlockType.getTexCoords(tex));
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

}
