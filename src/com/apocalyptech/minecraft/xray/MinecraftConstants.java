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

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.Integer;

import org.lwjgl.input.Keyboard;


/***
 * Precalcs and the like
 * @author Vincent
 */
public class MinecraftConstants {
    
    // translation table (precalc) from sprite sheet index to texture coordinates
	public static float[] precalcSpriteSheetToTextureX;
	public static float[] precalcSpriteSheetToTextureY;
	
	public static final float TEX16 = 1.0f/16.0f;
	public static final float TEX32 = 1.0f/32.0f;
	public static final float TEX64 = 1.0f/64.0f;
	public static final float TEX128 = 1.0f/128.0f;
	public static final float TEX256 = 1.0f/256.0f;
	public static final float TEX512 = 1.0f/512.0f;
	
	public static final int BLOCKSPERROW = 128;
	public static final int BLOCKSPERCOLUMN = BLOCKSPERROW * 16;
	    
    // sensitivity and speed mouse configuration
	public static final float MOUSE_SENSITIVITY 	= 0.05f; // mouse delta is multiplied by this
    public static float MOVEMENT_SPEED 		= 10.0f; // world units per second
    
    // the font to draw information to the screen
    public static final Font ARIALFONT = new Font("Arial", Font.BOLD, 14);
    public static final Font HEADERFONT = new Font("Arial", Font.BOLD, 26);
    public static final Font DETAILFONT = new Font("Arial", Font.PLAIN, 13);
    public static final Font DETAILVALUEFONT = new Font("Arial", Font.BOLD, 13);
	
    // some convenience statics regarding time calculation
	public static final long NANOSPERSECOND 	= 1000000000;
	public static final long MILLISPERSECOND 	= 1000;
	public static final long NANOSPERMILLIS 	= NANOSPERSECOND / MILLISPERSECOND;
	
	// Types of blocks
	public static enum BLOCK_TYPE {
		NORMAL,
		TORCH,
		DECORATION_CROSS,
		CROPS,
		LADDER,
		FLOOR,
		PRESSURE_PLATE,
		HALFHEIGHT,
		CAKE,
		BED,
		THINSLICE,
		DOOR,
		STAIRS,
		SIGNPOST,
		WALLSIGN,
		FENCE,
		FENCE_GATE,
		LEVER,
		BUTTON,
		PORTAL,
		MINECART_TRACKS,
		SIMPLE_RAIL,
		WATER,
		SEMISOLID,
		TRAPDOOR,
		PISTON_BODY,
		PISTON_HEAD,
		VINE,
		HUGE_MUSHROOM,
		SOLID_PANE
	}

	// Some block types' renderers automatically use other textures that we don't
	// specify manually.  Here are the offsets from the texture specified in the Yaml
	// file.
	public static HashMap<BLOCK_TYPE, Integer[]> blockTypeExtraTextures =
		new HashMap<BLOCK_TYPE, Integer[]>();
	static
	{
		blockTypeExtraTextures.put(BLOCK_TYPE.CROPS, new Integer[] {-1, -2, -3, -4, -5, -6, -7});
		blockTypeExtraTextures.put(BLOCK_TYPE.CAKE, new Integer[] {1, 2, 3});
		blockTypeExtraTextures.put(BLOCK_TYPE.BED, new Integer[] {-1, 14, 15, 16, 17});
		blockTypeExtraTextures.put(BLOCK_TYPE.DOOR, new Integer[] {-16});
		blockTypeExtraTextures.put(BLOCK_TYPE.MINECART_TRACKS, new Integer[] {-16});
		blockTypeExtraTextures.put(BLOCK_TYPE.SIMPLE_RAIL, new Integer[] {16}); // actually just for powered rails, but whatever
		blockTypeExtraTextures.put(BLOCK_TYPE.PISTON_BODY, new Integer[] {-2, -1, 1, 2});
		blockTypeExtraTextures.put(BLOCK_TYPE.PISTON_HEAD, new Integer[] {-1, 1, 2, 3});
	}

	// ... aand, because Huge Mushrooms are ridiculous, some hardcoded textures to reserve
	public static int TEX_HUGE_MUSHROOM_STEM = 13+(16*8);
	public static int TEX_HUGE_MUSHROOM_PORES = 14+(16*8);
	public static int[] blockTypeAbsoluteTextures = new int[] {
		TEX_HUGE_MUSHROOM_STEM,
		TEX_HUGE_MUSHROOM_PORES
	};

