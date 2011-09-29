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

import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

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
	private String texfile;
	private ArrayList<BlockTypeRegular> blocks;
	private ArrayList<BlockTypeFilename> mlblocks;
	private ArrayList<BlockType> blocks_composite;
	private HashMap<String, BlockType> blocksByName;
	public BlockType[] blockArray;
	private ArrayList<Boolean[]> usedTextures;
	private ArrayList<Integer> reserved_texture_count;
	public ArrayList<BufferedImage> textures;
	public int square_width;
	private int cur_texture_page;
	private File file;
	private boolean global;
	private BlockTypeLoadException exception;
	private ArrayList<BlockTypeCollection> loadedCollections;

	public BlockTypeCollection()
	{
		this.blocks = new ArrayList<BlockTypeRegular>();
		this.mlblocks = new ArrayList<BlockTypeFilename>();
		this.blocks_composite = new ArrayList<BlockType>();
		this.blocksByName = new HashMap<String, BlockType>();
		this.loadedCollections = new ArrayList<BlockTypeCollection>();
		this.blockArray = new BlockType[256];
		this.usedTextures = new ArrayList<Boolean[]>();
		this.reserved_texture_count = new ArrayList<Integer>();
		this.textures = new ArrayList<BufferedImage>();
		this.cur_texture_page = -1;
		this.global = false;
		this.exception = null;
		this.addTextureMap(true);
	}

	private BufferedImage addTextureMap()
	{
		return this.addTextureMap(false);
	}

	private BufferedImage addTextureMap(boolean initial)
	{
		Boolean[] usedTextures = new Boolean[256];
		int res_tex_count = 0;
		for (int i=0; i<256; i++)
		{
			usedTextures[i] = false;
		}
		this.usedTextures.add(usedTextures);
		this.reserved_texture_count.add(res_tex_count);
		this.cur_texture_page++;
		if (!initial)
		{
			BufferedImage rootImg = this.textures.get(0);
			BufferedImage image = new BufferedImage(rootImg.getWidth(), rootImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
			this.textures.add(image);
			return image;
		}
		return null;
	}

	public void setInitialTexture(BufferedImage image)
	{
		if (this.textures.size() > 0)
		{
			return;
		}
		this.textures.add(image);
		this.square_width = image.getWidth()/16;
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

	public void setTexfile(String texfile)
	{
		this.texfile = texfile;
	}

	public String getTexfile()
	{
		return this.texfile;
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
		if (newBlockType.texfile == null)
		{
			for (Integer tex : newBlockType.getUsedTextures())
			{
				this.useTexture(tex);
			}
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
		this.usedTextures.get(this.cur_texture_page)[texture] = true;
	}

	public int reserveTexture()
	{
		return this.reserveTexture(true);
	}

	/**
	 * Reserves an unused texture, and returns the texture ID.
	 * Returns -1 if there are no unused texture slots.  Also,
	 * for now we're assuming that this is being used on
	 * blocks that use filenames for textures, so we're going
	 * to increment that count to use as an offset, even though
	 * probably that'll never actually come into play.
	 *
	 * @param allowCreate Allow ourselves to create a new texture, if needed
	 */
	public int reserveTexture(boolean allowCreate)
	{
		while (true)
		{
			for (int i=0; i<256; i++)
			{
				if (!this.usedTextures.get(this.cur_texture_page)[i])
				{
					this.useTexture(i);
					this.reserved_texture_count.set(this.cur_texture_page, this.reserved_texture_count.get(this.cur_texture_page) + 1);
					return i;
				}
			}

			// If we got here, we have no more textures available on
			// our current page.  Add a new one and recurse
			if (allowCreate)
			{
				this.addTextureMap();
			}
			else
			{
				return -1;
			}
		}
	}

	/**
	 * Returns the total number of unused textures.
	 */
	public int unusedTextureCount()
	{
		int count = 0;
		for (int i=0; i<256; i++)
		{
			if (!this.usedTextures.get(this.cur_texture_page)[i])
			{
				count++;
			}
		}
		count -= this.getFilenameTextureCount();
		count += this.reserved_texture_count.get(this.cur_texture_page);
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
			if (this.usedTextures.get(this.cur_texture_page)[i])
			{
				count++;
			}
		}
		count += this.getFilenameTextureCount();
		count -= this.reserved_texture_count.get(this.cur_texture_page);
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
			block.postNormalizeData();
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

		// If we've gotten here, add the collection to our added-collections list
		if (importData && !otherCollection.getFile().getName().equals("minecraft.yaml"))
		{
			this.loadedCollections.add(otherCollection);
		}
	}

	/**
	 * Gets our list of loaded collections
	 */
	public ArrayList<BlockTypeCollection> getLoadedCollections()
	{
		return this.loadedCollections;
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
	 * Returns a list of all custom texture files (complete sheets) that the
	 * collection is using.
	 */
	private ArrayList<String> getCustomTextureFileList()
	{
		HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
		ArrayList<String> list = new ArrayList<String>();

		// Loop through and grab them
		for (BlockType block : this.getBlocksFull())
		{
			String filename = block.getTexfile();
			if (filename != null && !tempMap.containsKey(filename))
			{
				tempMap.put(filename, null);
				list.add(filename);
			}
		}

		// Return
		return list;
	}

	/**
	 * Returns the count of all custom texture files (complete sheets) that the
	 * collection is using.
	 */
	private int getCustomTextureFileCount()
	{
		return this.getCustomTextureFileList().size();
	}

	/**
	 * Returns a list of all BlockTypes which use the specified texture file.
	 */
	private ArrayList<BlockType> getCustomTextureFileBlocks(String tex)
	{
		ArrayList<BlockType> list = new ArrayList<BlockType>();

		// Loop through and grab them
		for (BlockType block : this.getBlocksFull())
		{
			String filename = block.getTexfile();
			if (filename != null && filename.equalsIgnoreCase(tex))
			{
				list.add(block);
			}
		}

		// Return
		return list;
	}

	/**
	 * Imports any extra-sheet textures into our main texture
	 *
	 * TODO: Proper exception reporting
	 */
	public void importCustomTextureSheets()
		throws BlockTypeLoadException
	{
		int new_tex;
		if (this.getCustomTextureFileCount() > 0)
		{
			BufferedImage bi = this.textures.get(this.cur_texture_page);
			Graphics2D g2d = bi.createGraphics();
			for (String sheet : this.getCustomTextureFileList())
			{
				HashMap<Integer, Integer> seenTextures = new HashMap<Integer, Integer>();
				BufferedImage bi2 = MinecraftEnvironment.buildImageFromInput(MinecraftEnvironment.getMinecraftTexturepackData(sheet));
				int bi2_width = bi2.getWidth()/16;
				for (BlockType block : this.getCustomTextureFileBlocks(sheet))
				{
					ArrayList<Integer> blockTex = new ArrayList<Integer>();
					for (Integer tex : block.getUsedTextures())
					{
						if (!seenTextures.containsKey(tex))
						{
							seenTextures.put(tex, null);
							blockTex.add(tex);
						}
					}
					if (blockTex.size() > this.unusedTextureCount())
					{
						// This check is to make sure that all textures used by a particular block are
						// contained within the same texture sheet.
						// TODO: Ideally what we should do multiple passes and fill up the pages as much
						// as absolutely possible - using this method it's possible we'll be leaving
						// empty spots.
						bi = this.addTextureMap();
						g2d = bi.createGraphics();

						// Technically speaking this may cause the same texture to exist on both
						// sheets, if more than one block shares a texture
						blockTex = block.getUsedTextures();
						seenTextures.clear();
						for (int tex : blockTex)
						{
							seenTextures.put(tex, null);
						}
					}

					// Whatever's left in blockTex at this point should be copied+converted into
					// our texture
					for (int tex : blockTex)
					{
						seenTextures.put(tex, this.reserveTexture(false));
						if (seenTextures.get(tex) == -1)
						{
							throw new BlockTypeLoadException("Could not allocate additional textures");
						}
						int[] idx_coords = BlockType.getTexCoordsArr(seenTextures.get(tex));
						int[] old_coords = BlockType.getTexCoordsArr(tex);

						// Do the actual copying
						g2d.setComposite(AlphaComposite.Src);
						if (this.square_width < bi2_width)
						{
							g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
						}
						else
						{
							g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
							g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);			
						}
						g2d.drawImage(bi2,
								idx_coords[0]*this.square_width, idx_coords[1]*this.square_width,
								(idx_coords[0]+1)*this.square_width, (idx_coords[1]+1)*this.square_width,
								old_coords[0]*bi2_width, old_coords[1]*bi2_width, (old_coords[0]+1)*bi2_width, (old_coords[1]+1)*bi2_width,
								null);
					}

					// And now, lest we forget, convert all of the texture indexes in the
					// BlockType object
					block.convertTexIdx(seenTextures);

					// TODO: specify texture page on the blocktype!
				}
			}
		}
	}

	/**
	 * Loads any filename-based textures into our main texture
	 *
	 * TODO: Proper exception reporting
	 * TODO: It would be nice to have this process all blocks from the same "group"
	 *       at the same time, rather than potentially interleaved.  Probably not
	 *       that big of a deal, though.
	 */
	public void loadFilenameTextures()
		throws BlockTypeLoadException
	{
		HashMap<String, Integer> mapping = new HashMap<String, Integer>();
		ArrayList<String> texList;

		BufferedImage bi = this.textures.get(this.cur_texture_page);
		Graphics2D g2d = bi.createGraphics();

		for (BlockType block : this.getBlocksFull())
		{
			texList = block.getTextureFilenames();
			if (texList.size() > 0)
			{
				ArrayList<String> blockTex = new ArrayList<String>();
				for (String tex : texList)
				{
					if (!mapping.containsKey(tex))
					{
						mapping.put(tex, null);
						blockTex.add(tex);
					}
				}
				if (blockTex.size() > this.unusedTextureCount())
				{
					// See notes above in importCustomTextureSheets, re: some downsides to this
					bi = this.addTextureMap();
					g2d = bi.createGraphics();

					// Again, see notes above.
					blockTex = block.getTextureFilenames();
					mapping.clear();
					for (String tex : blockTex)
					{
						mapping.put(tex, null);
					}
				}

				// Anything left in blockTex at this point gets copied+converted
				for (String tex : blockTex)
				{
					mapping.put(tex, this.reserveTexture(false));
					if (mapping.get(tex) == -1)
					{
						throw new BlockTypeLoadException("Could not allocate additional textures");
					}
					int[] idx_coords = BlockType.getTexCoordsArr(mapping.get(tex));

					BufferedImage bi2 = MinecraftEnvironment.buildImageFromInput(MinecraftEnvironment.getMinecraftTexturepackData(tex));
					if (bi2 == null)
					{
						throw new BlockTypeLoadException("File " + tex + " is not found");
					}
					int new_width = bi2.getWidth();
					g2d.setComposite(AlphaComposite.Src);
					if (this.square_width < new_width)
					{
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					}
					else
					{
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
						g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);			
					}
					g2d.drawImage(bi2, idx_coords[0]*square_width, idx_coords[1]*square_width, square_width, square_width, null);
				}

				// Finally, update with our mapping
				block.setTextureFilenameMapping(mapping);

				// TODO: specify texture page on the blocktype!
			}
		}
	}
}
