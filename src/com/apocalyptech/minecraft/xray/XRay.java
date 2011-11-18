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

import com.apocalyptech.minecraft.xray.MinecraftConstants.KEY_ACTION;
import com.apocalyptech.minecraft.xray.dialog.JumpDialog;
import com.apocalyptech.minecraft.xray.dialog.ResolutionDialog;
import com.apocalyptech.minecraft.xray.dialog.WarningDialog;
import com.apocalyptech.minecraft.xray.dialog.KeyHelpDialog;
import com.apocalyptech.minecraft.xray.dialog.BlockBindDialog;
import com.apocalyptech.minecraft.xray.dialog.ExceptionDialog;


import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.apocalyptech.minecraft.xray.Block;
import com.apocalyptech.minecraft.xray.WorldInfo;
import static com.apocalyptech.minecraft.xray.MinecraftConstants.*;

public class XRay
{

	// number of chunks around the camera which are visible (Square)
	private int visible_chunk_range = 5;

	private static final int[] CHUNK_RANGES_KEYS = new int[6];
	private static final int[] CHUNK_RANGES = new int[] { 3, 4, 5, 6, 7, 8 };
	private int currentChunkRange = 4;

	// highlight distance
	private static final int[] HIGHLIGHT_RANGES_KEYS = new int[7];
	private static final int[] HIGHLIGHT_RANGES = new int[] { 2, 3, 4, 5, 6, 7, 8 };
	private int currentHighlightDistance = 1;

	// ore highlight vars
	private static short[] HIGHLIGHT_ORES = new short[preferred_highlight_ores.length];
	private static final int[] HIGHLIGHT_ORE_KEYS = new int[preferred_highlight_ores.length];

	// By default we'll keep 20x20 chunks in our cache, which should hopefully let
	// us stay ahead of the camera
	// TODO: keep this at 8, or back up to 10?
	private final int loadChunkRange = 8;

	// set to true when the program is finished
	private boolean done = false;
	// are we full screen
	private boolean fullscreen = false;
	// are we inverting the mouse
	private boolean invertMouse = false;
	// window title
	public static String app_version = "3.5.0";
	public static String app_name    = "Minecraft X-Ray";
	public static String windowTitle = app_name + " " + app_version;

	// Minimap size - I did try increasing this but there were some performance
	// issues
	private final int minimap_dim = 2048;
	private final float minimap_dim_f = (float) minimap_dim;
	private final int minimap_dim_h = minimap_dim / 2;
	private final float minimap_dim_h_f = (float) minimap_dim_h;
	private boolean minimap_needs_updating = false;

	// current display mode
	private DisplayMode displayMode;

	// last system time in the main loop (to calculate delta for camera movement)
	private long lastTime;

	// our camera
	private FirstPersonCameraController camera;
	private boolean camera_lock = false;

	// the current mouseX and mouseY on the screen
	private int mouseX;
	private int mouseY;

	// the sprite sheet for all textures
	public ArrayList<Texture> minecraftTextures;
	public Texture paintingTexture;
	public Texture loadingTextTexture;
	public Texture chunkBorderTexture;
	public Texture slimeChunkTexture;

	// the textures used by the minimap
	private Texture minimapTexture;
	private Texture minimapArrowTexture;
	private Graphics2D minimapGraphics;

	public enum HIGHLIGHT_TYPE
	{
		DISCO ("Disco", Color.GREEN.darker()),
		WHITE ("White", Color.GREEN.darker()),
		OFF ("Off", Color.RED.darker())
		;
		public String reportText;
		public Color reportColor;
		HIGHLIGHT_TYPE(String reportText, Color reportColor)
		{
			this.reportText = reportText;
			this.reportColor = reportColor;
		}
	}
	private static final HIGHLIGHT_TYPE defaultHighlightOre = HIGHLIGHT_TYPE.DISCO;

	// Toggles that need to be available to the renderers
	public static class RenderToggles
	{
		public boolean render_bedrock = false;
		public boolean render_water = true;
		public boolean highlight_explored = false;
		public boolean beta19_fences = true;
		public HIGHLIGHT_TYPE highlightOres = defaultHighlightOre;
	}
	public static RenderToggles toggle = new RenderToggles();

	// the minecraft level we are exploring
	private MinecraftLevel level;

	// the current block (universal coordinate) where the camera is hovering on
	private int levelBlockX, levelBlockZ;

	// the current and previous chunk coordinates where the camera is hovering on
	private int currentLevelX, currentLevelZ;

	// we render to a display list and use that later for quick drawing, this is the index
	@SuppressWarnings("unused")
	private int worldDisplayListNum;
	@SuppressWarnings("unused")
	private int visibleOresListNum;

	// wheter we need to reload the world
	private boolean needToReloadWorld = false;

	// the width and height of the current screen resolution
	private int screenWidth, screenHeight;

	// the current camera position
	private float currentCameraPosX;
	private float currentCameraPosZ;

	// wheter we show the big map or the mini map
	private boolean mapBig = false;

	// wheter we are done with loading the map data (just for the mini map really)
	private boolean map_load_started = false;

	// the available world numbers
	private ArrayList<WorldInfo> availableWorlds;
	private int selectedWorld;

	// the world chunks we still need to load
	private LinkedList<Block> mapChunksToLoad;

	// the current (selected) world number
	private WorldInfo world = null;

	// the current fps we are 'doing'
	private int fps;

	// the laste time fps was updated
	private long lastFpsTime = -1;

	// the number of frames since the last fps update
	private int framesSinceLastFps;

	// the fps display texture
	private Texture fpsTexture;

	// far too many fps calculation variables (copied this from another project)
	public long previousTime;
	public long timeDelta;
	private boolean updateFPSText;
	private long time;

	private boolean[] mineralToggle;
	private Texture[] mineralToggleTextures;

	// lighting on or of (its actually fog, but hey...)
	private boolean lightMode = true;

	// level info texture
	private boolean levelInfoToggle = false;
	private Texture levelInfoTexture;
	private boolean renderDetailsToggle = true;
	private Texture renderDetailsTexture;
	private int renderDetails_w = 160;
	private int cur_renderDetails_h;
	private int levelInfoTexture_h = 144;
	private boolean regenerateRenderDetailsTexture = false;
	private boolean regenerateOreHighlightTexture = false;

	// light level
	private int[] lightLevelEnd = new int[] { 30, 50, 70, 100, 130 };
	private int[] lightLevelStart = new int[] { 0, 20, 30, 40, 60 };
	private int currentLightLevel = 2;

	// Grass rendering status
	private boolean accurateGrass = true;

	// Silverfish rendering status
	private boolean silverfishHighlight = true;

	// Chunk border rendering status
	private boolean renderChunkBorders = false;

	// Slime chunk rendering status
	private boolean renderSlimeChunks = false;

	// vars to keep track of our current chunk coordinates
	private int cur_chunk_x = 0;
	private int cur_chunk_z = 0;
	private boolean initial_load_done = false;
	private boolean initial_load_queued = false;

	// vars to keep track of how much the camera has moved since our last
	// minimap trim.
	private int total_dX = 0;
	private int total_dZ = 0;
	private int minimap_trim_chunks = 10;
	private int minimap_trim_chunk_distance = 64;

	// How long are we allowed to spend loading chunks before we update?
	private long max_chunkload_time = Sys.getTimerResolution() / 10; // a tenth of a second

	// The current camera position that we're at
	private CameraPreset currentPosition;
	private String cameraTextOverride = null;

	private HashMap<KEY_ACTION, Integer> key_mapping;
	private XRayProperties xray_properties;

	public boolean jump_dialog_trigger = false;

	public static HashMap<Integer, TextureDecorationStats> decorationStats;

	public static Logger logger = Logger.getLogger(XRay.class);

	// lets start with the program
	public static void main(String args[])
	{
		//PropertyConfigurator.configure("xray-log4j.properties");
		Date now = new Date();
		logger.info("Starting " + windowTitle + " at " + now.toString());
		logger.info("LWJGL version " + Sys.getVersion());
		logger.info("JVM version " + System.getProperty("java.version"));
		logger.info("Detected OS " + MinecraftEnvironment.os.toString());
		logger.info("");
		new XRay().run();
	}

	// go
	public void run()
	{
		// First up: initialize our static datastructures in MinecraftConstants.
		// This used to happen in a static {} block, but that makes some things
		// difficult.
		try
		{
			MinecraftConstants.initialize();
		}
		catch (BlockTypeLoadException e)
		{
			ExceptionDialog.presentDialog("Error reading Minecraft block data", e);
			return;
		}

		// This was moved from initialize() because we want to have this variable
		// available for loadOptionStates(), which happens first.
		mineralToggle = new boolean[HIGHLIGHT_ORES.length];

		try
		{
			// check whether we can access minecraft
			// and if we have worlds to load
			checkMinecraftFiles();

			// Load our preferences (this includes key mappings)
			setPreferenceDefaults();
			ArrayList<String> errors = loadPreferences();
			if (errors.size() > 0)
			{
				StringBuffer errorText = new StringBuffer();
				errorText.append("The following errors were encountered while loading xray.properties:\n\n");
				for (String error : errors)
				{
					errorText.append(" * " + error + "\n");
				}
				WarningDialog.presentDialog("Errors in xray.properties", errorText.toString(), false, 600, 250);
			}

			// prompt for the resolution and initialize the window
			createWindow();

			// Save any prefs which may have changed
			savePreferences();

			// basic opengl initialization
			initGL();

			// init our program
			try
			{
				initialize();
			}
			catch (BlockTypeLoadException e)
			{
				ExceptionDialog.presentDialog("Error reading Minecraft block data", e);
				return;
			}

			// And now load our world
			this.setMinecraftWorld(availableWorlds.get(this.selectedWorld));
			this.triggerChunkLoads();

			// Render details
			updateRenderDetails();

			// main loop
			while (!done)
			{
				long time = Sys.getTime();
				float timeDelta = (time - lastTime) / 1000.0f;
				lastTime = time;

				// handle input given the timedelta (for mouse control)
				handleInput(timeDelta);

				// Load chunks if needed
				if (mapChunksToLoad != null)
				{
					loadPendingChunks();
				}

				// Regenerate our rendering details window if we've been told to
				if (this.regenerateRenderDetailsTexture)
				{
					updateRenderDetails();
				}
				if (this.regenerateOreHighlightTexture)
				{
					updateOreHighlightTextures();
				}

				// render whatever we need to render
				render(timeDelta);

				// update our minimap if we need to (new chunks loaded, etc)
				if (minimap_needs_updating)
				{
					minimapTexture.update();
					minimap_needs_updating = false;
				}

				// Sleep a bit if we're not visible, to save on CPU
				// This is especially important when isVisible() is false, because
				// Display.update() does NOT vSync in that case.
				if (!Display.isVisible())
				{
					Thread.sleep(100);
				}
				else if (!Display.isActive())
				{
					Thread.sleep(33);
				}

				// Push to screen
				Display.update();

			}
			// cleanup
			saveOptionStates();
			cleanup();
		}
		catch (Exception e)
		{
			// bah, some error happened
			Mouse.setGrabbed(false);
			ExceptionDialog.clearExtraStatus();
			ExceptionDialog.presentDialog("Exception Encountered!", e);
			System.exit(0);
		}
	}

