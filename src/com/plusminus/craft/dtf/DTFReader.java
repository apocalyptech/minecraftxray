package com.plusminus.craft.dtf;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
