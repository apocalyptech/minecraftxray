/**
 * Copyright (c) 2010-2011, Vincent Vollers
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
 * DISCLAIMED. IN NO EVENT SHALL VINCENT VOLLERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.apocalyptech.minecraft.xray.dtf;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

public class DTFReader {
	public static Tag readTag(byte tagType, String name, DataInputStream stream) throws IOException {
		switch(tagType) {
			case 0: // end
				return new EndTag();
			case 1:
				return new ByteTag(name, stream.readByte());
			case 2:
				return new ShortTag(name, stream.readShort());
			case 3:
				return new IntTag(name, stream.readInt());
			case 4:
				return new LongTag(name, stream.readLong());
			case 5:
				return new FloatTag(name, stream.readFloat());
			case 6:
				return new DoubleTag(name, stream.readDouble());
			case 7:
				int len = stream.readInt();
				//System.out.println(len);
				byte[] data = new byte[len];
				
				stream.readFully(data);
				//System.out.println(ll + " van " + len);
				return new ByteArrayTag(name, data);
			case 8:
				return new StringTag(name, stream.readUTF());
			case 9:
				byte type 		= stream.readByte();
				int listLength 	= stream.readInt();
				ArrayList<Tag> list = new ArrayList<Tag>();
				for(int i=0;i<listLength;i++) {
					Tag t = readTag(type, "", stream);
					//System.out.println(t.toString());
					list.add(t);
				}
				return new ListTag(name, list);
			case 10:
				ArrayList<Tag> compound = new ArrayList<Tag>();
				while((type = stream.readByte()) != 0) {
					//String tagName = stream.readUTF();
					//short l = stream.readShort();
					//System.out.println(type + ", " + l);
					//stream.skip(l);
					//String tagName = "errorTest";
					String tagName = stream.readUTF();
					Tag tag = readTag(type, tagName, stream);
					//System.out.println(tag.toString());
					compound.add(tag);
				}
				return new CompoundTag(name, compound);
		}
		return null;
	}
	
	/**
	 * Reads tag data from the given inputstream.  Note that right now we close the stream
	 * after reading, which I guess may not be a good idea, should probably do that the
	 * Right Way in the future.
	 * 
	 * TODO: figure that out.
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static Tag readTagData(DataInputStream stream) throws IOException {
		if(stream.available() > 0) {
			byte type = stream.readByte();
			if(type != 0){
				String name = stream.readUTF();
				Tag t = readTag(type, name, stream);
				//System.out.println(t);
				stream.close();
				return t;
			}
		}
		stream.close();
		return null;
	}
	
	public static Tag readDTFFile(File f) {
		try {
			DataInputStream stream = new DataInputStream(new GZIPInputStream(new FileInputStream(f)));
			
			return readTagData(stream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block"
			System.err.println("Error reading " + f.getPath() + " -");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error reading " + f.getPath() + " -");
			e.printStackTrace();
		}
		return null;
	}
}