	// Our BLOCK structure is no longer an Enum, since we're reading it from a file
	public static BlockTypeCollection blockCollection = new BlockTypeCollection();

	// Just to omit one extra level of lookups, we'll also keep a reference to our
	// block collection's ID-based array
	public static BlockType[] blockArray;

	// There are a few blocks that we know we need references to.
	public static BlockType BLOCK_BEDROCK;
	public static BlockType BLOCK_GRASS;
	public static BlockType BLOCK_COBBLESTONE;
	public static BlockType BLOCK_PORTAL;
	public static BlockType BLOCK_TORCH;
	public static BlockType BLOCK_SAPLING;
	public static BlockType BLOCK_FIRE;
	public static BlockType BLOCK_WATER;
	public static BlockType BLOCK_STATIONARY_WATER;
	public static BlockType BLOCK_FENCE;
	public static BlockType BLOCK_FENCE_GATE;
	public static BlockType BLOCK_IRON_BARS;

	// A meta-block to use for unknown block types
	public static BlockType BLOCK_UNKNOWN;

	// Some data to save for grass
	public static HashMap<BlockType.DIRECTION_REL, Integer> grassDirectionMap;

	// Block types to compute decoration information for
	public static final BLOCK_TYPE[] DECORATION_BLOCKS = new BLOCK_TYPE[] {
		BLOCK_TYPE.LEVER, BLOCK_TYPE.TORCH, BLOCK_TYPE.DECORATION_CROSS
	};
	
	// HIGHLIGHT_ORES defines the kinds of blocks that we'll highlight.
	public static final String[] preferred_highlight_ores = new String[] {
		"CLAY", "PUMPKIN", "OBSIDIAN", "COAL_ORE", "IRON_ORE",
		"GOLD_ORE", "LAPIS_LAZULI_ORE", "DIAMOND_ORE", "REDSTONE_ORE", "MOB_SPAWNER"};

