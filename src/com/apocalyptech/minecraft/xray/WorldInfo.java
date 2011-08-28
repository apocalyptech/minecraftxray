/**
 * Copyright (c) 2010-2011, Vincent Vollers and Christopher J. Kucera
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

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;
import java.lang.Comparable;
import java.lang.NumberFormatException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class to aid in maintaining a list of possible worlds for us to use.
 */
public class WorldInfo implements Comparable<WorldInfo>
{
	private String basepath;
	private String dirName;
	private String levelName;
	private boolean custom;
	private int dimension;
	public TreeMap<String, File> mp_players;

	public static HashMap<Integer, String> known_dimensions;

	// Some static initializations
	static
	{
		known_dimensions = new HashMap<Integer, String>();
		known_dimensions.put(-1, "Nether");
	}
	
	// Couple of variables to determine whether our chunks are stored in the new
	// Region format introduced in Beta 1.3.  Minecraft converts chunks on the fly,
	// so for a time a world directory will have chunks in both formats.  Once all
	// the chunks have been converted, Minecraft will write out a few new tags in
	// the main level data file, so we can stop looking for the old format.
	public boolean has_region_data = false;
	public boolean is_beta_1_3_level = false;
	
	private class PlayerDatFilter implements FilenameFilter
	{
		public PlayerDatFilter() {
			// Nothing, really
		}
		
		public boolean accept(File directory, String filename) {
			return (filename.endsWith(".dat"));
		}
	}
	
	/**
	 * Instansiate a new object.  "custom" refers to when the user has specified
	 * a custom path, instead of one of the standard Minecraft singleplayer world
	 * locations.
	 * 
	 * @param basepath
	 * @param dimension
	 * @param custom
	 */
	public WorldInfo(String basepath, String dirName, int dimension, boolean custom)
	{
		this.basepath = basepath;
		this.dimension = dimension;
		this.custom = custom;
		this.dirName = dirName;
		this.populateMPPlayerList();

		// Load in the minecraft level, to read its name
		if (basepath != null)
		{
			this.levelName = MinecraftLevel.getLevelName(this);
		}
		else
		{
			this.levelName = null;
		}
	}
	
	/**
	 * Instansiate a new WorldInfo with only the path and what dimension we are
	 * 
	 * @param basepath
	 * @param dimension
	 */
	public WorldInfo(String basepath, String dirName, int dimension)
	{
		this(basepath, dirName, dimension, false);
	}
	
	/**
	 * Instansiate a new WorldInfo given only the path (assumed to be overworld)
	 * 
	 * @param basepath
	 */
	public WorldInfo(String basepath, String dirName)
	{
		this(basepath, dirName, 0);
	}
	
	/**
	 * Instansiate a custom WorldInfo - path will be added later when the user
	 * selects it.
	 * 
	 * @param custom
	 */
	public WorldInfo(boolean custom)
	{
		this(null, null, 0, true);
	}
	
	public void populateMPPlayerList()
	{
		this.mp_players = new TreeMap<String, File>();
		if (this.basepath != null)
		{
			File playerdir = this.getPlayerListDir();
			if (playerdir != null && playerdir.exists() && playerdir.isDirectory())
			{
				String basename;
				File[] players = playerdir.listFiles(new PlayerDatFilter());
				for (File player : players)
				{
					basename = player.getName();
					this.mp_players.put(basename.substring(0, basename.lastIndexOf('.')), player);
				}
			}
		}
	}

	/**
	 * Sets the base path.  Has no effect on non-custom worlds.
	 * 
	 * @param newpath
	 */
	public void setBasePath(String newpath)
	{
		if (this.custom)
		{
			this.basepath = newpath;
			// TODO: this isn't correct anymore
			if (this.hasOverworld() && !this.hasDimension(-1))
			{
				this.dimension = -1;
			}
			else
			{
				this.dimension = 0;
			}
		}
		this.populateMPPlayerList();
	}
	
	/**
	 * Gets the base path
	 * 
	 * @return
	 */
	public String getBasePath()
	{
		return this.basepath;
	}

	/**
	 * Gets the base path as a File object
	 * 
	 */
	public File getBaseFile()
	{
		return new File(this.getBasePath());
	}
	
	public File getLevelDatFile()
	{
		if (this.isOverworld())
		{
			return new File(this.getBasePath(), "level.dat");
		}
		else
		{
			return new File(this.getBasePath(), "../level.dat");
		}
	}
	
	public File getPlayerListDir()
	{
		if (this.isOverworld())
		{
			return new File(this.getBasePath(), "players");
		}
		else
		{
			return new File(this.getBasePath(), "../players");
		}
	}
	
