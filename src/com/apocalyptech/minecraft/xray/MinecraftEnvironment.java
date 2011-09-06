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

import com.apocalyptech.minecraft.xray.WorldInfo;
import static com.apocalyptech.minecraft.xray.MinecraftConstants.*;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.LineNumberReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileFilter;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;

/***
 * Utility class which has convenience methods to access the
 * files of the current minecraft installation
 * @author Vincent Vollers
 */
public class MinecraftEnvironment {
	public static enum OS {XP, Vista, MacOS, Linux, NotSupported};
	public static OS os; 
	public static File baseDir;
	public static File xrayBaseDir;
	
    private static class MCDirectoryFilter implements FileFilter
    {
        public MCDirectoryFilter() {
            // Nothing, really
        }

        public boolean accept(File pathname) {
			if (pathname.exists() && pathname.canRead() && pathname.isDirectory())
			{
				File levelDat = new File(pathname, "level.dat");
				return (levelDat.exists() && levelDat.canRead());
			}
			return false;
        }
    }

	private static class CaseInsensitiveComparator implements Comparator<File>
	{
		public int compare(File filea, File fileb)
		{
			return filea.getName().compareToIgnoreCase(fileb.getName());
		}
	}
	
	static {
		String os = System.getProperty( "os.name" );
		HashMap<String,OS> osData = new HashMap<String,OS>();
		/*  Full list of os.name strings
		    AIX
			Digital Unix
			FreeBSD
			HP UX
			Irix
			Linux
			Mac OS
			Mac OS X
			MPE/iX
			Netware 4.11
			OS/2
			Solaris
			Windows 2000
			Windows 7
			Windows 95
			Windows 98
			Windows NT
			Windows Vista
			Windows XP
		 */
		osData.put("FreeBSD", 	OS.Linux);
		osData.put("HP UX", 	OS.Linux);
		osData.put("Linux", 	OS.Linux);
		osData.put("Mac OS", 	OS.MacOS);
		osData.put("Mac OS X", 	OS.MacOS);
		osData.put("Windows", 	OS.XP);
		osData.put("Windows 7", OS.Vista);
		osData.put("Windows XP", OS.XP);
		osData.put("Windows 2003", OS.XP);
		osData.put("Windows 2000", OS.XP);
		osData.put("Windows Vista", OS.Vista);
		
		if(!osData.containsKey(os)) {
			MinecraftEnvironment.os = OS.NotSupported;
		} else {
			MinecraftEnvironment.os = osData.get(os);
		}
		
		switch(MinecraftEnvironment.os) {
			case Vista:
			case XP:
				String basedir = System.getenv("APPDATA");
				if (basedir == null)
				{
					basedir = System.getProperty("user.home");
				}
				MinecraftEnvironment.baseDir = new File(basedir, ".minecraft");
				MinecraftEnvironment.xrayBaseDir = new File(basedir, ".minecraft_xray");
				break;
			case Linux:
				MinecraftEnvironment.baseDir = new File(System.getProperty("user.home"), ".minecraft");
				MinecraftEnvironment.xrayBaseDir = new File(System.getProperty("user.home"), ".minecraft_xray");
				break;
			case MacOS:
				// damn macs ;p
				File dotMinecraftEnv = new File(System.getProperty("user.home"), "Library/Application Support/.minecraft");
				if(dotMinecraftEnv.exists()) {
					MinecraftEnvironment.baseDir = dotMinecraftEnv;
					MinecraftEnvironment.xrayBaseDir = new File(System.getProperty("user.home"), "Library/Application Support/.minecraft_xray");
				} else {
					MinecraftEnvironment.baseDir = new File(System.getProperty("user.home"), "Library/Application Support/minecraft"); // untested
					MinecraftEnvironment.xrayBaseDir = new File(System.getProperty("user.home"), "Library/Application Support/minecraft_xray"); // untested
				}
				break;
			default:
				MinecraftEnvironment.baseDir = null;
				MinecraftEnvironment.xrayBaseDir = null;
		}
		System.out.println(MinecraftEnvironment.baseDir.getAbsolutePath());
	}
	
