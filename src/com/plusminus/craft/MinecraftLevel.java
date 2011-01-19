package com.plusminus.craft;

import java.io.File;
import java.util.ArrayList;

import com.plusminus.craft.dtf.CompoundTag;
import com.plusminus.craft.dtf.DTFReader;
import com.plusminus.craft.dtf.DoubleTag;
import com.plusminus.craft.dtf.FloatTag;
import com.plusminus.craft.dtf.IntTag;
import com.plusminus.craft.dtf.ListTag;
import com.plusminus.craft.dtf.Tag;

/***
 * A Minecraft level 
 * @author Vincent
 */
public class MinecraftLevel {

	//public static int LEVELDATA_SIZE = 256;
	public static int LEVELDATA_SIZE = 128;
	public static int LEVELDATA_OFFSET = Integer.MAX_VALUE/2;
	public Chunk[][] levelData;
	
	private WorldInfo world;
	
	// This var holds the index of the player position we've most recently picked
	private ArrayList<CameraPreset> playerPositions;
	private int playerPos_idx;
	private int spawnPoint_idx;
	
	public Texture minecraftTexture;
	public Texture paintingTexture;
	public Texture portalTexture;

	/***
	 * Create a minecraftLevel from the given world
	 * @param world
	 */
	public MinecraftLevel(WorldInfo world, Texture minecraftTexture, Texture paintingTexture, Texture portalTexture) {
		this.world = world;
		this.minecraftTexture = minecraftTexture;
		this.paintingTexture = paintingTexture;
		this.portalTexture = portalTexture;
		
		this.levelData = new Chunk[LEVELDATA_SIZE][LEVELDATA_SIZE];
		
		File levelFile = world.getLevelDatFile();
		
		CompoundTag levelData = (CompoundTag) DTFReader.readDTFFile(levelFile);
		
		//	System.out.println(levelData.toString());
		
		this.playerPositions = new ArrayList<CameraPreset>();
		this.playerPos_idx = -1;
		this.spawnPoint_idx = -1;
		
		CompoundTag levelDataData = (CompoundTag) levelData.getTagWithName("Data");
		CompoundTag levelPlayerData = (CompoundTag) levelDataData.getTagWithName("Player");
		if(levelPlayerData != null) {
			
			// Figure out what dimension the player's in.  If it matches, move our camera there.
			// TODO: if playerDim is null, perhaps we should move the camera to the spawnpoint...
			IntTag playerDim = (IntTag) levelPlayerData.getTagWithName("Dimension");
			if (playerDim != null && ((playerDim.value == 0 && !world.isNether()) || (playerDim.value == -1 && world.isNether())))
			{
				ListTag playerPos = (ListTag) levelPlayerData .getTagWithName("Pos");
				ListTag playerRotation = (ListTag) levelPlayerData .getTagWithName("Rotation");
		
				DoubleTag posX = (DoubleTag) playerPos.value.get(0);
				DoubleTag posY = (DoubleTag) playerPos.value.get(1);
				DoubleTag posZ = (DoubleTag) playerPos.value.get(2);
				
				FloatTag rotYaw = (FloatTag) playerRotation.value.get(0);
				FloatTag rotPitch = (FloatTag) playerRotation.value.get(1);

				this.playerPositions.add(new CameraPreset(0, "Singleplayer User", new Block((int) -posX.value, (int) -posY.value, (int) -posZ.value),
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
					if (world.isNether())
					{
						continue;
					}
				}
				else
				{
					if ((playerDim.value == 0 && world.isNether()) || (playerDim.value == -1 && !world.isNether()))
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
						mpusername, new Block((int) -posX.value, (int) -posY.value-1, (int) -posZ.value),
						rotYaw.value, rotPitch.value));
			}
			catch (Exception e)
			{
				// Just report to console and continue.
				System.out.println("Error loading position information for user " + mpusername + ": " + e.toString());
			}
		}
		
		// Set the spawn point if we're not in the Nether
		this.spawnPoint_idx = this.playerPositions.size();
		if (world.isNether())
		{
			this.playerPositions.add(new CameraPreset(this.spawnPoint_idx, "Map Center", new Block(0,-66,0), 0, 0));
		}
		else
		{
			IntTag spawnX = (IntTag) levelDataData.getTagWithName("SpawnX");
			IntTag spawnY = (IntTag) levelDataData.getTagWithName("SpawnY");
			IntTag spawnZ = (IntTag) levelDataData.getTagWithName("SpawnZ");
			this.playerPositions.add(new CameraPreset(this.spawnPoint_idx, "Spawnpoint", new Block(-spawnX.value, -spawnY.value-1, -spawnZ.value), 0, 0));
		}

		// Figure out where to set the "player" position, if we have no singleplayer user
		if (this.playerPos_idx == -1)
		{
			this.playerPos_idx = this.spawnPoint_idx;
		}
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
	public int getChunkX(int x) {
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
	public int getBlockX(int x) {
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
	public int getChunkZ(int z) {
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
	public int getBlockZ(int z) {
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
	public byte getBlockData(int x, int z, int y) {
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
			System.out.println(blockOffset);
			System.out.println("" + x + ", " + y + ", " + z);
			System.out.println("" + blockX + ", " + blockZ );
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
					chunk.isSelectedDirty = true;
					if (main_dirty)
					{
						chunk.isDirty = true;
					}
				}
			}
		}
	}
	
	public void markChunkAsDirty(int x, int z) {
		Chunk c = this.getChunk(x, z);
		if (c != null)
		{
			c.isDirty = true;
		}
	}
	
	public Tag loadChunk(int x, int z) {
		File chunkFile = MineCraftEnvironment.getChunkFile(world, x,z);
		if(!chunkFile.exists()) {
			return null;
		}
		Tag t = DTFReader.readDTFFile(chunkFile);

		if (t != null)
		{
			levelData[(x+LEVELDATA_OFFSET)%LEVELDATA_SIZE][(z+LEVELDATA_OFFSET)%LEVELDATA_SIZE] = new Chunk(this, t);
		}
		
		return t;
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
				//System.out.println("(" + chunkX + ") Removing from minimap on " + xval + ", " + i);
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
				//System.out.println("Removing from minimap on " + i + ", " + zval);
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
					//System.out.println("(" + chunkX + ") Removing from minimap on " + xval + ", " + i);
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
	public byte[] getChunkData(int chunkX, int chunkZ) {
		Chunk c = this.getChunk(chunkX, chunkZ);
		if(c == null) {
			return new byte[32768];
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
