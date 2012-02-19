/**
 * Copyright (c) 2010-2012, Vincent Vollers and Christopher J. Kucera
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

import java.lang.Math;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import org.lwjgl.opengl.GL11;

import com.apocalyptech.minecraft.xray.dtf.ShortArrayTag;
import com.apocalyptech.minecraft.xray.dtf.ByteArrayTag;
import com.apocalyptech.minecraft.xray.dtf.CompoundTag;
import com.apocalyptech.minecraft.xray.dtf.StringTag;
import com.apocalyptech.minecraft.xray.dtf.ListTag;
import com.apocalyptech.minecraft.xray.dtf.IntTag;
import com.apocalyptech.minecraft.xray.dtf.Tag;

import static com.apocalyptech.minecraft.xray.MinecraftConstants.*;

/**
 * The original style chunk, basically from Alpha on upwards, until the
 * weekly release 12w07a (and, eventually, Minecraft 1.2).  All data
 * for the chunk is stored directly in the main tags, with an effective
 * height limit of 128.
 */
public class ChunkOriginal extends Chunk {
	
	private static final int BLOCKSPERROW = 128;
	private static final int BLOCKSPERCOLUMN = BLOCKSPERROW * 16;

	private ShortArrayTag blockData;
	private ByteArrayTag mapData;
	
	public ChunkOriginal(MinecraftLevel level, Tag data) {

		super(level, data);

		this.maxHeight = 127;
		
		blockData = (ShortArrayTag) this.levelTag.getTagWithName("Blocks");
		mapData = (ByteArrayTag) this.levelTag.getTagWithName("Data");

		this.finishConstructor();
	}
	
	/**
	 * Will return an array of values which are suitable for feeding into a
	 * minimap.
	 */
	public short[][] getMinimapValues()
	{
		short[][] minimap = new short[16][16];
		boolean in_nether = this.level.world.isDimension(-1);
		boolean found_air;
		boolean found_solid;
		boolean drew_block;

		int blockOffset;
		short block;
		for (int zz = 0; zz < 16; zz++)
		{
			for (int xx = 0; xx < 16; xx++)
			{
				// determine the top most visible block
				found_air = !in_nether;
				drew_block = false;
				found_solid = false;
				for (int yy = this.maxHeight; yy >= 0; yy--)
				{
					blockOffset = yy + (zz * 128) + (xx * 128 * 16);
					block = blockData.value[blockOffset];

					if (block > 0)
					{
						if (in_nether && !found_solid)
						{
							found_air = false;
						}
						found_solid = true;
						if (found_air)
						{
							minimap[xx][zz] = block;
							drew_block = true;
							break;
						}
					}
					else
					{
						found_air = true;
					}
				}

				// Make sure we don't have holes in our Nether minimap
				if (in_nether && found_solid && !drew_block)
				{
					minimap[xx][zz] = MinecraftConstants.BLOCK_BEDROCK.id;
				}
			}
		}

		return minimap;
	}

	/**
	 * Gets the Block ID of the block immediately to the west.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	protected short getAdjWestBlockId(int x, int y, int z, int blockOffset)
	{
		if (x > 0)
		{
			return blockData.value[blockOffset-BLOCKSPERCOLUMN];
		}
		else
		{
			Chunk otherChunk = level.getChunk(this.x-1, this.z);
			if (otherChunk == null)
			{
				return -1;
			}
			else
			{
				return otherChunk.getBlock(15, y, z);
			}
		}
	}

	/**
	 * Gets the Block ID of the block immediately to the east.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	protected short getAdjEastBlockId(int x, int y, int z, int blockOffset)
	{
		if (x < 15)
		{
			return blockData.value[blockOffset+BLOCKSPERCOLUMN];
		}
		else
		{
			Chunk otherChunk = level.getChunk(this.x+1, this.z);
			if (otherChunk == null)
			{
				return -1;
			}
			else
			{
				return otherChunk.getBlock(0, y, z);
			}
		}
	}

	/**
	 * Gets the Block ID of the block immediately to the south.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	protected short getAdjNorthBlockId(int x, int y, int z, int blockOffset)
	{
		if (z > 0)
		{
			return blockData.value[blockOffset-BLOCKSPERROW];
		}
		else
		{
			Chunk otherChunk = level.getChunk(this.x, this.z-1);
			if (otherChunk == null)
			{
				return -1;
			}
			else
			{
				return otherChunk.getBlock(x, y, 15);
			}
		}
	}

	/**
	 * Gets the Block ID of the block immediately to the north.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	protected short getAdjSouthBlockId(int x, int y, int z, int blockOffset)
	{
		if (z < 15)
		{
			return blockData.value[blockOffset+BLOCKSPERROW];
		}
		else
		{
			Chunk otherChunk = level.getChunk(this.x, this.z+1);
			if (otherChunk == null)
			{
				return -1;
			}
			else
			{
				return otherChunk.getBlock(x, y, 0);
			}
		}
	}

	/**
	 * Gets the Block ID of the block immediately up.
	 * Will return -1 if we're already at the top
	 */
	protected short getAdjUpBlockId(int x, int y, int z, int blockOffset)
	{
		if (y >= this.maxHeight)
		{
			return -1;
		}
		else
		{
			return blockData.value[blockOffset+1];
		}
	}

