package com.plusminus.craft;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;

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
	
	public static final int BLOCKSPERROW = 128;
	public static final int BLOCKSPERCOLUMN = BLOCKSPERROW * 16;
	    
    // sensitivity and speed mouse configuration
	public static final float MOUSE_SENSITIVITY 	= 0.05f; // mouse delta is multiplied by this
    public static float MOVEMENT_SPEED 		= 10.0f; // world units per second
    
    // the font to draw information to the screen
    public static final Font ARIALFONT = new Font("Arial", Font.BOLD, 14);
    public static final Font HEADERFONT = new Font("Arial", Font.BOLD, 26);
	
    // some convenience statics regarding time calculation
	public static final long NANOSPERSECOND 	= 1000000000;
	public static final long MILLISPERSECOND 	= 1000;
	public static final long NANOSPERMILLIS 	= NANOSPERSECOND / MILLISPERSECOND;
	
	public static final int TYPE_WATER = 8;
	public static final int TYPE_WATER_STATIONARY = 9;
	public static final int TYPE_LAVA = 10;
	public static final int TYPE_LAVA_STATIONARY = 11;
	
	// Types of blocks
	public static enum BLOCK_TYPE {
		NORMAL,
		TORCH,
		DECORATION_SMALL,
		DECORATION_FULL,
		CROPS,
		LADDER,
		FLOOR,
		PRESSURE_PLATE,
		HALFHEIGHT,
		DOOR,
		STAIRS,
		SIGNPOST,
		WALLSIGN,
		FENCE,
		LEVER,
		BUTTON,
		PORTAL,
		DATAVAL,
		MINECART_TRACKS
	}
	
	// This HashMap determines how we draw various block types
	public static HashMap<Byte, BLOCK_TYPE> BLOCK_TYPE_MAP = new HashMap<Byte, BLOCK_TYPE>();
	
	// This HashMap is used to determine which texture to use for blocks whose data value determines
	// what texture to use
	public static HashMap<Byte, HashMap<Byte, Integer>> blockDataSpriteSheetMap = new HashMap<Byte, HashMap<Byte, Integer>>();
	
	public static final int BLOCK_STONE = 1;
	public static final int BLOCK_GRASS = 2;
	public static final int BLOCK_DIRT = 3;
	public static final int BLOCK_COBBLESTONE = 4;
	public static final int BLOCK_WOOD = 5;
	public static final int BLOCK_SAPLING = 6;
	public static final int BLOCK_BEDROCK = 7;
	public static final int BLOCK_WATER = 8;
	public static final int BLOCK_STATIONARY_WATER = 9;
	public static final int BLOCK_LAVA = 10;
	public static final int BLOCK_STATIONARY_LAVA = 11;
	public static final int BLOCK_SAND = 12;
	public static final int BLOCK_GRAVEL = 13;
	public static final int BLOCK_GOLD_ORE = 14;
	public static final int BLOCK_IRON_ORE = 15;
	public static final int BLOCK_COAL_ORE = 16;
	public static final int BLOCK_LOG = 17;
	public static final int BLOCK_LEAVES = 18;
	public static final int BLOCK_SPONGE = 19;
	public static final int BLOCK_GLASS = 20;
	public static final int BLOCK_LAPIS_LAZULI_ORE = 21;
	public static final int BLOCK_LAPIS_LAZULI_BLOCK = 22;
	public static final int BLOCK_DISPENSER = 23;
	public static final int BLOCK_SANDSTONE = 24;
	public static final int BLOCK_NOTE_BLOCK = 25;
	public static final int BLOCK_WOOL = 35;
	public static final int BLOCK_YELLOW_FLOWER = 37;
	public static final int BLOCK_RED_ROSE = 38;
	public static final int BLOCK_BROWN_MUSHROOM = 39;
	public static final int BLOCK_RED_MUSHROOM = 40;
	public static final int BLOCK_GOLD_BLOCK = 41;
	public static final int BLOCK_IRON_BLOCK = 42;
	public static final int BLOCK_DOUBLE_STEP = 43;
	public static final int BLOCK_STEP = 44;
	public static final int BLOCK_BRICK = 45;
	public static final int BLOCK_TNT = 46;
	public static final int BLOCK_BOOKSHELF = 47;
	public static final int BLOCK_MOSSY_COBBLESTONE = 48;
	public static final int BLOCK_OBSIDIAN = 49;
	public static final int BLOCK_TORCH = 50;
	public static final int BLOCK_FIRE = 51;
	public static final int BLOCK_MOB_SPAWNER = 52;
	public static final int BLOCK_WOODEN_STAIRS = 53;
	public static final int BLOCK_CHEST = 54;
	public static final int BLOCK_REDSTONE_WIRE = 55;
	public static final int BLOCK_DIAMOND_ORE = 56;
	public static final int BLOCK_DIAMOND_BLOCK = 57;
	public static final int BLOCK_WORKBENCH = 58;
	public static final int BLOCK_CROPS = 59;
	public static final int BLOCK_SOIL = 60;
	public static final int BLOCK_FURNACE = 61;
	public static final int BLOCK_BURNING_FURNACE = 62;
	public static final int BLOCK_SIGNPOST = 63;
	public static final int BLOCK_WOODEN_DOOR = 64;
	public static final int BLOCK_LADDER = 65;
	public static final int BLOCK_MINECART_TRACKS = 66;
	public static final int BLOCK_COBBLESTONE_STAIRS = 67;
	public static final int BLOCK_WALL_SIGN = 68;
	public static final int BLOCK_LEVER = 69;
	public static final int BLOCK_STONE_PRESSURE_PLATE = 70;
	public static final int BLOCK_IRON_DOOR = 71;
	public static final int BLOCK_WOODEN_PRESSURE_PLATE = 72;
	public static final int BLOCK_REDSTONE_ORE = 73;
	public static final int BLOCK_GLOWING_REDSTONE_ORE = 74;
	public static final int BLOCK_REDSTONE_TORCH_OFF = 75;
	public static final int BLOCK_REDSTONE_TORCH_ON = 76;
	public static final int BLOCK_STONE_BUTTON = 77;
	public static final int BLOCK_SNOW = 78;
	public static final int BLOCK_ICE = 79;
	public static final int BLOCK_SNOW_BLOCK = 80;
	public static final int BLOCK_CACTUS = 81;
	public static final int BLOCK_CLAY = 82;
	public static final int BLOCK_SUGARCANE = 83;
	public static final int BLOCK_JUKEBOX = 84;
	public static final int BLOCK_FENCE = 85;
	public static final int BLOCK_PUMPKIN = 86;
	public static final int BLOCK_NETHERSTONE = 87;
	public static final int BLOCK_SLOW_SAND = 88;
	public static final int BLOCK_LIGHTSTONE = 89;
	public static final int BLOCK_PORTAL = 90;
	public static final int BLOCK_JACK_O_LANTERN = 91;
	public static final int BLOCK_CAKE = 92;
	
	// HIGHLIGHT_ORES defines the kinds of blocks that we'll highlight.
	// TODO: Really, redstone should highlight whether it's glowing or not - that's the one situation
	// where it's a bit nicer to highlight based on texture rather than block type (which is what X-Ray
	// used to do before the Maintenance Branch changed things around).  Because you'll only get glowing
	// restone ore when you're actually LOOKING at it in-game, though, I feel okay leaving it the way
	// it is.
	public static final int[] HIGHLIGHT_ORES = new int[] {BLOCK_CLAY, BLOCK_PUMPKIN, BLOCK_OBSIDIAN, BLOCK_COAL_ORE, BLOCK_IRON_ORE, BLOCK_GOLD_ORE, BLOCK_LAPIS_LAZULI_ORE, BLOCK_DIAMOND_ORE, BLOCK_REDSTONE_ORE, BLOCK_MOB_SPAWNER};
	public static final String[] ORES_DESCRIPTION = new String[] {"Clay", "Pumpkin", "Obsidian", "Coal", "Iron", "Gold", "Lapis", "Diamond", "Redstone", "Spawner"};
	
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
		BLOCK_TYPE_MAP.put((byte)BLOCK_TORCH, BLOCK_TYPE.TORCH);
		BLOCK_TYPE_MAP.put((byte)BLOCK_REDSTONE_TORCH_ON, BLOCK_TYPE.TORCH);
		BLOCK_TYPE_MAP.put((byte)BLOCK_REDSTONE_TORCH_OFF, BLOCK_TYPE.TORCH);
		
		// Small decoration blocks
		BLOCK_TYPE_MAP.put((byte)BLOCK_RED_MUSHROOM, BLOCK_TYPE.DECORATION_SMALL);
		BLOCK_TYPE_MAP.put((byte)BLOCK_BROWN_MUSHROOM, BLOCK_TYPE.DECORATION_SMALL);
		BLOCK_TYPE_MAP.put((byte)BLOCK_RED_ROSE, BLOCK_TYPE.DECORATION_SMALL);
		BLOCK_TYPE_MAP.put((byte)BLOCK_YELLOW_FLOWER, BLOCK_TYPE.DECORATION_SMALL);
		BLOCK_TYPE_MAP.put((byte)BLOCK_SAPLING, BLOCK_TYPE.DECORATION_SMALL);
		
		// "Full" decoration blocks
		BLOCK_TYPE_MAP.put((byte)BLOCK_SUGARCANE, BLOCK_TYPE.DECORATION_FULL);
		
		// Crops
		BLOCK_TYPE_MAP.put((byte)BLOCK_CROPS, BLOCK_TYPE.CROPS);
		
		// Ladders
		BLOCK_TYPE_MAP.put((byte)BLOCK_LADDER, BLOCK_TYPE.LADDER);
		
		// "floor" decorations
		BLOCK_TYPE_MAP.put((byte)BLOCK_REDSTONE_WIRE, BLOCK_TYPE.FLOOR);
		
		// Minecart tracks
		BLOCK_TYPE_MAP.put((byte)BLOCK_MINECART_TRACKS, BLOCK_TYPE.MINECART_TRACKS);
		
		// Pressure plates
		BLOCK_TYPE_MAP.put((byte)BLOCK_STONE_PRESSURE_PLATE, BLOCK_TYPE.PRESSURE_PLATE);
		BLOCK_TYPE_MAP.put((byte)BLOCK_WOODEN_PRESSURE_PLATE, BLOCK_TYPE.PRESSURE_PLATE);
		
		// Half-height blocks
		BLOCK_TYPE_MAP.put((byte)BLOCK_STEP, BLOCK_TYPE.HALFHEIGHT);
		BLOCK_TYPE_MAP.put((byte)BLOCK_CAKE, BLOCK_TYPE.HALFHEIGHT);
		
		// Doors
		BLOCK_TYPE_MAP.put((byte)BLOCK_WOODEN_DOOR, BLOCK_TYPE.DOOR);
		BLOCK_TYPE_MAP.put((byte)BLOCK_IRON_DOOR, BLOCK_TYPE.DOOR);
		
		// Stairs
		BLOCK_TYPE_MAP.put((byte)BLOCK_WOODEN_STAIRS, BLOCK_TYPE.STAIRS);
		BLOCK_TYPE_MAP.put((byte)BLOCK_COBBLESTONE_STAIRS, BLOCK_TYPE.STAIRS);
		
		// Signs
		BLOCK_TYPE_MAP.put((byte)BLOCK_SIGNPOST, BLOCK_TYPE.SIGNPOST);
		BLOCK_TYPE_MAP.put((byte)BLOCK_WALL_SIGN, BLOCK_TYPE.WALLSIGN);
		
		// Fences
		BLOCK_TYPE_MAP.put((byte)BLOCK_FENCE, BLOCK_TYPE.FENCE);
		
		// Lever
		BLOCK_TYPE_MAP.put((byte)BLOCK_LEVER, BLOCK_TYPE.LEVER);
		
		// Button
		BLOCK_TYPE_MAP.put((byte)BLOCK_STONE_BUTTON, BLOCK_TYPE.BUTTON);
		
		// Portal
		BLOCK_TYPE_MAP.put((byte)BLOCK_PORTAL, BLOCK_TYPE.PORTAL);
		
		// Blocks whose texture depends on their data value (in addition to block type)
		BLOCK_TYPE_MAP.put((byte)BLOCK_LOG, BLOCK_TYPE.DATAVAL);
		BLOCK_TYPE_MAP.put((byte)BLOCK_WOOL, BLOCK_TYPE.DATAVAL);
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
		blockColors[BLOCK_STONE] = new Color(120,120,120);
		blockColors[BLOCK_GRASS] = new Color(117,176,73);
		blockColors[BLOCK_DIRT] = new Color(134,96,67);
		blockColors[BLOCK_COBBLESTONE] = new Color(115,115,115);
		blockColors[BLOCK_MOSSY_COBBLESTONE] = new Color(115,115,115);
		blockColors[BLOCK_WOOD] = new  Color(157,128,79);
		blockColors[BLOCK_SAPLING] = new Color(120,120,120);
		blockColors[BLOCK_BEDROCK] = new Color(84,84,84);
		blockColors[BLOCK_WATER] = new Color(38,92,255);
		blockColors[BLOCK_STATIONARY_WATER] = new  Color(38,92,255);
		blockColors[BLOCK_LAVA] = new Color(255,90,0);
		blockColors[BLOCK_STATIONARY_LAVA] = new Color(255,90,0);
		blockColors[BLOCK_SAND] = new Color(218,210,158);
		blockColors[BLOCK_GRAVEL] = new Color(136,126,126);
		blockColors[BLOCK_GOLD_ORE] = new Color(143,140,125);
		blockColors[BLOCK_IRON_ORE] = new Color(136,130,127);
		blockColors[BLOCK_COAL_ORE] = new Color(115,115,115);
		blockColors[BLOCK_LOG] = new Color(102,81,51);
		blockColors[BLOCK_LEAVES] = new Color(60,192,41);
		blockColors[BLOCK_GLASS] = new Color(255,255,255);
		blockColors[BLOCK_LAPIS_LAZULI_ORE] = new Color(27,70,161);
		blockColors[BLOCK_LAPIS_LAZULI_BLOCK] = blockColors[BLOCK_LAPIS_LAZULI_BLOCK];
		blockColors[BLOCK_DISPENSER] = new Color(96,96,96);
		blockColors[BLOCK_NOTE_BLOCK] = new Color(114,88,56);
		blockColors[BLOCK_SANDSTONE] = blockColors[BLOCK_SAND];
		blockColors[BLOCK_WOOL] = new Color(222,222,222); //Color(143,143,143,255); 
		blockColors[BLOCK_RED_ROSE] = new Color(255,0,0);
		blockColors[BLOCK_YELLOW_FLOWER] = new Color(255,255,0);
		blockColors[BLOCK_GOLD_BLOCK] = new Color(231,165,45);
		blockColors[BLOCK_IRON_BLOCK] = new Color(191,191,191);
		blockColors[BLOCK_DOUBLE_STEP] = new Color(200,200,200);
		blockColors[BLOCK_STEP] = new Color(200,200,200);
		blockColors[BLOCK_BRICK] = new Color(170,86,62);
		blockColors[BLOCK_TNT] = new Color(160,83,65);
		blockColors[BLOCK_OBSIDIAN] = new Color(26,11,43);
		blockColors[BLOCK_TORCH] = new Color(245,220,50);
		blockColors[BLOCK_FIRE] = new Color(255,170,30);
		//blockColors[BLOCK_MOB_SPAWNER] = Color(245,220,50,255); unnecessary afaik
		blockColors[BLOCK_WOODEN_STAIRS] = new Color(157,128,79);
		blockColors[BLOCK_CHEST] = new Color(125,91,38);
		//blockColors[BLOCK_REDSTONE_WIRE] = Color(245,220,50,255); unnecessary afaik
		blockColors[BLOCK_DIAMOND_ORE] = new Color(129,140,143);
		blockColors[BLOCK_DIAMOND_BLOCK] = new Color(45,166,152);
		blockColors[BLOCK_WORKBENCH] = blockColors[BLOCK_NOTE_BLOCK];
		blockColors[BLOCK_CROPS] = new Color(146,192,0);
		blockColors[BLOCK_SOIL] = new Color(95,58,30);
		blockColors[BLOCK_FURNACE] = new Color(96,96,96);
		blockColors[BLOCK_BURNING_FURNACE] = new Color(96,96,96);
		blockColors[BLOCK_SIGNPOST] = new Color(111,91,54);
		blockColors[BLOCK_WOODEN_DOOR] = new Color(136,109,67);
		blockColors[BLOCK_LADDER] = new Color(181,140,64);
		blockColors[BLOCK_MINECART_TRACKS] = new Color(150,134,102);
		blockColors[BLOCK_COBBLESTONE_STAIRS] = new Color(115,115,115);
		blockColors[BLOCK_IRON_DOOR] = new Color(191,191,191);
		blockColors[BLOCK_REDSTONE_ORE] = new Color(131,107,107);
		blockColors[BLOCK_GLOWING_REDSTONE_ORE] = new Color(131,107,107);
		blockColors[BLOCK_REDSTONE_TORCH_OFF] = new Color(181,140,64);
		blockColors[BLOCK_REDSTONE_TORCH_ON] = new Color(255,0,0);
		blockColors[BLOCK_SNOW] = new Color(255,255,255);
		blockColors[BLOCK_ICE] = new Color(83,113,163);
		blockColors[BLOCK_SNOW_BLOCK] = new Color(250,250,250);
		blockColors[BLOCK_CACTUS] = new Color(25,120,25);
		blockColors[BLOCK_CLAY] = new Color(151,157,169);
		blockColors[BLOCK_SUGARCANE] = new Color(100,67,50);
		blockColors[BLOCK_JUKEBOX] = blockColors[BLOCK_NOTE_BLOCK];
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
			blockDataToSpriteSheet[i] = -1;
		}
		//blockTrans[0] = -1;
		blockDataToSpriteSheet[BLOCK_STONE] = 1;
		blockDataToSpriteSheet[BLOCK_GRASS] = 0;
		blockDataToSpriteSheet[BLOCK_DIRT] = 2;
		blockDataToSpriteSheet[BLOCK_COBBLESTONE] = 16;
		blockDataToSpriteSheet[BLOCK_WOOD] = 4;
		blockDataToSpriteSheet[BLOCK_SAPLING] = 15;
		blockDataToSpriteSheet[BLOCK_BEDROCK] = 17;
		blockDataToSpriteSheet[BLOCK_WATER] = 207;
		blockDataToSpriteSheet[BLOCK_STATIONARY_WATER] = 207;
		blockDataToSpriteSheet[BLOCK_LAVA] = 239;
		blockDataToSpriteSheet[BLOCK_STATIONARY_LAVA] = 239;
		blockDataToSpriteSheet[BLOCK_SAND] = 18;
		blockDataToSpriteSheet[BLOCK_GRAVEL] = 19;
		blockDataToSpriteSheet[BLOCK_GOLD_ORE] = (2*16) + 0;
		blockDataToSpriteSheet[BLOCK_IRON_ORE] = (2*16) + 1;
		blockDataToSpriteSheet[BLOCK_COAL_ORE] = (2*16) + 2;
		blockDataToSpriteSheet[BLOCK_LOG] = 20;
		blockDataToSpriteSheet[BLOCK_LEAVES] = (3*16)+5; // The "correct" one is actually +4, but with the current transparency
														 // rendering glitches, the non-transparent texture looks better.
		blockDataToSpriteSheet[BLOCK_SPONGE] = (3*16);
		blockDataToSpriteSheet[BLOCK_GLASS] = 49;
		blockDataToSpriteSheet[BLOCK_LAPIS_LAZULI_ORE] = 10*16;
		blockDataToSpriteSheet[BLOCK_LAPIS_LAZULI_BLOCK] = 9*16;
		blockDataToSpriteSheet[BLOCK_DISPENSER] = (2*16) + 14;
		blockDataToSpriteSheet[BLOCK_SANDSTONE] = 12*16;
		blockDataToSpriteSheet[BLOCK_NOTE_BLOCK] = (4*16) + 10;
		blockDataToSpriteSheet[BLOCK_WOOL] = 64;		
		blockDataToSpriteSheet[BLOCK_YELLOW_FLOWER] = 13;
		blockDataToSpriteSheet[BLOCK_RED_ROSE] = 12;
		blockDataToSpriteSheet[BLOCK_BROWN_MUSHROOM] = (1*16) + 13;
		blockDataToSpriteSheet[BLOCK_RED_MUSHROOM] = (1*16) + 12;
		blockDataToSpriteSheet[BLOCK_GOLD_BLOCK] = 23;
		blockDataToSpriteSheet[BLOCK_IRON_BLOCK] = 22;
		blockDataToSpriteSheet[BLOCK_DOUBLE_STEP] = 5; // previously 72, which was clay
		blockDataToSpriteSheet[BLOCK_STEP] = 6; // previously 72, which was clay
		blockDataToSpriteSheet[BLOCK_BRICK] = 7;
		blockDataToSpriteSheet[BLOCK_TNT] = 8;
		blockDataToSpriteSheet[BLOCK_BOOKSHELF] = 35;
		blockDataToSpriteSheet[BLOCK_MOSSY_COBBLESTONE] = (2*16)+4;
		blockDataToSpriteSheet[BLOCK_OBSIDIAN] = (2*16)+5;
		blockDataToSpriteSheet[BLOCK_TORCH] = 80;
		blockDataToSpriteSheet[BLOCK_FIRE] = 16+15;  // previously 30
		blockDataToSpriteSheet[BLOCK_MOB_SPAWNER] = (4*16) + 1;
		blockDataToSpriteSheet[BLOCK_WOODEN_STAIRS] = 4;  // previously 55
		blockDataToSpriteSheet[BLOCK_CHEST] = 26;
		blockDataToSpriteSheet[BLOCK_REDSTONE_WIRE] = 100;
		blockDataToSpriteSheet[BLOCK_DIAMOND_ORE] = (3*16) + 2;
		blockDataToSpriteSheet[BLOCK_DIAMOND_BLOCK] = 24;
		blockDataToSpriteSheet[BLOCK_WORKBENCH] = 25;
		blockDataToSpriteSheet[BLOCK_CROPS] = (5*16)+15;
		blockDataToSpriteSheet[BLOCK_SOIL] = (5*16)+6;
		blockDataToSpriteSheet[BLOCK_FURNACE] = 44;
		blockDataToSpriteSheet[BLOCK_BURNING_FURNACE] = (3*16)+13;
		blockDataToSpriteSheet[BLOCK_SIGNPOST] = 4;
		blockDataToSpriteSheet[BLOCK_WOODEN_DOOR] = (6*16)+1;
		blockDataToSpriteSheet[BLOCK_LADDER] = (5*16)+3;
		blockDataToSpriteSheet[BLOCK_MINECART_TRACKS] = (8*16);
		blockDataToSpriteSheet[BLOCK_COBBLESTONE_STAIRS] = 16;
		blockDataToSpriteSheet[BLOCK_WALL_SIGN] = 4;
		blockDataToSpriteSheet[BLOCK_LEVER] = (6*16);
	    blockDataToSpriteSheet[BLOCK_STONE_PRESSURE_PLATE] = 6;
		blockDataToSpriteSheet[BLOCK_IRON_DOOR] = (6*16)+2;
		blockDataToSpriteSheet[BLOCK_WOODEN_PRESSURE_PLATE] = 4;
		blockDataToSpriteSheet[BLOCK_REDSTONE_ORE] = (3*16) + 3;
		blockDataToSpriteSheet[BLOCK_GLOWING_REDSTONE_ORE] = 51;
		blockDataToSpriteSheet[BLOCK_REDSTONE_TORCH_OFF] = (7*16)+3;
		blockDataToSpriteSheet[BLOCK_REDSTONE_TORCH_ON] = (6*16)+3;
		blockDataToSpriteSheet[BLOCK_STONE_BUTTON] = 6;
		blockDataToSpriteSheet[BLOCK_SNOW] = 66;
		blockDataToSpriteSheet[BLOCK_ICE] = 67;
		blockDataToSpriteSheet[BLOCK_SNOW_BLOCK] = 66;
		blockDataToSpriteSheet[BLOCK_CACTUS] = 70;
		blockDataToSpriteSheet[BLOCK_CLAY] = (4*16)+8;
		blockDataToSpriteSheet[BLOCK_SUGARCANE] = 73;
		blockDataToSpriteSheet[BLOCK_JUKEBOX] = (4*16)+10;
		blockDataToSpriteSheet[BLOCK_FENCE] = 4;
		blockDataToSpriteSheet[BLOCK_PUMPKIN] = (7*16)+7;
		blockDataToSpriteSheet[BLOCK_NETHERSTONE] = (6*16)+7;
		blockDataToSpriteSheet[BLOCK_SLOW_SAND] = (6*16)+8;
		blockDataToSpriteSheet[BLOCK_LIGHTSTONE] = (6*16)+9;
		blockDataToSpriteSheet[BLOCK_PORTAL] = 16+14;
		blockDataToSpriteSheet[BLOCK_JACK_O_LANTERN] = (7*16)+8;
		blockDataToSpriteSheet[BLOCK_CAKE] = (7*16) + 9;
		
		// Textures used by logs
		HashMap<Byte, Integer> logMap = new HashMap<Byte, Integer>();
		blockDataSpriteSheetMap.put((byte)BLOCK_LOG, logMap);
		logMap.put((byte)0, 16+4); // Regular log
		logMap.put((byte)1, (7*16)+4); // Redwood-or-whatever
		logMap.put((byte)2, (7*16)+5); // Birch

		// Textures used by wool
		HashMap<Byte, Integer> woolMap = new HashMap<Byte, Integer>();
		blockDataSpriteSheetMap.put((byte)BLOCK_WOOL, woolMap);
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
	}
	
	/***
	 * Sprite sheet texture index to texture coordinates
	 */
	public static void initSpriteSheetToTextureTable() {
		precalcSpriteSheetToTextureX = new float[256];
		precalcSpriteSheetToTextureY = new float[256];
		for(int i=0;i<256;i++) {
			float texYy = ((int) (i / 16))/16.0f;
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

		paintingback = new PaintingInfo(4, 4, 12, 0);
	}
}
