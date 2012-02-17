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
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.apocalyptech.minecraft.xray.dtf.CompoundTag;
import com.apocalyptech.minecraft.xray.dtf.DTFReader;
import com.apocalyptech.minecraft.xray.dtf.DoubleTag;
import com.apocalyptech.minecraft.xray.dtf.FloatTag;
import com.apocalyptech.minecraft.xray.dtf.IntTag;
import com.apocalyptech.minecraft.xray.dtf.ListTag;
import com.apocalyptech.minecraft.xray.dtf.StringTag;
import com.apocalyptech.minecraft.xray.dtf.LongTag;
import com.apocalyptech.minecraft.xray.dtf.Tag;

/***
 * A Minecraft level 
 * @author Vincent
 */
public class MinecraftLevel {

	//public static int LEVELDATA_SIZE = 256;
	public static int LEVELDATA_SIZE = 128;
	public static int LEVELDATA_OFFSET = Integer.MAX_VALUE/2;
	public Chunk[][] levelData;
	
	public WorldInfo world;
	
	// This var holds the index of the player position we've most recently picked
	private ArrayList<CameraPreset> playerPositions;
	private int playerPos_idx;
	private int spawnPoint_idx;
	private long randomSeed;
	public short[] HIGHLIGHT_ORES;
	
	public ArrayList<Texture> minecraftTextures;
	public Texture paintingTexture;

	public String levelName;
	
	/**
	 * Given a WorldInfo object, return its user-defined name.  There's
	 * some duplication here from the constructor object, but I think that's
	 * okay for this case.
	 */
	public static String getLevelName(WorldInfo world)
	{
		File levelFile = world.getLevelDatFile();
		CompoundTag levelData = (CompoundTag) DTFReader.readDTFFile(levelFile);
		CompoundTag levelDataData = (CompoundTag) levelData.getTagWithName("Data");
		StringTag levelNameTag = (StringTag) levelDataData.getTagWithName("LevelName");
		if (levelNameTag != null)
		{
			return levelNameTag.value;
		}
		else
		{
			return null;
		}
	}