	/**
	 * Loads our preferences. This also sets our default keybindings if they're
	 * not overridden somewhere.
	 */
	public ArrayList<String> loadPreferences()
	{
		xray_properties = new XRayProperties();
		ArrayList<String> errors = new ArrayList<String>();
		String error;

		// First load our defaults into the prefs object
		for (KEY_ACTION action : KEY_ACTION.values())
		{
			xray_properties.setProperty("KEY_" + action.toString(), Keyboard.getKeyName(this.key_mapping.get(action)));
		}

		// Here's where we would load from our prefs file
		File prefs = MinecraftEnvironment.getXrayConfigFile();
		if (prefs.exists() && prefs.canRead())
		{
			try
			{
				xray_properties.load(new FileInputStream(prefs));
			}
			catch (IOException e)
			{
				// Just report and continue
				logger.warn("Could not load configuration file: " + e.toString());
			}
		}

		// Loop through the key mappings that we just loaded
		int newkey;
		String prefskey;
		for (KEY_ACTION action : KEY_ACTION.values())
		{
			prefskey = xray_properties.getProperty("KEY_" + action.toString());
			if (prefskey.equalsIgnoreCase("none"))
			{
				// If the user actually specified "NONE" in the config file,
				// unbind the key
				newkey = Keyboard.KEY_NONE;
			}
			else
			{
				newkey = Keyboard.getKeyIndex(prefskey);
				if (newkey == Keyboard.KEY_NONE)
				{
					// TODO: Should output something more visible to the user
					error = "Key '" + prefskey + "' for action " + action + " is unknown.  Default key '" + Keyboard.getKeyName(key_mapping.get(action)) + "' assigned.";
					logger.warn(error);
					errors.add(error);
					continue;
				}
			}
			this.key_mapping.put(action, newkey);
		}

		// Populate our key ranges
		int i;
		for (i = 0; i < CHUNK_RANGES.length; i++)
		{
			CHUNK_RANGES_KEYS[i] = this.key_mapping.get(KEY_ACTION.valueOf("CHUNK_RANGE_" + (i + 1)));
		}
		for (i = 0; i < HIGHLIGHT_RANGES.length; i++)
		{
			HIGHLIGHT_RANGES_KEYS[i] = this.key_mapping.get(KEY_ACTION.valueOf("HIGHLIGHT_RANGE_" + (i + 1)));
		}
		for (i = 0; i < HIGHLIGHT_ORES.length; i++)
		{
			HIGHLIGHT_ORE_KEYS[i] = this.key_mapping.get(KEY_ACTION.valueOf("TOGGLE_ORE_" + (i + 1)));
		}

		// Populate our list of ores to highlight
		String prefs_highlight;
		String prefs_highlight_key;
		for (i = 0; i < preferred_highlight_ores.length; i++)
		{
			prefs_highlight_key = "HIGHLIGHT_" + (i + 1);
			prefs_highlight = xray_properties.getProperty(prefs_highlight_key);
			try
			{
				HIGHLIGHT_ORES[i] = blockCollection.getByName(prefs_highlight).id;
			}
			catch (Exception e)
			{
				// no worries, just populate with our default
				error = "Block type '" + prefs_highlight + "', for HIGHLIGHT_" + (i+1) + " is an unknown block.  Reverting to default: " + blockArray[HIGHLIGHT_ORES[i]].idStr;
				logger.warn(error);
				errors.add(error);
			}
			xray_properties.put(prefs_highlight_key, blockArray[HIGHLIGHT_ORES[i]].idStr);
		}

		// Read in our saved option states, if we have 'em
		this.loadOptionStates();

		// Save the file immediately, in case we picked up new defaults which weren't present previously
		this.savePreferences();

		// Return
		return errors;
	}

	/***
	 * Updates our key-mapping preferences and saves out the config file.
	 */
	public void updateKeyMapping()
	{
		for (Map.Entry<KEY_ACTION, Integer> entry : key_mapping.entrySet())
		{
			xray_properties.setProperty("KEY_" + entry.getKey().toString(), Keyboard.getKeyName(entry.getValue()));
		}
		this.savePreferences();

		// It's entirely possible that we changed the key to bring up
		// the key dialog itself, so let's force a redraw of that.
		// Because this is called directly from the AWT dialog itself,
		// though, we can't call it directly.  Instead just set a boolean
		// and it'll get picked up in the mainloop.
		this.regenerateRenderDetailsTexture = true;
	}

	/***
	 * Updates our ore-binding preferences and saves out the config file.
	 * Will also invalidate our selection stuff.
	 */
	public void updateHighlightBindings()
	{
		for (int i=0; i < HIGHLIGHT_ORES.length; i++)
		{
			xray_properties.setProperty("HIGHLIGHT_" + (i+1), blockArray[HIGHLIGHT_ORES[i]].idStr);
		}
		this.savePreferences();

		this.regenerateOreHighlightTexture = true;
		this.invalidateSelectedChunks();
	}

	/**
	 * Saves our preferences out
	 */
	public void savePreferences()
	{
		File prefs = MinecraftEnvironment.getXrayConfigFile();
		try
		{
			xray_properties.store(new FileOutputStream(prefs),
			"Feel free to edit.  Use \"NONE\" to disable an action.  Keys taken from http://www.lwjgl.org/javadoc/constant-values.html#org.lwjgl.input.Keyboard.KEY_1");
		}
		catch (IOException e)
		{
			// Just report on the console and move on
			logger.error("Could not save preferences to file: " + e.toString());
		}
	}

	/**
	 * Sets our default preferences
	 */
	public void setPreferenceDefaults()
	{
		// First do the default key mappings
		key_mapping = new HashMap<KEY_ACTION, Integer>();
		for (KEY_ACTION action : KEY_ACTION.values())
		{
			key_mapping.put(action, action.def_key);
		}

		// Then populate our highlight blocks
		for (int i = 0; i < preferred_highlight_ores.length; i++)
		{
			XRay.HIGHLIGHT_ORES[i] = blockCollection.getByName(preferred_highlight_ores[i]).id;
		}
	}

	/**
	 * Loads any pending chunks, but won't exceed max_chunkload_time timer ticks
	 * (unless we're doing the initial load).
	 */
	public void loadPendingChunks()
	{
		Block b;
		long time = Sys.getTime();
		int total = 0;
		int counter = 0;
		if (!initial_load_done)
		{
			total = mapChunksToLoad.size();
			setOrthoOn();

			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			GL11.glLineWidth(20);

			BufferedImage i = loadingTextTexture.getImage();
			Graphics2D g = i.createGraphics();
			g.setColor(new Color(0f, 0f, 0f, 0f));
			g.setComposite(AlphaComposite.Src);
			g.fillRect(0, 0, i.getWidth(), i.getHeight());
			String statusmessage;
			if (this.cameraTextOverride == null)
			{
				statusmessage = "Moving camera to " + this.currentPosition.name;
			}
			else
			{
				statusmessage = "Moving camera to " + this.cameraTextOverride;
				this.cameraTextOverride = null;
			}
			Rectangle2D bounds = HEADERFONT.getStringBounds(statusmessage, g.getFontRenderContext());
			g.setFont(HEADERFONT);
			g.setColor(Color.white);
			g.drawString(statusmessage, (1024 / 2) - ((float) bounds.getWidth() / 2), 40f);
			loadingTextTexture.update();
		}

		// There's various cases where parts of our crosshairs may be covered over by
		// other blocks, or bits of the crosshairs left on the map when wrapping, etc.
		// Whatever.
		boolean got_spawn_chunk = false;
		boolean got_playerpos_chunk = false;
		CameraPreset spawn = level.getSpawnPoint();
		CameraPreset playerpos = level.getPlayerPosition();
		Chunk c;
		while (!mapChunksToLoad.isEmpty())
		{
			// Load and draw the chunk
			b = (Block) mapChunksToLoad.removeFirst();
			// logger.debug("Loading chunk " + b.x + "," + b.z);

			// There may be some circumstances where a chunk we're going to load is already loaded.
			// Mostly while moving diagonally, I think. I'm actually not convinced that it's worth
			// checking for, as it doesn't happen TOO often.
			c = level.getChunk(b.x, b.z);
			if (c != null)
			{
				if (c.x == b.x && c.z == b.z)
				{
					continue;
				}
			}
			level.loadChunk(b.x, b.z);
			drawChunkToMap(b.x, b.z);
			if (spawn.block.cx == b.x && spawn.block.cz == b.z)
			{
				got_spawn_chunk = true;
			}
			if (playerpos.block.cx == b.x && playerpos.block.cz == b.z)
			{
				got_playerpos_chunk = true;
			}

			// Make sure we update the minimap
			minimap_needs_updating = true;

			// Draw a progress bar if we're doing the initial load
			if (!initial_load_done)
			{
				counter++;
				if (counter % 5 == 0)
				{
					float progress = ((float) counter / (float) total);

					float bx = 100;
					float ex = screenWidth - 100;
					float by = (screenHeight / 2.0f) - 50;
					float ey = (screenHeight / 2.0f) + 50;

					float px = ((ex - bx) * progress) + bx;

					// progress bar outer box
					GL11.glBegin(GL11.GL_LINE_LOOP);
					GL11.glVertex2f(bx, by);
					GL11.glVertex2f(ex, by);
					GL11.glVertex2f(ex, ey);
					GL11.glVertex2f(bx, ey);
					GL11.glEnd();

					// progress bar 'progress'
					GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
					GL11.glVertex2f(bx, by);
					GL11.glVertex2f(px, by);
					GL11.glVertex2f(bx, ey);
					GL11.glVertex2f(px, ey);
					GL11.glEnd();

					// Draw our message
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glEnable(GL11.GL_TEXTURE_2D);
					SpriteTool.drawSpriteAbsoluteXY(loadingTextTexture, 0f, by - 100);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					Display.update();
				}
			}
			else
			{
				// Otherwise (if our initial load is done), mark any existing adjacent chunks
				// as dirty so that they re-render. This is needed so that we don't get gaps
				// in our terrain because the adjacent chunks weren't ready yet.
				level.markChunkAsDirty(b.x + 1, b.z);
				level.markChunkAsDirty(b.x - 1, b.z);
				level.markChunkAsDirty(b.x, b.z + 1);
				level.markChunkAsDirty(b.x, b.z - 1);
			}

			// If we've taken too long, break out so the GUI can update
			if (initial_load_done && Sys.getTime() - time > max_chunkload_time)
			{
				break;
			}
		}
		if (got_spawn_chunk)
		{
			drawSpawnMarkerToMinimap();
		}
		if (got_playerpos_chunk)
		{
			drawPlayerposMarkerToMinimap();
		}
		if (!initial_load_done)
		{
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			setOrthoOff();
		}
		initial_load_done = true;
	}

	public void incLightLevel()
	{
		this.currentLightLevel++;
		if (this.currentLightLevel >= this.lightLevelStart.length)
		{
			this.currentLightLevel = this.lightLevelStart.length - 1;
		}
	}

	public void decLightLevel()
	{
		this.currentLightLevel--;
		if (this.currentLightLevel <= 0)
		{
			this.currentLightLevel = 0;
		}
	}

	public void setLightLevel()
	{
		this.setLightLevel(0);
	}

	public void setLightLevel(int diff)
	{
		int min = this.lightLevelStart[this.currentLightLevel];
		int max = this.lightLevelEnd[this.currentLightLevel];

		min = min + diff;
		max = max + diff;

		if (min <= 0)
		{
			min = 0;
		}
		if (max <= 0)
		{
			max = 0;
		}

		GL11.glFogf(GL11.GL_FOG_START, min);
		GL11.glFogf(GL11.GL_FOG_END, max);
	}

	/**
	 * Alters our grass texture_dir_map to include or not include the fancier
	 * grass rendering, in case anyone wants that behavior on occasion.
	 */
	private void setAccurateGrass()
	{
		if (accurateGrass)
		{
			if (BLOCK_GRASS.texture_dir_map == null)
			{
				BLOCK_GRASS.texture_dir_map = grassDirectionMap;
			}
		}
		else
		{
			if (BLOCK_GRASS.texture_dir_map != null)
			{
				BLOCK_GRASS.texture_dir_map = null;
			}
		}
	}

	/**
	 * Alters our silverfish texture_dir_map to include or not include the fancier
	 * silverfish highlighting
	 */
	private void setSilverfishHighlight()
	{
		if (silverfishHighlight)
		{
			BLOCK_SILVERFISH.texture_data_map = MinecraftEnvironment.silverfishDataHighlighted;
			BLOCK_SILVERFISH.setTexIdx(MinecraftEnvironment.silverfishDataHighlighted.get((byte)0));
		}
		else
		{
			BLOCK_SILVERFISH.texture_data_map = MinecraftEnvironment.silverfishDataPlain;
			BLOCK_SILVERFISH.setTexIdx(MinecraftEnvironment.silverfishDataPlain.get((byte)0));
		}
	}

	/***
	 * Initialize the basic openGL environment
	 */
	private void initGL()
	{
		GL11.glEnable(GL11.GL_TEXTURE_2D); // Enable Texture Mapping
		GL11.glShadeModel(GL11.GL_FLAT); // Disable Smooth Shading
		GL11.glClearColor(0.0f, 0.3f, 1.0f, 0.3f); // Blue Background
		GL11.glClearDepth(1.0); // Depth Buffer Setup
		GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing
		GL11.glDepthFunc(GL11.GL_LEQUAL); // The Type Of Depth Testing To Do
		// GL11.glDepthFunc(GL11.GL_ALWAYS);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		// GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

		GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
		GL11.glLoadIdentity(); // Reset The Projection Matrix

		// Calculate The Aspect Ratio Of The Window
		GLU.gluPerspective(90.0f, (float) displayMode.getWidth() / (float) displayMode.getHeight(), 0.1f, 400.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW); // Select The Modelview Matrix

		// Really Nice Perspective Calculations
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

		GL11.glDisable(GL11.GL_FOG);
		GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
		float[] color = new float[] { 0.0f, 0.3f, 1.0f, 0.3f };
		ByteBuffer colorBytes = ByteBuffer.allocateDirect(64);
		FloatBuffer colorBuffer = colorBytes.asFloatBuffer();
		colorBuffer.rewind();
		colorBuffer.put(color);
		colorBuffer.rewind();
		GL11.glFog(GL11.GL_FOG_COLOR, colorBytes.asFloatBuffer());
		GL11.glFogf(GL11.GL_FOG_DENSITY, 0.3f);
		GL11.glHint(GL11.GL_FOG_HINT, GL11.GL_NICEST);
		setLightLevel();

	}