	// Keyboard actions
	public static enum KEY_ACTIONS {
		SPEED_INCREASE (Keyboard.KEY_LSHIFT),
		SPEED_DECREASE (Keyboard.KEY_RSHIFT),
		MOVE_FORWARD (Keyboard.KEY_W),
		MOVE_BACKWARD (Keyboard.KEY_S),
		MOVE_LEFT (Keyboard.KEY_A),
		MOVE_RIGHT (Keyboard.KEY_D),
		MOVE_UP (Keyboard.KEY_SPACE),
		MOVE_DOWN (Keyboard.KEY_LCONTROL),
		TOGGLE_MINIMAP (Keyboard.KEY_TAB),
		TOGGLE_ORE_1 (Keyboard.KEY_F1),
		TOGGLE_ORE_2 (Keyboard.KEY_F2),
		TOGGLE_ORE_3 (Keyboard.KEY_F3),
		TOGGLE_ORE_4 (Keyboard.KEY_F4),
		TOGGLE_ORE_5 (Keyboard.KEY_F5),
		TOGGLE_ORE_6 (Keyboard.KEY_F6),
		TOGGLE_ORE_7 (Keyboard.KEY_F7),
		TOGGLE_ORE_8 (Keyboard.KEY_F8),
		TOGGLE_ORE_9 (Keyboard.KEY_F9),
		TOGGLE_ORE_10 (Keyboard.KEY_F10),
		TOGGLE_FULLSCREEN (Keyboard.KEY_BACK),
		TOGGLE_FULLBRIGHT (Keyboard.KEY_F),
		TOGGLE_ORE_HIGHLIGHTING (Keyboard.KEY_H),
		TOGGLE_CAMERA_LOCK (Keyboard.KEY_L),
		MOVE_TO_SPAWN (Keyboard.KEY_HOME),
		MOVE_TO_PLAYER (Keyboard.KEY_END),
		MOVE_NEXT_CAMERAPOS (Keyboard.KEY_INSERT),
		MOVE_PREV_CAMERAPOS (Keyboard.KEY_DELETE),
		LIGHT_INCREASE (Keyboard.KEY_ADD),
		LIGHT_DECREASE (Keyboard.KEY_SUBTRACT),
		TOGGLE_POSITION_INFO (Keyboard.KEY_GRAVE),
		TOGGLE_RENDER_DETAILS (Keyboard.KEY_R),
		TOGGLE_BEDROCK (Keyboard.KEY_B),
		TOGGLE_ACCURATE_GRASS (Keyboard.KEY_G),
		TOGGLE_WATER (Keyboard.KEY_T),
		TOGGLE_HIGHLIGHT_EXPLORED (Keyboard.KEY_E),
		DIMENSION_NEXT (Keyboard.KEY_N),
		DIMENSION_PREV (Keyboard.KEY_P),
		CHUNK_RANGE_1 (Keyboard.KEY_NUMPAD1),
		CHUNK_RANGE_2 (Keyboard.KEY_NUMPAD2),
		CHUNK_RANGE_3 (Keyboard.KEY_NUMPAD3),
		CHUNK_RANGE_4 (Keyboard.KEY_NUMPAD4),
		CHUNK_RANGE_5 (Keyboard.KEY_NUMPAD5),
		CHUNK_RANGE_6 (Keyboard.KEY_NUMPAD6),
		HIGHLIGHT_RANGE_1 (Keyboard.KEY_1),
		HIGHLIGHT_RANGE_2 (Keyboard.KEY_2),
		HIGHLIGHT_RANGE_3 (Keyboard.KEY_3),
		HIGHLIGHT_RANGE_4 (Keyboard.KEY_4),
		HIGHLIGHT_RANGE_5 (Keyboard.KEY_5),
		HIGHLIGHT_RANGE_6 (Keyboard.KEY_6),
		HIGHLIGHT_RANGE_7 (Keyboard.KEY_7),
		RELEASE_MOUSE (Keyboard.KEY_ESCAPE),
		JUMP (Keyboard.KEY_J),
		RELOAD (Keyboard.KEY_EQUALS),
		QUIT (Keyboard.KEY_Q)
		;
		public final int def_key;
		KEY_ACTIONS(int def_key)
		{
			this.def_key = def_key;
		}
	}
	
	public static final Block[] SURROUNDINGBLOCKS = new Block[] { 
    	new Block(+1,0,0),
    	new Block(-1,0,0),
    	new Block(0,+1,0),
    	new Block(0,-1,0),
    	new Block(0,0,+1),
    	new Block(0,0,-1),
	};
	
	// Vars for painting information
	public static HashMap<String, PaintingInfo> paintings;
	public static PaintingInfo paintingback;
	
	static void initialize()
		throws BlockTypeLoadException
	{
		loadMainBlocks();
		initSpriteSheetToTextureTable();
		initPaintings();

		// For now...
		//BlockTypeCollection blockinfo = loadBlocks("blockdefs/aether.yaml");
		//blockCollection.importFrom(blockinfo, true);

		//TODO: Exceptions generated should really be saving our static state information (for later reporting)
		for (BlockTypeCollection coll : MinecraftEnvironment.getBlockTypeCollectionFiles())
		{
			String g = "user";
			if (coll.getGlobal())
			{
				g = "built-in";
			}
			if (coll.getException() == null)
			{
				// I think I will actually keep this the way it is, rather than create a GUI for
				// loading these.  As I noticed during the 1.8 prerelease stuff, as I had been
				// testing Aethermod things, people using mods might be fairly likely to shuffle
				// their minecraft.jar file around quite a bit.  If I had gone forward with my
				// previous plans, this would mean that whenever folks switched from, say, Aethermod
				// to 1.8 (assuming here that Aethermod might take a little while to get updated
				// to the 1.8 codebase), our stuff would disable Aether, and users would have to
				// continually be going into that dialog to re-enable stuff.
				//
				// If we just auto-load everything all the time, then it should hopefully error
				// out harmlessly on the ones that didn't load, and pick them up again once they
				// can.  Of course, this DOES open ourselves up to issues if two mods use the same
				// block ID, and a user is switching back and forth between them.  I feel okay
				// requiring the user to manually swap out some blockdef files in that case, though.
				try
				{
					// Do it without importing first.  If there are some obvious errors then we'd catch
					// them before potentially polluting our blockCollection with partial blockdef files
					blockCollection.importFrom(coll, false);
					blockCollection.importFrom(coll, true);
					System.out.println("Got " + g + " modinfo " + coll.getName() + " (" + coll.getFile().getName() + "), " + coll.usedTextureCount() + " textures.");
				}
				catch (BlockTypeLoadException e)
				{
					System.out.println("Error loading " + g + " modinfo at " + coll.getFile().getName() + ": " + e.toString());
				}
			}
			else
			{
				System.out.println("Error in " + g + " modinfo at " + coll.getFile().getName() + ": " + coll.getException().toString());
			}
		}
	}