	/***
	 * Create a minecraftLevel from the given world
	 * @param world
	 */
	public MinecraftLevel(WorldInfo world, ArrayList<Texture> minecraftTextures, Texture paintingTexture, short[] HIGHLIGHT_ORES) {
		this.world = world;
		this.minecraftTextures = minecraftTextures;
		this.paintingTexture = paintingTexture;
		this.HIGHLIGHT_ORES = HIGHLIGHT_ORES;
		
		this.levelData = new Chunk[LEVELDATA_SIZE][LEVELDATA_SIZE];
		
		File levelFile = world.getLevelDatFile();
		
		CompoundTag levelData = (CompoundTag) DTFReader.readDTFFile(levelFile);
		
		//	XRay.logger.trace(levelData.toString());
		
		this.playerPositions = new ArrayList<CameraPreset>();
		this.playerPos_idx = -1;
		this.spawnPoint_idx = -1;
		
		CompoundTag levelDataData = (CompoundTag) levelData.getTagWithName("Data");
		CompoundTag levelPlayerData = (CompoundTag) levelDataData.getTagWithName("Player");
		if(levelPlayerData != null) {
			
			// Figure out what dimension the player's in.  If it matches, move our camera there.
			IntTag playerDim = (IntTag) levelPlayerData.getTagWithName("Dimension");
			if ((playerDim == null && world.isOverworld()) || (playerDim != null && ((playerDim.value == 0 && world.isOverworld()) || (playerDim.value == world.getDimension()))))
			{
				ListTag playerPos = (ListTag) levelPlayerData .getTagWithName("Pos");
				ListTag playerRotation = (ListTag) levelPlayerData .getTagWithName("Rotation");
		
				DoubleTag posX = (DoubleTag) playerPos.value.get(0);
				DoubleTag posY = (DoubleTag) playerPos.value.get(1);
				DoubleTag posZ = (DoubleTag) playerPos.value.get(2);
				
				FloatTag rotYaw = (FloatTag) playerRotation.value.get(0);
				FloatTag rotPitch = (FloatTag) playerRotation.value.get(1);

				this.playerPositions.add(new CameraPreset(0, "Singleplayer User", new Block((int) -posX.value, (int) -posY.value, (int) -posZ.value+1),
						rotYaw.value, rotPitch.value));
				this.playerPos_idx = 0;
			}
		}
		
		// Get a list of MP users that can provide valid camera positions for us
		CompoundTag mpuserData;
		for (String mpusername : world.mp_players.keySet())
		{
			try
			{
				mpuserData = (CompoundTag) DTFReader.readDTFFile(world.mp_players.get(mpusername));
				
				// Skip players who aren't currently in the dimension that we're using
				// (which would be weird, since SMP doesn't support Nether properly yet)
				IntTag playerDim = (IntTag) mpuserData.getTagWithName("Dimension");
				if (playerDim == null)
				{
					if (!world.isOverworld())
					{
						continue;
					}
				}
				else
				{
					if ((playerDim.value == 0 && !world.isOverworld()) || (playerDim.value != world.getDimension()))
					{
						continue;
					}
				}
				
				// Pull out the data
				ListTag playerPos = (ListTag) mpuserData.getTagWithName("Pos");
				ListTag playerRotation = (ListTag) mpuserData.getTagWithName("Rotation");	
				DoubleTag posX = (DoubleTag) playerPos.value.get(0);
				DoubleTag posY = (DoubleTag) playerPos.value.get(1);
				DoubleTag posZ = (DoubleTag) playerPos.value.get(2);
				FloatTag rotYaw = (FloatTag) playerRotation.value.get(0);
				FloatTag rotPitch = (FloatTag) playerRotation.value.get(1);
				this.playerPositions.add(new CameraPreset(this.playerPositions.size(),
						mpusername, new Block((int) -posX.value, (int) -posY.value-1, (int) -posZ.value+1),
						rotYaw.value, rotPitch.value));
			}
			catch (Exception e)
			{
				// Just report to console and continue.
				XRay.logger.warn("Error loading position information for user " + mpusername + ": " + e.toString());
			}
		}
		
		// Set the spawn point if we're not in the Nether
		this.spawnPoint_idx = this.playerPositions.size();
		if (world.isOverworld())
		{
			IntTag spawnX = (IntTag) levelDataData.getTagWithName("SpawnX");
			IntTag spawnY = (IntTag) levelDataData.getTagWithName("SpawnY");
			IntTag spawnZ = (IntTag) levelDataData.getTagWithName("SpawnZ");
			this.playerPositions.add(new CameraPreset(this.spawnPoint_idx, "Spawnpoint", new Block(-spawnX.value, -spawnY.value-1, -spawnZ.value+1), 0, 0));
		}
		else
		{
			this.playerPositions.add(new CameraPreset(this.spawnPoint_idx, "Map Center", new Block(0,-66,0), 0, 0));
		}

		// Figure out where to set the "player" position, if we have no singleplayer user
		if (this.playerPos_idx == -1)
		{
			this.playerPos_idx = this.spawnPoint_idx;
		}
		
		// Figure out what sort of data we should be looking for
		IntTag versionTag = (IntTag) levelDataData.getTagWithName("version");
		if (versionTag != null)
		{
			switch (versionTag.value)
			{
				case 19133:
					world.data_format = WorldInfo.MAP_TYPE.ANVIL;
					break;
					
				case 19132:
				default:
					world.data_format = WorldInfo.MAP_TYPE.MCREGION;
					break;
			}
		}
		else
		{
			world.data_format = WorldInfo.MAP_TYPE.ORIGINAL;
		}

		// Grab the level name
		StringTag levelNameTag = (StringTag) levelDataData.getTagWithName("LevelName");
		if (levelNameTag != null)
		{
			this.levelName = levelNameTag.value;
		}
		else
		{
			this.levelName = null;
		}

		// Pull our random seed
		this.randomSeed = ((LongTag) levelDataData.getTagWithName("RandomSeed")).value;
	}

	/**
	 * Sets the random seed.
	 */
	public void setRandomSeed(long randomSeed)
	{
		this.randomSeed = randomSeed;
	}

	/**
	 * Returns the random seed.
	 */
	public long getRandomSeed()
	{
		return this.randomSeed;
	}
	
