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
import java.io.InputStream;
import java.io.IOException;

import static com.apocalyptech.minecraft.xray.MinecraftConstants.*;

/**
 */
public class BlockTypeFilename extends BlockType
{

	// YAML Attributes
	private String tex;
	private HashMap<Integer, String> tex_data;
	private HashMap<String, String> tex_direction;
	private HashMap<DIRECTION_REL, String> tex_direction_int;

	// Attributes brought over from BlockTypeCollection
	public String texpath;

	public BlockTypeFilename()
	{
		super();
	}

	public void setTex(String tex)
	{
		this.tex = tex;
	}

	public String getTex()
	{
		return this.tex;
	}

	public void setTex_data(HashMap<Integer, String> tex_data)
	{
		this.tex_data = tex_data;
	}

	public HashMap<Integer, String> getTex_data()
	{
		return this.tex_data;
	}

	public void setTex_direction(HashMap<String, String> tex_direction)
	{
		this.tex_direction = tex_direction;
	}

	public HashMap<String, String> getTex_direction()
	{
		return this.tex_direction;
	}

	/**
	 * Pulls data from the master collection
	 */
	public void pullDataFromCollection(BlockTypeCollection collection)
	{
		this.texpath = collection.getTexpath();
	}

	/**
	 * Returns the full filename for the given texture file
	 */
	private String getFullTextureFilename(String filename)
	{
		return this.texpath + "/" + filename;
	}

	/**
	 * Gets a list of all texture filenames we're using
	 */
	public ArrayList<String> getTextureFilenames()
	{
		ArrayList<String> list = new ArrayList<String>();
		list.add(this.tex);
		if (this.tex_data != null)
		{
			for (String tex : this.tex_data.values())
			{
				list.add(tex);
			}
		}
		if (this.tex_direction_int != null)
		{
			for (String tex : this.tex_direction_int.values())
			{
				list.add(tex);
			}
		}
		return list;
	}

	/**
	 * Sets our "real" texture values
	 *
	 * TODO: Exception error reporting
	 */
	public void setTextureFilenameMapping(HashMap<String, Integer> texmap)
		throws BlockTypeLoadException
	{
		// First the "main" texture
		if (texmap.containsKey(this.tex))
		{
			this.tex_idx = texmap.get(this.tex);
		}
		else
		{
			throw new BlockTypeLoadException("No texture mapping found for " + this.tex);
		}

		// Now data the data mapping
		if (this.tex_data != null && this.tex_data.size() > 0)
		{
			this.texture_data_map = new HashMap<Byte, Integer>();
			for (Map.Entry<Integer, String> entry : this.tex_data.entrySet())
			{
				if (texmap.containsKey(entry.getValue()))
				{
					this.texture_data_map.put(entry.getKey().byteValue(), texmap.get(entry.getValue()));
				}
				else
				{
					throw new BlockTypeLoadException("No texture mapping found for " + entry.getValue());
				}
			}
		}

		// Now direction data mapping
		if (this.tex_direction_int != null && this.tex_direction_int.size() > 0)
		{
			this.texture_dir_map = new HashMap<DIRECTION_REL, Integer>();
			for (Map.Entry<DIRECTION_REL, String> entry : this.tex_direction_int.entrySet())
			{
				if (texmap.containsKey(entry.getValue()))
				{
					this.texture_dir_map.put(entry.getKey(), texmap.get(entry.getValue()));
				}
				else
				{
					throw new BlockTypeLoadException("No texture mapping found for " + entry.getValue());
				}
			}
		}
	}

	/**
	 * Attempts to open the file, to make sure that it's readable before we go
	 * any further.  This should be the full path, post-normalization.
	 */
	private static void checkTextureAvailability(String filename)
		throws BlockTypeLoadException
	{
		try
		{
			InputStream stream = MinecraftEnvironment.getMinecraftTexturepackData(filename);
			if (stream == null)
			{
				throw new BlockTypeLoadException("File " + filename + " is not found");
			}
			stream.close();
		}
		catch (IOException e)
		{
			throw new BlockTypeLoadException("Error while opening " + filename + ": " + e.toString(), e);
		}
	}

	/**
	 * Normalizes our data from what we get from YAML, to a format that's
	 * easier to deal with in X-Ray.
	 *
	 * TODO: Exception error reporting
	 */
	public void normalizeData()
		throws BlockTypeLoadException
	{
		// First check our super
		super.normalizeData();

		// Now check for some required attributes
		if (this.tex == null || this.tex.length() == 0)
		{
			throw new BlockTypeLoadException("tex is a required attribute");
		}

		// Now do the actual normalizing...  Modify our textures to use the full path
		this.tex = this.getFullTextureFilename(this.tex);
		checkTextureAvailability(this.tex);
		
		if (this.tex_data != null && this.tex_data.size() > 0)
		{
		    for (Map.Entry<Integer, String> entry : this.tex_data.entrySet())
			{
				entry.setValue(this.getFullTextureFilename(entry.getValue()));
				checkTextureAvailability(entry.getValue());
			}
		}
		if (this.tex_direction != null && this.tex_direction.size() > 0)
		{
			this.tex_direction_int = new HashMap<DIRECTION_REL, String>();
			for (Map.Entry<String, String> entry: this.tex_direction.entrySet())
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
				this.tex_direction_int.put(dir, this.getFullTextureFilename(entry.getValue()));
				checkTextureAvailability(this.tex_direction_int.get(dir));
			}
		}
	}
}
