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

import org.lwjgl.opengl.GL11;


/**
 * @author Vincent Vollers
 *
 * Some tools to draw very simple unoptimized sprites (ortho (2D) mode)
 */
public class SpriteTool {
	/***
	 * Draw a sprite starting at x,y
	 * @param texture
	 * @param x
	 * @param y
	 */
	public static void drawSpriteAbsoluteXY(Texture texture, float x, float y) {
		//manager.bindTexture(spriteName);
		//XRay.logger.debug(texture.getImage().getWidth() + " " + texture.getImage().getHeight());
		
		int width = texture.getImage().getWidth();
		int height = texture.getImage().getHeight();
		texture.bind();
		GL11.glPushMatrix();
			GL11.glTranslatef(x, y, 0.0f);
			GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

				GL11.glTexCoord2f(0, 0);
				GL11.glVertex2f(0, 0);
	
				GL11.glTexCoord2f(1, 0);
				GL11.glVertex2f(width, 0);
	
				GL11.glTexCoord2f(0, 1);
				GL11.glVertex2f(0, height);
	
				GL11.glTexCoord2f(1, 1);
				GL11.glVertex2f(width, height);

			GL11.glEnd();
		GL11.glPopMatrix();
	}
	
	public static void drawCurrentSprite(float x, float y, float width, float height, float startTexX, float startTexY, float endTexX, float endTexY) {
		GL11.glPushMatrix();
			GL11.glTranslatef(x, y, 0.0f);
			GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
	
				GL11.glTexCoord2f(startTexX, startTexY);
				GL11.glVertex2f(0, 0);
	
				GL11.glTexCoord2f(endTexX, startTexY);
				GL11.glVertex2f(width, 0);
	
				GL11.glTexCoord2f(startTexX, endTexY);
				GL11.glVertex2f(0, height);
	
				GL11.glTexCoord2f(endTexX, endTexY);
				GL11.glVertex2f(width, height);
	
			GL11.glEnd();
		GL11.glPopMatrix();
	}
	
	/***
	 * Draw a sprite with the center of the sprite at x,y
	 * @param frame
	 * @param x
	 * @param y
	 */
	public static void drawSprite(Texture frame, float x, float y) {
		float halfWidth = frame.getImage().getWidth() / 2.0f;
		float halfHeight = frame.getImage().getHeight() / 2.0f;

		frame.bind();
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0.0f);
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(-halfWidth, -halfHeight);

		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(halfWidth, -halfHeight);

		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(-halfWidth, halfHeight);

		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(halfWidth, halfHeight);
		GL11.glEnd();
		GL11.glPopMatrix();
	}

	/***
	 * Draw a sprite with its center at x,y and rotate it
	 * @param frame
	 * @param x
	 * @param y
	 * @param rotation
	 */
	public static void drawSpriteAndRotate(Texture frame, float x, float y,
			float rotation) {
		float halfWidth = frame.getImage().getWidth() / 2.0f;
		float halfHeight = frame.getImage().getHeight() / 2.0f;

		rotation = (rotation + 90.0f) % 360.0f;

		frame.bind();
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0.0f);
		GL11.glRotatef(rotation, 0, 0, 1);
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(-halfWidth, -halfHeight);

		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(halfWidth, -halfHeight);

		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(-halfWidth, halfHeight);

		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(halfWidth, halfHeight);
		GL11.glEnd();
		GL11.glPopMatrix();
	}

	/***
	 * Draw a sprite with its center at x,y and scale it
	 * @param frame
	 * @param x
	 * @param y
	 * @param scale
	 */
	public static void drawSpriteAndScale(Texture frame, float x, float y, float scale) {
		float halfWidth = frame.getImage().getWidth() / 2.0f;
		float halfHeight = frame.getImage().getHeight() / 2.0f;

		frame.bind();
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0.0f);
		GL11.glScalef(scale, scale, 1.0f);
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(-halfWidth, -halfHeight);

		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(halfWidth, -halfHeight);

		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(-halfWidth, halfHeight);

		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(halfWidth, halfHeight);
		GL11.glEnd();
		GL11.glPopMatrix();
	}

	/***
	 * Draw a sprite with its center at x,y and scale and rotate it
	 * @param frame
	 * @param x
	 * @param y
	 * @param scale
	 */
	public static void drawSpriteAndRotateAndScale(Texture frame, float x,
			float y, float rotation, float scale) {
		float halfWidth = frame.getImage().getWidth() / 2.0f;
		float halfHeight = frame.getImage().getHeight() / 2.0f;

		rotation = (rotation + 90.0f) % 360.0f;

		frame.bind();
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0.0f);
		GL11.glRotatef(rotation, 0, 0, 1.0f);
		GL11.glScalef(scale, scale, 1.0f);
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(-halfWidth, -halfHeight);

		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(halfWidth, -halfHeight);

		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(-halfWidth, halfHeight);

		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(halfWidth, halfHeight);
		GL11.glEnd();
		GL11.glPopMatrix();
	}

}
