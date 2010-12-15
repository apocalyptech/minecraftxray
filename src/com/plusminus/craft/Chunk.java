package com.plusminus.craft;

import org.lwjgl.opengl.GL11;

import com.plusminus.craft.dtf.ByteArrayTag;
import com.plusminus.craft.dtf.CompoundTag;
import com.plusminus.craft.dtf.IntTag;
import com.plusminus.craft.dtf.Tag;

import static com.plusminus.craft.MineCraftConstants.*;

public class Chunk {
	private int displayListNum;
	private int transparentListNum;
	private int selectedDisplayListNum;
	public int x;
	public int z;
	public boolean isDirty;
	public boolean isSelectedDirty;
	private CompoundTag chunkData;
	private ByteArrayTag blockData;
	private ByteArrayTag mapData;

	private MinecraftLevel level;
		
	public Chunk(Tag data, MinecraftLevel level) {
		this.chunkData = (CompoundTag) data;
		//System.out.println(this.chunkData);
		//System.exit(0);
		displayListNum = GL11.glGenLists(1);
		selectedDisplayListNum = GL11.glGenLists(1);
		transparentListNum = GL11.glGenLists(1);
		CompoundTag levelTag = (CompoundTag) chunkData.value.get(0); // first tag
		IntTag xPosTag = (IntTag) levelTag.getTagWithName("xPos");
		IntTag zPosTag = (IntTag) levelTag.getTagWithName("zPos");
		
		this.x = xPosTag.value;
		this.z = zPosTag.value;
		
		blockData = (ByteArrayTag) levelTag.getTagWithName("Blocks");
		mapData = (ByteArrayTag) levelTag.getTagWithName("Data");
		
		this.isDirty = true;
		this.isSelectedDirty = true;
		this.level = level;
		
		//System.out.println(data);
		//System.exit(0);
	}
	
	public CompoundTag getChunkData() {
		return this.chunkData;
	}
	
	public ByteArrayTag getMapData() {
		return this.blockData;
	}
	
	public void renderNorthSouth(int t, float x, float y, float z) {
		this.renderNorthSouth(t, x, y, z, 0.5f, 0.5f);
	}
	