	/**
	 * Clears out variables and textures that we'd need while loading a new world
	 * (just when switching dimensions, at the moment)
	 */
	private void prepareNewWorld()
	{
		try
		{
			minimapTexture = TextureTool.allocateTexture(minimap_dim, minimap_dim);
			minimapGraphics = minimapTexture.getImage().createGraphics();
			loadingTextTexture = TextureTool.allocateTexture(1024, 64);
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// level data
		levelBlockX = Integer.MIN_VALUE;
		levelBlockZ = Integer.MIN_VALUE;
	}

	/***
	 * Load textures init precalc tables determine available worlds init misc
	 * variables
	 */
	private void initialize()
	throws BlockTypeLoadException
	{
		// init the precalc tables

		mineralToggleTextures = new Texture[HIGHLIGHT_ORES.length];

		// world display list
		worldDisplayListNum = GL11.glGenLists(1);
		visibleOresListNum = GL11.glGenLists(1);

		// camera
		camera = new FirstPersonCameraController(0, 0, 0);

		// textures
		try
		{
			// Note that in order to avoid weird texture-resize fuzziness, these textures
			// should have dimensions which are powers of 2
			minimapArrowTexture = TextureTool.allocateTexture(32, 32);
			fpsTexture = TextureTool.allocateTexture(128, 32);
			levelInfoTexture = TextureTool.allocateTexture(128, 256);
			renderDetailsTexture = TextureTool.allocateTexture(256, 256);

			createMinimapSprites();

			// minecraft textures
			minecraftTextures = new ArrayList<Texture>();
			ArrayList<BufferedImage> textureImages = MinecraftEnvironment.getMinecraftTexture();
			if (textureImages == null)
			{
			}
			for (BufferedImage image : textureImages)
			{
				Texture newtex = TextureTool.allocateTexture(image, GL11.GL_NEAREST);
				newtex.update();
				minecraftTextures.add(newtex);
			}
			Texture minecraftTexture = minecraftTextures.get(0);

			// Get a list of block types organized by type
			HashMap<BLOCK_TYPE, ArrayList<BlockType>> reverse_block_type_map = new HashMap<BLOCK_TYPE, ArrayList<BlockType>>();
			for (BlockType block : blockCollection.getBlocksFull())
			{
				if (!reverse_block_type_map.containsKey(block.type))
				{
					reverse_block_type_map.put(block.type, new ArrayList<BlockType>());
				}
				reverse_block_type_map.get(block.type).add(block);
			}

			// Compute some information about some decorative textures
			decorationStats = new HashMap<Integer, TextureDecorationStats>();
			for (BLOCK_TYPE decBlockType : DECORATION_BLOCKS)
			{
				for (BlockType decBlock : reverse_block_type_map.get(decBlockType))
				{
					// First the basic data map
					if (decBlock.texture_data_map != null)
					{
						for (int textureId : decBlock.texture_data_map.values())
						{
							if (!decorationStats.containsKey(textureId))
							{
								decorationStats.put(textureId, new TextureDecorationStats(minecraftTexture, textureId));
							}
						}
					}

					// Now the directional map
					if (decBlock.texture_dir_map != null)
					{
						for (int textureId : decBlock.texture_dir_map.values())
						{
							if (!decorationStats.containsKey(textureId))
							{
								decorationStats.put(textureId, new TextureDecorationStats(minecraftTexture, textureId));
							}
						}
					}

					// Now any "extra" textures which might exist for the block type
					if (blockTypeExtraTexturesReq.containsKey(decBlock.type))
					{
						for (String key : blockTypeExtraTexturesReq.get(decBlock.type))
						{
							int textureId = decBlock.texture_extra_map.get(key);
							if (!decorationStats.containsKey(textureId))
							{
								decorationStats.put(textureId, new TextureDecorationStats(minecraftTexture, textureId));
							}
						}
					}

					// Now the "base" texture, if we didn't already do it
					if (!decorationStats.containsKey(decBlock.tex_idx))
					{
						int textureId = decBlock.tex_idx;
						decorationStats.put(textureId, new TextureDecorationStats(minecraftTexture, textureId));
					}
				}
			}

			// painting textures
			BufferedImage minecraftPaintingImage = MinecraftEnvironment.getMinecraftPaintings();
			paintingTexture = TextureTool.allocateTexture(minecraftPaintingImage, GL11.GL_NEAREST);
			paintingTexture.update();

			// mineral textures
			this.updateOreHighlightTextures();

			// Chunk border texture
			int chunkBorderWidth = 256;
			int chunkBorderHeight = 2048;
			int stripeheight = 64;
			BufferedImage chunkBorderImage = new BufferedImage(chunkBorderWidth, chunkBorderHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = chunkBorderImage.createGraphics();
			g2d.setColor(new Color(0f, 0f, 0f, .4f));
			g2d.fillRect(0, 0, chunkBorderWidth, chunkBorderHeight);
			g2d.setColor(new Color(1f, 1f, 1f, .05f));
			for (int y=stripeheight; y<chunkBorderHeight; y += (stripeheight*2))
			{
				g2d.fillRect(0, y, chunkBorderWidth, stripeheight);
			}
			g2d.setColor(new Color(1f, 1f, 1f, .8f));
			g2d.drawRect(0, 0, chunkBorderWidth-1, chunkBorderHeight-1);
			chunkBorderTexture = TextureTool.allocateTexture(chunkBorderImage, GL11.GL_NEAREST);
			chunkBorderTexture.update();

			// Slime chunk textures
			int slimeChunkWidth = 256;
			BufferedImage slimeChunkImage = new BufferedImage(slimeChunkWidth, slimeChunkWidth, BufferedImage.TYPE_INT_ARGB);
			g2d = slimeChunkImage.createGraphics();
			g2d.setColor(new Color(0f, .5f, 0f, .4f));
			g2d.fillRect(0, 0, slimeChunkWidth, slimeChunkWidth);
			g2d.setColor(new Color(.7f, 1f, .7f, .8f));
			g2d.drawRect(0, 0, slimeChunkWidth-1, slimeChunkWidth-1);
			slimeChunkTexture = TextureTool.allocateTexture(slimeChunkImage, GL11.GL_NEAREST);
			slimeChunkTexture.update();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Extra things we have to do
		this.prepareNewWorld();
		setAccurateGrass();
		setSilverfishHighlight();

		// set mouse grabbed so we can get x/y coordinates
		Mouse.setGrabbed(true);

		// Disable repeat key events
		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Generate our text labels for the ores we're highlighting
	 */
	private void updateOreHighlightTextures()
		throws IOException
	{
		for (int i = 0; i < HIGHLIGHT_ORES.length; i++)
		{
			mineralToggleTextures[i] = TextureTool.allocateTexture(128, 32);
			Graphics2D g = mineralToggleTextures[i].getImage().createGraphics();
			g.setFont(ARIALFONT);
			g.setColor(Color.white);
			g.drawString("[F" + (i + 1) + "] " + blockArray[HIGHLIGHT_ORES[i]].name, 0, 16);
			mineralToggleTextures[i].update();
		}
		this.regenerateOreHighlightTexture = false;
	}

	private BufferedImage resizeImage(Image baseImage, int newWidth, int newHeight)
	{
		BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = newImage.createGraphics();
		g.drawImage(baseImage, 0, 0, newWidth, newHeight, null);

		return newImage;
	}

	private byte[] convertIcon(byte[] icon)
	{
		byte[] newIcon = new byte[icon.length];
		for (int i = 0; i < newIcon.length; i += 4)
		{
			newIcon[i + 3] = icon[i + 0];
			newIcon[i + 2] = icon[i + 1];
			newIcon[i + 1] = icon[i + 2];
			newIcon[i + 0] = icon[i + 3];
		}

		return newIcon;
	}

	/***
	 * Creates the window and initializes the lwjgl display object
	 * 
	 * @throws Exception
	 */
	private void createWindow() throws Exception
	{

		//Temporary map to hold new key bindings
		HashMap<KEY_ACTION, Integer> newMap = null;
		// set icon buffers
		// stupid conversions needed
		File iconFile = new File("xray_icon.png");
		ByteBuffer[] icons = null;
		if (iconFile.exists() || iconFile.canRead())
		{
			BufferedImage iconTexture128 = ImageIO.read(iconFile);
			iconTexture128 = resizeImage(iconTexture128, 128, 128); // just to be sure all icons are the same imagetype
			BufferedImage iconTexture64 = resizeImage(iconTexture128, 64, 64);
			BufferedImage iconTexture32 = resizeImage(iconTexture128, 32, 32);
			BufferedImage iconTexture16 = resizeImage(iconTexture128, 16, 16);

			byte[] iconBuffer128d = ((DataBufferByte) iconTexture128.getRaster().getDataBuffer()).getData();
			byte[] iconBuffer64d = ((DataBufferByte) iconTexture64.getRaster().getDataBuffer()).getData();
			byte[] iconBuffer32d = ((DataBufferByte) iconTexture32.getRaster().getDataBuffer()).getData();
			byte[] iconBuffer16d = ((DataBufferByte) iconTexture16.getRaster().getDataBuffer()).getData();

			iconBuffer128d = convertIcon(iconBuffer128d); // LWJGL (opengl?) needs RGBA ... imagetype available is ABGR
			iconBuffer64d = convertIcon(iconBuffer64d);
			iconBuffer32d = convertIcon(iconBuffer32d);
			iconBuffer16d = convertIcon(iconBuffer16d);

			ByteBuffer iconBuffer128 = ByteBuffer.wrap(iconBuffer128d);
			ByteBuffer iconBuffer64 = ByteBuffer.wrap(iconBuffer64d);
			ByteBuffer iconBuffer32 = ByteBuffer.wrap(iconBuffer32d);
			ByteBuffer iconBuffer16 = ByteBuffer.wrap(iconBuffer16d);

			iconBuffer128.rewind();
			iconBuffer64.rewind();
			iconBuffer32.rewind();
			iconBuffer16.rewind();

			icons = new ByteBuffer[] { iconBuffer128, iconBuffer64, iconBuffer32, iconBuffer16 };

			ResolutionDialog.iconImage = iconTexture128;
			JumpDialog.iconImage = iconTexture128;
			KeyHelpDialog.iconImage = iconTexture128;
			BlockBindDialog.iconImage = iconTexture128;
			WarningDialog.iconImage = iconTexture128;
			ExceptionDialog.iconImage = iconTexture128;
		}

		// If we're on Windows, show a warning about running at the same time as Minecraft
		if (MinecraftEnvironment.os == MinecraftEnvironment.OS.Windows)
		{
			if (xray_properties.getBooleanProperty("SHOW_WINDOWS_WARNING", true))
			{
				WarningDialog.presentDialog("Warning", "Because of the way Windows locks files, it's possible that your Minecraft data files could get corrupted if you use X-Ray on a world which Minecraft currently has open.  If you're running Minecraft at the same time as X-Ray, be extra careful and make sure you have backups.");
				xray_properties.setBooleanProperty("SHOW_WINDOWS_WARNING", WarningDialog.selectedShow);
				savePreferences();
			}
		}

		// Open our main dialog and see how it turns out.
		if (ResolutionDialog.presentDialog(windowTitle, availableWorlds, xray_properties) == ResolutionDialog.DIALOG_BUTTON_EXIT)
		{
			System.exit(0);
		}

		// Mark which world to load (which will happen later during initialize()
		this.selectedWorld = ResolutionDialog.selectedWorld;

		// set fullscreen from the dialog
		fullscreen = ResolutionDialog.selectedFullScreenValue;

		// set invertMouse from the dialog
		invertMouse = ResolutionDialog.selectedInvertMouseValue;

		if (icons != null)
			Display.setIcon(icons);

		// Display.setIcon();
		Display.setFullscreen(fullscreen);
		displayMode = ResolutionDialog.selectedDisplayMode;
		Display.setDisplayMode(displayMode);
		Display.setTitle(windowTitle);
		// TODO: actually do what the user requests here
		Display.setVSyncEnabled(true);
		Display.create();
		screenWidth = displayMode.getWidth();
		screenHeight = displayMode.getHeight();
	}

	/***
	 * Checks for sanity of the minecraft environment
	 */
	private void checkMinecraftFiles()
	{
		if (MinecraftEnvironment.getMinecraftDirectory() == null)
		{
			logger.error("OS not supported (" + System.getProperty("os.name") + ")");
			JOptionPane.showMessageDialog(null, "OS not supported (" + System.getProperty("os.name") + "), please report.", "Minecraft X-Ray Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		if (!MinecraftEnvironment.getMinecraftDirectory().exists())
		{
			logger.error("Minecraft directory not found: " + MinecraftEnvironment.getMinecraftDirectory().getAbsolutePath());
			JOptionPane.showMessageDialog(null, "Minecraft directory not found: " + MinecraftEnvironment.getMinecraftDirectory().getAbsolutePath(), "Minecraft X-Ray Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		if (!MinecraftEnvironment.getMinecraftDirectory().canRead())
		{
			logger.error("Minecraft directory not readable: " + MinecraftEnvironment.getMinecraftDirectory().getAbsolutePath());
			JOptionPane.showMessageDialog(null, "Minecraft directory not readable: " + MinecraftEnvironment.getMinecraftDirectory().getAbsolutePath(), "Minecraft X-Ray Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		availableWorlds = MinecraftEnvironment.getAvailableWorlds();
		// Add in a custom "Other..." world
		availableWorlds.add(new WorldInfo());

	}

	private void setChunkRange(int n)
	{
		if (n >= CHUNK_RANGES.length)
			n = CHUNK_RANGES.length - 1;
		if (n <= 0)
			n = 0;
		if (n == currentChunkRange)
		{
			return;
		}
		this.currentChunkRange = n;
		this.visible_chunk_range = CHUNK_RANGES[n];
		this.needToReloadWorld = true;
	}

	private void setHighlightRange(int n)
	{
		if (n >= HIGHLIGHT_RANGES.length)
			n = HIGHLIGHT_RANGES.length - 1;
		if (n <= 0)
			n = 0;
		if (n == currentHighlightDistance)
		{
			return;
		}
		this.currentHighlightDistance = n;
	}

	/**
	 * Updates our exploredBlocks hashmap based on dimension (Glowstone should trigger
	 * in Overworld, but not in Nether)
	 */
	private void updateExploredBlocks()
	{
		BlockType glowstone = blockCollection.getByName("GLOWSTONE");
		if (glowstone != null)
		{
			if (this.world.isDimension(-1))
			{
				if (exploredBlocks.containsKey(glowstone.id))
				{
					exploredBlocks.remove(glowstone.id);
				}
			}
			else
			{
				if (glowstone.getExplored() && !exploredBlocks.containsKey(glowstone.id))
				{
					exploredBlocks.put(glowstone.id, true);
				}
			}
		}
	}

	/***
	 * Sets the world number we want to view
	 * 
	 * @param world
	 */
	private void setMinecraftWorld(WorldInfo world)
	{
		this.world = world;
		this.level = new MinecraftLevel(world, minecraftTextures, paintingTexture, HIGHLIGHT_ORES);

		// determine which chunks are available in this world
		mapChunksToLoad = new LinkedList<Block>();

		updateExploredBlocks();

		moveCameraToPlayerPos();
	}

	/**
	 * Sets the world number we want, and moves the camera to the specified
	 * coordinates. There's a bit of code duplication going on here; should fix
	 * that.
	 * 
	 * @param world
	 * @param camera_x
	 * @param camera_z
	 */
	private void setMinecraftWorld(WorldInfo world, FirstPersonCameraController camera)
	{
		this.world = world;
		this.level = new MinecraftLevel(world, minecraftTextures, paintingTexture, HIGHLIGHT_ORES);

		// determine which chunks are available in this world
		mapChunksToLoad = new LinkedList<Block>();

		updateExploredBlocks();

		this.camera = camera;
		initial_load_queued = false;
		initial_load_done = false;
		this.removeChunklistFromMap(level.removeAllChunksFromMinimap());
		this.triggerChunkLoads();

	}

	private void moveCameraToPosition(CameraPreset playerPos)
	{
		this.camera.getPosition().set(playerPos.block.x, playerPos.block.y, playerPos.block.z);
		this.camera.setYawAndPitch(180 + playerPos.yaw, playerPos.pitch);
		initial_load_queued = false;
		initial_load_done = false;
		this.removeChunklistFromMap(level.removeAllChunksFromMinimap());
		this.triggerChunkLoads();
		this.currentPosition = playerPos;
	}

	private void launchNewMapDialog()
	{
		Mouse.setGrabbed(false);
		if (ResolutionDialog.presentDialog(windowTitle, availableWorlds, xray_properties, false) == ResolutionDialog.DIALOG_BUTTON_EXIT)
		{
			Mouse.setGrabbed(true);
			return;
		}

		this.selectedWorld = ResolutionDialog.selectedWorld;
		this.savePreferences();

		// A full reinitialization is kind of overkill, but whatever.
		// TODO: code duplicated from switchDimension
		this.prepareNewWorld();
		this.setMinecraftWorld(availableWorlds.get(this.selectedWorld));
		this.updateRenderDetails();
		this.triggerChunkLoads();
		Mouse.setGrabbed(true);
	}

	private void launchJumpDialog()
	{
		Mouse.setGrabbed(false);
		JumpDialog.presentDialog("Choose a New Position", this);
	}

	private void launchKeyHelpDialog()
	{
		Mouse.setGrabbed(false);
		KeyHelpDialog.presentDialog(key_mapping, this);
	}

	private void launchBlockBindDialog()
	{
		Mouse.setGrabbed(false);
		BlockBindDialog.presentDialog(HIGHLIGHT_ORES, minecraftTextures, this);
	}

	/**
	 * Calls moveCameraToPosition() with our current camera position, to invalidate
	 * our chunk cache and trigger reloads from disk.
	 */
	private void reloadFromDisk()
	{
		Block block = new Block((int)camera.getPosition().x, (int)camera.getPosition().y, (int)camera.getPosition().z);
		this.moveCameraToPosition(new CameraPreset(-1, "current location", block, camera.getYaw()-180, camera.getPitch()));
	}

	/**
	 * Moves the camera to the position specified by the JumpDialog.
	 */
	private void moveCameraToArbitraryPosition()
	{
		int x = JumpDialog.selectedX;
		int z = JumpDialog.selectedZ;
		String name;
		if (JumpDialog.selectedChunk)
		{
			name = "Chunk (" + x + ", " + z + ")";
			x = x * 16;
			z = z * 16;
		}
		else
		{
			name = "Position (" + x + ", " + z + ")";
		}
		Block block = new Block(-x, (int)camera.getPosition().y, -z);
		this.jump_dialog_trigger = false;
		this.moveCameraToPosition(new CameraPreset(-1, name, block, camera.getYaw()-180, camera.getPitch()));
		Mouse.setGrabbed(true);
	}

	private void moveCameraToSpawnPoint()
	{
		this.moveCameraToPosition(level.getSpawnPoint());
	}

	private void moveCameraToPlayerPos()
	{
		this.moveCameraToPosition(level.getPlayerPosition());
	}

	private void moveCameraToNextPlayer()
	{
		this.moveCameraToPosition(level.getNextPlayerPosition(this.currentPosition));
	}

	private void moveCameraToPreviousPlayer()
	{
		this.moveCameraToPosition(level.getPrevPlayerPosition(this.currentPosition));
	}

	/**
	 * Populates mapChunksToLoad with a list of chunks that need adding, based
	 * on how far we've moved since our last known position. Realistically this
	 * is never going to be more than one line at a time, though if someone's
	 * getting hit with ridiculously low FPS or something, perhaps there could
	 * end up being more.
	 */
	private void triggerChunkLoads()
	{
		int chunkX = level.getChunkX((int) -camera.getPosition().x);
		int chunkZ = level.getChunkZ((int) -camera.getPosition().z);

		if (initial_load_queued)
		{
			Chunk tempchunk;
			int dx = chunkX - cur_chunk_x;
			int dz = chunkZ - cur_chunk_z;

			int top_x = 0;
			int bot_x = 0;
			int top_z = 0;
			int bot_z = 0;

			// X
			if (dx < 0)
			{
				// logger.trace("Loading in chunks from the X range " + (cur_chunk_x-1-loadChunkRange) + " to " + (chunkX-loadChunkRange) + " (going down)");
				top_x = cur_chunk_x - 1 - loadChunkRange;
				bot_x = chunkX - loadChunkRange;
			}
			else if (dx > 0)
			{
				// logger.trace("Loading in chunks from the X range " + (cur_chunk_x+1+loadChunkRange) + " to " + (chunkX+loadChunkRange) + " (going up)");
				top_x = chunkX + loadChunkRange;
				bot_x = cur_chunk_x + 1 + loadChunkRange;
			}
			if (dx != 0)
			{
				for (int lx = bot_x; lx <= top_x; lx++)
				{
					for (int lz = chunkZ - loadChunkRange; lz <= chunkZ + loadChunkRange; lz++)
					{
						tempchunk = level.getChunk(lx, lz);
						if (tempchunk != null)
						{
							if (tempchunk.x == lx && tempchunk.z == lz)
							{
								if (!tempchunk.isOnMinimap)
								{
									drawChunkToMap(tempchunk.x, tempchunk.z);
									// minimap_changed = true;
								}
								continue;
							}
							level.clearChunk(lx, lz);
						}
						mapChunksToLoad.add(new Block(lx, 0, lz));
					}
				}
			}

			// Z
			if (dz < 0)
			{
				// logger.trace("Loading in chunks from the Z range " + (cur_chunk_z-1-loadChunkRange) + " to " + (chunkZ-loadChunkRange) + " (going down)");
				top_z = cur_chunk_z - 1 - loadChunkRange;
				bot_z = chunkZ - loadChunkRange;
			}
			else if (dz > 0)
			{
				// logger.trace("Loading in chunks from the Z range " + (cur_chunk_z+1+loadChunkRange) + " to " + (chunkZ+loadChunkRange) + " (going up)");
				top_z = chunkZ + loadChunkRange;
				bot_z = cur_chunk_z + 1 + loadChunkRange;
			}
			if (dz != 0)
			{
				for (int lx = chunkX - loadChunkRange; lx <= chunkX + loadChunkRange; lx++)
				{
					for (int lz = bot_z; lz <= top_z; lz++)
					{
						tempchunk = level.getChunk(lx, lz);
						if (tempchunk != null)
						{
							if (tempchunk.x == lx && tempchunk.z == lz)
							{
								if (!tempchunk.isOnMinimap)
								{
									drawChunkToMap(tempchunk.x, tempchunk.z);
									// minimap_changed = true;
								}
								continue;
							}
							level.clearChunk(lx, lz);
						}
						mapChunksToLoad.add(new Block(lx, 0, lz));
					}
				}
			}

			// Figure out if we need to trim our minimap (to prevent wrapping around)
			total_dX += dx;
			total_dZ += dz;
			ArrayList<Chunk> trimList = new ArrayList<Chunk>();
			int i;
			if (Math.abs(total_dX) >= minimap_trim_chunks)
			{
				if (total_dX < 0)
				{
					// logger.trace("Clearing X from " + (chunkX-minimap_trim_chunk_distance+minimap_trim_chunks) + " to " + (chunkX-minimap_trim_chunk_distance));
					for (i = chunkX - minimap_trim_chunk_distance + minimap_trim_chunks; i >= chunkX - minimap_trim_chunk_distance; i--)
					{
						trimList.addAll(level.removeChunkRowXFromMinimap(i));
					}
					total_dX = -(Math.abs(total_dX) % minimap_trim_chunks);
				}
				else
				{
					// logger.trace("Clearing X from " + (chunkX+minimap_trim_chunk_distance-minimap_trim_chunks) + " to " + (chunkX+minimap_trim_chunk_distance));
					for (i = chunkX + minimap_trim_chunk_distance - minimap_trim_chunks; i <= chunkX + minimap_trim_chunk_distance; i++)
					{
						trimList.addAll(level.removeChunkRowXFromMinimap(i));
					}
					total_dX = total_dX % minimap_trim_chunks;
				}
			}
			if (Math.abs(total_dZ) >= minimap_trim_chunks)
			{
				if (total_dZ < 0)
				{
					// logger.trace("Clearing Z from " + (chunkZ-minimap_trim_chunk_distance+minimap_trim_chunks) + " to " + (chunkZ-minimap_trim_chunk_distance));
					for (i = chunkZ - minimap_trim_chunk_distance + minimap_trim_chunks; i >= chunkZ - minimap_trim_chunk_distance; i--)
					{
						trimList.addAll(level.removeChunkRowZFromMinimap(i));
					}
					total_dZ = -(Math.abs(total_dZ) % minimap_trim_chunks);
				}
				else
				{
					// logger.trace("Clearing Z from " + (chunkZ+minimap_trim_chunk_distance-minimap_trim_chunks) + " to " + (chunkZ+minimap_trim_chunk_distance));
					for (i = chunkZ + minimap_trim_chunk_distance - minimap_trim_chunks; i <= chunkZ + minimap_trim_chunk_distance; i++)
					{
						trimList.addAll(level.removeChunkRowZFromMinimap(i));
					}
					total_dZ = total_dZ % minimap_trim_chunks;
				}
			}

			removeChunklistFromMap(trimList);
		}
		else
		{
			// logger.trace("Loading world from X: " + (chunkX-loadChunkRange) + " - " + (chunkX+loadChunkRange) + ", Z: " + (chunkZ-loadChunkRange) + " - " + (chunkZ+loadChunkRange));
			for (int lx = chunkX - loadChunkRange; lx <= chunkX + loadChunkRange; lx++)
			{
				for (int lz = chunkZ - loadChunkRange; lz <= chunkZ + loadChunkRange; lz++)
				{
					level.clearChunk(lx, lz);
					mapChunksToLoad.add(new Block(lx, 0, lz));
				}
			}
			initial_load_queued = true;
		}
		cur_chunk_x = chunkX;
		cur_chunk_z = chunkZ;
	}

	/***
	 * handles all input on all screens
	 * 
	 * @param timeDelta
	 */
	private void handleInput(float timeDelta)
	{

		int key;

		// distance in mouse movement from the last getDX() call.
		mouseX = Mouse.getDX();
		// distance in mouse movement from the last getDY() call.
		mouseY = Mouse.getDY();

		// we are on the main world screen or the level loading screen update the camera (but only if the mouse is grabbed)
		if (Mouse.isGrabbed())
		{
			camera.incYaw(mouseX * MOUSE_SENSITIVITY);
			if (invertMouse)
			{
				camera.incPitch(mouseY * MOUSE_SENSITIVITY);
			}
			else
			{
				camera.incPitch(-mouseY * MOUSE_SENSITIVITY);
			}
		}

		//
		// Keyboard commands (well, and mouse presses)
		// First up: "continual" commands which we're just using isKeyDown for
		//

		// Speed shifting
		if (Mouse.isButtonDown(0) || Keyboard.isKeyDown(key_mapping.get(KEY_ACTION.SPEED_INCREASE)))
		{
			MOVEMENT_SPEED = 30.0f;
		}
		else if (Mouse.isButtonDown(1) || Keyboard.isKeyDown(key_mapping.get(KEY_ACTION.SPEED_DECREASE)))
		{
			MOVEMENT_SPEED = 3.0f;
		}
		else
		{
			MOVEMENT_SPEED = 10.0f;
		}

		// Move forward
		if (Keyboard.isKeyDown(key_mapping.get(KEY_ACTION.MOVE_FORWARD)))
		{
			camera.walkForward(MOVEMENT_SPEED * timeDelta, camera_lock);
			triggerChunkLoads();
		}

		// Move backwards
		if (Keyboard.isKeyDown(key_mapping.get(KEY_ACTION.MOVE_BACKWARD)))
		{
			camera.walkBackwards(MOVEMENT_SPEED * timeDelta, camera_lock);
			triggerChunkLoads();
		}

		// Strafe Left
		if (Keyboard.isKeyDown(key_mapping.get(KEY_ACTION.MOVE_LEFT)))
		{
			camera.strafeLeft(MOVEMENT_SPEED * timeDelta);
			triggerChunkLoads();
		}

		// Strafe right
		if (Keyboard.isKeyDown(key_mapping.get(KEY_ACTION.MOVE_RIGHT)))
		{
			camera.strafeRight(MOVEMENT_SPEED * timeDelta);
			triggerChunkLoads();
		}

		// Fly Up
		if (Keyboard.isKeyDown(key_mapping.get(KEY_ACTION.MOVE_UP)))
		{
			camera.moveUp(MOVEMENT_SPEED * timeDelta);
			triggerChunkLoads();
		}

		// Fly Down
		if (Keyboard.isKeyDown(key_mapping.get(KEY_ACTION.MOVE_DOWN)))
		{
			camera.moveUp(-MOVEMENT_SPEED * timeDelta);
			triggerChunkLoads();
		}

		//
		// And now, keys that were meant to just be hit once and do their thing
		// 
		while (Keyboard.next())
		{
			if (Keyboard.getEventKeyState())
			{
				key = Keyboard.getEventKey();

				if (key == key_mapping.get(KEY_ACTION.TOGGLE_MINIMAP))
				{
					// Toggle minimap/largemap
					mapBig = !mapBig;
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_FULLSCREEN))
				{
					// Fullscreen
					switchFullScreenMode();
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_FULLBRIGHT))
				{
					// Toggle fullbright
					setLightMode(!lightMode);
					updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_ORE_HIGHLIGHTING))
				{
					// Toggle ore highlighting
					boolean found = false;
					boolean set = false;
					boolean have_off = false;
					for (HIGHLIGHT_TYPE type : HIGHLIGHT_TYPE.values())
					{
						if (type == toggle.highlightOres)
						{
							found = true;
							if (type == HIGHLIGHT_TYPE.OFF)
							{
								have_off = true;
							}
						}
						else if (found)
						{
							toggle.highlightOres = type;
							set = true;
							if (type == HIGHLIGHT_TYPE.OFF)
							{
								have_off = true;
							}
							break;
						}
					}
					if (!set)
					{
						toggle.highlightOres = HIGHLIGHT_TYPE.DISCO;
					}
					updateRenderDetails();
					if (have_off)
					{
						invalidateSelectedChunks();
					}
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_ACCURATE_GRASS))
				{
					// Toggle the drawing of accurate grass
					accurateGrass = !accurateGrass;
					setAccurateGrass();
					invalidateSelectedChunks(true);
					updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.MOVE_TO_SPAWN))
				{
					// Move camera to spawn point
					moveCameraToSpawnPoint();
				}
				else if (key == key_mapping.get(KEY_ACTION.MOVE_TO_PLAYER))
				{
					// Move camera to player position
					moveCameraToPlayerPos();
				}
				else if (key == key_mapping.get(KEY_ACTION.MOVE_NEXT_CAMERAPOS))
				{
					// Switch to the next available camera preset
					moveCameraToNextPlayer();
				}
				else if (key == key_mapping.get(KEY_ACTION.MOVE_PREV_CAMERAPOS))
				{
					// Switch to the previous camera preset
					moveCameraToPreviousPlayer();
				}
				else if (key == key_mapping.get(KEY_ACTION.RELOAD))
				{
					// Reload from disk
					reloadFromDisk();
				}
				else if (key == key_mapping.get(KEY_ACTION.LIGHT_INCREASE))
				{
					// Increase light level
					incLightLevel();
					updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.LIGHT_DECREASE))
				{
					// Decrease light level
					decLightLevel();
					updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_POSITION_INFO))
				{
					// Toggle position info popup
					levelInfoToggle = !levelInfoToggle;
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_RENDER_DETAILS))
				{
					// Toggle rendering info popup
					renderDetailsToggle = !renderDetailsToggle;
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_BEDROCK))
				{
					// Toggle bedrock rendering
					toggle.render_bedrock = !toggle.render_bedrock;
					invalidateSelectedChunks(true);
					updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_HIGHLIGHT_EXPLORED))
				{
					// Toggle explored-area highlighting
					toggle.highlight_explored = !toggle.highlight_explored;
					invalidateSelectedChunks(true);
					updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_WATER))
				{
					// Toggle water rendering
					toggle.render_water = !toggle.render_water;
					invalidateSelectedChunks(true);
					updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_BETA19_FENCES))
				{
					// Toggle "new" fence rendering
					toggle.beta19_fences = !toggle.beta19_fences;
					invalidateSelectedChunks(true);
					updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_SILVERFISH))
				{
					// Toggle silverfish highlighting
					silverfishHighlight = !silverfishHighlight;
					setSilverfishHighlight();
					invalidateSelectedChunks(true);
					updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_CAMERA_LOCK))
				{
					// Toggle camera lock
					camera_lock = !camera_lock;
					updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_CHUNK_BORDERS))
				{
					renderChunkBorders = !renderChunkBorders;
					// I think this one should be obvious enough not to bother with wording in the info box
					//updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.TOGGLE_SLIME_CHUNKS))
				{
					renderSlimeChunks = !renderSlimeChunks;
					updateRenderDetails();
				}
				else if (key == key_mapping.get(KEY_ACTION.DIMENSION_NEXT))
				{
					// Toggle between dimenaions
					switchDimension();
				}
				else if (key == key_mapping.get(KEY_ACTION.DIMENSION_PREV))
				{
					// Toggle between dimenaions
					switchDimension(-1);
				}
				else if (key == key_mapping.get(KEY_ACTION.KEY_HELP))
				{
					// Launch the dialog
					launchKeyHelpDialog();
				}
				else if (key == key_mapping.get(KEY_ACTION.SET_ORE_BINDS))
				{
					// Launch the ore-binding dialog
					launchBlockBindDialog();
				}
				else if (key == key_mapping.get(KEY_ACTION.RELEASE_MOUSE))
				{
					// Release the mouse
					Mouse.setGrabbed(false);
				}
				else if (key == key_mapping.get(KEY_ACTION.QUIT))
				{
					// Quit
					if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
					{
						done = true;
					}
				}
				/*
				else if (key == Keyboard.KEY_P)
				{
					// Temp routine to write the minimap out to a PNG (for debugging purposes)
					BufferedImage bi = minimapTexture.getImage();
					try {
						ImageIO.write(bi, "PNG", new File("/home/pez/xray.png"));
						logger.info("Wrote minimap to disk.");
					}
					catch (Exception e)
					{
						// whatever
					}
				}
				 */
				else
				{
					// Toggle highlightable ores
					needToReloadWorld = false;
					for (int i = 0; i < mineralToggle.length; i++)
					{
						if (key == HIGHLIGHT_ORE_KEYS[i])
						{
							mineralToggle[i] = !mineralToggle[i];
							needToReloadWorld = true;
						}
					}
					if (needToReloadWorld)
					{
						invalidateSelectedChunks();
					}

					// Handle changing chunk ranges (how far out we draw from the camera
					for (int i = 0; i < CHUNK_RANGES.length; i++)
					{
						if (key == CHUNK_RANGES_KEYS[i])
						{
							setChunkRange(i);
							updateRenderDetails();
						}
					}

					// Handle changing the ore highlight distances
					for (int i = 0; i < HIGHLIGHT_RANGES.length; i++)
					{
						if (key == HIGHLIGHT_RANGES_KEYS[i])
						{
							setHighlightRange(i);
							updateRenderDetails();
						}
					}
				}
			}
			else
			{
				// Here are keys which we process once they're RELEASED.
				// Currently that's just the Jump menu; if we launch other dialogs
				// in the future they'll probably be here as well.  The reason for
				// this is because if we handle it and launch the dialog on the key
				// PRESS event, it's the new dialog which receives the key-release
				// event, not the main window, so the LWJGL context doesn't know
				// about the release and believes that the key is being perpetually
				// pressed (at least, until the key is pressed again).

				key = Keyboard.getEventKey();

				if (key == key_mapping.get(KEY_ACTION.JUMP))
				{
					// Launch the Jump dialog
					launchJumpDialog();
				}
				else if (key == key_mapping.get(KEY_ACTION.OPEN_NEW_MAP))
				{
					// Launch a New Map dialog
					launchNewMapDialog();
				}
			}
		}

		// Grab the mouse on a click
		if (Mouse.isButtonDown(0))
		{
			Mouse.setGrabbed(true);
		}

		// Handle a requested window close
		if (Display.isCloseRequested())
		{
			done = true;
		}

		// and finally, check to see if we should be jumping to a new position
		if (this.jump_dialog_trigger)
		{
			moveCameraToArbitraryPosition();
		}
	}

	/**
	 * Switch between dimensions (going "up")
	 */
	private void switchDimension()
	{
		this.switchDimension(1);
	}

	/**
	 * If we can, switch between dimensions.  This will attempt to do an
	 * approximate translation of your position,  when going to/from the Nether,
	 * though it won't totally line up with what Minecraft does. Note that
	 * height is unaffected by this, so the adjacent portal might show up higher
	 * or lower, depending on the local terrain.
	 *
	 * Any unknown dimension will be assumed to be at overworld scale
	 *
	 * @param count How many dimensions to switch; should really just be -1 or 1
	 */
	private void switchDimension(int count)
	{
		// First get our list of dimensions and find out what
		// some important indexes are
		ArrayList<WorldInfo> dims = world.getAllDimensions();
		int cur_dim_idx = -1;
		int overworld_idx = -1;
		int change_idx = -1;
		for (int i=0; i<dims.size(); i++)
		{
			if (dims.get(i).getDimension() == world.getDimension())
			{
				cur_dim_idx = i;
			}
			if (dims.get(i).getDimension() == 0)
			{
				overworld_idx = i;
			}
		}

		// Increment by one, or default to overworld if our current
		// dimension couldn't be found.
		if (cur_dim_idx == -1)
		{
			change_idx = overworld_idx;
		}
		else
		{
			change_idx = (cur_dim_idx + dims.size() + count) % dims.size();
		}

		// If our change dimension is the same as our current one, do nothing.
		WorldInfo newworld = dims.get(change_idx);
		if (newworld.getDimension() == world.getDimension())
		{
			return;
		}

		// Now, do the actual change.
		float camera_mult = 1.0f;
		if (world.isDimension(-1))
		{
			camera_mult = 8.0f;
			this.cameraTextOverride = "equivalent " + newworld.getDimensionDesc() + " location (approx.)";
		}
		else if (newworld.isDimension(-1))
		{
			camera_mult = 1.0f / 8.0f;
			this.cameraTextOverride = "equivalent " + newworld.getDimensionDesc() + " location (approx.)";
		}
		else
		{
			this.cameraTextOverride = newworld.getDimensionDesc() + " (same location)";
		}

		// A full reinitialization is kind of overkill, but whatever.
		FirstPersonCameraController cur_camera = this.camera;
		this.camera.processNetherWarp(camera_mult);
		this.prepareNewWorld();
		this.setMinecraftWorld(newworld, cur_camera);
		this.updateRenderDetails();
		this.triggerChunkLoads();
	}

	private void invalidateSelectedChunks()
	{
		level.invalidateSelected(false);
	}

	private void invalidateSelectedChunks(boolean main_dirty)
	{
		level.invalidateSelected(main_dirty);
	}

	private void setLightMode(boolean lightMode)
	{
		this.lightMode = lightMode;
		if (lightMode)
		{
			GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black Background
			GL11.glEnable(GL11.GL_FOG);
		}
		else
		{
			GL11.glClearColor(0.0f, 0.3f, 1.0f, 0.3f); // Blue Background
			GL11.glDisable(GL11.GL_FOG);
		}
	}

	/***
	 * Switches full screen mode
	 */
	private void switchFullScreenMode()
	{
		fullscreen = !fullscreen;
		try
		{
			Display.setFullscreen(fullscreen);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/***
	 * Draw the spawn position to the minimap
	 */
	private void drawSpawnMarkerToMinimap()
	{
		Graphics2D g = minimapGraphics;

		CameraPreset spawn = level.getSpawnPoint();
		int sy = getMinimapBaseY(spawn.block.cz) + (spawn.block.x % 16);
		int sx = (getMinimapBaseX(spawn.block.cx) + (spawn.block.z % 16)) % minimap_dim;

		g.setStroke(new BasicStroke(2));
		g.setColor(Color.red.brighter());
		g.drawOval(sx - 6, sy - 6, 11, 11);
		g.drawLine(sx - 8, sy, sx + 8, sy);
		g.drawLine(sx, sy - 8, sx, sy + 8);
		minimapTexture.update();
	}

	/***
	 * Draw the current position to the minimap
	 */
	private void drawPlayerposMarkerToMinimap()
	{
		Graphics2D g = minimapGraphics;

		CameraPreset player = level.getPlayerPosition();
		int py = getMinimapBaseY(player.block.cz) + (player.block.x % 16);
		int px = getMinimapBaseX(player.block.cx) + (player.block.z % 16);

		g.setStroke(new BasicStroke(2));
		g.setColor(Color.yellow.brighter());
		g.drawOval(px - 6, py - 6, 11, 11);
		g.drawLine(px - 8, py, px + 8, py);
		g.drawLine(px, py - 8, px, py + 8);
		minimapTexture.update();
	}

	/***
	 * Main render loop
	 * 
	 * @param timeDelta
	 * @return
	 */
	private boolean render(float timeDelta)
	{
		// GL11.glLoadIdentity();
		GL11.glLoadIdentity();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer

		// are we still loading the map?
		if (!map_load_started)
		{
			map_load_started = true;
			// drawMapMarkersToMinimap();
			// minimapTexture.update();
			setLightMode(lightMode); // basically enable fog etc
		}

		// we are viewing a world
		GL11.glPushMatrix();

		// change the camera to point a the right direction
		camera.applyCameraTransformation();

		currentCameraPosX = -camera.getPosition().x;
		currentCameraPosZ = -camera.getPosition().z;
		int tempX = (int)(currentCameraPosX-.5f);
		int tempZ = (int)(currentCameraPosZ-.5f);

		// determine if we need to load new map chunks
		if (tempX != levelBlockX || tempZ != levelBlockZ || needToReloadWorld)
		{
			levelBlockX = tempX;
			levelBlockZ = tempZ;
			currentLevelX = level.getChunkX(levelBlockX);
			currentLevelZ = level.getChunkZ(levelBlockZ);
		}

		// draw the visible world
		int chunk_range = visible_chunk_range;
		if (HIGHLIGHT_RANGES[currentHighlightDistance] < chunk_range)
		{
			chunk_range = HIGHLIGHT_RANGES[currentHighlightDistance];
		}

		// Get a list of chunks that we'll iterate over, on our various passes
		ArrayList<Chunk> chunkList = new ArrayList<Chunk>();
		Chunk curChunk = null;
		for (int lx = currentLevelX - visible_chunk_range; lx < currentLevelX + visible_chunk_range; lx++)
		{
			for (int lz = currentLevelZ - visible_chunk_range; lz < currentLevelZ + visible_chunk_range; lz++)
			{
				Chunk k = level.getChunk(lx, lz);
				if (k != null)
				{
					chunkList.add(k);
					if (lx == currentLevelX && lz == currentLevelZ)
					{
						curChunk = k;
					}
				}
			}
		}

		// Now do various passes
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		int last_tex = -1;
		int i;

		// May as well render paintings first; they'll be "behind" everything then.
		for (Chunk k : chunkList)
		{
			if (k.hasPaintings())
			{
				if (last_tex != -2)
				{
					paintingTexture.bind();
				}
				k.renderPaintings();
				last_tex = -2;
			}
		}

		// Now our regular blocks
		for (i=0; i<this.minecraftTextures.size(); i++)
		{
			for (Chunk k : chunkList)
			{
				if (k.usesSheet(i))
				{
					if (last_tex != i)
					{
						minecraftTextures.get(i).bind();
						last_tex = i;
					}
					k.renderSolid(i);
					if (toggle.highlightOres == HIGHLIGHT_TYPE.OFF)
					{
						k.renderSelected(i, this.mineralToggle);
					}
				}
			}
		}

		// Now nonstandard blocks
		for (i=0; i<this.minecraftTextures.size(); i++)
		{
			for (Chunk k : chunkList)
			{
				if (k.usesSheet(i))
				{
					if (last_tex != i)
					{
						minecraftTextures.get(i).bind();
						last_tex = i;
					}
					k.renderNonstandard(i);
				}
			}
		}

		// Now Glass, or anything else we consider glassy
		for (i=0; i<this.minecraftTextures.size(); i++)
		{
			for (Chunk k : chunkList)
			{
				if (k.usesSheet(i))
				{
					if (last_tex != i)
					{
						minecraftTextures.get(i).bind();
						last_tex = i;
					}
					k.renderGlass(i);
				}
			}
		}

		// Slime chunks!
		if (world.isDimension(0) && renderSlimeChunks)
		{
			for (Chunk k : chunkList)
			{
				if (k.willSpawnSlimes)
				{
					slimeChunkTexture.bind();
					k.renderSlimeBox();
					last_tex = -1;
				}
			}
		}

		// Now chunk borders
		if (renderChunkBorders && curChunk != null)
		{
			chunkBorderTexture.bind();
			curChunk.renderBorder();
			last_tex = -1;
		}

		// And now, if we're highlighting ores, highlight them.
		if (toggle.highlightOres != HIGHLIGHT_TYPE.OFF)
		{

			GL11.glDisable(GL11.GL_DEPTH_TEST);
			switch (toggle.highlightOres)
			{
			// Old-style; at least one person prefers it
			case WHITE:
				long time = System.currentTimeMillis();
				float alpha = (time % 1000) / 1000.0f;
				if (time % 2000 > 1000)
				{
					alpha = 1.0f - alpha;
				}
				alpha = 0.1f + (alpha * 0.8f);
				GL11.glColor4f(alpha, alpha, alpha, alpha); 
				break;

				// New style disco-y highlighting
			case DISCO:
				float timeidx = (System.currentTimeMillis() % 1000) * 6.28318f / 1000.0f;
				float red = (float)Math.sin(timeidx)*.5f+.5f;
				float green = (float)Math.sin(timeidx+2.09439f)*.5f+.5f;
				float blue = (float)Math.sin(timeidx+4.18878f)*.5f+.5f;
				GL11.glColor4f(red, green, blue, 1f);
				break;
			}
			setLightLevel(20);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			// TODO: could check for specific textures here, rather than looping over all
			for (i=0; i<this.minecraftTextures.size(); i++)
			{
				for (Chunk k : chunkList)
				{
					if (k.usesSheet(i))
					{
						if (last_tex != i)
						{
							minecraftTextures.get(i).bind();
							last_tex = i;
						}
						k.renderSelected(i, this.mineralToggle);
					}
				}
			}
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}

		setLightLevel();

		GL11.glPopMatrix();

		// draw the user interface (fps and map)
		drawUI();

		return true;
	}

	/***
	 * Draw the ui
	 */
	private void drawUI()
	{
		framesSinceLastFps++;

		setOrthoOn(); // 2d mode

		drawMinimap();
		drawFPSCounter();
		drawMineralToggle();
		if (levelInfoToggle)
		{
			drawLevelInfo();
		}
		if (renderDetailsToggle)
		{
			drawRenderDetails();
		}

		setOrthoOff(); // back to 3d mode
	}

	private void updateLevelInfo()
	{
		int labelX = 5;
		int valueX = 70;
		Graphics2D g = levelInfoTexture.getImage().createGraphics();
		g.setBackground(Color.BLUE);
		g.clearRect(0, 0, 128, levelInfoTexture_h);
		g.setColor(Color.WHITE);
		g.fillRect(2, 2, 124, levelInfoTexture_h - 4);
		g.setFont(ARIALFONT);
		int chunkX = level.getChunkX(levelBlockX);
		int chunkZ = level.getChunkZ(levelBlockZ);
		g.setColor(Color.BLACK);
		g.drawString("Chunk X:", labelX, 22);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(chunkX), valueX, 22);

		g.setColor(Color.BLACK);
		g.drawString("Chunk Z:", labelX, 22 + 16);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(chunkZ), valueX, 22 + 16);

		g.setColor(Color.BLACK);
		g.drawString("World X:", labelX, 22 + 32);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(levelBlockX), valueX, 22 + 32);

		g.setColor(Color.BLACK);
		g.drawString("World Z:", labelX, 22 + 16 + 32);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(levelBlockZ), valueX, 22 + 16 + 32);

		g.setColor(Color.BLACK);
		g.drawString("World Y:", labelX, 22 + 16 + 32 + 16);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString((int) (-camera.getPosition().y-.5f)), valueX, 22 + 16 + 32 + 16);

		long heapSize = Runtime.getRuntime().totalMemory();
		g.setColor(Color.BLACK);
		g.drawString("Memory Used", labelX, 22 + 16 + 32 + 16 + 25);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString((int) (heapSize / 1024 / 1024)) + " MB", 20, 22 + 16 + 32 + 16 + 25 + 20);

		levelInfoTexture.update();
	}

	/**
	 * Renders a text label in an info box, with differing fonts/colors for the
	 * label and its value
	 * 
	 * @param g
	 *            Graphics context to render to
	 * @param x
	 *            Baseline x offset for the label
	 * @param y
	 *            Baseline y offset for the label
	 * @param label
	 *            The label to draw
	 * @param labelColor
	 *            Label color
	 * @param labelFont
	 *            Label font
	 * @param value
	 *            The value
	 * @param valueColor
	 *            Value color
	 * @param valueFont
	 *            Value font
	 */
	private void infoboxTextLabel(Graphics2D g, int x, int y, String label, Color labelColor, Font labelFont, String value, Color valueColor, Font valueFont)
	{
		Rectangle2D bounds = labelFont.getStringBounds(label, g.getFontRenderContext());
		g.setColor(labelColor);
		g.setFont(labelFont);
		g.drawString(label, x, y);
		g.setColor(valueColor);
		g.setFont(valueFont);
		g.drawString(value, (int) (x + bounds.getWidth()), y);
	}


	/**
	 * Renders a note in the info box, centered.  Note that for the centering, this routine
	 * will assume a border of "x" on each side, so it'll only work properly if the infobox
	 * is right at the edge of the screen.
	 *
	 * 
	 * @param g
	 *            Graphics context to render to
	 * @param x
	 *            Baseline x offset for the label
	 * @param y
	 *            Baseline y offset for the label
	 * @param label
	 *            The label to draw
	 * @param labelColor
	 *            Label color
	 * @param labelFont
	 *            Label font
	 */
	private void infoboxTextNote(Graphics2D g, int x, int y, String label, Color labelColor, Font labelFont)
	{
		Rectangle2D bounds = labelFont.getStringBounds(label, g.getFontRenderContext());
		g.setColor(labelColor);
		g.setFont(labelFont);
		g.drawString(label, x + (int)((renderDetails_w - (x*2) - bounds.getWidth())/2), y);
	}

	/**
	 * Renders a slider-type graphic in an info box, including its label
	 * 
	 * @param g
	 *            Graphics context to render to
	 * @param x
	 *            Baseline X offset for the label
	 * @param y
	 *            Baseline Y offset for the label
	 * @param label
	 *            The label
	 * @param labelColor
	 *            Label color
	 * @param labelFont
	 *            Label font
	 * @param line_h
	 *            How tall our individual lines are
	 * @param slider_start_x
	 *            X offset to start the slider at
	 * @param curval
	 *            Current value of slider
	 * @param val_length
	 *            Length of slider data (array length, for us)
	 */
	private void infoboxSlider(Graphics2D g, int x, int y, String label, Color labelColor, Font labelFont, int line_h, int slider_start_x, int curval, int val_length)
	{
		int slider_top_y = y - line_h + 10;
		int slider_h = 8;
		int slider_end_x = renderDetails_w - 8;
		int marker_x = slider_start_x + (curval * ((slider_end_x - slider_start_x) / (val_length - 1)));

		// Label
		g.setColor(labelColor);
		g.setFont(labelFont);
		g.drawString(label, x, y);

		// Slider Base
		g.setColor(Color.BLACK);
		g.drawRect(slider_start_x, slider_top_y, slider_end_x - slider_start_x, slider_h);

		// Slider Location
		g.setColor(Color.RED);
		g.fillRect(marker_x, y - line_h + 8, 3, 13);
	}

	/**
	 * Update our render-details infobox
	 */
	private void updateRenderDetails()
	{
		int line_h = 20;
		int x_off = 5;
		int line_count = 0;
		Graphics2D g = renderDetailsTexture.getImage().createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, renderDetails_w, renderDetailsTexture.getTextureWidth());
		g.setFont(DETAILFONT);
		g.setColor(Color.BLACK);
		if (!lightMode)
		{
			line_count++;
			infoboxTextLabel(g, x_off, line_count * line_h, "Fullbright: ", Color.BLACK, DETAILFONT, "On", Color.GREEN.darker(), DETAILVALUEFONT);
		}
		else
		{
			line_count++;
			infoboxSlider(g, x_off, line_count * line_h, "Light Level:", Color.BLACK, DETAILFONT, line_h, 90, currentLightLevel, lightLevelEnd.length);
		}
		line_count++;
		infoboxSlider(g, x_off, line_count * line_h, "Render Dist:", Color.BLACK, DETAILFONT, line_h, 90, currentChunkRange, CHUNK_RANGES.length);
		line_count++;
		infoboxSlider(g, x_off, line_count * line_h, "Highlight Dist:", Color.BLACK, DETAILFONT, line_h, 90, currentHighlightDistance, HIGHLIGHT_RANGES.length);
		line_count++;
		infoboxTextLabel(g, x_off, line_count * line_h, "Ore Highlight: ", Color.BLACK, DETAILFONT, toggle.highlightOres.reportText, toggle.highlightOres.reportColor, DETAILVALUEFONT);
		if (toggle.highlight_explored)
		{
			line_count++;
			infoboxTextLabel(g, x_off, line_count * line_h, "Explored Highlight: ", Color.BLACK, DETAILFONT, "On", Color.GREEN.darker(), DETAILVALUEFONT);
		}
		if (toggle.render_bedrock)
		{
			line_count++;
			infoboxTextLabel(g, x_off, line_count * line_h, "Bedrock: ", Color.BLACK, DETAILFONT, "On", Color.GREEN.darker(), DETAILVALUEFONT);
		}
		if (!toggle.render_water)
		{
			line_count++;
			infoboxTextLabel(g, x_off, line_count * line_h, "Water: ", Color.BLACK, DETAILFONT, "Off", Color.RED.darker(), DETAILVALUEFONT);
		}
		if (!accurateGrass)
		{
			line_count++;
			infoboxTextLabel(g, x_off, line_count * line_h, "Grass: ", Color.BLACK, DETAILFONT, "Inaccurate", Color.RED.darker(), DETAILVALUEFONT);
		}
		if (camera_lock)
		{
			line_count++;
			infoboxTextLabel(g, x_off, line_count * line_h, "Vertical Lock: ", Color.BLACK, DETAILFONT, "On", Color.green.darker(), DETAILVALUEFONT);
		}
		if (!toggle.beta19_fences)
		{
			line_count++;
			infoboxTextLabel(g, x_off, line_count * line_h, "\"New\" Fences: ", Color.BLACK, DETAILFONT, "Off", Color.green.darker(), DETAILVALUEFONT);
		}
		if (!silverfishHighlight)
		{
			line_count++;
			infoboxTextLabel(g, x_off, line_count * line_h, "Silverfish Highlight: ", Color.BLACK, DETAILFONT, "Off", Color.green.darker(), DETAILVALUEFONT);
		}
		if (renderSlimeChunks)
		{
			line_count++;
			infoboxTextLabel(g, x_off, line_count * line_h, "Slime Chunks: ", Color.BLACK, DETAILFONT, "On", Color.green.darker(), DETAILVALUEFONT);
		}

		// Add a note about our keyboard reference, since we have that now.
		line_count++;
		infoboxTextNote(g, x_off, line_count * line_h, "Keyboard Reference: " + Keyboard.getKeyName(key_mapping.get(KEY_ACTION.KEY_HELP)), Color.BLACK, SMALLFONT);

		cur_renderDetails_h = (line_count + 1) * line_h - 8;
		g.setColor(Color.BLUE);
		g.setStroke(new BasicStroke(2));
		g.drawRect(1, 1, renderDetails_w - 2, cur_renderDetails_h - 2);
		renderDetailsTexture.update();

		this.regenerateRenderDetailsTexture = false;
	}

	/***
	 * Draws our level info dialog to the screen
	 */
	private void drawLevelInfo()
	{
		int y = 48;
		if (renderDetailsToggle)
		{
			y += cur_renderDetails_h + 16;
		}
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		levelInfoTexture.bind();
		SpriteTool.drawCurrentSprite(0, y, 128, levelInfoTexture_h, 0, 0, 1f, levelInfoTexture_h / 256f);
	}

	/***
	 * Draws our rendering details infobox to the screen
	 */
	private void drawRenderDetails()
	{
		renderDetailsTexture.bind();
		GL11.glColor4f(1.0f, 1.0f, 1.0f, .7f);
		SpriteTool.drawCurrentSprite(0, 48, renderDetails_w, cur_renderDetails_h, 0, 0, renderDetails_w / 256f, cur_renderDetails_h / 256f);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1f);
	}

	/**
	 * Draws a 2d GL box over which we can show some info which might be
	 * difficult to make out otherwise (used for our ore highlights,
	 * "loading" messages, etc).
	 *
	 * @param bgX X coordinate to draw to
	 * @param bgY Y coordinate to draw to
	 * @param bgWidth Width of the box
	 * @param bgHeight Height of the box
	 */
	private void drawBgBox(float bgX, float bgY, float bgWidth, float bgHeight)
	{
		GL11.glColor4f(0f, 0f, 0f, .6f);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPushMatrix();
		GL11.glTranslatef(bgX, bgY, 0.0f);
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		GL11.glVertex2f(0, 0);
		GL11.glVertex2f(bgWidth, 0);
		GL11.glVertex2f(0, bgHeight);
		GL11.glVertex2f(bgWidth, bgHeight);
		GL11.glEnd();
		GL11.glColor4f(.4f, .4f, .4f, .9f);
		GL11.glLineWidth(2);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2f(0, 0);
		GL11.glVertex2f(bgWidth, 0);
		GL11.glVertex2f(bgWidth, bgHeight);
		GL11.glVertex2f(0, bgHeight);
		GL11.glEnd();
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	/***
	 * Draw the mineral toggles
	 */
	private void drawMineralToggle()
	{
		int barWidth = 128 + 10 + 32;
		int barHeight = 42;
		int maxCols = 5;
		float mineralTogglebarLength;
		if ((mineralToggleTextures.length % maxCols) == 0)
		{
			mineralTogglebarLength = maxCols * barWidth;
		}
		else
		{
			mineralTogglebarLength = (mineralToggleTextures.length % maxCols) * barWidth;
		}
		float curX = (screenWidth / 2.0f) - (mineralTogglebarLength / 2.0f);
		float curY = screenHeight - barHeight;
		if (mineralToggleTextures.length > maxCols)
		{
			curY -= barHeight;
		}

		// These are hard to read if we don't put a box behind 'em
		float bgPadding = 10f;
		this.drawBgBox(curX - bgPadding,
				curY - bgPadding,
				mineralTogglebarLength + (2f*bgPadding),
				(barHeight * 2f) + bgPadding);

		for (int i = 0; i < mineralToggleTextures.length; i++)
		{
			if (i == mineralToggleTextures.length - maxCols)
			{
				mineralTogglebarLength = maxCols * barWidth;
				curY += barHeight;
				curX = (screenWidth / 2.0f) - (mineralTogglebarLength / 2.0f);
			}
			if (mineralToggle[i])
			{
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				SpriteTool.drawCurrentSprite(curX - 2, curY - 2, 36, 36, MinecraftConstants.precalcSpriteSheetToTextureX[blockArray[HIGHLIGHT_ORES[i]].tex_idx],
						MinecraftConstants.precalcSpriteSheetToTextureY[blockArray[HIGHLIGHT_ORES[i]].tex_idx],
						MinecraftConstants.precalcSpriteSheetToTextureX[blockArray[HIGHLIGHT_ORES[i]].tex_idx] + TEX16,
						MinecraftConstants.precalcSpriteSheetToTextureY[blockArray[HIGHLIGHT_ORES[i]].tex_idx] + TEX32);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
			}
			else
			{
				GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
			}
			// TODO: should really fix this up so that we minimize binds
			minecraftTextures.get(blockArray[HIGHLIGHT_ORES[i]].getTexSheet()).bind();
			SpriteTool.drawCurrentSprite(curX, curY, 32, 32, MinecraftConstants.precalcSpriteSheetToTextureX[blockArray[HIGHLIGHT_ORES[i]].tex_idx],
					MinecraftConstants.precalcSpriteSheetToTextureY[blockArray[HIGHLIGHT_ORES[i]].tex_idx],
					MinecraftConstants.precalcSpriteSheetToTextureX[blockArray[HIGHLIGHT_ORES[i]].tex_idx] + TEX16,
					MinecraftConstants.precalcSpriteSheetToTextureY[blockArray[HIGHLIGHT_ORES[i]].tex_idx] + TEX32);

			SpriteTool.drawSpriteAbsoluteXY(mineralToggleTextures[i], curX + 32 + 10, curY + 7);
			curX += barWidth;
		}
	}

	/***
	 * Draws a simple fps counter on the top-left of the screen
	 *
	 * TODO: rather than do our thing with lastFpsTime, etc, compare the values that
	 * we'll be showing, and update whenever we need to (so we get immediate feeback,
	 * instead of delayed feedback)
	 */
	private void drawFPSCounter()
	{
		previousTime = time;
		time = System.nanoTime();
		timeDelta = time - previousTime;

		if (time - lastFpsTime > NANOSPERSECOND)
		{
			fps = framesSinceLastFps;
			framesSinceLastFps = 0;
			lastFpsTime = time;
			updateFPSText = true;
		}
		if (updateFPSText)
		{
			if (levelInfoToggle)
				updateLevelInfo();
			Graphics2D g = fpsTexture.getImage().createGraphics();
			g.setBackground(Color.BLUE);
			g.clearRect(0, 0, 128, 32);
			g.setColor(Color.WHITE);
			g.fillRect(2, 2, 124, 28);
			g.setColor(Color.BLACK);
			g.setFont(ARIALFONT);
			g.drawString("FPS:", 10, 22);
			g.setColor(Color.RED.darker());
			g.drawString(Integer.toString(fps), 60, 22);

			fpsTexture.update();

			updateFPSText = false;
		}

		fpsTexture.bind();

		GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
		SpriteTool.drawSpriteAbsoluteXY(fpsTexture, 0, 0);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1f);
	}

	/***
	 * Sets ortho (2d) mode
	 */
	public void setOrthoOn()
	{
		// prepare projection matrix to render in 2D
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix(); // preserve perspective view
		GL11.glLoadIdentity(); // clear the perspective matrix
		GL11.glOrtho( // turn on 2D mode
				// //viewportX,viewportX+viewportW, // left, right
				// //viewportY,viewportY+viewportH, // bottom, top !!!
				0, screenWidth, // left, right
				screenHeight, 0, // bottom, top
				-500, 500); // Zfar, Znear
		// clear the modelview matrix
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix(); // Preserve the Modelview Matrix
		GL11.glLoadIdentity(); // clear the Modelview Matrix
		// disable depth test so further drawing will go over the current scene
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	/**
	 * Restore the previous mode
	 */
	public static void setOrthoOff()
	{
		// restore the original positions and views
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
		// turn Depth Testing back on
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	/***
	 * draws the minimap or the big map
	 */
	private void drawMinimap()
	{
		if (mapBig)
		{
			// the big map
			// just draws the texture, but move the texture so the middle of the
			// screen is where we currently are

			minimapTexture.bind();

			float vSizeFactor = .5f;

			float vTexX = (1.0f / minimap_dim_f) * currentCameraPosX;
			float vTexY = (1.0f / minimap_dim_f) * currentCameraPosZ;
			float vTexZ = vSizeFactor;

			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
			GL11.glPushMatrix();
			GL11.glTranslatef((screenWidth / 2.0f), (screenHeight / 2.0f), 0.0f);
			GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

			GL11.glTexCoord2f(vTexX - vTexZ, vTexY - vTexZ);
			GL11.glVertex2f(-minimap_dim_h_f, -minimap_dim_h_f);

			GL11.glTexCoord2f(vTexX + vTexZ, vTexY - vTexZ);
			GL11.glVertex2f(+minimap_dim_h_f, -minimap_dim_h_f);

			GL11.glTexCoord2f(vTexX - vTexZ, vTexY + vTexZ);
			GL11.glVertex2f(-minimap_dim_h_f, +minimap_dim_h_f);

			GL11.glTexCoord2f(vTexX + vTexZ, vTexY + vTexZ);
			GL11.glVertex2f(+minimap_dim_h_f, +minimap_dim_h_f);

			GL11.glEnd();
			GL11.glPopMatrix();
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1f);

			//SpriteTool.drawSpriteAndRotateAndScale(minimapArrowTexture, screenWidth / 2.0f, screenHeight / 2.0f, camera.getYaw() + 90, 0.5f);
			SpriteTool.drawSpriteAndRotateAndScale(minimapArrowTexture, screenWidth / 2.0f, screenHeight / 2.0f, camera.getYaw(), 0.5f);
		}
		else
		{
			// the minimap
			// I set the minimap to 200 wide and tall

			// Interestingly, thanks to the fact that we're using GL11.GL_REPEAT on our
			// textures (via glTexParameter), we don't have to worry about checking
			// bounds here, etc. Or in other words, our map will automatically wrap for
			// us. Sweet!
			float vSizeFactor = 200.0f / minimap_dim_f;

			float vTexX = (1.0f / minimap_dim_f) * currentCameraPosX;
			float vTexY = (1.0f / minimap_dim_f) * currentCameraPosZ;
			float vTexZ = vSizeFactor;

			minimapTexture.bind();
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
			GL11.glPushMatrix();
			GL11.glTranslatef(screenWidth - 100, 100, 0.0f);
			GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

			GL11.glTexCoord2f(vTexX - vTexZ, vTexY - vTexZ);
			GL11.glVertex2f(-100, -100);

			GL11.glTexCoord2f(vTexX + vTexZ, vTexY - vTexZ);
			GL11.glVertex2f(+100, -100);

			GL11.glTexCoord2f(vTexX - vTexZ, vTexY + vTexZ);
			GL11.glVertex2f(-100, +100);

			GL11.glTexCoord2f(vTexX + vTexZ, vTexY + vTexZ);
			GL11.glVertex2f(+100, +100);

			GL11.glEnd();
			GL11.glPopMatrix();
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1f);

			//SpriteTool.drawSpriteAndRotateAndScale(minimapArrowTexture, screenWidth - 100, 100, camera.getYaw() + 90, 0.5f);
			SpriteTool.drawSpriteAndRotateAndScale(minimapArrowTexture, screenWidth - 100, 100, camera.getYaw(), 0.5f);
		}
	}

	/**
	 * Returns the "base" minimap X coordinate, given chunk coordinate X. The
	 * "base" will be the upper left corner.
	 * 
	 * As of Beta 1.9-pre4, X increases to the east (decreasing to the west),
	 * and Z increases to the South (decreasing to the North).  This is much nicer
	 * to deal with then the way we were pretending things worked previously.
	 * 
	 * @param chunkZ
	 * @return
	 */
	private int getMinimapBaseX(int chunkX)
	{
		return (((chunkX*16) % minimap_dim) + minimap_dim) % minimap_dim;
	}

	/**
	 * Returns the "base" minimap Y coordinate, given chunk coordinate Z. The
	 * "base" will be the upper left corner.
	 * 
	 * As of Beta 1.9-pre4, X increases to the east (decreasing to the west),
	 * and Z increases to the South (decreasing to the North).  This is much nicer
	 * to deal with then the way we were pretending things worked previously.
	 * 
	 * @param chunkZ
	 * @return
	 */
	private int getMinimapBaseY(int chunkZ)
	{
		return (((chunkZ*16) % minimap_dim) + minimap_dim) % minimap_dim;
	}

	/**
	 * Clears out the area on the minimap belonging to this chunk
	 * 
	 * @param x
	 * @param z
	 */
	public void removeMapChunkFromMap(int x, int z)
	{
		// minimapGraphics.setColor(new Color(0f, 0f, 0f, 1f));
		// minimapGraphics.setComposite(AlphaComposite.Src);
		minimapGraphics.fillRect(getMinimapBaseX(x), getMinimapBaseY(z), 16, 16);
		level.getChunk(x, z).isOnMinimap = false;
	}

	/**
	 * Loops through a list of chunks and removes them from the minimap
	 * 
	 * @param trimList
	 */
	private void removeChunklistFromMap(ArrayList<Chunk> trimList)
	{
		minimapGraphics.setColor(new Color(0f, 0f, 0f, 0f));
		minimapGraphics.setComposite(AlphaComposite.Src);
		boolean minimap_changed = false;
		for (Chunk tempchunk_trim : trimList)
		{
			removeMapChunkFromMap(tempchunk_trim.x, tempchunk_trim.z);
			minimap_changed = true;
		}
		if (minimap_changed)
		{
			minimapTexture.update();
		}
	}

	/***
	 * draws a chunk to the (mini) map
	 * 
	 * @param x
	 * @param z
	 */
	public void drawChunkToMap(int x, int z)
	{

		Chunk c = level.getChunk(x, z);
		if (c != null)
		{
			c.isOnMinimap = true;
		}
		short[] chunkData = level.getChunkData(x, z);

		int base_x = getMinimapBaseX(x);
		int base_y = getMinimapBaseY(z);

		boolean in_nether = world.isDimension(-1);
		boolean found_air;
		boolean found_solid;
		boolean drew_block;

		Color blockColor;

		Graphics2D g = minimapGraphics;
		for (int zz = 0; zz < 16; zz++)
		{
			for (int xx = 0; xx < 16; xx++)
			{
				// determine the top most visible block
				found_air = !in_nether;
				drew_block = false;
				found_solid = false;
				for (int yy = 127; yy >= 0; yy--)
				{
					int blockOffset = yy + (zz * 128) + (xx * 128 * 16);
					short blockData = chunkData[blockOffset];

					if (blockData > 0)
					{
						if (in_nether && !found_solid)
						{
							found_air = false;
						}
						found_solid = true;
						if (found_air)
						{
							if (MinecraftConstants.blockArray[blockData] == null)
							{
								blockColor = BLOCK_UNKNOWN.color;
							}
							else
							{
								blockColor = MinecraftConstants.blockArray[blockData].color;
							}
							if (blockColor != null)
							{
								// Previously we were using g.drawLine() here, but a minute-or-so's worth of investigating
								// didn't uncover a way to force that to be pixel-precise (the color would often bleed over
								// into adjoining pixels), so we're using g.fillRect() instead, which actually looks like it
								// is probably a faster operation anyway. I'm sure there'd have been a way to get drawLine
								// to behave, but c'est la vie!
								g.setColor(blockColor);
								g.fillRect(base_x + xx, base_y + zz, 1, 1);
							}
							drew_block = true;
							break;
						}
					}
					else
					{
						found_air = true;
					}
				}

				// Make sure we don't have holes in our Nether minimap
				if (in_nether && found_solid && !drew_block)
				{
					g.setColor(MinecraftConstants.BLOCK_BEDROCK.color);
					g.fillRect(base_x + xx, base_y + xx, 1, 1);
				}
			}
		}
	}

	/***
	 * Draws the minimap sprites (currently just the arrow image) to their
	 * textures
	 */
	private void createMinimapSprites()
	{

		// First the arrow
		Graphics2D g = minimapArrowTexture.getImage().createGraphics();
		g.setColor(Color.red);
		g.setStroke(new BasicStroke(5));
		g.drawLine(3, 16, 30, 24);
		g.drawLine(30, 24, 30, 8);
		g.drawLine(30, 8, 3, 16);
		minimapArrowTexture.update();
	}

	/**
	 * Returns our camera object
	 */
	public FirstPersonCameraController getCamera()
	{
		return camera;
	}

	/**
	 * Saves our current option states to our properties file.
	 */
	private void saveOptionStates()
	{
		xray_properties.setBooleanProperty("STATE_BEDROCK", toggle.render_bedrock);
		xray_properties.setBooleanProperty("STATE_WATER", toggle.render_water);
		xray_properties.setBooleanProperty("STATE_EXPLORED", toggle.highlight_explored);
		xray_properties.setBooleanProperty("STATE_BETA19_FENCES", toggle.beta19_fences);
		xray_properties.setProperty("STATE_HIGHLIGHT_ORES", toggle.highlightOres.toString());
		xray_properties.setBooleanProperty("STATE_CAMERA_LOCK", camera_lock);
		xray_properties.setBooleanProperty("STATE_LIGHTING", lightMode);
		xray_properties.setBooleanProperty("STATE_LEVEL_INFO", levelInfoToggle);
		xray_properties.setBooleanProperty("STATE_RENDER_DETAILS", renderDetailsToggle);
		xray_properties.setBooleanProperty("STATE_ACCURATE_GRASS", accurateGrass);
		xray_properties.setBooleanProperty("STATE_SILVERFISH_HIGHLIGHT", silverfishHighlight);
		xray_properties.setBooleanProperty("STATE_CHUNK_BORDERS", renderChunkBorders);
		xray_properties.setBooleanProperty("STATE_SLIME_CHUNKS", renderSlimeChunks);
		xray_properties.setIntProperty("STATE_CHUNK_RANGE", currentChunkRange);
		xray_properties.setIntProperty("STATE_HIGHLIGHT_DISTANCE", currentHighlightDistance);
		xray_properties.setIntProperty("STATE_LIGHT_LEVEL", currentLightLevel);
		for (int i=0; i<mineralToggle.length; i++)
		{
			xray_properties.setBooleanProperty("STATE_HIGHLIGHT_" + i, mineralToggle[i]);
		}
		savePreferences();
	}

	/**
	 * Loads our option states from the properties object.
	 */
	private void loadOptionStates()
	{
		toggle.render_bedrock = xray_properties.getBooleanProperty("STATE_BEDROCK", toggle.render_bedrock);
		toggle.render_water = xray_properties.getBooleanProperty("STATE_WATER", toggle.render_water);
		toggle.highlight_explored = xray_properties.getBooleanProperty("STATE_EXPLORED", toggle.highlight_explored);
		toggle.beta19_fences = xray_properties.getBooleanProperty("STATE_BETA19_FENCES", toggle.beta19_fences);
		String highlight = xray_properties.getProperty("STATE_HIGHLIGHT_ORES");
		if (highlight == null || highlight.equals("1"))
		{
			toggle.highlightOres = defaultHighlightOre;
		}
		else if (highlight.equals("0"))
		{
			toggle.highlightOres = HIGHLIGHT_TYPE.OFF;
		}
		else
		{
			try
			{
				toggle.highlightOres = Enum.valueOf(HIGHLIGHT_TYPE.class, highlight);
			}
			catch (IllegalArgumentException e)
			{
				toggle.highlightOres = defaultHighlightOre;
			}
		}
		camera_lock = xray_properties.getBooleanProperty("STATE_CAMERA_LOCK", camera_lock);
		lightMode = xray_properties.getBooleanProperty("STATE_LIGHTING", lightMode);
		levelInfoToggle = xray_properties.getBooleanProperty("STATE_LEVEL_INFO", levelInfoToggle);
		renderDetailsToggle = xray_properties.getBooleanProperty("STATE_RENDER_DETAILS", renderDetailsToggle);
		accurateGrass = xray_properties.getBooleanProperty("STATE_ACCURATE_GRASS", accurateGrass);
		silverfishHighlight = xray_properties.getBooleanProperty("STATE_SILVERFISH_HIGHLIGHT", silverfishHighlight);
		renderChunkBorders = xray_properties.getBooleanProperty("STATE_CHUNK_BORDERS", renderChunkBorders);
		renderSlimeChunks = xray_properties.getBooleanProperty("STATE_SLIME_CHUNKS", renderSlimeChunks);
		currentChunkRange = xray_properties.getIntProperty("STATE_CHUNK_RANGE", currentChunkRange);
		currentHighlightDistance = xray_properties.getIntProperty("STATE_HIGHLIGHT_DISTANCE", currentHighlightDistance);
		currentLightLevel = xray_properties.getIntProperty("STATE_LIGHT_LEVEL", currentLightLevel);
		for (int i=0; i<mineralToggle.length; i++)
		{
			mineralToggle[i] = xray_properties.getBooleanProperty("STATE_HIGHLIGHT_" + i, mineralToggle[i]);
		}
	}

	/***
	 * cleanup
	 */
	private void cleanup()
	{
		JumpDialog.closeDialog();
		KeyHelpDialog.closeDialog();
		BlockBindDialog.closeDialog();
		Display.destroy();
	}

}