	/**
	 * Gets the Block ID of the block immediately down.
	 * Will return -1 if we're already at the bottom
	 */
	protected short getAdjDownBlockId(int x, int y, int z, int blockOffset)
	{
		if (y <= 0)
		{
			return -1;
		}
		else
		{
			return blockData.value[blockOffset-1];
		}
	}
	
	/**
	 * Gets the block ID at the specified coordinate in the chunk.  This is
	 * only really used in the getAdj*BlockId() methods.
	 */
	public short getBlock(int x, int y, int z) {
		return blockData.value[y + (z * 128) + (x * 128 * 16)];
	}

	/**
	 * Gets the block data at the specified coordinates.
	 */
	public byte getData(int x, int y, int z) {
		int offset = y + (z * 128) + (x * 128 * 16);
		int halfOffset = offset / 2;
		if(offset % 2 == 0) {
			return (byte) (mapData.value[halfOffset] & 0xF);
		} else {
			// We shouldn't have to &0xF here, but if we don't the value
			// returned could be negative, even though that would be silly.
			return (byte) ((mapData.value[halfOffset] >> 4) & 0xF);
		}
	}
	
	/**
	 * Tests if the given source block has a torch nearby.  This is, I'm willing
	 * to bet, the least efficient way possible of doing this.  It turns out that
	 * despite that, it doesn't really have a noticeable impact on performance,
	 * which is why it remains in here, but perhaps one day I'll rewrite this
	 * stuff to be less stupid.  The one upside to doing it like this is that
	 * we're not using any extra memory storing data about which block should be
	 * highlighted...
	 *
	 * TODO: Should implement this in Chunk, not here.
	 * 
	 * @param sx
	 * @param sy
	 * @param sz
	 * @return
	 */
	public boolean hasAdjacentTorch(int sx, int sy, int sz)
	{
		int distance = 3;
		int x, y, z;
		int min_x = sx-distance;
		int max_x = sx+distance;
		int min_z = sz-distance;
		int max_z = sz+distance;
		int min_y = Math.max(0, sy-distance);
		int max_y = Math.min(this.maxHeight, sy+distance);
		Chunk otherChunk;
		int cx, cz;
		int tx, tz;
		for (x = min_x; x<=max_x; x++)
		{
			for (y = min_y; y<=max_y; y++)
			{
				for (z = min_z; z<=max_z; z++)
				{
					otherChunk = null;
					if (x < 0)
					{
						cx = this.x-1;
						tx = 16+x;
					}
					else if (x > 15)
					{
						cx = this.x+1;
						tx = x-16;
					}
					else
					{
						cx = this.x;
						tx = x;
					}

					if (z < 0)
					{
						cz = this.z-1;
						tz = 16+z;
					}
					else if (z > 15)
					{
						cz = this.z+1;
						tz = z-16;
					}
					else
					{
						cz = this.z;
						tz = z;
					}
					
					if (cx != this.x || cz != this.z)
					{
						otherChunk = level.getChunk(cx, cz);
						if (otherChunk == null)
						{
							continue;
						}
						/* TODO: yeah
						else if (exploredBlocks.containsKey(otherChunk.blockData.value[(tz*128)+(tx*128*16)+y]))
						{
							return true;
						}
						*/
					}
					else
					{
						/* TODO: yeah
						if (exploredBlocks.containsKey(blockData.value[(z*128)+(x*128*16)+y]))
						{
							return true;
						}
						*/
					}
				}
			}
		}
		return false;
	}

	/**
	 * Advances our block loop
	 */
	protected short nextBlock()
	{
		this.lOffset++;
		if (this.lOffset >= 32768)
		{
			return -2;
		}
		this.ly = this.lOffset % 128;
		this.lz = (this.lOffset / 128) % 16;
		this.lx = this.lOffset / 2048;
		return this.blockData.value[this.lOffset];
	}

}