	/***
	 * returns the spawning point for this level
	 */
	public CameraPreset getPlayerPositionIdx(int idx)
	{
		return this.playerPositions.get(idx);
	}
	
	public CameraPreset getSpawnPoint() {
		return this.getPlayerPositionIdx(this.spawnPoint_idx);
	}
	
	public CameraPreset getPlayerPosition() {
		return this.getPlayerPositionIdx(this.playerPos_idx);
	}
	
	public CameraPreset getNextPlayerPosition(CameraPreset current) {
		int next_idx = (current.idx+1) % this.playerPositions.size();
		return this.getPlayerPositionIdx(next_idx);
	}

	public CameraPreset getPrevPlayerPosition(CameraPreset current) {
		int prev_idx = current.idx - 1;
		if (prev_idx < 0)
		{
			prev_idx = this.playerPositions.size()-1;
		}
		return this.getPlayerPositionIdx(prev_idx);
	}
	
	/***
	 * correctly calculate the chunk X value given a universal coordinate
	 * @param x
	 * @return
	 */
	public static int getChunkX(int x) {
		if(x<0) {
			return  -(((-x)-1) / 16)-1; // otherwise -1 and +1 would return the same chunk
		} else {
			return x / 16;
		}
	}
	
	/***
	 * correctly calculate the block X value given a universal coordinate
	 * @param x
	 * @return
	 */
	public static int getBlockX(int x) {
		if(x<0) {
			return 15-(((-x)-1) % 16); // compensate for different chunk calculation
		} else {
			return x % 16;
		}
	}
	
	/***
	 * correctly calculate the chunk Z value given a universal coordinate
	 * @param z
	 * @return
	 */
	public static int getChunkZ(int z) {
		if(z<0) {
			return  -(((-z)-1) / 16)-1; // otherwise -1 and +1 would return the same chunk
		} else {
			return z / 16;
		}
	}
	
	/***
	 * correctly calculate the block Z value given a universal coordinate
	 * @param z
	 * @return
	 */
	public static int getBlockZ(int z) {
		if(z<0) {
			return 15-(((-z)-1) % 16); // compensate for different chunk calculation
		} else {
			return z % 16;
		}
	}
	
	/***
	 * Returns a single byte representing the block data at the given universal coordinates
	 * @param x
	 * @param z
	 * @param y
	 * @return
	 */
	public short getBlockData(int x, int z, int y) {
		int chunkX = getChunkX(x);
		int chunkZ = getChunkZ(z);
		
		int blockX = getBlockX(x);
		int blockZ = getBlockZ(z);
		
		Chunk chunk = this.getChunk(chunkX, chunkZ);
		if(chunk == null) { // no chunk for the given coordinate
			return 0;
		}
		int blockOffset = y + (blockZ * 128) + (blockX * 128 * 16);
		
		try {
			return chunk.getMapData().value[blockOffset];
		} catch(Exception e) {
			// dirty, but there was an error with out of range blockvalues O_o
			XRay.logger.fatal(blockOffset);
			XRay.logger.fatal("" + x + ", " + y + ", " + z);
			XRay.logger.fatal("" + blockX + ", " + blockZ );
			System.exit(0);
			return 0;
		}
	}
	

	public void invalidateSelected() {
		this.invalidateSelected(false);
	}

	public void invalidateSelected(boolean main_dirty) {
		for (Chunk[] chunkrow : this.levelData)
		{
			for (Chunk chunk : chunkrow)
			{
				if (chunk != null)
				{
					chunk.setSelectedDirty();
					if (main_dirty)
					{
						chunk.setDirty();
					}
				}
			}
		}
	}
	
	public void markChunkAsDirty(int x, int z) {
		Chunk c = this.getChunk(x, z);
		if (c != null)
		{
			c.setDirty();
		}
	}
	
