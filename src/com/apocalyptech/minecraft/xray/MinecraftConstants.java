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

import com.apocalyptech.minecraft.xray.dialog.ExceptionDialog;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.Integer;
import java.io.InputStream;
import java.io.IOException;

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
    public static final Font SMALLFONT = new Font("Arial", Font.PLAIN, 10);
	
    // some convenience statics regarding time calculation
	public static final long NANOSPERSECOND 	= 1000000000;
	public static final long MILLISPERSECOND 	= 1000;
	public static final long NANOSPERMILLIS 	= NANOSPERSECOND / MILLISPERSECOND;
	
	// Types of blocks
	public static enum BLOCK_TYPE {
		NORMAL,
		TORCH,
		DECORATION_CROSS,
		DECORATION_GRID,
		LADDER,
		FLOOR,
		PRESSURE_PLATE,
		HALFHEIGHT,
		CAKE,
		BED,
		SNOW,
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
		GLASS,
		TRAPDOOR,
		PISTON_BODY,
		PISTON_HEAD,
		VINE,
		HUGE_MUSHROOM,
		SOLID_PANE,
		CHEST,
		STEM,
		END_PORTAL,
		END_PORTAL_FRAME,
		CAULDRON,
		ENCHANTMENT_TABLE,
		BREWING_STAND,
		DRAGON_EGG
	}

	// Extra textures specified by various block types
	public static HashMap<BLOCK_TYPE, String[]> blockTypeExtraTexturesReq =
		new HashMap<BLOCK_TYPE, String[]>();
	static
	{
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.DOOR, new String[] {"bottom"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.MINECART_TRACKS, new String[] {"curve"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.SIMPLE_RAIL, new String[] {"powered"}); // actually just for powered rails, but whatever
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.CAKE, new String[] {"side_uncut", "side_cut", "bottom"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.BED, new String[] {"foot_top", "head_side", "foot_side", "foot", "head"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.PISTON_BODY, new String[] {"head", "back", "front"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.PISTON_HEAD, new String[] {"head_sticky", "body"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.CHEST, new String[] {"side_small", "top",
			"front_big_left", "front_big_right",
			"back_big_left", "back_big_right"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.HUGE_MUSHROOM, new String[] {"stem", "pores"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.STEM, new String[] {"curve"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.CAULDRON, new String[] {"inside", "top"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.ENCHANTMENT_TABLE, new String[] {"sides", "bottom"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.BREWING_STAND, new String[] {"base"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.LEVER, new String[] {"base"});
		blockTypeExtraTexturesReq.put(BLOCK_TYPE.END_PORTAL_FRAME, new String[] {"sides", "bottom", "eye"});
	}

	// Our BLOCK structure is no longer an Enum, since we're reading it from a file
	public static BlockTypeCollection blockCollection = new BlockTypeCollection();

	// Just to omit one extra level of lookups, we'll also keep a reference to our
	// block collection's ID-based array
	public static BlockType[] blockArray;

	// There are a few blocks that we know we need references to.
	public static BlockType BLOCK_BEDROCK;
	public static BlockType BLOCK_GRASS;
	public static BlockType BLOCK_PORTAL;
	public static BlockType BLOCK_END_PORTAL;
	public static BlockType BLOCK_SAPLING;
	public static BlockType BLOCK_FIRE;
	public static BlockType BLOCK_WATER;
	public static BlockType BLOCK_STATIONARY_WATER;
	public static BlockType BLOCK_IRON_BARS;
	public static BlockType BLOCK_PISTON_HEAD;
	public static BlockType BLOCK_PISTON_STICKY_BODY;
	public static BlockType BLOCK_SILVERFISH;

	// A HashMap to define blocks that the "explored" highlight will use
	public static HashMap<Short, Boolean> exploredBlocks;

	// A meta-block to use for unknown block types
	public static BlockType BLOCK_UNKNOWN;

	// Some data to save for grass
	public static HashMap<BlockType.DIRECTION_REL, Integer> grassDirectionMap;

	// Block types to compute decoration information for
	public static final BLOCK_TYPE[] DECORATION_BLOCKS = new BLOCK_TYPE[] {
		BLOCK_TYPE.LEVER, BLOCK_TYPE.TORCH, BLOCK_TYPE.DECORATION_CROSS,
		BLOCK_TYPE.DECORATION_GRID, BLOCK_TYPE.STEM,
		BLOCK_TYPE.END_PORTAL_FRAME, BLOCK_TYPE.BREWING_STAND
	};
	
	// HIGHLIGHT_ORES defines the kinds of blocks that we'll highlight.
	public static final String[] preferred_highlight_ores = new String[] {
		"CLAY", "PUMPKIN", "OBSIDIAN", "COAL_ORE", "IRON_ORE",
		"GOLD_ORE", "LAPIS_LAZULI_ORE", "DIAMOND_ORE", "REDSTONE_ORE", "MOB_SPAWNER"};

	// Keyboard action categories
	public static enum ACTION_CAT {
		MOVEMENT ("Movement"),
		CAMERA ("Camera"),
		RENDERING ("Rendering"),
		OTHER ("Other")
		;
		public String title;
		ACTION_CAT(String title)
		{
			this.title = title;
		}
	}

	// Keyboard actions
	public static enum KEY_ACTION {
		MOVE_FORWARD   (ACTION_CAT.MOVEMENT, Keyboard.KEY_W, "Move Forward"),
		MOVE_BACKWARD  (ACTION_CAT.MOVEMENT, Keyboard.KEY_S, "Move Backward"),
		MOVE_LEFT      (ACTION_CAT.MOVEMENT, Keyboard.KEY_A, "Strafe Left"),
		MOVE_RIGHT     (ACTION_CAT.MOVEMENT, Keyboard.KEY_D, "Strafe Right"),
		MOVE_UP        (ACTION_CAT.MOVEMENT, Keyboard.KEY_SPACE, "Fly Up"),
		MOVE_DOWN      (ACTION_CAT.MOVEMENT, Keyboard.KEY_LCONTROL, "Fly Down"),
		SPEED_INCREASE (ACTION_CAT.MOVEMENT, Keyboard.KEY_LSHIFT, "Move Faster"),
		SPEED_DECREASE (ACTION_CAT.MOVEMENT, Keyboard.KEY_RSHIFT, "Move Slower"),

		MOVE_TO_SPAWN       (ACTION_CAT.CAMERA, Keyboard.KEY_HOME, "Jump to Spawnpoint"),
		MOVE_TO_PLAYER      (ACTION_CAT.CAMERA, Keyboard.KEY_END, "Jump to Player Position"),
		MOVE_NEXT_CAMERAPOS (ACTION_CAT.CAMERA, Keyboard.KEY_INSERT, "Jump to Next Camera Preset"),
		MOVE_PREV_CAMERAPOS (ACTION_CAT.CAMERA, Keyboard.KEY_DELETE, "Jump to Previous Camera Preset"),
		JUMP                (ACTION_CAT.CAMERA, Keyboard.KEY_J, "Jump to Abritrary Position"),
		JUMP_NEAREST        (ACTION_CAT.CAMERA, Keyboard.KEY_MINUS, "Jump to nearest existing chunk"),
		DIMENSION_NEXT      (ACTION_CAT.CAMERA, Keyboard.KEY_N, "Jump to Next Dimension"),
		DIMENSION_PREV      (ACTION_CAT.CAMERA, Keyboard.KEY_P, "Jump to Previous Dimension"),
		TOGGLE_CAMERA_LOCK  (ACTION_CAT.CAMERA, Keyboard.KEY_L, "Lock Camera to Vertical Axis"),

		TOGGLE_ORE_1              (ACTION_CAT.RENDERING, Keyboard.KEY_F1, "Toggle Ore #1"),
		TOGGLE_ORE_2              (ACTION_CAT.RENDERING, Keyboard.KEY_F2, "Toggle Ore #2"),
		TOGGLE_ORE_3              (ACTION_CAT.RENDERING, Keyboard.KEY_F3, "Toggle Ore #3"),
		TOGGLE_ORE_4              (ACTION_CAT.RENDERING, Keyboard.KEY_F4, "Toggle Ore #4"),
		TOGGLE_ORE_5              (ACTION_CAT.RENDERING, Keyboard.KEY_F5, "Toggle Ore #5"),
		TOGGLE_ORE_6              (ACTION_CAT.RENDERING, Keyboard.KEY_F6, "Toggle Ore #6"),
		TOGGLE_ORE_7              (ACTION_CAT.RENDERING, Keyboard.KEY_F7, "Toggle Ore #7"),
		TOGGLE_ORE_8              (ACTION_CAT.RENDERING, Keyboard.KEY_F8, "Toggle Ore #8"),
		TOGGLE_ORE_9              (ACTION_CAT.RENDERING, Keyboard.KEY_F9, "Toggle Ore #9"),
		TOGGLE_ORE_10             (ACTION_CAT.RENDERING, Keyboard.KEY_F10, "Toggle Ore #10"),
		TOGGLE_ORE_HIGHLIGHTING   (ACTION_CAT.RENDERING, Keyboard.KEY_H, "Toggle Ore Highlight Glow"),
		HIGHLIGHT_RANGE_1         (ACTION_CAT.RENDERING, Keyboard.KEY_1, "Ore Highlight Distance 1"),
		HIGHLIGHT_RANGE_2         (ACTION_CAT.RENDERING, Keyboard.KEY_2, "Ore Highlight Distance 2"),
		HIGHLIGHT_RANGE_3         (ACTION_CAT.RENDERING, Keyboard.KEY_3, "Ore Highlight Distance 3"),
		HIGHLIGHT_RANGE_4         (ACTION_CAT.RENDERING, Keyboard.KEY_4, "Ore Highlight Distance 4"),
		HIGHLIGHT_RANGE_5         (ACTION_CAT.RENDERING, Keyboard.KEY_5, "Ore Highlight Distance 5"),
		HIGHLIGHT_RANGE_6         (ACTION_CAT.RENDERING, Keyboard.KEY_6, "Ore Highlight Distance 6"),
		HIGHLIGHT_RANGE_7         (ACTION_CAT.RENDERING, Keyboard.KEY_7, "Ore Highlight Distance 7"),
		TOGGLE_FULLBRIGHT         (ACTION_CAT.RENDERING, Keyboard.KEY_F, "Toggle Fullbright"),
		TOGGLE_BEDROCK            (ACTION_CAT.RENDERING, Keyboard.KEY_B, "Toggle Bedrock"),
		TOGGLE_WATER              (ACTION_CAT.RENDERING, Keyboard.KEY_T, "Toggle Water"),
		LIGHT_INCREASE            (ACTION_CAT.RENDERING, Keyboard.KEY_ADD, "Increase Lighting Range"),
		LIGHT_DECREASE            (ACTION_CAT.RENDERING, Keyboard.KEY_SUBTRACT, "Decrease Lighting Range"),
		CHUNK_RANGE_1             (ACTION_CAT.RENDERING, Keyboard.KEY_NUMPAD1, "Visibility Range 1"),
		CHUNK_RANGE_2             (ACTION_CAT.RENDERING, Keyboard.KEY_NUMPAD2, "Visibility Range 2"),
		CHUNK_RANGE_3             (ACTION_CAT.RENDERING, Keyboard.KEY_NUMPAD3, "Visibility Range 3"),
		CHUNK_RANGE_4             (ACTION_CAT.RENDERING, Keyboard.KEY_NUMPAD4, "Visibility Range 4"),
		CHUNK_RANGE_5             (ACTION_CAT.RENDERING, Keyboard.KEY_NUMPAD5, "Visibility Range 5"),
		CHUNK_RANGE_6             (ACTION_CAT.RENDERING, Keyboard.KEY_NUMPAD6, "Visibility Range 6"),
		TOGGLE_HIGHLIGHT_EXPLORED (ACTION_CAT.RENDERING, Keyboard.KEY_E, "Toggle 'Explored' Areas"),
		TOGGLE_ACCURATE_GRASS     (ACTION_CAT.RENDERING, Keyboard.KEY_G, "Toggle Accurate Grass"),
		TOGGLE_BETA19_FENCES      (ACTION_CAT.RENDERING, Keyboard.KEY_C, "Toggle Beta 1.9 Fences"),
		TOGGLE_SILVERFISH         (ACTION_CAT.RENDERING, Keyboard.KEY_V, "Toggle Silverfish Highlighting"),
		TOGGLE_CHUNK_BORDERS      (ACTION_CAT.RENDERING, Keyboard.KEY_U, "Toggle Chunk Borders"),
		TOGGLE_SLIME_CHUNKS       (ACTION_CAT.RENDERING, Keyboard.KEY_M, "Toggle Slime Chunk Highlighting"),

		TOGGLE_FULLSCREEN     (ACTION_CAT.OTHER, Keyboard.KEY_BACK, "Toggle Fullscreen"),
		TOGGLE_POSITION_INFO  (ACTION_CAT.OTHER, Keyboard.KEY_GRAVE, "Toggle Level Info"),
		TOGGLE_RENDER_DETAILS (ACTION_CAT.OTHER, Keyboard.KEY_R, "Toggle Rendering Info"),
		OPEN_NEW_MAP          (ACTION_CAT.OTHER, Keyboard.KEY_O, "Open New Map"),
		RELOAD                (ACTION_CAT.OTHER, Keyboard.KEY_EQUALS, "Reload Map from Disk"),
		TOGGLE_MINIMAP        (ACTION_CAT.OTHER, Keyboard.KEY_TAB, "Toggle Minimap"),
		RELEASE_MOUSE         (ACTION_CAT.OTHER, Keyboard.KEY_ESCAPE, "Release Mouse"),
		KEY_HELP              (ACTION_CAT.OTHER, Keyboard.KEY_Y, "Show Keyboard Reference"),
		SET_ORE_BINDS         (ACTION_CAT.OTHER, Keyboard.KEY_RBRACKET, "Set Ore Highlight Binds"),
		QUIT                  (ACTION_CAT.OTHER, Keyboard.KEY_Q, "Quit")
		;
		public final ACTION_CAT category;
		public final int def_key;
		public final String desc;
		KEY_ACTION(ACTION_CAT category, int def_key, String desc)
		{
			this.category = category;
			this.def_key = def_key;
			this.desc = desc;
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
					XRay.logger.debug("Got " + g + " blockdef " + coll.getName() + " (" + coll.getFile().getName() + "), " + coll.usedTextureCount() + " sheet textures, " + coll.getFilenameTextureCount() + " file textures");
				}
				catch (BlockTypeLoadException e)
				{
					XRay.logger.info("Error loading " + g + " blockdef at " + coll.getFile().getName() + ": " + e.toString());
				}
			}
			else
			{
				XRay.logger.info("Error in " + g + " blockdef at " + coll.getFile().getName() + ": " + coll.getException().toString());
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
		BLOCK_PORTAL = blockCollection.getByName("PORTAL");
		if (BLOCK_PORTAL == null)
		{
			throw new BlockTypeLoadException("PORTAL block definition not found");
		}
		BLOCK_END_PORTAL = blockCollection.getByName("END_PORTAL");
		if (BLOCK_END_PORTAL == null)
		{
			throw new BlockTypeLoadException("END_PORTAL block definition not found");
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
		BLOCK_IRON_BARS = blockCollection.getByName("IRON_BARS");
		if (BLOCK_IRON_BARS == null)
		{
			throw new BlockTypeLoadException("IRON_BARS block definition not found");
		}
		BLOCK_PISTON_HEAD = blockCollection.getByName("PISTON_HEAD");
		if (BLOCK_PISTON_HEAD == null)
		{
			throw new BlockTypeLoadException("PISTON_HEAD block definition not found");
		}
		BLOCK_PISTON_STICKY_BODY = blockCollection.getByName("PISTON_STICKY_BODY");
		if (BLOCK_PISTON_STICKY_BODY == null)
		{
			throw new BlockTypeLoadException("PISTON_STICKY_BODY block definition not found");
		}
		BLOCK_SILVERFISH = blockCollection.getByName("SILVERFISH");
		if (BLOCK_SILVERFISH == null)
		{
			throw new BlockTypeLoadException("SILVERFISH block definition not found");
		}
		if (BLOCK_SILVERFISH.texture_data_map == null || !BLOCK_SILVERFISH.texture_data_map.containsKey((byte)0))
		{
			throw new BlockTypeLoadException("SILVERFISH block definition must include at least one data value of 0");
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

		// Check to make sure our texfile exists, if it's defined
		if (blockinfo.getTexfile() != null)
		{
			try
			{
				InputStream stream = MinecraftEnvironment.getMinecraftTexturepackData(blockinfo.getTexfile());
				if (stream == null)
				{
					throw new BlockTypeLoadException("File " + blockinfo.getTexfile() + " is not found");
				}
				stream.close();
			}
			catch (IOException e)
			{
				throw new BlockTypeLoadException("Error while opening " + blockinfo.getTexfile() + ": " + e.toString(), e);
			}
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

	/**
	 * Get the text of the key that we want to display to the user
	 * @return The text to display for the key
	 */
	public static String getKeyEnglish(KEY_ACTION action, int bound_key)
	{
		if (Keyboard.getKeyName(bound_key).equals("GRAVE"))
		{
			return "`";
		}
		else if (Keyboard.getKeyName(bound_key).equals("SYSRQ"))
		{
			return "PRINTSCREEN";
		}
		else if (Keyboard.getKeyName(bound_key).equals("MINUS"))
		{
			return "-";
		}
		else
		{
			return Keyboard.getKeyName(bound_key);
		}
	}

	/**
	 * Get any text which should be displayed after the key
	 * @return Text
	 */
	public static String getKeyExtraAfter(KEY_ACTION action, int bound_key)
	{
		switch (action)
		{
			case SPEED_INCREASE:
				return " / Left Mouse Button (hold)";

			case SPEED_DECREASE:
				return " / Right Mouse Button (hold)";

			default:
				String key_name = Keyboard.getKeyName(bound_key);
				if (key_name.startsWith("NUMPAD") || key_name.equals("DECIMAL"))
				{
					return " (numlock must be on)";
				}
				else if (key_name.equals("DIVIDE") || key_name.equals("MULTIPLY") ||
						key_name.equals("SUBTRACT") || key_name.equals("ADD"))
				{
					return " (on numeric keypad)";
				}
				else if (key_name.equals("GRAVE"))
				{
					return " (grave accent)";
				}
				else if (key_name.equals("SYSRQ"))
				{
					return " (also called SYSRQ)";
				}
				else if (key_name.equals("MINUS"))
				{
					return " (minus, dash)";
				}
				break;
		}
		return "";
	}

	/**
	 * Get any text which should be displayed before the key
	 * @return Text
	 */
	public static String getKeyExtraBefore(KEY_ACTION action, int bound_key)
	{
		switch (action)
		{
			case QUIT:
				return "CTRL-";
		}
		return "";
	}

	/**
	 * Returns a full text description of the given key
	 */
	public static String getKeyFullText(KEY_ACTION action, int bound_key)
	{
		return getKeyExtraBefore(action, bound_key) +
			getKeyEnglish(action, bound_key) +
			getKeyExtraAfter(action, bound_key);
	}
}
