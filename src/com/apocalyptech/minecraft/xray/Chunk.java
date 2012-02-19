/**
 * Copyright (c) 2010-2012, Vincent Vollers and Christopher J. Kucera
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

import java.lang.Math;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import org.lwjgl.opengl.GL11;

import com.apocalyptech.minecraft.xray.dtf.ShortArrayTag;
import com.apocalyptech.minecraft.xray.dtf.ByteArrayTag;
import com.apocalyptech.minecraft.xray.dtf.CompoundTag;
import com.apocalyptech.minecraft.xray.dtf.StringTag;
import com.apocalyptech.minecraft.xray.dtf.ListTag;
import com.apocalyptech.minecraft.xray.dtf.IntTag;
import com.apocalyptech.minecraft.xray.dtf.Tag;

import static com.apocalyptech.minecraft.xray.MinecraftConstants.*;

/**
 * Chunk functions, including the meat of our rendering stuffs
 *
 * TODO: There are a lot of functions that do very similar things in here, it would be
 * good to consolidate some of those.  I don't know why it took me so long to come
 * up with the current implementation of renderVertical and renderHorizontal - I suspect
 * that much of the rendering code would be improved by moving to those if possible.
 */
public abstract class Chunk {
	private HashMap<Integer, Integer> displayListNums;
	private HashMap<Integer, Integer> nonstandardListNums;
	private HashMap<Integer, Integer> glassListNums;
	private HashMap<Integer, Integer> selectedDisplayListNums;
	public int x;
	public int z;
	public HashMap<Integer, Boolean> isDirty;
	public HashMap<Integer, Boolean> isSelectedDirty;
	public boolean isOnMinimap;
	private CompoundTag chunkData;
	private ArrayList<PaintingEntity> paintings;
	/* TODO: Make sure that storing levelTag like this isn't chewing up memory. */
	protected CompoundTag levelTag;

	// These are vars used while looping over our set of blocks
	protected int lx, ly, lz;
	protected int lOffset;
	
	protected MinecraftLevel level;
	public boolean willSpawnSlimes;

	public HashMap<Integer, Boolean> usedTextureSheets;

	private final float fence_postsize = .125f;
	private final float fence_postsize_h = fence_postsize/2f;
	private final float fence_slat_height = .1875f;
	private final float fence_top_slat_offset = .375f;
	private final float fence_slat_start_offset = -.125f;

	private static enum RENDER_PASS {
		SOLIDS,
		NONSTANDARD,
		GLASS,
		SELECTED
	}

	private static enum SOLID_PASS {
		TOP,
		BOTTOM,
		EASTWEST,
		NORTHSOUTH
	}
	
	public Chunk(MinecraftLevel level, Tag data) {
		
		this.level = level;
		this.chunkData = (CompoundTag) data;
		this.isOnMinimap = false;

		this.levelTag = (CompoundTag) chunkData.value.get(0); // first tag
		IntTag xPosTag = (IntTag) levelTag.getTagWithName("xPos");
		IntTag zPosTag = (IntTag) levelTag.getTagWithName("zPos");
		
		paintings = new ArrayList<PaintingEntity>();
		ListTag entities = (ListTag)levelTag.getTagWithName("Entities");
		StringTag entity_id;
		CompoundTag ct;
		for (Tag t : entities.value)
		{
			ct = (CompoundTag)t;
			entity_id = (StringTag) ct.getTagWithName("id");
			if (entity_id.value.equalsIgnoreCase("painting"))
			{
				paintings.add(new PaintingEntity(ct));
			}
		}
		
		this.x = xPosTag.value;
		this.z = zPosTag.value;

		// Lastly, compute whether or not we'll spawn slimes, based on the randomSeed
		// Taken from http://www.minecraftforum.net/topic/397835-find-slime-spawning-chunks/
		// Or, indirectly from http://www.minecraftwiki.net/wiki/Slime#Spawning
		Random rnd = new Random(level.getRandomSeed() +
				(long) (this.x * this.x * 0x4c1906) +
				(long) (this.x * 0x5ac0db) +
				(long) (this.z * this.z) * 0x4307a7L +
				(long) (this.z * 0x5f24f) ^ 0x3ad8025f);
		this.willSpawnSlimes = (rnd.nextInt(10) == 0);
	}

	/**
	 * Tasks in the constructor which need to be done after the implementing class has
	 * finished loading in their chunks.
	 */
	protected void finishConstructor()
	{
		// Compute which texture sheets are in-use by this chunk
		// Much of this is copied from our main render loop, way down below
		this.usedTextureSheets = new HashMap<Integer, Boolean>();
		this.rewindLoop();
		int t = 0;
		while (t != -2)
		{
			t = this.nextBlock();
			if(t < 1) {
				continue;
			}
			BlockType block = blockArray[t];
			if (block == null)
			{
				block = BLOCK_UNKNOWN;
			}
			this.usedTextureSheets.put(block.getTexSheet(), true);
		}

		// And create all our necessary GL Lists (and other structures)
		displayListNums = new HashMap<Integer, Integer>();
		nonstandardListNums = new HashMap<Integer, Integer>();
		glassListNums = new HashMap<Integer, Integer>();
		selectedDisplayListNums = new HashMap<Integer, Integer>();
		this.isDirty = new HashMap<Integer, Boolean>();
		this.isSelectedDirty = new HashMap<Integer, Boolean>();
		for (int sheet : this.usedTextureSheets.keySet())
		{
			displayListNums.put(sheet, GL11.glGenLists(1));
			selectedDisplayListNums.put(sheet, GL11.glGenLists(1));
			glassListNums.put(sheet, GL11.glGenLists(1));
			nonstandardListNums.put(sheet, GL11.glGenLists(1));
			this.isDirty.put(sheet, true);
			this.isSelectedDirty.put(sheet, true);
		}
	}

	/**
	 * Returns whether or not this chunk contains blocks which use the specified sheet
	 */
	public boolean usesSheet(int sheet)
	{
		return this.usedTextureSheets.containsKey(sheet);
	}

	/**
	 * Marks a our lists as dirty
	 */
	public void setDirty()
	{
		for (Map.Entry<Integer, Boolean> entry : this.isDirty.entrySet())
		{
			entry.setValue(true);
		}
	}

	/**
	 * Marks a our selected lists as dirty
	 */
	public void setSelectedDirty()
	{
		for (Map.Entry<Integer, Boolean> entry : this.isSelectedDirty.entrySet())
		{
			entry.setValue(true);
		}
	}
	
	/**
	 * Will return an array of values which are suitable for feeding into a
	 * minimap.
	 */
	public abstract short[][] getMinimapValues();

	public CompoundTag getChunkData() {
		return this.chunkData;
	}

	/**
	 * Gets the Block ID of the block immediately to the west.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	protected abstract short getAdjWestBlockId(int x, int y, int z, int blockOffset);

	/**
	 * Gets the data value of the block immediately to the west.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	private byte getAdjWestBlockData(int x, int y, int z)
	{
		if (x > 0)
		{
			return getData(x-1, y, z);
		}
		else
		{
			Chunk otherChunk = level.getChunk(this.x-1, this.z);
			if (otherChunk == null)
			{
				return -1;
			}
			else
			{
				return otherChunk.getData(15, y, z);
			}
		}
	}

	/**
	 * Gets the Block ID of the block immediately to the east.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	protected abstract short getAdjEastBlockId(int x, int y, int z, int blockOffset);

	/**
	 * Gets the data value of the block immediately to the east.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	private byte getAdjEastBlockData(int x, int y, int z)
	{
		if (x < 15)
		{
			return getData(x+1, y, z);
		}
		else
		{
			Chunk otherChunk = level.getChunk(this.x+1, this.z);
			if (otherChunk == null)
			{
				return -1;
			}
			else
			{
				return otherChunk.getData(0, y, z);
			}
		}
	}

	/**
	 * Gets the Block ID of the block immediately to the south.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	protected abstract short getAdjNorthBlockId(int x, int y, int z, int blockOffset);

	/**
	 * Gets the data value of the block immediately to the south.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	private byte getAdjNorthBlockData(int x, int y, int z)
	{
		if (z > 0)
		{
			return getData(x, y, z-1);
		}
		else
		{
			Chunk otherChunk = level.getChunk(this.x, this.z-1);
			if (otherChunk == null)
			{
				return -1;
			}
			else
			{
				return otherChunk.getData(x, y, 15);
			}
		}
	}

	/**
	 * Gets the Block ID of the block immediately to the north.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	protected abstract short getAdjSouthBlockId(int x, int y, int z, int blockOffset);

	/**
	 * Gets the data value of the block immediately to the north.  This might
	 * load in the adjacent chunk, if needed.  Will return -1 if that adjacent
	 * chunk can't be found.
	 */
	private byte getAdjSouthBlockData(int x, int y, int z)
	{
		if (z < 15)
		{
			return getData(x, y, z+1);
		}
		else
		{
			Chunk otherChunk = level.getChunk(this.x, this.z+1);
			if (otherChunk == null)
			{
				return -1;
			}
			else
			{
				return otherChunk.getData(x, y, 0);
			}
		}
	}

	/**
	 * Gets the Block ID of the block immediately up.
	 * Will return -1 if we're already at the top
	 */
	protected abstract short getAdjUpBlockId(int x, int y, int z, int blockOffset);

	/**
	 * Gets the Block ID of the block immediately down.
	 * Will return -1 if we're already at the bottom
	 */
	protected abstract short getAdjDownBlockId(int x, int y, int z, int blockOffset);
	
	/**
	 * Render something which is a West/East face.
	 */
	public void renderWestEast(int t, float x, float y, float z) {
		this.renderWestEast(t, x, y, z, 0.5f, 0.5f);
	}
	