	/**
	 * Loads a chunk given the chunk's x and z coordinates (not world coordinates)
	 *
	 * @param chunkX The Chunk X coordinate
	 * @param chunkZ The Chunk Z coordinate
	 */
	public Tag loadChunk(int chunkX, int chunkZ) {
		DataInputStream chunkInputStream = MinecraftEnvironment.getChunkInputStream(world, chunkX, chunkZ);
		if(chunkInputStream == null) {
			return null;
		}
		try
		{
			Tag t = DTFReader.readTagData(chunkInputStream);
			if (t != null)
			{
				levelData[(chunkX+LEVELDATA_OFFSET)%LEVELDATA_SIZE][(chunkZ+LEVELDATA_OFFSET)%LEVELDATA_SIZE] = new Chunk(this, t);
			}	
			return t;
		}
		catch (IOException e)
		{
			return null;
		}
	}
	
	/**
	 * Gets the specified Chunk object
	 *
	 * @param chunkX
	 * @param chunkZ
	 * @return
	 */
	public Chunk getChunk(int chunkX, int chunkZ) {
		return this.levelData[(chunkX+LEVELDATA_OFFSET)%LEVELDATA_SIZE][(chunkZ+LEVELDATA_OFFSET)%LEVELDATA_SIZE];
	}

	/**
	 * Sets a chunk to null
	 * 
	 * @param chunkX
	 * @param chunkZ
	 */
	public void clearChunk(int chunkX, int chunkZ)
	{
		this.levelData[(chunkX+LEVELDATA_OFFSET)%LEVELDATA_SIZE][(chunkZ+LEVELDATA_OFFSET)%LEVELDATA_SIZE] = null;
	}
	
	/**
	 * Sets all chunks in the given X row to be no longer on the minimap
	 * 
	 * @param chunkX
	 */
	public ArrayList<Chunk> removeChunkRowXFromMinimap(int chunkX)
	{
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		int xval = (chunkX+LEVELDATA_OFFSET)%LEVELDATA_SIZE;
		for (int i=0; i<LEVELDATA_SIZE; i++)
		{
			if (this.levelData[xval][i] != null && this.levelData[xval][i].isOnMinimap)
			{
				//XRay.logger.trace("(" + chunkX + ") Removing from minimap on " + xval + ", " + i);
				this.levelData[xval][i].isOnMinimap = false;
				chunks.add(this.levelData[xval][i]);
			}
		}
		return chunks;
	}
	
	/**
	 * Sets all chunks in the given Z row to be no longer on the minimap
	 * 
	 * @param chunkZ
	 */
	public ArrayList<Chunk> removeChunkRowZFromMinimap(int chunkZ)
	{
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		int zval = (chunkZ+LEVELDATA_OFFSET)%LEVELDATA_SIZE;
		for (int i=0; i<LEVELDATA_SIZE; i++)
		{
			if (this.levelData[i][zval] != null && this.levelData[i][zval].isOnMinimap)
			{
				//XRay.logger.trace("Removing from minimap on " + i + ", " + zval);
				this.levelData[i][zval].isOnMinimap = false;
				chunks.add(this.levelData[i][zval]);
			}
		}
		return chunks;
	}
	
	/**
	 * Sets all chunks to be no longer on the minimap
	 * @return
	 */
	public ArrayList<Chunk> removeAllChunksFromMinimap()
	{
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();	
		for (int z=0; z<LEVELDATA_SIZE; z++)
		{
			for (int x=0; x<LEVELDATA_SIZE; x++)
			{
				if (this.levelData[x][z] != null && this.levelData[x][z].isOnMinimap)
				{
					//XRay.logger.trace("(" + chunkX + ") Removing from minimap on " + xval + ", " + i);
					this.levelData[x][z].isOnMinimap = false;
					chunks.add(this.levelData[x][z]);
				}
			}
		}
		return chunks;
	}
	
	/***
	 * gets the data for a given chunk (coordinates are CHUNK coordinates, not world coordinates!)
	 * @param chunkX
	 * @param chunkZ
	 * @return
	 */
	public short[] getChunkData(int chunkX, int chunkZ) {
		Chunk c = this.getChunk(chunkX, chunkZ);
		if(c == null) {
			return new short[32768];
		} else {
			return c.getMapData().value;
		}
	}
		
	
	public Tag getFullChunk(int chunkX, int chunkZ) {
		Chunk c = this.getChunk(chunkX, chunkZ);
		if(c == null) {
			return null;
		}
		return c.getChunkData();
	}
}
