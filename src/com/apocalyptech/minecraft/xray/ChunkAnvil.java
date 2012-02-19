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
import java.util.Collections;
import java.util.regex.PatternSyntaxException;

import org.lwjgl.opengl.GL11;

import com.apocalyptech.minecraft.xray.dtf.ShortArrayTag;
import com.apocalyptech.minecraft.xray.dtf.ByteArrayTag;
import com.apocalyptech.minecraft.xray.dtf.CompoundTag;
import com.apocalyptech.minecraft.xray.dtf.StringTag;
import com.apocalyptech.minecraft.xray.dtf.ListTag;
import com.apocalyptech.minecraft.xray.dtf.ByteTag;
import com.apocalyptech.minecraft.xray.dtf.IntTag;
import com.apocalyptech.minecraft.xray.dtf.Tag;

import static com.apocalyptech.minecraft.xray.MinecraftConstants.*;

/**
 * A new-style "Anvil" chunk.  Similar to the original, except that the
 * data is split up into 16x16x16 "Sections"
 */
public class ChunkAnvil extends Chunk {

	private HashMap<Integer, ShortArrayTag> blockData;
	private HashMap<Integer, ByteArrayTag> mapData;
	private HashMap<Integer, Boolean> availableSections;
	private ArrayList<Integer> availableSectionsList;

	private int lSection;
	
