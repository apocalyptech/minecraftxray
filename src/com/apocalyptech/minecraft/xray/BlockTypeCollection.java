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

import java.io.FileReader;
import java.util.HashMap;
import java.util.ArrayList;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Class to hold a collection of BlockTypes.  Can read itself
 * in via a YAML file.
 *
 * Like the BlockType class, this class suffers from a bit of split-
 * personality.  You'll see the "blocksByName" and "blockArray" values
 * below, but those are only populated when addBlockType() is called
 * manually on the class.
 *
 * It's set up this way because this class does sort of serve two purposes:
 * 1) to load in a specific set of blocks from a Yaml file, and 2) to
 * provide a global catalog of all available block types for X-Ray (which
 * may include one or more additional Yaml files (for mods) beyond the
 * basic Minecraft set)
 *
 * So what happens is that first we'll read in a Yaml file and get a
 * BlockTypeCollection out of it...  Then we'll one-by-one add those
 * BlockTypes into the "master" collection (which incidentally allows us
 * to do some better sanity checking, like making sure that we're not
 * overwriting block types, etc) using addBlockType().
 *
 * I suppose I could write two classes for this, but it seems more silly
 * to have two classes that do effectively the same thing (ie: contain
 * a group of BlockTypes).  I've got similar reasoning for why I'm not
 * using two different BlockType classes; one for Yaml-loading and one for
 * runtime.
 */
public class BlockTypeCollection
{

	private String name;
	private ArrayList<BlockType> blocks;
	private HashMap<String, BlockType> blocksByName;
	public BlockType[] blockArray;

	public BlockTypeCollection()
	{
		this.blocks = new ArrayList<BlockType>();
		this.blocksByName = new HashMap<String, BlockType>();
		this.blockArray = new BlockType[256];
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public void setBlocks(ArrayList<BlockType> blocks)
	{
		this.blocks = blocks;
	}

	public ArrayList<BlockType> getBlocks()
	{
		return this.blocks;
	}

	/**
	 * Adds a new block type
	 */
	public void addBlockType(BlockType newBlockType)
		throws BlockTypeLoadException
	{
		this.addBlockType(newBlockType, true);
	}

	/**
	 * Adds a new block type to this collection.
	 *
	 * TODO: Errors should be more informative
	 */
	public void addBlockType(BlockType newBlockType, boolean importData)
		throws BlockTypeLoadException
	{
		String name = newBlockType.getIdStr();
		short id = newBlockType.getId();
		boolean override = newBlockType.getOverride();
		boolean exists_name = false;
		boolean exists_id = false;

		if (this.blocksByName.containsKey(name))
		{
			if (override)
			{
				exists_name = true;
			}
			else
			{
				throw new BlockTypeLoadException("\"" + name + "\" already exists");
			}
		}
		if (this.blockArray[id] != null)
		{
			if (override)
			{
				exists_id = true;
			}
			else
			{
				throw new BlockTypeLoadException("Block ID " + id + " already exists");
			}
		}

		// If we're just validating, return now
		if (!importData)
		{
			return;
		}

		// If we get here, our block was set to override, so clear out old records first
		if (exists_id)
		{
			this.blocks.remove(blockArray[id]);
			blockArray[id] = null;
		}
		if (exists_name)
		{
			this.blocks.remove(this.blocksByName.get(name));
			this.blocksByName.remove(name);
		}

		// Now add the new one
		this.blocks.add(newBlockType);
		this.blocksByName.put(newBlockType.getIdStr(), newBlockType);
		this.blockArray[newBlockType.getId()] = newBlockType;
	}

	public BlockType getByName(String name)
	{
		if (this.blocksByName.containsKey(name))
		{
			return this.blocksByName.get(name);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Loads from a Yaml document.
	 */
	public static BlockTypeCollection loadFromYaml(String filename)
		throws java.io.FileNotFoundException
	{
		Constructor constructor = new Constructor(BlockTypeCollection.class);
		TypeDescription blockDesc = new TypeDescription(BlockTypeCollection.class);
		blockDesc.putListPropertyType("blocks", BlockType.class);
		Yaml yaml = new Yaml(constructor);

		return (BlockTypeCollection) yaml.load(new FileReader(filename));
	}
}
