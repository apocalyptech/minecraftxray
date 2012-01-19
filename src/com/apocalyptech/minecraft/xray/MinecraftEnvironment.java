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
import java.io.FilenameFilter;
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
	public static enum OS {Windows, MacOS, Linux};
	public static OS os; 
	public static File baseDir;
	public static File xrayBaseDir;
	public static HashMap<Byte, Integer> silverfishDataPlain;
	public static HashMap<Byte, Integer> silverfishDataHighlighted;
	
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

	private static class BlockdefFilter implements FilenameFilter
	{
		public BlockdefFilter() {
			// Nothing, really
		}

		public boolean accept(File pathname, String filename) {
			File tempFile = new File(pathname, filename);
			return (tempFile.isFile() &&
					tempFile.canRead() &&
					!filename.equalsIgnoreCase("minecraft.yaml") &&
					filename.endsWith(".yaml"));
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
		if (os.startsWith("Mac"))
		{
			MinecraftEnvironment.os = OS.MacOS;
		}
		else if (os.startsWith("Windows"))
		{
			MinecraftEnvironment.os = OS.Windows;
		}
		else
		{
			MinecraftEnvironment.os = OS.Linux;
		}
		
		switch(MinecraftEnvironment.os) {
			case Windows:
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
				// TODO: we should really be using "minecraft_xray" without a dot; perhaps have something to convert that.
				// Apparently the minecraft directory itself doesn't use a dot here, so we're being weird by doing it
				// ourselves.
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
		//XRay.logger.debug(MinecraftEnvironment.baseDir.getAbsolutePath());
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
					XRay.logger.warn(e.toString());
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

	/**
	 * Passthrough to stuff
	 */
	public static IntegerPair getClosestRegion(WorldInfo world, int chunkX, int chunkZ)
	{
		return RegionFileCache.getClosestRegion(world.getBasePath(), chunkX, chunkZ);
	}
	
	/***
	 * Returns a file handle to a chunk file in a world.  Will attempt to load
	 * from region data first, if it's present, and then from the old-style
	 * chunk-per-file format if the world's not from Beta 1.3 or later.  It
	 * turns out that there isn't really any circumstance where there would be
	 * a mix, so we could be more strict about it, but whatever.
	 *
	 * @param world
	 * @param chunkX The Chunk X coordinate
	 * @param chunkZ The Chunk Z coordinate
	 * @return
	 */
	public static DataInputStream getChunkInputStream(WorldInfo world, int chunkX, int chunkZ) {
		if (world.has_region_data)
		{
			RegionFile rf = RegionFileCache.getRegionFile(new File(world.getBasePath()), chunkX, chunkZ);
			if (rf != null)
			{
				DataInputStream chunk = rf.getChunkDataInputStream(chunkX & 31, chunkZ & 31);
				if (chunk != null)
				{
					return chunk;
				}
			}
		}
		if (!world.is_beta_1_3_level)
		{
			int xx = chunkX % 64;
			if(xx<0) xx = 64+xx;
			int zz = chunkZ % 64;
			if(zz<0) zz = 64+zz;
			String firstFolder 		= Integer.toString(xx, 36);
			String secondFolder 	= Integer.toString(zz, 36);
			String filename 		= "c." + Integer.toString(chunkX, 36) + "." + Integer.toString(chunkZ, 36) + ".dat";
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
	 * directory if needed.  This will also create some other
	 * subdirectories if needed.
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
		File texDir = new File(MinecraftEnvironment.xrayBaseDir, "textures");
		if (!texDir.exists())
		{
			texDir.mkdir();
		}
		File blockdefDir = new File(MinecraftEnvironment.xrayBaseDir, "blockdefs");
		if (!blockdefDir.exists())
		{
			blockdefDir.mkdir();
		}
		return new File(MinecraftEnvironment.xrayBaseDir, "xray.properties");
	}

	/**
	 * Get a list of BlockTypeCollections from the given directory
	 */
	public static ArrayList<BlockTypeCollection> getBlockTypeCollectionFilesFromDir(File dir, boolean global)
	{
		ArrayList<BlockTypeCollection> list = new ArrayList<BlockTypeCollection>();
		BlockTypeCollection tempcollection;
		String fullFilename;
		if (dir.exists() && dir.isDirectory() && dir.canRead())
		{
			for (String filename : dir.list(new BlockdefFilter()))
			{
				fullFilename = dir.getPath() + "/" + filename;
				try
				{
					tempcollection = MinecraftConstants.loadBlocks(fullFilename, global);
				}
				catch (BlockTypeLoadException e)
				{
					tempcollection = new BlockTypeCollection(new File(fullFilename), global, e);
				}
				list.add(tempcollection);
			}
		}
		return list;
	}

	/**
	 * Returns a list of available BlockType files, both from our global directory,
	 * and the user xrayBaseDir
	 */
	public static ArrayList<BlockTypeCollection> getBlockTypeCollectionFiles()
	{
		ArrayList<BlockTypeCollection> list = new ArrayList<BlockTypeCollection>();
		list.addAll(getBlockTypeCollectionFilesFromDir(new File("blockdefs"), true));
		list.addAll(getBlockTypeCollectionFilesFromDir(new File(xrayBaseDir, "blockdefs"), false));
		return list;
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
				XRay.logger.info("Overriding " + filename + " at " + overrideFile.getPath());
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
						XRay.logger.info("Using " + filename + " from texturepack " + texturepack);
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
	public static BufferedImage buildImageFromInput(InputStream i) {
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

	/**
	 * Tints a square at the given coords by the given color, into the specified BufferedImage
	 * @param coords The coordinates to read from and draw to
	 * @param square_width width of each square
	 * @param ac an AlphaComposite to blend with
	 * @param color The color to tint
	 * @param bi The image we'll be drawing to and reading from
	 * @param g2d That image's Graphics2D object
	 */
	private static void tintSquare(int[] coords, int square_width, AlphaComposite ac, Color color, BufferedImage bi, Graphics2D g2d)
	{
		Rectangle rect = new Rectangle(coords[0]*square_width, coords[1]*square_width, square_width, square_width);
		g2d.setComposite(ac);
		g2d.setColor(color);
		g2d.fill(rect);
		g2d.drawImage(bi, null, 0, 0);
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
	 *   6) Then we create a nether (portal) texture and an air portal texture
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
	public static ArrayList<BufferedImage> getMinecraftTexture()
		throws BlockTypeLoadException
	{
		BufferedImage bi = buildImageFromInput(getMinecraftTextureData());
		if (bi == null)
		{
			return null;
		}
		Graphics2D g2d = bi.createGraphics();
		
		// Figure out our square size, and then check to see if the grass tile is
		// grayscale or not; we do this by examining the middle row of pixels.  If
		// it *is* grayscale, then colorize it. 
		// It does seem a little stupid, at this point, to be bothering with this,
		// but in the end I think I'd like this to continue working properly on older
		// versions of Minecraft.
		int square_width = bi.getWidth()/16;
		int[] pixels = new int[square_width];
		int i;
		int r, g, b;
		boolean grayscale = true;
		int[] tex_coords = BLOCK_GRASS.getTexCoordsArr();
		bi.getRGB(tex_coords[0]*square_width, (tex_coords[1]*square_width)+(square_width/2), square_width, 1, pixels, 0, square_width);
		for (i=0; i<square_width; i++)
		{
			//a = (pixels[i] & 0xFF000000) >> 24;
			r = (pixels[i] & 0x00FF0000) >> 16;
			g = (pixels[i] & 0x0000FF00) >> 8;
			b = (pixels[i] & 0x000000FF);
			//XRay.logger.debug("Pixel " + i + ": " + r + ", " + g + ", " + b + ", " + a);
			if (g > r+50 || g > b+50)
			{
				grayscale = false;
				break;
			}
		}
		
		// Now do our colorizing.  We'll do it in two parts because technically we're doing
		// a check for whether or not grass is grayscale (if grass isn't grayscale, leaves
		// won't be either) (though technically if that's the case, our map won't contain
		// any of the other blocks we're colorizing, either, so it's a moot point)
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.4f);
		BlockType block;
		Rectangle rect;
		if (grayscale)
		{	
			for (String blockname : new String[] { "GRASS", "LEAVES" })
			{
				block = blockCollection.getByName(blockname);
				if (block != null)
				{
					tex_coords = block.getTexCoordsArr();
					tintSquare(block.getTexCoordsArr(), square_width, ac, Color.green, bi, g2d);
				}
			}

			// Our one hardcoded-for-now colorization: the side-grass overlay
			int[] sideGrassCoordsOverlay = new int[] {6, 2};
			tintSquare(sideGrassCoordsOverlay, square_width, ac, Color.green, bi, g2d);

			// Overlay our custom-colorized side grass on top of the side-grass image
			int[] sideGrassCoords = BlockType.getTexCoordsArr(BLOCK_GRASS.texture_dir_map.get(BlockType.DIRECTION_REL.SIDES));
			g2d.setComposite(AlphaComposite.SrcOver);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);			
			g2d.drawImage(bi,
					sideGrassCoords[0]*square_width, sideGrassCoords[1]*square_width,
					(sideGrassCoords[0]+1)*square_width, (sideGrassCoords[1]+1)*square_width,
					sideGrassCoordsOverlay[0]*square_width, sideGrassCoordsOverlay[1]*square_width,
					(sideGrassCoordsOverlay[0]+1)*square_width, (sideGrassCoordsOverlay[1]+1)*square_width,
					null);
		}

		// Some blocks to tint unconditionally
		for (String blockname : new String[] { "VINE", "PUMPKIN_STEM", "LILY_PAD" })
		{
			block = blockCollection.getByName(blockname);
			if (block != null)
			{
				tex_coords = block.getTexCoordsArr();
				tintSquare(block.getTexCoordsArr(), square_width, ac, Color.green.darker(), bi, g2d);
			}
		}

		// PUMPKIN_STEM has an additional texture as well
		// TODO: Technically we should maybe do MELON_STEM (both here and above) separately,
		// but we'd need to keep track of which textures we've tinted, so as not to tint
		// something twice.  Right now these both use the same texture (though technically
		// we should copy the texture and tint it twice, differently for each) so I'm just
		// doing the one.
		block = blockCollection.getByName("PUMPKIN_STEM");
		if (block != null)
		{
			if (block.texture_extra_map != null)
			{
				tex_coords = BlockType.getTexCoordsArr(block.texture_extra_map.get("curve"));
				tintSquare(tex_coords, square_width, ac, Color.green.darker(), bi, g2d);
			}
		}

		// Two data values of Tall Grass should get colorized
		block = blockCollection.getByName("TALL_GRASS");
		if (block != null)
		{
			if (block.texture_data_map != null)
			{
				for (byte data : new byte[] { 1, 2 })
				{
					if (block.texture_data_map.containsKey(data))
					{
						tintSquare(BlockType.getTexCoordsArr(block.texture_data_map.get(data)),
								square_width, ac, Color.green.darker(), bi, g2d);
					}
				}
			}
		}

		// One extra data value of Leaves should get colorized
		block = blockCollection.getByName("LEAVES");
		if (block != null)
		{
			if (block.texture_data_map != null)
			{
				for (byte data : new byte[] { 3 })
				{
					if (block.texture_data_map.containsKey(data))
					{
						tintSquare(BlockType.getTexCoordsArr(block.texture_data_map.get(data)),
								square_width, ac, Color.green, bi, g2d);
					}
				}
			}
		}

		// Colorize redstone wire
		block = blockCollection.getByName("REDSTONE_WIRE");
		if (block != null)
		{
			AlphaComposite redstone_ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f);
			tintSquare(block.getTexCoordsArr(), square_width, redstone_ac, Color.red, bi, g2d);
		}

		// Generate separate textures for each of the Silverfish textures, and colorize them
		// I feel a little guilty for occupying three whole new textures for this, especially given the
		// recent addition of mod support.  More and more likely that folks will break into the
		// two-texture range (though at least we do support it now).  I may want to look into just
		// doing some GL trickery to tint them at render-time instead.  If I ever get around to doing
		// some simple shading on the blocks, that'd probably be a good opportunity to do so.
		// TODO: that ^
		block = BLOCK_SILVERFISH;
		silverfishDataPlain = new HashMap<Byte, Integer>();
		silverfishDataHighlighted = new HashMap<Byte, Integer>();
		if (block.texture_data_map != null)
		{
			for (Map.Entry<Byte, Integer> entry : block.texture_data_map.entrySet())
			{
				int new_tex = blockCollection.reserveTexture();
				int[] old_coords = BlockType.getTexCoordsArr(entry.getValue());
				int[] new_coords = BlockType.getTexCoordsArr(new_tex);
				g2d.setComposite(AlphaComposite.Src);
				g2d.drawImage(bi,
						new_coords[0]*square_width, new_coords[1]*square_width,
						(new_coords[0]+1)*square_width, (new_coords[1]+1)*square_width,
						old_coords[0]*square_width, old_coords[1]*square_width,
						(old_coords[0]+1)*square_width, (old_coords[1]+1)*square_width,
						null);
				tintSquare(new_coords, square_width, ac, Color.red, bi, g2d);
				silverfishDataPlain.put(entry.getKey(), entry.getValue());
				silverfishDataHighlighted.put(entry.getKey(), new_tex);
			}
		}

		// Load in the water texture separately and pretend it's a part of the main texture pack.
		BLOCK_WATER.setTexIdx(blockCollection.reserveTexture());
		int[] water_tex = BLOCK_WATER.getTexCoordsArr();
		BLOCK_STATIONARY_WATER.setTexIdxCoords(water_tex[0], water_tex[1]);
		BufferedImage bi2 = buildImageFromInput(getMinecraftWaterData());
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

		// Create an end portal texture
		BLOCK_END_PORTAL.setTexIdx(blockCollection.reserveTexture());
		int[] end_tex = BLOCK_END_PORTAL.getTexCoordsArr();
		int end_start_x = square_width*end_tex[0];
		int end_start_y = square_width*end_tex[1];
		g2d.setColor(new Color(0f, 0f, 0f));
		g2d.fillRect(end_start_x, end_start_y, square_width, square_width);

		// Send our processed texture 
		blockCollection.setInitialTexture(bi);

		// Load in extra texture sheets
		blockCollection.importCustomTextureSheets();

		// Load in filename textures, if needed
		blockCollection.loadFilenameTextures();

		// Report on the count of texture sheets
		XRay.logger.debug("Texture Sheet count: " + blockCollection.textures.size());
		
		// For each texture we now have, copy the texture underneath, tinted for our "explored" areas
		i = 0;
		ArrayList<BufferedImage> explored = new ArrayList<BufferedImage>();
		for (BufferedImage image : blockCollection.textures)
		{
			bi2 = new BufferedImage(image.getWidth(), image.getHeight()*2, BufferedImage.TYPE_INT_ARGB);
			g2d = bi2.createGraphics();
			g2d.setComposite(AlphaComposite.Src);
			g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
			g2d.drawImage(image, 0, image.getHeight(), image.getWidth(), image.getHeight(), null);
			g2d.setComposite(AlphaComposite.SrcAtop);
			g2d.setColor(new Color(0f, 1f, 0f, .2f));
			g2d.fillRect(0, image.getHeight(), image.getWidth(), image.getHeight());

			explored.add(bi2);

			/*
			try {
				ImageIO.write(image, "PNG", new File(System.getProperty("user.home"), "xray_terrain_" + i + ".png"));
				XRay.logger.info("Wrote texture to ~/xray_terrain_" + i + ".png");
			}
			catch (Exception e)
			{
				// whatever
			}
			*/

			i++;
		}

		// This bit here is extremely grody; we should just do this in-place or something
		blockCollection.textures = explored;

		// And while we're on that subject, let's populate our exploredBlocks HashMap
		exploredBlocks = new HashMap<Short, Boolean>();
		for (BlockType expblock : blockCollection.getBlocksFull())
		{
			if (expblock.getExplored())
			{
				exploredBlocks.put(expblock.id, true);
			}
		}
		
		// a bit unnecessary since that's a constant...
		return blockCollection.textures;
	}
	
	/***
	 * Creates an Inputstream to a file in side the main minecraft.jar file, or failing
	 * that, to our bundled fallbacks.
	 *
	 * @param fileName
	 * @return
	 */
	public static InputStream getMinecraftFile(String fileName){
		File minecraftDataFile = new File(baseDir, "bin/minecraft.jar");
		if(minecraftDataFile.exists()) {
			if (minecraftDataFile.isDirectory())
			{
				// Some mods (TooManyItems in particular) on some OSes (OSX in particular)
				// end up replacing minecraft.jar with an unpacked directory containing
				// its contents.  We may as well check for that.
				File dataFile = new File(minecraftDataFile, fileName);
				if (dataFile.exists())
				{
					try
					{
						return new FileInputStream(dataFile);
					}
					catch (FileNotFoundException e)
					{
					}
				}
			}
			else
			{
				try {
					JarFile jf = new JarFile(minecraftDataFile);
					ZipEntry zipEntry = jf.getEntry(fileName);
					if(zipEntry != null) {
						return jf.getInputStream(zipEntry);
					}
				} catch (IOException e) {
					XRay.logger.warn(e.toString());
					//return null;
				}
			}
		}

		// If we get here, either we couldn't find minecraft.jar or there was
		// something wrong with it (perhaps altered by a mod, or whatever).  In
		// the absence of anything else to do, we'll just load a bundled version
		// that comes with X-Ray.  If that doesn't work, we'll just sit down
		// and have a cry.  Note that this bit is sort of implicitly assuming that
		// any file we might have bundled is a texture, hence the directory name.
		if (fileName.equals("terrain.png") || fileName.equals("particles.png") ||
				fileName.equals("art/kz.png") || fileName.equals("misc/water.png"))
		{
			XRay.logger.info("Resorting to bundled " + fileName);
			File dataFile = new File("textures", fileName);
			if (dataFile.exists())
			{
				try
				{
					return new FileInputStream(dataFile);
				}
				catch (FileNotFoundException e)
				{
				}
			}
		}

		// *sob*
		return null;
	}
}