	/**
	 * Render something which is a North/South face.
	 * 
	 * @param t Texture to render
	 * @param x
	 * @param y
	 * @param z
	 * @param yHeightOffset How tall this block is.  0.5f is the usual, specify 0 for half-height
	 * @param xzScale How large the rest of the block is.  0.5f is full-size, 0.1 would be tiny.
	 */
	public void renderNorthSouth(int t, float x, float y, float z, float yHeightOffset, float xzScale) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-xzScale, y+yHeightOffset, z+xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-xzScale, y+yHeightOffset, z-xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t],precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x-xzScale, y-xzScale, z+xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x-xzScale, y-xzScale, z-xzScale);
		GL11.glEnd();
	}
	
	
	/**
	 * Render something which is a North/South face.
	 * 
	 * @param t Texture to render
	 * @param x
	 * @param y
	 * @param z
	 */
	public void renderSignNorthSouth(int t, float x, float y, float z) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x, y+0.3f, z+0.4f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x, y+0.3f, z-0.4f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t],precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x, y-0.2f, z+0.4f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x, y-0.2f, z-0.4f);
		GL11.glEnd();
	}
	
	public void renderTopDown(int t, float x, float y, float z) {
		this.renderTopDown(t, x, y, z, 0.5f);
	}
	
	/**
	 * Render the top or bottom of a block, depending on how we're looking at it.
	 * 
	 * @param t The texture ID to draw
	 * @param x
	 * @param y
	 * @param z
	 * @param scale ".5" is a full-sized block, ".1" would be tiny.
	 */
	public void renderTopDown(int t, float x, float y, float z, float scale) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-scale, y-scale, z+scale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-scale, y-scale, z-scale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x+scale, y-scale, z+scale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x+scale, y-scale, z-scale);
		GL11.glEnd();
	}
	

	public void renderWestEast(int t, float x, float y, float z) {
		this.renderWestEast(t, x, y, z, 0.5f, 0.5f);
	}
	
	/**
	 * Renders something which is a West/East face.
	 * 
	 * @param t Texture to draw
	 * @param x
	 * @param y
	 * @param z
	 * @param yHeightOffset How tall this block is.  0.5f is the usual, specify 0 for half-height
	 * @param xzScale How large the rest of the block is.  0.5f is full-size, 0.1 would be tiny.
	 */
	public void renderWestEast(int t, float x, float y, float z, float yHeightOffset, float xzScale) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-xzScale, y+yHeightOffset, z-xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x+xzScale, y+yHeightOffset, z-xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x-xzScale, y-xzScale, z-xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x+xzScale, y-xzScale, z-xzScale);
		GL11.glEnd();
	}
	
	/**
	 * Renders a sign facing West or East
	 * TODO: Would be nice to just have a function where we can specify arbitrary scales per-axis.
	 * 
	 * @param t
	 * @param x
	 * @param y
	 * @param z
	 */
	public void renderSignWestEast(int t, float x, float y, float z) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-0.4f, y+0.3f, z);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x+0.4f, y+0.3f, z);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x-0.4f, y-0.2f, z);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x+0.4f, y-0.2f, z);
		GL11.glEnd();
	}
	
	/**
	 * Renders a somewhat-arbitrary vertical rectangle.  Pass in (x, z) pairs for the endpoints,
	 * and information about the height.
	 * 
	 * @param t Texture to draw
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param y	The lower part of the rectangle
	 * @param height Height of the rectangle.
	 */
	public void renderVertical(int t, float x1, float z1, float x2, float z2, float y, float height) {

		float bx = precalcSpriteSheetToTextureX[t];
		float by = precalcSpriteSheetToTextureY[t];

		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by);
			GL11.glVertex3f(x1, y+height, z1);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x2, y+height, z2);
	
			GL11.glTexCoord2f(bx, by+TEX16);
			GL11.glVertex3f(x1, y, z1);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX16);
			GL11.glVertex3f(x2, y, z2);
		GL11.glEnd();
	}
	
	/**
	 * Renders the side of a stair piece that runs East/West.  Verticies are in the following order:
	 * <pre>
	 *         6---5
	 *         |   |
	 *     2---4   |
	 *     |       |
	 *     1-------3
	 * </pre>
	 * 
	 * Note that the function is "WestEast" which corresponds to the stair direction;
	 * this will actually draw the face on the north or south sides.
	 * 
	 * @param t
	 * @param x
	 * @param y
	 * @param z
	 */
	public void renderStairSideWestEast(int t, float x, float y, float z, boolean swapZ) {
		
		float bx = precalcSpriteSheetToTextureX[t];
		float by = precalcSpriteSheetToTextureY[t];
		
		float zoff=0.5f;
		if (swapZ)
		{
			zoff = -0.5f;
		}
		
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		
			GL11.glTexCoord2f(bx, by+TEX16);
			GL11.glVertex3f(x-0.5f, y-0.5f, z+zoff);
	
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x-0.5f, y, z+zoff);
			
			GL11.glTexCoord2f(bx+TEX16, by+TEX16);
			GL11.glVertex3f(x-0.5f, y-0.5f, z-zoff);
	
			GL11.glTexCoord2f(bx+TEX32, by+TEX32);
			GL11.glVertex3f(x-0.5f, y, z);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x-0.5f, y+0.5f, z-zoff);
			
			GL11.glTexCoord2f(bx+TEX32, by);
			GL11.glVertex3f(x-0.5f, y+0.5f, z);

		GL11.glEnd();
	}	

	/**
	 * Renders the stair surface, for a stair running West/East
	 * 
	 * @param t Texture to draw
	 * @param x
	 * @param y
	 * @param z
	 * @param swapX
	 */
	public void renderStairSurfaceWestEast(int t, float x, float y, float z, boolean swapZ) {
		
		float bx = precalcSpriteSheetToTextureX[t];
		float by = precalcSpriteSheetToTextureY[t];
		
		float zoff = 0.5f;
		if (swapZ)
		{
			zoff = -0.5f;
		}
		
		// Lower Step surface
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by);
			GL11.glVertex3f(x+0.5f, y, z+zoff);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x-0.5f, y, z+zoff);
	
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x+0.5f, y, z);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x-0.5f, y, z);
		GL11.glEnd();

		// Lower Step Side
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x+0.5f, y, z+zoff);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x-0.5f, y, z+zoff);
	
			GL11.glTexCoord2f(bx,by+TEX16);
			GL11.glVertex3f(x+0.5f, y-0.5f, z+zoff);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX16);
			GL11.glVertex3f(x-0.5f, y-0.5f, z+zoff);
		GL11.glEnd();

		// Higher Step surface
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x+0.5f, y+0.5f, z);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x-0.5f, y+0.5f, z);
	
			GL11.glTexCoord2f(bx, by+TEX16);
			GL11.glVertex3f(x+0.5f, y+0.5f, z-zoff);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX16);
			GL11.glVertex3f(x-0.5f, y+0.5f, z-zoff);
		GL11.glEnd();

		// Higher Step Side
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by);
			GL11.glVertex3f(x+0.5f, y+0.5f, z);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x-0.5f, y+0.5f, z);
	
			GL11.glTexCoord2f(bx,by+TEX32);
			GL11.glVertex3f(x+0.5f, y, z);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x-0.5f, y, z);
		GL11.glEnd();
	}
	
	/**
	 * Renders the side of a stair piece that runs North/South.  Verticies are in the following order:
	 * <pre>
	 *         6---5
	 *         |   |
	 *     2---4   |
	 *     |       |
	 *     1-------3
	 * </pre>
	 * 
	 * Note that the function is "NorthSouth" which corresponds to the stair direction;
	 * this will actually draw the face on the east or west sides.
	 * 
	 * @param t
	 * @param x
	 * @param y
	 * @param z
	 */
	public void renderStairSideNorthSouth(int t, float x, float y, float z, boolean swapX) {
		
		float bx = precalcSpriteSheetToTextureX[t];
		float by = precalcSpriteSheetToTextureY[t];
		
		float xoff=0.5f;
		if (swapX)
		{
			xoff = -0.5f;
		}
		
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		
			GL11.glTexCoord2f(bx, by+TEX16);
			GL11.glVertex3f(x+xoff, y-0.5f, z-0.5f);
	
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x+xoff, y, z-0.5f);
			
			GL11.glTexCoord2f(bx+TEX16, by+TEX16);
			GL11.glVertex3f(x-xoff, y-0.5f, z-0.5f);
	
			GL11.glTexCoord2f(bx+TEX32, by+TEX32);
			GL11.glVertex3f(x, y, z-0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x-xoff, y+0.5f, z-0.5f);
			
			GL11.glTexCoord2f(bx+TEX32, by);
			GL11.glVertex3f(x, y+0.5f, z-0.5f);

		GL11.glEnd();
	}	

	/**
	 * Renders the stair surface, for a stair running North/South
	 * 
	 * @param t Texture to draw
	 * @param x
	 * @param y
	 * @param z
	 * @param swapX
	 */
	public void renderStairSurfaceNorthSouth(int t, float x, float y, float z, boolean swapX) {
		
		float bx = precalcSpriteSheetToTextureX[t];
		float by = precalcSpriteSheetToTextureY[t];
		
		float xoff = 0.5f;
		if (swapX)
		{
			xoff = -0.5f;
		}
		
		// Lower Step surface
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by);
			GL11.glVertex3f(x+xoff, y, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x+xoff, y, z-0.5f);
	
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x, y, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x, y, z-0.5f);
		GL11.glEnd();

		// Lower Step Side
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x+xoff, y, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x+xoff, y, z-0.5f);
	
			GL11.glTexCoord2f(bx,by+TEX16);
			GL11.glVertex3f(x+xoff, y-0.5f, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX16);
			GL11.glVertex3f(x+xoff, y-0.5f, z-0.5f);
		GL11.glEnd();

		// Higher Step surface
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x, y+0.5f, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x, y+0.5f, z-0.5f);
	
			GL11.glTexCoord2f(bx, by+TEX16);
			GL11.glVertex3f(x-xoff, y+0.5f, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX16);
			GL11.glVertex3f(x-xoff, y+0.5f, z-0.5f);
		GL11.glEnd();


		// Higher Step Side
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by);
			GL11.glVertex3f(x, y+0.5f, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x, y+0.5f, z-0.5f);
	
			GL11.glTexCoord2f(bx,by+TEX32);
			GL11.glVertex3f(x, y, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x, y, z-0.5f);
		GL11.glEnd();
	}
	
	public boolean isInRange(float x, float y, float maxDistance) {
		float realX = this.x*16;
		float realY = this.z*16;
		double distance = Math.sqrt(((x-realX) * (x-realX)) + ((y-realY) * (y-realY)));
		return distance < maxDistance;
	}
	
	public byte getBlock(int x, int y, int z) {
		return blockData.value[y + (z * 128) + (x * 128 * 16)];
	}

	public byte getData(int x, int y, int z) {
		int offset = y + (z * 128) + (x * 128 * 16);
		int halfOffset = offset / 2;
		if(offset % 2 == 0) {
			return (byte) (mapData.value[halfOffset] & 0xF);
		} else {
			return (byte) (mapData.value[halfOffset] >> 4);
		}
	}
	
	public boolean isSolid(byte i) {
		BLOCK_TYPE block_type = BLOCK_TYPE_MAP.get(i);
		if((i == 0) || (i == TYPE_WATER) || (i == TYPE_WATER_STATIONARY) ||
				(i==TYPE_LAVA) || (i==TYPE_LAVA_STATIONARY) ||
				(block_type != BLOCK_TYPE.NORMAL)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Renders a "special" block; AKA something that's not just an ordinary cube.
	 * Alters the geometry based on its input, which lets us do funky things to
	 * torches and the like.  Basically it draws four "faces" of the object.
	 * Note that the torch graphic also draws a little "cap" on the end.
	 * 
	 * @param bx Texture Beginning-X coordinate (inside the texture PNG)
	 * @param by Texture Beginning-Y coordinate
	 * @param ex Texture Ending-X coordinate
	 * @param ey Texture Ending-Y coordinate
	 * @param x Absolute X position of block
	 * @param y Absolute Y position of block
	 * @param z Absolute Z position of block
	 * @param yy Additional Y offset for rendering (this and the rest of the arguments are really
	 *           only used for torches; pass in 0 for just about everything else)
	 * @param x1 X offset for the top of the faces
	 * @param x2 X offset for the bottom of the faces
	 * @param z1 Z offset for the top of the faces
	 * @param z2 Z offset for the bottom of the faces
	 * @param torch Do we draw the "special" torch tips?
	 */
	public void renderSpecial(float bx, float by, float ex, float ey, float x, float y, float z, float yy, float x1, float x2, float z1, float z2, boolean torch) {
		 
		// GL11.glDisable(GL11.GL_CULL_FACE);
		 //GL11.glDisable(GL11.GL_DEPTH_TEST);
		 GL11.glBegin(GL11.GL_QUADS);
		 GL11.glNormal3f(1.0f, 0.0f, 0.0f);
		 GL11.glTexCoord2f(bx, by); 	GL11.glVertex3f(x+x1+9/16.0f+TEX64, y+yy+1.0f, 	z+z1);
		 GL11.glTexCoord2f(ex, by); 	GL11.glVertex3f(x+x1+9/16.0f+TEX64, y+yy+1.0f, 	z+z1+1.0f);
		 GL11.glTexCoord2f(ex, ey); 	GL11.glVertex3f(x+x2+9/16.0f-TEX64, y+yy, 		z+z2+1.0f);
		 GL11.glTexCoord2f(bx, ey); 	GL11.glVertex3f(x+x2+9/16.0f-TEX64, y+yy,	 	z+z2);
		
		 GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
		 GL11.glTexCoord2f(bx, by); 	GL11.glVertex3f(x+x1+7/16.0f+TEX64, y+yy+1.0f, 	z+z1+1.0f);
		 GL11.glTexCoord2f(ex, by); 	GL11.glVertex3f(x+x1+7/16.0f+TEX64, y+yy+1.0f, 	z+z1);
		 GL11.glTexCoord2f(ex, ey); 	GL11.glVertex3f(x+x2+7/16.0f-TEX64, y+yy, 		z+z2);
		 GL11.glTexCoord2f(bx, ey); 	GL11.glVertex3f(x+x2+7/16.0f-TEX64, y+yy, 		z+z2+1.0f);
		 
		 GL11.glNormal3f(0.0f, 0.0f, 1.0f);
		 GL11.glTexCoord2f(bx, by); 	GL11.glVertex3f(x+x1+1.0f, 	y+yy+1.0f, 	z+z1+9/16.0f+TEX64);
		 GL11.glTexCoord2f(ex, by); 	GL11.glVertex3f(x+x1, 		y+yy+1.0f, 	z+z1+9/16.0f+TEX64);
		 GL11.glTexCoord2f(ex, ey); 	GL11.glVertex3f(x+x2, 		y+yy, 		z+z2+9/16.0f-TEX64);
		 GL11.glTexCoord2f(bx, ey);	 	GL11.glVertex3f(x+x2+1.0f, 	y+yy, 		z+z2+9/16.0f-TEX64);
		 
		 GL11.glNormal3f(0.0f, 0.0f, -1.0f);
		 GL11.glTexCoord2f(bx, by); 	GL11.glVertex3f(x+x1, 		y+yy+1.0f, 	z+z1+7/16.0f+TEX64);
		 GL11.glTexCoord2f(ex, by); 	GL11.glVertex3f(x+x1+1.0f, 	y+yy+1.0f, 	z+z1+7/16.0f+TEX64);
		 GL11.glTexCoord2f(ex, ey); 	GL11.glVertex3f(x+x2+1.0f, 	y+yy, 		z+z2+7/16.0f-TEX64);
		 GL11.glTexCoord2f(bx, ey); 	GL11.glVertex3f(x+x2, 		y+yy, 		z+z2+7/16.0f-TEX64);
		
		 if (torch)
		 {
			 x1 *= 2.125f; z1 *= 2.125f;
			 GL11.glNormal3f(0.0f, 1.0f, 0.0f);
			 GL11.glTexCoord2f(bx+TEX128, by+TEX128); 	GL11.glVertex3f(x+x1+9/16.0f, y+yy+10/16.0f, z+z1+7/16.0f);
			 GL11.glTexCoord2f(ex-TEX128, by+TEX128); 	GL11.glVertex3f(x+x1+7/16.0f, y+yy+10/16.0f, z+z1+7/16.0f);
			 GL11.glTexCoord2f(ex-TEX128, ey-TEX32); 	GL11.glVertex3f(x+x1+7/16.0f, y+yy+10/16.0f, z+z1+9/16.0f);
			 GL11.glTexCoord2f(bx+TEX128, ey-TEX32); 	GL11.glVertex3f(x+x1+9/16.0f, y+yy+10/16.0f, z+z1+9/16.0f);
		 }
		 
		 GL11.glEnd();
		 //GL11.glEnable(GL11.GL_DEPTH_TEST);
		 //GL11.glEnable(GL11.GL_CULL_FACE);	
	}

	/**
	 * Renders a torch, making an attempt to render properly given the wall face it's
	 * attached to, etc.  We take in textureId because we support redstone torches as
	 * well.
	 *
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderTorch(int textureId, int xxx, int yyy, int zzz) {
		 float x1 = 0, x2 = 0, z1 = 0, z2 = 0, yy = 0;
		 byte data = getData(xxx, yyy, zzz);
		 switch (data) {
		 case 1:
			 x1 -= 0.125; x2 -= 0.5;
			 yy = 3/16.0f; break;
		 case 2:
			 x1 += 0.125; x2 += 0.5;
			 yy = 3/16.0f; break;
		 case 3:
			 z1 -= 0.125; z2 -= 0.5;
			 yy = 3/16.0f; break;
		 case 4:
			 z1 += 0.125; z2 += 0.5;
			 yy = 3/16.0f; break;
		 }
		 //Light(chunk, x, y, z);
		 float x = xxx + this.x*16 -0.5f;
		 float z = zzz + this.z*16 -0.5f;
		 float y = yyy - 0.5f;
		 /*
		  * 
		  *  RectangleF rect = new RectangleF(x / 16.0f + 0.00004f, y / 16.0f + 0.00004f,
1 / 16.0f - 0.00008f, 1 / 16.0f - 0.00008f);
		  */
		 
		 float bx,by;
		 float ex,ey;
		 
		 bx = precalcSpriteSheetToTextureX[textureId] + TEX64;
		 by = precalcSpriteSheetToTextureY[textureId];

		 ex = bx + TEX16-TEX32;
		 ey = by + TEX16;
		 
		 renderSpecial(bx, by, ex, ey, x, y, z, yy, x1, x2, z1, z2, true);
	}
	
	/***
	 * Render a "small" floor-based decoration, like a flower or a mushroom
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param block_type
	 */
	public void renderDecorationSmall(int textureId, int xxx, int yyy, int zzz) {
		 float x = xxx + this.x*16 -0.5f;
		 float z = zzz + this.z*16 -0.5f;
		 float y = yyy - 0.5f;
		 
		 float bx,by;
		 float ex,ey;
		 
		 bx = precalcSpriteSheetToTextureX[textureId] + TEX64;
		 by = precalcSpriteSheetToTextureY[textureId];
 		 ex = bx + TEX16-TEX32;
 		 ey = by + TEX16;

 		 renderSpecial(bx, by, ex, ey, x, y, z, 0, 0, 0, 0, 0, false);
	}
	
	/**
	 * Renders a "full" decoration, like reeds.  Something that takes up the whole square
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param block_type
	 */
	public void renderDecorationFull(int textureId, int xxx, int yyy, int zzz) {
		 float x = xxx + this.x*16 -0.5f;
		 float z = zzz + this.z*16 -0.5f;
		 float y = yyy - 0.5f;
		 
		 float bx,by;
		 float ex,ey;
		 
		 bx = precalcSpriteSheetToTextureX[textureId];
		 by = precalcSpriteSheetToTextureY[textureId];
		 ex = bx + TEX16;
		 ey = by + TEX16;

		 renderSpecial(bx, by, ex, ey, x, y, z, 0, 0, 0, 0, 0, false);
	}
	
	/**
	 * Renders crops.  We still take the fully-grown textureId in the function so that everything
	 * remains defined in MineCraftConstants
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderCrops(int textureId, int xxx, int yyy, int zzz) {
		 float x = xxx + this.x*16 -0.5f;
		 float z = zzz + this.z*16 -0.5f;
		 float y = yyy - 0.5f;
		 
		 float bx,by;
		 float ex,ey;

		 bx = precalcSpriteSheetToTextureX[textureId];
		 by = precalcSpriteSheetToTextureY[textureId];

		 // Adjust for crop size; fortunately the textures are all in the same row so it's easy.
		 byte data = getData(xxx, yyy, zzz);
		 bx -= TEX16 * (7-data);
		 
		 ex = bx + TEX16;
		 ey = by + TEX16;
		 
		 renderSpecial(bx, by, ex, ey, x, y, z, 0, 0, 0, 0, 0, false);
	}
    
	/**
	 * Renders a ladder, given its attached-side data.  We still take in textureId just so
	 * that everything's still defined in MineCraftConstants
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderLadder(int textureId, int xxx, int yyy, int zzz) {
		 float x = xxx + this.x*16;
		 float z = zzz + this.z*16;
		 float y = yyy;
		 
		 byte data = getData(xxx, yyy, zzz);
		 switch(data)
		 {
		 	case 2:
		 		// East
		 		this.renderWestEast(textureId, x, y, z+1.0f-TEX64);
		 		break;
		 	case 3:
		 		// West
		 		this.renderWestEast(textureId, x, y, z+TEX64);
		 		break;
		 	case 4:
		 		// North
		 		this.renderNorthSouth(textureId, x+1.0f-TEX64, y, z);
		 		break;
		 	case 5:
	 		default:
	 			// South
				this.renderNorthSouth(textureId, x+TEX64, y, z);
	 			break;
		 }
	}

	/**
	 * This is actually used for rendering "decoration" type things which are on
	 * the floor (eg: minecart tracks, redstone wires, etc)
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderFloor(int textureId, int xxx, int yyy, int zzz) {
		 float x = xxx + this.x*16;
		 float z = zzz + this.z*16;
		 float y = yyy;
		 
		this.renderTopDown(textureId, x, y+TEX64, z);
	}

	/**
	 * Renders a floor button.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderFloorButton(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		 
		this.renderTopDown(textureId, x, y+TEX64, z, 0.4f);
	}
	
	/**
	 * Renders a door
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderDoor(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		
		byte data = getData(xxx, yyy, zzz);
		if ((data & 0x8) == 0x8)
		{
			textureId -= 16;
		}
		boolean swung = false;
		if ((data & 0x4) == 0x4)
		{
			swung = true;
		}
		int dir = (data & 0x3);

		// TODO: need to fix texture orientation
		if ((dir == 3 && swung) || (dir == 0 && !swung))
		{
			// North			
			this.renderNorthSouth(textureId, x, y, z);
		}
		else if ((dir == 0 && swung) || (dir == 1 && !swung))
		{
			// East
			this.renderWestEast(textureId, x, y, z);
		}
		else if ((dir == 1 && swung) || (dir == 2 && !swung))
		{
			// South
			this.renderNorthSouth(textureId, x+1, y, z);
		}
		else
		{
			// West
			this.renderWestEast(textureId, x, y, z+1);
		}
		
	}
	
	/**
	 * Renders stair graphics
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderStairs(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		
		byte data = getData(xxx, yyy, zzz);
		boolean swap = false;
		if (data == 0 || data == 2)
		{
			swap = true;
		}

		if (data == 0 || data == 1)
		{
			// 0 is ascending-south, 1 is ascending-north
			
			// Sides
			this.renderStairSideNorthSouth(textureId, x, y, z+.05f, swap);
			this.renderStairSideNorthSouth(textureId, x, y, z+.95f, swap);
			
			// Back
			if (swap)
			{
				this.renderNorthSouth(textureId, x+0.94f, y, z, 0.5f, 0.45f);
			}
			else
			{
				this.renderNorthSouth(textureId, x+0.06f, y, z, 0.5f, 0.45f);
			}
			
			// Bottom
			this.renderTopDown(textureId, x, y, z, 0.45f);
			
			// Stair Surface
			this.renderStairSurfaceNorthSouth(textureId, x, y, z, swap);
		}
		else
		{
			// 2 is ascending-west, 3 is ascending-east
			
			// Sides
			this.renderStairSideWestEast(textureId, x+.05f, y, z, swap);
			this.renderStairSideWestEast(textureId, x+.95f, y, z, swap);
			
			// Back
			if (swap)
			{
				this.renderWestEast(textureId, x, y, z+0.94f, 0.5f, 0.45f);
			}
			else
			{
				this.renderWestEast(textureId, x, y, z+0.06f, 0.5f, 0.45f);
			}
			
			// Bottom
			this.renderTopDown(textureId, x, y, z, 0.45f);
			
			// Stair Surface
			this.renderStairSurfaceWestEast(textureId, x, y, z, swap);		
		}
		
	}
	
	/**
	 * Renders a signpost.
	 * TODO: show the actual message
	 * TODO: should be solid instead of just one plane
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderSignpost(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		
		float signBottom = 0f;
		float signHeight = .6f;
		float postRadius = .05f;
		
		// First a signpost
		this.renderVertical(textureId, x-postRadius, z-postRadius, x+postRadius, z-postRadius, y-0.5f, 0.5f+signBottom);
		this.renderVertical(textureId, x-postRadius, z+postRadius, x+postRadius, z+postRadius, y-0.5f, 0.5f+signBottom);
		this.renderVertical(textureId, x+postRadius, z-postRadius, x+postRadius, z+postRadius, y-0.5f, 0.5f+signBottom);
		this.renderVertical(textureId, x-postRadius, z+postRadius, x-postRadius, z-postRadius, y-0.5f, 0.5f+signBottom);
		
		// Now we continue to draw the sign itself.
		byte data = getData(xxx, yyy, zzz);
		data &= 0xF;
		// data: 0 is West, increasing numbers add 22.5 degrees (so 4 is North, 8 south, etc)
		// Because we're not actually drawing the message (yet), as far as we're concerned
		// West is the same as East, etc.
		float angle = (data % 8) * 22.5f;
		float radius = 0.5f;

		// First x/z
		float x1 = x + radius * (float)Math.cos(Math.toRadians(angle));
		float z1 = z + radius * (float)Math.sin(Math.toRadians(angle));
		
		// Now the other side
		angle -= 180;
		float x2 = x + radius * (float)Math.cos(Math.toRadians(angle));
		float z2 = z + radius * (float)Math.sin(Math.toRadians(angle));
		
		this.renderVertical(textureId, x1, z1, x2, z2, y+signBottom, signHeight);
		
	}
	
	/**
	 * Renders a wall sign.  This is virtually identical to renderLadder, except that
	 * we draw a smaller box, basically.
	 * TODO: Would be kind of neat to actually draw the message, too.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderWallSign(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		
		byte data = getData(xxx, yyy, zzz);
		switch(data)
		{
		 	case 2:
		 		// East
		 		this.renderSignWestEast(textureId, x, y, z+0.45f);
		 		break;
		 	case 3:
		 		// West
		 		this.renderSignWestEast(textureId, x, y, z-0.45f);
		 		break;
		 	case 4:
		 		// North
		 		this.renderSignNorthSouth(textureId, x+0.45f, y, z);
		 		break;
		 	case 5:
	 		default:
	 			// South
				this.renderSignNorthSouth(textureId, x-0.45f, y, z);
	 			break;
		}
	}
	
	public boolean checkSolid(byte block, boolean transpararency) {
		if(block == 0) {
			return true;
		}
		return isSolid(block) == transpararency;
	}
	
	public void renderWorld(boolean transparency, boolean render_bedrock, boolean onlySelected, boolean[] selectedMap) {
		float worldX = this.x*16;
		float worldZ = this.z*16;
		for(int x=0;x<16;x++) {
			int xOff = (x * 128 * 16);
			for(int z=0;z<16;z++) {
				int zOff = (z * 128);
				int blockOffset = zOff + xOff-1;
				for(int y=0;y<128;y++) {
					blockOffset++;
					byte t = blockData.value[blockOffset];
					
					if(t == 0) {
						continue;
					}
					
					/*if(t == TYPE_TORCH) {
						//System.out.println("Torch: " + getData(x,y,z));
					}*/
					
					int textureId = blockDataToSpriteSheet[t];
					
					if(textureId == -1) {
						//System.out.println("unknown block type: " + t);
						continue;
					}
					
					if(transparency && isSolid(t)) {
						continue;
					}
					if(!transparency && !isSolid(t)) {
						continue;
					}
					//if(!transparency && !isSolid(t)) {
					//	continue;
					//}
					
					boolean draw = false;
					
					if(!onlySelected) {
						boolean above = true;
						boolean below = true;
						boolean left = true;
						boolean right = true;
						boolean near = true;
						boolean far = true;

						if (render_bedrock && t == MineCraftConstants.BLOCK_BEDROCK)
						{
							// This block of code was more or less copied/modified directly from the "else" block
							// below - should see if there's a way we can abstract this instead.  Also, I suspect
							// that this is where we'd fix water rendering...
							
							// check above
							if(y<127 && blockData.value[blockOffset+1] != MineCraftConstants.BLOCK_BEDROCK) {
								draw = true;
								above = false;
							}
							
							// check below
							if(y>0 && blockData.value[blockOffset-1] != MineCraftConstants.BLOCK_BEDROCK) {
								draw = true;
								below = false;
							}
							
							// check left;
							if(x>0 && blockData.value[blockOffset-BLOCKSPERCOLUMN] != MineCraftConstants.BLOCK_BEDROCK) {
								draw = true;
								left = false;
							} else if(x==0 && this.x > -63) {
								Chunk leftChunk = level.getChunk(this.x-1, this.z);
								if(leftChunk != null && leftChunk.getBlock(15, y, z) != MineCraftConstants.BLOCK_BEDROCK) {
									draw = true;
									left = false;
								}
							}
						
							// check right
							if(x<15 && blockData.value[blockOffset+BLOCKSPERCOLUMN] != MineCraftConstants.BLOCK_BEDROCK) {
								draw = true;
								right = false;
							} else if(x==15 && this.x < 63) {
								Chunk rightChunk = level.getChunk(this.x+1,this.z);
								if(rightChunk != null && rightChunk.getBlock(0, y, z) != MineCraftConstants.BLOCK_BEDROCK) {
									draw = true;
									right = false;
								}
							}
							
							// check near
							if(z>0 && blockData.value[blockOffset-BLOCKSPERROW] != MineCraftConstants.BLOCK_BEDROCK) {
								draw = true;
								near = false;
							} else if(z==0 && this.z > -63) {
								Chunk nearChunk = level.getChunk(this.x,this.z-1);
								if(nearChunk != null && nearChunk.getBlock(x, y, 15) != MineCraftConstants.BLOCK_BEDROCK) {
									draw = true;
									near = false;
								}
							}
							
							// check far
							if(z<15 && blockData.value[blockOffset+BLOCKSPERROW] != MineCraftConstants.BLOCK_BEDROCK) {
								draw = true;
								far = false;
							} else if(z==15 && this.z < 63) {
								Chunk farChunk = level.getChunk(this.x,this.z+1);
								if(farChunk != null && farChunk.getBlock(x, y, 0) != MineCraftConstants.BLOCK_BEDROCK) {
									draw = true;
									far = false;
								}
							}
						}
						else
						{
							// check above
							if(y<127 && checkSolid(blockData.value[blockOffset+1], transparency)) {
								draw = true;
								above = false;
							}
							
							// check below
							if(y>0 && checkSolid(blockData.value[blockOffset-1], transparency)) {
								draw = true;
								below = false;
							}
							
							// check left;
							if(x>0 && checkSolid(blockData.value[blockOffset-BLOCKSPERCOLUMN], transparency)) {
								draw = true;
								left = false;
							} else if(x==0 && this.x > -63) {
								Chunk leftChunk = level.getChunk(this.x-1, this.z);
								if(leftChunk != null && checkSolid(leftChunk.getBlock(15, y, z), transparency)) {
									draw = true;
									left = false;
								}
							}
						
							// check right
							if(x<15 && checkSolid(blockData.value[blockOffset+BLOCKSPERCOLUMN], transparency)) {
								draw = true;
								right = false;
							} else if(x==15 && this.x < 63) {
								Chunk rightChunk = level.getChunk(this.x+1,this.z);
								if(rightChunk != null && checkSolid(rightChunk.getBlock(0, y, z), transparency)) {
									draw = true;
									right = false;
								}
							}
							
							// check near
							if(z>0 && checkSolid(blockData.value[blockOffset-BLOCKSPERROW], transparency)) {
								draw = true;
								near = false;
							} else if(z==0 && this.z > -63) {
								Chunk nearChunk = level.getChunk(this.x,this.z-1);
								if(nearChunk != null && checkSolid(nearChunk.getBlock(x, y, 15), transparency)) {
									draw = true;
									near = false;
								}
							}
							
							// check far
							if(z<15 && checkSolid(blockData.value[blockOffset+BLOCKSPERROW], transparency)) {
								draw = true;
								far = false;
							} else if(z==15 && this.z < 63) {
								Chunk farChunk = level.getChunk(this.x,this.z+1);
								if(farChunk != null && checkSolid(farChunk.getBlock(x, y, 0), transparency)) {
									draw = true;
									far = false;
								}
							}
						}
						
						switch(BLOCK_TYPE_MAP.get(t))
						{
							case TORCH:
								renderTorch(textureId,x,y,z);
								break;
							case DECORATION_SMALL:
								renderDecorationSmall(textureId,x,y,z);
								break;
							case DECORATION_FULL:
								renderDecorationFull(textureId,x,y,z);
								break;
							case CROPS:
								renderCrops(textureId,x,y,z);
								break;
							case LADDER:
								renderLadder(textureId,x,y,z);
								break;
							case FLOOR:
								renderFloor(textureId,x,y,z);
								break;
							case FLOOR_BUTTON:
								renderFloorButton(textureId,x,y,z);
								break;
							case DOOR:
								renderDoor(textureId,x,y,z);
								break;
							case STAIRS:
								renderStairs(textureId,x,y,z);
								break;
							case SIGNPOST:
								renderSignpost(textureId,x,y,z);
								break;
							case WALLSIGN:
								renderWallSign(textureId,x,y,z);
								break;
							case HALFHEIGHT:
								// TODO: these (and other non-"block" things) seem to disappear behind glass
								if(draw) {
									if(!near) this.renderWestEast(textureId, worldX+x, y, worldZ+z, 0f, .495f);
									if(!far) this.renderWestEast(textureId, worldX+x, y, worldZ+z+1, 0f, .495f);
									
									if(!below) this.renderTopDown(textureId, worldX+x, y, worldZ+z);
									this.renderTopDown(textureId, worldX+x, y+0.5f, worldZ+z);	
									
									if(!left) this.renderNorthSouth(textureId, worldX+x, y, worldZ+z, 0f, .495f);
									if(!right) this.renderNorthSouth(textureId, worldX+x+1, y, worldZ+z, 0f, .495f);
								}
								break;
							default:
								// if we have to draw this block
								if(draw) {
									if(!near) this.renderWestEast(textureId, worldX+x, y, worldZ+z);
									if(!far) this.renderWestEast(textureId, worldX+x, y, worldZ+z+1);
									
									if(!below) this.renderTopDown(textureId, worldX+x, y, worldZ+z);
									if(!above) this.renderTopDown(textureId, worldX+x, y+1, worldZ+z);	
									
									if(!left) this.renderNorthSouth(textureId, worldX+x, y, worldZ+z);
									if(!right) this.renderNorthSouth(textureId, worldX+x+1, y, worldZ+z);
								}
						}
					} else {
						draw = false;
						for(int i=0;i<selectedMap.length;i++) {
							if(selectedMap[i] && HIGHLIGHT_ORES[i] == t) {
								draw = true;
								break;
							}
						}
						if(draw) {
							this.renderWestEast(textureId, worldX+x, y, worldZ+z);
							this.renderWestEast(textureId, worldX+x, y, worldZ+z+1);
							
							this.renderTopDown(textureId, worldX+x, y, worldZ+z);
							this.renderTopDown(textureId, worldX+x, y+1, worldZ+z);	
							
							this.renderNorthSouth(textureId, worldX+x, y, worldZ+z);
							this.renderNorthSouth(textureId, worldX+x+1, y, worldZ+z);
						}
					}
					
				}
			}
		}
	}
	
	public void renderSolid(boolean render_bedrock) {
		if(isDirty) {
				GL11.glNewList(this.displayListNum, GL11.GL_COMPILE);
				renderWorld(false, render_bedrock, false, null);
				GL11.glEndList();
				GL11.glNewList(this.transparentListNum, GL11.GL_COMPILE);
				renderWorld(true, false, false, null);
				GL11.glEndList();
				this.isDirty = false;
		}
		GL11.glCallList(this.displayListNum);
	}
	
	public void renderTransparency() {
		GL11.glCallList(this.transparentListNum);
	}
	
	public void renderSelected(boolean[] selectedMap) {
		if(isSelectedDirty) {
			GL11.glNewList(this.selectedDisplayListNum, GL11.GL_COMPILE);
			renderWorld(false, false, true, selectedMap);
			GL11.glEndList();
			this.isSelectedDirty = false;
		}
		GL11.glCallList(this.selectedDisplayListNum);
	}
}
