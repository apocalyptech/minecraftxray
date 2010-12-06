package com.plusminus.craft;

import java.awt.Color;
import java.awt.Font;

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
	
    // some convenience statics regarding time calculation
	public static final long NANOSPERSECOND 	= 1000000000;
	public static final long MILLISPERSECOND 	= 1000;
	public static final long NANOSPERMILLIS 	= NANOSPERSECOND / MILLISPERSECOND;
	
	// TEXTURE_ORES defines the kinds of blocks that we'll highlight.  Note that it's keyed off of the *texture* that we show, not the block type,
	// which could potentially have some issues if we're showing the same texture for more than one block type (as with wood).
	public static final int TEXTURE_COAL = (2*16) + 2;
	public static final int TEXTURE_IRON = (2*16) + 1;
	public static final int TEXTURE_GOLD = (2*16) + 0;
	public static final int TEXTURE_DIAMOND = (3*16) + 2;
	public static final int TEXTURE_REDSTONE = (3*16) + 3;
	public static final int TEXTURE_SPAWNER = (4*16) + 1;
	public static final int TEXTURE_CLAY = (4*16)+8;
	public static final int[] TEXTURE_ORES = new int[] {TEXTURE_COAL, TEXTURE_IRON, TEXTURE_GOLD, TEXTURE_DIAMOND, TEXTURE_REDSTONE, TEXTURE_SPAWNER, TEXTURE_CLAY};
	public static final String[] ORES_DESCRIPTION = new String[] {"Coal", "Iron", "Gold", "Diamond", "Redstone", "Spawner", "Clay"};
	/*public static final int[] TEXTURE_ORES = new int[] {TEXTURE_COAL, TEXTURE_IRON, TEXTURE_GOLD, TEXTURE_DIAMOND, TEXTURE_CLAY, TEXTURE_SPAWNER};
	public static final String[] ORES_DESCRIPTION = new String[] {"Coal", "Iron", "Gold", "Diamond", "Clay", "Spawner"};*/
	
	public static final int TEXTURE_WATER = 207;
	public static final int TEXTURE_TORCH = 5*16;
	
	public static final int TYPE_WATER = 8;
	public static final int TYPE_WATER_STATIONARY = 9;
	public static final int TYPE_LAVA = 10;
	public static final int TYPE_LAVA_STATIONARY = 11;
	public static final int TYPE_TORCH = 50;
	
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
	public static final int BLOCK_GRAY_CLOTH = 35;
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
	public static final int BLOCK_REED = 83;
	public static final int BLOCK_JUKEBOX = 84;
	public static final int BLOCK_FENCE = 85;
	public static final int BLOCK_PUMPKIN = 86;
	public static final int BLOCK_NETHERSTONE = 87;
	public static final int BLOCK_SLOW_SAND = 88;
	public static final int BLOCK_LIGHTSTONE = 89;
	public static final int BLOCK_PORTAL = 90;
	public static final int BLOCK_JACK_O_LANTERN = 91;
	
	public static final Block[] SURROUNDINGBLOCKS = new Block[] { 
    	new Block(+1,0,0),
    	new Block(-1,0,0),
    	new Block(0,+1,0),
    	new Block(0,-1,0),
    	new Block(0,0,+1),
    	new Block(0,0,-1),
	};
	
	static {
		initBlockColorsTable();
		initBlockDatatoSpriteSheetIndexTable();
		initSpriteSheetToTextureTable();
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
		blockColors[BLOCK_GLASS] = new Color(255,255,255); //glass
		blockColors[BLOCK_GRAY_CLOTH] = new Color(222,222,222); //Color(143,143,143,255); 
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
		blockColors[BLOCK_WORKBENCH] = new Color(114,88,56);
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
		blockColors[BLOCK_REED] = new Color(100,67,50);
	}
    
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
		blockDataToSpriteSheet[BLOCK_GOLD_ORE] = TEXTURE_GOLD;
		blockDataToSpriteSheet[BLOCK_IRON_ORE] = TEXTURE_IRON;
		blockDataToSpriteSheet[BLOCK_COAL_ORE] = TEXTURE_COAL;
		blockDataToSpriteSheet[BLOCK_LOG] = 20;
		blockDataToSpriteSheet[BLOCK_LEAVES] = 53;
		blockDataToSpriteSheet[BLOCK_SPONGE] = 52;
		blockDataToSpriteSheet[BLOCK_GLASS] = 49;
		blockDataToSpriteSheet[BLOCK_GRAY_CLOTH] = 64;		
		blockDataToSpriteSheet[BLOCK_YELLOW_FLOWER] = 13;
		blockDataToSpriteSheet[BLOCK_RED_ROSE] = 12;
		blockDataToSpriteSheet[BLOCK_BROWN_MUSHROOM] = (1*16) + 13;
		blockDataToSpriteSheet[BLOCK_RED_MUSHROOM] = (1*16) + 12;
		blockDataToSpriteSheet[BLOCK_GOLD_BLOCK] = 23;
		blockDataToSpriteSheet[BLOCK_IRON_BLOCK] = 22;
		blockDataToSpriteSheet[BLOCK_DOUBLE_STEP] = 5; // previously 72, which was clay
		blockDataToSpriteSheet[BLOCK_STEP] = 5; // previously 72, which was clay
		blockDataToSpriteSheet[BLOCK_BRICK] = 7;
		blockDataToSpriteSheet[BLOCK_TNT] = 8;
		blockDataToSpriteSheet[BLOCK_BOOKSHELF] = 35;
		blockDataToSpriteSheet[BLOCK_MOSSY_COBBLESTONE] = (2*16)+4;
		blockDataToSpriteSheet[BLOCK_OBSIDIAN] = (2*16)+5;
		blockDataToSpriteSheet[BLOCK_TORCH] = 80;
		blockDataToSpriteSheet[BLOCK_FIRE] = 30;
		blockDataToSpriteSheet[BLOCK_MOB_SPAWNER] = TEXTURE_SPAWNER;
		blockDataToSpriteSheet[BLOCK_WOODEN_STAIRS] = 4;  // previously 55
		blockDataToSpriteSheet[BLOCK_CHEST] = 26;
		blockDataToSpriteSheet[BLOCK_REDSTONE_WIRE] = 100;
		blockDataToSpriteSheet[BLOCK_DIAMOND_ORE] = TEXTURE_DIAMOND;
		blockDataToSpriteSheet[BLOCK_DIAMOND_BLOCK] = 24;
		blockDataToSpriteSheet[BLOCK_WORKBENCH] = 25;
		blockDataToSpriteSheet[BLOCK_CROPS] = (5*16)+15;
		blockDataToSpriteSheet[BLOCK_SOIL] = (5*16)+6;
		blockDataToSpriteSheet[BLOCK_FURNACE] = 44;
		blockDataToSpriteSheet[BLOCK_BURNING_FURNACE] = (3*16)+13;
		blockDataToSpriteSheet[BLOCK_SIGNPOST] = 4;  // perhaps this is best left out actually		
		blockDataToSpriteSheet[BLOCK_WOODEN_DOOR] = (6*16)+1;
		blockDataToSpriteSheet[BLOCK_LADDER] = (5*16)+3;
		blockDataToSpriteSheet[BLOCK_MINECART_TRACKS] = (8*16);
		blockDataToSpriteSheet[BLOCK_COBBLESTONE_STAIRS] = 16;
		blockDataToSpriteSheet[BLOCK_WALL_SIGN] = 4;  // perhaps this is best left out actually		
		//blockDataToSpriteSheet[BLOCK_LEVER] = ;
	    blockDataToSpriteSheet[BLOCK_STONE_PRESSURE_PLATE] = 6;  // perhaps this is best left out actually
		blockDataToSpriteSheet[BLOCK_IRON_DOOR] = (6*16)+2;
		blockDataToSpriteSheet[BLOCK_WOODEN_PRESSURE_PLATE] = 4;  // perhaps this is best left out actually
		blockDataToSpriteSheet[BLOCK_REDSTONE_ORE] = TEXTURE_REDSTONE;
		blockDataToSpriteSheet[BLOCK_GLOWING_REDSTONE_ORE] = 51;
		blockDataToSpriteSheet[BLOCK_REDSTONE_TORCH_OFF] = (7*16)+3;
		blockDataToSpriteSheet[BLOCK_REDSTONE_TORCH_ON] = (6*16)+3;
		//blockDataToSpriteSheet[BLOCK_STONE_BUTTON] = ;
		blockDataToSpriteSheet[BLOCK_SNOW] = 66;
		blockDataToSpriteSheet[BLOCK_ICE] = 67;
		blockDataToSpriteSheet[BLOCK_SNOW_BLOCK] = 66;
		blockDataToSpriteSheet[BLOCK_CACTUS] = 70;
		blockDataToSpriteSheet[BLOCK_CLAY] = TEXTURE_CLAY;
		blockDataToSpriteSheet[BLOCK_REED] = 73;
		blockDataToSpriteSheet[BLOCK_JUKEBOX] = (4*16)+10;
		blockDataToSpriteSheet[BLOCK_FENCE] = 4;
		blockDataToSpriteSheet[BLOCK_PUMPKIN] = (7*16)+7;
		blockDataToSpriteSheet[BLOCK_NETHERSTONE] = (6*16)+7;
		blockDataToSpriteSheet[BLOCK_SLOW_SAND] = (6*16)+8;
		blockDataToSpriteSheet[BLOCK_LIGHTSTONE] = (6*16)+9;
		//blockDataToSpriteSheet[BLOCK_PORTAL] = ;
		blockDataToSpriteSheet[BLOCK_JACK_O_LANTERN] = (7*16)+8;
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
}