	/**
	 * Returns our directory name (this value is meaningless for custom worlds)
	 * 
	 * @return
	 */
	public String getDirName()
	{
		return this.dirName;
	}
	
	/**
	 * Returns our level name
	 * 
	 * @return
	 */
	public String getLevelName()
	{
		return this.levelName;
	}
	
	/**
	 * A custom world is one that lives outside the usual Minecraft directory
	 * structure.
	 * 
	 * @return
	 */
	public boolean isCustom()
	{
		return this.custom;
	}
	
	/**
	 * Are we a dimension world?  Note that custom worlds will always return false
	 * until their path is set with setBasePath()
	 * 
	 * @return
	 */
	public boolean isDimension(int dimension)
	{
		return (this.dimension == dimension);
	}

	/**
	 * Return which dimension we are
	 */
	public int getDimension()
	{
		return this.dimension;
	}

	/**
	 * Returns whether this is a dimension that we "know" about (mostly just for
	 * the text label)
	 */
	public boolean isKnownDimension()
	{
		return (known_dimensions.containsKey(this.dimension));
	}

	/**
	 * Returns a text description of this dimension
	 */
	public String getDimensionDesc()
	{
		if (this.isKnownDimension())
		{
			return (String)known_dimensions.get(this.dimension);
		}
		else if (this.isDimension(0))
		{
			return "Overworld";
		}
		else
		{
			return "Dimension " + Integer.toString(this.dimension);
		}
	}

	/**
	 * Are we an overworld?  Note that custom worlds will always return true
	 * until their path is set with setBasePath()
	 */
	public boolean isOverworld()
	{
		return (this.dimension == 0);
	}
	
	/**
	 * Do we have a dimension subdirectory to read?
	 * 
	 * @return
	 */
	public boolean hasDimension(int dimension)
	{
		if (!this.custom && this.dimension == dimension)
		{
			return false;
		}
		else
		{
			File test = new File(this.getBasePath(), "DIM" + Integer.toString(dimension));
			return (test.exists() && test.canRead() && test.isDirectory());
		}
	}
	
	/**
	 * Do we have an overworld?
	 * 
	 * @return
	 */
	public boolean hasOverworld()
	{
		if (!this.custom && this.dimension != 0)
		{
			File test = new File(this.getBasePath(), "../level.dat");
			return (test.exists() && test.canRead());
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Returns an array of new WorldInfo objects pointing to any associated
	 * extra dimensions, if we have one.
	 * 
	 * @return A new WorldInfo array
	 */
	public ArrayList<WorldInfo> getDimensionInfo()
	{
		ArrayList<WorldInfo> ret_array = new ArrayList<WorldInfo>();

		File dir = this.getBaseFile();
		if (dir != null && dir.exists() && dir.isDirectory())
		{
			File[] dimensions = dir.listFiles(new DimensionFilter());
			for (File dim_dir : dimensions)
			{
				try
				{
					int dimension = DimensionFilter.get_dimension(dim_dir.getName());
					ret_array.add(new WorldInfo(dim_dir.getCanonicalPath(), this.dirName, dimension, this.custom));
				}
				catch (DimensionFilterException e)
				{
					// whatever, just skip
				}
				catch (IOException e)
				{
					System.out.println("Exception attempting to read world at " + this.dirName + ": " + e.toString());
					// whatever, just skip
				}
			}
		}

		Collections.sort(ret_array);
		return ret_array;
	}
	
	/**
	 * Returns a new WorldInfo object pointing to the overworld, if we currently
	 * live in a subdirectory of another world.
	 * 
	 * @return A new WorldInfo, or null
	 */
	public WorldInfo getOverworldInfo()
	{
		if (this.hasOverworld())
		{
			File info = new File(this.getBasePath(), "..");
			try
			{
				return new WorldInfo(info.getCanonicalPath(), this.dirName, 0, this.custom);
			}
			catch (IOException e)
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * Method for Comparable interface, so we can sort based on dimension.
	 */
	public int compareTo(WorldInfo anotherInstance)
	{
		return this.getDimension() - anotherInstance.getDimension();
	}

	/**
	 * Gets an ordered list of all dimensions for this world, including the one
	 * we're on, and the overworld, etc.
	 */
	public ArrayList<WorldInfo> getAllDimensions()
	{
		ArrayList<WorldInfo> dims;
		if (this.isOverworld())
		{
			dims = this.getDimensionInfo();
			dims.add(this);
		}
		else
		{
			dims = this.getOverworldInfo().getDimensionInfo();
			dims.add(this.getOverworldInfo());
		}
		Collections.sort(dims);
		return dims;
	}
}
