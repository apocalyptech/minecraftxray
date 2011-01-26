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
package com.plusminus.craft;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;
import java.util.TreeMap;

/**
 * Class to aid in maintaining a list of possible worlds for us to use.
 */
public class WorldInfo
{
	private String basepath;
	private int worldnum;
	private boolean nether;
	private boolean custom;
	public TreeMap<String, File> mp_players;
	
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
	 * @param isNether
	 * @param custom
	 */
	public WorldInfo(String basepath, int worldnum, boolean isNether, boolean custom)
	{
		this.basepath = basepath;
		this.nether = isNether;
		this.custom = custom;
		this.worldnum = worldnum;
		this.populateMPPlayerList();
	}
	
	/**
	 * Instansiate a new WorldInfo with only the path and whether it's Nether or not.
	 * 
	 * @param basepath
	 * @param isNether
	 */
	public WorldInfo(String basepath, int worldnum, boolean isNether)
	{
		this(basepath, worldnum, isNether, false);
	}
	
	/**
	 * Instansiate a new WorldInfo given only the path (assumed to be non-Nether)
	 * 
	 * @param basepath
	 */
	public WorldInfo(String basepath, int worldnum)
	{
		this(basepath, worldnum, false, false);
	}
	
	/**
	 * Instansiate a custom WorldInfo - path will be added later when the user
	 * selects it.
	 * 
	 * @param custom
	 */
	public WorldInfo(boolean custom)
	{
		this(null, -1, false, true);
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
			if (this.hasOverworld() && !this.hasNether())
			{
				this.nether = true;
			}
			else
			{
				this.nether = false;
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
	
	public File getLevelDatFile()
	{
		if (this.isNether())
		{
			return new File(this.getBasePath(), "../level.dat");
		}
		else
		{
			return new File(this.getBasePath(), "level.dat");
		}
	}
	
	public File getPlayerListDir()
	{
		if (this.isNether())
		{
			return new File(this.getBasePath(), "../players");
		}
		else
		{
			return new File(this.getBasePath(), "players");
		}
	}
	
	/**
	 * Returns our world number (this value is meaningless for custom worlds)
	 * 
	 * @return
	 */
	public int getWorldnum()
	{
		return this.worldnum;
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
	 * Are we a Nether world?  Note that custom worlds will always return false
	 * until their path is set with setBasePath()
	 * 
	 * @return
	 */
	public boolean isNether()
	{
		return this.nether;
	}
	
	/**
	 * Do we have a Nether subdirectory to read?
	 * 
	 * @return
	 */
	public boolean hasNether()
	{
		if (!this.custom && this.nether)
		{
			return false;
		}
		else
		{
			File test = new File(this.getBasePath(), "DIM-1");
			return (test.exists() && test.canRead());
		}
	}
	
	/**
	 * Do we have an overworld?
	 * 
	 * @return
	 */
	public boolean hasOverworld()
	{
		if (!this.custom && this.nether)
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
	 * Returns a new WorldInfo object pointing to our associated Nether
	 * world, if we have one.
	 * 
	 * @return A new WorldInfo, or null
	 */
	public WorldInfo getNetherInfo()
	{
		if (this.hasNether())
		{
			File info = new File(this.getBasePath(), "DIM-1");
			try
			{
				return new WorldInfo(info.getCanonicalPath(), this.worldnum, true, this.custom);
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
				return new WorldInfo(info.getCanonicalPath(), this.worldnum, false, this.custom);
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
}