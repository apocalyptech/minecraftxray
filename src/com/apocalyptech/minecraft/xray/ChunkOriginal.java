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
 * Chunk functions, including the meat of our rendering stuffs
 *
 * TODO: There are a lot of functions that do very similar things in here, it would be
 * good to consolidate some of those.  I don't know why it took me so long to come
 * up with the current implementation of renderVertical and renderHorizontal - I suspect
 * that much of the rendering code would be improved by moving to those if possible.
 */
public class ChunkOriginal extends Chunk {

	private ShortArrayTag blockData;
	private ByteArrayTag mapData;
	
	public ChunkOriginal(MinecraftLevel level, Tag data) {

		super(level, data);
		
		blockData = (ShortArrayTag) this.levelTag.getTagWithName("Blocks");
		mapData = (ByteArrayTag) this.levelTag.getTagWithName("Data");

		// Compute which texture sheets are in-use by this chunk
		// Much of this is copied from our main render loop, way down below
		this.usedTextureSheets = new HashMap<Integer, Boolean>();
		for(int x=0; x<16; x++) {
			int xOff = (x * 128 * 16);
			for(int z=0; z<16; z++) {
				int zOff = (z * 128);
				int blockOffset = zOff + xOff-1;
				for(int y=0; y<128; y++) {
					blockOffset++;
					int t = blockData.value[blockOffset];
					if(t < 1) {
						continue;
					}
					BlockType block = blockArray[t];
					if (block == null)
					{
						block = BLOCK_UNKNOWN;
					}
					this.usedTextureSheets.put(block.getTexSheet(), true);
				}
			}
		}

		this.finishConstructor();
	}
	
	/**
	 * Will return an array of values which are suitable for feeding into a
	 * minimap.
	 */
	public short[] getMinimapValues(boolean nether)
	{
		return new short[0];
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
		if (y >= 127)
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
		int max_y = Math.min(127, sy+distance);
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
