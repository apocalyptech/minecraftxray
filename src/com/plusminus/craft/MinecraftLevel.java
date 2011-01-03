package com.plusminus.craft;

import java.awt.Point;
import java.io.File;
import java.util.HashMap;

import com.plusminus.craft.dtf.ByteArrayTag;
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
		
	public HashMap<Long, Chunk> levelData;
	
	private int world;
	private Block spawnPoint;
	private Block playerPos;
	private float playerYaw;
	private float playerPitch;
	private boolean nether;
	
	public Texture minecraftTexture;
	public Texture portalTexture;


	/***
	 * Create a minecraftLevel from the given world
	 * @param world
	 */
	public MinecraftLevel(int world, boolean nether, Texture minecraftTexture, Texture portalTexture) {
		this.world = world;
		this.nether = nether;
		this.minecraftTexture = minecraftTexture;
		this.portalTexture = portalTexture;
		//this.levelData = new Chunk[LEVEL_MAX_WIDTH][LEVEL_MAX_HEIGHT];
		this.levelData = new HashMap<Long, Chunk>();
		File levelFile = new File(MineCraftEnvironment.getWorldDirectory(world), "level.dat");
		
		CompoundTag levelData = (CompoundTag) DTFReader.readDTFFile(levelFile);
		
		//	System.out.println(levelData.toString());
		
		CompoundTag levelDataData = (CompoundTag) levelData.getTagWithName("Data");
		CompoundTag levelPlayerData = (CompoundTag) levelDataData.getTagWithName("Player");
		if(levelPlayerData != null) {
			
			// Figure out what dimension the player's in.  If it matches, move our camera there.
			// TODO: if playerDim is null, perhaps we should move the camera to the spawnpoint...
			IntTag playerDim = (IntTag) levelPlayerData.getTagWithName("Dimension");
			if (playerDim != null && ((playerDim.value == 0 && !nether) || (playerDim.value == -1 && nether)))
			{
				ListTag playerPos = (ListTag) levelPlayerData .getTagWithName("Pos");
				ListTag playerRotation = (ListTag) levelPlayerData .getTagWithName("Rotation");
		
				DoubleTag posX = (DoubleTag) playerPos.value.get(0);
				DoubleTag posY = (DoubleTag) playerPos.value.get(1);
				DoubleTag posZ = (DoubleTag) playerPos.value.get(2);
				
				FloatTag rotYaw = (FloatTag) playerRotation.value.get(0);
				FloatTag rotPitch = (FloatTag) playerRotation.value.get(1);

				this.playerPos = new Block((int) -posX.value, (int) -posY.value, (int) -posZ.value);
				this.playerYaw = rotYaw.value;
				this.playerPitch = rotPitch.value;

			}
			else
			{
				this.playerPos = new Block(0,0,0);
				this.playerYaw =0;
				this.playerPitch = 0;				
			}
	
			// Set the spawn point if we're not in the Nether
			if (nether)
			{
				this.spawnPoint = new Block(0,0,0);				
			}
			else
			{
				IntTag spawnX = (IntTag) levelDataData.getTagWithName("SpawnX");
				IntTag spawnY = (IntTag) levelDataData.getTagWithName("SpawnY");
				IntTag spawnZ = (IntTag) levelDataData.getTagWithName("SpawnZ");
				this.spawnPoint = new Block(-spawnX.value, -spawnY.value, -spawnZ.value);
			}
		} else {
			this.spawnPoint = new Block(0,0,0);
			this.playerPos = new Block(0,0,0);
			this.playerYaw =0;
			this.playerPitch = 0;
		}
			
		
	}
	
	/***
	 * returns the spawning point for this level
	 */
	public Block getSpawnPoint() {
		return this.spawnPoint;
	}
	
	public Block getPlayerPosition() {
		return this.playerPos;
	}
	
	public float getPlayerPitch() {
		return this.playerPitch;
	}
	
	public float getPlayerYaw() {
		return this.playerYaw;
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
		for (Chunk chunk : this.levelData.values())
		{
			chunk.isSelectedDirty = true;
			if (main_dirty)
			{
				chunk.isDirty = true;
			}
		}
	}
	
	public Tag loadChunk(int x, int z) {
		File chunkFile = MineCraftEnvironment.getChunkFile(world, x,z, this.nether);
		if(!chunkFile.exists()) {
			return null;
		}
		Tag t = DTFReader.readDTFFile(chunkFile);

		//levelData[x+LEVEL_HALF_WIDTH][z+LEVEL_HALF_HEIGHT] = new Chunk(t, this);
		long key = this.getChunkKey(x, z);
		Chunk chunk = new Chunk(this, t);
		this.levelData.put(key, chunk);
		
		return t;
	}
	
	/**
	 * Returns the internal key that we use to store a given chunk.  Internally
	 * these are stored as a hashmap.  Note that right now we're only using ints
	 * for the chunk coordinates, so X-Ray cannot actually load every possible
	 * Minecraft chunk (in fact it's quite a bit more limited, though I'd guess that
	 * nobody'll end up actually bumping up against our limit.
	 * 
	 * TODO: It may be nice to just use a HashMap of HashMaps, both with long keys,
	 * so that we can support all possible Minecraft chunks.
	 * 
	 * @param chunkX
	 * @param chunkZ
	 * @return
	 */
	private long getChunkKey(int chunkX, int chunkZ)
	{
		return ((long)chunkX<<32)+(long)chunkZ;
	}
	
	/**
	 * Gets the specified Chunk object
	 *
	 * @param chunkX
	 * @param chunkZ
	 * @return
	 */
	public Chunk getChunk(int chunkX, int chunkZ) {
		/*Chunk chunk =  levelData[chunkX+LEVEL_HALF_WIDTH][chunkZ+LEVEL_HALF_HEIGHT];
		CompoundTag global = (CompoundTag) chunk.getChunkData();
		CompoundTag level = (CompoundTag) global.value.get(0); // first tag
		return level;*/
		long key = this.getChunkKey(chunkX, chunkZ);
		if (this.levelData.containsKey(key))
		{
			return this.levelData.get(key);
		}
		else
		{
			return null;
		}
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
