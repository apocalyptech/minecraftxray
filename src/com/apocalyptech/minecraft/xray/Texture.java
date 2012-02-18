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

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

import org.lwjgl.opengl.GL11;

/***
 * Represents a texture with methods to quickly (!) update it from a given BufferedImage
 * 
 * I took this from a previous project 
 * 
 * The most interesting about this is the 'update' method, which will redraw an image into a texture compatible buffer and update the texture
 * 
 * @todo more documentation
 * @author Vincent
 */
public class Texture {
	private BufferedImage image;

	private WritableRaster raster;

	private BufferedImage texImage;

	private byte[] iBuffer;

	private byte[] tBuffer;

	private ByteBuffer textureCompatibleBuffer;

	private int[] bankOffsets;

	private int textureWidth = -1;

	private int textureHeight = -1;
	
	private int xScaleUnit;

	private int yScaleUnit;

	private int scanLineStride;

	private int pixelStride;

	private int frameNum;

	private long playTime;

	private long delayTime;
	
	private boolean flip = false;

	private Object syncObj = new Object();
	
	private int textureId = -1;

	/*public static final ColorModel glAlphaColorModel = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB),
			new int[] { 8, 8, 8, 8 }, true, false,
			ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);

	public static final ColorModel glColorModel = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB),
			new int[] { 8, 8, 8, 0 }, false, false, ComponentColorModel.OPAQUE,
			DataBuffer.TYPE_BYTE);*/

	public Texture(BufferedImage image, int frameNum, long playTime,
			long delayTime) {
		// XRay.logger.trace(image.hashCode());
		this.image = image;
		this.frameNum = frameNum;
		this.playTime = playTime;
		this.delayTime = delayTime;
		this.textureCompatibleBuffer = null;
	}
	
	public Texture(BufferedImage image) {
		// XRay.logger.trace(image.hashCode());
		this.image = image;
		this.frameNum = -1;
		this.playTime = -1;
		this.delayTime = -1;
		this.textureCompatibleBuffer = null;
	}

	public int getTextureId() {
		return this.textureId;
	}
	
	public void setTextureId(int id) {
		this.textureId = id;
	}
	
	public void setFlip(boolean flip) {
		this.flip = flip;
	}
	
	public boolean getFlip() {
		return flip;
	}
	
	public int getTextureWidth() {
		return this.textureWidth;
	}

	public int getTextureHeight() {
		return this.textureHeight;
	}
	
	public long getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(long delayTime) {
		this.delayTime = delayTime;
	}

	public int getFrameNum() {
		return frameNum;
	}

	public void setFrameNum(int frameNum) {
		this.frameNum = frameNum;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public long getPlayTime() {
		return playTime;
	}

	public void setPlayTime(long playTime) {
		this.playTime = playTime;
	}

	public byte[] getTextureData() {
		return this.tBuffer;
	}

	public byte[] getImageData() {
		return this.iBuffer;
	}
	
	public void setImageData(byte[] iBuffer) {
		this.iBuffer = iBuffer;
	}

	public void initializeTextureCompatibleBuffer() {
		textureWidth = TextureTool.get2Fold(image.getWidth());
		textureHeight = TextureTool.get2Fold(image.getHeight());

		// generate an interleaved byte-based ARGB raster and image
		raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
				this.textureWidth, this.textureHeight, 4, null);
		texImage = new BufferedImage(TextureTool.glAlphaColorModel, raster, false,
				new Hashtable<String, Object>());

		// get the pointers to the data of the [t]exture buffer
		// and the data of the [i]image buffer
		iBuffer = ((DataBufferByte) image.getRaster().getDataBuffer())
				.getData();
		tBuffer = ((DataBufferByte) texImage.getRaster()
				.getDataBuffer()).getData();

		// get information on how the image is stored in the buffer
		ComponentSampleModel imageSampleModel = (ComponentSampleModel) image
				.getSampleModel();
		scanLineStride = imageSampleModel.getScanlineStride();
		pixelStride = imageSampleModel.getPixelStride();
		bankOffsets = imageSampleModel.getBandOffsets();

		// generate a fixed point floating point number
		// which will allow us to calculate for a given x or y on the
		// texture
		// where the source pixel on the image is
		xScaleUnit = (int) (((double) image.getWidth() / (double) textureWidth) * 65536);
		yScaleUnit = (int) (((double) image.getHeight() / (double) textureHeight) * 65536);

		// generate a bytebuffer to store the resulting image
		textureCompatibleBuffer = ByteBuffer
				.allocateDirect(tBuffer.length);
		textureCompatibleBuffer.order(ByteOrder.nativeOrder());
	}
	