	/**
	 * Reads in our default, base Minecraft texture data, and run a number of
	 * sanity checks on the data that we get.
	 *
	 * TODO: should this (or maybe just loadBlocks()) be in MinecraftEnvironment, maybe?
	 */
	public static void loadMainBlocks()
		throws BlockTypeLoadException
	{
		// First load the blocks
		BlockTypeCollection blockinfo = loadBlocks("blockdefs/minecraft.yaml", true);

		// Import into blockCollection
		blockCollection.importFrom(blockinfo, true);

		// A number of blocks that we require be present
		BLOCK_BEDROCK = blockCollection.getByName("BEDROCK");
		if (BLOCK_BEDROCK == null)
		{
			throw new BlockTypeLoadException("BEDROCK block definition not found");
		}
		BLOCK_GRASS = blockCollection.getByName("GRASS");
		if (BLOCK_GRASS == null)
		{
			throw new BlockTypeLoadException("GRASS block definition not found");
		}
		BLOCK_COBBLESTONE = blockCollection.getByName("COBBLESTONE");
		if (BLOCK_COBBLESTONE == null)
		{
			throw new BlockTypeLoadException("COBBLESTONE block definition not found");
		}
		BLOCK_PORTAL = blockCollection.getByName("PORTAL");
		if (BLOCK_PORTAL == null)
		{
			throw new BlockTypeLoadException("PORTAL block definition not found");
		}
		BLOCK_TORCH = blockCollection.getByName("TORCH");
		if (BLOCK_TORCH == null)
		{
			throw new BlockTypeLoadException("TORCH block definition not found");
		}
		BLOCK_SAPLING = blockCollection.getByName("SAPLING");
		if (BLOCK_SAPLING == null)
		{
			throw new BlockTypeLoadException("SAPLING block definition not found");
		}
		BLOCK_FIRE = blockCollection.getByName("FIRE");
		if (BLOCK_FIRE == null)
		{
			throw new BlockTypeLoadException("FIRE block definition not found");
		}
		BLOCK_WATER = blockCollection.getByName("WATER");
		if (BLOCK_WATER == null)
		{
			throw new BlockTypeLoadException("WATER block definition not found");
		}
		BLOCK_STATIONARY_WATER = blockCollection.getByName("STATIONARY_WATER");
		if (BLOCK_STATIONARY_WATER == null)
		{
			throw new BlockTypeLoadException("STATIONARY_WATER block definition not found");
		}
		BLOCK_FENCE = blockCollection.getByName("FENCE");
		if (BLOCK_FENCE == null)
		{
			throw new BlockTypeLoadException("FENCE block definition not found");
		}
		BLOCK_FENCE_GATE = blockCollection.getByName("FENCE_GATE");
		if (BLOCK_FENCE_GATE == null)
		{
			throw new BlockTypeLoadException("FENCE_GATE block definition not found");
		}
		BLOCK_IRON_BARS = blockCollection.getByName("IRON_BARS");
		if (BLOCK_IRON_BARS == null)
		{
			throw new BlockTypeLoadException("IRON_BARS block definition not found");
		}

		// We also define a "special" block for unknown block types, so that instead
		// of empty space, they'll show up as purple blocks.
		BLOCK_UNKNOWN = new BlockType();
		BLOCK_UNKNOWN.setIdStr("SPECIAL_UNKNOWN");
		BLOCK_UNKNOWN.setName("Internal Special Unknown Block");
		BLOCK_UNKNOWN.color = new Color(214, 127, 255);
		BLOCK_UNKNOWN.setType(BLOCK_TYPE.NORMAL);

		// For grass, in particular, for its rendering toggle, we'll save some info
		// that we can later tear out if need be.
		grassDirectionMap = BLOCK_GRASS.texture_dir_map;

		// Set our blockArray
		blockArray = blockCollection.blockArray;

		// Make sure we're reserving some hardcoded, absolute textures.
		for (int tex : blockTypeAbsoluteTextures)
		{
			blockCollection.useTexture(tex);
		}

		// Clean up.
		ExceptionDialog.clearExtraStatus();
	}