	/**
	 * Render something which is a West/East face.
	 * 
	 * @param t Texture to render
	 * @param x
	 * @param y
	 * @param z
	 * @param yHeightOffset How tall this block is.  0.5f is the usual, specify 0 for half-height
	 * @param xzScale How large the rest of the block is.  0.5f is full-size, 0.1 would be tiny.
	 */
	public void renderWestEast(int t, float x, float y, float z, float yHeightOffset, float xzScale) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-xzScale, y+yHeightOffset, z+xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-xzScale, y+yHeightOffset, z-xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t],precalcSpriteSheetToTextureY[t]+TEX32);
			GL11.glVertex3f(x-xzScale, y-xzScale, z+xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX32);
			GL11.glVertex3f(x-xzScale, y-xzScale, z-xzScale);
		GL11.glEnd();
	}
	
	/**
	 * Renders a floor tile which is also rotated
	 * 
	 * @param t
	 * @param x
	 * @param y
	 * @param z
	 * @param turns The number of clockwise 90-degree turns to rotate the texture
	 */
	public void renderTopDownRotate(int t, float x, float y, float z, int turns)
	{
		float scale = 0.5f;
		float tx = precalcSpriteSheetToTextureX[t];
		float ty = precalcSpriteSheetToTextureY[t];
		float x1, y1;
		float x2, y2;
		float x3, y3;
		float x4, y4;
		
		switch (turns)
		{
			case 0:
				x1 = tx;       y1 = ty;
				x2 = tx+TEX16; y2 = ty;
				x3 = tx;       y3 = ty+TEX32;
				x4 = tx+TEX16; y4 = ty+TEX32;
				break;
			case 1:
				x1 = tx+TEX16; y1 = ty;
				x2 = tx+TEX16; y2 = ty+TEX32;
				x3 = tx;       y3 = ty;
				x4 = tx;       y4 = ty+TEX32;
				break;
			case 2:
				x1 = tx+TEX16; y1 = ty+TEX32;
				x2 = tx;       y2 = ty+TEX32;
				x3 = tx+TEX16; y3 = ty;
				x4 = tx;       y4 = ty;
				break;
			case 3:
			default:
				x1 = tx;       y1 = ty+TEX32;
				x2 = tx;       y2 = ty;
				x3 = tx+TEX16; y3 = ty+TEX32;
				x4 = tx+TEX16; y4 = ty;
				break;
		}
		
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(x1, y1);
			GL11.glVertex3f(x-scale, y-scale, z+scale);
	
			GL11.glTexCoord2f(x2, y2);
			GL11.glVertex3f(x-scale, y-scale, z-scale);
	
			GL11.glTexCoord2f(x3, y3);
			GL11.glVertex3f(x+scale, y-scale, z+scale);
	
			GL11.glTexCoord2f(x4, y4);
			GL11.glVertex3f(x+scale, y-scale, z-scale);
		GL11.glEnd();
		
	}
	
	/**
	 * Render the top or bottom of a block, depending on how we're looking at it.
	 */
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
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]+TEX32);
			GL11.glVertex3f(x+scale, y-scale, z+scale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX32);
			GL11.glVertex3f(x+scale, y-scale, z-scale);
		GL11.glEnd();
	}
	

	/**
	 * Renders something which is a North/South face.
	 */
	public void renderNorthSouth(int t, float x, float y, float z) {
		this.renderNorthSouth(t, x, y, z, 0.5f, 0.5f);
	}
	
	/**
	 * Renders something which is a North/South face.
	 * 
	 * @param t Texture to draw
	 * @param x
	 * @param y
	 * @param z
	 * @param yHeightOffset How tall this block is.  0.5f is the usual, specify 0 for half-height
	 * @param xzScale How large the rest of the block is.  0.5f is full-size, 0.1 would be tiny.
	 */
	public void renderNorthSouth(int t, float x, float y, float z, float yHeightOffset, float xzScale) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-xzScale, y+yHeightOffset, z-xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x+xzScale, y+yHeightOffset, z-xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]+TEX32);
			GL11.glVertex3f(x-xzScale, y-xzScale, z-xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX32);
			GL11.glVertex3f(x+xzScale, y-xzScale, z-xzScale);
		GL11.glEnd();
	}


	/**
	 * Renders a vertical texture with a full square texture.
	 */
	public void renderVertical(int t, float x1, float z1, float x2, float z2, float y, float height) {
		renderVertical(t, x1, z1, x2, z2, y, height, 16, 16, 0, 0);
	}

	/**
	 * Renders a somewhat-arbitrary vertical rectangle.  Pass in (x, z) pairs for the endpoints,
	 * and information about the height.  The texture variables given are in terms of 1/16ths of
	 * the texture square, which means that for the default Minecraft 16x16 texture, they're in
	 * pixels.
	 * 
	 * @param t Texture to draw
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param y	The lower part of the rectangle
	 * @param height Height of the rectangle.
	 */
	public void renderVertical(int t, float x1, float z1, float x2, float z2, float y, float height, int tex_width, int tex_height, int tex_start_x, int tex_start_y) {

		float bx = precalcSpriteSheetToTextureX[t]+(TEX256*tex_start_x);
		float by = precalcSpriteSheetToTextureY[t]+(TEX512*tex_start_y);

		float tdx = TEX256*tex_width;
		float tdy = TEX512*tex_height;

		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by);
			GL11.glVertex3f(x1, y+height, z1);
	
			GL11.glTexCoord2f(bx+tdx, by);
			GL11.glVertex3f(x2, y+height, z2);
	
			GL11.glTexCoord2f(bx, by+tdy);
			GL11.glVertex3f(x1, y, z1);
	
			GL11.glTexCoord2f(bx+tdx, by+tdy);
			GL11.glVertex3f(x2, y, z2);
		GL11.glEnd();
	}
	
	/**
	 * Renders a nonstandard vertical rectangle (nonstandard referring primarily to
	 * the texture size (ie: when we're not pulling a single element out of a 16x16
	 * grid).  This differs from renderVertical also in that we specify two full
	 * (x, y, z) coordinates for the bounds, instead of passing in y and a height.
	 * Texture coordinates are passed in as the usual float from 0 to 1.
	 * 
	 * @param tx X index within the texture
	 * @param ty Y index within the texture
	 * @param tdx Width of texture
	 * @param tdy Height of texture
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 */
	public void renderNonstandardVertical(float tx, float ty, float tdx, float tdy, float x1, float y1, float z1, float x2, float y2, float z2)
	{
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(tx, ty);
			GL11.glVertex3f(x1, y1, z1);
			
			GL11.glTexCoord2f(tx+tdx, ty);
			GL11.glVertex3f(x2, y1, z2);
			
			GL11.glTexCoord2f(tx, ty+tdy);
			GL11.glVertex3f(x1, y2, z1);
			
			GL11.glTexCoord2f(tx+tdx, ty+tdy);
			GL11.glVertex3f(x2, y2, z2);
		GL11.glEnd();
	}
	
	/**
	 * Renders a nonstandard vertical rectangle (nonstandard referring primarily to
	 * the texture size (ie: when we're not pulling a single element out of a 16x16
	 * grid).  This differs from renderVertical also in that we specify two full
	 * (x, y, z) coordinates for the bounds, instead of passing in y and a height.
	 * Texture coordinates are passed in as the usual float from 0 to 1.
	 *
	 * Additionally, this method will rotate the texture while drawing; I needed this
	 * for Pistons, specifically - will probably come in handy elsewhere too.
	 * 
	 * @param tx X index within the texture
	 * @param ty Y index within the texture
	 * @param tdx Width of texture
	 * @param tdy Height of texture
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 */
	public void renderNonstandardVerticalTexRotate(float tx, float ty, float tdx, float tdy, float x1, float y1, float z1, float x2, float y2, float z2)
	{
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(tx+tdx, ty);
			GL11.glVertex3f(x1, y1, z1);
			
			GL11.glTexCoord2f(tx+tdx, ty+tdy);
			GL11.glVertex3f(x2, y1, z2);
			
			GL11.glTexCoord2f(tx, ty);
			GL11.glVertex3f(x1, y2, z1);
			
			GL11.glTexCoord2f(tx, ty+tdy);
			GL11.glVertex3f(x2, y2, z2);
		GL11.glEnd();
	}

	/**
	 * Renders a "default" horizontal rectangle, using a full square for the texture.
	 */
	public void renderHorizontal(int t, float x1, float z1, float x2, float z2, float y) {
		renderHorizontal(t, x1, z1, x2, z2, y, 16, 16, 0, 0, false);
	}
	
	/**
	 * Renders an arbitrary horizontal rectangle (will be orthogonal).  The texture parameters
	 * are specified in terms of 1/16ths of the texture (which equates to one pixel, when using
	 * the default 16x16 Minecraft texture.
	 *
	 * @param t
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param y
	 */
	public void renderHorizontal(int t, float x1, float z1, float x2, float z2, float y, int tex_width, int tex_height, int tex_start_x, int tex_start_y, boolean flip_tex) {

		float bx = precalcSpriteSheetToTextureX[t]+(TEX256*tex_start_x);
		float by = precalcSpriteSheetToTextureY[t]+(TEX512*tex_start_y);

		float tdx = TEX256*tex_width;
		float tdy = TEX512*tex_height;

		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			
			if (flip_tex)
			{
				GL11.glTexCoord2f(bx, by);
				GL11.glVertex3f(x1, y, z2);
		
				GL11.glTexCoord2f(bx+tdx, by);
				GL11.glVertex3f(x2, y, z2);
		
				GL11.glTexCoord2f(bx, by+tdy);
				GL11.glVertex3f(x1, y, z1);
		
				GL11.glTexCoord2f(bx+tdx, by+tdy);
				GL11.glVertex3f(x2, y, z1);
			}
			else
			{
				GL11.glTexCoord2f(bx, by);
				GL11.glVertex3f(x1, y, z1);
		
				GL11.glTexCoord2f(bx+tdx, by);
				GL11.glVertex3f(x1, y, z2);
		
				GL11.glTexCoord2f(bx, by+tdy);
				GL11.glVertex3f(x2, y, z1);
		
				GL11.glTexCoord2f(bx+tdx, by+tdy);
				GL11.glVertex3f(x2, y, z2);
			}
		GL11.glEnd();
	}
	
	/**
	 * Render a surface on a horizontal plane; pass in all four verticies.  This can result,
	 * obviously, in non-rectangular and non-orthogonal shapes.
	 * 
	 * @param t
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param x3
	 * @param z3
	 * @param x4
	 * @param z4
	 * @param y
	 */
	public void renderHorizontalAskew(int t, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float y) {

		float bx = precalcSpriteSheetToTextureX[t];
		float by = precalcSpriteSheetToTextureY[t];

		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by);
			GL11.glVertex3f(x1, y, z1);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x2, y, z2);
	
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x3, y, z3);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x4, y, z4);
		GL11.glEnd();
	}
	
	/**
	 * Renders a nonstandard horizontal rectangle (nonstandard referring primarily to
	 * the texture size (ie: when we're not pulling a single element out of a 16x16
	 * grid).
	 * 
	 * @param tx X index within the texture
	 * @param ty Y index within the texture
	 * @param tdx Width of texture
	 * @param tdy Height of texture
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param y
	 */
	public void renderNonstandardHorizontal(float tx, float ty, float tdx, float tdy, float x1, float z1, float x2, float z2, float y) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(tx, ty);
			GL11.glVertex3f(x1, y, z1);
	
			GL11.glTexCoord2f(tx+tdx, ty);
			GL11.glVertex3f(x1, y, z2);
	
			GL11.glTexCoord2f(tx, ty+tdy);
			GL11.glVertex3f(x2, y, z1);
	
			GL11.glTexCoord2f(tx+tdx, ty+tdy);
			GL11.glVertex3f(x2, y, z2);
		GL11.glEnd();
	}

	/**
	 * Renders a nonstandard horizontal rectangle (nonstandard referring primarily to
	 * the texture size (ie: when we're not pulling a single element out of a 16x16
	 * grid).
	 *
	 * Additionally, this method will rotate the texture while drawing; I needed this
	 * for Pistons, specifically - will probably come in handy elsewhere too.
	 * 
	 * @param tx X index within the texture
	 * @param ty Y index within the texture
	 * @param tdx Width of texture
	 * @param tdy Height of texture
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param y
	 */
	public void renderNonstandardHorizontalTexRotate(float tx, float ty, float tdx, float tdy, float x1, float z1, float x2, float z2, float y) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(tx+tdx, ty);
			GL11.glVertex3f(x1, y, z1);
	
			GL11.glTexCoord2f(tx+tdx, ty+tdy);
			GL11.glVertex3f(x1, y, z2);
	
			GL11.glTexCoord2f(tx, ty);
			GL11.glVertex3f(x2, y, z1);
	
			GL11.glTexCoord2f(tx, ty+tdy);
			GL11.glVertex3f(x2, y, z2);
		GL11.glEnd();
	}

	/**
	 * Given a whole mess of coordinates, draws an arbitrary rectangle
	 * 
	 * @param t
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @param x3
	 * @param y3
	 * @param z3
	 * @param x4
	 * @param y4
	 * @param z4
	 */
	public void renderArbitraryRect(int t,
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float x4, float y4, float z4)
	{
		float tx = precalcSpriteSheetToTextureX[t];
		float ty = precalcSpriteSheetToTextureY[t];

		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(tx, ty);
			GL11.glVertex3f(x1, y1, z1);
	
			GL11.glTexCoord2f(tx+TEX16, ty);
			GL11.glVertex3f(x2, y2, z2);
	
			GL11.glTexCoord2f(tx, ty+TEX32);
			GL11.glVertex3f(x3, y3, z3);
	
			GL11.glTexCoord2f(tx+TEX16, ty+TEX32);
			GL11.glVertex3f(x4, y4, z4);
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
	 * this will actually draw the face on the west or east sides.
	 * 
	 * @param t
	 * @param x
	 * @param y
	 * @param z
	 */
	public void renderStairSideNorthSouth(int t, float x, float y, float z, boolean swapZ) {
		
		float bx = precalcSpriteSheetToTextureX[t];
		float by = precalcSpriteSheetToTextureY[t];
		
		float zoff=0.5f;
		if (swapZ)
		{
			zoff = -0.5f;
		}
		
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x-0.5f, y-0.5f, z+zoff);
	
			GL11.glTexCoord2f(bx, by+TEX64);
			GL11.glVertex3f(x-0.5f, y, z+zoff);
			
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x-0.5f, y-0.5f, z-zoff);
	
			GL11.glTexCoord2f(bx+TEX32, by+TEX64);
			GL11.glVertex3f(x-0.5f, y, z);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x-0.5f, y+0.5f, z-zoff);
			
			GL11.glTexCoord2f(bx+TEX32, by);
			GL11.glVertex3f(x-0.5f, y+0.5f, z);

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
	public void renderStairSurfaceNorthSouth(int t, float x, float y, float z, boolean swapZ) {
		
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
	
			GL11.glTexCoord2f(bx, by+TEX64);
			GL11.glVertex3f(x+0.5f, y, z);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX64);
			GL11.glVertex3f(x-0.5f, y, z);
		GL11.glEnd();

		// Lower Step Side
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by+TEX64);
			GL11.glVertex3f(x+0.5f, y, z+zoff);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX64);
			GL11.glVertex3f(x-0.5f, y, z+zoff);
	
			GL11.glTexCoord2f(bx,by+TEX32);
			GL11.glVertex3f(x+0.5f, y-0.5f, z+zoff);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x-0.5f, y-0.5f, z+zoff);
		GL11.glEnd();

		// Higher Step surface
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by+TEX64);
			GL11.glVertex3f(x+0.5f, y+0.5f, z);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX64);
			GL11.glVertex3f(x-0.5f, y+0.5f, z);
	
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x+0.5f, y+0.5f, z-zoff);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x-0.5f, y+0.5f, z-zoff);
		GL11.glEnd();

		// Higher Step Side
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by);
			GL11.glVertex3f(x+0.5f, y+0.5f, z);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x-0.5f, y+0.5f, z);
	
			GL11.glTexCoord2f(bx,by+TEX64);
			GL11.glVertex3f(x+0.5f, y, z);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX64);
			GL11.glVertex3f(x-0.5f, y, z);
		GL11.glEnd();
	}
	
	/**
	 * Renders the side of a stair piece that runs West/East.  Verticies are in the following order:
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
	public void renderStairSideWestEast(int t, float x, float y, float z, boolean swapX) {
		
		float bx = precalcSpriteSheetToTextureX[t];
		float by = precalcSpriteSheetToTextureY[t];
		
		float xoff=0.5f;
		if (swapX)
		{
			xoff = -0.5f;
		}
		
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x+xoff, y-0.5f, z-0.5f);
	
			GL11.glTexCoord2f(bx, by+TEX64);
			GL11.glVertex3f(x+xoff, y, z-0.5f);
			
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x-xoff, y-0.5f, z-0.5f);
	
			GL11.glTexCoord2f(bx+TEX32, by+TEX64);
			GL11.glVertex3f(x, y, z-0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x-xoff, y+0.5f, z-0.5f);
			
			GL11.glTexCoord2f(bx+TEX32, by);
			GL11.glVertex3f(x, y+0.5f, z-0.5f);

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
	public void renderStairSurfaceWestEast(int t, float x, float y, float z, boolean swapX) {
		
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
	
			GL11.glTexCoord2f(bx, by+TEX64);
			GL11.glVertex3f(x, y, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX64);
			GL11.glVertex3f(x, y, z-0.5f);
		GL11.glEnd();

		// Lower Step Side
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by+TEX64);
			GL11.glVertex3f(x+xoff, y, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX64);
			GL11.glVertex3f(x+xoff, y, z-0.5f);
	
			GL11.glTexCoord2f(bx,by+TEX32);
			GL11.glVertex3f(x+xoff, y-0.5f, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x+xoff, y-0.5f, z-0.5f);
		GL11.glEnd();

		// Higher Step surface
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by+TEX64);
			GL11.glVertex3f(x, y+0.5f, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX64);
			GL11.glVertex3f(x, y+0.5f, z-0.5f);
	
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x-xoff, y+0.5f, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x-xoff, y+0.5f, z-0.5f);
		GL11.glEnd();


		// Higher Step Side
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by);
			GL11.glVertex3f(x, y+0.5f, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x, y+0.5f, z-0.5f);
	
			GL11.glTexCoord2f(bx,by+TEX64);
			GL11.glVertex3f(x, y, z+0.5f);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX64);
			GL11.glVertex3f(x, y, z-0.5f);
		GL11.glEnd();
	}
	
	/**
	 * Gets the block ID at the specified coordinate in the chunk.  This is
	 * only really used in the getAdj*BlockId() methods.
	 */
	public abstract short getBlock(int x, int y, int z);

	/**
	 * Gets the block data at the specified coordinates.
	 */
	public abstract byte getData(int x, int y, int z);
	
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
		 byte data = getData(xxx, yyy, zzz);
		 data &= 0xF;
		 switch (data) {
			 case 1:
				 renderRectDecoration(textureId, xxx, yyy, zzz, -30, 0f, 1f, -.6f, 0f);
				 return;
			 case 2:
				 renderRectDecoration(textureId, xxx, yyy, zzz, 30, 0f, 1f, .6f, 0f);
				 return;
			 case 3:
				 renderRectDecoration(textureId, xxx, yyy, zzz, 30, 1f, 0f, 0f, -.6f);
				 return;
			 case 4:
				 renderRectDecoration(textureId, xxx, yyy, zzz, -30, 1f, 0f, 0f, .6f);
				 return;
			 default:
				 renderRectDecoration(textureId, xxx, yyy, zzz);
				 return;
		 }
	}
	
	/**
	 * Renders a lever; copied and modified from renderTorch for the most part.
	 * TODO: Looks no better than the torches; should put the cobble base on at the least.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderLever(int textureId, int xxx, int yyy, int zzz, BlockType block, int tex_offset)
	{
		byte data = getData(xxx, yyy, zzz);
		boolean thrown = false;
		if ((data & 0x8) == 0x8)
		{
			thrown = true;
		}
		data &= 7;
		//XRay.logger.trace("Data: " + data);
		 
		// First draw the base
		int cobble_tex = block.texture_extra_map.get("base") + tex_offset;
		float box_height = .15f;
		float box_length = .2f;
		float box_width = .15f;
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		switch (data) {
			case 1:
				renderVertical(cobble_tex, x-.5f, z+box_width, x-.5f+box_height, z+box_width, y-box_length, box_length*2f);
				renderVertical(cobble_tex, x-.5f, z-box_width, x-.5f+box_height, z-box_width, y-box_length, box_length*2f);
				renderVertical(cobble_tex, x-.5f+box_height, z+box_width, x-.5f+box_height, z-box_width, y-box_length, box_length*2f);
				renderHorizontal(cobble_tex, x-.5f, z-box_width, x-.5f+box_height, z+box_width, y-box_length);
				renderHorizontal(cobble_tex, x-.5f, z-box_width, x-.5f+box_height, z+box_width, y+box_length);
				break;
			case 2:
				renderVertical(cobble_tex, x+.5f, z+box_width, x+.5f-box_height, z+box_width, y-box_length, box_length*2f);
				renderVertical(cobble_tex, x+.5f, z-box_width, x+.5f-box_height, z-box_width, y-box_length, box_length*2f);
				renderVertical(cobble_tex, x+.5f-box_height, z+box_width, x+.5f-box_height, z-box_width, y-box_length, box_length*2f);
				renderHorizontal(cobble_tex, x+.5f, z-box_width, x+.5f-box_height, z+box_width, y-box_length);
				renderHorizontal(cobble_tex, x+.5f, z-box_width, x+.5f-box_height, z+box_width, y+box_length);
				break;
			case 3:
				renderVertical(cobble_tex, x-box_width, z-.5f, x-box_width, z-.5f+box_height, y-box_length, box_length*2f);
				renderVertical(cobble_tex, x+box_width, z-.5f, x+box_width, z-.5f+box_height, y-box_length, box_length*2f);
				renderVertical(cobble_tex, x-box_width, z-.5f+box_height, x+box_width, z-.5f+box_height, y-box_length, box_length*2f);
				renderHorizontal(cobble_tex, x-box_width, z-.5f, x+box_width, z-.5f+box_height, y-box_length);
				renderHorizontal(cobble_tex, x-box_width, z-.5f, x+box_width, z-.5f+box_height, y+box_length);
				break;
			case 4:
				renderVertical(cobble_tex, x-box_width, z+.5f, x-box_width, z+.5f-box_height, y-box_length, box_length*2f);
				renderVertical(cobble_tex, x+box_width, z+.5f, x+box_width, z+.5f-box_height, y-box_length, box_length*2f);
				renderVertical(cobble_tex, x-box_width, z+.5f-box_height, x+box_width, z+.5f-box_height, y-box_length, box_length*2f);
				renderHorizontal(cobble_tex, x-box_width, z+.5f, x+box_width, z+.5f-box_height, y-box_length);
				renderHorizontal(cobble_tex, x-box_width, z+.5f, x+box_width, z+.5f-box_height, y+box_length);
				break;
			case 5:
				renderVertical(cobble_tex, x-box_width, z+box_length, x-box_width, z-box_length, y-.5f, box_height);
				renderVertical(cobble_tex, x+box_width, z+box_length, x+box_width, z-box_length, y-.5f, box_height);
				renderVertical(cobble_tex, x-box_width, z+box_length, x+box_width, z+box_length, y-.5f, box_height);
				renderVertical(cobble_tex, x+box_width, z-box_length, x-box_width, z-box_length, y-.5f, box_height);
				renderHorizontal(cobble_tex, x-box_width, z-box_length, x+box_width, z+box_length, y-.5f+box_height);
				break;
			case 6:
			default:
				renderVertical(cobble_tex, x-box_length, z+box_width, x-box_length, z-box_width, y-.5f, box_height);
				renderVertical(cobble_tex, x+box_length, z+box_width, x+box_length, z-box_width, y-.5f, box_height);
				renderVertical(cobble_tex, x-box_length, z+box_width, x+box_length, z+box_width, y-.5f, box_height);
				renderVertical(cobble_tex, x+box_length, z-box_width, x-box_length, z-box_width, y-.5f, box_height);
				renderHorizontal(cobble_tex, x-box_length, z-box_width, x+box_length, z+box_width, y-.5f+box_height);
				break;
		}

		// Now draw the lever itself
		if (thrown)
		{
			switch (data) {
				case 1:
					renderRectDecoration(textureId, xxx, yyy+1, zzz, -135, 0f, 1f, .6f, 0f);
					break;
				case 2:
					renderRectDecoration(textureId, xxx, yyy+1, zzz, 135, 0f, 1f, -.6f, 0f);
					break;
				case 3:
					renderRectDecoration(textureId, xxx, yyy+1, zzz, 135, 1f, 0f, 0f, .6f);
					break;
				case 4:
					renderRectDecoration(textureId, xxx, yyy+1, zzz, -135, 1f, 0f, 0f, -.6f);
					break;
				case 5:
					renderRectDecoration(textureId, xxx, yyy, zzz, -45, 1f, 0f, 0f, 0f);
					break;
				case 6:
					renderRectDecoration(textureId, xxx, yyy, zzz, 45, 0f, 1f, 0f, 0f);
					break;
			}
		}
		else
		{
			switch (data) {
				case 1:
					renderRectDecoration(textureId, xxx, yyy, zzz, -45, 0f, 1f, -.6f, 0f);
					break;
				case 2:
					renderRectDecoration(textureId, xxx, yyy, zzz, 45, 0f, 1f, .6f, 0f);
					break;
				case 3:
					renderRectDecoration(textureId, xxx, yyy, zzz, 45, 1f, 0f, 0f, -.6f);
					break;
				case 4:
					renderRectDecoration(textureId, xxx, yyy, zzz, -45, 1f, 0f, 0f, .6f);
					break;
				case 5:
					renderRectDecoration(textureId, xxx, yyy, zzz, 45, 1f, 0f, 0f, 0f);
					break;
				case 6:
					renderRectDecoration(textureId, xxx, yyy, zzz, -45, 0f, 1f, 0f, 0f);
					break;
			}
		}
	}

	/**
	 * Renders a decoration which is supposed to be a "cross" in a single block.  There's
	 * some code duplication from renderRectDecoration in here, but not too much, hopefully.
	 * This will require an entry in XRay.decorationStats for the given textureId.
	 */
	public void renderCrossDecoration(int textureId, int xxx, int yyy, int zzz)
	{
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy - 0.5f;

		// We do the "% 256" here because our texture ID might be in the "highlighted"
		// range, for Explored highlighting.
		TextureDecorationStats stats = XRay.decorationStats.get(textureId % 256);
		if (stats == null)
		{
			return;
		}
		float tex_begin_x = precalcSpriteSheetToTextureX[textureId] + stats.getTexLeft();
		float tex_begin_y = precalcSpriteSheetToTextureY[textureId] + stats.getTexTop();
		float tex_width = stats.getTexWidth();
		float tex_height = stats.getTexHeight();

		float width = stats.getWidth();
		float width_h = width/2f;
		float height = stats.getHeight();
		float top_tex_height;

		renderNonstandardVertical(tex_begin_x, tex_begin_y, tex_width, tex_height,
				x-width_h, y+height, z-width_h,
				x+width_h, y, z+width_h);
		renderNonstandardVertical(tex_begin_x, tex_begin_y, tex_width, tex_height,
				x+width_h, y+height, z-width_h,
				x-width_h, y, z+width_h);
	}

	/**
	 * Renders an rectangular decoration which is just standing straight up.  This will require
	 * an entry in XRay.decorationStats for the given textureId.
	 *
	 * Currently only used for torches and levers, actually.
	 */
	public void renderRectDecoration(int textureId, int xxx, int yyy, int zzz)
	{
		renderRectDecoration(textureId, xxx, yyy, zzz, 0, 0f, 0f, 0f, 0f);
	}

	/**
	 * Renders a rectangular decoration.  This will require an entry in XRay.decorationStats for
	 * the given textureId.  Optionally pass in some parameters for rotation, currently used
	 * for torches and levers.
	 *
	 * @param textureId Texture to draw
	 * @param xxx Chunk X
	 * @param yyy Chunk Y
	 * @param zzz Chunk Z
	 * @param rotate_degrees Degrees to rotate, use zero for no rotation
	 * @param rotate_x Use 1.0f to rotate in the X direction (passed to glRotatef)
	 * @param rotate_z Use 1.0f to rotate in the X direction (passed to glRotatef)
	 * @param x_off X offset, so it's not just in the center
	 * @param z_off Z offset, so it's not just in the center
	 */
	public void renderRectDecoration(int textureId, int xxx, int yyy, int zzz,
			int rotate_degrees, float rotate_x, float rotate_z, float x_off, float z_off)
	{
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy - 0.5f;

		boolean do_rotate = false;
		float tx=0, ty=0, tz=0;
		if (rotate_degrees != 0)
		{
			tx = x;
			ty = y;
			tz = z;
			x = x_off;
			y = 0;
			z = z_off;
			do_rotate = true;
		}

		float my_x = xxx + this.x*16;
		float my_z = zzz + this.z*16;
		float my_y = yyy - 0.5f;
		// We do the "% 256" here because our texture ID might be in the "highlighted"
		// range, for Explored highlighting.
		TextureDecorationStats stats = XRay.decorationStats.get(textureId % 256);
		if (stats == null)
		{
			return;
		}

		float tex_begin_x = precalcSpriteSheetToTextureX[textureId] + stats.getTexLeft();
		float tex_begin_y = precalcSpriteSheetToTextureY[textureId] + stats.getTexTop();
		float tex_width = stats.getTexWidth();
		float tex_height = stats.getTexHeight();

		float width = stats.getWidth();
		float width_h = width/2f;
		float height = stats.getHeight();
		float top_tex_height;
		if (height > width)
		{
			top_tex_height = tex_width/2f;
		}
		else
		{
			top_tex_height = tex_height;
		}

		// Math is for suckers; let's let the video hardware take care of rotation
		// Relatedly, is this how I should be drawing *everything?*  Draw relative
		// to the origin for the actual verticies, and then translate?
		if (do_rotate)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(tx, ty, tz);
			GL11.glRotatef((float)rotate_degrees, rotate_x, 0f, rotate_z);
		}
		
		// First draw the borders
		renderNonstandardVertical(tex_begin_x, tex_begin_y, tex_width, tex_height,
				x-width_h, y+height, z-width_h,
				x+width_h, y, z-width_h);
		renderNonstandardVertical(tex_begin_x, tex_begin_y, tex_width, tex_height,
				x-width_h, y+height, z+width_h,
				x+width_h, y, z+width_h);
		renderNonstandardVertical(tex_begin_x, tex_begin_y, tex_width, tex_height,
				x+width_h, y+height, z-width_h,
				x+width_h, y, z+width_h);
		renderNonstandardVertical(tex_begin_x, tex_begin_y, tex_width, tex_height,
				x-width_h, y+height, z+width_h,
				x-width_h, y, z-width_h);

		// Now the top
		renderNonstandardHorizontal(tex_begin_x, tex_begin_y, tex_width, top_tex_height,
				x-width_h, z-width_h,
				x+width_h, z+width_h,
				y+height);

		if (do_rotate)
		{
			GL11.glPopMatrix();
		}
	}
	
	/**
	 * Renders a "grid" decoration (in the manner of crops and netherwart)
	 * 
	 * @param textureId Texture
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderGridDecoration(int textureId, int xxx, int yyy, int zzz)
	{
		 float x = xxx + this.x*16;
		 float z = zzz + this.z*16;
		 float y = yyy;

		// We do the "% 256" here because our texture ID might be in the "highlighted"
		// range, for Explored highlighting.
		TextureDecorationStats stats = XRay.decorationStats.get(textureId % 256);
		if (stats == null)
		{
			return;
		}

		 float tex_x = precalcSpriteSheetToTextureX[textureId] + stats.getTexLeft();
		 float tex_y = precalcSpriteSheetToTextureY[textureId] + stats.getTexTop();
		 float tex_dx = stats.getTexWidth();
		 float tex_dy = stats.getTexHeight();

		 float width_h = stats.getWidth()/2;
		 float side_offset = .25f;
		 float bottom = y -.5f;
		 float top = bottom + stats.getHeight();

		 // now each side
		 renderNonstandardVertical(tex_x, tex_y, tex_dx, tex_dy,
				 x-width_h, top, z+side_offset,
				 x+width_h, bottom, z+side_offset);

		 renderNonstandardVertical(tex_x, tex_y, tex_dx, tex_dy,
				 x-width_h, top, z-side_offset,
				 x+width_h, bottom, z-side_offset);

		 renderNonstandardVertical(tex_x, tex_y, tex_dx, tex_dy,
				 x+side_offset, top, z-width_h,
				 x+side_offset, bottom, z+width_h);

		 renderNonstandardVertical(tex_x, tex_y, tex_dx, tex_dy,
				 x-side_offset, top, z-width_h,
				 x-side_offset, bottom, z+width_h);
	}
    
	/**
	 * Renders a ladder, given its attached-side data.  We still take in textureId just so
	 * that everything's still defined in MinecraftConstants
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
		 		// South
		 		this.renderNorthSouth(textureId, x, y, z+1.0f-TEX64);
		 		break;
		 	case 3:
		 		// North
		 		this.renderNorthSouth(textureId, x, y, z+TEX64);
		 		break;
		 	case 4:
		 		// East
		 		this.renderWestEast(textureId, x+1.0f-TEX64, y, z);
		 		break;
		 	case 5:
	 		default:
	 			// West
				this.renderWestEast(textureId, x+TEX64, y, z);
	 			break;
		 }
	}
    
	/**
	 * Renders a vine, given its attached-side data.  Pretty much identical
	 * to renderLadder, except that there's different data values.  Alas!
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderVine(int textureId, int xxx, int yyy, int zzz, int blockOffset) {
		 float x = xxx + this.x*16;
		 float z = zzz + this.z*16;
		 float y = yyy;
		 
		 byte data = getData(xxx, yyy, zzz);
		 boolean rendered = false;
		 if ((data & 1) == 1)
		 {
			// South
			this.renderNorthSouth(textureId, x, y, z+1.0f-TEX64);
			rendered = true;
		 }
		 if ((data & 2) == 2)
		 {
			// West
			this.renderWestEast(textureId, x+TEX64, y, z);
			rendered = true;
		 }
		 if ((data & 4) == 4)
		 {
			// North
			this.renderNorthSouth(textureId, x, y, z+TEX64);
			rendered = true;
		 }
		 if ((data & 8) == 8)
		 {
			// East
			this.renderWestEast(textureId, x+1.0f-TEX64, y, z);
			rendered = true;
		 }
		 if (data == 0 || (rendered && isSolid(this.getAdjUpBlockId(xxx, yyy, zzz, blockOffset))))
		 {
			// Top
			this.renderHorizontal(textureId, x-.5f, z-.5f, x+.5f, z+.5f, y+.45f);
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
	 * Minecart tracks
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderMinecartTracks(int textureId, int xxx, int yyy, int zzz, BlockType block, int tex_offset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		 
		byte data = getData(xxx, yyy, zzz);
		if (data > 0x5)
		{
			textureId = block.texture_extra_map.get("curve") + tex_offset;
		}
 
		switch (data)
		{
			case 0x0:
				this.renderTopDownRotate(textureId, x, y+TEX64, z, 1);
				break;
			case 0x1:
				this.renderTopDown(textureId, x, y+TEX64, z);
				break;
			case 0x2:
				this.renderArbitraryRect(textureId,
						x-0.5f, y-0.5f, z+0.5f,
						x-0.5f, y-0.5f, z-0.5f,
						x+0.5f, y+0.5f, z+0.5f,
						x+0.5f, y+0.5f, z-0.5f
						);
				break;
			case 0x3:
				this.renderArbitraryRect(textureId,
						x-0.5f, y+0.5f, z+0.5f,
						x-0.5f, y+0.5f, z-0.5f,
						x+0.5f, y-0.5f, z+0.5f,
						x+0.5f, y-0.5f, z-0.5f
						);
				break;
			case 0x4:
				this.renderArbitraryRect(textureId,
						x-0.5f, y+0.5f, z-0.5f,
						x+0.5f, y+0.5f, z-0.5f,
						x-0.5f, y-0.5f, z+0.5f,
						x+0.5f, y-0.5f, z+0.5f
						);
				break;
			case 0x5:
				this.renderArbitraryRect(textureId,
						x-0.5f, y-0.5f, z-0.5f,
						x+0.5f, y-0.5f, z-0.5f,
						x-0.5f, y+0.5f, z+0.5f,
						x+0.5f, y+0.5f, z+0.5f
						);
				break;
			case 0x6:
				this.renderTopDownRotate(textureId, x, y+TEX64, z, 3);
				break;
			case 0x7:
				this.renderTopDownRotate(textureId, x, y+TEX64, z, 2);
				break;
			case 0x8:
				this.renderTopDownRotate(textureId, x, y+TEX64, z, 1);
				break;
			case 0x9:
				this.renderTopDownRotate(textureId, x, y+TEX64, z, 0);
				break;
			default:
				// Just do the usual for now
				this.renderTopDown(textureId, x, y+TEX64, z);
				break;
		}
	}

	/**
	 * "Simple" rails, which don't have corners.  This actually isn't as
	 * simple as it should be, since we have a special-case for powered rails.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderSimpleRail(int textureId, int xxx, int yyy, int zzz, BlockType block, int tex_offset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		 
		byte data = getData(xxx, yyy, zzz);
		byte powered = data;
		powered >>= 3;
		if (powered > 0)
		{
			// This is just for powered rails, to light them up properly
			textureId = block.texture_extra_map.get("powered") + tex_offset;
		}
		data &= 7;
 
		switch (data)
		{
			case 0x0:
				this.renderTopDownRotate(textureId, x, y+TEX64, z, 1);
				break;
			case 0x1:
				this.renderTopDown(textureId, x, y+TEX64, z);
				break;
			case 0x2:
				this.renderArbitraryRect(textureId,
						x-0.5f, y-0.5f, z+0.5f,
						x-0.5f, y-0.5f, z-0.5f,
						x+0.5f, y+0.5f, z+0.5f,
						x+0.5f, y+0.5f, z-0.5f
						);
				break;
			case 0x3:
				this.renderArbitraryRect(textureId,
						x-0.5f, y+0.5f, z+0.5f,
						x-0.5f, y+0.5f, z-0.5f,
						x+0.5f, y-0.5f, z+0.5f,
						x+0.5f, y-0.5f, z-0.5f
						);
				break;
			case 0x4:
				this.renderArbitraryRect(textureId,
						x-0.5f, y+0.5f, z-0.5f,
						x+0.5f, y+0.5f, z-0.5f,
						x-0.5f, y-0.5f, z+0.5f,
						x+0.5f, y-0.5f, z+0.5f
						);
				break;
			case 0x5:
				this.renderArbitraryRect(textureId,
						x-0.5f, y-0.5f, z-0.5f,
						x+0.5f, y-0.5f, z-0.5f,
						x-0.5f, y+0.5f, z+0.5f,
						x+0.5f, y+0.5f, z+0.5f
						);
				break;
			default:
				// Just do the usual for now
				this.renderTopDown(textureId, x, y+TEX64, z);
				break;
		}
	}
	
	/**
	 * Renders a pressure plate.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderPlate(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		float radius = 0.4f;
		
		// The plate itself
		this.renderHorizontal(textureId, x+radius, z+radius, x-radius, z-radius, y-0.45f);
		
		// Sides
		this.renderVertical(textureId, x+radius, z+radius, x+radius, z-radius, y-0.5f, 0.05f);
		this.renderVertical(textureId, x-radius, z+radius, x-radius, z-radius, y-0.5f, 0.05f);
		this.renderVertical(textureId, x+radius, z+radius, x-radius, z+radius, y-0.5f, 0.05f);
		this.renderVertical(textureId, x+radius, z-radius, x-radius, z-radius, y-0.5f, 0.05f);
	}

	/**
	 * Renders snow (the naturally-ocurring snow, not crafted snow blocks)
	 *
	 * TODO: Technically this has data, but I don't feel like getting the sides
	 * to render properly.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param blockOffset Should be passed in from our main draw loop so we don't have to recalculate
	 */
	public void renderSnow(int textureId, int xxx, int yyy, int zzz, int blockOffset, int blockId) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		float edge = 0.5f;
		float top = -.375f;
		float height = .125f;
		short adj;
		
		// The top face
		this.renderHorizontal(textureId, x+edge, z+edge, x-edge, z-edge, y+top);
		
		// East
		adj = getAdjEastBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != blockId && !isSolid(adj))
		{
			this.renderVertical(textureId, x+edge, z+edge, x+edge, z-edge, y-.5f, height, 16, 2, 0, 14);
		}

		// West
		adj = getAdjWestBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != blockId && !isSolid(adj))
		{
			this.renderVertical(textureId, x-edge, z+edge, x-edge, z-edge, y-.5f, height, 16, 2, 0, 14);
		}

		// South
		adj = getAdjSouthBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != blockId && !isSolid(adj))
		{
			this.renderVertical(textureId, x+edge, z+edge, x-edge, z+edge, y-.5f, height, 16, 2, 0, 14);
		}

		// North
		adj = getAdjNorthBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != blockId && !isSolid(adj))
		{
			this.renderVertical(textureId, x+edge, z-edge, x-edge, z-edge, y-.5f, height, 16, 2, 0, 14);
		}
	}

	/**
	 * Renders a bed block.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderBed(int textureId, int xxx, int yyy, int zzz, BlockType block, int tex_offset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		float side_part = 0.49f;
		float side_full = 0.5f;
		float bed_height = 0.5625f;
		float horiz_off = bed_height-0.5f;
		float bed_tex_height = TEX256/2f*9f;
		boolean head = true;

		byte data = getData(xxx, yyy, zzz);
		data &= 0xF;
		if ((data & 0x8) == 0)
		{
			textureId = block.texture_extra_map.get("foot_top") + tex_offset;
			head = false;
		}
		data &= 0x3;

		// Use GL to rotate these properly
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		// We're drawing the bed with the head facing East (direction 2)
		if (data == 0)
		{
			// Pointing West
			GL11.glRotatef(180f, 0f, 1f, 0f);
		}
		else if (data == 1)
		{
			// Pointing South
			GL11.glRotatef(90f, 0f, 1f, 0f);
		}
		else if (data == 3)
		{
			// Pointing North
			GL11.glRotatef(-90f, 0f, 1f, 0f);
		}

		float end_tex_x, end_tex_y;
		float first_z, second_z, end_z;
		float side_tex_x, side_tex_y;
		if (head)
		{
			end_tex_x = precalcSpriteSheetToTextureX[block.texture_extra_map.get("head")+tex_offset];
			end_tex_y = precalcSpriteSheetToTextureY[block.texture_extra_map.get("head")+16+tex_offset]-bed_tex_height;
			side_tex_x = precalcSpriteSheetToTextureX[block.texture_extra_map.get("head_side")+tex_offset];
			side_tex_y = precalcSpriteSheetToTextureY[block.texture_extra_map.get("head_side")+16+tex_offset]-bed_tex_height;
			first_z = side_full;
			second_z = -side_part;
			end_z = -side_part;
		}
		else
		{
			end_tex_x = precalcSpriteSheetToTextureX[block.texture_extra_map.get("foot")+tex_offset];
			end_tex_y = precalcSpriteSheetToTextureY[block.texture_extra_map.get("foot")+16+tex_offset]-bed_tex_height;
			side_tex_x = precalcSpriteSheetToTextureX[block.texture_extra_map.get("foot_side")+tex_offset];
			side_tex_y = precalcSpriteSheetToTextureY[block.texture_extra_map.get("foot_side")+16+tex_offset]-bed_tex_height;
			first_z = side_part;
			second_z = -side_full;
			end_z = side_part;
		}

		// Top face
		this.renderHorizontal(textureId, side_part, first_z, -side_part, second_z, horiz_off);

		// Side faces
		this.renderNonstandardVertical(side_tex_x, side_tex_y, TEX16, bed_tex_height, side_part, bed_height-side_full, first_z, side_part, -side_full, second_z);
		this.renderNonstandardVertical(side_tex_x, side_tex_y, TEX16, bed_tex_height, -side_part, bed_height-side_full, first_z, -side_part, -side_full, second_z);

		// end face (either the very foot or the very head of the bed)
		this.renderNonstandardVertical(end_tex_x, end_tex_y, TEX16, bed_tex_height, side_part, bed_height-side_full, end_z, -side_part, -side_full, end_z);

		// Pop the matrix
		GL11.glPopMatrix();
	}
	
	/**
	 * Renders a door
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderDoor(int textureId, int xxx, int yyy, int zzz, BlockType block, int tex_offset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		
		byte data = getData(xxx, yyy, zzz);
		if ((data & 0x8) == 0x0)
		{
			textureId = block.texture_extra_map.get("bottom") + tex_offset;
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
			// West			
			this.renderWestEast(textureId, x, y, z);
		}
		else if ((dir == 0 && swung) || (dir == 1 && !swung))
		{
			// North
			this.renderNorthSouth(textureId, x, y, z);
		}
		else if ((dir == 1 && swung) || (dir == 2 && !swung))
		{
			// East
			this.renderWestEast(textureId, x+1, y, z);
		}
		else
		{
			// South
			this.renderNorthSouth(textureId, x, y, z+1);
		}
		
	}
	
	/**
	 * Renders a trapdoor
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderTrapdoor(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		float twidth = .1f;
		//float twidth_h = twidth/2f;
		float toff = .02f;
		
		byte data = getData(xxx, yyy, zzz);
		boolean swung = false;
		if ((data & 0x4) == 0x4)
		{
			swung = true;
		}
		int dir = (data & 0x3);

		float tex_x = precalcSpriteSheetToTextureX[textureId];
		float tex_y = precalcSpriteSheetToTextureY[textureId];
		float tex_dx = TEX16;
		float tex_dy = TEX32 * twidth;

		// Use GL to rotate these properly
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);
		if (swung)
		{
			if (dir == 0)
			{
				// South
				GL11.glRotatef(-90f, 1f, 0f, 0f);
			}
			else if (dir == 1)
			{
				// North
				GL11.glRotatef(90f, 1f, 0f, 0f);
			}
			else if (dir == 2)
			{
				// East
				GL11.glRotatef(90f, 0f, 0f, 1f);
			}
			else
			{
				// West
				GL11.glRotatef(-90f, 0f, 0f, 1f);
			}
		}
		
		// First the faces
		//this.renderHorizontal(textureId, .5f-toff, .5f-toff, -.5f+toff, -.5f+toff, -.5f+toff);
		this.renderHorizontal(textureId, .5f-toff, .5f-toff, -.5f+toff, -.5f+toff, -.5f+toff+twidth);

		// Now the sides
		this.renderNonstandardVertical(tex_x, tex_y, tex_dx, tex_dy,
				.5f-toff, -.5f+toff,         .5f-toff,
				-.5f+toff, -.5f+toff+twidth, .5f-toff);
		this.renderNonstandardVertical(tex_x, tex_y, tex_dx, tex_dy,
				.5f-toff, -.5f+toff,        .5f-toff,
				.5f-toff, -.5f+toff+twidth, -.5f+toff);
		this.renderNonstandardVertical(tex_x, tex_y, tex_dx, tex_dy,
				-.5f+toff, -.5f+toff,        -.5f+toff,
				-.5f+toff, -.5f+toff+twidth, .5f-toff);
		this.renderNonstandardVertical(tex_x, tex_y, tex_dx, tex_dy,
				-.5f+toff, -.5f+toff,       -.5f+toff,
				.5f-toff, -.5f+toff+twidth, -.5f+toff);

		GL11.glPopMatrix();
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
			// 0 is ascending-east, 1 is ascending-west
			
			// Sides
			this.renderStairSideWestEast(textureId, x, y, z+.05f, swap);
			this.renderStairSideWestEast(textureId, x, y, z+.95f, swap);
			
			// Back
			if (swap)
			{
				this.renderWestEast(textureId, x+0.94f, y, z, 0.5f, 0.45f);
			}
			else
			{
				this.renderWestEast(textureId, x+0.06f, y, z, 0.5f, 0.45f);
			}
			
			// Bottom
			this.renderTopDown(textureId, x, y, z, 0.45f);
			
			// Stair Surface
			this.renderStairSurfaceWestEast(textureId, x, y, z, swap);
		}
		else
		{
			// 2 is ascending-south, 3 is ascending-north
			
			// Sides
			this.renderStairSideNorthSouth(textureId, x+.05f, y, z, swap);
			this.renderStairSideNorthSouth(textureId, x+.95f, y, z, swap);
			
			// Back
			if (swap)
			{
				this.renderNorthSouth(textureId, x, y, z+0.94f, 0.5f, 0.45f);
			}
			else
			{
				this.renderNorthSouth(textureId, x, y, z+0.06f, 0.5f, 0.45f);
			}
			
			// Bottom
			this.renderTopDown(textureId, x, y, z, 0.45f);
			
			// Stair Surface
			this.renderStairSurfaceNorthSouth(textureId, x, y, z, swap);		
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
		float face_spacing = 3; // in degrees
		
		// First a signpost
		this.renderVertical(textureId, x-postRadius, z-postRadius, x+postRadius, z-postRadius, y-0.5f, 0.5f+signBottom);
		this.renderVertical(textureId, x-postRadius, z+postRadius, x+postRadius, z+postRadius, y-0.5f, 0.5f+signBottom);
		this.renderVertical(textureId, x+postRadius, z-postRadius, x+postRadius, z+postRadius, y-0.5f, 0.5f+signBottom);
		this.renderVertical(textureId, x-postRadius, z+postRadius, x-postRadius, z-postRadius, y-0.5f, 0.5f+signBottom);
		
		// Signpost top
		this.renderHorizontal(textureId, x-postRadius, z-postRadius, x+postRadius, z+postRadius, y+signBottom);
		
		// Now we continue to draw the sign itself.
		byte data = getData(xxx, yyy, zzz);
		data &= 0xF;
		// data: 0 is South, increasing numbers add 22.5 degrees (so 4 is West, 8 East, etc)
		// Because we're not actually drawing the message (yet), as far as we're concerned
		// West is the same as East, etc.
		float angle = (data % 8) * 22.5f;
		float radius = 0.5f;

		angle -= face_spacing;
		// First x/z
		float x1a = x + radius * (float)Math.cos(Math.toRadians(angle));
		float z1a = z + radius * (float)Math.sin(Math.toRadians(angle));
		angle += face_spacing*2;
		float x1b = x + radius * (float)Math.cos(Math.toRadians(angle));
		float z1b = z + radius * (float)Math.sin(Math.toRadians(angle));
		
		// Now the other side
		angle += 180;
		float x2a = x + radius * (float)Math.cos(Math.toRadians(angle));
		float z2a = z + radius * (float)Math.sin(Math.toRadians(angle));
		angle -= face_spacing*2;
		float x2b = x + radius * (float)Math.cos(Math.toRadians(angle));
		float z2b = z + radius * (float)Math.sin(Math.toRadians(angle));
		
		// Faces
		this.renderVertical(textureId, x1a, z1a, x2a, z2a, y+signBottom, signHeight);
		this.renderVertical(textureId, x1b, z1b, x2b, z2b, y+signBottom, signHeight);
		
		// Sides
		this.renderVertical(textureId, x1a, z1a, x1b, z1b, y+signBottom, signHeight);
		this.renderVertical(textureId, x2a, z2a, x2b, z2b, y+signBottom, signHeight);
		
		// Top/Bottom
		this.renderHorizontalAskew(textureId, x1a, z1a, x1b, z1b, x2a, z2a, x2b, z2b, y+signBottom);
		this.renderHorizontalAskew(textureId, x1a, z1a, x1b, z1b, x2a, z2a, x2b, z2b, y+signBottom+signHeight);
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

		float faceX1, faceX2;
		float faceZ1, faceZ2;
 		float back_dX, back_dZ;
 		float sign_length = 0.4f;
		
		byte data = getData(xxx, yyy, zzz);
		switch(data)
		{
		 	case 2:
		 		// North
	 			faceX1 = x-sign_length;
	 			faceX2 = x+sign_length;
	 			faceZ1 = z+0.45f;
	 			faceZ2 = z+0.45f;
	 			back_dX = 0f;
	 			back_dZ = 0.05f;
		 		break;
		 	case 3:
		 		// South
	 			faceX1 = x-sign_length;
	 			faceX2 = x+sign_length;
	 			faceZ1 = z-0.45f;
	 			faceZ2 = z-0.45f;
	 			back_dX = 0f;
	 			back_dZ = -0.05f;
		 		break;
		 	case 4:
		 		// West
	 			faceX1 = x+0.45f;
	 			faceX2 = x+0.45f;
	 			faceZ1 = z-sign_length;
	 			faceZ2 = z+sign_length;
	 			back_dX = 0.05f;
	 			back_dZ = 0f;
		 		break;
		 	case 5:
	 		default:
	 			// East
	 			faceX1 = x-0.45f;
	 			faceX2 = x-0.45f;
	 			faceZ1 = z-sign_length;
	 			faceZ2 = z+sign_length;
	 			back_dX = -0.05f;
	 			back_dZ = 0f;
	 			break;
		}
		
		// Face
		this.renderVertical(textureId, faceX1, faceZ1, faceX2, faceZ2, y-0.2f, 0.5f);
		
		// Sides
		this.renderVertical(textureId, faceX1, faceZ1, faceX1+back_dX, faceZ1+back_dZ, y-0.2f, 0.5f);
		this.renderVertical(textureId, faceX2, faceZ2, faceX2+back_dX, faceZ2+back_dZ, y-0.2f, 0.5f);
		
		// Top/Bottom
		this.renderHorizontal(textureId, faceX1, faceZ1, faceX2+back_dX, faceZ2+back_dZ, y-0.2f);
		this.renderHorizontal(textureId, faceX1, faceZ1, faceX2+back_dX, faceZ2+back_dZ, y+0.3f);
	}
	
	/**
	 * Renders a fence.  Note that as of 1.9-pre, the two kinds of fences (regular and nether)
	 * won't link up with each other, but either will attach to a nearby Fence Gate, if they can.
	 *
	 * TODO: Sort out the proper texture offsets + orientations, or at least do it consistently
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param blockOffset Should be passed in from our main draw loop so we don't have to recalculate
	 */
	public void renderFence(int textureId, int xxx, int yyy, int zzz, int blockOffset, int blockId) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		float slat_start = y+fence_slat_start_offset;
		boolean beta19_fences = XRay.toggle.beta19_fences;
		
		// First the fencepost
		this.renderVertical(textureId, x+fence_postsize, z+fence_postsize, x+fence_postsize, z-fence_postsize, y-0.5f, 1f, 4, 16, 6, 0);
		this.renderVertical(textureId, x+fence_postsize, z-fence_postsize, x-fence_postsize, z-fence_postsize, y-0.5f, 1f, 4, 16, 6, 0);
		this.renderVertical(textureId, x-fence_postsize, z-fence_postsize, x-fence_postsize, z+fence_postsize, y-0.5f, 1f, 4, 16, 6, 0);
		this.renderVertical(textureId, x-fence_postsize, z+fence_postsize, x+fence_postsize, z+fence_postsize, y-0.5f, 1f, 4, 16, 6, 0);
		if (y == 127 || !isSolid(this.getAdjUpBlockId(xxx, yyy, zzz, blockOffset)))
		{
			this.renderHorizontal(textureId, x+fence_postsize, z+fence_postsize, x-fence_postsize, z-fence_postsize, y+0.5f, 4, 4, 6, 6, false);
		}

		short adj_id;
		byte adj_data;

		// Check for adjacent fences / fence gates in the -x direction
		adj_id = this.getAdjWestBlockId(xxx, yyy, zzz, blockOffset);
		if (adj_id == blockId)
		{
			// Fence to the West

			// Bottom slat
			this.renderVertical(textureId, x-fence_postsize, z+fence_postsize_h, x-1f+fence_postsize, z+fence_postsize_h, slat_start, fence_slat_height, 12, 3, 2, 5);
			this.renderVertical(textureId, x-fence_postsize, z-fence_postsize_h, x-1f+fence_postsize, z-fence_postsize_h, slat_start, fence_slat_height, 12, 3, 2, 5);
			this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-1f+fence_postsize, z-fence_postsize_h, slat_start, 2, 12, 14, 2, false);
			this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-1f+fence_postsize, z-fence_postsize_h, slat_start+fence_slat_height, 2, 12, 14, 2, false);

			// Top slat
			this.renderVertical(textureId, x-fence_postsize, z+fence_postsize_h, x-1f+fence_postsize, z+fence_postsize_h, slat_start+fence_top_slat_offset, fence_slat_height, 12, 3, 2, 5);
			this.renderVertical(textureId, x-fence_postsize, z-fence_postsize_h, x-1f+fence_postsize, z-fence_postsize_h, slat_start+fence_top_slat_offset, fence_slat_height, 12, 3, 2, 5);
			this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-1f+fence_postsize, z-fence_postsize_h, slat_start+fence_top_slat_offset, 2, 12, 14, 2, false);
			this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-1f+fence_postsize, z-fence_postsize_h, slat_start+fence_top_slat_offset+fence_slat_height, 2, 12, 14, 2, false);
		}
		else if (beta19_fences && this.isSolid(adj_id))
		{
			// Solid block to the West

			// Bottom slat
			this.renderVertical(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z+fence_postsize_h, slat_start, fence_slat_height, 6, 3, 0, 5);
			this.renderVertical(textureId, x-fence_postsize, z-fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start, fence_slat_height, 6, 3, 0, 5);
			this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start, 2, 6, 14, 0, false);
			this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start+fence_slat_height, 2, 6, 14, 0, false);

			// Top slat
			this.renderVertical(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z+fence_postsize_h, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
			this.renderVertical(textureId, x-fence_postsize, z-fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
			this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset, 2, 6, 14, 0, false);
			this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset+fence_slat_height, 2, 6, 14, 0, false);
		}
		else if (adj_id > -1 && blockArray[adj_id] != null && blockArray[adj_id].type == BLOCK_TYPE.FENCE_GATE)
		{
			// Fence Gate to the West
			adj_data = this.getAdjWestBlockData(xxx, yyy, zzz);
			if (adj_data != -1)
			{
				adj_data &= 0x3;
				if (adj_data == 0 || adj_data == 2)
				{
					// Bottom slat
					this.renderVertical(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z+fence_postsize_h, slat_start, fence_slat_height, 6, 3, 0, 5);
					this.renderVertical(textureId, x-fence_postsize, z-fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start, fence_slat_height, 6, 3, 0, 5);
					this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start, 2, 6, 14, 0, false);
					this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start+fence_slat_height, 2, 6, 14, 0, false);

					// Top slat
					this.renderVertical(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z+fence_postsize_h, slat_start+fence_top_slat_offset, fence_slat_height, 8, 3, 0, 5);
					this.renderVertical(textureId, x-fence_postsize, z-fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset, fence_slat_height, 8, 3, 0, 5);
					this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset, 2, 8, 14, 0, false);
					this.renderHorizontal(textureId, x-fence_postsize, z+fence_postsize_h, x-.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset+fence_slat_height, 2, 8, 14, 0, false);
				}
			}
		}

		// Check for adjacent fence gates in the +x direction
		adj_id = this.getAdjEastBlockId(xxx, yyy, zzz, blockOffset);
		if (adj_id > -1 && blockArray[adj_id] != null && blockArray[adj_id].type == BLOCK_TYPE.FENCE_GATE)
		{
			// Fence Gate to the East
			adj_data = this.getAdjEastBlockData(xxx, yyy, zzz);
			if (adj_data != -1)
			{
				adj_data &= 0x3;
				if (adj_data == 0 || adj_data == 2)
				{
					// Bottom slat
					this.renderVertical(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z+fence_postsize_h, slat_start, fence_slat_height, 6, 3, 0, 5);
					this.renderVertical(textureId, x+fence_postsize, z-fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start, fence_slat_height, 6, 3, 0, 5);
					this.renderHorizontal(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start, 2, 6, 14, 0, false);
					this.renderHorizontal(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start+fence_slat_height, 2, 6, 14, 0, false);

					// Top slat
					this.renderVertical(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z+fence_postsize_h, slat_start+fence_top_slat_offset, fence_slat_height, 8, 3, 0, 5);
					this.renderVertical(textureId, x+fence_postsize, z-fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset, fence_slat_height, 8, 3, 0, 5);
					this.renderHorizontal(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset, 2, 8, 14, 0, false);
					this.renderHorizontal(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset+fence_slat_height, 2, 8, 14, 0, false);
				}
			}
		}
		else if (beta19_fences && this.isSolid(adj_id))
		{
			// Solid block to the East

			// Bottom slat
			this.renderVertical(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z+fence_postsize_h, slat_start, fence_slat_height, 6, 3, 0, 5);
			this.renderVertical(textureId, x+fence_postsize, z-fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start, fence_slat_height, 6, 3, 0, 5);
			this.renderHorizontal(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start, 2, 6, 14, 0, false);
			this.renderHorizontal(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start+fence_slat_height, 2, 6, 14, 0, false);

			// Top slat
			this.renderVertical(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z+fence_postsize_h, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
			this.renderVertical(textureId, x+fence_postsize, z-fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
			this.renderHorizontal(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset, 2, 6, 14, 0, false);
			this.renderHorizontal(textureId, x+fence_postsize, z+fence_postsize_h, x+.5f, z-fence_postsize_h, slat_start+fence_top_slat_offset+fence_slat_height, 2, 6, 14, 0, false);
		}
		
		// Check for adjacent fences / fence gates in the -z direction
		adj_id = this.getAdjNorthBlockId(xxx, yyy, zzz, blockOffset);
		if (adj_id == blockId)
		{
			// Bottom slat
			this.renderVertical(textureId, x+fence_postsize_h, z-fence_postsize, x+fence_postsize_h, z-1f+fence_postsize, slat_start, fence_slat_height, 12, 3, 2, 5);
			this.renderVertical(textureId, x-fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-1f+fence_postsize, slat_start, fence_slat_height, 12, 3, 2, 5);
			this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-1f+fence_postsize, slat_start, 2, 12, 14, 2, true);
			this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-1f+fence_postsize, slat_start+fence_slat_height, 2, 12, 14, 2, true);

			// Top slat
			this.renderVertical(textureId, x+fence_postsize_h, z-fence_postsize, x+fence_postsize_h, z-1f+fence_postsize, slat_start+fence_top_slat_offset, fence_slat_height, 12, 3, 2, 5);
			this.renderVertical(textureId, x-fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-1f+fence_postsize, slat_start+fence_top_slat_offset, fence_slat_height, 12, 3, 2, 5);
			this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-1f+fence_postsize, slat_start+fence_top_slat_offset, 2, 12, 14, 2, true);
			this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-1f+fence_postsize, slat_start+fence_top_slat_offset+fence_slat_height, 2, 12, 14, 2, true);
		}
		else if (beta19_fences && this.isSolid(adj_id))
		{
			// Solid block to the North

			// Bottom slat
			this.renderVertical(textureId, x+fence_postsize_h, z-fence_postsize, x+fence_postsize_h, z-.5f, slat_start, fence_slat_height, 6, 3, 0, 5);
			this.renderVertical(textureId, x-fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start, fence_slat_height, 6, 3, 0, 5);
			this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start, 2, 6, 14, 0, true);
			this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start+fence_slat_height, 2, 6, 14, 0, true);

			// Top slat
			this.renderVertical(textureId, x+fence_postsize_h, z-fence_postsize, x+fence_postsize_h, z-.5f, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
			this.renderVertical(textureId, x-fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
			this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start+fence_top_slat_offset, 2, 6, 14, 0, true);
			this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start+fence_top_slat_offset+fence_slat_height, 2, 6, 14, 0, true);
		}
		else if (adj_id > -1 && blockArray[adj_id] != null && blockArray[adj_id].type == BLOCK_TYPE.FENCE_GATE)
		{
			// Fence Gate to the North
			adj_data = this.getAdjNorthBlockData(xxx, yyy, zzz);
			if (adj_data != -1)
			{
				adj_data &= 0x3;
				if (adj_data == 1 || adj_data == 3)
				{
					// Bottom slat
					this.renderVertical(textureId, x+fence_postsize_h, z-fence_postsize, x+fence_postsize_h, z-.5f, slat_start, fence_slat_height, 6, 3, 0, 5);
					this.renderVertical(textureId, x-fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start, fence_slat_height, 6, 3, 0, 5);
					this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start, 2, 6, 14, 0, true);
					this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start+fence_slat_height, 2, 6, 14, 0, true);

					// Top slat
					this.renderVertical(textureId, x+fence_postsize_h, z-fence_postsize, x+fence_postsize_h, z-.5f, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
					this.renderVertical(textureId, x-fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
					this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start+fence_top_slat_offset, 2, 6, 14, 0, true);
					this.renderHorizontal(textureId, x+fence_postsize_h, z-fence_postsize, x-fence_postsize_h, z-.5f, slat_start+fence_top_slat_offset+fence_slat_height, 2, 6, 14, 0, true);
				}
			}
		}

		// Check for adjacent fence gates in the +z direction
		adj_id = this.getAdjSouthBlockId(xxx, yyy, zzz, blockOffset);
		if (adj_id > -1 && blockArray[adj_id] != null && blockArray[adj_id].type == BLOCK_TYPE.FENCE_GATE)
		{
			// Fence Gate to the South
			adj_data = this.getAdjSouthBlockData(xxx, yyy, zzz);
			if (adj_data != -1)
			{
				adj_data &= 0x3;
				if (adj_data == 1 || adj_data == 3)
				{
					// Bottom slat
					this.renderVertical(textureId, x+fence_postsize_h, z+fence_postsize, x+fence_postsize_h, z+.5f, slat_start, fence_slat_height, 6, 3, 0, 5);
					this.renderVertical(textureId, x-fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start, fence_slat_height, 6, 3, 0, 5);
					this.renderHorizontal(textureId, x+fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start, 2, 6, 14, 0, true);
					this.renderHorizontal(textureId, x+fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start+fence_slat_height, 2, 6, 14, 0, true);

					// Top slat
					this.renderVertical(textureId, x+fence_postsize_h, z+fence_postsize, x+fence_postsize_h, z+.5f, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
					this.renderVertical(textureId, x-fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
					this.renderHorizontal(textureId, x+fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start+fence_top_slat_offset, 2, 6, 14, 0, true);
					this.renderHorizontal(textureId, x+fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start+fence_top_slat_offset+fence_slat_height, 2, 6, 14, 0, true);
				}
			}
		}
		else if (beta19_fences && this.isSolid(adj_id))
		{
			// Solid block to the South

			// Bottom slat
			this.renderVertical(textureId, x+fence_postsize_h, z+fence_postsize, x+fence_postsize_h, z+.5f, slat_start, fence_slat_height, 6, 3, 0, 5);
			this.renderVertical(textureId, x-fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start, fence_slat_height, 6, 3, 0, 5);
			this.renderHorizontal(textureId, x+fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start, 2, 6, 14, 0, true);
			this.renderHorizontal(textureId, x+fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start+fence_slat_height, 2, 6, 14, 0, true);

			// Top slat
			this.renderVertical(textureId, x+fence_postsize_h, z+fence_postsize, x+fence_postsize_h, z+.5f, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
			this.renderVertical(textureId, x-fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start+fence_top_slat_offset, fence_slat_height, 6, 3, 0, 5);
			this.renderHorizontal(textureId, x+fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start+fence_top_slat_offset, 2, 6, 14, 0, true);
			this.renderHorizontal(textureId, x+fence_postsize_h, z+fence_postsize, x-fence_postsize_h, z+.5f, slat_start+fence_top_slat_offset+fence_slat_height, 2, 6, 14, 0, true);
		}
	}
	
	/**
	 * Renders a fence gate.  Good lord, this is a heck of a function.   I continually feel like I'm
	 * going about these things in the wrong way.  Ah, well - it works.
	 *
	 * TODO: Sort out the proper texture offsets + orientations, or at least do it consistently
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param blockOffset Should be passed in from our main draw loop so we don't have to recalculate
	 */
	public void renderFenceGate(int textureId, int xxx, int yyy, int zzz, int blockOffset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;

		float post_x1 = .375f;
		float post_x2 = .5f;
		float post_z = .0625f;
		float post_y_start = -.1875f;
		float post_h = .6875f;
		float middle_w = .125f;
		float middle_y = fence_slat_start_offset + fence_slat_height;
		float middle_h = fence_slat_start_offset + fence_top_slat_offset - middle_y;
		float middle_width = .0625f;
		float open_gate = .4375f;

		byte data = getData(xxx, yyy, zzz);
		boolean open = ((data & 0x4) == 0x4);
		int dir = (data & 0x3);

		// GL stuff; only draw one way
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);
		switch (dir)
		{
			case 1:
				GL11.glRotatef(270f, 0f, 1f, 0f);
				break;
			case 2:
				GL11.glRotatef(180f, 0f, 1f, 0f);
				break;
			case 3:
				GL11.glRotatef(90f, 0f, 1f, 0f);
				break;
			case 0:
			default:
				break;
		}

		// One side post
		this.renderVertical(textureId, post_x1, post_z, post_x2, post_z, post_y_start, post_h, 2, 11, 7, 0);
		this.renderVertical(textureId, post_x1, -post_z, post_x2, -post_z, post_y_start, post_h, 2, 11, 7, 0);
		this.renderVertical(textureId, post_x1, post_z, post_x1, -post_z, post_y_start, post_h, 2, 11, 7, 0);
		this.renderVertical(textureId, post_x2, post_z, post_x2, -post_z, post_y_start, post_h, 2, 11, 7, 0);
		this.renderHorizontal(textureId, post_x1, post_z, post_x2, -post_z, post_y_start, 2, 2, 7, 7, false);
		this.renderHorizontal(textureId, post_x1, post_z, post_x2, -post_z, post_y_start+post_h, 2, 2, 7, 7, false);
		
		// The other side post
		this.renderVertical(textureId, -post_x1, post_z, -post_x2, post_z, post_y_start, post_h, 2, 11, 7, 0);
		this.renderVertical(textureId, -post_x1, -post_z, -post_x2, -post_z, post_y_start, post_h, 2, 11, 7, 0);
		this.renderVertical(textureId, -post_x1, post_z, -post_x1, -post_z, post_y_start, post_h, 2, 11, 7, 0);
		this.renderVertical(textureId, -post_x2, post_z, -post_x2, -post_z, post_y_start, post_h, 2, 11, 7, 0);
		this.renderHorizontal(textureId, -post_x1, post_z, -post_x2, -post_z, post_y_start, 2, 2, 7, 7, false);
		this.renderHorizontal(textureId, -post_x1, post_z, -post_x2, -post_z, post_y_start+post_h, 2, 2, 7, 7, false);

		// Now the gate itself
		if (open)
		{
			// One side, bottom slat
			this.renderVertical(textureId, post_x1, post_z, post_x1, open_gate, fence_slat_start_offset, fence_slat_height, 6, 3, 5, 7);
			this.renderVertical(textureId, post_x2, post_z, post_x2, open_gate, fence_slat_start_offset, fence_slat_height, 6, 3, 5, 7);
			this.renderHorizontal(textureId, post_x1, post_z, post_x2, open_gate, fence_slat_start_offset, 6, 2, 5, 7, true);
			this.renderHorizontal(textureId, post_x1, post_z, post_x2, open_gate, fence_slat_start_offset+fence_slat_height, 6, 2, 5, 7, false);

			// One side, top slat
			this.renderVertical(textureId, post_x1, post_z, post_x1, open_gate, fence_slat_start_offset+fence_top_slat_offset, fence_slat_height, 6, 3, 5, 7);
			this.renderVertical(textureId, post_x2, post_z, post_x2, open_gate, fence_slat_start_offset+fence_top_slat_offset, fence_slat_height, 6, 3, 5, 7);
			this.renderHorizontal(textureId, post_x1, post_z, post_x2, open_gate, fence_slat_start_offset+fence_top_slat_offset, 6, 2, 5, 7, true);
			this.renderHorizontal(textureId, post_x1, post_z, post_x2, open_gate, fence_slat_start_offset+fence_top_slat_offset+fence_slat_height, 6, 2, 5, 7, false);

			// One side, middle bit
			this.renderVertical(textureId, post_x1, open_gate, post_x1, open_gate-middle_w, middle_y, middle_h, 2, 3, 7, 5);
			this.renderVertical(textureId, post_x2, open_gate, post_x2, open_gate-middle_w, middle_y, middle_h, 2, 3, 7, 5);
			this.renderVertical(textureId, post_x1, open_gate-middle_w, post_x2, open_gate-middle_w, middle_y, middle_h, 2, 3, 7, 5);
			this.renderVertical(textureId, post_x1, open_gate, post_x2, open_gate, fence_slat_start_offset, fence_slat_height+fence_top_slat_offset, 2, 9, 7, 0);

			// Other side, bottom slat
			this.renderVertical(textureId, -post_x1, post_z, -post_x1, open_gate, fence_slat_start_offset, fence_slat_height, 6, 3, 5, 7);
			this.renderVertical(textureId, -post_x2, post_z, -post_x2, open_gate, fence_slat_start_offset, fence_slat_height, 6, 3, 5, 7);
			this.renderHorizontal(textureId, -post_x1, post_z, -post_x2, open_gate, fence_slat_start_offset, 6, 2, 5, 7, true);
			this.renderHorizontal(textureId, -post_x1, post_z, -post_x2, open_gate, fence_slat_start_offset+fence_slat_height, 6, 2, 5, 7, false);

			// Other side, top slat
			this.renderVertical(textureId, -post_x1, post_z, -post_x1, open_gate, fence_slat_start_offset+fence_top_slat_offset, fence_slat_height, 6, 3, 5, 7);
			this.renderVertical(textureId, -post_x2, post_z, -post_x2, open_gate, fence_slat_start_offset+fence_top_slat_offset, fence_slat_height, 6, 3, 5, 7);
			this.renderHorizontal(textureId, -post_x1, post_z, -post_x2, open_gate, fence_slat_start_offset+fence_top_slat_offset, 6, 2, 5, 7, true);
			this.renderHorizontal(textureId, -post_x1, post_z, -post_x2, open_gate, fence_slat_start_offset+fence_top_slat_offset+fence_slat_height, 6, 2, 5, 7, false);

			// Other side, middle bit
			this.renderVertical(textureId, -post_x1, open_gate, -post_x1, open_gate-middle_w, middle_y, middle_h, 2, 3, 7, 5);
			this.renderVertical(textureId, -post_x2, open_gate, -post_x2, open_gate-middle_w, middle_y, middle_h, 2, 3, 7, 5);
			this.renderVertical(textureId, -post_x1, open_gate-middle_w, -post_x2, open_gate-middle_w, middle_y, middle_h, 2, 3, 7, 5);
			this.renderVertical(textureId, -post_x1, open_gate, -post_x2, open_gate, fence_slat_start_offset, fence_slat_height+fence_top_slat_offset, 2, 9, 7, 0);
		}
		else
		{
			// Bottom bar
			this.renderVertical(textureId, post_x1, post_z, -post_x1, post_z, fence_slat_start_offset, fence_slat_height, 12, 3, 2, 7);
			this.renderVertical(textureId, post_x1, -post_z, -post_x1, -post_z, fence_slat_start_offset, fence_slat_height, 12, 3, 2, 7);
			this.renderHorizontal(textureId, post_x1, post_z, -post_x1, -post_z, fence_slat_start_offset, 12, 2, 2, 3, true);
			this.renderHorizontal(textureId, post_x1, post_z, -post_x1, -post_z, fence_slat_start_offset+fence_slat_height, 12, 2, 2, 3, true);

			// Top bar
			this.renderVertical(textureId, post_x1, post_z, -post_x1, post_z, fence_slat_start_offset+fence_top_slat_offset, fence_slat_height, 12, 3, 2, 7);
			this.renderVertical(textureId, post_x1, -post_z, -post_x1, -post_z, fence_slat_start_offset+fence_top_slat_offset, fence_slat_height, 12, 3, 2, 7);
			this.renderHorizontal(textureId, post_x1, post_z, -post_x1, -post_z, fence_slat_start_offset+fence_top_slat_offset, 12, 2, 2, 3, true);
			this.renderHorizontal(textureId, post_x1, post_z, -post_x1, -post_z, fence_slat_start_offset+fence_top_slat_offset+fence_slat_height, 12, 2, 2, 3, true);

			// Middle bit
			this.renderVertical(textureId, middle_w, post_z, -middle_w, post_z, middle_y, middle_h, 4, 3, 6, 5);
			this.renderVertical(textureId, middle_w, -post_z, -middle_w, -post_z, middle_y, middle_h, 4, 3, 6, 5);
			this.renderVertical(textureId, middle_w, middle_width, middle_w, -middle_width, middle_y, middle_h, 2, 3, 7, 5);
			this.renderVertical(textureId, -middle_w, middle_width, -middle_w, -middle_width, middle_y, middle_h, 2, 3, 7, 5);
		}

		// aaand pop our GL matrix
		GL11.glPopMatrix();
	}

	/**
	 * Renders a button.
	 */
	public void renderButton(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		
		float faceX1, faceX2;
		float faceZ1, faceZ2;
		float back_dX, back_dZ;
		float button_radius = .1f;
		
		byte data = getData(xxx, yyy, zzz);
		switch(data)
		{
		 	case 1:
	 			// East
		 		faceX1 = x-0.5f+button_radius;
		 		faceX2 = x-0.5f+button_radius;
		 		faceZ1 = z-button_radius;
		 		faceZ2 = z+button_radius;
		 		back_dX = -button_radius;
		 		back_dZ = 0;
	 			break;
		 	case 2:
		 		// West
		 		faceX1 = x+0.5f-button_radius;
		 		faceX2 = x+0.5f-button_radius;
		 		faceZ1 = z-button_radius;
		 		faceZ2 = z+button_radius;
		 		back_dX = button_radius;
		 		back_dZ = 0;
		 		break;
		 	case 3:
		 		// South
		 		faceX1 = x-button_radius;
		 		faceX2 = x+button_radius;
		 		faceZ1 = z-0.5f+button_radius;
		 		faceZ2 = z-0.5f+button_radius;
		 		back_dX = 0;
		 		back_dZ = -button_radius;
		 		break;
		 	case 4:
	 		default:
		 		// North
		 		faceX1 = x-button_radius;
		 		faceX2 = x+button_radius;
		 		faceZ1 = z+0.5f-button_radius;
		 		faceZ2 = z+0.5f-button_radius;
		 		back_dX = 0;
		 		back_dZ = button_radius;
		 		break;
		}
		
		// Button face
		this.renderVertical(textureId, faceX1, faceZ1, faceX2, faceZ2, y-button_radius, button_radius*2);
		
		// Sides
		this.renderVertical(textureId, faceX1, faceZ1, faceX1+back_dX, faceZ1+back_dZ, y-button_radius, button_radius*2);
		this.renderVertical(textureId, faceX2, faceZ2, faceX2+back_dX, faceZ2+back_dZ, y-button_radius, button_radius*2);
		
		// Top/Bottom
		this.renderHorizontal(textureId, faceX1, faceZ1, faceX2+back_dX, faceZ2+back_dZ, y-button_radius);
		this.renderHorizontal(textureId, faceX1, faceZ1, faceX2+back_dX, faceZ2+back_dZ, y+button_radius);

	}
	
	/**
	 * Portal square rendering.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param blockOffset Should be passed in from our main draw loop so we don't have to recalculate
	 * @param blockId The block ID we match on, to find out how to link up
	 */
	public void renderPortal(int textureId, int xxx, int yyy, int zzz, int blockOffset, int blockId) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		
		// Check to see where adjoining Portal spaces are, so we know which
		// faces to draw
		boolean drawWestEast = false;
		if (this.getAdjWestBlockId(xxx, yyy, zzz, blockOffset) == blockId ||
				this.getAdjEastBlockId(xxx, yyy, zzz, blockOffset) == blockId)
		{
			drawWestEast = true;
		}

		if (drawWestEast)
		{
			this.renderVertical(textureId, x-0.5f, z-0.3f, x+0.5f, z-0.3f, y-0.5f, 1.0f);
			this.renderVertical(textureId, x-0.5f, z+0.3f, x+0.5f, z+0.3f, y-0.5f, 1.0f);
		}
		else
		{
			this.renderVertical(textureId, x-0.3f, z-0.5f, x-0.3f, z+0.5f, y-0.5f, 1.0f);
			this.renderVertical(textureId, x+0.3f, z-0.5f, x+0.3f, z+0.5f, y-0.5f, 1.0f);
		}
	}
	
	/**
	 * This is a bizarre little method, and should probably be both renamed and refactored
	 * into some functions that make more sense.  It's used in a few places to determine
	 * which faces of a block we're supposed to actually render.  "transparency" is whether
	 * or not we're currently rendering transparent objects.
	 */
	public boolean checkSolid(short block) {
		if(block < 0)
		{
			return false;
		}
		else if (block == 0)
		{
			return true;
		}
		if (blockArray[block] == null)
		{
			return false;
		}
		return !blockArray[block].isSolid();
	}

	/**
	 * This method, on the other hand, is a bit more clear.  We'll return true if the
	 * block ID is solid.
	 */
	public boolean isSolid(short block)
	{
		if(block <= 0) {
			return false;
		}
		if (blockArray[block] == null)
		{
			return false;
		}
		return blockArray[block].isSolid();
	}
	
	/**
	 * Renders the body of a piston.  If the piston is retracted, we'll also make a call out
	 * to renderPistonHead to draw the actual head.  Note that we do need to have the block
	 * type passed in, so that we can tell renderPistonHead what texture to draw.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param block The actual BlockType object; needed for the piston head
	 */
	public void renderPistonBody(int textureId, int xxx, int yyy, int zzz, BlockType block, int tex_offset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		byte data = getData(xxx, yyy, zzz);
		boolean extended = ((data & 0x8) == 0x8);
		int direction = (data & 0x7);

		float tex_x = precalcSpriteSheetToTextureX[textureId];
		float tex_y = precalcSpriteSheetToTextureY[textureId]+TEX128;
		float TEX_PISTON = TEX128*3f;

		// Use GL to rotate these properly
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		// This routine draws the piston facing south, which is direction value 3
		if (direction == 1)
		{
			// Up
			GL11.glRotatef(-90f, 1f, 0f, 0f);
		}
		else if (direction == 2)
		{
			// North
			GL11.glRotatef(180f, 0f, 1f, 0f);
		}
		else if (direction == 4)
		{
			// West
			GL11.glRotatef(-90f, 0f, 1f, 0f);
		}
		else if (direction == 5)
		{
			// East
			GL11.glRotatef(90f, 0f, 1f, 0f);
		}

		// First the main body bit
		renderNonstandardHorizontal(tex_x, tex_y, TEX16, TEX_PISTON, -.49f, .25f, .49f, -.49f, .49f);
		renderNonstandardHorizontal(tex_x, tex_y, TEX16, TEX_PISTON, -.49f, .25f, .49f, -.49f, -.49f);
		renderNonstandardVertical(tex_x, tex_y, TEX16, TEX_PISTON, -.49f, .49f, .25f, -.49f, -.49f, -.49f);
		renderNonstandardVertical(tex_x, tex_y, TEX16, TEX_PISTON, .49f, .49f, .25f, .49f, -.49f, -.49f);
		renderVertical(block.texture_extra_map.get("back")+tex_offset, -.49f, -.49f, .49f, -.49f, -.49f, .98f);

		// If we're extended, draw our faceplate; if not, draw the retracted face
		if (extended)
		{
			renderVertical(block.texture_extra_map.get("front")+tex_offset, -.49f, .25f, .49f, .25f, -.49f, .98f);

			// Pop the matrix after
			GL11.glPopMatrix();
		}
		else
		{
			// Pop the matrix before
			GL11.glPopMatrix();

			renderPistonHead(block.texture_extra_map.get("head")+tex_offset, xxx, yyy, zzz, BLOCK_PISTON_HEAD, tex_offset,
					true, (block.id == BLOCK_PISTON_STICKY_BODY.id));
		}

	}

	/**
	 * Renders the head of a piston.  This can get called from either the main loop, or from
	 * renderPistonBody.  If called from renderPistonBody, "attached" should be set to true,
	 * so we know not to draw the back face and post.  The override_sticky boolean should be
	 * used (when attached is true) to specify whether to use the sticky-piston texture or
	 * not, since the data value we read will be the Body's data value, which does not
	 * indicate stickiness.  Fortunately, the remaining three bits (the direction) matches
	 * up between head and body, so we should be okay just reading the body's data for
	 * direction information.
	 *
	 * The textureId passed in should be the ID of the non-sticky texture; some logic here
	 * depends on the layout of the texture information in terrain.png
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param attached Are we attached to the piston body?
	 * @param override_sticky If attached, are we a sticky piston or a regular piston?
	 */
	public void renderPistonHead(int textureId, int xxx, int yyy, int zzz, BlockType block, int tex_offset, boolean attached, boolean override_sticky) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		byte data = getData(xxx, yyy, zzz);
		boolean sticky = ((data & 0x8) == 0x8);
		int direction = (data & 0x7);

		float side_tex_x = precalcSpriteSheetToTextureX[block.texture_extra_map.get("body")+tex_offset];
		float side_tex_y = precalcSpriteSheetToTextureY[block.texture_extra_map.get("body")+tex_offset];

		// Matrix stuff
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		// This routine draws the piston facing south, which is direction value 3
		if (direction == 1)
		{
			// Up
			GL11.glRotatef(-90f, 1f, 0f, 0f);
		}
		else if (direction == 2)
		{
			// North
			GL11.glRotatef(180f, 0f, 1f, 0f);
		}
		else if (direction == 4)
		{
			// West
			GL11.glRotatef(-90f, 0f, 1f, 0f);
		}
		else if (direction == 5)
		{
			// East
			GL11.glRotatef(90f, 0f, 1f, 0f);
		}

		// Outside edges
		renderNonstandardHorizontalTexRotate(side_tex_x, side_tex_y, TEX16, TEX128, -.49f, .25f, .49f, .49f, .49f);
		renderNonstandardHorizontalTexRotate(side_tex_x, side_tex_y, TEX16, TEX128, -.49f, .25f, .49f, .49f, -.49f);
		renderNonstandardVerticalTexRotate(side_tex_x, side_tex_y, TEX16, TEX128, -.49f, .49f, .25f, -.49f, -.49f, .49f);
		renderNonstandardVerticalTexRotate(side_tex_x, side_tex_y, TEX16, TEX128, .49f, .49f, .25f, .49f, -.49f, .49f);

		// Back face and post, if we're not attached
		if (!attached)
		{
			// Back face first
			renderVertical(textureId, -.49f, .25f, .49f, .25f, -.49f, .98f);

			// Now the post
			renderNonstandardHorizontal(side_tex_x, side_tex_y, TEX16, TEX128, -.125f, .25f, .125f, -.75f, .125f);
			renderNonstandardHorizontal(side_tex_x, side_tex_y, TEX16, TEX128, -.125f, .25f, .125f, -.75f, -.125f);
			renderNonstandardVertical(side_tex_x, side_tex_y, TEX16, TEX128, -.125f, .125f, .25f, -.125f, -.125f, -.75f);
			renderNonstandardVertical(side_tex_x, side_tex_y, TEX16, TEX128, .125f, .125f, .25f, .125f, -.125f, -.75f);
		}

		// Front face
		if (attached)
		{
			if (override_sticky)
			{
				textureId = block.texture_extra_map.get("head_sticky")+tex_offset;
			}
		}
		else
		{
			if (sticky)
			{
				textureId = block.texture_extra_map.get("head_sticky")+tex_offset;
			}
		}
		renderVertical(textureId, -.49f, .49f, .49f, .49f, -.49f, .98f);

		// Pop the matrix
		GL11.glPopMatrix();
	}
	
	/**
	 * Renders a cake
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderCake(int textureId, int xxx, int yyy, int zzz, BlockType block, int tex_offset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;

		byte bites_eaten = getData(xxx, yyy, zzz);

		int bottom_tex_idx = block.texture_extra_map.get("bottom")+tex_offset;
		int edge_tex_idx = block.texture_extra_map.get("side_uncut")+tex_offset;
		int cut_tex_idx = block.texture_extra_map.get("side_cut")+tex_offset;

		float top_tex_x = precalcSpriteSheetToTextureX[textureId]+TEX256;
		float top_tex_y = precalcSpriteSheetToTextureY[textureId]+TEX512;
		float bottom_tex_x = precalcSpriteSheetToTextureX[bottom_tex_idx]+TEX256;
		float bottom_tex_y = precalcSpriteSheetToTextureY[bottom_tex_idx]+TEX512;
		float edge_tex_x = precalcSpriteSheetToTextureX[edge_tex_idx]+TEX256;
		float edge_tex_y = precalcSpriteSheetToTextureY[edge_tex_idx]+TEX64;
		float cut_tex_x = precalcSpriteSheetToTextureX[cut_tex_idx]+TEX256;
		float cut_tex_y = precalcSpriteSheetToTextureY[cut_tex_idx]+TEX64;

		float far_tex_x, far_tex_y;
		if (bites_eaten == 0)
		{
			far_tex_x = edge_tex_x;
			far_tex_y = edge_tex_y;
		}
		else
		{
			far_tex_x = cut_tex_x;
			far_tex_y = cut_tex_y;
		}


		float tex_full_width = TEX256*14f;
		float tex_full_height = TEX512*14f;
		float tex_side_height = TEX64;
		float cake_height = .5f;
		float cake_full_width = .875f;
		float cake_full_width_h = .4375f;

		float actual_width = (6f-(float)bites_eaten)/6f;

		// Use GL to rotate these properly
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		// Note that cake will always be eaten from the West
		// Knowing that, draw the east face, first
		renderNonstandardVertical(edge_tex_x, edge_tex_y, tex_full_width, tex_side_height,
				cake_full_width_h, 0f, cake_full_width_h,
				cake_full_width_h, -.49f, -cake_full_width_h);

		// Draw the far edge next
		renderNonstandardVertical(far_tex_x, far_tex_y, tex_full_width, tex_side_height,
				cake_full_width_h-(actual_width*cake_full_width), 0f, cake_full_width_h,
				cake_full_width_h-(actual_width*cake_full_width), -.49f, -cake_full_width_h);

		// And now the sides
		renderNonstandardVertical(edge_tex_x, edge_tex_y, tex_full_width*actual_width, tex_side_height,
				cake_full_width_h, 0f, cake_full_width_h,
				cake_full_width_h-(actual_width*cake_full_width), -.49f, cake_full_width_h);
		renderNonstandardVertical(edge_tex_x, edge_tex_y, tex_full_width*actual_width, tex_side_height,
				cake_full_width_h, 0f, -cake_full_width_h,
				cake_full_width_h-(actual_width*cake_full_width), -.49f, -cake_full_width_h);

		// Now the bottom
		renderNonstandardHorizontal(bottom_tex_x, bottom_tex_y, tex_full_width, tex_full_height*actual_width,
				cake_full_width_h, cake_full_width_h,
				cake_full_width_h-(actual_width*cake_full_width), -cake_full_width_h,
				-.49f);

		// ... and the top
		renderNonstandardHorizontal(top_tex_x, top_tex_y, tex_full_width, tex_full_height*actual_width,
				cake_full_width_h, cake_full_width_h,
				cake_full_width_h-(actual_width*cake_full_width), -cake_full_width_h,
				0f);

		// Pop the matrix
		GL11.glPopMatrix();
	}
	
	/**
	 * Renders a sold pane-like object (glass pane, iron bars, etc)
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderSolidPane(int textureId, int xxx, int yyy, int zzz, int blockOffset, int blockId) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;

		boolean has_north = false;
		boolean has_south = false;
		boolean has_west = false;
		boolean has_east = false;

		float top_width = .0625f;
		int top_row_1 = 0;
		int top_row_2 = 15;
		if (blockId == BLOCK_IRON_BARS.id)
		{
			top_row_1 = 2;
			top_row_2 = 3;
		}

		short temp_id;
		temp_id = this.getAdjWestBlockId(xxx, yyy, zzz, blockOffset);
		if (temp_id == blockId || this.isSolid(temp_id))
		{
			has_west = true;
		}
		temp_id = this.getAdjEastBlockId(xxx, yyy, zzz, blockOffset);
		if (temp_id == blockId || this.isSolid(temp_id))
		{
			has_east = true;
		}
		temp_id = this.getAdjSouthBlockId(xxx, yyy, zzz, blockOffset);
		if (temp_id == blockId || this.isSolid(temp_id))
		{
			has_south = true;
		}
		temp_id = this.getAdjNorthBlockId(xxx, yyy, zzz, blockOffset);
		if (temp_id == blockId || this.isSolid(temp_id))
		{
			has_north = true;
		}

		if (!has_north && !has_south && !has_west && !has_east)
		{
			has_north = true;
			has_south = true;
			has_west = true;
			has_east = true;
		}

		// Now we should be able to actually draw stuff
		if (has_west && has_east)
		{
			this.renderVertical(textureId, x-.5f, z, x+.5f, z, y-.5f, .98f);
		}
		else
		{
			if (has_west)
			{
				this.renderVertical(textureId, x, z, x-.5f, z, y-.5f, .98f, 8, 16, 8, 0);
			}
			if (has_east)
			{
				this.renderVertical(textureId, x, z, x+.5f, z, y-.5f, .98f, 8, 16, 8, 0);
			}
		}
		if (has_west)
		{
			this.renderHorizontal(textureId, x-.5f, z+top_width, x-top_width, z, y+.48f, 1, 7, top_row_1, 0, false);
			this.renderHorizontal(textureId, x-.5f, z-top_width, x-top_width, z, y+.48f, 1, 7, top_row_2, 0, false);
		}
		if (has_east)
		{
			this.renderHorizontal(textureId, x+.5f, z+top_width, x+top_width, z, y+.48f, 1, 7, top_row_1, 0, false);
			this.renderHorizontal(textureId, x+.5f, z-top_width, x+top_width, z, y+.48f, 1, 7, top_row_2, 0, false);
		}

		if (has_north && has_south)
		{
			this.renderVertical(textureId, x, z-.5f, x, z+.5f, y-.5f, .98f);
		}
		else
		{
			if (has_south)
			{
				this.renderVertical(textureId, x, z, x, z+.5f, y-.5f, .98f, 8, 16, 8, 0);
			}
			if (has_north)
			{
				this.renderVertical(textureId, x, z, x, z-.5f, y-.5f, .98f, 8, 16, 8, 0);
			}
		}
		if (has_south)
		{
			this.renderHorizontal(textureId, x+top_width, z+.5f, x, z+top_width, y+.48f, 1, 7, top_row_1, 0, true);
			this.renderHorizontal(textureId, x-top_width, z+.5f, x, z+top_width, y+.48f, 1, 7, top_row_2, 0, true);
		}
		if (has_north)
		{
			this.renderHorizontal(textureId, x+top_width, z-.5f, x, z-top_width, y+.48f, 1, 7, top_row_1, 0, true);
			this.renderHorizontal(textureId, x-top_width, z-.5f, x, z-top_width, y+.48f, 1, 7, top_row_2, 0, true);
		}

		// Finally, the center top square.  Technically we shouldn't draw past the edge, but whatever.
		this.renderHorizontal(textureId, x+top_width, z+top_width, x-top_width, z, y+.48f, 1, 2, top_row_1, 7, false);
		this.renderHorizontal(textureId, x+top_width, z-top_width, x-top_width, z, y+.48f, 1, 2, top_row_2, 7, false);
	}
	
	/**
	 * Renders a chest
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderChest(int textureId, int xxx, int yyy, int zzz, int blockOffset, BlockType block, int tex_offset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;

		float edges = .45f;
		float bottom = .49f;
		float height = .94f;
		float full = .5f;

		byte orientation = getData(xxx, yyy, zzz);

		// Use GL to rotate these properly
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		// Find out if we have adjacent chests, and rotate.  Our "have_right" and
		// "have_left" booleans are a little bit at odds with the orientation of the
		// chest itself, since my "left/right" is looking *at* the front of the
		// chest, not pointing out in the direction the chest is facing.  Alas.
		boolean have_left = false;
		boolean have_right = false;
		switch(orientation)
		{
			case 4:
				// Facing West
				have_right = (getAdjSouthBlockId(xxx, yyy, zzz, blockOffset) == block.id);
				have_left = (getAdjNorthBlockId(xxx, yyy, zzz, blockOffset) == block.id);
				GL11.glRotatef(270f, 0f, 1f, 0f);
				break;
			case 5:
				// Facing East
				have_right = (getAdjNorthBlockId(xxx, yyy, zzz, blockOffset) == block.id);
				have_left = (getAdjSouthBlockId(xxx, yyy, zzz, blockOffset) == block.id);
				GL11.glRotatef(90f, 0f, 1f, 0f);
				break;
			case 2:
				// Facing North
				have_right = (getAdjWestBlockId(xxx, yyy, zzz, blockOffset) == block.id);
				have_left = (getAdjEastBlockId(xxx, yyy, zzz, blockOffset) == block.id);
				GL11.glRotatef(180f, 0f, 1f, 0f);
				break;
			case 3:
			default:
				// Facing South (this is what chests with a data of "0" will show up as,
				// weirdly, in Minecraft itself)
				have_right = (getAdjEastBlockId(xxx, yyy, zzz, blockOffset) == block.id);
				have_left = (getAdjWestBlockId(xxx, yyy, zzz, blockOffset) == block.id);
				break;
		}

		// Render appropriately
		if (have_left)
		{
			renderVertical(block.texture_extra_map.get("front_big_right")+tex_offset, -full, edges, edges, edges, -bottom, height);
			renderVertical(block.texture_extra_map.get("back_big_right")+tex_offset, -full, -edges, edges, -edges, -bottom, height);
			renderVertical(block.texture_extra_map.get("side_small")+tex_offset, edges, -edges, edges, edges, -bottom, height);
			renderHorizontal(block.texture_extra_map.get("top")+tex_offset, -full, -edges, edges, edges, edges, 8, 16, 8, 0, true);
			renderHorizontal(block.texture_extra_map.get("top")+tex_offset, -full, -edges, edges, edges, -bottom, 8, 16, 8, 0, true);
		}
		else if (have_right)
		{
			renderVertical(block.texture_extra_map.get("front_big_left")+tex_offset, -edges, edges, full, edges, -bottom, height);
			renderVertical(block.texture_extra_map.get("back_big_left")+tex_offset, -edges, -edges, full, -edges, -bottom, height);
			renderVertical(block.texture_extra_map.get("side_small")+tex_offset, -edges, -edges, -edges, edges, -bottom, height);
			renderHorizontal(block.texture_extra_map.get("top")+tex_offset, -edges, -edges, full, edges, edges, 8, 16, 0, 0, true);
			renderHorizontal(block.texture_extra_map.get("top")+tex_offset, -edges, -edges, full, edges, -bottom, 8, 16, 0, 0, true);
		}
		else
		{
			renderVertical(textureId, -edges, edges, edges, edges, -bottom, height);
			renderVertical(block.texture_extra_map.get("side_small")+tex_offset, -edges, -edges, edges, -edges, -bottom, height);
			renderVertical(block.texture_extra_map.get("side_small")+tex_offset, -edges, -edges, -edges, edges, -bottom, height);
			renderVertical(block.texture_extra_map.get("side_small")+tex_offset, edges, -edges, edges, edges, -bottom, height);
			renderHorizontal(block.texture_extra_map.get("top")+tex_offset, -edges, -edges, edges, edges, edges);
			renderHorizontal(block.texture_extra_map.get("top")+tex_offset, -edges, -edges, edges, edges, -bottom);
		}

		// Pop the matrix
		GL11.glPopMatrix();
	}

	
	/**
	 * Renders a stem.  Will check for adjacent blocks, but note that this is very particular about
	 * block naming.  If the stem name is MELON_STEM, we'll be checking for MELON.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderStem(int textureId, int xxx, int yyy, int zzz, int blockOffset, BlockType block, int tex_offset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;

		byte data = getData(xxx, yyy, zzz);
		float rotate = 0f;
		boolean connected = false;

		// Look for adjacent blocks if it makes sense to do so
		if (data == 7)
		{
			String[] nameparts = null;
			try
			{
				nameparts = block.idStr.split("_");
			}
			catch (PatternSyntaxException e)
			{
			}

			if (nameparts != null && nameparts.length > 1)
			{
				short adjblock;
				// Stems will prefer: West, East, North, South
				adjblock = getAdjWestBlockId(xxx,yyy,zzz,blockOffset);
				if (adjblock > 0 && blockArray[adjblock] != null &&
						blockArray[adjblock].idStr.equals(nameparts[0]))
				{
					connected = true;
				}
				if (!connected)
				{
					adjblock = getAdjEastBlockId(xxx,yyy,zzz,blockOffset);
					if (adjblock > 0 && blockArray[adjblock] != null &&
							blockArray[adjblock].idStr.equals(nameparts[0]))
					{
						connected = true;
						rotate = 180f;
					}
				}
				if (!connected)
				{
					adjblock = getAdjNorthBlockId(xxx,yyy,zzz,blockOffset);
					if (adjblock > 0 && blockArray[adjblock] != null &&
							blockArray[adjblock].idStr.equals(nameparts[0]))
					{
						connected = true;
						rotate = 270f;
					}
				}
				if (!connected)
				{
					adjblock = getAdjSouthBlockId(xxx,yyy,zzz,blockOffset);
					if (adjblock > 0 && blockArray[adjblock] != null &&
							blockArray[adjblock].idStr.equals(nameparts[0]))
					{
						connected = true;
						rotate = 90f;
					}
				}
			}
		}

		if (connected)
		{
			int curve_tex = block.texture_extra_map.get("curve")+tex_offset;
			TextureDecorationStats stats = XRay.decorationStats.get(curve_tex % 256);
			float tex_begin_x = precalcSpriteSheetToTextureX[curve_tex] + stats.getTexLeft();
			float tex_begin_y = precalcSpriteSheetToTextureY[curve_tex] + stats.getTexTop();
			float tex_width = stats.getTexWidth();
			float tex_height = stats.getTexHeight();

			float width = stats.getWidth();
			float height = stats.getHeight();

			// Use GL to rotate these properly
			GL11.glPushMatrix();
			GL11.glTranslatef(x, y, z);
			GL11.glRotatef(rotate, 0f, 1f, 0f);

			this.renderNonstandardVertical(tex_begin_x, tex_begin_y, tex_width, tex_height,
				-.5f, -.5f+height, 0f,
				-.5f+width, -.5f, 0f);

			// Pop the matrix
			GL11.glPopMatrix();
		}
		else
		{
			// TODO: technically this should offset with the data value
			this.renderCrossDecoration(textureId, xxx, yyy, zzz);
		}
	}
	
	/**
	 * Renders a cauldron
	 *
	 * TODO: technically this should extend for the entire length of the block; I didn't feel
	 * like running checks of the adjacent blocks, though, so it's a bit smaller than it should be
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderCauldron(int textureId, int xxx, int yyy, int zzz, int blockOffset, BlockType block, int tex_offset)
	{
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		float edge = .48f;
		float bottom = -.5f;
		float height = 1f;
		float base = -.3125f;
		float inside = (.375f*(edge*2f)); // Modified to account for scaling
		float insideheight = 1f - (.5f + base);

		byte data = getData(xxx, yyy, zzz);
		if (data > 3)
		{
			data = 3;
		}

		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		int inside_tex = block.texture_extra_map.get("inside")+tex_offset;

		// Base
		renderHorizontal(inside_tex, edge, edge, -edge, -edge, base);

		// Sides
		renderVertical(textureId, edge, edge, -edge, edge, bottom, height);
		renderVertical(textureId, edge, -edge, -edge, -edge, bottom, height);
		renderVertical(textureId, edge, edge, edge, -edge, bottom, height);
		renderVertical(textureId, -edge, edge, -edge, -edge, bottom, height);

		// Inside faces
		renderVertical(textureId, inside, inside, -inside, inside, base, insideheight);
		renderVertical(textureId, inside, -inside, -inside, -inside, base, insideheight);
		renderVertical(textureId, inside, inside, inside, -inside, base, insideheight);
		renderVertical(textureId, -inside, inside, -inside, -inside, base, insideheight);

		// Contents
		if (data > 0)
		{
			renderHorizontal(BLOCK_WATER.tex_idx+tex_offset, inside, inside, -inside, -inside, base+((insideheight-.0625f)*(data/3f)));
		}

		// Top
		renderHorizontal(block.texture_extra_map.get("top")+tex_offset, edge, edge, -edge, -edge, height-.5f);

		GL11.glPopMatrix();
	}
	
	/**
	 * Renders an enchantment table.  
	 * 
	 * TODO: technically this should extend for the entire length of the block; I didn't feel
	 * like running checks of the adjacent blocks, though, so it's a bit smaller than it should be
	 *
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderEnchantmentTable(int textureId, int xxx, int yyy, int zzz, int blockOffset, BlockType block, int tex_offset)
	{
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		float edge = .48f;
		float height = .75f;
		float bottom = -.5f;

		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		int side_tex = block.texture_extra_map.get("sides") + tex_offset;

		// Top
		renderHorizontal(textureId, edge, edge, -edge, -edge, height+bottom);

		// Bottom
		if (y == 0 || !isSolid(getAdjDownBlockId(xxx, yyy, zzz, blockOffset)))
		{
			renderHorizontal(block.texture_extra_map.get("bottom")+tex_offset, edge, edge, -edge, -edge, bottom);
		}

		// Sides
		renderVertical(side_tex, edge, edge, -edge, edge, bottom, height, 16, 12, 0, 4);
		renderVertical(side_tex, edge, -edge, -edge, -edge, bottom, height, 16, 12, 0, 4);
		renderVertical(side_tex, edge, edge, edge, -edge, bottom, height, 16, 12, 0, 4);
		renderVertical(side_tex, -edge, edge, -edge, -edge, bottom, height, 16, 12, 0, 4);

		GL11.glPopMatrix();
	}

	/**
	 * Helper function for renderHalfHeight - given an adjacent block ID, it
	 * will return true if we should render that "side"
	 */
	public boolean shouldRenderHalfHeightAdj(short adj_block)
	{
		if (adj_block < 0)
		{
			return true;
		}
		if (blockArray[adj_block] == null)
		{
			return true;
		}
		return (!blockArray[adj_block].isSolid() &&
				blockArray[adj_block].type != BLOCK_TYPE.HALFHEIGHT);
	}
	
	/**
	 * Renders a half-height block
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderHalfHeight(int textureId, int xxx, int yyy, int zzz, int blockOffset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;

		// Sides
		if (shouldRenderHalfHeightAdj(getAdjNorthBlockId(xxx, yyy, zzz, blockOffset)))
		{
			this.renderNorthSouth(textureId, x, y, z, 0f, .5f);
		}
		if (shouldRenderHalfHeightAdj(getAdjSouthBlockId(xxx, yyy, zzz, blockOffset)))
		{
			this.renderNorthSouth(textureId, x, y, z+1, 0f, .5f);
		}
		if (shouldRenderHalfHeightAdj(getAdjWestBlockId(xxx, yyy, zzz, blockOffset)))
		{
			this.renderWestEast(textureId, x, y, z, 0f, .5f);
		}
		if (shouldRenderHalfHeightAdj(getAdjEastBlockId(xxx, yyy, zzz, blockOffset)))
		{
			this.renderWestEast(textureId, x+1, y, z, 0f, .5f);
		}
		
		// Bottom
		boolean render_bottom = true;
		if (y > 0)
		{
			short bottom = getAdjDownBlockId(xxx, yyy, zzz, blockOffset);
			render_bottom = (bottom == 0 || (bottom > 0 && blockArray[bottom] != null && !blockArray[bottom].isSolid()));
		}
		if (render_bottom)
		{
			this.renderTopDown(textureId, x, y, z);
		}

		// Always render the top
		this.renderTopDown(textureId, x, y+0.5f, z);	
	}
	
	/**
	 * Renders a semisolid or water block
	 *
	 * TODO: We're doing some ridiculousness here to avoid z-fighting when we're is up against
	 * halfheight blocks.  When we draw our face on top of an already-existing face with
	 * the exact same geometry (say, a "full" solid block), the textures combine perfectly and
	 * we're all happy.  If the polygons are at all differently-sized, though, we get z-fighting
	 * and it's terrible.  I'd love to know why that is (or, more to the point, why we *don't*
	 * get z-fighting when the geometry is equal).  Apparently if we move to using shaders,
	 * using multitexturing is pretty easy, so we may want to look at doing that.  Whatever that
	 * actually is.  Someday I should learn how to actually use this stuff, yeah?
	 *
	 * TODO: sight, West and South halfheight stuff doesn't seem to actually render for some reason,
	 * it gets overwritten by the halfheight texture itself.  I'd love to figure out why all of
	 * this is happening.  Still, IMO it's better than the z-fighting, so for now I'll leave it.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderSemisolid(int textureId, int xxx, int yyy, int zzz, int blockOffset, int blockId) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		short adj;

		// Sides
		adj = getAdjNorthBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != blockId)
		{
			if (adj > 0 && blockArray[adj] != null && blockArray[adj].type == BLOCK_TYPE.HALFHEIGHT)
			{
				this.renderVertical(textureId, x-.5f, z-.5f, x+.5f, z-.5f, y, .5f, 16, 8, 0, 0);
				this.renderVertical(textureId, x-.5f, z-.5f, x+.5f, z-.5f, y-.5f, .5f, 16, 8, 0, 8);
			}
			else
			{
				this.renderVertical(textureId, x-.5f, z-.5f, x+.5f, z-.5f, y-.5f, 1f);
			}
		}
		adj = getAdjSouthBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != blockId)
		{
			if (adj > 0 && blockArray[adj] != null && blockArray[adj].type == BLOCK_TYPE.HALFHEIGHT)
			{
				this.renderVertical(textureId, x-.5f, z+.5f, x+.5f, z+.5f, y, .5f, 16, 8, 0, 0);
				this.renderVertical(textureId, x-.5f, z+.5f, x+.5f, z+.5f, y-.5f, .5f, 16, 8, 0, 8);
			}
			else
			{
				this.renderVertical(textureId, x-.5f, z+.5f, x+.5f, z+.5f, y-.5f, 1f);
			}
		}
		adj = getAdjWestBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != blockId)
		{
			if (adj > 0 && blockArray[adj] != null && blockArray[adj].type == BLOCK_TYPE.HALFHEIGHT)
			{
				this.renderVertical(textureId, x-.5f, z+.5f, x-.5f, z-.5f, y, .5f, 16, 8, 0, 0);
				this.renderVertical(textureId, x-.5f, z+.5f, x-.5f, z-.5f, y-.5f, .5f, 16, 8, 0, 8);
			}
			else
			{
				this.renderVertical(textureId, x-.5f, z+.5f, x-.5f, z-.5f, y-.5f, 1f);
			}
		}
		adj = getAdjEastBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != blockId)
		{
			if (adj > 0 && blockArray[adj] != null && blockArray[adj].type == BLOCK_TYPE.HALFHEIGHT)
			{
				this.renderVertical(textureId, x+.5f, z+.5f, x+.5f, z-.5f, y, .5f, 16, 8, 0, 0);
				this.renderVertical(textureId, x+.5f, z+.5f, x+.5f, z-.5f, y-.5f, .5f, 16, 8, 0, 8);
			}
			else
			{
				this.renderVertical(textureId, x+.5f, z+.5f, x+.5f, z-.5f, y-.5f, 1f);
			}
		}
		
		// Bottom
		boolean render_bottom = true;
		if (y > 0)
		{
			render_bottom = this.getAdjDownBlockId(xxx, yyy, zzz, blockOffset) != blockId;
		}
		if (render_bottom)
		{
			this.renderTopDown(textureId, x, y, z);
		}

		// Top
		boolean render_top = true;
		if (y < 127)
		{
			render_top = this.getAdjUpBlockId(xxx, yyy, zzz, blockOffset) != blockId;
		}
		if (render_top)
		{
			this.renderTopDown(textureId, x, y+1f, z);
		}
	}

	/**
	 * Renders an End Portal block.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param blockOffset
	 * @param blockId
	 */
	public void renderEndPortal(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		
		renderHorizontal(textureId, x-.5f, z-.5f, x+.5f, z+.5f, y+.25f);
	}

	/**
	 * Renders an End Portal Frame block.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param blockOffset
	 * @param blockId
	 */
	public void renderEndPortalFrame(int textureId, int xxx, int yyy, int zzz, int blockOffset, BlockType block, int tex_offset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		float side_height = .8125f;
		float side = .5f;
		float side_top = .3125f;
		float eye_side = .25f;
		float eye_height = .1875f;
		byte data = getData(xxx, yyy, zzz);
		boolean eye = ((data & 0x4) == 0x4);
		short adj;

		int tex_side = block.texture_extra_map.get("sides")+tex_offset;
		int tex_bottom = block.texture_extra_map.get("bottom")+tex_offset;
		int tex_eye = block.texture_extra_map.get("eye")+tex_offset;

		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		// First draw the base, regardless of eye state
		adj = getAdjWestBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != block.id && !isSolid(adj))
		{
			renderVertical(tex_side, -side, -side, -side, side, -side, side_height, 16, 13, 0, 3);
		}
		adj = getAdjEastBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != block.id && !isSolid(adj))
		{
			renderVertical(tex_side, side, -side, side, side, -side, side_height, 16, 13, 0, 3);
		}
		adj = getAdjSouthBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != block.id && !isSolid(adj))
		{
			renderVertical(tex_side, side, side, -side, side, -side, side_height, 16, 13, 0, 3);
		}
		adj = getAdjNorthBlockId(xxx, yyy, zzz, blockOffset);
		if (adj != block.id && !isSolid(adj))
		{
			renderVertical(tex_side, side, -side, -side, -side, -side, side_height, 16, 13, 0, 3);
		}
		renderHorizontal(textureId, -side, -side, side, side, side_top);
		renderHorizontal(tex_bottom, -side, -side, side, side, -side);

		// Now the Eye of Ender, if we should
		if (eye)
		{
			renderVertical(tex_eye, eye_side, eye_side, -eye_side, eye_side, side_top, eye_height, 8, 3, 4, 0);
			renderVertical(tex_eye, eye_side, -eye_side, -eye_side, -eye_side, side_top, eye_height, 8, 3, 4, 0);
			renderVertical(tex_eye, eye_side, eye_side, eye_side, -eye_side, side_top, eye_height, 8, 3, 4, 0);
			renderVertical(tex_eye, -eye_side, eye_side, -eye_side, -eye_side, side_top, eye_height, 8, 3, 4, 0);
			renderHorizontal(tex_eye, -eye_side, -eye_side, eye_side, eye_side, side, 8, 8, 4, 4, false);
		}

		GL11.glPopMatrix();
	}
	
	/**
	 * Renders a brewing stand
	 *
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderBrewingStand(int textureId, int xxx, int yyy, int zzz, BlockType block, int tex_offset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;

		float zero = 0f;
		float one = .0625f;
		float two = .125f;
		float three = .1875f;
		float four = .25f;
		float five = .3125f;
		float six = .375f;
		float seven = .4375f;
		float eight= .5f;
		float nine = .5625f;
		float postheight = .875f;

		TextureDecorationStats stats = XRay.decorationStats.get(textureId % 256);
		if (stats == null)
		{
			return;
		}
		byte data = getData(xxx, yyy, zzz);
		int tex_base = block.texture_extra_map.get("base") + tex_offset;

		float potion_on_begin_x = precalcSpriteSheetToTextureX[textureId] + stats.getTexLeft();
		float potion_on_width = TEX32 - TEX256 - stats.getTexLeft();
		float potion_off_begin_x = precalcSpriteSheetToTextureX[textureId] + TEX32 + TEX256;
		float potion_off_width = stats.getTexRight() - TEX32;

		float potion_begin_y = precalcSpriteSheetToTextureY[textureId] + stats.getTexTop();
		float potion_height = TEX32 - stats.getTexTop() - TEX256;

		float potion_abs_top = eight - stats.getTexTop();
		float potion_abs_bottom = -six;
		float potion_on_abs_length = .5f-stats.getLeft();
		float potion_off_abs_length = 1f-stats.getRight();

		boolean potion_0x1 = false;  // East
		boolean potion_0x2 = false;  // Northwest
		boolean potion_0x4 = false;  // Southwest
		if ((data & 0x1) == 0x1) { potion_0x1 = true; }
		if ((data & 0x2) == 0x2) { potion_0x2 = true; }
		if ((data & 0x4) == 0x4) { potion_0x4 = true; }

		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		// Center post
		renderVertical(textureId, one, one, -one, one, -eight, postheight, 2, 14, 7, 2);
		renderVertical(textureId, one, -one, -one, -one, -eight, postheight, 2, 14, 7, 2);
		renderVertical(textureId, one, one, one, -one, -eight, postheight, 2, 14, 7, 2);
		renderVertical(textureId, -one, one, -one, -one, -eight, postheight, 2, 14, 7, 2);
		renderHorizontal(textureId, one, one, -one, -one, six, 2, 2, 7, 7, false);

		// Base, potion 0x1
		renderHorizontal(tex_base, one, -three, seven, three, -seven, 6, 6, 9, 6, true);
		renderVertical(tex_base, one, -three, seven, -three, -eight, one, 6, 1, 0, 0);
		renderVertical(tex_base, one, three, seven, three, -eight, one, 6, 1, 0, 0);
		renderVertical(tex_base, one, three, one, -three, -eight, one, 6, 1, 0, 0);
		renderVertical(tex_base, seven, three, seven, -three, -eight, one, 6, 1, 0, 0);

		// Base, potion 0x2
		renderHorizontal(tex_base, -six, -one, zero, -seven, -seven, 6, 6, 2, 1, true);
		renderVertical(tex_base, -six, -one, zero, -one, -eight, one, 6, 1, 0, 0);
		renderVertical(tex_base, -six, -seven, zero, -seven, -eight, one, 6, 1, 0, 0);
		renderVertical(tex_base, -six, -one, -six, -seven, -eight, one, 6, 1, 0, 0);
		renderVertical(tex_base, zero, -one, zero, -seven, -eight, one, 6, 1, 0, 0);

		// Base, potion 0x4
		renderHorizontal(tex_base, -six, seven, zero, one, -seven, 6, 6, 2, 9, true);
		renderVertical(tex_base, -six, seven, zero, seven, -eight, one, 6, 1, 0, 0);
		renderVertical(tex_base, -six, one, zero, one, -eight, one, 6, 1, 0, 0);
		renderVertical(tex_base, -six, seven, -six, one, -eight, one, 6, 1, 0, 0);
		renderVertical(tex_base, zero, seven, zero, one, -eight, one, 6, 1, 0, 0);

		// Potion 0x1
		if (potion_0x1)
		{
			renderNonstandardVertical(potion_on_begin_x, potion_begin_y, potion_on_width, potion_height,
				potion_on_abs_length, potion_abs_top, 0,
				one, potion_abs_bottom, 0);
		}
		else
		{
			renderNonstandardVertical(potion_off_begin_x, potion_begin_y, potion_off_width, potion_height,
				one, potion_abs_top, 0,
				potion_off_abs_length, potion_abs_bottom, 0);
		}

		// Potion 0x2
		if (potion_0x2)
		{
			float dist = -(float)Math.sqrt(Math.pow(potion_on_abs_length, 2)/2);
			renderNonstandardVertical(potion_on_begin_x, potion_begin_y, potion_on_width, potion_height,
				dist, potion_abs_top, dist,
			    -one, potion_abs_bottom, -one);
		}
		else
		{
			float dist = -(float)Math.sqrt(Math.pow(potion_off_abs_length, 2)/2);
			renderNonstandardVertical(potion_off_begin_x, potion_begin_y, potion_off_width, potion_height,
			    -one, potion_abs_top, -one,
				dist, potion_abs_bottom, dist);
		}

		// Potion 0x4
		if (potion_0x4)
		{
			float dist = -(float)Math.sqrt(Math.pow(potion_on_abs_length, 2)/2);
			renderNonstandardVertical(potion_on_begin_x, potion_begin_y, potion_on_width, potion_height,
				dist, potion_abs_top, -dist,
			    -one, potion_abs_bottom, one);
		}
		else
		{
			float dist = -(float)Math.sqrt(Math.pow(potion_off_abs_length, 2)/2);
			renderNonstandardVertical(potion_off_begin_x, potion_begin_y, potion_off_width, potion_height,
			    -one, potion_abs_top, one,
				dist, potion_abs_bottom, -dist);
		}

		GL11.glPopMatrix();
	}

	/**
	 * Renders a dragon egg.  Note that we're just drawing it as a slightly smaller, boxy object
	 * for now.  Making it look like an actual egg looks like too much work.
	 *
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderDragonEgg(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;

		float bottom = -.5f;
		float bottom_base = -.48f;
		float top = .25f;
		float height = .75f;
		float sides = .4375f;

		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		// Sides
		renderVertical(textureId, -sides, -sides, sides, -sides, bottom, height, 14, 12, 1, 4);
		renderVertical(textureId, -sides, sides, sides, sides, bottom, height, 14, 12, 1, 4);
		renderVertical(textureId, -sides, sides, -sides, -sides, bottom, height, 14, 12, 1, 4);
		renderVertical(textureId, sides, sides, sides, -sides, bottom, height, 14, 12, 1, 4);

		// Top + Bottom
		renderHorizontal(textureId, -sides, -sides, sides, sides, top, 14, 14, 1, 1, false);
		renderHorizontal(textureId, -sides, -sides, sides, sides, bottom_base, 14, 14, 1, 1, false);

		GL11.glPopMatrix();
	}
	
	/**
	 * Tests if the given source block has a torch nearby.  This is, I'm willing
	 * to bet, the least efficient way possible of doing this.  It turns out that
	 * despite that, it doesn't really have a noticeable impact on performance,
	 * which is why it remains in here, but perhaps one day I'll rewrite this
	 * stuff to be less stupid.  The one upside to doing it like this is that
	 * we're not using any extra memory storing data about which block should be
	 * highlighted...
	 *
	 * TODO: should implement this here, instead.
	 * 
	 * @param sx
	 * @param sy
	 * @param sz
	 * @return
	 */
	public abstract boolean hasAdjacentTorch(int sx, int sy, int sz);

	public void renderWorldSolids(int sheet)
	{
		renderWorld(RENDER_PASS.SOLIDS, sheet, null);
	}

	public void renderWorldNonstandard(int sheet)
	{
		renderWorld(RENDER_PASS.NONSTANDARD, sheet, null);
	}

	public void renderWorldGlass(int sheet)
	{
		renderWorld(RENDER_PASS.GLASS, sheet, null);
	}

	public void renderWorldSelected(int sheet, boolean[] selectedMap)
	{
		renderWorld(RENDER_PASS.SELECTED, sheet, selectedMap);
	}
	
	/**
	 * Renders our chunk.
	 * 
	 * @param pass What pass of rendering are we processing?
	 * @param sheet Which texture sheet are we currently rendering?
	 * @param selectedMap If in RENDER_PASS.SELECTED, here's a HashMap to which ones to highlight.
	 */
	public void renderWorld(RENDER_PASS pass, int sheet, boolean[] selectedMap) {

		float worldX = this.x*16;
		float worldZ = this.z*16;
		
		boolean draw = false;
		boolean above = true;
		boolean below = true;
		boolean north = true;
		boolean south = true;
		boolean east = true;
		boolean west = true;
		int tex_offset = 0;
		BlockType block;
		boolean adj_torch;
		boolean highlightingOres = (XRay.toggle.highlightOres != XRay.HIGHLIGHT_TYPE.OFF);
		short t;
		int xOff, zOff, blockOffset;
		int x, y, z;
		int textureId;
		byte data;
		int north_t, south_t, west_t, east_t, top_t, bottom_t;

		// This is to support dynamically highlighting "regular" blocks based on their
		// face.  It's quite slow to do the tinting down at the bottom on a per-block
		// basis, so instead we're going to loop through each face.  (Meaning that for solid
		// blocks, we're looping through the whole chunk four times, in addition to all
		// the other passes that the other functions use.  Alas!)
		SOLID_PASS[] loopPasses;
		if (pass == RENDER_PASS.SOLIDS || pass == RENDER_PASS.SELECTED)
		{
			loopPasses = new SOLID_PASS[] { SOLID_PASS.TOP, SOLID_PASS.BOTTOM, SOLID_PASS.EASTWEST, SOLID_PASS.NORTHSOUTH };
		}
		else
		{
			loopPasses = new SOLID_PASS[] { SOLID_PASS.TOP };
		}

		for (SOLID_PASS loopPass :  loopPasses)
		{
			// If we're rendering "selected" stuff, we want the main XRay
			// loop to be determining our color
			if (pass != RENDER_PASS.SELECTED || !highlightingOres)
			{
				switch (loopPass)
				{
					case TOP:
						GL11.glColor3f(1f, 1f, 1f);
						break;
					case BOTTOM:
						GL11.glColor3f(.5f, .5f, .5f);
						break;
					case EASTWEST:
						GL11.glColor3f(.83f, .83f, .83f);
						break;
					case NORTHSOUTH:
						GL11.glColor3f(.66f, .66f, .66f);
						break;
				}
			}
		
			this.rewindLoop();
			t = 0;
			while (t != -2)
			{
				// Grab our block type
				t = this.nextBlock();
				if(t < 1) {
					continue;
				}

				adj_torch = false;

				// Get the actual BlockType object
				block = blockArray[t];
				if (block == null)
				{
					//XRay.logger.debug("Unknown block ID: " + t);
					block = BLOCK_UNKNOWN;
				}

				// Check our texture sheet
				if (sheet != block.getTexSheet())
				{
					continue;
				}
				
				// Doublecheck for water
				if ((pass != RENDER_PASS.NONSTANDARD && block.type == BLOCK_TYPE.WATER) ||
						(!XRay.toggle.render_water && block.type == BLOCK_TYPE.WATER))
				{
					continue;
				}

				// Doublecheck for glass stuffs
				if ((pass == RENDER_PASS.GLASS && (block.type != BLOCK_TYPE.GLASS && block.type != BLOCK_TYPE.SOLID_PANE)) ||
						(pass != RENDER_PASS.SELECTED &&
						 pass != RENDER_PASS.GLASS && (block.type == BLOCK_TYPE.GLASS || block.type == BLOCK_TYPE.SOLID_PANE)))
				{
					continue;
				}
				
				// Grab our texture ID and verify it
				textureId = block.tex_idx;
				if(textureId == -1) {
					//XRay.logger.debug("Unknown block id: " + t);
					continue;
				}
				
				// Set up our intitial drawing parameters
				switch (pass)
				{
					case SOLIDS:
						if (!block.isSolid())
						{
							continue;
						}
						draw = false;
						above = true;
						below = true;
						north = true;
						south = true;
						east = true;
						west = true;

						// Check for adjacent blocks
						if (XRay.toggle.render_bedrock && t == BLOCK_BEDROCK.id)
						{
							// This block of code was more or less copied/modified directly from the "else" block
							// below - should see if there's a way we can abstract this instead.  Also, I suspect
							// that this is where we'd fix water rendering...
							
							switch (loopPass)
							{
								case TOP:
									// check above
									if(this.getAdjUpBlockId(this.lx, this.ly, this.lz, this.lOffset) != BLOCK_BEDROCK.id) {
										draw = true;
										above = false;
									}
									break;

								case BOTTOM:
									// check below
									if(this.getAdjDownBlockId(this.lx, this.ly, this.lz, this.lOffset) != BLOCK_BEDROCK.id) {
										draw = true;
										below = false;
									}
									break;

								case NORTHSOUTH:
									// check north;
									if (this.getAdjNorthBlockId(this.lx, this.ly, this.lz, this.lOffset) != BLOCK_BEDROCK.id) {
										draw = true;
										north = false;
									}
								
									// check south
									if (this.getAdjSouthBlockId(this.lx, this.ly, this.lz, this.lOffset) != BLOCK_BEDROCK.id) {
										draw = true;
										south = false;
									}
									break;

								case EASTWEST:
									// check east
									if (this.getAdjEastBlockId(this.lx, this.ly, this.lz, this.lOffset) != BLOCK_BEDROCK.id) {
										draw = true;
										east = false;
									}
									
									// check west
									if (this.getAdjWestBlockId(this.lx, this.ly, this.lz, this.lOffset) != BLOCK_BEDROCK.id) {
										draw = true;
										west = false;
									}
									break;
							}
						}
						else
						{
							switch (loopPass)
							{
								case TOP:
									// check above
									if(checkSolid(this.getAdjUpBlockId(this.lx, this.ly, this.lz, this.lOffset))) {
										draw = true;
										above = false;
									}
									break;

								case BOTTOM:
									// check below
									if(checkSolid(this.getAdjDownBlockId(this.lx, this.ly, this.lz, this.lOffset))) {
										draw = true;
										below = false;
									}
									break;
							
								case NORTHSOUTH:
									// check north;
									if (checkSolid(this.getAdjNorthBlockId(this.lx, this.ly, this.lz, this.lOffset))) {
										draw = true;
										north = false;
									}
								
									// check south
									if (checkSolid(this.getAdjSouthBlockId(this.lx, this.ly, this.lz, this.lOffset))) {
										draw = true;
										south = false;
									}
									break;
							
								case EASTWEST:
									// check east
									if (checkSolid(this.getAdjEastBlockId(this.lx, this.ly, this.lz, this.lOffset))) {
										draw = true;
										east = false;
									}
									
									// check west
									if (checkSolid(this.getAdjWestBlockId(this.lx, this.ly, this.lz, this.lOffset))) {
										draw = true;
										west = false;
									}
									break;
							}
						}
						break;

					case NONSTANDARD:
						if (block.isSolid())
						{
							continue;
						}
						draw = true;
						break;

					case GLASS:
						// If we got here, our checks above would have made sure that we're
						// only dealing with the proper materials.
						draw = true;
						break;

					case SELECTED:
						draw = false;
						for(int i=0;i<selectedMap.length;i++) {
							if(selectedMap[i] && level.HIGHLIGHT_ORES[i] == t) {
								// TODO: should maybe check our boundaries for similar ores, like we do for regular blocks
								draw = true;
								above = false;
								below = false;
								north = false;
								south = false;
								east = false;
								west = false;
								break;
							}
						}
						break;

					default:
						// Should never get here
						continue;
				}
				
				// Continue on to the actual rendering
				if (draw)
				{
					// Check to see if this block type has a texture ID which changes depending
					// on the block's data value
					if (block.texture_data_map != null)
					{
						data = getData(this.lx, this.ly, this.lz);

						if (t == BLOCK_SAPLING.id)
						{
							// Special-case here for Sapling data, since we can't trust the upper two bits
							data &= 0x3;
						}
						else
						{
							// ... otherwise, just make sure we're dealing with the bottom four
							data &= 0xF;
						}

						// Now try to get the new texture
						try
						{
							textureId = block.texture_data_map.get(data);
						}
						catch (NullPointerException e)
						{
							// Just report and continue
							XRay.logger.debug("Unknown data value for block " + block.idStr + ": " + data);
						}
					}

					// If we're highlighting explored regions and there's an adjacent
					// torch, flip over to the "highlighted" textures
					if (XRay.toggle.highlight_explored)
					{
						adj_torch = hasAdjacentTorch(this.lx,this.ly,this.lz);
						if (adj_torch)
						{
							textureId += 256;
							tex_offset = 256;
						}
						else
						{
							tex_offset = 0;
						}
					}
					else
					{
						tex_offset = 0;
					}

					// Now process the actual drawing
					switch(block.type)
					{
						case TORCH:
							renderTorch(textureId,this.lx,this.ly,this.lz);
							break;
						case DECORATION_CROSS:
							renderCrossDecoration(textureId,this.lx,this.ly,this.lz);
							break;
						case DECORATION_GRID:
							renderGridDecoration(textureId,this.lx,this.ly,this.lz);
							break;
						case LADDER:
							renderLadder(textureId,this.lx,this.ly,this.lz);
							break;
						case FLOOR:
							renderFloor(textureId,this.lx,this.ly,this.lz);
							break;
						case MINECART_TRACKS:
							renderMinecartTracks(textureId,this.lx,this.ly,this.lz,block,tex_offset);
							break;
						case SIMPLE_RAIL:
							renderSimpleRail(textureId,this.lx,this.ly,this.lz,block,tex_offset);
							break;
						case PRESSURE_PLATE:
							renderPlate(textureId,this.lx,this.ly,this.lz);
							break;
						case DOOR:
							renderDoor(textureId,this.lx,this.ly,this.lz,block,tex_offset);
							break;
						case STAIRS:
							renderStairs(textureId,this.lx,this.ly,this.lz);
							break;
						case SIGNPOST:
							renderSignpost(textureId,this.lx,this.ly,this.lz);
							break;
						case WALLSIGN:
							renderWallSign(textureId,this.lx,this.ly,this.lz);
							break;
						case FENCE:
							renderFence(textureId,this.lx,this.ly,this.lz,this.lOffset,t);
							break;
						case FENCE_GATE:
							renderFenceGate(textureId,this.lx,this.ly,this.lz,this.lOffset);
							break;
						case LEVER:
							renderLever(textureId,this.lx,this.ly,this.lz,block,tex_offset);
							break;
						case BUTTON:
							renderButton(textureId,this.lx,this.ly,this.lz);
							break;
						case PORTAL:
							renderPortal(textureId,this.lx,this.ly,this.lz,this.lOffset,t);
							break;
						case SNOW:
							renderSnow(textureId,this.lx,this.ly,this.lz,this.lOffset,t);
							break;
						case BED:
							renderBed(textureId,this.lx,this.ly,this.lz,block,tex_offset);
							break;
						case TRAPDOOR:
							renderTrapdoor(textureId,this.lx,this.ly,this.lz);
							break;
						case PISTON_BODY:
							renderPistonBody(textureId,this.lx,this.ly,this.lz,block,tex_offset);
							break;
						case PISTON_HEAD:
							renderPistonHead(textureId,this.lx,this.ly,this.lz,block,tex_offset,false,false);
							break;
						case CAKE:
							renderCake(textureId,this.lx,this.ly,this.lz,block,tex_offset);
							break;
						case VINE:
							renderVine(textureId,this.lx,this.ly,this.lz,this.lOffset);
							break;
						case SOLID_PANE:
							renderSolidPane(textureId,this.lx,this.ly,this.lz,this.lOffset,t);
							break;
						case CHEST:
							renderChest(textureId,this.lx,this.ly,this.lz,this.lOffset,block,tex_offset);
							break;
						case STEM:
							renderStem(textureId,this.lx,this.ly,this.lz,this.lOffset,block,tex_offset);
							break;
						case HALFHEIGHT:
							renderHalfHeight(textureId,this.lx,this.ly,this.lz,this.lOffset);
							break;
						case CAULDRON:
							renderCauldron(textureId,this.lx,this.ly,this.lz,this.lOffset,block,tex_offset);
							break;
						case ENCHANTMENT_TABLE:
							renderEnchantmentTable(textureId,this.lx,this.ly,this.lz,this.lOffset,block,tex_offset);
							break;
						case BREWING_STAND:
							renderBrewingStand(textureId,this.lx,this.ly,this.lz,block,tex_offset);
							break;
						case SEMISOLID:
						case WATER:
						case GLASS:
							renderSemisolid(textureId,this.lx,this.ly,this.lz,this.lOffset,t);
							break;
						case END_PORTAL:
							renderEndPortal(textureId,this.lx,this.ly,this.lz);
							break;
						case END_PORTAL_FRAME:
							renderEndPortalFrame(textureId,this.lx,this.ly,this.lz,this.lOffset,block,tex_offset);
							break;
						case DRAGON_EGG:
							renderDragonEgg(textureId,this.lx,this.ly,this.lz);
							break;

						case NORMAL:
						case HUGE_MUSHROOM:
						default:
							north_t = textureId;
							south_t = textureId;
							west_t = textureId;
							east_t = textureId;
							top_t = textureId;
							bottom_t = textureId;

							// Huge Mushrooms are special-case since keeping the data in YAML seemed
							// like far too much work at the time. Keeping them there does technically
							// make more sense, so we should do that eventually.
							// TODO: That ^
							if (block.type == BLOCK_TYPE.HUGE_MUSHROOM)
							{
								int TEX_HUGE_MUSHROOM_PORES = block.texture_extra_map.get("pores");
								int TEX_HUGE_MUSHROOM_STEM = block.texture_extra_map.get("stem");
								data = getData(this.lx, this.ly, this.lz);
								switch (data)
								{
									case 0:
										west_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										east_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										south_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										north_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										top_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
									case 1:
										east_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										south_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
									case 2:
										west_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										east_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										south_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
									case 3:
										west_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										south_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
									case 4:
										east_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										south_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										north_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
									case 5:
										west_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										east_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										south_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										north_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
									case 6:
										west_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										south_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										north_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
									case 7:
										east_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										north_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
									case 8:
										west_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										east_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										north_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
									case 9:
										west_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										north_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
									case 10:
										west_t = TEX_HUGE_MUSHROOM_STEM + tex_offset;
										east_t = TEX_HUGE_MUSHROOM_STEM + tex_offset;
										south_t = TEX_HUGE_MUSHROOM_STEM + tex_offset;
										north_t = TEX_HUGE_MUSHROOM_STEM + tex_offset;
										top_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
									default:
										west_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										east_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										south_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										north_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										top_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										bottom_t = TEX_HUGE_MUSHROOM_PORES + tex_offset;
										break;
								}
							}

							// Now assign the textures for each face, if we're supposed to
							if (block.texture_dir_map != null)
							{
								data = getData(this.lx, this.ly, this.lz);
								BlockType.DIRECTION_ABS dir;
								if (block.texture_dir_data_map != null && block.texture_dir_data_map.containsKey(data))
								{
									dir = block.texture_dir_data_map.get(data);
								}
								else
								{
									dir = BlockType.DIRECTION_ABS.NORTH;
								}

								switch (dir)
								{
									case NORTH:
										switch (loopPass)
										{
											case NORTHSOUTH:
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.FORWARD))
												{
													north_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.FORWARD) + tex_offset;
												}
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.BACKWARD))
												{
													south_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.BACKWARD) + tex_offset;
												}
												break;
											case EASTWEST:
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.SIDES))
												{
													west_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.SIDES) + tex_offset;
													east_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.SIDES) + tex_offset;
												}
												break;
										}
										break;
									case SOUTH:
										switch (loopPass)
										{
											case NORTHSOUTH:
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.BACKWARD))
												{
													north_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.BACKWARD) + tex_offset;
												}
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.FORWARD))
												{
													south_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.FORWARD) + tex_offset;
												}
												break;
											case EASTWEST:
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.SIDES))
												{
													west_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.SIDES) + tex_offset;
													east_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.SIDES) + tex_offset;
												}
												break;
										}
										break;
									case WEST:
										switch (loopPass)
										{
											case NORTHSOUTH:
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.SIDES))
												{
													north_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.SIDES) + tex_offset;
													south_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.SIDES) + tex_offset;
												}
												break;
											case EASTWEST:
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.FORWARD))
												{
													west_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.FORWARD) + tex_offset;
												}
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.BACKWARD))
												{
													east_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.BACKWARD) + tex_offset;
												}
												break;
										}
										break;
									case EAST:
										switch (loopPass)
										{
											case NORTHSOUTH:
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.SIDES))
												{
													north_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.SIDES) + tex_offset;
													south_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.SIDES) + tex_offset;
												}
												break;
											case EASTWEST:
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.BACKWARD))
												{
													west_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.BACKWARD) + tex_offset;
												}
												if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.FORWARD))
												{
													east_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.FORWARD) + tex_offset;
												}
												break;
										}
										break;
								}

								// Top/Bottom doesn't depend on orientation, at least for anything currently in Minecraft.
								// If Minecraft starts adding blocks that can be oriented Up or Down, we'll have to move
								// this back into the case statement above
								switch (loopPass)
								{
									case TOP:
										if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.TOP))
										{
											top_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.TOP) + tex_offset;
										}
										break;
									case BOTTOM:
										if (block.texture_dir_map.containsKey(BlockType.DIRECTION_REL.BOTTOM))
										{
											bottom_t = block.texture_dir_map.get(BlockType.DIRECTION_REL.BOTTOM) + tex_offset;
										}
										break;
								}
							}

							// Finally, we're to the point of actually rendering the solid
							switch (loopPass)
							{
								case EASTWEST:
									if ((pass == RENDER_PASS.SELECTED && highlightingOres) || loopPass == SOLID_PASS.EASTWEST)
									{
										if(!east) this.renderWestEast(east_t, worldX+this.lx+1, this.ly, worldZ+this.lz);
										if(!west) this.renderWestEast(west_t, worldX+this.lx, this.ly, worldZ+this.lz);
									}
									break;
								case BOTTOM:
									if ((pass == RENDER_PASS.SELECTED && highlightingOres) || loopPass == SOLID_PASS.BOTTOM)
									{
										if(!below) this.renderTopDown(bottom_t, worldX+this.lx, this.ly, worldZ+this.lz);
									}
									break;
								case TOP:
									if ((pass == RENDER_PASS.SELECTED && highlightingOres) || loopPass == SOLID_PASS.TOP)
									{
										if(!above) this.renderTopDown(top_t, worldX+this.lx, this.ly+1, worldZ+this.lz);	
									}
									break;
								case NORTHSOUTH:
									if ((pass == RENDER_PASS.SELECTED && highlightingOres) || loopPass == SOLID_PASS.NORTHSOUTH)
									{
										if(!north) this.renderNorthSouth(north_t, worldX+this.lx, this.ly, worldZ+this.lz);
										if(!south) this.renderNorthSouth(south_t, worldX+this.lx, this.ly, worldZ+this.lz+1);
									}
									break;
							}
							break;
					}
				}
			}
		}
	}

	/**
	 * Returns whether or not this chunk contains paintings (to avoid unnecessary texture-swapping)
	 */
	public boolean hasPaintings()
	{
		return !this.paintings.isEmpty();
	}
	
	/**
	 * Renders paintings into our world.  Paintings are stored as Entities, not
	 * block-level data, so they have to be handled differently than everything else.
	 */
	public void renderPaintings()
	{
		PaintingInfo info;
		float start_x;
		float start_y;
		float start_z;
		float back_x;
		float back_z;
		float dX;
		float dZ;
		for (PaintingEntity painting : this.paintings)
		{
			info = MinecraftConstants.paintings.get(painting.name.toLowerCase());
			if (info == null)
			{
				XRay.logger.warn("Unknown painting name: " + painting.name);
				continue;
			}
			
			start_y = painting.tile_y + 0.5f + info.centery;
			switch (painting.dir)
			{
				case 0x0:
					// East
					start_x = painting.tile_x + 0.5f + info.centerx;
					start_z = painting.tile_z - 0.5f - TEX16;
					back_x = start_x;
					back_z = start_z + TEX32;
					dX = -1;
					dZ = 0;
					break;
				case 0x1:
					// North
					start_x = painting.tile_x - 0.5f - TEX16;
					start_z = painting.tile_z - 0.5f - info.centerx;
					back_x = start_x + TEX32;
					back_z = start_z;
					dX = 0;
					dZ = 1;
					break;
				case 0x2:
					// West
					start_x = painting.tile_x - 0.5f - info.centerx;
					start_z = painting.tile_z + 0.5f + TEX16;
					back_x = start_x;
					back_z = start_z - TEX32;
					dX = 1;
					dZ = 0;
					break;
				case 0x3:
				default:
					// South
					start_x = painting.tile_x + 0.5f + TEX16;
					start_z = painting.tile_z + 0.5f + info.centerx;
					back_x = start_x - TEX32;
					back_z = start_z;
					dX = 0;
					dZ = -1;
					break;
			}
			
			// Draw the painting face
			renderNonstandardVertical(info.offsetx, info.offsety, info.sizex_tex, info.sizey_tex,
					start_x, start_y, start_z,
					start_x + (dX*info.sizex), start_y-info.sizey, start_z + (dZ*info.sizex));
			
			PaintingInfo backinfo = MinecraftConstants.paintingback;

			// Back
			renderNonstandardVertical(backinfo.offsetx, backinfo.offsety, info.sizex_tex, info.sizey_tex,
					back_x, start_y, back_z,
					back_x + (dX*info.sizex), start_y-info.sizey, back_z + (dZ*info.sizex));
 
			// Sides
			renderNonstandardVertical(backinfo.offsetx, backinfo.offsety, info.sizex_tex, info.sizey_tex,
					start_x, start_y, start_z,
					back_x, start_y-info.sizey, back_z);
			renderNonstandardVertical(backinfo.offsetx, backinfo.offsety, info.sizex_tex, info.sizey_tex,
					start_x + (dX*info.sizex), start_y, start_z + (dZ*info.sizex),
					back_x + (dX*info.sizex), start_y-info.sizey, back_z + (dZ*info.sizex));
			
			// Top/Bottom
			renderNonstandardHorizontal(backinfo.offsetx, backinfo.offsety, info.sizex_tex, info.sizey_tex,
					start_x, start_z,
					back_x + (dX*info.sizex), back_z + (dZ*info.sizex),
					start_y-info.sizey
					);
			renderNonstandardHorizontal(backinfo.offsetx, backinfo.offsety, info.sizex_tex, info.sizey_tex,
					start_x, start_z,
					back_x + (dX*info.sizex), back_z + (dZ*info.sizex),
					start_y
					);
		}
	}
	
	public void renderSolid(int sheet) {
		if (!this.usedTextureSheets.containsKey(sheet))
		{
			return;
		}
		if(isDirty.get(sheet)) {
				GL11.glNewList(this.displayListNums.get(sheet), GL11.GL_COMPILE);
				renderWorldSolids(sheet);
				GL11.glEndList();
				GL11.glNewList(this.nonstandardListNums.get(sheet), GL11.GL_COMPILE);
				//GL11.glDepthMask(false);
				renderWorldNonstandard(sheet);
				//GL11.glDepthMask(true);
				GL11.glEndList();
				GL11.glNewList(this.glassListNums.get(sheet), GL11.GL_COMPILE);
				renderWorldGlass(sheet);
				GL11.glEndList();
				this.isDirty.put(sheet, false);
		}
		GL11.glCallList(this.displayListNums.get(sheet));
	}
	
	public void renderNonstandard(int sheet) {
		if (!this.usedTextureSheets.containsKey(sheet))
		{
			return;
		}
		GL11.glCallList(this.nonstandardListNums.get(sheet));
	}

	public void renderGlass(int sheet) {
		if (!this.usedTextureSheets.containsKey(sheet))
		{
			return;
		}
		GL11.glCallList(this.glassListNums.get(sheet));
	}
	
	public void renderSelected(int sheet, boolean[] selectedMap) {
		if (!this.usedTextureSheets.containsKey(sheet))
		{
			return;
		}
		if(isSelectedDirty.get(sheet)) {
			GL11.glNewList(this.selectedDisplayListNums.get(sheet), GL11.GL_COMPILE);
			renderWorldSelected(sheet, selectedMap);
			GL11.glEndList();
			this.isSelectedDirty.put(sheet, false);
		}
		GL11.glCallList(this.selectedDisplayListNums.get(sheet));
	}

	/**
	 * Renders a border around this chunk.  Note that we should have our chunkBorderTexture
	 * bound before we get in here.
	 */
	public void renderBorder()
	{
		float x = this.x*16-.49f;
		float z = this.z*16-.49f;
		float top = 128.5f;
		float bottom = .5f;
		float width = 15.98f;
		this.renderNonstandardVertical(0, 0, 1, 1, x, top, z, x+width, bottom, z);
		this.renderNonstandardVertical(0, 0, 1, 1, x, top, z+width, x+width, bottom, z+width);
		this.renderNonstandardVertical(0, 0, 1, 1, x, top, z, x, bottom, z+width);
		this.renderNonstandardVertical(0, 0, 1, 1, x+width, top, z, x+width, bottom, z+width);
	}

	/**
	 * Renders a box around where a slime would spawn.  We don't check our own willSpawnSlimes
	 * var here; that's controlled by the main XRay render loop.
	 */
	public void renderSlimeBox()
	{
		float x = this.x*16-.48f;
		float z = this.z*16-.48f;
		float top = 40.51f;
		float bottom = .49f;
		float width = 15.94f;
		this.renderNonstandardVertical(0, 0, 1, 1, x, top, z, x+width, bottom, z);
		this.renderNonstandardVertical(0, 0, 1, 1, x, top, z+width, x+width, bottom, z+width);
		this.renderNonstandardVertical(0, 0, 1, 1, x, top, z, x, bottom, z+width);
		this.renderNonstandardVertical(0, 0, 1, 1, x+width, top, z, x+width, bottom, z+width);
		this.renderNonstandardHorizontal(0, 0, 1, 1, x, z, x+width, z+width, top);
		this.renderNonstandardHorizontal(0, 0, 1, 1, x, z, x+width, z+width, bottom);
	}

	/**
	 * "Rewinds" our looping over blocks.
	 */
	protected void rewindLoop()
	{
		this.lx = 0;
		this.ly = 0;
		this.lz = 0;
		this.lOffset = -1;
	}

	/**
	 * Advances our block loop
	 */
	protected abstract short nextBlock();
}
