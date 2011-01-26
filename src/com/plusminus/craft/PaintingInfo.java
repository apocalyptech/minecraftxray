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
package com.plusminus.craft;

public class PaintingInfo
{
	public float sizex;
	public float sizey;
	public float sizex_tex;
	public float sizey_tex;
	public float offsetx;
	public float offsety;
	public float centerx;
	public float centery;
	
	/**
	 * A new PaintingInfo.  Passed-in units are abstracted from pixels, so
	 * for (for instance) "Wanderer," you'd pass in (1, 2, 0, 4)
	 * 
	 * @param sizex How wide the painting is, in units of 16 pixels
	 * @param sizey How tall the painting is, in units of 16 pixels
	 * @param offsetx Offset in the painting file, in units of 16 pixels
	 * @param offsety Offset in the painting file, in units of 16 pixels
	 */
	public PaintingInfo(int sizex, int sizey, int offsetx, int offsety)
	{
		this.sizex = (float)sizex;
		this.sizey = (float)sizey;
		this.sizex_tex = (float)sizex/16.0f;
		this.sizey_tex = (float)sizey/16.0f;
		this.offsetx = (float)offsetx/16.0f;
		this.offsety = (float)offsety/16.0f;

		this.centerx = (float)((int)(this.sizex/2)-1);
		if (this.centerx < 0)
		{
			this.centerx = 0f;
		}
		this.centery = (float)((int)(this.sizey/2));
	}
}