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
	
	public void renderLeftRight(int t, float x, float y, float z) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-0.5f, y+0.5f, z+0.5f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-0.5f, y+0.5f, z-0.5f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t],precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x-0.5f, y-0.5f, z+0.5f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x-0.5f, y-0.5f, z-0.5f);
		GL11.glEnd();
	}
	
	public void renderTopDown(int t, float x, float y, float z) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-0.5f, y-0.5f, z+0.5f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-0.5f, y-0.5f, z-0.5f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x+0.5f, y-0.5f, z+0.5f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x+0.5f, y-0.5f, z-0.5f);
		GL11.glEnd();
	}
	
	public void renderFarNear(int t, float x, float y, float z) {
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x-0.5f, y+0.5f, z-0.5f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]);
			GL11.glVertex3f(x+0.5f, y+0.5f, z-0.5f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t], precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x-0.5f, y-0.5f, z-0.5f);
	
			GL11.glTexCoord2f(precalcSpriteSheetToTextureX[t]+TEX16, precalcSpriteSheetToTextureY[t]+TEX16);
			GL11.glVertex3f(x+0.5f, y-0.5f, z-0.5f);
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
				(block_type == BLOCK_TYPE.TORCH) || (block_type == BLOCK_TYPE.UPRIGHT) ||
				(block_type == BLOCK_TYPE.CROPS)) {
			return false;
		}
		
		return true;
	}
	
	public void renderSpecial(float bx, float by, float ex, float ey, float x, float y, float z, float yy, float x1, float x2, float z1, float z2) {
		 
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
		 x1 *= 2.125f; z1 *= 2.125f;
		
		 GL11.glNormal3f(0.0f, 1.0f, 0.0f);
		 GL11.glTexCoord2f(bx+TEX128, by+TEX128); 	GL11.glVertex3f(x+x1+9/16.0f, y+yy+10/16.0f, z+z1+7/16.0f);
		 GL11.glTexCoord2f(ex-TEX128, by+TEX128); 	GL11.glVertex3f(x+x1+7/16.0f, y+yy+10/16.0f, z+z1+7/16.0f);
		 GL11.glTexCoord2f(ex-TEX128, ey-TEX32); 	GL11.glVertex3f(x+x1+7/16.0f, y+yy+10/16.0f, z+z1+9/16.0f);
		 GL11.glTexCoord2f(bx+TEX128, ey-TEX32); 	GL11.glVertex3f(x+x1+9/16.0f, y+yy+10/16.0f, z+z1+9/16.0f);
		 
		 GL11.glEnd();
		 //GL11.glEnable(GL11.GL_DEPTH_TEST);
		 //GL11.glEnable(GL11.GL_CULL_FACE);	
	}
	
	public void renderTorch(int xxx, int yyy, int zzz, int block_type) {
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
		 
		 switch(block_type)
		 {
		 	case MineCraftConstants.BLOCK_REDSTONE_TORCH_ON:		 		
				bx = (TEX16*3) + TEX64;
				by = TEX16 + (5.0f / 16.0f);
		 		break;
		 	case MineCraftConstants.BLOCK_REDSTONE_TORCH_OFF:
				bx = (TEX16*3) + TEX64;
				by = (TEX16*2) + (5.0f / 16.0f);
		 		break;
		 	default:
		 		bx = 0.0f+TEX64;
		 		by = 5.0f / 16.0f;
		 }
		 ex = bx + TEX16-TEX32;
		 ey = by + TEX16;
		 
		 renderSpecial(bx, by, ex, ey, x, y, z, yy, x1, x2, z1, z2);
	}
	
	/***
	 * Yanked wholesale from renderTorch
	 * @param xxx
	 * @param yyy
	 * @param zzz
	 * @param block_type
	 */
	public void renderUpright(int xxx, int yyy, int zzz, int block_type) {
		 float x1 = 0, x2 = 0, z1 = 0, z2 = 0, yy = 0;
		 //Light(chunk, x, y, z);
		 float x = xxx + this.x*16 -0.5f;
		 float z = zzz + this.z*16 -0.5f;
		 float y = yyy - 0.5f;
		 
		 float bx,by;
		 float ex,ey;
		 
		 switch(block_type)
		 {
		 	case MineCraftConstants.BLOCK_SAPLING:		 		
				bx = .9375f + TEX64; // 12/16
				by = 0;
		 		break;
		 	case MineCraftConstants.BLOCK_RED_ROSE:		 		
				bx = .75f + TEX64; // 12/16
				by = 0;
		 		break;
		 	case MineCraftConstants.BLOCK_YELLOW_FLOWER:		 		
				bx = .8125f + TEX64; // 13/16
				by = 0;
		 		break;
		 	case MineCraftConstants.BLOCK_RED_MUSHROOM:
				bx = .75f + TEX64; // 12/16
				by = .0625f; // 1/16
		 		break;
		 	case MineCraftConstants.BLOCK_BROWN_MUSHROOM:
		 	default:
		 		bx = .8125f + TEX64; // 13/16
		 		by = .0625f; // 1/16
		 }		 
 		 ex = bx + TEX16-TEX32;
 		 ey = by + TEX16;

 		 renderSpecial(bx, by, ex, ey, x, y, z, yy, x1, x2, z1, z2);
	}

	public void renderCrops(int xxx, int yyy, int zzz) {
		 float x1 = 0, x2 = 0, z1 = 0, z2 = 0, yy = 0;
		 float x = xxx + this.x*16 -0.5f;
		 float z = zzz + this.z*16 -0.5f;
		 float y = yyy - 0.5f;
		 
		 float bx,by;
		 float ex,ey;

		 byte data = getData(xxx, yyy, zzz);
		 switch(data)
		 {
		 	case 7:
		 		bx = .9375f; // 15/16
		 		break;
		 	case 6:
		 		bx = .875f; // 14/16
		 		break;
		 	case 5:
		 		bx = .8125f; // 13/16
		 		break;
		 	case 4:
		 		bx = .75f; // 12/16
		 		break;
		 	case 3:
		 		bx = .6875f; // 11/16
		 		break;
		 	case 2:
		 		bx = .625f; // 10/16
		 		break;
		 	case 1:
		 		bx = .5625f; // 9/16
		 		break;
		 	case 0:
		 	default:
		 		bx = 0.5f; // 8/16
		 }
		 by = .3125f; // 5/16
		 ex = bx + TEX16;
		 ey = by + TEX16;
		 
		 renderSpecial(bx, by, ex, ey, x, y, z, yy, x1, x2, z1, z2);
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
								renderTorch(x,y,z,t);
								break;
							case UPRIGHT:
								renderUpright(x,y,z,t);
								break;
							case CROPS:
								renderCrops(x,y,z);
								break;
							default:
								// if we have to draw this block
								if(draw) {
									if(!near) this.renderFarNear(textureId, worldX+x, y, worldZ+z);
									if(!far) this.renderFarNear(textureId, worldX+x, y, worldZ+z+1);
									
									if(!below) this.renderTopDown(textureId, worldX+x, y, worldZ+z);
									if(!above) this.renderTopDown(textureId, worldX+x, y+1, worldZ+z);	
									
									if(!left) this.renderLeftRight(textureId, worldX+x, y, worldZ+z);
									if(!right) this.renderLeftRight(textureId, worldX+x+1, y, worldZ+z);
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
							this.renderFarNear(textureId, worldX+x, y, worldZ+z);
							this.renderFarNear(textureId, worldX+x, y, worldZ+z+1);
							
							this.renderTopDown(textureId, worldX+x, y, worldZ+z);
							this.renderTopDown(textureId, worldX+x, y+1, worldZ+z);	
							
							this.renderLeftRight(textureId, worldX+x, y, worldZ+z);
							this.renderLeftRight(textureId, worldX+x+1, y, worldZ+z);
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