	public ChunkAnvil(MinecraftLevel level, Tag data) {

		super(level, data);

		blockData = new HashMap<Integer, ShortArrayTag>();
		mapData = new HashMap<Integer, ByteArrayTag>();
		availableSections = new HashMap<Integer, Boolean>();
		availableSectionsList = new ArrayList<Integer>();
		
		ListTag sectionsTag = (ListTag) this.levelTag.getTagWithName("Sections");
		for (Tag sectionTagTemp : sectionsTag.value)
		{
			CompoundTag sectionTag = (CompoundTag) sectionTagTemp;
			ByteTag sectionNumTag = (ByteTag) sectionTag.getTagWithName("Y");
			int section = sectionNumTag.value;
			availableSections.put(section, true);
			availableSectionsList.add(section);
			blockData.put(section, (ShortArrayTag) sectionTag.getTagWithName("Blocks"));
			mapData.put(section, (ByteArrayTag) sectionTag.getTagWithName("Data"));

			// Merge in the AddBlocks tag, if present
			ByteArrayTag addBlocksTag = (ByteArrayTag) sectionTag.getTagWithName("AddBlocks");
			if (addBlocksTag != null)
			{
				ByteArrayTag dataTag = mapData.get(section);
				int data_add;
				for (int offset = 0; offset < 4096; offset++)
				{
					// TODO: Java's lack of unsigned datatypes is annoying.  We should
					// really doublecheck to make sure that we're not doing things we
					// shouldn't with negative values, here.  A little test app I wrote
					// seems to say that this should Do The Right Thing here...
					data_add = addBlocksTag.value[offset / 2];
					if (offset % 2 == 1)
					{
						data_add = (data_add >> 4);
					}
					dataTag.value[offset] += ((data_add & 0xF) << 8);
				}
			}
		}

		// Make sure our list of available sections is ordered
		Collections.sort(availableSectionsList);

		// And set our max height for the chunk
		this.maxHeight = ((availableSectionsList.get(availableSectionsList.size()-1)+1)*16)-1;

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

		// We'll want to process our sections in reverse order
		Collections.reverse(this.availableSectionsList);

		// Do the loop
		int offset;
		short block;
		ShortArrayTag blockDataTag;
		for (int zz = 0; zz < 16; zz++)
		{
			for (int xx = 0; xx < 16; xx++)
			{
				// determine the top most visible block
				found_air = !in_nether;
				drew_block = false;
				found_solid = false;

				sectionloop: for (int section : this.availableSectionsList)
				{
					blockDataTag = this.blockData.get(section);
					if (blockDataTag == null)
					{
						continue;
					}
					for (int yy = 15; yy >= 0; yy--)
					{
						offset = xx + (zz * 16) + (yy * 256);
						block = blockDataTag.value[offset];

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
								break sectionloop;
							}
						}
						else
						{
							found_air = true;
						}
					}
				}

				// Make sure we don't have holes in our Nether minimap
				if (in_nether && found_solid && !drew_block)
				{
					minimap[xx][zz] = MinecraftConstants.BLOCK_BEDROCK.id;
				}
			}
		}

		// re-reverse our section list
		Collections.reverse(this.availableSectionsList);
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
			int section = y/16;
			if (this.blockData.containsKey(section))
			{
				return blockData.get(section).value[blockOffset-1];
			}
			else
			{
				return 0;
			}
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
			int section = y/16;
			if (this.blockData.containsKey(section))
			{
				return blockData.get(section).value[blockOffset+1];
			}
			else
			{
				return 0;
			}
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
			int section = y/16;
			if (blockData.containsKey(section))
			{
				return blockData.get(section).value[blockOffset-16];
			}
			else
			{
				return 0;
			}
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
			int section = y/16;
			if (blockData.containsKey(section))
			{
				return blockData.get(section).value[blockOffset+16];
			}
			else
			{
				return 0;
			}
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
		int section = y/16;
		if ((y % 16) == 15)
		{
			if (blockData.containsKey(section + 1))
			{
				return blockData.get(section+1).value[x + (z*16)];
			}
			else
			{
				return 0;
			}
		}
		else
		{
			return blockData.get(section).value[blockOffset+256];
		}
	}

	/**
	 * Gets the Block ID of the block immediately down.
	 * Will return -1 if we're already at the bottom
	 */
	protected short getAdjDownBlockId(int x, int y, int z, int blockOffset)
	{
		int section = y/16;
		if ((y % 16) == 0)
		{
			if (blockData.containsKey(section - 1))
			{
				return blockData.get(section-1).value[3840 + x + (16*z)];
			}
			else
			{
				return 0;
			}
		}
		else
		{
			return blockData.get(section).value[blockOffset-256];
		}
	}
	
	/**
	 * Gets the block ID at the specified coordinate in the chunk.  This is
	 * only really used in the getAdj*BlockId() methods.
	 */
	public short getBlock(int x, int y, int z) {
		int section = y/16;
		if (blockData.containsKey(section))
		{
			return blockData.get(section).value[((y % 16) * 256) + (z * 16) + x];
		}
		else
		{
			return 0;
		}
	}

	/**
	 * Gets the block data at the specified coordinates.
	 */
	public byte getData(int x, int y, int z) {
		int section = y/16;
		if (mapData.containsKey(section))
		{
			int offset = ((y%16)*256) + (z * 16) + x;
			int halfOffset = offset / 2;
			if(offset % 2 == 0) {
				return (byte) (mapData.get(section).value[halfOffset] & 0xF);
			} else {
				// We shouldn't have to &0xF here, but if we don't the value
				// returned could be negative, even though that would be silly.
				return (byte) ((mapData.get(section).value[halfOffset] >> 4) & 0xF);
			}
		}
		else
		{
			return (byte)0;
		}
	}

	/**
	 * Rewind our loop
	 */
	protected void rewindLoop()
	{
		super.rewindLoop();
		this.lSection = -1;
	}

	/**
	 * Advances our block loop
	 */
	protected short nextBlock()
	{
		boolean advance_to_next_section = false;
		boolean found_next_section = false;
		this.lOffset = ((this.lOffset+1) % 4096);
		if (this.lOffset == 0)
		{
			advance_to_next_section = true;
			for (int section : this.availableSectionsList)
			{
				if (section > this.lSection)
				{
					found_next_section = true;
					this.lSection = section;
					break;
				}
			}
		}
		if (advance_to_next_section && !found_next_section)
		{
			return -2;
		}
		this.lx = this.lOffset % 16;
		this.lz = (this.lOffset / 16) % 16;
		this.ly = (this.lOffset / 256) + (16*this.lSection);

		return this.blockData.get(this.lSection).value[this.lOffset];
	}

}
