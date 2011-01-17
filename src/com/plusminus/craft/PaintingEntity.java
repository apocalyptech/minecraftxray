package com.plusminus.craft;

import com.plusminus.craft.dtf.CompoundTag;
import com.plusminus.craft.dtf.StringTag;
//import com.plusminus.craft.dtf.DoubleTag;
import com.plusminus.craft.dtf.ByteTag;
//import com.plusminus.craft.dtf.ListTag;
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
		
		//ListTag pos = (ListTag) t.getTagWithName("Pos");
		//DoubleTag x = (DoubleTag) pos.value.get(0);
		//DoubleTag y = (DoubleTag) pos.value.get(1);
		//DoubleTag z = (DoubleTag) pos.value.get(2);
		
		this.tile_x = tile_x.value;
		this.tile_y = tile_y.value;
		this.tile_z = tile_z.value;
		this.name = name.value;
		this.dir = dir.value;
		
		//PaintingInfo info = MineCraftConstants.paintings.get(this.name.toLowerCase());
		//System.out.println("Painting: (" + x.value + ", " + y.value + ", " + z.value +
		//		") anchored to (" + this.tile_x + ", " + this.tile_y + ", " + this.tile_z + ") - " +
		//		this.name + ": " + info.sizex + "x" + info.sizey);

	}
}