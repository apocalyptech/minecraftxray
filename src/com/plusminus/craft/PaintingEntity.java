package com.plusminus.craft;

import com.plusminus.craft.dtf.CompoundTag;
import com.plusminus.craft.dtf.StringTag;
import com.plusminus.craft.dtf.ByteTag;
import com.plusminus.craft.dtf.IntTag;

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