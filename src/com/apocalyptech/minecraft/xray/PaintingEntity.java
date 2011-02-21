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

import com.apocalyptech.minecraft.xray.dtf.CompoundTag;
import com.apocalyptech.minecraft.xray.dtf.StringTag;
import com.apocalyptech.minecraft.xray.dtf.ByteTag;
import com.apocalyptech.minecraft.xray.dtf.IntTag;

public class PaintingEntity
{
	public float tile_x;
	public float tile_y;
	public float tile_z;
	public String name;
	public byte dir;
	
	public PaintingEntity(CompoundTag t)
	{
		IntTag tile_x = (IntTag) t.getTagWithName("TileX");
		IntTag tile_y = (IntTag) t.getTagWithName("TileY");
		IntTag tile_z = (IntTag) t.getTagWithName("TileZ");
		StringTag name = (StringTag) t.getTagWithName("Motive");
		ByteTag dir = (ByteTag) t.getTagWithName("Dir");
		
		this.tile_x = tile_x.value;
		this.tile_y = tile_y.value;
		this.tile_z = tile_z.value;
		this.name = name.value;
		this.dir = dir.value;
	}
}