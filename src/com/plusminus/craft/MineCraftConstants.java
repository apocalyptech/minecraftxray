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
    public static final Font ARIALFONT = new Font("Arial", Font.BOLD, 16);
	
    // some convenience statics regarding time calculation
	public static final long NANOSPERSECOND 	= 1000000000;
	public static final long MILLISPERSECOND 	= 1000;
	public static final long NANOSPERMILLIS 	= NANOSPERSECOND / MILLISPERSECOND;
	
	public static final int TEXTURE_COAL = (2*16) + 2;
	public static final int TEXTURE_IRON = (2*16) + 1;
	public static final int TEXTURE_GOLD = (2*16) + 0;
	public static final int TEXTURE_DIAMOND = (3*16) + 2;
	public static final int TEXTURE_REDSTONE = (3*16) + 3;
	public static final int TEXTURE_SPAWNER = (4*16) + 1;
	public static final int[] TEXTURE_ORES = new int[] {TEXTURE_COAL, TEXTURE_IRON, TEXTURE_GOLD, TEXTURE_DIAMOND, TEXTURE_REDSTONE, TEXTURE_SPAWNER};
	public static final String[] ORES_DESCRIPTION = new String[] {"Coal", "Iron", "Gold", "Diamond", "Redstone", "Spawner"};
	
	public static final int TEXTURE_WATER = 207;
	public static final int TEXTURE_TORCH = 5*16;
	
	public static final int TYPE_WATER = 8;
	public static final int TYPE_WATER_STATIONARY = 9;
	public static final int TYPE_LAVA = 10;
	public static final int TYPE_LAVA_STATIONARY = 11;
	public static final int TYPE_TORCH = 50;
	
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
		blockColors[1] = new Color(120,120,120);
		blockColors[2] = new Color(117,176,73);
		blockColors[3] = new Color(134,96,67);
		blockColors[4] = new Color(115,115,115);
		blockColors[48] = new Color(115,115,115);
		blockColors[5] = new  Color(157,128,79);
		blockColors[6] = new Color(120,120,120);
		blockColors[7] = new Color(84,84,84);
		blockColors[8] = new Color(38,92,255);
		blockColors[9] = new  Color(38,92,255);
		blockColors[10] = new Color(255,90,0);
		blockColors[11] = new Color(255,90,0);
		blockColors[12] = new Color(218,210,158);
		blockColors[13] = new Color(136,126,126);
		blockColors[14] = new Color(143,140,125);
		blockColors[15] = new Color(136,130,127);
		blockColors[16] = new Color(115,115,115);
		blockColors[17] = new Color(102,81,51);
		blockColors[18] = new Color(60,192,41);
		blockColors[20] = new Color(255,255,255); //glass
		blockColors[35] = new Color(222,222,222); //Color(143,143,143,255); 
		blockColors[38] = new Color(255,0,0);
		blockColors[37] = new Color(255,255,0);
		blockColors[41] = new Color(231,165,45);
		blockColors[42] = new Color(191,191,191);
		blockColors[43] = new Color(200,200,200);
		blockColors[44] = new Color(200,200,200);
		blockColors[45] = new Color(170,86,62);
		blockColors[46] = new Color(160,83,65);
		blockColors[49] = new Color(26,11,43);
		blockColors[50] = new Color(245,220,50);
		blockColors[51] = new Color(255,170,30);
		//blockColors[52] = Color(245,220,50,255); unnecessary afaik
		blockColors[53] = new Color(157,128,79);
		blockColors[54] = new Color(125,91,38);
		//blockColors[55] = Color(245,220,50,255); unnecessary afaik
		blockColors[56] = new Color(129,140,143);
		blockColors[57] = new Color(45,166,152);
		blockColors[58] = new Color(114,88,56);
		blockColors[59] = new Color(146,192,0);
		blockColors[60] = new Color(95,58,30);
		blockColors[61] = new Color(96,96,96);
		blockColors[62] = new Color(96,96,96);
		blockColors[63] = new Color(111,91,54);
		blockColors[64] = new Color(136,109,67);
		blockColors[65] = new Color(181,140,64);
		blockColors[66] = new Color(150,134,102);
		blockColors[67] = new Color(115,115,115);
		blockColors[71] = new Color(191,191,191);
		blockColors[73] = new Color(131,107,107);
		blockColors[74] = new Color(131,107,107);
		blockColors[75] = new Color(181,140,64);
		blockColors[76] = new Color(255,0,0);
		blockColors[78] = new Color(255,255,255);
		blockColors[79] = new Color(83,113,163);
		blockColors[80] = new Color(250,250,250);
		blockColors[81] = new Color(25,120,25);
		blockColors[82] = new Color(151,157,169);
		blockColors[83] = new Color(193,234,150);
		blockColors[83] = new Color(100,67,50);
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
		blockDataToSpriteSheet[1] = 1;
		blockDataToSpriteSheet[2] = 0;
		blockDataToSpriteSheet[3] = 2;
		blockDataToSpriteSheet[4] = 16;
		blockDataToSpriteSheet[5] = 4;
		blockDataToSpriteSheet[6] = 15;
		blockDataToSpriteSheet[7] = 49;
		blockDataToSpriteSheet[8] = 207;
		blockDataToSpriteSheet[9] = 207;
		blockDataToSpriteSheet[10] = 239;
		blockDataToSpriteSheet[11] = 239;
		blockDataToSpriteSheet[12] = 18;
		blockDataToSpriteSheet[13] = 19;
		blockDataToSpriteSheet[14] = 32;
		blockDataToSpriteSheet[15] = 33;
		blockDataToSpriteSheet[16] = 34;
		blockDataToSpriteSheet[17] = 20;
		blockDataToSpriteSheet[18] = 53;
		blockDataToSpriteSheet[19] = 52;
		blockDataToSpriteSheet[20] = 49;
		blockDataToSpriteSheet[37] = 13;
		blockDataToSpriteSheet[38] = 12;
		blockDataToSpriteSheet[39] = (1*16) + 13;
		blockDataToSpriteSheet[40] = (1*16) + 12;
		blockDataToSpriteSheet[41] = 23;
		blockDataToSpriteSheet[42] = 22;
		blockDataToSpriteSheet[43] = 72;
		blockDataToSpriteSheet[44] = 72;
		blockDataToSpriteSheet[45] = 7;
		blockDataToSpriteSheet[46] = 8;
		blockDataToSpriteSheet[47] = 35;
		blockDataToSpriteSheet[48] = 16;
		blockDataToSpriteSheet[49] = 49;
		blockDataToSpriteSheet[50] = 80;
		blockDataToSpriteSheet[51] = 30;
		blockDataToSpriteSheet[52] = 65;
		blockDataToSpriteSheet[53] = 55;
		blockDataToSpriteSheet[54] = 26;
		blockDataToSpriteSheet[55] = 100;
		blockDataToSpriteSheet[56] = 50;
		blockDataToSpriteSheet[57] = 24;
		blockDataToSpriteSheet[58] = 25;
		blockDataToSpriteSheet[59] = 12;
		blockDataToSpriteSheet[60] = 19;
		blockDataToSpriteSheet[61] = 44;
		blockDataToSpriteSheet[62] = 44;
		//blockDataToSpriteSheet[67] = cobblestone stairs
		//blockDataToSpriteSheet[64] = wooden door
		// blockDataToSpriteSheet[65] = ladder
		//blockDataToSpriteSheet[70] = stone pressure plate
		blockDataToSpriteSheet[73] = 51;
		blockDataToSpriteSheet[74] = 51;
		blockDataToSpriteSheet[78] = 66;
		blockDataToSpriteSheet[79] = 67;
		blockDataToSpriteSheet[80] = 66;
		blockDataToSpriteSheet[81] = 70;
		blockDataToSpriteSheet[82] = 87;
		blockDataToSpriteSheet[83] = 73;
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
