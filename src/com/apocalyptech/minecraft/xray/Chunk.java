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

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.apocalyptech.minecraft.xray.dtf.ByteArrayTag;
import com.apocalyptech.minecraft.xray.dtf.CompoundTag;
import com.apocalyptech.minecraft.xray.dtf.StringTag;
import com.apocalyptech.minecraft.xray.dtf.ListTag;
import com.apocalyptech.minecraft.xray.dtf.IntTag;
import com.apocalyptech.minecraft.xray.dtf.Tag;

import static com.apocalyptech.minecraft.xray.MineCraftConstants.*;

public class Chunk {
	private int displayListNum;
	private int transparentListNum;
	private int selectedDisplayListNum;
	public int x;
	public int z;
	public boolean isDirty;
	public boolean isSelectedDirty;
	public boolean isOnMinimap;
	private CompoundTag chunkData;
	private ByteArrayTag blockData;
	private ByteArrayTag mapData;
	private ArrayList<PaintingEntity> paintings;
	
	private MinecraftLevel level;
	
	public Chunk(MinecraftLevel level, Tag data) {
		
		this.level = level;
		this.chunkData = (CompoundTag) data;
		this.isOnMinimap = false;

		CompoundTag levelTag = (CompoundTag) chunkData.value.get(0); // first tag
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
		
		blockData = (ByteArrayTag) levelTag.getTagWithName("Blocks");
		mapData = (ByteArrayTag) levelTag.getTagWithName("Data");
		
		this.isDirty = true;
		this.isSelectedDirty = true;

		displayListNum = GL11.glGenLists(1);
		selectedDisplayListNum = GL11.glGenLists(1);
		transparentListNum = GL11.glGenLists(1);
		
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
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]+TEX32);
			GL11.glVertex3f(x-xzScale, y-xzScale, z-xzScale);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX32);
			GL11.glVertex3f(x+xzScale, y-xzScale, z-xzScale);
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
	
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x1, y, z1);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
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
	 * Renders a nonstandard vertical rectangle that's been rotated.
	 * 
	 * @param tx X index within the texture
	 * @param ty Y index within the texture
	 * @param tdx Width of texture
	 * @param tdy Height of texture
	 * @param x_width The width of the rectangle in the X axis
	 * @param z_width The width of the rectangle in the Z axis
	 * @param radius Distance from the center point to draw each point
	 * @param x1 Center X Coord
	 * @param y1 Center Y Coord
	 * @param z1 Center Z Coord
	 * @param rotate_x Degrees to rotate on the X axis
	 */
	public void renderNonstandardVerticalRotatedX(float tx, float ty, float tdx, float tdy,
			float x_width_h, float radius,
			float x1, float y1, float z1,
			double rotate_x)
	{
		/*
		double radians = Math.toRadians(rotate_x);
		float cosine = (float)Math.cos(radians);
		float sine = (float)Math.sin(radians);

		float x1a = x - x_width_h + radius * cosine;
		float y1a = y - z_width_h + radius * sine;
		float x1b = x + x_width_h + radius * cosine;
		float y1b = y + z_width_h + radius * sine;

		radians = Math.toRadians(rotate_x + 180);
		cosine = (float)Math.cos(radians);
		sine = (float)Math.sin(radians);

		float x2a = x - x_width_h + radius * cosine;
		float z2a = z - z_width_h + radius * sine;
		float x2b = x + x_width_h + radius * cosine;
		float z2b = z + z_width_h + radius * sine;

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
		*/
	}
	
	/**
	 * Renders an arbitrary horizontal rectangle (will be orthogonal)
	 * @param t
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @param y
	 */
	public void renderHorizontal(int t, float x1, float z1, float x2, float z2, float y) {

		float bx = precalcSpriteSheetToTextureX[t];
		float by = precalcSpriteSheetToTextureY[t];

		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(bx, by);
			GL11.glVertex3f(x1, y, z1);
	
			GL11.glTexCoord2f(bx+TEX16, by);
			GL11.glVertex3f(x1, y, z2);
	
			GL11.glTexCoord2f(bx, by+TEX32);
			GL11.glVertex3f(x2, y, z1);
	
			GL11.glTexCoord2f(bx+TEX16, by+TEX32);
			GL11.glVertex3f(x2, y, z2);
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
	
	public void renderFenceBody(int t, float x1, float x2, float y, float z) {
		
		// Outside edges
		this.renderVertical(t, x1, z+.35f, x2, z+.35f, y-0.5f, .7f);
		this.renderVertical(t, x1, z-.35f, x2, z-.35f, y-0.5f, .7f);
		
		// Inside edges
		this.renderVertical(t, x1, z+.25f, x2, z+.25f, y-0.5f, .7f);
		this.renderVertical(t, x1, z-.25f, x2, z-.25f, y-0.5f, .7f);
		
		// Top edge
		this.renderHorizontal(t, x1, z+.35f, x2, z-.35f, y+.2f);
		
		// Inner edges
		this.renderHorizontal(t, x1, z+.25f, x2, z-.25f, y);
		this.renderHorizontal(t, x1, z+.25f, x2, z-.25f, y-.1f);
		this.renderHorizontal(t, x1, z+.25f, x2, z-.25f, y-.3f);
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
		if((i == 0) || (block_type != BLOCK_TYPE.NORMAL)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Renders a "special" block; AKA something that's not just an ordinary cube.
	 * Basically it draws four "faces" of the object, which creates a plus sign of
	 * sorts.  This should probably be handled in some other way, actually.
	 * 
	 * @param bx Texture Beginning-X coordinate (inside the texture PNG)
	 * @param by Texture Beginning-Y coordinate
	 * @param ex Texture Ending-X coordinate
	 * @param ey Texture Ending-Y coordinate
	 * @param x Absolute X position of block
	 * @param y Absolute Y position of block
	 * @param z Absolute Z position of block
	 */
	public void renderSpecial(float bx, float by, float ex, float ey, float x, float y, float z)
	{
		 
		// GL11.glDisable(GL11.GL_CULL_FACE);
		 //GL11.glDisable(GL11.GL_DEPTH_TEST);
		 GL11.glBegin(GL11.GL_QUADS);
		 GL11.glNormal3f(1.0f, 0.0f, 0.0f);
		 GL11.glTexCoord2f(bx, by); 	GL11.glVertex3f(x+9/16.0f+TEX64, y+1.0f, 	z);
		 GL11.glTexCoord2f(ex, by); 	GL11.glVertex3f(x+9/16.0f+TEX64, y+1.0f, 	z+1.0f);
		 GL11.glTexCoord2f(ex, ey); 	GL11.glVertex3f(x+9/16.0f-TEX64, y, 		z+1.0f);
		 GL11.glTexCoord2f(bx, ey); 	GL11.glVertex3f(x+9/16.0f-TEX64, y,	 		z);
		
		 GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
		 GL11.glTexCoord2f(bx, by); 	GL11.glVertex3f(x+7/16.0f+TEX64, y+1.0f,	z+1.0f);
		 GL11.glTexCoord2f(ex, by); 	GL11.glVertex3f(x+7/16.0f+TEX64, y+1.0f,	z);
		 GL11.glTexCoord2f(ex, ey); 	GL11.glVertex3f(x+7/16.0f-TEX64, y,			z);
		 GL11.glTexCoord2f(bx, ey); 	GL11.glVertex3f(x+7/16.0f-TEX64, y,			z+1.0f);
		 
		 GL11.glNormal3f(0.0f, 0.0f, 1.0f);
		 GL11.glTexCoord2f(bx, by); 	GL11.glVertex3f(x+1.0f,	y+1.0f,	z+9/16.0f+TEX64);
		 GL11.glTexCoord2f(ex, by); 	GL11.glVertex3f(x, 		y+1.0f,	z+9/16.0f+TEX64);
		 GL11.glTexCoord2f(ex, ey); 	GL11.glVertex3f(x, 		y, 		z+9/16.0f-TEX64);
		 GL11.glTexCoord2f(bx, ey);	 	GL11.glVertex3f(x+1.0f,	y, 		z+9/16.0f-TEX64);
		 
		 GL11.glNormal3f(0.0f, 0.0f, -1.0f);
		 GL11.glTexCoord2f(bx, by); 	GL11.glVertex3f(x, 		y+1.0f,	z+7/16.0f+TEX64);
		 GL11.glTexCoord2f(ex, by); 	GL11.glVertex3f(x+1.0f,	y+1.0f,	z+7/16.0f+TEX64);
		 GL11.glTexCoord2f(ex, ey); 	GL11.glVertex3f(x+1.0f,	y, 		z+7/16.0f-TEX64);
		 GL11.glTexCoord2f(bx, ey); 	GL11.glVertex3f(x, 		y, 		z+7/16.0f-TEX64);
		 
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
	public void renderLever(int textureId, int xxx, int yyy, int zzz)
	{
		byte data = getData(xxx, yyy, zzz);
		boolean thrown = false;
		if ((data & 0x8) == 0x8)
		{
			thrown = true;
		}
		data &= 7;
		//System.out.println("Data: " + data);
		 
		// First draw the cobblestoney box
		int cobble_tex = blockDataToSpriteSheet[BLOCK.COBBLESTONE.id];
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
		 ey = by + TEX32;
		 
		 renderSpecial(bx, by, ex, ey, x, y, z);
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
	 * Minecart tracks
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderMinecartTracks(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		 
		byte data = getData(xxx, yyy, zzz);
		if (data > 0x5)
		{
			textureId -= 16;
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
	public void renderSimpleRail(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		 
		byte data = getData(xxx, yyy, zzz);
		byte powered = data;
		powered >>= 3;
		if (powered > 0)
		{
			// This is just for powered rails, to light them up properly
			textureId += 16;
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
	 * Renders a thin slice of something on the ground (used for snow currently).  Practically
	 * the same as renderPlate actually, just wider and a bit taller
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderThinslice(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		float radius = 0.48f;
		
		// The top face
		this.renderHorizontal(textureId, x+radius, z+radius, x-radius, z-radius, y-0.38f);
		
		// Sides
		this.renderVertical(textureId, x+radius, z+radius, x+radius, z-radius, y-0.48f, 0.1f);
		this.renderVertical(textureId, x-radius, z+radius, x-radius, z-radius, y-0.48f, 0.1f);
		this.renderVertical(textureId, x+radius, z+radius, x-radius, z+radius, y-0.48f, 0.1f);
		this.renderVertical(textureId, x+radius, z-radius, x-radius, z-radius, y-0.48f, 0.1f);
	}

	/**
	 * Renders a bed block.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 */
	public void renderBed(int textureId, int xxx, int yyy, int zzz) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		float side_part = 0.48f;
		float side_full = 0.5f;
		float bed_height = 0.7f;
		float horiz_off = bed_height-0.5f;
		boolean head = true;

		byte data = getData(xxx, yyy, zzz);
		data &= 0xF;
		if ((data & 0x8) == 0)
		{
			textureId -= 1;
			head = false;
		}
		data &= 0x3;
		
		// There's a fair amount of duplicated code in here, but whatever.  It
		// feels like assigning variables to do all this would be a lot MORE work.
		// TODO: it'd be nice to properly align the pillow texture so it looks right.
		if (head)
		{
			switch (data)
			{
				case 0x0:
					// Head is facing west
					this.renderHorizontal(textureId, x+side_part, z+side_part, x-side_part, z-side_full, y+horiz_off);
					this.renderVertical(textureId, x+side_part, z+side_part, x+side_part, z-side_full, y-side_part, bed_height);
					this.renderVertical(textureId, x-side_part, z+side_part, x-side_part, z-side_full, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_part, z+side_part, x-side_part, z+side_part, y-side_part, bed_height);
					break;
				case 0x1:
					// Head is facing north
					this.renderHorizontal(textureId, x+side_full, z+side_part, x-side_part, z-side_part, y+horiz_off);
					this.renderVertical(textureId, x-side_part, z+side_part, x-side_part, z-side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_full, z+side_part, x-side_part, z+side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_full, z-side_part, x-side_part, z-side_part, y-side_part, bed_height);
					break;
				case 0x2:
					// Head is facing east
					this.renderHorizontal(textureId, x+side_part, z+side_full, x-side_part, z-side_part, y+horiz_off);
					this.renderVertical(textureId, x+side_part, z+side_full, x+side_part, z-side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x-side_part, z+side_full, x-side_part, z-side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_part, z-side_part, x-side_part, z-side_part, y-side_part, bed_height);
					break;
				case 0x3:
					// Head is facing south
					this.renderHorizontal(textureId, x+side_part, z+side_part, x-side_full, z-side_part, y+horiz_off);
					this.renderVertical(textureId, x+side_part, z+side_part, x+side_part, z-side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_part, z+side_part, x-side_full, z+side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_part, z-side_part, x-side_full, z-side_part, y-side_part, bed_height);
					break;
			}
		}
		else
		{
			switch (data)
			{
				case 0x0:
					// Head is facing west
					this.renderHorizontal(textureId, x+side_part, z+side_full, x-side_part, z-side_part, y+horiz_off);
					this.renderVertical(textureId, x+side_part, z+side_full, x+side_part, z-side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x-side_part, z+side_full, x-side_part, z-side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_part, z-side_part, x-side_part, z-side_part, y-side_part, bed_height);
					break;
				case 0x1:
					// Head is facing north
					this.renderHorizontal(textureId, x+side_part, z+side_part, x-side_full, z-side_part, y+horiz_off);
					this.renderVertical(textureId, x+side_part, z+side_part, x+side_part, z-side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_part, z+side_part, x-side_full, z+side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_part, z-side_part, x-side_full, z-side_part, y-side_part, bed_height);
					break;
				case 0x2:
					// Head is facing east
					this.renderHorizontal(textureId, x+side_part, z+side_part, x-side_part, z-side_full, y+horiz_off);
					this.renderVertical(textureId, x+side_part, z+side_part, x+side_part, z-side_full, y-side_part, bed_height);
					this.renderVertical(textureId, x-side_part, z+side_part, x-side_part, z-side_full, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_part, z+side_part, x-side_part, z+side_part, y-side_part, bed_height);
					break;
				case 0x3:
					// Head is facing south
					this.renderHorizontal(textureId, x+side_full, z+side_part, x-side_part, z-side_part, y+horiz_off);
					this.renderVertical(textureId, x-side_part, z+side_part, x-side_part, z-side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_full, z+side_part, x-side_part, z+side_part, y-side_part, bed_height);
					this.renderVertical(textureId, x+side_full, z-side_part, x-side_part, z-side_part, y-side_part, bed_height);
					break;
			}
		}
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
				// West
				GL11.glRotatef(-90f, 1f, 0f, 0f);
			}
			else if (dir == 1)
			{
				// East
				GL11.glRotatef(90f, 1f, 0f, 0f);
			}
			else if (dir == 2)
			{
				// South
				GL11.glRotatef(90f, 0f, 0f, 1f);
			}
			else
			{
				// North
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
		// data: 0 is West, increasing numbers add 22.5 degrees (so 4 is North, 8 south, etc)
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
		 		// East
	 			faceX1 = x-sign_length;
	 			faceX2 = x+sign_length;
	 			faceZ1 = z+0.45f;
	 			faceZ2 = z+0.45f;
	 			back_dX = 0f;
	 			back_dZ = 0.05f;
		 		break;
		 	case 3:
		 		// West
	 			faceX1 = x-sign_length;
	 			faceX2 = x+sign_length;
	 			faceZ1 = z-0.45f;
	 			faceZ2 = z-0.45f;
	 			back_dX = 0f;
	 			back_dZ = -0.05f;
		 		break;
		 	case 4:
		 		// North
	 			faceX1 = x+0.45f;
	 			faceX2 = x+0.45f;
	 			faceZ1 = z-sign_length;
	 			faceZ2 = z+sign_length;
	 			back_dX = 0.05f;
	 			back_dZ = 0f;
		 		break;
		 	case 5:
	 		default:
	 			// South
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
	 * Renders a fence.  Ideally we should try and figure out at least if
	 * we can do it in one orientation versus another, but for now this will have to
	 * do.
	 * 
	 * @param textureId
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param blockOffset Should be passed in from our main draw loop so we don't have to recalculate
	 */
	public void renderFence(int textureId, int xxx, int yyy, int zzz, int blockOffset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		float postsize = .1f;
		float postsize_h = postsize/2f;
		float slat_height = .2f;
		float top_slat_offset = .3f;
		float slat_start = y-.1f;
		
		// First the fencepost
		this.renderVertical(textureId, x+postsize, z+postsize, x+postsize, z-postsize, y-0.5f, 1f);
		this.renderVertical(textureId, x+postsize, z-postsize, x-postsize, z-postsize, y-0.5f, 1f);
		this.renderVertical(textureId, x-postsize, z-postsize, x-postsize, z+postsize, y-0.5f, 1f);
		this.renderVertical(textureId, x-postsize, z+postsize, x+postsize, z+postsize, y-0.5f, 1f);
		this.renderHorizontal(textureId, x+postsize, z+postsize, x-postsize, z-postsize, y+0.5f);

		// Check for adjacent fences in the -x direction
		boolean have_adj = false;
		if (xxx>0)
		{
			if (blockData.value[blockOffset-BLOCKSPERCOLUMN] == MineCraftConstants.BLOCK.FENCE.id)
			{
				have_adj = true;
			}
		}
		else
		{
			Chunk otherChunk = level.getChunk(this.x-1, this.z);
			if (otherChunk != null && otherChunk.getBlock(15, yyy, zzz) == MineCraftConstants.BLOCK.FENCE.id)
			{
				have_adj  = true;
			}
		}
		if (have_adj)
		{
			// Bottom slat
			this.renderVertical(textureId, x-postsize, z+postsize_h, x-1f+postsize, z+postsize_h, slat_start, slat_height);
			this.renderVertical(textureId, x-postsize, z-postsize_h, x-1f+postsize, z-postsize_h, slat_start, slat_height);
			this.renderHorizontal(textureId, x-postsize, z+postsize_h, x-1f+postsize, z-postsize_h, slat_start);
			this.renderHorizontal(textureId, x-postsize, z+postsize_h, x-1f+postsize, z-postsize_h, slat_start+slat_height);

			// Top slat
			this.renderVertical(textureId, x-postsize, z+postsize_h, x-1f+postsize, z+postsize_h, slat_start+top_slat_offset, slat_height);
			this.renderVertical(textureId, x-postsize, z-postsize_h, x-1f+postsize, z-postsize_h, slat_start+top_slat_offset, slat_height);
			this.renderHorizontal(textureId, x-postsize, z+postsize_h, x-1f+postsize, z-postsize_h, slat_start+top_slat_offset);
			this.renderHorizontal(textureId, x-postsize, z+postsize_h, x-1f+postsize, z-postsize_h, slat_start+top_slat_offset+slat_height);
		}
		
		// Check for adjacent fences in the -z direction
		have_adj = false;
		if(zzz>0)
		{
			if (blockData.value[blockOffset-BLOCKSPERROW] == MineCraftConstants.BLOCK.FENCE.id)
			{
				have_adj = true;
			}
		}
		else
		{
			Chunk otherChunk = level.getChunk(this.x,this.z-1);
			if(otherChunk != null && otherChunk.getBlock(xxx, yyy, 15) == MineCraftConstants.BLOCK.FENCE.id) {
				have_adj = true;
			}
		}
		if (have_adj)
		{
			// Bottom slat
			this.renderVertical(textureId, x+postsize_h, z-postsize, x+postsize_h, z-1f+postsize, slat_start, slat_height);
			this.renderVertical(textureId, x-postsize_h, z-postsize, x-postsize_h, z-1f+postsize, slat_start, slat_height);
			this.renderHorizontal(textureId, x+postsize_h, z-postsize, x-postsize_h, z-1f+postsize, slat_start);
			this.renderHorizontal(textureId, x+postsize_h, z-postsize, x-postsize_h, z-1f+postsize, slat_start+slat_height);

			// Top slat
			this.renderVertical(textureId, x+postsize_h, z-postsize, x+postsize_h, z-1f+postsize, slat_start+top_slat_offset, slat_height);
			this.renderVertical(textureId, x-postsize_h, z-postsize, x-postsize_h, z-1f+postsize, slat_start+top_slat_offset, slat_height);
			this.renderHorizontal(textureId, x+postsize_h, z-postsize, x-postsize_h, z-1f+postsize, slat_start+top_slat_offset);
			this.renderHorizontal(textureId, x+postsize_h, z-postsize, x-postsize_h, z-1f+postsize, slat_start+top_slat_offset+slat_height);
		}
	}

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
	 			// South
		 		faceX1 = x-0.5f+button_radius;
		 		faceX2 = x-0.5f+button_radius;
		 		faceZ1 = z-button_radius;
		 		faceZ2 = z+button_radius;
		 		back_dX = -button_radius;
		 		back_dZ = 0;
	 			break;
		 	case 2:
		 		// North
		 		faceX1 = x+0.5f-button_radius;
		 		faceX2 = x+0.5f-button_radius;
		 		faceZ1 = z-button_radius;
		 		faceZ2 = z+button_radius;
		 		back_dX = button_radius;
		 		back_dZ = 0;
		 		break;
		 	case 3:
		 		// West
		 		faceX1 = x-button_radius;
		 		faceX2 = x+button_radius;
		 		faceZ1 = z-0.5f+button_radius;
		 		faceZ2 = z-0.5f+button_radius;
		 		back_dX = 0;
		 		back_dZ = -button_radius;
		 		break;
		 	case 4:
	 		default:
		 		// East
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
	 */
	public void renderPortal(int textureId, int xxx, int yyy, int zzz, int blockOffset) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		
		// Check to see where adjoining Portal spaces are, so we know which
		// faces to draw
		boolean drawWestEast = true;
		
		// Doing this in a for loop just so we can break out more
		// easily once we find something
		for (int i=0; i<1; i++)
		{
			if (xxx>0)
			{
				if (blockData.value[blockOffset-BLOCKSPERCOLUMN] == MineCraftConstants.BLOCK.PORTAL.id)
				{
					break;
				}
			}
			else
			{
				Chunk otherChunk = level.getChunk(this.x-1, this.z);
				if (otherChunk != null && otherChunk.getBlock(15, yyy, zzz) == MineCraftConstants.BLOCK.PORTAL.id)
				{
					break;
				}
			}
			
			if (xxx<15)
			{
				if (blockData.value[blockOffset+BLOCKSPERCOLUMN] == MineCraftConstants.BLOCK.PORTAL.id)
				{
					break;
				}
			}
			else
			{
				Chunk otherChunk = level.getChunk(this.x+1, this.z);
				if (otherChunk != null && otherChunk.getBlock(0, yyy, zzz) == MineCraftConstants.BLOCK.PORTAL.id)
				{
					break;
				}
			}
			
			// If we've gotten here without finding a Portal space, we'll just assume that we're going in
			// the other direction.
			drawWestEast = false;
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
	
	public boolean checkSolid(byte block, boolean transpararency) {
		if(block == 0) {
			return true;
		}
		return isSolid(block) == transpararency;
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
	 * @param blockType The actual block type; needed for the piston head
	 */
	public void renderPistonBody(int textureId, int xxx, int yyy, int zzz, byte blockType) {
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

		// This routine draws the piston facing west, which is direction value 3
		if (direction == 1)
		{
			// Up
			GL11.glRotatef(-90f, 1f, 0f, 0f);
		}
		else if (direction == 2)
		{
			// East
			GL11.glRotatef(180f, 0f, 1f, 0f);
		}
		else if (direction == 4)
		{
			// North
			GL11.glRotatef(-90f, 0f, 1f, 0f);
		}
		else if (direction == 5)
		{
			// South
			GL11.glRotatef(90f, 0f, 1f, 0f);
		}

		// First the main body bit
		renderNonstandardHorizontal(tex_x, tex_y, TEX16, TEX_PISTON, -.49f, .25f, .49f, -.49f, .49f);
		renderNonstandardHorizontal(tex_x, tex_y, TEX16, TEX_PISTON, -.49f, .25f, .49f, -.49f, -.49f);
		renderNonstandardVertical(tex_x, tex_y, TEX16, TEX_PISTON, -.49f, .49f, .25f, -.49f, -.49f, -.49f);
		renderNonstandardVertical(tex_x, tex_y, TEX16, TEX_PISTON, .49f, .49f, .25f, .49f, -.49f, -.49f);
		renderVertical(textureId+1, -.49f, -.49f, .49f, -.49f, -.49f, .98f);

		// If we're extended, draw our faceplate; if not, draw the retracted face
		if (extended)
		{
			renderVertical(textureId+2, -.49f, .25f, .49f, .25f, -.49f, .98f);

			// Pop the matrix after
			GL11.glPopMatrix();
		}
		else
		{
			// Pop the matrix before
			GL11.glPopMatrix();

			renderPistonHead(textureId-1, xxx, yyy, zzz, true, (blockType == 29));
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
	public void renderPistonHead(int textureId, int xxx, int yyy, int zzz, boolean attached, boolean override_sticky) {
		float x = xxx + this.x*16;
		float z = zzz + this.z*16;
		float y = yyy;
		byte data = getData(xxx, yyy, zzz);
		boolean sticky = ((data & 0x8) == 0x8);
		int direction = (data & 0x7);

		float side_tex_x = precalcSpriteSheetToTextureX[textureId+1];
		float side_tex_y = precalcSpriteSheetToTextureY[textureId+1];

		// Matrix stuff
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);

		// This routine draws the piston facing west, which is direction value 3
		if (direction == 1)
		{
			// Up
			GL11.glRotatef(-90f, 1f, 0f, 0f);
		}
		else if (direction == 2)
		{
			// East
			GL11.glRotatef(180f, 0f, 1f, 0f);
		}
		else if (direction == 4)
		{
			// North
			GL11.glRotatef(-90f, 0f, 1f, 0f);
		}
		else if (direction == 5)
		{
			// South
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
				textureId -= 1;
			}
		}
		else
		{
			if (sticky)
			{
				textureId -= 1;
			}
		}
		renderVertical(textureId, -.49f, .49f, .49f, .49f, -.49f, .98f);

		// Pop the matrix before
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
	 * @param sx
	 * @param sy
	 * @param sz
	 * @return
	 */
	public boolean hasAdjacentTorch(int sx, int sy, int sz)
	{
		int distance = 3;
		int x, y, z;
		int min_x = sx-distance;
		int max_x = sx+distance;
		int min_z = sz-distance;
		int max_z = sz+distance;
		int min_y = Math.max(0, sy-distance);
		int max_y = Math.min(127, sy+distance);
		Chunk otherChunk;
		int cx, cz;
		int tx, tz;
		for (x = min_x; x<=max_x; x++)
		{
			for (y = min_y; y<=max_y; y++)
			{
				for (z = min_z; z<=max_z; z++)
				{
					otherChunk = null;
					if (x < 0)
					{
						cx = this.x-1;
						tx = 16+x;
					}
					else if (x > 15)
					{
						cx = this.x+1;
						tx = x-16;
					}
					else
					{
						cx = this.x;
						tx = x;
					}

					if (z < 0)
					{
						cz = this.z-1;
						tz = 16+z;
					}
					else if (z > 15)
					{
						cz = this.z+1;
						tz = z-16;
					}
					else
					{
						cz = this.z;
						tz = z;
					}
					
					if (cx != this.x || cz != this.z)
					{
						otherChunk = level.getChunk(cx, cz);
						if (otherChunk == null)
						{
							continue;
						}
						else if (otherChunk.blockData.value[(tz*128)+(tx*128*16)+y] == BLOCK.TORCH.id)
						{
							return true;
						}
					}
					else
					{
						if (blockData.value[(z*128)+(x*128*16)+y] == BLOCK.TORCH.id)
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Renders our chunk.  Most of these options should really be consolidated somehow; maybe just pass in
	 * a HashMap or something with the options.  Anyway, for now it'll remain the same.
	 * 
	 * @param transparency Are we rendering "transparent" objects this time?  (ie: any nonstandard, nonsolid block)
	 * @param render_bedrock Are we forcing bedrock to be rendered?
	 * @param render_water Are we forcing water to be rendered?
	 * @param highlight_explored Are we highlighting the area around torches?
	 * @param onlySelected Are we ONLY rendering ores that the user's selected?
	 * @param selectedMap ... if so, here's a HashMap to which ones to highlight.
	 */
	public void renderWorld(boolean transparency, boolean render_bedrock, boolean render_water, boolean highlight_explored,
			boolean onlySelected, boolean[] selectedMap) {
		float worldX = this.x*16;
		float worldZ = this.z*16;
		
		boolean draw = false;
		boolean above = true;
		boolean below = true;
		boolean left = true;
		boolean right = true;
		boolean near = true;
		boolean far = true;
		
		for(int x=0;x<16;x++) {
			int xOff = (x * 128 * 16);
			for(int z=0;z<16;z++) {
				int zOff = (z * 128);
				int blockOffset = zOff + xOff-1;
				for(int y=0;y<128;y++) {
					blockOffset++;
					byte t = blockData.value[blockOffset];
					
					if(t < 1) {
						continue;
					}
					
					if (onlySelected)
					{
						draw = false;
						for(int i=0;i<selectedMap.length;i++) {
							if(selectedMap[i] && level.HIGHLIGHT_ORES[i].id == t) {
								// TODO: should maybe check our boundaries for similar ores, like we do for regular blocks
								draw = true;
								above = false;
								below = false;
								left = false;
								right = false;
								near = false;
								far = false;
								break;
							}
						}
					}
					else
					{
						if(transparency && isSolid(t)) {
							continue;
						}
						if(!transparency && !isSolid(t)) {
							continue;
						}
						
						draw = false;
						above = true;
						below = true;
						left = true;
						right = true;
						near = true;
						far = true;
					}
					
					if (!render_water && BLOCK_TYPE_MAP.get(t) == BLOCK_TYPE.WATER)
					{
						continue;
					}
					
					int textureId = blockDataToSpriteSheet[t];
					
					if(textureId == -1) {
						//System.out.println("Unknown block id: " + t);
						continue;
					}
					/*
					if(textureId == 253) {
						System.out.println("Unknown block id: " + t);
					}
					*/

					if (!onlySelected)
					{
						if (render_bedrock && t == MineCraftConstants.BLOCK.BEDROCK.id)
						{
							// This block of code was more or less copied/modified directly from the "else" block
							// below - should see if there's a way we can abstract this instead.  Also, I suspect
							// that this is where we'd fix water rendering...
							
							// check above
							if(y<127 && blockData.value[blockOffset+1] != MineCraftConstants.BLOCK.BEDROCK.id) {
								draw = true;
								above = false;
							}
							
							// check below
							if(y>0 && blockData.value[blockOffset-1] != MineCraftConstants.BLOCK.BEDROCK.id) {
								draw = true;
								below = false;
							}
							
							// check left;
							if(x>0 && blockData.value[blockOffset-BLOCKSPERCOLUMN] != MineCraftConstants.BLOCK.BEDROCK.id) {
								draw = true;
								left = false;
							} else if(x==0) {
								Chunk leftChunk = level.getChunk(this.x-1, this.z);
								if(leftChunk != null && leftChunk.getBlock(15, y, z) != MineCraftConstants.BLOCK.BEDROCK.id) {
									draw = true;
									left = false;
								}
							}
						
							// check right
							if(x<15 && blockData.value[blockOffset+BLOCKSPERCOLUMN] != MineCraftConstants.BLOCK.BEDROCK.id) {
								draw = true;
								right = false;
							} else if(x==15) {
								Chunk rightChunk = level.getChunk(this.x+1,this.z);
								if(rightChunk != null && rightChunk.getBlock(0, y, z) != MineCraftConstants.BLOCK.BEDROCK.id) {
									draw = true;
									right = false;
								}
							}
							
							// check near
							if(z>0 && blockData.value[blockOffset-BLOCKSPERROW] != MineCraftConstants.BLOCK.BEDROCK.id) {
								draw = true;
								near = false;
							} else if(z==0) {
								Chunk nearChunk = level.getChunk(this.x,this.z-1);
								if(nearChunk != null && nearChunk.getBlock(x, y, 15) != MineCraftConstants.BLOCK.BEDROCK.id) {
									draw = true;
									near = false;
								}
							}
							
							// check far
							if(z<15 && blockData.value[blockOffset+BLOCKSPERROW] != MineCraftConstants.BLOCK.BEDROCK.id) {
								draw = true;
								far = false;
							} else if(z==15) {
								Chunk farChunk = level.getChunk(this.x,this.z+1);
								if(farChunk != null && farChunk.getBlock(x, y, 0) != MineCraftConstants.BLOCK.BEDROCK.id) {
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
							} else if(x==0) {
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
							} else if(x==15) {
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
							} else if(z==0) {
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
							} else if(z==15) {
								Chunk farChunk = level.getChunk(this.x,this.z+1);
								if(farChunk != null && checkSolid(farChunk.getBlock(x, y, 0), transparency)) {
									draw = true;
									far = false;
								}
							}
						}
					}
					
					boolean adj_torch = false;
					if (draw)
					{
						// Check to see if this block type has a texture ID which changes depending
						// on the block's data value
						if (blockDataSpriteSheetMap.containsKey(t))
						{
							byte data = getData(x, y, z);

							if (t == BLOCK.SAPLING.id)
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
								textureId = blockDataSpriteSheetMap.get(t).get(data);
							}
							catch (NullPointerException e)
							{
								// Just report and continue
								System.out.println("Unknown data value for block type " + t + ": " + data);
							}
						}

						// If we're highlighting explored regions and there's an adjacent
						// torch, flip over to the "highlighted" textures
						if (highlight_explored)
						{
							adj_torch = hasAdjacentTorch(x,y,z);
							if (adj_torch)
							{
								textureId += 256;
							}
						}

						// Now process the actual drawing
						switch(BLOCK_TYPE_MAP.get(t))
						{
							case TORCH:
								renderTorch(textureId,x,y,z);
								break;
							case DECORATION_CROSS:
								renderCrossDecoration(textureId,x,y,z);
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
							case MINECART_TRACKS:
								renderMinecartTracks(textureId,x,y,z);
								break;
							case SIMPLE_RAIL:
								renderSimpleRail(textureId,x,y,z);
								break;
							case PRESSURE_PLATE:
								renderPlate(textureId,x,y,z);
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
							case FENCE:
								renderFence(textureId,x,y,z,blockOffset);
								break;
							case LEVER:
								renderLever(textureId,x,y,z);
								break;
							case BUTTON:
								renderButton(textureId,x,y,z);
								break;
							case PORTAL:
								renderPortal(textureId,x,y,z,blockOffset);
								break;
							case THINSLICE:
								renderThinslice(textureId,x,y,z);
								break;
							case BED:
								renderBed(textureId,x,y,z);
								break;
							case TRAPDOOR:
								renderTrapdoor(textureId,x,y,z);
								break;
							case PISTON_BODY:
								renderPistonBody(textureId,x,y,z,t);
								break;
							case PISTON_HEAD:
								renderPistonHead(textureId,x,y,z,false,false);
								break;
							case HALFHEIGHT:
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
								if(!near) this.renderWestEast(textureId, worldX+x, y, worldZ+z);
								if(!far) this.renderWestEast(textureId, worldX+x, y, worldZ+z+1);
								
								if(!below) this.renderTopDown(textureId, worldX+x, y, worldZ+z);
								if(!above) this.renderTopDown(textureId, worldX+x, y+1, worldZ+z);	
								
								if(!left) this.renderNorthSouth(textureId, worldX+x, y, worldZ+z);
								if(!right) this.renderNorthSouth(textureId, worldX+x+1, y, worldZ+z);
						}					
					}
				}
			}
		}
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
			info = MineCraftConstants.paintings.get(painting.name.toLowerCase());
			if (info == null)
			{
				System.out.println("Unknown painting name: " + painting.name);
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
			
			PaintingInfo backinfo = MineCraftConstants.paintingback;

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
	
	public void renderSolid(boolean render_bedrock, boolean render_water, boolean highlight_explored) {
		if(isDirty) {
				GL11.glNewList(this.displayListNum, GL11.GL_COMPILE);
				renderWorld(false, render_bedrock, false, highlight_explored, false, null);
				GL11.glEndList();
				GL11.glNewList(this.transparentListNum, GL11.GL_COMPILE);
				//GL11.glDepthMask(false);
				renderWorld(true, false, render_water, highlight_explored, false, null);
				//GL11.glDepthMask(true);
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
			renderWorld(false, false, false, false, true, selectedMap);
			GL11.glEndList();
			this.isSelectedDirty = false;
		}
		GL11.glCallList(this.selectedDisplayListNum);
	}
}