	/**
	 * Reads in block information from a YAML file.
	 * TODO: should probably go elsewhere
	 */
	public static BlockTypeCollection loadBlocks(String filename, boolean global)
		throws BlockTypeLoadException
	{
		ExceptionDialog.clearExtraStatus();
		ExceptionDialog.setExtraStatus1("Loading blocks from " + filename);

		// First load the actual YAML
		BlockTypeCollection blockinfo;
		try
		{
			blockinfo = BlockTypeCollection.loadFromYaml(filename, global);
		}
		catch (Exception e)
		{
			throw new BlockTypeLoadException("Could not load " + filename + ": " + e.toString(), e);
		}

		// Run through and normalize everything
		blockinfo.normalizeBlocks();

		// Return the blocks that we read
		return blockinfo;
	}
	
	/***
	 * Sprite sheet texture index to texture coordinates
	 */
	public static void initSpriteSheetToTextureTable() {
		precalcSpriteSheetToTextureX = new float[512];
		precalcSpriteSheetToTextureY = new float[512];
		for(int i=0;i<512;i++) {
			float texYy = ((int) (i / 16))/32.0f;
		    float texXx = ((int) (i % 16))/16.0f;
		    precalcSpriteSheetToTextureX[i] = texXx;
		    precalcSpriteSheetToTextureY[i] = texYy;
		}
	}
	
	/**
	 * Initialize our paintings.  Note that our hashmap exepects to be
	 * in lowercase.
	 */
	public static void initPaintings() {
		// I put these in here in the same order that they appeared at
		// http://www.minecraftwiki.net/wiki/Painting
		paintings = new HashMap<String, PaintingInfo>();
		
		paintings.put("kebab", new PaintingInfo(1, 1, 0, 0));
		paintings.put("aztec", new PaintingInfo(1, 1, 1, 0));
		paintings.put("alban", new PaintingInfo(1, 1, 2, 0));
		paintings.put("aztec2", new PaintingInfo(1, 1, 3, 0));
		paintings.put("bomb", new PaintingInfo(1, 1, 4, 0));
		paintings.put("plant", new PaintingInfo(1, 1, 5, 0));
		paintings.put("wasteland", new PaintingInfo(1, 1, 6, 0));
		paintings.put("wanderer", new PaintingInfo(1, 2, 0, 4));
		paintings.put("graham", new PaintingInfo(1, 2, 1, 4));
		paintings.put("pool", new PaintingInfo(2, 1, 0, 2));
		paintings.put("courbet", new PaintingInfo(2, 1, 2, 2));
		paintings.put("sunset", new PaintingInfo(2, 1, 6, 2));
		paintings.put("sea", new PaintingInfo(2, 1, 4, 2));
		paintings.put("creebet", new PaintingInfo(2, 1, 8, 2));
		paintings.put("match", new PaintingInfo(2, 2, 0, 8));
		paintings.put("bust", new PaintingInfo(2, 2, 2, 8));
		paintings.put("stage", new PaintingInfo(2, 2, 4, 8));
		paintings.put("void", new PaintingInfo(2, 2, 6, 8));
		paintings.put("skullandroses", new PaintingInfo(2, 2, 8, 8));
		paintings.put("fighters", new PaintingInfo(4, 2, 0, 6));
		paintings.put("skeleton", new PaintingInfo(4, 3, 12, 4));
		paintings.put("donkeykong", new PaintingInfo(4, 3, 12, 7));
		paintings.put("pointer", new PaintingInfo(4, 4, 0, 12));
		paintings.put("pigscene", new PaintingInfo(4, 4, 4, 12));
		paintings.put("burningskull", new PaintingInfo(4, 4, 8, 12));

		paintingback = new PaintingInfo(4, 4, 12, 0);
	}
}