	public void updateTextureCompatibleBuffer() {
		synchronized (this.syncObj) {
			// long time = System.currentTimeMillis();
			int adr = 0; // the address in the texture buffer
			int xOffset = 0; // the x coordinate in the image
			int bufferOffset = 0; // the final buffer offset in the image
			int yOffset = 0; // the y coordinate in the image
			int yBufferOffset = 0; // a temp value containing the start of the
									// scanline in the image
			int x,y; // the coordinates on the texture image
			
			
			if(flip) {
				yOffset = (image.getHeight()-1)  * 65536;
				if (bankOffsets.length > 3) { // RGBA or ABGR images
					int bOff0 = bankOffsets[0];
					int bOff1 = bankOffsets[1];
					int bOff2 = bankOffsets[2];
					int bOff3 = bankOffsets[3];
					
					for (y = 1; y < textureHeight; y++) {
						xOffset = 0;

						yBufferOffset = (yOffset >> 16) * scanLineStride;

						for (x = 0; x < textureWidth; x++) {
							bufferOffset = yBufferOffset + (xOffset >> 16)
									* pixelStride;

							tBuffer[adr] 	= iBuffer[bufferOffset + bOff0];
							tBuffer[adr+1] 	= iBuffer[bufferOffset + bOff1];
							tBuffer[adr+2]	= iBuffer[bufferOffset + bOff2];
							tBuffer[adr+3] 	= iBuffer[bufferOffset + bOff3];

							xOffset += xScaleUnit;
							adr += 4;
						}
						yOffset -= yScaleUnit;
					}
				} else { // RGB or BGR images
					//int y;
					//int x;
					int bOff0 = bankOffsets[0];
					int bOff1 = bankOffsets[1];
					int bOff2 = bankOffsets[2];
					for (y = 1; y < textureHeight; y++) {
						xOffset = 0;

						yBufferOffset = (yOffset >> 16) * scanLineStride;

						for (x = 0; x < textureWidth; x++) {
							bufferOffset = yBufferOffset + (xOffset >> 16)
									* pixelStride;

							tBuffer[adr] 	= iBuffer[bufferOffset + bOff0];
							tBuffer[adr+1] 	= iBuffer[bufferOffset + bOff1];
							tBuffer[adr+2] 	= iBuffer[bufferOffset + bOff2];
							tBuffer[adr+3] 	= -1; // -1 signed = 255 unsigned

							xOffset += xScaleUnit;
							adr += 4;
						}
						yOffset -= yScaleUnit;
					}
				}	
			} else {
				if (bankOffsets.length > 3) { // RGBA or ABGR images
					int bOff0 = bankOffsets[0];
					int bOff1 = bankOffsets[1];
					int bOff2 = bankOffsets[2];
					int bOff3 = bankOffsets[3];
					for (y = 0; y < textureHeight; y++) {
						xOffset = 0;
		
						yBufferOffset = (yOffset >> 16) * scanLineStride;
		
						for (x = 0; x < textureWidth; x++) {
							bufferOffset = yBufferOffset + (xOffset >> 16)
									* pixelStride;
		
							tBuffer[adr] 	= iBuffer[bufferOffset + bOff0];
							tBuffer[adr+1] 	= iBuffer[bufferOffset + bOff1];
							tBuffer[adr+2]	= iBuffer[bufferOffset + bOff2];
							tBuffer[adr+3] 	= iBuffer[bufferOffset + bOff3];
		
							xOffset += xScaleUnit;
							adr += 4;
						}
						yOffset += yScaleUnit;
					}
				} else { // RGB or BGR images
					//int y;
					//int x;
					int bOff0 = bankOffsets[0];
					int bOff1 = bankOffsets[1];
					int bOff2 = bankOffsets[2];
					for (y = 0; y < textureHeight; y++) {
						xOffset = 0;
		
						yBufferOffset = (yOffset >> 16) * scanLineStride;
		
						for (x = 0; x < textureWidth; x++) {
							bufferOffset = yBufferOffset + (xOffset >> 16)
									* pixelStride;
		
							tBuffer[adr] 	= iBuffer[bufferOffset + bOff0];
							tBuffer[adr+1] 	= iBuffer[bufferOffset + bOff1];
							tBuffer[adr+2] 	= iBuffer[bufferOffset + bOff2];
							tBuffer[adr+3] 	= -1; // -1 signed = 255 unsigned
		
							xOffset += xScaleUnit;
							adr += 4;
						}
						yOffset += yScaleUnit;
					}
				}
			}
			textureCompatibleBuffer.rewind();
			textureCompatibleBuffer.put(tBuffer, 0, tBuffer.length);
		}
	}

	public void bind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId); 
    }
	
	public ByteBuffer getTextureCompatibleBuffer() {
		synchronized (this.syncObj) {
			return (ByteBuffer) textureCompatibleBuffer.rewind();
		}
	}

	public synchronized void setTextureCompatibleBuffer(
			ByteBuffer textureCompatibleBuffer) {
		synchronized (this.syncObj) {
			this.textureCompatibleBuffer = textureCompatibleBuffer;
		}
	}
	
	public void update() {
		updateTextureCompatibleBuffer();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId); 
		textureCompatibleBuffer.rewind();
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, textureWidth, textureHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureCompatibleBuffer);
	}
}