	/***
	 * Returns a list of WorldInfo objects, corresponding to available worlds
	 * @return
	 */
	public static ArrayList<WorldInfo> getAvailableWorlds() {
		ArrayList<WorldInfo> worlds = new ArrayList<WorldInfo>();
		File saveDir = new File(baseDir, "saves");
		File[] worldDirs = saveDir.listFiles(new MCDirectoryFilter());
		try
		{
			Arrays.sort(worldDirs, new CaseInsensitiveComparator());
			for (File worldDir : worldDirs)
			{
				try
				{
					// First snatch up the overworld
					WorldInfo info = new WorldInfo(worldDir.getCanonicalPath(), worldDir.getName(), 0, false);
					worlds.add(info);
					
					// Now see if there's any associated dimensions we can add.
					ArrayList<WorldInfo> diminfo = info.getDimensionInfo();
					for (WorldInfo dim : diminfo)
					{
						if (dim != null)
						{
							worlds.add(dim);
						}
					}
				}
				catch (Exception e)
				{
					System.out.println(e.toString());
					// Nothing; guess we'll ignore it.
				}
			}
		}
		catch (NullPointerException e)
		{
			// This happens on the Arrays.sort() call if there's no "saves" dir at all.
		}
		return worlds;
	}
	
	/***
	 * Returns a file handle to a chunk file in a world.  Will attempt to load
	 * from region data first, if it's present, and then from the old-style
	 * chunk-per-file format if the world's not from Beta 1.3 or later.  It
	 * turns out that there isn't really any circumstance where there would be
	 * a mix, so we could be more strict about it, but whatever.
	 *
	 * @param world
	 * @param x
	 * @param z
	 * @return
	 */
	public static DataInputStream getChunkInputStream(WorldInfo world, int x, int z) {
		if (world.has_region_data)
		{
			RegionFile rf = RegionFileCache.getRegionFile(new File(world.getBasePath()), x, z);
			if (rf != null)
			{
				DataInputStream chunk = rf.getChunkDataInputStream(x & 31, z & 31);
				if (chunk != null)
				{
					return chunk;
				}
			}
		}
		if (!world.is_beta_1_3_level)
		{
			int xx = x % 64;
			if(xx<0) xx = 64+xx;
			int zz = z % 64;
			if(zz<0) zz = 64+zz;
			String firstFolder 		= Integer.toString(xx, 36);
			String secondFolder 	= Integer.toString(zz, 36);
			String filename 		= "c." + Integer.toString(x, 36) + "." + Integer.toString(z, 36) + ".dat";
			File chunk = new File(world.getBasePath(), firstFolder + "/" + secondFolder + "/" + filename);
			if (chunk.exists())
			{
				//  There's some code duplication here from DTFReader.readDTFFile()
				try {
					return new DataInputStream(new GZIPInputStream(new FileInputStream(chunk)));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	/***
	 * Returns a file handle to the base minecraft directory
	 * @return
	 */
	public static File getMinecraftDirectory() {
		return MinecraftEnvironment.baseDir;
	}
	
	/**
	 * Returns a file handle to our own data directory
	 * @return
	 */
	public static File getXrayDirectory() {
		return MinecraftEnvironment.xrayBaseDir;
	}
	
	/**
	 * Returns a file handle to our config file; will create the
	 * directory if needed.
	 * @return
	 */
	public static File getXrayConfigFile() {
		if (MinecraftEnvironment.xrayBaseDir.exists())
		{
			if (!MinecraftEnvironment.xrayBaseDir.isDirectory())
			{
				return null;
			}
		}
		else
		{
			if (!MinecraftEnvironment.xrayBaseDir.mkdir())
			{
				return null;
			}
		}
		return new File(MinecraftEnvironment.xrayBaseDir, "xray.properties");
	}
	
	/***
	 * Returns a stream to an arbitrary file either from our override directory,
	 * the main jar, or from the user-specified texture pack.
	 * 
	 * @return
	 */
	public static InputStream getMinecraftTexturepackData(String filename) {

		// First check our override directory for the file
		File overrideFile = new File(xrayBaseDir, "textures/" + filename);
		if(overrideFile.exists()) {
			try {
				return new FileInputStream(overrideFile);
			} catch (FileNotFoundException e) {
				// Don't do anything; just continue on our merry little way
			}
		}
		
		// Next check the options.txt file to see if we should be using the defined
		// texture pack.
		File optionsFile = new File(baseDir, "options.txt");
		String texturepack = null;
		if (optionsFile.exists())
		{
			LineNumberReader reader = null;
			try
			{
				reader = new LineNumberReader(new FileReader(optionsFile));
				String line = null;
				String[] parts;
				while ((line = reader.readLine()) != null)
				{
					parts = line.split(":", 2);
					if (parts.length == 2)
					{
						if (parts[0].equalsIgnoreCase("skin"))
						{
							if (parts[1].equalsIgnoreCase("Default"))
							{
								// Default skin, just break and 
								break;
							}
							else
							{
								// Use the specified texture pack
								texturepack = parts[1];
								break;
							}
						}
					}
				}
			}
			catch (FileNotFoundException e)
			{
				// Just ignore it and load the default terrain.png
			}
			catch (IOException e)
			{
				// Ditto, just ignore
			}
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					// do nothing
				}
			}
		}
		
		// Attempt to load in the texture pack
		if (texturepack != null)
		{
			File packFile = new File(baseDir, "texturepacks/" + texturepack);
			if (packFile.exists())
			{
				ZipFile zf = null;
				try
				{
					zf = new ZipFile(packFile);
					ZipEntry entry = zf.getEntry(filename);
					if (entry != null)
					{
						return zf.getInputStream(entry);
					}
				}
				catch (ZipException e)
				{
					// Do nothing.
				}
				catch (IOException e)
				{
					// Do nothing
				}
				if (zf != null)
				{
					try
					{
						zf.close();
					}
					catch (IOException e)
					{
						// do nothing
					}
				}
			}
		}
		
		// If we got here, just do what we've always done.
		return getMinecraftFile(filename);
	}
	
	/***
	 * Returns a stream to the texture data (overrides in the directory are handled)
	 * @return
	 */
	public static InputStream getMinecraftTextureData() {
		return getMinecraftTexturepackData("terrain.png");
	}
	
	/***
	 * Returns a stream to the water texture data
	 * @return
	 */
	public static InputStream getMinecraftWaterData() {
		return getMinecraftTexturepackData("misc/water.png");
	}
	
	/***
	 * Returns a stream to the water texture data
	 * @return
	 */
	public static InputStream getMinecraftParticleData() {
		return getMinecraftTexturepackData("particles.png");
	}
	
	/**
	 * Returns a stream to the painting data
	 * @return
	 */
	public static InputStream getMinecraftPaintingData() {
		return getMinecraftTexturepackData("art/kz.png");
	}
	
	/***
	 * Attempts to create a bufferedImage for a stream
	 * @param i
	 * @return
	 */
	private static BufferedImage buildImageFromInput(InputStream i) {
		if (i == null)
		{
			return null;
		}
		try {
			return ImageIO.read(i);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Attempts to create a bufferedImage containing our painting sheet
	 * @return
	 */
	public static BufferedImage getMinecraftPaintings() {
		return buildImageFromInput(getMinecraftPaintingData());
	}
	
	/***
	 * Attempts to create a bufferedImage containing the texture sprite sheet.  This does
	 * some munging to make things a little more useful for us.  Namely:
	 * 
	 *   1) It will attempt to colorize any biome-ready skin by first checking for green
	 *      pixels in the texture, and then doing some blending if it looks greyscale.
	 *   2) It will colorize the redstone wire texture appropriately (due to the Beta 1.3
	 *      change where redstone wire color depends on how far from the source it is)
	 *   3) We also copy in the water texture from misc/water.png, because many third-party
	 *      skins don't actually have a water graphic in the same place as the default skin
	 *   4) Then we attempt to construct a passable "fire" texture from the particles file.
	 *   5) Then we create a "blank" texture, for use with unknown block types
	 *   6) Then we create a nether (portal) texture
	 *   7) Lastly, we duplicate the texture with a green tint, immediately below the
	 *      main texture group.  We do this to support our "explored" highlighting - the
	 *      tinting can be done easily via OpenGL itself, but there were pretty severe
	 *      performance issues when I tried that on my laptop.  If we just modify the texture
	 *      and use offsets instead, there's no FPS drop on there.  This DOES have the
	 *      unfortunate downside that, when specifying texture coordinates with glTexCoord2f(),
	 *      we can no longer think of the textures as perfectly "square."  The Y offsets must
	 *      be half of what we're used to.  Perhaps it would make sense to double the X axis
	 *      here as well, so that we could avoid some confusion; for now I'll leave it though.
	 *
	 * TODO: it would be good to use the data values loaded from YAML to figure out where
	 * to colorize, rather than hardcoding them here.
	 *
	 * @return
	 */
	public static BufferedImage getMinecraftTexture()
		throws BlockTypeLoadException
	{
		BufferedImage bi = buildImageFromInput(getMinecraftTextureData());
		Graphics2D g2d = bi.createGraphics();
		
		// Figure out our square size, and then check to see if the grass tile is
		// grayscale or not; we do this by examining the middle row of pixels.  If
		// it *is* grayscale, then colorize it. 
		int square_width = bi.getWidth()/16;
		int[] pixels = new int[square_width];
		int i;
		int r, g, b;
		boolean grayscale = true;
		bi.getRGB(0, square_width/2, square_width, 1, pixels, 0, square_width);
		for (i=0; i<square_width; i++)
		{
			//a = (pixels[i] & 0xFF000000) >> 24;
			r = (pixels[i] & 0x00FF0000) >> 16;
			g = (pixels[i] & 0x0000FF00) >> 8;
			b = (pixels[i] & 0x000000FF);
			//System.out.println("Pixel " + i + ": " + r + ", " + g + ", " + b + ", " + a);
			if (g > r+10 || g > b+10)
			{
				grayscale = false;
				break;
			}
		}
		
		// Now do the coloring if we have to.
		if (grayscale)
		{	
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.4f);
			
			// First greenify grass
			Rectangle rect = new Rectangle(0, 0, square_width, square_width);
			g2d.setComposite(ac);
			g2d.setColor(Color.green);
			g2d.fill(rect);
			g2d.drawImage(bi, null, 0, 0);
			
			// Now greenify leaves
			rect = new Rectangle(4*square_width, 3*square_width, 2*square_width, square_width);
			g2d.setComposite(ac);
			g2d.setColor(Color.green);
			g2d.fill(rect);
			g2d.drawImage(bi, null, 0, 0);

			// Now tall grass
			rect = new Rectangle(7*square_width, 2*square_width, square_width, square_width);
			g2d.setComposite(ac);
			g2d.setColor(Color.green);
			g2d.fill(rect);
			g2d.drawImage(bi, null, 0, 0);

			// Now ferns
			rect = new Rectangle(8*square_width, 3*square_width, square_width, square_width);
			g2d.setComposite(ac);
			g2d.setColor(Color.green);
			g2d.fill(rect);
			g2d.drawImage(bi, null, 0, 0);

			// Now the side-grass overlay
			rect = new Rectangle(6*square_width, 2*square_width, square_width, square_width);
			g2d.setComposite(ac);
			g2d.setColor(Color.green);
			g2d.fill(rect);
			g2d.drawImage(bi, null, 0, 0);
		}

		// Overlay our custom-colorized side grass on top of the side-grass image
		g2d.setComposite(AlphaComposite.SrcOver);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);			
		g2d.drawImage(bi, 3*square_width, 0, 4*square_width, square_width,
				6*square_width, 2*square_width, 7*square_width, 3*square_width,
				null);

		// Colorize redstone wire
		AlphaComposite redstone_ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f);
		Rectangle redstone_rect = new Rectangle(4*square_width, 10*square_width, square_width, square_width);
		g2d.setComposite(redstone_ac);
		g2d.setColor(Color.red);
		g2d.fill(redstone_rect);
		g2d.drawImage(bi, null, 0, 0);

		// Load in the water texture separately and pretend it's a part of the main texture pack.
		BLOCK_WATER.setTexIdx(blockCollection.reserveTexture());
		int[] water_tex = BLOCK_WATER.getTexCoordsArr();
		BLOCK_STATIONARY_WATER.setTexIdxCoords(water_tex[0], water_tex[1]);
		BufferedImage bi2 = buildImageFromInput(getMinecraftWaterData());
		if (bi2 == null)
		{
			// If we don't have a water texture (some mods seem to get rid of it), construct our own
			bi2 = new BufferedImage(square_width, square_width, BufferedImage.TYPE_INT_ARGB);
			Graphics2D bi2g2d = bi2.createGraphics();
			bi2g2d.setComposite(AlphaComposite.Src);
			bi2g2d.setColor(new Color(.14f, .36f, 1f, .52f));
			bi2g2d.fillRect(0, 0, square_width, square_width);

			// A bit of detail
			bi2g2d.setColor(new Color(.23f, .42f, 1f, .57f));
			bi2g2d.drawLine((int)(square_width*.1), (int)(square_width*.2), (int)(square_width*.8), (int)(square_width*.3));
			bi2g2d.drawLine((int)(square_width*.2), (int)(square_width*.5), (int)(square_width*.7), (int)(square_width*.54));
			bi2g2d.drawLine((int)(square_width*.05), (int)(square_width*.9), (int)(square_width*.6), (int)(square_width*.9));
			bi2g2d.drawLine((int)(square_width*.5), (int)(square_width*.7), (int)(square_width*.9), (int)(square_width*.6));
		}
		int water_width = bi2.getWidth();
		g2d.setComposite(AlphaComposite.Src);
		if (square_width < water_width)
		{
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		}
		else
		{
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);			
		}
		g2d.drawImage(bi2, water_tex[0]*square_width, water_tex[1]*square_width, square_width, square_width, null);

		// Also create a fake sort of "fire" graphic to use
		BLOCK_FIRE.setTexIdx(blockCollection.reserveTexture());
		int[] fire_tex = BLOCK_FIRE.getTexCoordsArr();
		bi2 = buildImageFromInput(getMinecraftParticleData());
		int particle_width = bi2.getWidth()/16;
		int fire_x = fire_tex[0];
		int fire_y = fire_tex[1];
		int flame_x = 0;
		int flame_y = 3;
		int start_fire_x = fire_x*square_width;
		int start_fire_y = fire_y*square_width;
		int start_flame_x = flame_x*particle_width;
		int start_flame_y = flame_y*particle_width;
		g2d.setComposite(AlphaComposite.Src);
		g2d.setColor(new Color(0f, 0f, 0f, 0f));
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);			
		g2d.fillRect(fire_x*square_width, fire_y*square_width, square_width, square_width);
		if (square_width < (particle_width*2))
		{
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);			
		}
		g2d.drawImage(bi2,
				start_fire_x, start_fire_y, start_fire_x+(square_width/2), start_fire_y+(square_width/2),
				start_flame_x, start_flame_y, start_flame_x+particle_width, start_flame_y+particle_width,
				null);
		g2d.drawImage(bi2,
				start_fire_x+(square_width/2), start_fire_y, start_fire_x+square_width, start_fire_y+(square_width/2),
				start_flame_x, start_flame_y, start_flame_x+particle_width, start_flame_y+particle_width,
				null);
		g2d.drawImage(bi2,
				start_fire_x, start_fire_y+(square_width/2), start_fire_x+(square_width/2), start_fire_y+square_width,
				start_flame_x, start_flame_y, start_flame_x+particle_width, start_flame_y+particle_width,
				null);
		g2d.drawImage(bi2,
				start_fire_x+(square_width/2), start_fire_y+(square_width/2), start_fire_x+square_width, start_fire_y+square_width,
				start_flame_x, start_flame_y, start_flame_x+particle_width, start_flame_y+particle_width,
				null);		

		// Create an "empty block" graphic.  We could just leave it, probably, but
		// some texture packs get creative with the empty space.
		// First the fill
		BLOCK_UNKNOWN.setTexIdx(blockCollection.reserveTexture());
		int[] unknown_tex = BLOCK_UNKNOWN.getTexCoordsArr();
		int empty_start_x = square_width*unknown_tex[0];
		int empty_start_y = square_width*unknown_tex[1];
		g2d.setColor(new Color(214,127,255));
		g2d.fillRect(empty_start_x, empty_start_y, square_width-1, square_width-1);
		// Then the border
		g2d.setColor(new Color(107,63,127));
		g2d.drawRect(empty_start_x, empty_start_y, square_width-1, square_width-1);

		// Create a nether portal texture
		BLOCK_PORTAL.setTexIdx(blockCollection.reserveTexture());
		int[] portal_tex = BLOCK_PORTAL.getTexCoordsArr();
		int nether_start_x = square_width*portal_tex[0];
		int nether_start_y = square_width*portal_tex[1];
		g2d.setColor(new Color(.839f, .203f, .952f, .4f));
		g2d.fillRect(nether_start_x, nether_start_y, square_width, square_width);

		// Load in filename textures, if needed
		// TODO: Exception error reporting
		for (Map.Entry<String, Integer> entry : blockCollection.getFilenameTextureBlocks().entrySet())
		{
			int[] new_tex = BlockType.getTexCoordsArr(entry.getValue());
			bi2 = buildImageFromInput(getMinecraftTexturepackData(entry.getKey()));
			if (bi2 == null)
			{
				throw new BlockTypeLoadException("File " + entry.getKey() + " is not found");
			}
			int new_width = bi2.getWidth();
			g2d.setComposite(AlphaComposite.Src);
			if (square_width < new_width)
			{
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			}
			else
			{
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);			
			}
			g2d.drawImage(bi2, new_tex[0]*square_width, new_tex[1]*square_width, square_width, square_width, null);
		}
		
		// Duplicate the texture underneath, tinted for our "explored" areas
		bi2 = new BufferedImage(bi.getWidth(), bi.getHeight()*2, BufferedImage.TYPE_INT_ARGB);
		g2d = bi2.createGraphics();
		g2d.setComposite(AlphaComposite.Src);
		g2d.drawImage(bi, 0, 0, bi.getWidth(), bi.getHeight(), null);
		g2d.drawImage(bi, 0, bi.getHeight(), bi.getWidth(), bi.getHeight(), null);
		g2d.setComposite(AlphaComposite.SrcAtop);
		g2d.setColor(new Color(0f, 1f, 0f, .2f));
		g2d.fillRect(0, bi.getHeight(), bi.getWidth(), bi.getHeight());

		/*
		try {
			ImageIO.write(bi, "PNG", new File(System.getProperty("user.home"), "xray_terrain.png"));
			System.out.println("Wrote texture to ~/xray_terrain.png");
		}
		catch (Exception e)
		{
			// whatever
		}
		*/
		
		return bi2;
	}
	
	/***
	 * Creates an Inputstream to a file in side the main minecraft.jar file.
	 * @param fileName
	 * @return
	 */
	public static InputStream getMinecraftFile(String fileName){
		File minecraftDataFile = new File(baseDir, "bin/minecraft.jar");
		if(!minecraftDataFile.exists()) {
			return null;
		}

		if (minecraftDataFile.isDirectory())
		{
			// Some mods (TooManyItems in particular) on some OSes (OSX in particular)
			// end up replacing minecraft.jar with an unpacked directory containing
			// its contents.  We may as well check for that.
			File dataFile = new File(minecraftDataFile, fileName);
			if (!dataFile.exists())
			{
				return null;
			}
			try
			{
				return new FileInputStream(dataFile);
			}
			catch (FileNotFoundException e)
			{
				return null;
			}
		}
		else
		{
			try {
				JarFile jf = new JarFile(minecraftDataFile);
				ZipEntry zipEntry = jf.getEntry(fileName);
				if(zipEntry == null) {
					return null;
				}
				return jf.getInputStream(zipEntry);
			} catch (IOException e) {
				System.out.println(e.toString());
				return null;
			}
		}
	}
}
