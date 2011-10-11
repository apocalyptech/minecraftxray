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

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/***
 * Convenience class to quickly load/generate textures
 * 
 * @author Vincent
 */
public class TextureTool {
	public static double invln2 = 1.0 / Math.log(2.0);
	public static ColorModel glAlphaColorModel= new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
	new int[] {8,8,8,8},
	true,
	false,
	ComponentColorModel.TRANSLUCENT,
	DataBuffer.TYPE_BYTE);
	public static ColorModel glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
	 new int[] {8,8,8,0},
	 false,
	 false,
	 ComponentColorModel.OPAQUE,
	 DataBuffer.TYPE_BYTE);
	
	public static Texture allocateTexture(Texture frame) {
			return TextureTool.allocateTexture(frame, GL11.GL_RGBA,     // dst pixel format
					GL11.GL_LINEAR, // min filter (unused)
					GL11.GL_LINEAR, // mag filter (unused)
                        true);
	}
	
	public static Texture  allocateTexture(Texture  frame,  int dstPixelFormat, 
	           int minFilter, 
	           int magFilter, 
	           boolean wrap) {
		int srcPixelFormat = 0;
        
        // create the texture ID for this texture
        int textureId = TextureTool.createTextureID(); 
 
		//XRay.logger.trace("frame: " + frame.getFrameNum());
        frame.setTextureId(textureId);
		
		frame.bind();
					       
		frame.initializeTextureCompatibleBuffer();
		frame.updateTextureCompatibleBuffer();
        
        if (frame.getImage().getColorModel().hasAlpha()) {
            srcPixelFormat = GL11.GL_RGBA;
        } else {
            srcPixelFormat = GL11.GL_RGB;
        }
        
        // convert that image into a byte buffer of texture data
        //ByteBuffer textureBuffer = TextureLoader.convertImageData(bufferedImage,texture); 
        int wrapMode = wrap ? GL11.GL_REPEAT : GL11.GL_CLAMP; 

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapMode); 
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapMode); 
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter); 
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
 
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 
                          0, 
                          dstPixelFormat, 
                          frame.getTextureWidth(), 
						  frame.getTextureHeight(), 
                          0, 
                          srcPixelFormat, 
						  GL11.GL_UNSIGNED_BYTE, 
						  frame.getTextureCompatibleBuffer()); 
		
		return frame;
	}
	
	public static Texture  allocateTexture(int width, int height) throws IOException {
       return TextureTool.allocateTexture(
			   GL11.GL_RGBA,     // dst pixel format
			   GL11.GL_LINEAR, // min filter (unused)
			   GL11.GL_LINEAR, // mag filter (unused)
                        width,
                        height,
                        true);  // wrap? 
	}
	
	public static Texture  allocateTexture(BufferedImage image) throws IOException {
	       Texture tex = TextureTool.allocateTexture(
				   GL11.GL_RGBA,     // dst pixel format
				   GL11.GL_LINEAR, // min filter (unused)
				   GL11.GL_LINEAR, // mag filter (unused)
	                       image.getWidth(),
	                        image.getHeight(),
	                        true);  // wrap? 
	       Graphics2D g = tex.getImage().createGraphics();
	       g.drawImage(image, 0, 0, null);
	       return tex;
		}
	public static Texture allocateTexture(BufferedImage image, int filter) throws IOException {
		Texture tex = TextureTool.allocateTexture(
				   GL11.GL_RGBA,     // dst pixel format
				   filter, 
				   filter, 
	                       image.getWidth(),
	                        image.getHeight(),
	                        true);  // wrap? 
	       Graphics2D g = tex.getImage().createGraphics();
	       g.drawImage(image, 0, 0, null);
	       return tex;
	}
	public static int TEXTURETYPE=BufferedImage.TYPE_4BYTE_ABGR;
	
	public static Texture  allocateTexture(
           int dstPixelFormat, 
           int minFilter, 
           int magFilter, 
           int width,
           int height,
           boolean wrap) throws IOException {
		  	int srcPixelFormat = 0;
	        
	        // create the texture ID for this texture
	        int textureId = TextureTool.createTextureID(); 
	 
	        BufferedImage bufferedImage = new BufferedImage(width, height, TEXTURETYPE);
	        Texture  frame = new Texture(bufferedImage);
			
			frame.setTextureId(textureId);
			
			//gl.glBindTexture(GL.GL_TEXTURE_2D, textureID); 
			frame.bind();
						       
			frame.initializeTextureCompatibleBuffer();
			frame.updateTextureCompatibleBuffer();
	        //XRay.logger.trace(realWidth);
	        //XRay.logger.trace(realHeight);
	        
	        if (bufferedImage.getColorModel().hasAlpha()) {
	            srcPixelFormat = GL11.GL_RGBA;
	        } else {
	            srcPixelFormat = GL11.GL_RGB;
	        }
	        
	        // convert that image into a byte buffer of texture data
	        //ByteBuffer textureBuffer = TextureLoader.convertImageData(bufferedImage,texture); 
	        int wrapMode = wrap ? GL11.GL_REPEAT : GL11.GL_CLAMP; 

			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapMode); 
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapMode); 
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter); 
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
	 
			
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 
	                          0, 
	                          dstPixelFormat, 
	                          frame.getTextureWidth(), 
							  frame.getTextureHeight(), 
	                          0, 
	                          srcPixelFormat, 
							  GL11.GL_UNSIGNED_BYTE, 
							  frame.getTextureCompatibleBuffer()); 

	        return frame; 
	}
	
   private static int createTextureID() 
   { 
	  IntBuffer newBuffer = BufferUtils.createIntBuffer(1);//ByteBuffer.allocateDirect(Integer.SIZE/8).asIntBuffer();
	  GL11.glGenTextures(newBuffer);
      return newBuffer.get(0); 
   } 
   
	
	public static void updateTexture(Texture frame){			
		frame.bind();
				
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, frame.getTextureWidth(), frame.getTextureHeight(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, frame.getTextureCompatibleBuffer());
	}
	
	public static void updateTexture(Texture frame, BufferedImage image){
		frame.setImage(image);
		frame.updateTextureCompatibleBuffer();
		
		frame.bind();
				
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, frame.getTextureWidth(), frame.getTextureHeight(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, frame.getTextureCompatibleBuffer());
	}
	
	public static int get2Fold2(int num) {
		return (int) Math.pow(2, Math.ceil(Math.log(num) * invln2));
	}
	
	/**
	 * Calculate how many bits wide a number is, i.e. position of highest 1 bit.
	 * Fully unraveled binary search method.
	 * 
	 * @return p where 2**p is first power of two >= n. e.g. binary 0001_0101 ->
	 *         5, 0xffffffff -> 32, 0 -> 0, 1 -> 1, 2 -> 2, 3 -> 2, 4 -> 3
	 *         (Vincent) Changed to return 2^n instead of n
	 * @author Dirk Bosmans Dirk.Bosmans@tijd.com
	 * @author Vincent Vollers
	 */
	static public final int get2Fold(int n) {
		if (n < 0)
			return 32;
		if (n > 0x00010000) {
			if (n > 0x01000000) {
				if (n > 0x10000000) {
					if (n > 0x40000000) {
						// if ( n > 0x7fffffff )
						// return 32
						// else
						// return 2147483648; is too much, so return -1
						return -1;
					} else {
						// !( n > 0x3fffffff )
						if (n > 0x20000000)
							return 1073741824;
						else
							return 536870912;
					}
				} else {
					// !( n > 0x0fffffff )
					if (n > 0x04000000) {
						if (n > 0x08000000)
							return 268435456;
						else
							return 134217728;
					} else {
						// !( n > 0x03ffffff )
						if (n > 0x02000000)
							return 67108864;
						else
							return 33554432;
					}
				}
			} else {
				// !( n > 0x00ffffff )
				if (n > 0x00100000) {
					if (n > 0x00400000) {
						if (n > 0x00800000)
							return 16777216;
						else
							return 8388608;
					} else {
						// !( n > 0x003fffff )
						if (n > 0x00200000)
							return 4194304;
						else
							return 2097152;
					}
				} else {
					// !( n > 0x000fffff )
					if (n > 0x00040000) {
						if (n > 0x00080000)
							return 1048576;
						else
							return 524288;
					} else {
						// !( n > 0x0003ffff )
						if (n > 0x00020000)
							return 262144;
						else
							return 131072;
					}
				}
			}
		} else {
			// !( n > 0x0000ffff )
			if (n > 0x00000100) {
				if (n > 0x00001000) {
					if (n > 0x00004000) {
						if (n > 0x00008000)
							return 65536;
						else
							return 32768;
					} else {
						// !( n > 0x00003fff )
						if (n > 0x00002000)
							return 16384;
						else
							return 8192;
					}
				} else {
					// !( n > 0x00000fff )
					if (n > 0x00000400) {
						if (n > 0x00000800)
							return 4096;
						else
							return 2048;
					} else {
						// !( n > 0x000003ff )
						if (n > 0x00000200)
							return 1024;
						else
							return 512;
					}
				}
			} else {
				// !( n > 0x000000ff )
				if (n > 0x00000010) {
					if (n > 0x00000040) {
						if (n > 0x00000080)
							return 256;
						else
							return 128;
					} else {
						// !( n > 0x0000003f )
						if (n > 0x00000020)
							return 64;
						else
							return 32;
					}
				} else {
					// !( n > 0x0000000f )
					if (n > 0x00000004) {
						if (n > 0x00000008)
							return 16;
						else
							return 8;
					} else {
						// !( n > 0x00000003 )
						if (n > 0x00000002)
							return 4;

						return n;
					}
				}
			}
		}
	} // end widthInBits
}
