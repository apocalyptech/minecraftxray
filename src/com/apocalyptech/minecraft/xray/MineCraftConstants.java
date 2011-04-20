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
import java.util.HashMap;

import org.lwjgl.input.Keyboard;

/***
 * Precalcs and the like
 * @author Vincent
 */
public class MineCraftConstants {
	 // translation table for colors for the minimap
    public static Color[] blockColors;
    
    // translation table from block data to sprite sheet index
    public static int[] blockDataToSpriteSheet;
    
    // translation table (precalc) from sprite sheet index to texture coordinates
	public static float[] precalcSpriteSheetToTextureX;
	public static float[] precalcSpriteSheetToTextureY;
	
	public static final float TEX16 = 1.0f/16.0f;
	public static final float TEX32 = 1.0f/32.0f;
	public static final float TEX64 = 1.0f/64.0f;
	public static final float TEX128 = 1.0f/128.0f;
	public static final float TEX256 = 1.0f/256.0f;
	
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
		BED,
		THINSLICE,
		DOOR,
		STAIRS,
		SIGNPOST,
		WALLSIGN,
		FENCE,
		LEVER,
		BUTTON,
		PORTAL,
		MINECART_TRACKS,
		SIMPLE_RAIL,
		WATER,
		LAVA,
		FIRE,
		SEMISOLID
	}
	
	// This HashMap determines how we draw various block types
	public static HashMap<Byte, BLOCK_TYPE> BLOCK_TYPE_MAP = new HashMap<Byte, BLOCK_TYPE>();
	
	// This HashMap is used to determine which texture to use for blocks whose data value determines
	// what texture to use
	public static HashMap<Byte, HashMap<Byte, Integer>> blockDataSpriteSheetMap = new HashMap<Byte, HashMap<Byte, Integer>>();
	
	public static enum BLOCK {
		STONE (1, "Stone"),
		GRASS (2, "Grass"),
		DIRT (3, "Dirt"),
		COBBLESTONE (4, "Cobbles"),
		PLANK (5, "Plank"),
		SAPLING (6, "Sapling"),
		BEDROCK (7, "Bedrock"),
		WATER (8, "Water"),
		STATIONARY_WATER (9, "Water"),
		LAVA (10, "Lava"),
		STATIONARY_LAVA (11, "Lava"),
		SAND (12, "Sand"),
		GRAVEL (13, "Gravel"),
		GOLD_ORE (14, "Gold"),
		IRON_ORE (15, "Iron"),
		COAL_ORE (16, "Coal"),
		WOOD (17, "Wood"),
		LEAVES (18, "Leaves"),
		SPONGE (19, "Sponge"),
		GLASS (20, "Glass"),
		LAPIS_LAZULI_ORE (21, "Lapis"),
		LAPIS_LAZULI_BLOCK (22, "Lapis"),
		DISPENSER (23, "Dispenser"),
		SANDSTONE (24, "Sandstone"),
		NOTE_BLOCK (25, "Note"),
		BED (26, "Bed"),
		POWERED_RAIL (27, "Power Rail"),
		DETECTOR_RAIL (28, "Detector"),
		WOOL (35, "Wool"),
		YELLOW_FLOWER (37, "Flower"),
		RED_ROSE (38, "Rose"),
		BROWN_MUSHROOM (39, "Mushroom"),
		RED_MUSHROOM (40, "Mushroom"),
		GOLD_BLOCK (41, "Gold"),
		IRON_BLOCK (42, "Iron"),
		DOUBLE_SLAB (43, "Slab"),
		SLAB (44, "Slab"),
		BRICK (45, "Brick"),
		TNT (46, "TNT"),
		BOOKSHELF (47, "Bookshelf"),
		MOSSY_COBBLESTONE (48, "Moss"),
		OBSIDIAN (49, "Obsidian"),
		TORCH (50, "Torch"),
		FIRE (51, "Fire"),
		MOB_SPAWNER (52, "Spawner"),
		WOODEN_STAIRS (53, "Stairs"),
		CHEST (54, "Chest"),
		REDSTONE_WIRE (55, "Wire"),
		DIAMOND_ORE (56, "Diamond"),
		DIAMOND_BLOCK (57, "Diamond"),
		WORKBENCH (58, "Bench"),
		CROPS (59, "Crops"),
		FARMLAND (60, "Farmland"),
		FURNACE (61, "Furnace"),
		BURNING_FURNACE (62, "Furnace"),
		SIGNPOST (63, "Sign"),
		WOODEN_DOOR (64, "Door"),
		LADDER (65, "Ladder"),
		MINECART_TRACKS (66, "Tracks"),
		COBBLESTONE_STAIRS (67, "Stairs"),
		WALL_SIGN (68, "Sign"),
		LEVER (69, "Lever"),
		STONE_PRESSURE_PLATE (70, "Plate"),
		IRON_DOOR (71, "Door"),
		WOODEN_PRESSURE_PLATE (72, "Plate"),
		REDSTONE_ORE (73, "Redstone"),
		GLOWING_REDSTONE_ORE (74, "Redstone"),
		REDSTONE_TORCH_OFF (75, "Torch"),
		REDSTONE_TORCH_ON (76, "Torch"),
		STONE_BUTTON (77, "Button"),
		SNOW (78, "Snow"),
		ICE (79, "Ice"),
		SNOW_BLOCK (80, "Snow"),
		CACTUS (81, "Cactus"),
		CLAY (82, "Clay"),
		SUGARCANE (83, "Sugarcane"),
		JUKEBOX (84, "Jukebox"),
		FENCE (85, "Fence"),
		PUMPKIN (86, "Pumpkin"),
		NETHERRACK (87, "Netherrack"),
		SOUL_SAND (88, "Soul Sand"),
		GLOWSTONE (89, "Glowstone"),
		PORTAL (90, "Portal"),
		JACK_O_LANTERN (91, "Jack"),
		CAKE (92, "Cake"),
		REDSTONE_REPEATER_OFF (93, "Redstone Repeater (off)"),
		REDSTONE_REPEATER_ON (94, "Redstone Repeater (on)")
		;
		public final int id;
		public final String name;
		BLOCK(int id, String name)
		{
			this.id = id;
			this.name = name;
		}
	}

	// Block types to compute decoration information for
	public static final BLOCK[] DECORATION_BLOCKS = new BLOCK[] {
		BLOCK.TORCH, BLOCK.REDSTONE_TORCH_ON, BLOCK.REDSTONE_TORCH_OFF,
		BLOCK.RED_ROSE, BLOCK.YELLOW_FLOWER,
		BLOCK.BROWN_MUSHROOM, BLOCK.RED_MUSHROOM,
		BLOCK.LEVER, BLOCK.SAPLING, BLOCK.SUGARCANE
	};
	
	// HIGHLIGHT_ORES defines the kinds of blocks that we'll highlight.
	public static final BLOCK[] preferred_highlight_ores = new BLOCK[] {
		BLOCK.CLAY, BLOCK.PUMPKIN, BLOCK.OBSIDIAN, BLOCK.COAL_ORE, BLOCK.IRON_ORE,
		BLOCK.GOLD_ORE, BLOCK.LAPIS_LAZULI_ORE, BLOCK.DIAMOND_ORE, BLOCK.REDSTONE_ORE, BLOCK.MOB_SPAWNER};

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
		TOGGLE_WATER (Keyboard.KEY_T),
		TOGGLE_HIGHLIGHT_EXPLORED (Keyboard.KEY_E),
		SWITCH_NETHER (Keyboard.KEY_N),
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
	
	static {
		initBlockColorsTable();
		initBlockDatatoSpriteSheetIndexTable();
		initSpriteSheetToTextureTable();
		initBlockTypes();
		initPaintings();
	}
	
	/***
	 * Initializes our BLOCK_TYPE_MAP HashMap, which determines how we render various blocks
	 */
	public static void initBlockTypes() {
		// First seed the array with "normal" so we don't have to catch NullPointerExceptions
		int i;
		for (i=Byte.MIN_VALUE; i<=Byte.MAX_VALUE; i++)
		{
			BLOCK_TYPE_MAP.put((byte)i, BLOCK_TYPE.NORMAL);
		}
		
		// Now our actual type values...
		
		// Torches
		BLOCK_TYPE_MAP.put((byte)BLOCK.TORCH.id, BLOCK_TYPE.TORCH);
		BLOCK_TYPE_MAP.put((byte)BLOCK.REDSTONE_TORCH_ON.id, BLOCK_TYPE.TORCH);
		BLOCK_TYPE_MAP.put((byte)BLOCK.REDSTONE_TORCH_OFF.id, BLOCK_TYPE.TORCH);
		
		// Small decoration blocks
		BLOCK_TYPE_MAP.put((byte)BLOCK.RED_MUSHROOM.id, BLOCK_TYPE.DECORATION_CROSS);
		BLOCK_TYPE_MAP.put((byte)BLOCK.BROWN_MUSHROOM.id, BLOCK_TYPE.DECORATION_CROSS);
		BLOCK_TYPE_MAP.put((byte)BLOCK.RED_ROSE.id, BLOCK_TYPE.DECORATION_CROSS);
		BLOCK_TYPE_MAP.put((byte)BLOCK.YELLOW_FLOWER.id, BLOCK_TYPE.DECORATION_CROSS);
		BLOCK_TYPE_MAP.put((byte)BLOCK.SAPLING.id, BLOCK_TYPE.DECORATION_CROSS);
		BLOCK_TYPE_MAP.put((byte)BLOCK.SUGARCANE.id, BLOCK_TYPE.DECORATION_CROSS);
		
		// Crops
		BLOCK_TYPE_MAP.put((byte)BLOCK.CROPS.id, BLOCK_TYPE.CROPS);
		
		// Ladders
		BLOCK_TYPE_MAP.put((byte)BLOCK.LADDER.id, BLOCK_TYPE.LADDER);
		
		// "floor" decorations
		BLOCK_TYPE_MAP.put((byte)BLOCK.REDSTONE_WIRE.id, BLOCK_TYPE.FLOOR);
		
		// Minecart tracks
		BLOCK_TYPE_MAP.put((byte)BLOCK.MINECART_TRACKS.id, BLOCK_TYPE.MINECART_TRACKS);

		// "Simple" rails
		BLOCK_TYPE_MAP.put((byte)BLOCK.POWERED_RAIL.id, BLOCK_TYPE.SIMPLE_RAIL);
		BLOCK_TYPE_MAP.put((byte)BLOCK.DETECTOR_RAIL.id, BLOCK_TYPE.SIMPLE_RAIL);
		
		// Pressure plates
		BLOCK_TYPE_MAP.put((byte)BLOCK.STONE_PRESSURE_PLATE.id, BLOCK_TYPE.PRESSURE_PLATE);
		BLOCK_TYPE_MAP.put((byte)BLOCK.WOODEN_PRESSURE_PLATE.id, BLOCK_TYPE.PRESSURE_PLATE);
		BLOCK_TYPE_MAP.put((byte)BLOCK.REDSTONE_REPEATER_ON.id, BLOCK_TYPE.PRESSURE_PLATE);
		BLOCK_TYPE_MAP.put((byte)BLOCK.REDSTONE_REPEATER_OFF.id, BLOCK_TYPE.PRESSURE_PLATE);
		
		// Half-height blocks
		BLOCK_TYPE_MAP.put((byte)BLOCK.SLAB.id, BLOCK_TYPE.HALFHEIGHT);
		BLOCK_TYPE_MAP.put((byte)BLOCK.CAKE.id, BLOCK_TYPE.HALFHEIGHT);

		// Bed blocks
		BLOCK_TYPE_MAP.put((byte)BLOCK.BED.id, BLOCK_TYPE.BED);
		
		// Doors
		BLOCK_TYPE_MAP.put((byte)BLOCK.WOODEN_DOOR.id, BLOCK_TYPE.DOOR);
		BLOCK_TYPE_MAP.put((byte)BLOCK.IRON_DOOR.id, BLOCK_TYPE.DOOR);
		
		// Stairs
		BLOCK_TYPE_MAP.put((byte)BLOCK.WOODEN_STAIRS.id, BLOCK_TYPE.STAIRS);
		BLOCK_TYPE_MAP.put((byte)BLOCK.COBBLESTONE_STAIRS.id, BLOCK_TYPE.STAIRS);
		
		// Signs
		BLOCK_TYPE_MAP.put((byte)BLOCK.SIGNPOST.id, BLOCK_TYPE.SIGNPOST);
		BLOCK_TYPE_MAP.put((byte)BLOCK.WALL_SIGN.id, BLOCK_TYPE.WALLSIGN);
		
		// Fences
		BLOCK_TYPE_MAP.put((byte)BLOCK.FENCE.id, BLOCK_TYPE.FENCE);
		
		// Lever
		BLOCK_TYPE_MAP.put((byte)BLOCK.LEVER.id, BLOCK_TYPE.LEVER);
		
		// Button
		BLOCK_TYPE_MAP.put((byte)BLOCK.STONE_BUTTON.id, BLOCK_TYPE.BUTTON);
		
		// Portal
		BLOCK_TYPE_MAP.put((byte)BLOCK.PORTAL.id, BLOCK_TYPE.PORTAL);
		
		// Water
		BLOCK_TYPE_MAP.put((byte)BLOCK.WATER.id, BLOCK_TYPE.WATER);
		BLOCK_TYPE_MAP.put((byte)BLOCK.STATIONARY_WATER.id, BLOCK_TYPE.WATER);
		BLOCK_TYPE_MAP.put((byte)BLOCK.ICE.id, BLOCK_TYPE.WATER);
		
		// Lava
		BLOCK_TYPE_MAP.put((byte)BLOCK.LAVA.id, BLOCK_TYPE.LAVA);
		BLOCK_TYPE_MAP.put((byte)BLOCK.STATIONARY_LAVA.id, BLOCK_TYPE.LAVA);
		
		// Fire
		BLOCK_TYPE_MAP.put((byte)BLOCK.FIRE.id, BLOCK_TYPE.FIRE);

		// "Thin" slices
		BLOCK_TYPE_MAP.put((byte)BLOCK.SNOW.id, BLOCK_TYPE.THINSLICE);

		// Solid blocks which nevertheless shouldn't be considered "normal."
		// Mostly for rendering purposes.
		BLOCK_TYPE_MAP.put((byte)BLOCK.GLASS.id, BLOCK_TYPE.SEMISOLID);
		BLOCK_TYPE_MAP.put((byte)BLOCK.MOB_SPAWNER.id, BLOCK_TYPE.SEMISOLID);
	}
	
	/***
     * Colors. From block data -> colors
     * (I took the color list from a c program which draws minecraft maps.. forgot the name)
     */
    public static void initBlockColorsTable() {
		blockColors = new Color[128];
		for(int i=0;i<128;i++) {
			blockColors[i] = Color.BLACK;
		}
		
		//blockColors[0] = new Color(255,255,255);
		blockColors[BLOCK.STONE.id] = new Color(120,120,120);
		blockColors[BLOCK.GRASS.id] = new Color(117,176,73);
		blockColors[BLOCK.DIRT.id] = new Color(134,96,67);
		blockColors[BLOCK.COBBLESTONE.id] = new Color(115,115,115);
		blockColors[BLOCK.PLANK.id] = new  Color(157,128,79);
		blockColors[BLOCK.SAPLING.id] = new Color(120,120,120);
		blockColors[BLOCK.BEDROCK.id] = new Color(84,84,84);
		blockColors[BLOCK.WATER.id] = new Color(38,92,255);
		blockColors[BLOCK.STATIONARY_WATER.id] = new  Color(38,92,255);
		blockColors[BLOCK.LAVA.id] = new Color(255,90,0);
		blockColors[BLOCK.STATIONARY_LAVA.id] = new Color(255,90,0);
		blockColors[BLOCK.SAND.id] = new Color(218,210,158);
		blockColors[BLOCK.GRAVEL.id] = new Color(136,126,126);
		blockColors[BLOCK.GOLD_ORE.id] = new Color(143,140,125);
		blockColors[BLOCK.IRON_ORE.id] = new Color(136,130,127);
		blockColors[BLOCK.COAL_ORE.id] = new Color(115,115,115);
		blockColors[BLOCK.WOOD.id] = new Color(102,81,51);
		blockColors[BLOCK.LEAVES.id] = new Color(60,192,41);
		blockColors[BLOCK.SPONGE.id] = new Color(193,193,57);
		blockColors[BLOCK.GLASS.id] = new Color(255,255,255);
		blockColors[BLOCK.LAPIS_LAZULI_ORE.id] = new Color(27,70,161);
		blockColors[BLOCK.LAPIS_LAZULI_BLOCK.id] = blockColors[BLOCK.LAPIS_LAZULI_BLOCK.id];
		blockColors[BLOCK.DISPENSER.id] = new Color(96,96,96);
		blockColors[BLOCK.SANDSTONE.id] = blockColors[BLOCK.SAND.id];
		blockColors[BLOCK.NOTE_BLOCK.id] = new Color(114,88,56);
		blockColors[BLOCK.BED.id] = new Color(255,0,0);
		blockColors[BLOCK.POWERED_RAIL.id] = new Color(120,53,28);
		blockColors[BLOCK.DETECTOR_RAIL.id] = new Color(200,189,189);
		blockColors[BLOCK.WOOL.id] = new Color(222,222,222); //Color(143,143,143,255); 
		blockColors[BLOCK.YELLOW_FLOWER.id] = new Color(255,255,0);
		blockColors[BLOCK.RED_ROSE.id] = new Color(255,0,0);
		blockColors[BLOCK.BROWN_MUSHROOM.id] = new Color(145,109,85);
		blockColors[BLOCK.RED_MUSHROOM.id] = new Color(226,18,18);
		blockColors[BLOCK.GOLD_BLOCK.id] = new Color(231,165,45);
		blockColors[BLOCK.IRON_BLOCK.id] = new Color(191,191,191);
		blockColors[BLOCK.DOUBLE_SLAB.id] = new Color(200,200,200);	// TODO: this should vary with the data value, technically
		blockColors[BLOCK.SLAB.id] = new Color(200,200,200);	// TODO: this should vary with the data value, technically
		blockColors[BLOCK.BRICK.id] = new Color(170,86,62);
		blockColors[BLOCK.TNT.id] = new Color(160,83,65);
		blockColors[BLOCK.BOOKSHELF.id] = new Color(188,152,98);
		blockColors[BLOCK.MOSSY_COBBLESTONE.id] = new Color(115,169,115);
		blockColors[BLOCK.OBSIDIAN.id] = new Color(26,11,43);
		blockColors[BLOCK.TORCH.id] = new Color(245,220,50);
		blockColors[BLOCK.FIRE.id] = new Color(255,170,30);
		blockColors[BLOCK.MOB_SPAWNER.id] = new Color(25,82,122);
		blockColors[BLOCK.WOODEN_STAIRS.id] = new Color(157,128,79);
		blockColors[BLOCK.CHEST.id] = new Color(125,91,38);
		blockColors[BLOCK.REDSTONE_WIRE.id] = new Color(245,50,50);
		blockColors[BLOCK.DIAMOND_ORE.id] = new Color(129,140,143);
		blockColors[BLOCK.DIAMOND_BLOCK.id] = new Color(45,166,152);
		blockColors[BLOCK.WORKBENCH.id] = blockColors[BLOCK.NOTE_BLOCK.id];
		blockColors[BLOCK.CROPS.id] = new Color(146,192,0);
		blockColors[BLOCK.FARMLAND.id] = new Color(95,58,30);
		blockColors[BLOCK.FURNACE.id] = new Color(96,96,96);
		blockColors[BLOCK.BURNING_FURNACE.id] = new Color(96,96,96);
		blockColors[BLOCK.SIGNPOST.id] = new Color(111,91,54);
		blockColors[BLOCK.WOODEN_DOOR.id] = new Color(136,109,67);
		blockColors[BLOCK.LADDER.id] = new Color(181,140,64);
		blockColors[BLOCK.MINECART_TRACKS.id] = new Color(150,134,102);
		blockColors[BLOCK.COBBLESTONE_STAIRS.id] = new Color(115,115,115);
		blockColors[BLOCK.WALL_SIGN.id] = new Color(111,91,54);
		blockColors[BLOCK.LEVER.id] = new Color(124,98,62);
		blockColors[BLOCK.STONE_PRESSURE_PLATE.id] = new Color(120,120,120);
		blockColors[BLOCK.IRON_DOOR.id] = new Color(191,191,191);
		blockColors[BLOCK.WOODEN_PRESSURE_PLATE.id] = new  Color(157,128,79);
		blockColors[BLOCK.REDSTONE_ORE.id] = new Color(131,107,107);
		blockColors[BLOCK.GLOWING_REDSTONE_ORE.id] = new Color(131,107,107);
		blockColors[BLOCK.REDSTONE_TORCH_OFF.id] = new Color(181,140,64);
		blockColors[BLOCK.REDSTONE_TORCH_ON.id] = new Color(255,0,0);
		blockColors[BLOCK.STONE_BUTTON.id] = new Color(120,120,120);
		blockColors[BLOCK.SNOW.id] = new Color(255,255,255);
		blockColors[BLOCK.ICE.id] = new Color(83,113,163);
		blockColors[BLOCK.SNOW_BLOCK.id] = new Color(250,250,250);
		blockColors[BLOCK.CACTUS.id] = new Color(25,120,25);
		blockColors[BLOCK.CLAY.id] = new Color(151,157,169);
		blockColors[BLOCK.SUGARCANE.id] = new Color(100,67,50);
		blockColors[BLOCK.JUKEBOX.id] = blockColors[BLOCK.NOTE_BLOCK.id];
		blockColors[BLOCK.FENCE.id] = new Color(157,128,79);
		blockColors[BLOCK.PUMPKIN.id] = new Color(227,144,29);
		blockColors[BLOCK.NETHERRACK.id] = new Color(104,8,8);
		blockColors[BLOCK.SOUL_SAND.id] = new Color(106,82,68);
		blockColors[BLOCK.GLOWSTONE.id] = new Color(249,212,156);
		blockColors[BLOCK.PORTAL.id] = new Color(214,127,255); // should never actually show up, since obsidian will always be on top
		blockColors[BLOCK.JACK_O_LANTERN.id] = new Color(249,255,58);
		blockColors[BLOCK.CAKE.id] = new Color(234,233,235);
		blockColors[BLOCK.REDSTONE_REPEATER_OFF.id] = new Color(245,50,50);
		blockColors[BLOCK.REDSTONE_REPEATER_ON.id] = new Color(245,50,50);
	}
	public static final int TEXTURE_COAL = (2*16) + 2;
	public static final int TEXTURE_IRON = (2*16) + 1;
	public static final int TEXTURE_GOLD = (2*16) + 0;
	public static final int TEXTURE_DIAMOND = (3*16) + 2;
	public static final int TEXTURE_REDSTONE = (3*16) + 3;
	public static final int TEXTURE_SPAWNER = (4*16) + 1;
	public static final int TEXTURE_CLAY = (4*16)+8;
	public static final int TEXTURE_PUMPKIN = (7*16)+7;
	public static final int TEXTURE_OBSIDIAN = (2*16)+5;
	public static final int TEXTURE_BEDROCK = 17;
    
    /***
     * block data to sprite sheet texture index
     */
	public static void initBlockDatatoSpriteSheetIndexTable() {
		blockDataToSpriteSheet = new int[128];
		for(int i=0;i<128;i++) {
			blockDataToSpriteSheet[i] = (15*16)+13;
		}
		blockDataToSpriteSheet[0] = -1;
		blockDataToSpriteSheet[BLOCK.STONE.id] = 1;
		blockDataToSpriteSheet[BLOCK.GRASS.id] = 0;
		blockDataToSpriteSheet[BLOCK.DIRT.id] = 2;
		blockDataToSpriteSheet[BLOCK.COBBLESTONE.id] = 16;
		blockDataToSpriteSheet[BLOCK.PLANK.id] = 4;
		blockDataToSpriteSheet[BLOCK.SAPLING.id] = 15;
		blockDataToSpriteSheet[BLOCK.BEDROCK.id] = 17;
		blockDataToSpriteSheet[BLOCK.WATER.id] = 207;
		blockDataToSpriteSheet[BLOCK.STATIONARY_WATER.id] = 207;
		blockDataToSpriteSheet[BLOCK.LAVA.id] = 239;
		blockDataToSpriteSheet[BLOCK.STATIONARY_LAVA.id] = 239;
		blockDataToSpriteSheet[BLOCK.SAND.id] = 18;
		blockDataToSpriteSheet[BLOCK.GRAVEL.id] = 19;
		blockDataToSpriteSheet[BLOCK.GOLD_ORE.id] = (2*16) + 0;
		blockDataToSpriteSheet[BLOCK.IRON_ORE.id] = (2*16) + 1;
		blockDataToSpriteSheet[BLOCK.COAL_ORE.id] = (2*16) + 2;
		blockDataToSpriteSheet[BLOCK.WOOD.id] = 20;
		blockDataToSpriteSheet[BLOCK.LEAVES.id] = (3*16)+5; // The "correct" one is actually +4, but with the current transparency
														 // rendering glitches, the non-transparent texture looks better.
		blockDataToSpriteSheet[BLOCK.SPONGE.id] = (3*16);
		blockDataToSpriteSheet[BLOCK.GLASS.id] = 49;
		blockDataToSpriteSheet[BLOCK.LAPIS_LAZULI_ORE.id] = 10*16;
		blockDataToSpriteSheet[BLOCK.LAPIS_LAZULI_BLOCK.id] = 9*16;
		blockDataToSpriteSheet[BLOCK.DISPENSER.id] = (2*16) + 14;
		blockDataToSpriteSheet[BLOCK.SANDSTONE.id] = 12*16;
		blockDataToSpriteSheet[BLOCK.NOTE_BLOCK.id] = (4*16) + 10;
		blockDataToSpriteSheet[BLOCK.BED.id] = (8*16) + 7;
		blockDataToSpriteSheet[BLOCK.POWERED_RAIL.id] = (10*16) + 3;
		blockDataToSpriteSheet[BLOCK.DETECTOR_RAIL.id] = (12*16) + 3;
		blockDataToSpriteSheet[BLOCK.WOOL.id] = 64;		
		blockDataToSpriteSheet[BLOCK.YELLOW_FLOWER.id] = 13;
		blockDataToSpriteSheet[BLOCK.RED_ROSE.id] = 12;
		blockDataToSpriteSheet[BLOCK.BROWN_MUSHROOM.id] = (1*16) + 13;
		blockDataToSpriteSheet[BLOCK.RED_MUSHROOM.id] = (1*16) + 12;
		blockDataToSpriteSheet[BLOCK.GOLD_BLOCK.id] = 23;
		blockDataToSpriteSheet[BLOCK.IRON_BLOCK.id] = 22;
		blockDataToSpriteSheet[BLOCK.DOUBLE_SLAB.id] = 5;
		blockDataToSpriteSheet[BLOCK.SLAB.id] = 6;
		blockDataToSpriteSheet[BLOCK.BRICK.id] = 7;
		blockDataToSpriteSheet[BLOCK.TNT.id] = 8;
		blockDataToSpriteSheet[BLOCK.BOOKSHELF.id] = 35;
		blockDataToSpriteSheet[BLOCK.MOSSY_COBBLESTONE.id] = (2*16)+4;
		blockDataToSpriteSheet[BLOCK.OBSIDIAN.id] = (2*16)+5;
		blockDataToSpriteSheet[BLOCK.TORCH.id] = 80;
		blockDataToSpriteSheet[BLOCK.FIRE.id] = 16+15;  // previously 30
		blockDataToSpriteSheet[BLOCK.MOB_SPAWNER.id] = (4*16) + 1;
		blockDataToSpriteSheet[BLOCK.WOODEN_STAIRS.id] = 4;  // previously 55
		blockDataToSpriteSheet[BLOCK.CHEST.id] = 26;
		blockDataToSpriteSheet[BLOCK.REDSTONE_WIRE.id] = (10*16)+4;
		blockDataToSpriteSheet[BLOCK.DIAMOND_ORE.id] = (3*16) + 2;
		blockDataToSpriteSheet[BLOCK.DIAMOND_BLOCK.id] = 24;
		blockDataToSpriteSheet[BLOCK.WORKBENCH.id] = (3*16)+12;
		blockDataToSpriteSheet[BLOCK.CROPS.id] = (5*16)+15;
		blockDataToSpriteSheet[BLOCK.FARMLAND.id] = (5*16)+6;
		blockDataToSpriteSheet[BLOCK.FURNACE.id] = 44;
		blockDataToSpriteSheet[BLOCK.BURNING_FURNACE.id] = (3*16)+13;
		blockDataToSpriteSheet[BLOCK.SIGNPOST.id] = 4;
		blockDataToSpriteSheet[BLOCK.WOODEN_DOOR.id] = (6*16)+1;
		blockDataToSpriteSheet[BLOCK.LADDER.id] = (5*16)+3;
		blockDataToSpriteSheet[BLOCK.MINECART_TRACKS.id] = (8*16);
		blockDataToSpriteSheet[BLOCK.COBBLESTONE_STAIRS.id] = 16;
		blockDataToSpriteSheet[BLOCK.WALL_SIGN.id] = 4;
		blockDataToSpriteSheet[BLOCK.LEVER.id] = (6*16);
	    blockDataToSpriteSheet[BLOCK.STONE_PRESSURE_PLATE.id] = 6;
		blockDataToSpriteSheet[BLOCK.IRON_DOOR.id] = (6*16)+2;
		blockDataToSpriteSheet[BLOCK.WOODEN_PRESSURE_PLATE.id] = 4;
		blockDataToSpriteSheet[BLOCK.REDSTONE_ORE.id] = (3*16) + 3;
		blockDataToSpriteSheet[BLOCK.GLOWING_REDSTONE_ORE.id] = 51;
		blockDataToSpriteSheet[BLOCK.REDSTONE_TORCH_OFF.id] = (7*16)+3;
		blockDataToSpriteSheet[BLOCK.REDSTONE_TORCH_ON.id] = (6*16)+3;
		blockDataToSpriteSheet[BLOCK.STONE_BUTTON.id] = 6;
		blockDataToSpriteSheet[BLOCK.SNOW.id] = 66;
		blockDataToSpriteSheet[BLOCK.ICE.id] = 67;
		blockDataToSpriteSheet[BLOCK.SNOW_BLOCK.id] = 66;
		blockDataToSpriteSheet[BLOCK.CACTUS.id] = 70;
		blockDataToSpriteSheet[BLOCK.CLAY.id] = (4*16)+8;
		blockDataToSpriteSheet[BLOCK.SUGARCANE.id] = 73;
		blockDataToSpriteSheet[BLOCK.JUKEBOX.id] = (4*16)+11;
		blockDataToSpriteSheet[BLOCK.FENCE.id] = 4;
		blockDataToSpriteSheet[BLOCK.PUMPKIN.id] = (7*16)+7;
		blockDataToSpriteSheet[BLOCK.NETHERRACK.id] = (6*16)+7;
		blockDataToSpriteSheet[BLOCK.SOUL_SAND.id] = (6*16)+8;
		blockDataToSpriteSheet[BLOCK.GLOWSTONE.id] = (6*16)+9;
		blockDataToSpriteSheet[BLOCK.PORTAL.id] = 16+14;
		blockDataToSpriteSheet[BLOCK.JACK_O_LANTERN.id] = (7*16)+8;
		blockDataToSpriteSheet[BLOCK.CAKE.id] = (7*16) + 9;
		blockDataToSpriteSheet[BLOCK.REDSTONE_REPEATER_OFF.id] = (8*16) + 3;
		blockDataToSpriteSheet[BLOCK.REDSTONE_REPEATER_ON.id] = (9*16) + 3;
		
		// Textures used by logs
		HashMap<Byte, Integer> logMap = new HashMap<Byte, Integer>();
		blockDataSpriteSheetMap.put((byte)BLOCK.WOOD.id, logMap);
		logMap.put((byte)0, 16+4); // Regular log
		logMap.put((byte)1, (7*16)+4); // Spruce
		logMap.put((byte)2, (7*16)+5); // Birch
		
		// Textures used by saplings
		HashMap<Byte, Integer> saplingMap = new HashMap<Byte, Integer>();
		blockDataSpriteSheetMap.put((byte)BLOCK.SAPLING.id, saplingMap);
		saplingMap.put((byte)0, 15); // Regular sapling
		saplingMap.put((byte)1, (3*16)+15); // Spruce
		saplingMap.put((byte)2, (4*16)+15); // Birch

		// Textures used by wool
		HashMap<Byte, Integer> woolMap = new HashMap<Byte, Integer>();
		blockDataSpriteSheetMap.put((byte)BLOCK.WOOL.id, woolMap);
		woolMap.put((byte)0, 64); // Regular wool
		woolMap.put((byte)15, (7*16)+1); // Black
		woolMap.put((byte)14, (8*16)+1); // Red
		woolMap.put((byte)13, (9*16)+1); // Dark Green
		woolMap.put((byte)12, (10*16)+1); // Brown
		woolMap.put((byte)11, (11*16)+1); // Blue
		woolMap.put((byte)10, (12*16)+1); // Purple
		woolMap.put((byte)9, (13*16)+1); // Cyan
		woolMap.put((byte)8, (14*16)+1); // Grey
		woolMap.put((byte)7, (7*16)+2); // Dark Grey
		woolMap.put((byte)6, (8*16)+2); // Pink
		woolMap.put((byte)5, (9*16)+2); // Light Green
		woolMap.put((byte)4, (10*16)+2); // Yellow
		woolMap.put((byte)3, (11*16)+2); // Light Blue
		woolMap.put((byte)2, (12*16)+2); // Magenta
		woolMap.put((byte)1, (13*16)+2); // Orange	

		// Textures used by slabs
		HashMap<Byte, Integer> slabMap = new HashMap<Byte, Integer>();
		blockDataSpriteSheetMap.put((byte)BLOCK.SLAB.id, slabMap);
		slabMap.put((byte)0, 6); // Smooth stone
		slabMap.put((byte)1, (12*16)); // Sandstone
		slabMap.put((byte)2, 4); // Plank
		slabMap.put((byte)3, (1*16)); // Cobblestone

		// Textures used by double-slabs (we separate them because there's
		// a separate texture for double-smoothstone which IMO is better
		// to use, so this way it's separate between the two.
		HashMap<Byte, Integer> dblSlabMap = new HashMap<Byte, Integer>();
		blockDataSpriteSheetMap.put((byte)BLOCK.DOUBLE_SLAB.id, dblSlabMap);
		dblSlabMap.put((byte)0, 5); // Smooth stone
		dblSlabMap.put((byte)1, (12*16)); // Sandstone
		dblSlabMap.put((byte)2, 4); // Plank
		dblSlabMap.put((byte)3, (1*16)); // Cobblestone

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
