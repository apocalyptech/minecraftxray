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

import java.io.File;
import java.io.FileReader;
import java.util.Map;
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
 *
 * Likewise, the texture reservation system only takes into account types
 * added via addBlockType()
 */
public class BlockTypeCollection
{

	private String name;
	private String texpath;
	private ArrayList<BlockTypeRegular> blocks;
	private ArrayList<BlockTypeFilename> mlblocks;
	private ArrayList<BlockType> blocks_composite;
	private HashMap<String, BlockType> blocksByName;
	public BlockType[] blockArray;
	private boolean[] usedTextures;
	private int reserved_texture_count;
	private File file;
	private boolean global;
	private BlockTypeLoadException exception;

	public BlockTypeCollection()
	{
		this.blocks = new ArrayList<BlockTypeRegular>();
		this.mlblocks = new ArrayList<BlockTypeFilename>();
		this.blocks_composite = new ArrayList<BlockType>();
		this.blocksByName = new HashMap<String, BlockType>();
		this.blockArray = new BlockType[256];
		this.usedTextures = new boolean[256];
		this.reserved_texture_count = 0;
		this.global = false;
		this.exception = null;
		for (int i=0; i<256; i++)
		{
			this.usedTextures[i] = false;
		}
	}

	/**
	 * Constructor to use when we're using this as a record of a failed load.
	 */
	public BlockTypeCollection(File file, boolean global, BlockTypeLoadException exception)
	{
		this();
		this.setFile(file);
		this.setGlobal(global);
		this.setException(exception);
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public void setTexpath(String texpath)
	{
		this.texpath = texpath;
	}

	public String getTexpath()
	{
		return this.texpath;
	}

	public void setBlocks(ArrayList<BlockTypeRegular> blocks)
	{
		this.blocks = blocks;
	}

	public ArrayList<BlockTypeRegular> getBlocks()
	{
		return this.blocks;
	}

	public void setMlblocks(ArrayList<BlockTypeFilename> mlblocks)
	{
		this.mlblocks = mlblocks;
	}

	public ArrayList<BlockTypeFilename> getMlblocks()
	{
		return this.mlblocks;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public File getFile()
	{
		return this.file;
	}

	public void setGlobal(boolean global)
	{
		this.global = global;
	}

	public boolean getGlobal()
	{
		return this.global;
	}

	public void setException(BlockTypeLoadException exception)
	{
		this.exception = exception;
	}

	public BlockTypeLoadException getException()
	{
		return this.exception;
	}

	public ArrayList<BlockType> getBlocksFull()
	{
		ArrayList<BlockType> blocks = new ArrayList<BlockType>();
		for (BlockTypeRegular block : this.blocks)
		{
			blocks.add((BlockType)block);
		}
		for (BlockTypeFilename block : this.mlblocks)
		{
			blocks.add((BlockType)block);
		}
		for (BlockType block : this.blocks_composite)
		{
			blocks.add((BlockType)block);
		}
		return blocks;
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
			this.blocks_composite.remove(blockArray[id]);
			blockArray[id] = null;
		}
		if (exists_name)
		{
			this.blocks_composite.remove(this.blocksByName.get(name));
			this.blocksByName.remove(name);
		}

		// Now add the new one
		this.blocks_composite.add(newBlockType);
		this.blocksByName.put(newBlockType.getIdStr(), newBlockType);
		this.blockArray[newBlockType.getId()] = newBlockType;

		// Mark our textures as "used"
		for (Integer tex : newBlockType.getUsedTextures())
		{
			this.useTexture(tex);
		}
		//System.out.println("Textures used by " + newBlockType.idStr + ": " + newBlockType.getUsedTextures().toString());
		//System.out.println(this.usedTextureCount() + " textures used, " + this.unusedTextureCount() + " textures free.");
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
	 * Marks a specific texture as "used"
	 */
	public void useTexture(int texture)
	{
		if (texture > 255 || texture < 0)
		{
			return;
		}
		this.usedTextures[texture] = true;
	}

	/**
	 * Reserves an unused texture, and returns the texture ID.
	 * Returns -1 if there are no unused texture slots.  Also,
	 * for now we're assuming that this is being used on
	 * blocks that use filenames for textures, so we're going
	 * to increment that count to use as an offset, even though
	 * probably that'll never actually come into play.
	 */
	public int reserveTexture()
	{
		for (int i=0; i<256; i++)
		{
			if (!this.usedTextures[i])
			{
				this.useTexture(i);
				this.reserved_texture_count++;
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the total number of unused textures.
	 */
	public int unusedTextureCount()
	{
		int count = 0;
		for (int i=0; i<256; i++)
		{
			if (!this.usedTextures[i])
			{
				count++;
			}
		}
		count -= this.getFilenameTextureCount();
		count += this.reserved_texture_count;
		return count;
	}

	/**
	 * Returns the total number of used textures.
	 */
	public int usedTextureCount()
	{
		int count = 0;
		for (int i=0; i<256; i++)
		{
			if (this.usedTextures[i])
			{
				count++;
			}
		}
		count += this.getFilenameTextureCount();
		count -= this.reserved_texture_count;
		return count;
	}

	/**
	 * Loads from a Yaml document.
	 */
	public static BlockTypeCollection loadFromYaml(String filename, boolean global)
		throws java.io.FileNotFoundException
	{
		Constructor constructor = new Constructor(BlockTypeCollection.class);
		TypeDescription blockDesc = new TypeDescription(BlockTypeCollection.class);
		blockDesc.putListPropertyType("blocks", BlockTypeRegular.class);
		blockDesc.putListPropertyType("mlblocks", BlockTypeFilename.class);
		Yaml yaml = new Yaml(constructor);

		BlockTypeCollection collection = (BlockTypeCollection) yaml.load(new FileReader(filename));
		for (BlockType type : collection.getBlocksFull())
		{
			type.pullDataFromCollection(collection);
		}
		collection.setFile(new File(filename));
		collection.setGlobal(global);
		return collection;
	}

	/**
	 * Normalizes all YAML-read blocks.
	 *
	 * TODO: set a "normalized" flag
	 */
	public void normalizeBlocks()
		throws BlockTypeLoadException
	{
		for (BlockType block : this.getBlocksFull())
		{
			ExceptionDialog.setExtraStatus2("Looking at block ID " + block.id + ": " + block.idStr);
			block.normalizeData();
			for (int tex_idx : block.getUsedTextures())
			{
				this.useTexture(tex_idx);
			}
		}
		ExceptionDialog.clearExtraStatus2();
	}

	/**
	 * Imports blocktypes from another otherCollection.  The blocks in otherCollection
	 * should be already-normalized.
	 *
	 * TODO: set a "normalized" flag in there, and check for it.
	 */
	public void importFrom(BlockTypeCollection otherCollection, boolean importData)
		throws BlockTypeLoadException
	{
		for (BlockType block : otherCollection.getBlocksFull())
		{
			ExceptionDialog.setExtraStatus2("Looking at block ID " + block.id + ": " + block.idStr);
			this.addBlockType(block, importData);
		}
		ExceptionDialog.clearExtraStatus2();
	}

	/**
	 * Returns a list of all texture filenames that this collection is using.
	 */
	private ArrayList<String> getFilenameTextureList()
	{
		HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
		ArrayList<String> list = new ArrayList<String>();

		// Grab all the textures
		for (BlockType block : this.getBlocksFull())
		{
			for (String filename : block.getTextureFilenames())
			{
				if (!tempMap.containsKey(filename))
				{
					tempMap.put(filename, null);
					list.add(filename);
				}
			}
		}

		// Return
		return list;
	}

	/**
	 * Returns a count of all the filename textures we have in the collection.
	 */
	public int getFilenameTextureCount()
	{
		return this.getFilenameTextureList().size();
	}

	/**
	 * Loops through all our blocks to get a list of filenames which should be loaded into
	 * textures, update those blocks once the textures have been reserved, and then return
	 * the information so that those blocks can get actually loaded.
	 */
	public HashMap<String, Integer> getFilenameTextureBlocks()
		throws BlockTypeLoadException
	{
		HashMap<String, Integer> list = new HashMap<String, Integer>();

		// Load in our list of textures and reserve a texture for each
		for (String filename : this.getFilenameTextureList())
		{
			list.put(filename, this.reserveTexture());
		}

		// Update our blocks with the fresh texture location information
		for (BlockType block : this.getBlocksFull())
		{
			block.setTextureFilenameMapping(list);
		}

		// ... and return this list, so the files can actually be loaded.
		return list;
	}
}
