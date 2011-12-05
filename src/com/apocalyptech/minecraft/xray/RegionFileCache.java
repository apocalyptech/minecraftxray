/*
 ** 2011 January 5
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

/*
 * 2011 February 16
 * 
 * This source code is based on the work of Scaevolus (see notice above).
 * It has been slightly modified by Mojang AB to limit the maximum cache
 * size (relevant to extremely big worlds on Linux systems with limited
 * number of file handles). The region files are postfixed with ".mcr"
 * (Minecraft region file) instead of ".data" to differentiate from the
 * original McRegion files.
 * 
 */

/*
 * 2011 February 20
 * 
 * Imported by CJ Kucera into X-Ray, from a blog post by Jens Bergensten
 * at: http://mojang.com/2011/02/16/minecraft-save-file-format-in-beta-1-3/
 *
 * Plus further changes - just see git history to see 'em, if you care.  :)
 */

// A simple cache and wrapper for efficiently multiple RegionFiles simultaneously.
package com.apocalyptech.minecraft.xray;

import java.io.*;
import java.lang.Math;
import java.lang.Double;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RegionFileCache {

    private static final int MAX_CACHE_SIZE = 256;


    private static final Map<File, Reference<RegionFile>> cache = new HashMap<File, Reference<RegionFile>>();
	private static final HashMap<String, ArrayList<IntegerPair>> availableCache = new HashMap<String, ArrayList<IntegerPair>>();

    private RegionFileCache() {
    }

	/**
	 * Returns the integer chunk coordinates of the nearest chunk for which we have data,
	 * from (x, z).  
	 *
	 * @param basePath the base path we're looking at
	 * @param chunkX The current chunk X coordinate of the camera
	 * @param chunkZ The current chunk Z coordinate of the camera
	 * @return an IntegerPair describing the chunk coordinates
	 */
	public static synchronized IntegerPair getClosestRegion(String basePath, int chunkX, int chunkZ)
	{
		ArrayList<IntegerPair> available;
		if (availableCache.containsKey(basePath))
		{
			available = availableCache.get(basePath);
		}
		else
		{
			available = new ArrayList<IntegerPair>();
			availableCache.put(basePath, available);

			File base = new File(basePath, "region");
			File[] regions = base.listFiles(new RegionFileFilter());
			Pattern pattern = Pattern.compile(RegionFileFilter.match_regex);

			for (File region : regions)
			{
				// Note that there seem to be some circumstances where Minecraft will write out an empty
				// RegionFile here, so we're going to check on filesize as well.  Theoretically an
				// empty RegionFile would be 4096 bytes, but the ones that Minecraft writes out are actually
				// 8192 for some reason (entirely with null values).  Since it's pretty unlikely that we'd
				// have a region with only a single chunk (and equally unlikely that it would really make
				// that much of a difference even if there were), we're going to discard any regionfile
				// which is 8192 bytes or smaller.
				if (region.length() <= 8192)
				{
					continue;
				}

				// Continue on...
				Matcher matcher = pattern.matcher(region.getName());
				if (matcher.matches())
				{
					available.add(
							new IntegerPair(
								Integer.parseInt(matcher.group(1)),
								Integer.parseInt(matcher.group(2))
								)
							);
				}
				else
				{
					XRay.logger.error("Could not match region coordinates for " + region.getName());
				}
			}
		}

		// Loop through our available regions to find the closest
		int curRegionX = (chunkX >> 5);
		int curRegionZ = (chunkZ >> 5);
		IntegerPair closestPair = null;
		double closestDistance = Double.MAX_VALUE;
		boolean found = false;
		for (IntegerPair pair : available)
		{
			if (pair.getValueOne() == curRegionX && pair.getValueTwo() == curRegionZ)
			{
				XRay.logger.trace("Our current region is present, jumping internally");
				closestPair = pair;
				break;
			}
			else
			{
				int dist_one;
				int dist_two;
				if (pair.getValueOne() > curRegionX)
				{
					dist_one = Math.abs(pair.getValueOne() - curRegionX);
				}
				else
				{
					dist_one = Math.abs(curRegionX - pair.getValueOne());
				}
				if (pair.getValueTwo() > curRegionZ)
				{
					dist_two = Math.abs(pair.getValueTwo() - curRegionZ);
				}
				else
				{
					dist_two = Math.abs(curRegionZ - pair.getValueTwo());
				}
				double thisDistance = Math.sqrt(Math.pow(dist_one, 2) + Math.pow(dist_two, 2));
				if (!found || thisDistance < closestDistance)
				{
					found = true;
					closestDistance = thisDistance;
					closestPair = pair;
				}
			}
		}

		// Now compute the chunk coordinates we should actually jump to
		if (closestPair == null)
		{
			XRay.logger.debug("No regions found to jump to");
		}
		else
		{
			// This bit would fit better inside RegionFile itself, but the RegionFile
			// class doesn't actually know anything about its absolute positioning,
			// so we'll just do it here anyway.
			RegionFile rf = getRegionFileByRegion(new File(basePath), closestPair.getValueOne(), closestPair.getValueTwo());
			if (rf != null)
			{
				int adjustedChunkX = chunkX - (closestPair.getValueOne()*32);
				int adjustedChunkZ = chunkZ - (closestPair.getValueTwo()*32);
				IntegerPair closestChunk = null;
				double closestChunkDistance = Double.MAX_VALUE;
				boolean foundChunk = false;
				for (int x = 0; x < 32; x++)
				{
					for (int z = 0; z < 32; z++)
					{
						if (rf.hasChunk(x, z))
						{
							int dist_one;
							int dist_two;
							if (x > adjustedChunkX)
							{
								dist_one = Math.abs(x - adjustedChunkX);
							}
							else
							{
								dist_one = Math.abs(adjustedChunkX - x);
							}
							if (z > adjustedChunkZ)
							{
								dist_two = Math.abs(z - adjustedChunkZ);
							}
							else
							{
								dist_two = Math.abs(adjustedChunkZ - z);
							}
							double thisChunkDistance = Math.sqrt(Math.pow(dist_one, 2) + Math.pow(dist_two, 2));
							if (!foundChunk || thisChunkDistance < closestChunkDistance)
							{
								foundChunk = true;
								closestChunkDistance = thisChunkDistance;
								closestChunk = new IntegerPair(x, z);
							}
						}
					}
				}
				if (foundChunk)
				{
					return new IntegerPair(closestChunk.getValueOne() + (closestPair.getValueOne()*32),
							closestChunk.getValueTwo() + (closestPair.getValueTwo()*32));
				}
			}
		}

		// If we get here, nothing was found; return null
		return null;
	}

	public static synchronized RegionFile getRegionFileByRegion(File basePath, int regionX, int regionZ)
	{
		return getRegionFile(basePath, (regionX << 5), (regionZ << 5));
	}

    public static synchronized RegionFile getRegionFile(File basePath, int chunkX, int chunkZ) {
        File regionDir = new File(basePath, "region");
        File file = new File(regionDir, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mcr");

        Reference<RegionFile> ref = cache.get(file);

        if (ref != null && ref.get() != null) {
            return ref.get();
        }

        /* Commented for X-Ray because I'd rather not modify anything, even if it's just a
         * directory.  We should never get here unless stuff exists, anyway.
        if (!regionDir.exists()) {
            regionDir.mkdirs();
        }
        */

        if (cache.size() >= MAX_CACHE_SIZE) {
            RegionFileCache.clear();
        }

        if (file.exists())
        {
	        RegionFile reg = new RegionFile(file);
	        cache.put(file, new SoftReference<RegionFile>(reg));
	        return reg;
        }
        else
        {
        	return null;
        }
    }

    public static synchronized void clear() {
        for (Reference<RegionFile> ref : cache.values()) {
            try {
                if (ref.get() != null) {
                    ref.get().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cache.clear();
    }

    public static int getSizeDelta(File basePath, int chunkX, int chunkZ) {
        RegionFile r = getRegionFile(basePath, chunkX, chunkZ);
        return r.getSizeDelta();
    }

    public static DataInputStream getChunkDataInputStream(File basePath, int chunkX, int chunkZ) {
        RegionFile r = getRegionFile(basePath, chunkX, chunkZ);
        return r.getChunkDataInputStream(chunkX & 31, chunkZ & 31);
    }
    
    /* Commented for X-Ray since we won't be writing anything
    public static DataOutputStream getChunkDataOutputStream(File basePath, int chunkX, int chunkZ) {
        RegionFile r = getRegionFile(basePath, chunkX, chunkZ);
        return r.getChunkDataOutputStream(chunkX & 31, chunkZ & 31);
    }
    */
}
