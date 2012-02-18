/** * Copyright (c) 2010-2012, Christopher J. Kucera
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

import java.awt.image.BufferedImage;

public class TextureDecorationStats
{
	private int top;
	private int bottom;
	private int left;
	private int right;

	private float top_f;
	private float bottom_f;
	private float left_f;
	private float right_f;
	private float width_f;
	private float height_f;

	private float top_tex_f;
	private float bottom_tex_f;
	private float left_tex_f;
	private float right_tex_f;
	private float width_tex_f;
	private float height_tex_f;

	public TextureDecorationStats(Texture textureData, int textureId)
	{
		int x;
		int y;
		int a;
		int row = textureId % 16;
		int col = textureId / 16;
		BufferedImage bi = textureData.getImage();
		int square_width = bi.getWidth()/16;
		int[] pixels = new int[square_width*square_width];
		bi.getRGB(row*square_width, col*square_width, square_width, square_width, pixels, 0, square_width);

		// Alpha threshhold that we'll test against
		int threshhold = 20;

		// Now that we have the necessary pixel information, figure out what the actual
		// bounds are.  First top
		this.top = 0;
		mainloop: for (y=0; y<square_width; y++)
		{
			for (x=0; x<square_width; x++)
			{
				if (getAlpha(pixels, square_width, x, y) > threshhold)
				{
					this.top = y;
					break mainloop;
				}
			}
		}

		// Now bottom
		this.bottom = square_width-1;
		mainloop: for (y=square_width-1; y>=0; y--)
		{
			for (x=0; x<square_width; x++)
			{
				if (getAlpha(pixels, square_width, x, y) > threshhold)
				{
					this.bottom = y;
					break mainloop;
				}
			}
		}

		// Now left
		this.left = 0;
		mainloop: for (x=0; x<square_width; x++)
		{
			for (y=0; y<square_width; y++)
			{
				if (getAlpha(pixels, square_width, x, y) > threshhold)
				{
					this.left = x;
					break mainloop;
				}
			}
		}

		// Now right
		this.right = square_width-1;
		mainloop: for (x=square_width-1; x>=0; x--)
		{
			for (y=0; y<square_width; y++)
			{
				if (getAlpha(pixels, square_width, x, y) > threshhold)
				{
					this.right = x;
					break mainloop;
				}
			}
		}

		// Now compute the float values
		this.top_f = (float)this.top/(float)square_width;
		this.bottom_f = (float)this.bottom/(float)square_width;
		this.left_f = (float)this.left/(float)square_width;
		this.right_f = (float)this.right/(float)square_width;
		this.width_f = (float)(this.right+1-this.left)/(float)square_width;
		this.height_f = (float)(this.bottom+1-this.top)/(float)square_width;

		this.top_tex_f = (float)this.top/(float)bi.getHeight();
		this.bottom_tex_f = (float)this.bottom/(float)bi.getHeight();
		this.left_tex_f = (float)this.left/(float)bi.getWidth();
		this.right_tex_f = (float)this.right/(float)bi.getWidth();
		this.width_tex_f = (float)(this.right+1-this.left)/(float)bi.getWidth();
		this.height_tex_f = (float)(this.bottom+1-this.top)/(float)bi.getHeight();
	}

	/**
	 * Gets the alpha value for a given pixel in an array.
	 */
	private static int getAlpha(int[] pixels, int width, int x, int y)
	{
		return pixels[y*width + x] >>> 24;
	}

	public float getTop()
	{
		return this.top_f;
	}

	public float getBottom()
	{
		return this.bottom_f;
	}

	public float getLeft()
	{
		return this.left_f;
	}

	public float getRight()
	{
		return this.right_f;
	}

	public float getWidth()
	{
		return this.width_f;
	}

	public float getHeight()
	{
		return this.height_f;
	}

	public float getTexTop()
	{
		return this.top_tex_f;
	}

	public float getTexBottom()
	{
		return this.bottom_tex_f;
	}

	public float getTexLeft()
	{
		return this.left_tex_f;
	}

	public float getTexRight()
	{
		return this.right_tex_f;
	}

	public float getTexWidth()
	{
		return this.width_tex_f;
	}

	public float getTexHeight()
	{
		return this.height_tex_f;
	}
}
