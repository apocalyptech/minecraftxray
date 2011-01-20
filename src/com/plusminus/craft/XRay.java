package com.plusminus.craft;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.plusminus.craft.WorldInfo;
import static com.plusminus.craft.MineCraftConstants.*;

public class XRay {
	// for the sprite sheet
    
	// number of chunks around the camera which are visible (Square)
	private int visible_chunk_range = 5;
	
	private static final int[] CHUNK_RANGES_KEYS = new int[6];
	private static final int[] CHUNK_RANGES = new int[] {3,4,5,6,7,8};
	private int currentChunkRange = 4;
	
	// highlight distance
	private static final int[] HIGHLIGHT_RANGES_KEYS = new int[7];
	private static final int[] HIGHLIGHT_RANGES = new int[] {2, 3, 4, 5, 6, 7, 8};
	private int currentHighlightDistance = 1;
	
	// ore highlight keys
	private static final int[] HIGHLIGHT_ORE_KEYS = new int[HIGHLIGHT_ORES.length];
	
	// By default we'll keep 20x20 chunks in our cache, which should hopefully let
	// us stay ahead of the camera
	// TODO: keep this at 8, or back up to 10?
	private final int loadChunkRange = 8;
	
	// set to true when the program is finished 
	private boolean done 				= false; 
	// are we full screen
    private boolean fullscreen 			= false; 
    // window title
    private final String app_version    = "2.7 Maintenance Branch 6";
    private final String app_name       = "Minecraft X-Ray";
    private final String windowTitle 	= app_name + " " + app_version; 

    // Minimap size - I did try increasing this but there were some performance issues
    private final int minimap_dim = 2048;
    private final float minimap_dim_f = (float)minimap_dim;
    private final int minimap_dim_h = minimap_dim/2;
    private final float minimap_dim_h_f = (float)minimap_dim_h;
    private boolean minimap_needs_updating = false;
    
    // current display mode
    private DisplayMode displayMode; 	
    
    // last system time in the main loop (to calculate delta for camera movement)
    private long lastTime;
    
    // our camera
    private FirstPersonCameraController camera;
    
    // the current mouseX and mouseY on the screen
    private int mouseX;
    private int mouseY;

    // the sprite sheet for all textures
    public Texture minecraftTexture;
    public Texture paintingTexture;
    public Texture portalTexture;
    public Texture loadingTextTexture;
    
    // the textures used by the minimap
    private Texture minimapTexture;
    private Texture minimapArrowTexture;
    private Graphics2D minimapGraphics;
    
    // Whether or not we're showing bedrock
    private boolean render_bedrock = false;
    
    // the minecraft level we are exploring
    private MinecraftLevel level;
    
    // the current block (universal coordinate) where the camera is hovering on
    private int levelBlockX, levelBlockZ;
	
	// the current and previous chunk coordinates where the camera is hovering on
	private int currentLevelX, currentLevelZ;
	
	// we render to a display list and use that later for quick drawing, this is the index
	private int worldDisplayListNum;
	private int visibleOresListNum;

	// wheter we need to reload the world
	private boolean needToReloadWorld = false;
	
	// the width and height of the current screen resolution
	private int screenWidth, screenHeight;
	
	// the current camera position
	private int currentCameraPosX;
	private int currentCameraPosZ;
	
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
	
	// the currently pressed key
	private int keyPressed = -1;
	
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
	private boolean lightMode = false;
	
	// highlight the ores by making them blink
	private boolean highlightOres = true;

	// level info texture
	private boolean levelInfoToggle;
	private Texture levelInfoTexture;
	
	// light level
	private int[] lightLevelEnd = new int[]{30,50,70,100,130};
	private int[] lightLevelStart = new int[]{0,20,30,40,60};
	private int currentLightLevel = 2;
	
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
	private long max_chunkload_time = Sys.getTimerResolution() / 10;  // a tenth of a second
	
	// The current camera position that we're at
	private CameraPreset currentPosition;
	private String cameraTextOverride = null;
	
	// Keyboard actions
	private static enum KEY_ACTIONS {
		SPEED_INCREASE,
		SPEED_DECREASE,
		MOVE_FORWARD,
		MOVE_BACKWARD,
		MOVE_LEFT,
		MOVE_RIGHT,
		MOVE_UP,
		MOVE_DOWN,
		TOGGLE_MINIMAP,
		TOGGLE_ORE_1,
		TOGGLE_ORE_2,
		TOGGLE_ORE_3,
		TOGGLE_ORE_4,
		TOGGLE_ORE_5,
		TOGGLE_ORE_6,
		TOGGLE_ORE_7,
		TOGGLE_ORE_8,
		TOGGLE_ORE_9,
		TOGGLE_ORE_10,
		TOGGLE_FULLSCREEN,
		TOGGLE_FULLBRIGHT,
		TOGGLE_ORE_HIGHLIGHTING,
		MOVE_TO_SPAWN,
		MOVE_TO_PLAYER,
		MOVE_NEXT_CAMERAPOS,
		MOVE_PREV_CAMERAPOS,
		LIGHT_INCREASE,
		LIGHT_DECREASE,
		TOGGLE_POSITION_INFO,
		TOGGLE_BEDROCK,
		SWITCH_NETHER,
		CHUNK_RANGE_1,
		CHUNK_RANGE_2,
		CHUNK_RANGE_3,
		CHUNK_RANGE_4,
		CHUNK_RANGE_5,
		CHUNK_RANGE_6,
		HIGHLIGHT_RANGE_1,
		HIGHLIGHT_RANGE_2,
		HIGHLIGHT_RANGE_3,
		HIGHLIGHT_RANGE_4,
		HIGHLIGHT_RANGE_5,
		HIGHLIGHT_RANGE_6,
		HIGHLIGHT_RANGE_7,
		RELEASE_MOUSE,
		QUIT,
	}
	
	private TreeMap<KEY_ACTIONS, Integer> default_keys;
	private TreeMap<KEY_ACTIONS, Integer> current_keys;
	private Properties xray_properties;
	
	// lets start with the program
    public static void main(String args[]) {    
        new XRay().run();
    }
    
    // go
    public void run() {
        try {
        	// check whether we can access minecraft 
        	// and if we have worlds to load
        	checkMinecraftFiles();
        	
        	// Load our preferences (this includes key mappings)
        	setKeyDefaults();
        	loadPreferences();
        	
        	// prompt for the resolution and initialize the window
        	createWindow();

        	// basic opengl initialization
            initGL();

            // init our program
            initialize();
            
            // And now load our world
            this.setMinecraftWorld(availableWorlds.get(this.selectedWorld));
            this.triggerChunkLoads();
            
            // main loop
            while (!done)
            {
            	long time = Sys.getTime();
                float timeDelta = (time - lastTime)/1000.0f;
                lastTime = time;
                
                // handle input given the timedelta (for mouse control)
                handleInput(timeDelta);
                
                // Load chunks if needed
                if (mapChunksToLoad != null)
                {
                	loadPendingChunks();
                }

                // render whatever we need to render
                render(timeDelta);
                
                // update our minimap if we need to (new chunks loaded, etc)
                if (minimap_needs_updating)
                {
                	minimapTexture.update();
                	minimap_needs_updating=false;
                }
                
                // Push to screen
                Display.update();
            }
            // cleanup
            cleanup();
        }
        catch (Exception e) {
        	// bah some error happened
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    /**
     * Loads our preferences.  This also sets our default keybindings if they're not
     * overridden somewhere.
     */
	@SuppressWarnings("unchecked")
	public void loadPreferences()
    {
		int i;
		
    	xray_properties = new Properties();
    	this.current_keys = (TreeMap<KEY_ACTIONS, Integer>)default_keys.clone();
    	
    	// Here's where we would load from our prefs file
    	//for(KEY_ACTIONS action : KEY_ACTIONS.values())
    	//{
    	//}
    	
    	// Populate our key ranges
    	for (i=0; i<CHUNK_RANGES.length; i++)
    	{
    		CHUNK_RANGES_KEYS[i] = this.current_keys.get(KEY_ACTIONS.valueOf("CHUNK_RANGE_" + (i+1)));
    	}
    	for (i=0; i<HIGHLIGHT_RANGES.length; i++)
    	{
    		HIGHLIGHT_RANGES_KEYS[i] = this.current_keys.get(KEY_ACTIONS.valueOf("HIGHLIGHT_RANGE_" + (i+1)));
    	}
    	for (i=0; i<HIGHLIGHT_ORES.length; i++)
    	{
    		HIGHLIGHT_ORE_KEYS[i] = this.current_keys.get(KEY_ACTIONS.valueOf("TOGGLE_ORE_" + (i+1)));
    	}
    }
    
    /**
     * Sets our default key mappings
     */
    public void setKeyDefaults()
    {
    	default_keys = new TreeMap<KEY_ACTIONS, Integer>();
    	default_keys.put(KEY_ACTIONS.SPEED_INCREASE, Keyboard.KEY_LSHIFT);
    	default_keys.put(KEY_ACTIONS.SPEED_DECREASE, Keyboard.KEY_RSHIFT);
    	default_keys.put(KEY_ACTIONS.MOVE_FORWARD, Keyboard.KEY_W);
    	default_keys.put(KEY_ACTIONS.MOVE_BACKWARD, Keyboard.KEY_S);
    	default_keys.put(KEY_ACTIONS.MOVE_LEFT, Keyboard.KEY_A);
    	default_keys.put(KEY_ACTIONS.MOVE_RIGHT, Keyboard.KEY_D);
    	default_keys.put(KEY_ACTIONS.MOVE_UP, Keyboard.KEY_SPACE);
    	default_keys.put(KEY_ACTIONS.MOVE_DOWN, Keyboard.KEY_LCONTROL);
    	default_keys.put(KEY_ACTIONS.TOGGLE_MINIMAP, Keyboard.KEY_TAB);
    	default_keys.put(KEY_ACTIONS.TOGGLE_ORE_1, Keyboard.KEY_F1);
    	default_keys.put(KEY_ACTIONS.TOGGLE_ORE_2, Keyboard.KEY_F2);
    	default_keys.put(KEY_ACTIONS.TOGGLE_ORE_3, Keyboard.KEY_F3);
    	default_keys.put(KEY_ACTIONS.TOGGLE_ORE_4, Keyboard.KEY_F4);
    	default_keys.put(KEY_ACTIONS.TOGGLE_ORE_5, Keyboard.KEY_F5);
    	default_keys.put(KEY_ACTIONS.TOGGLE_ORE_6, Keyboard.KEY_F6);
    	default_keys.put(KEY_ACTIONS.TOGGLE_ORE_7, Keyboard.KEY_F7);
    	default_keys.put(KEY_ACTIONS.TOGGLE_ORE_8, Keyboard.KEY_F8);
    	default_keys.put(KEY_ACTIONS.TOGGLE_ORE_9, Keyboard.KEY_F9);
    	default_keys.put(KEY_ACTIONS.TOGGLE_ORE_10, Keyboard.KEY_F10);
    	default_keys.put(KEY_ACTIONS.TOGGLE_FULLSCREEN, Keyboard.KEY_BACK);
    	default_keys.put(KEY_ACTIONS.TOGGLE_FULLBRIGHT, Keyboard.KEY_F);
    	default_keys.put(KEY_ACTIONS.TOGGLE_ORE_HIGHLIGHTING, Keyboard.KEY_H);
    	default_keys.put(KEY_ACTIONS.MOVE_TO_SPAWN, Keyboard.KEY_HOME);
    	default_keys.put(KEY_ACTIONS.MOVE_TO_PLAYER, Keyboard.KEY_END);
    	default_keys.put(KEY_ACTIONS.MOVE_NEXT_CAMERAPOS, Keyboard.KEY_INSERT);
    	default_keys.put(KEY_ACTIONS.MOVE_PREV_CAMERAPOS, Keyboard.KEY_DELETE);
    	default_keys.put(KEY_ACTIONS.LIGHT_INCREASE, Keyboard.KEY_ADD);
    	default_keys.put(KEY_ACTIONS.LIGHT_DECREASE, Keyboard.KEY_SUBTRACT);
    	default_keys.put(KEY_ACTIONS.TOGGLE_POSITION_INFO, Keyboard.KEY_GRAVE);
    	default_keys.put(KEY_ACTIONS.TOGGLE_BEDROCK, Keyboard.KEY_B);
    	default_keys.put(KEY_ACTIONS.SWITCH_NETHER, Keyboard.KEY_N);
    	default_keys.put(KEY_ACTIONS.CHUNK_RANGE_1, Keyboard.KEY_NUMPAD1);
    	default_keys.put(KEY_ACTIONS.CHUNK_RANGE_2, Keyboard.KEY_NUMPAD2);
    	default_keys.put(KEY_ACTIONS.CHUNK_RANGE_3, Keyboard.KEY_NUMPAD3);
    	default_keys.put(KEY_ACTIONS.CHUNK_RANGE_4, Keyboard.KEY_NUMPAD4);
    	default_keys.put(KEY_ACTIONS.CHUNK_RANGE_5, Keyboard.KEY_NUMPAD5);
    	default_keys.put(KEY_ACTIONS.CHUNK_RANGE_6, Keyboard.KEY_NUMPAD6);
    	default_keys.put(KEY_ACTIONS.HIGHLIGHT_RANGE_1, Keyboard.KEY_1);
    	default_keys.put(KEY_ACTIONS.HIGHLIGHT_RANGE_2, Keyboard.KEY_2);
    	default_keys.put(KEY_ACTIONS.HIGHLIGHT_RANGE_3, Keyboard.KEY_3);
    	default_keys.put(KEY_ACTIONS.HIGHLIGHT_RANGE_4, Keyboard.KEY_4);
    	default_keys.put(KEY_ACTIONS.HIGHLIGHT_RANGE_5, Keyboard.KEY_5);
    	default_keys.put(KEY_ACTIONS.HIGHLIGHT_RANGE_6, Keyboard.KEY_6);
    	default_keys.put(KEY_ACTIONS.HIGHLIGHT_RANGE_7, Keyboard.KEY_7);
    	default_keys.put(KEY_ACTIONS.RELEASE_MOUSE, Keyboard.KEY_ESCAPE);
    	default_keys.put(KEY_ACTIONS.QUIT, Keyboard.KEY_Q);
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
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.drawString(statusmessage, (screenWidth/2)-((float)bounds.getWidth()/2), 40f);
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
			b = (Block) mapChunksToLoad.pop();
			//System.out.println("Loading chunk " + b.x + "," + b.z);
			
			// There may be some circumstances where a chunk we're going to load is already loaded.
			// Mostly while moving diagonally, I think.  I'm actually not convinced that it's worth
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
	                float progress= ((float) counter / (float) total);

	                float bx = 100;
	                float ex = screenWidth-100;
	                float by = (screenHeight/2.0f)-50;
	                float ey = (screenHeight/2.0f)+50;

	                float px = ((ex-bx)*progress) + bx;

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
				// as dirty so that they re-render.  This is needed so that we don't get gaps
				// in our terrain because the adjacent chunks weren't ready yet.
				level.markChunkAsDirty(b.x+1, b.z);
				level.markChunkAsDirty(b.x-1, b.z);
				level.markChunkAsDirty(b.x, b.z+1);
				level.markChunkAsDirty(b.x, b.z-1);
			}
			
			// If we've taken too long, break out so the GUI can update
			if (initial_load_done && Sys.getTime() - time  > max_chunkload_time)
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
    
    public void incLightLevel() {
    	this.currentLightLevel++;
    	if(this.currentLightLevel >= this.lightLevelStart.length) {
    		this.currentLightLevel = this.lightLevelStart.length-1;
    	}
    }
    
    public void decLightLevel() {
    	this.currentLightLevel--;
    	if(this.currentLightLevel <= 0) {
    		this.currentLightLevel = 0;
    	}
    }
    public void setLightLevel() {
    	this.setLightLevel(0);
    }
    
    public void setLightLevel(int diff) {
    	int min = this.lightLevelStart[this.currentLightLevel];
    	int max = this.lightLevelEnd[this.currentLightLevel];
    	
    	min = min + diff;
    	max = max + diff;
    	
    	if(min <= 0) {
    		min = 0;
    	}
    	if(max <= 0) {
    		max = 0;
    	}
    	
    	GL11.glFogf(GL11.GL_FOG_START, min);
        GL11.glFogf(GL11.GL_FOG_END, max);
    }
    
    /***
     * Initialize the basic openGL environment
     */
    private void initGL() {
        GL11.glEnable(GL11.GL_TEXTURE_2D); // Enable Texture Mapping
        GL11.glShadeModel(GL11.GL_FLAT); // Disable Smooth Shading
        GL11.glClearColor(0.0f, 0.3f, 1.0f, 0.3f); // Blue Background
        GL11.glClearDepth(1.0); // Depth Buffer Setup
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing
        GL11.glDepthFunc(GL11.GL_LEQUAL); // The Type Of Depth Testing To Do
        //GL11.glDepthFunc(GL11.GL_ALWAYS);
        
        GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
       // GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		
        GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
        GL11.glLoadIdentity(); // Reset The Projection Matrix
       
        // Calculate The Aspect Ratio Of The Window
        GLU.gluPerspective(
          90.0f,
          (float) displayMode.getWidth() / (float) displayMode.getHeight(),
          0.1f,
          400.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW); // Select The Modelview Matrix

        // Really Nice Perspective Calculations
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
    
        GL11.glDisable (GL11.GL_FOG);
        GL11.glFogi (GL11.GL_FOG_MODE, GL11.GL_LINEAR);
        float[] color = new float[] {0.0f, 0.3f, 1.0f, 0.3f};
        ByteBuffer colorBytes = ByteBuffer.allocateDirect(64);
        FloatBuffer colorBuffer = colorBytes.asFloatBuffer();
        colorBuffer.rewind();
        colorBuffer.put(color);
        colorBuffer.rewind();
        GL11.glFog (GL11.GL_FOG_COLOR, colorBytes.asFloatBuffer());
        GL11.glFogf (GL11.GL_FOG_DENSITY, 0.3f);
        GL11.glHint (GL11.GL_FOG_HINT, GL11.GL_NICEST);
        setLightLevel();

    }
    
    /***
     * Load textures
     * init precalc tables
     * determine available worlds
     * init misc variables
     */
	private void initialize() {
		// init the precalc tables

		mineralToggle = new boolean[HIGHLIGHT_ORES.length];
		mineralToggleTextures = new Texture[HIGHLIGHT_ORES.length];
				
		// world display list
		worldDisplayListNum = GL11.glGenLists(1);
        visibleOresListNum = GL11.glGenLists(1);
		
        // camera
        camera = new FirstPersonCameraController(0, 0, 0);
        
        // textures
        try {
        	// ui textures
        	minimapTexture 			= TextureTool.allocateTexture(minimap_dim,minimap_dim);
        	minimapGraphics			= minimapTexture.getImage().createGraphics();
			minimapArrowTexture 	= TextureTool.allocateTexture(32,32);
			fpsTexture				= TextureTool.allocateTexture(128, 32);
			levelInfoTexture		= TextureTool.allocateTexture(128,144);
			loadingTextTexture		= TextureTool.allocateTexture(screenWidth, 50);
			
			createMinimapSprites();
			
			// minecraft textures
			BufferedImage minecraftTextureImage 	= MineCraftEnvironment.getMinecraftTexture();
			minecraftTexture 						= TextureTool.allocateTexture(minecraftTextureImage, GL11.GL_NEAREST);
			minecraftTexture.update();
			
			// painting textures
			BufferedImage minecraftPaintingImage	= MineCraftEnvironment.getMinecraftPaintings();
			paintingTexture							= TextureTool.allocateTexture(minecraftPaintingImage, GL11.GL_NEAREST);
			paintingTexture.update();
			
			// Nether portal texture to use for drawing those, since there's no actual texture for it
			portalTexture = TextureTool.allocateTexture(16, 16);
			BufferedImage bi = portalTexture.getImage();
			Graphics2D pg = bi.createGraphics();
			pg.setColor(new Color(.839f, .203f, .952f, .4f));
			pg.fill(new Rectangle(0, 0, 16, 16));
			pg.drawImage(bi, null, 0, 0);
			portalTexture.update();
			
			// mineral textures
			for(int i=0;i<HIGHLIGHT_ORES.length;i++) {
				mineralToggleTextures[i] = TextureTool.allocateTexture(128,32);
				Graphics2D g = mineralToggleTextures[i].getImage().createGraphics();
				g.setFont(ARIALFONT);
				g.setColor(Color.white);
				g.drawString("[F" + (i+1) + "] " + ORES_DESCRIPTION[i], 10, 16);
				mineralToggleTextures[i].update();
			}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
       
		// level data
		levelBlockX = Integer.MIN_VALUE;
        levelBlockZ = Integer.MIN_VALUE;
        
        // set mouse grabbed so we can get x/y coordinates
        Mouse.setGrabbed(true);
	}
    
	private BufferedImage resizeImage(Image baseImage, int newWidth, int newHeight) {
		BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = newImage.createGraphics();
		g.drawImage(baseImage, 0,0, newWidth, newHeight, null);
		
		return newImage;
	}
	
	private byte[] convertIcon(byte[] icon) {
		byte[] newIcon = new byte[icon.length];
		for(int i=0;i<newIcon.length;i += 4) {
			newIcon[i+3] = icon[i+0];
			newIcon[i+2] = icon[i+1];
			newIcon[i+1] = icon[i+2];
			newIcon[i+0] = icon[i+3];
		}
		
		return newIcon;
	}
	
	/***
	 * Creates the window and initializes the lwjgl display object
	 * @throws Exception
	 */
    private void createWindow() throws Exception {
 
    	
    	// set icon buffers
    	// stupid conversions needed
    	File iconFile = new File("xray_icon.png");
    	ByteBuffer[] icons = null;
    	if(iconFile.exists() || iconFile.canRead()) {
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
	    	
	    	icons = new ByteBuffer[] {
	    			iconBuffer128, iconBuffer64, iconBuffer32, iconBuffer16
	    	};
	    	
	    	ResolutionDialog.iconImage = iconTexture128;
    	}
    	
    	// We loop on this dialog "forever" because 
    	while (true)
    	{
	    	if(ResolutionDialog.presentDialog(windowTitle, availableWorlds) == ResolutionDialog.DIALOG_BUTTON_EXIT) {
	    		System.exit(0);
	    	}
	
	        // Mark which world to load (which will happen later during initialize()
	        this.selectedWorld = ResolutionDialog.selectedWorld;
	    	
	    	// The last option will always be "Other..."  If that's been chosen, open a chooser dialog.
	        if (this.selectedWorld == availableWorlds.size() - 1)
	        {
	        	JFileChooser chooser = new JFileChooser();
	        	chooser.setFileHidingEnabled(false);
	        	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        	chooser.setCurrentDirectory(new File("."));
	        	chooser.setDialogTitle("Select a Minecraft World Directory");
	        	if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	        	{
	        		WorldInfo customWorld = availableWorlds.get(this.selectedWorld);
	        		customWorld.setBasePath(chooser.getSelectedFile().getCanonicalPath());
	        		File leveldat = customWorld.getLevelDatFile();
	        		if (leveldat.exists() && leveldat.canRead())
	        		{
	        			// We appear to have a valid level; break and continue.
	        			break;
	        		}
	        		else
	        		{
	        			// Invalid, show an error and then re-open the main dialog.
	            		JOptionPane.showMessageDialog(null,
	            				"Couldn't find a valid level.dat file for the specified directory",
	            				"Minecraft XRAY error",
	            				JOptionPane.ERROR_MESSAGE);
	        		}
	        	}
	        }
	        else
	        {
	        	// We chose one of the auto-detected worlds, continue.
	        	break;
	        }
    	}
    	
       	// set fullscreen from the dialog
    	fullscreen = ResolutionDialog.selectedFullScreenValue;
    	
    	if(icons != null)
    		Display.setIcon(icons);
    	
    	//Display.setIcon();
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
    private void checkMinecraftFiles() {
    	if(MineCraftEnvironment.getMinecraftDirectory() == null) {
    		JOptionPane.showMessageDialog(null, "OS not supported (" + System.getProperty( "os.name" ) + "), please report.", "Minecraft XRAY error" , JOptionPane.ERROR_MESSAGE);
    		System.exit(0);
    	}
    	if(!MineCraftEnvironment.getMinecraftDirectory().exists()) {
    		JOptionPane.showMessageDialog(null, "Minecraft directory not found: " + MineCraftEnvironment.getMinecraftDirectory().getAbsolutePath(), "Minecraft XRAY error" , JOptionPane.ERROR_MESSAGE);
    		System.exit(0);
    	}
    	if(!MineCraftEnvironment.getMinecraftDirectory().canRead()) {
    		JOptionPane.showMessageDialog(null, "Minecraft directory not readable: " + MineCraftEnvironment.getMinecraftDirectory().getAbsolutePath(), "Minecraft XRAY error", JOptionPane.ERROR_MESSAGE);
    		System.exit(0);
    	}
    	
    	availableWorlds = MineCraftEnvironment.getAvailableWorlds();
    	// Add in a custom "Other..." world
    	availableWorlds.add(new WorldInfo(true));
    	
    	// Since we're adding our custom world, this'll actually never get hit.  Ah well.
    	if(availableWorlds.size() == 0) {
    		JOptionPane.showMessageDialog(null, "Minecraft directory found, but no minecraft levels available.", "Minecraft XRAY error", JOptionPane.ERROR_MESSAGE);
    		System.exit(0);
    	}
    }
    
    private void setChunkRange(int n) {
    	if(n >= CHUNK_RANGES.length) n = CHUNK_RANGES.length-1;
    	if(n <= 0) n = 0;
    	if(n == currentChunkRange) {
    		return;
    	}
    	this.currentChunkRange = n;
    	this.visible_chunk_range = CHUNK_RANGES[n];
    	this.needToReloadWorld = true;
    }      
    
    private void setHighlightRange(int n) {
    	if(n >= HIGHLIGHT_RANGES.length) n = HIGHLIGHT_RANGES.length-1;
    	if(n <= 0) n = 0;
    	if(n == currentHighlightDistance) {
    		return;
    	}
    	this.currentHighlightDistance = n;
     }      

    /***
     * Sets the world number we want to view
     * @param world
     */
    private void setMinecraftWorld(WorldInfo world) {
    	this.world = world;
    	this.level =  new MinecraftLevel(world, minecraftTexture, paintingTexture, portalTexture);
    	
    	// determine which chunks are available in this world
    	mapChunksToLoad = new LinkedList<Block>();
		
		moveCameraToPlayerPos();
    }
    
    /**
     * Sets the world number we want, and moves the camera to the specified coordinates.  There's
     * a bit of code duplication going on here; should fix that.
     * 
     * @param world
     * @param camera_x
     * @param camera_z
     */
    private void setMinecraftWorld(WorldInfo world, FirstPersonCameraController camera)
    {
    	this.world = world;
    	this.level =  new MinecraftLevel(world, minecraftTexture, paintingTexture, portalTexture);
    	
    	// determine which chunks are available in this world
    	mapChunksToLoad = new LinkedList<Block>();

		this.camera = camera;
		initial_load_queued = false;
		initial_load_done = false;
		this.removeChunklistFromMap(level.removeAllChunksFromMinimap());
		this.triggerChunkLoads();
    	
    }
    
    private void moveCameraToPosition(CameraPreset playerPos)
    {
    	this.camera.getPosition().set(playerPos.block.x, playerPos.block.y, playerPos.block.z);
		this.camera.setYawAndPitch(180+playerPos.yaw, playerPos.pitch);
		initial_load_queued = false;
		initial_load_done = false;
		this.removeChunklistFromMap(level.removeAllChunksFromMinimap());
		this.triggerChunkLoads();
		this.currentPosition = playerPos;
    }

    private void moveCameraToSpawnPoint() {
    	this.moveCameraToPosition(level.getSpawnPoint());
    }
    
    private void moveCameraToPlayerPos() {
    	this.moveCameraToPosition(level.getPlayerPosition());
    }
    
    private void moveCameraToNextPlayer() {
    	this.moveCameraToPosition(level.getNextPlayerPosition(this.currentPosition));
    }
    
    private void moveCameraToPreviousPlayer() {
    	this.moveCameraToPosition(level.getPrevPlayerPosition(this.currentPosition));
    }
    
    /**
     * Populates mapChunksToLoad with a list of chunks that need adding, based
     * on how far we've moved since our last known position.  Realistically this
     * is never going to be more than one line at a time, though if someone's
     * getting hit with ridiculously low FPS or something, perhaps there could end
     * up being more.
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
    		
    		int top_x=0;
    		int bot_x=0;
    		int top_z=0;
    		int bot_z=0;
    		
    		// X
    		if (dx < 0)
    		{
    			//System.out.println("Loading in chunks from the X range " + (cur_chunk_x-1-loadChunkRange) + " to " + (chunkX-loadChunkRange) + " (going down)");
    			top_x = cur_chunk_x-1-loadChunkRange;
    			bot_x = chunkX-loadChunkRange;
    		}
    		else if (dx > 0)
    		{
    			//System.out.println("Loading in chunks from the X range " + (cur_chunk_x+1+loadChunkRange) + " to " + (chunkX+loadChunkRange) + " (going up)");
    			top_x = chunkX+loadChunkRange;
    			bot_x = cur_chunk_x+1+loadChunkRange;
    		}
    		if (dx != 0)
    		{
    			for (int lx=bot_x; lx <= top_x; lx++)
    			{
    				for (int lz=chunkZ-loadChunkRange; lz<=chunkZ+loadChunkRange; lz++)
    				{
    					tempchunk = level.getChunk(lx, lz);
    					if (tempchunk != null)
    					{
    						if (tempchunk.x == lx && tempchunk.z == lz)
	    					{
    							if (!tempchunk.isOnMinimap)
    							{
    								drawChunkToMap(tempchunk.x, tempchunk.z);
    								//minimap_changed = true;
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
    			//System.out.println("Loading in chunks from the Z range " + (cur_chunk_z-1-loadChunkRange) + " to " + (chunkZ-loadChunkRange) + " (going down)");
    			top_z = cur_chunk_z-1-loadChunkRange;
    			bot_z = chunkZ-loadChunkRange;
    		}
    		else if (dz > 0)
    		{
    			//System.out.println("Loading in chunks from the Z range " + (cur_chunk_z+1+loadChunkRange) + " to " + (chunkZ+loadChunkRange) + " (going up)");
    			top_z = chunkZ+loadChunkRange;
    			bot_z = cur_chunk_z+1+loadChunkRange;
    		}
    		if (dz != 0)
    		{
    			for (int lx=chunkX-loadChunkRange; lx<=chunkX+loadChunkRange; lx++)
    			{
    				for (int lz=bot_z; lz <= top_z; lz++)
    				{
    					tempchunk = level.getChunk(lx, lz);
    					if (tempchunk != null)
    					{
    						if (tempchunk.x == lx && tempchunk.z == lz)
	    					{
    							if (!tempchunk.isOnMinimap)
    							{
    								drawChunkToMap(tempchunk.x, tempchunk.z);
    								//minimap_changed = true;
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
    				//System.out.println("Clearing X from " + (chunkX-minimap_trim_chunk_distance+minimap_trim_chunks) + " to " + (chunkX-minimap_trim_chunk_distance));
    				for (i=chunkX-minimap_trim_chunk_distance+minimap_trim_chunks; i>=chunkX-minimap_trim_chunk_distance; i--)
    				{
    					trimList.addAll(level.removeChunkRowXFromMinimap(i));
    				}
	    			total_dX = -(Math.abs(total_dX) % minimap_trim_chunks);
    			}
    			else
    			{
    				//System.out.println("Clearing X from " + (chunkX+minimap_trim_chunk_distance-minimap_trim_chunks) + " to " + (chunkX+minimap_trim_chunk_distance));
    				for (i=chunkX+minimap_trim_chunk_distance-minimap_trim_chunks; i<=chunkX+minimap_trim_chunk_distance; i++)
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
    				//System.out.println("Clearing Z from " + (chunkZ-minimap_trim_chunk_distance+minimap_trim_chunks) + " to " + (chunkZ-minimap_trim_chunk_distance));
    				for (i=chunkZ-minimap_trim_chunk_distance+minimap_trim_chunks; i>=chunkZ-minimap_trim_chunk_distance; i--)
    				{
    					trimList.addAll(level.removeChunkRowZFromMinimap(i));
    				}
	    			total_dZ = -(Math.abs(total_dZ) % minimap_trim_chunks);
    			}
    			else
    			{
    				//System.out.println("Clearing Z from " + (chunkZ+minimap_trim_chunk_distance-minimap_trim_chunks) + " to " + (chunkZ+minimap_trim_chunk_distance));
    				for (i=chunkZ+minimap_trim_chunk_distance-minimap_trim_chunks; i<=chunkZ+minimap_trim_chunk_distance; i++)
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
    		//System.out.println("Loading world from X: " + (chunkX-loadChunkRange) + " - " + (chunkX+loadChunkRange) + ", Z: " + (chunkZ-loadChunkRange) + " - " + (chunkZ+loadChunkRange));
            for(int lx=chunkX-loadChunkRange;lx<=chunkX+loadChunkRange;lx++) {
        		for(int lz=chunkZ-loadChunkRange;lz<=chunkZ+loadChunkRange;lz++) {
					level.clearChunk(lx, lz);
 					mapChunksToLoad.add(new Block(lx,0,lz));
        		}
        	}
    		initial_load_queued = true;
    	}
        cur_chunk_x = chunkX;
        cur_chunk_z = chunkZ;
    }
    
    /***
     * handles all input on all screens
     * @param timeDelta
     */
	private void handleInput(float timeDelta) {
		
		int key;
		
		  //distance in mouse movement from the last getDX() call.
        mouseX = Mouse.getDX();
        //distance in mouse movement from the last getDY() call.
        mouseY = Mouse.getDY();
        
    	// we are on the main world screen or the level loading screen
    	// update the camera (but only if the mouse is grabbed)
    	if (Mouse.isGrabbed())
    	{
    		camera.incYaw(mouseX * MOUSE_SENSITIVITY);
    		camera.incPitch(-mouseY * MOUSE_SENSITIVITY);
    	}
    	
    	//
    	// Keyboard commands (well, and mouse presses)
    	//
       
    	// Speed shifting
    	if (Mouse.isButtonDown(0) || Keyboard.isKeyDown(current_keys.get(KEY_ACTIONS.SPEED_INCREASE))) {
        	MOVEMENT_SPEED = 30.0f;
        } else if (Mouse.isButtonDown(1) || Keyboard.isKeyDown(current_keys.get(KEY_ACTIONS.SPEED_DECREASE))) {
        	MOVEMENT_SPEED = 3.0f;
        } else {
        	MOVEMENT_SPEED = 10.0f;
        }
    	
        // Move forward
        if (Keyboard.isKeyDown(current_keys.get(KEY_ACTIONS.MOVE_FORWARD)))
        {
            camera.walkForward(MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
        
        // Move backwards
        if (Keyboard.isKeyDown(current_keys.get(KEY_ACTIONS.MOVE_BACKWARD)))
        {
            camera.walkBackwards(MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
        
        // Strafe Left
        if (Keyboard.isKeyDown(current_keys.get(KEY_ACTIONS.MOVE_LEFT)))
        {
            camera.strafeLeft(MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
        
        // Strafe right
        if (Keyboard.isKeyDown(current_keys.get(KEY_ACTIONS.MOVE_RIGHT)))
        {
            camera.strafeRight(MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
        
        // Fly Up
        if (Keyboard.isKeyDown(current_keys.get(KEY_ACTIONS.MOVE_UP))) {
            camera.moveUp(MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
        
        // Fly Down
        if (Keyboard.isKeyDown(current_keys.get(KEY_ACTIONS.MOVE_DOWN))) {
            camera.moveUp(-MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
       
        // Toggle minimap/largemap
        key = current_keys.get(KEY_ACTIONS.TOGGLE_MINIMAP);
        if(Keyboard.isKeyDown(key) && keyPressed != key) {
        	mapBig = !mapBig;
        	keyPressed = key;
        }

        // Toggle highlightable ores
    	needToReloadWorld = false;
        for(int i=0;i<mineralToggle.length;i++) {
        	if(Keyboard.isKeyDown(HIGHLIGHT_ORE_KEYS[i]) && keyPressed != HIGHLIGHT_ORE_KEYS[i]) {
        		keyPressed = HIGHLIGHT_ORE_KEYS[i];
        		mineralToggle[i] = !mineralToggle[i];
        		needToReloadWorld = true;
        	}
        }
        if(needToReloadWorld) {
        	invalidateSelectedChunks();
        }
        
        // Fullscreen
        key = current_keys.get(KEY_ACTIONS.TOGGLE_FULLSCREEN);
        if(Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;                                     
            switchFullScreenMode();
        }
        
        //  Toggle fullbright
        key = current_keys.get(KEY_ACTIONS.TOGGLE_FULLBRIGHT);
        if(Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;                                     
            setLightMode(!lightMode);
        }
        
        // Toggle ore highlighting
        key = current_keys.get(KEY_ACTIONS.TOGGLE_ORE_HIGHLIGHTING);
        if(Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;                                     
            highlightOres = !highlightOres;
        }
        
        // Move camera to spawn point
        key = current_keys.get(KEY_ACTIONS.MOVE_TO_SPAWN);
        if(Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;                                     
        	moveCameraToSpawnPoint();
        }
        
        // Move camera to player position
        key = current_keys.get(KEY_ACTIONS.MOVE_TO_PLAYER);
        if(Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;                                     
        	moveCameraToPlayerPos();
        }
        
        // Switch to the next available camera preset
        key = current_keys.get(KEY_ACTIONS.MOVE_NEXT_CAMERAPOS);
        if (Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;
        	moveCameraToNextPlayer();
        }
        
        // Switch to the previous camera preset
        key = current_keys.get(KEY_ACTIONS.MOVE_PREV_CAMERAPOS);
        if (Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;
        	moveCameraToPreviousPlayer();
        }
        
        // Increase light level
        key = current_keys.get(KEY_ACTIONS.LIGHT_INCREASE);
        if(Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;                                     
        	incLightLevel();
        }

        // Decrease light level
        key = current_keys.get(KEY_ACTIONS.LIGHT_DECREASE);
        if(Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;                                     
        	decLightLevel();
        }
        
        // Toggle position info popup
        key = current_keys.get(KEY_ACTIONS.TOGGLE_POSITION_INFO);
        if(Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;                                     
        	levelInfoToggle = !levelInfoToggle;
        }
        
        // Toggle bedrock rendering
        key = current_keys.get(KEY_ACTIONS.TOGGLE_BEDROCK);
        if (Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;
        	render_bedrock = !render_bedrock;
    		invalidateSelectedChunks(true);
        }
        
        // Toggle between Nether and Overworld
        key = current_keys.get(KEY_ACTIONS.SWITCH_NETHER);
        if (Keyboard.isKeyDown(key) && keyPressed != key) {
        	keyPressed = key;
        	switchNether();
        }
        
        // Temp routine to write the minimap out to a PNG (for debugging purposes)
        /*
        if (Keyboard.isKeyDown(Keyboard.KEY_P) && keyPressed != Keyboard.KEY_P) {
        	keyPressed = Keyboard.KEY_P;
        	BufferedImage bi = minimapTexture.getImage();
        	try
        	{
        		ImageIO.write(bi, "PNG", new File("/home/pez/xray.png"));
        		System.out.println("Wrote minimap to disk.");
        	}
        	catch (Exception e)
        	{
        		// whatever
        	}
        }
        */

        // Handle changing chunk ranges (how far out we draw from the camera
        for(int i = 0; i<CHUNK_RANGES.length;i++) {
	        if(Keyboard.isKeyDown(CHUNK_RANGES_KEYS[i]) && keyPressed != CHUNK_RANGES_KEYS[i]) {
	        	keyPressed = CHUNK_RANGES_KEYS[i];                                     
	        	setChunkRange(i);
	        }
        }

        // Handle changing the ore highlight distances
        for(int i = 0; i<HIGHLIGHT_RANGES.length;i++) {
	        if(Keyboard.isKeyDown(HIGHLIGHT_RANGES_KEYS[i]) && keyPressed != HIGHLIGHT_RANGES_KEYS[i]) {
	        	keyPressed = HIGHLIGHT_RANGES_KEYS[i];                                     
	        	setHighlightRange(i);
	        }
        }        
        	
        // Release the mouse
        if(Keyboard.isKeyDown(current_keys.get(KEY_ACTIONS.RELEASE_MOUSE))) {
            Mouse.setGrabbed(false);
        }
        
        // Grab the mouse on a click
        if (Mouse.isButtonDown(0)) {
        	Mouse.setGrabbed(true);
        }
        
        // Quit
        if (Keyboard.isKeyDown(current_keys.get(KEY_ACTIONS.QUIT)) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
        	done = true;
        }
        
        // Handle a requested window close
        if(Display.isCloseRequested()) {
            done = true;
        }
        
        // Clear out our keyPressed var if it's improperly-set
        if(keyPressed != -1) {
	        if(!Keyboard.isKeyDown(keyPressed)) {
	        	keyPressed = -1;
	        }
        }
	}
	
	/**
	 * If we can, switches to/from nether.  This will attempt to do an approximate translation
	 * of your position, though that hasn't been tested much, and won't totally line up with
	 * what Minecraft does.  Note that height is unaffected by this, so the adjacent portal
	 * might show up higher or lower, depending on the local terrain.
	 */
	private void switchNether()
	{
		WorldInfo newworld = null;
		float camera_mult = 1.0f;
		if (world.isNether() && world.hasOverworld())
		{
			this.cameraTextOverride = "equivalent Overworld location (approx.)";
			newworld = world.getOverworldInfo();
			camera_mult = 8.0f;
		}
		else if (!world.isNether() && world.hasNether())
		{
			this.cameraTextOverride = "equivalent Nether location (approx.)";
			newworld = world.getNetherInfo();
			camera_mult = 1.0f/8.0f;
		}
		if (newworld != null)
		{
			// A full reinitialization is kind of overkill, but whatever.
			FirstPersonCameraController cur_camera = this.camera;
			this.camera.processNetherWarp(camera_mult);
			initialize();
			this.setMinecraftWorld(newworld, cur_camera);
			this.triggerChunkLoads();
		}
	}
	
	private void invalidateSelectedChunks() {
    	level.invalidateSelected(false);
	}

	private void invalidateSelectedChunks(boolean main_dirty) {
    	level.invalidateSelected(main_dirty);
	}
	
	private void setLightMode(boolean lightMode) {
       this.lightMode = lightMode;
		if(lightMode) {
       	 GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black Background
       	 GL11.glEnable (GL11.GL_FOG);
       } else { 
       	 GL11.glClearColor(0.0f, 0.3f, 1.0f, 0.3f); // Blue Background
       	 GL11.glDisable (GL11.GL_FOG);
       }
	}
	
	/***
	 * Switches full screen mode
	 */
    private void switchFullScreenMode() {
        fullscreen = !fullscreen;
        try {
            Display.setFullscreen(fullscreen);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /***
     * Draw the spawn position to the minimap
     */
    private void drawSpawnMarkerToMinimap() {
    	Graphics2D g = minimapGraphics;
    	
    	CameraPreset spawn = level.getSpawnPoint();
		int sy = getMinimapBaseY(spawn.block.cx)-(spawn.block.x%16);
		int sx = (getMinimapBaseX(spawn.block.cz)+(spawn.block.z%16))%minimap_dim;
    	
    	g.setStroke(new BasicStroke(2));
    	g.setColor(Color.red.brighter());
    	g.drawOval(sx-6, sy-6, 11, 11);
    	g.drawLine(sx-8, sy, sx+8, sy);
    	g.drawLine(sx, sy-8, sx, sy+8);
    	minimapTexture.update();
    }
    
    /***
     * Draw the current position to the minimap
     */
    private void drawPlayerposMarkerToMinimap() {
    	Graphics2D g = minimapGraphics;
    	
    	CameraPreset player = level.getPlayerPosition();
    	int py = getMinimapBaseY(player.block.cx)-(player.block.x%16);
		int px = getMinimapBaseX(player.block.cz)+(player.block.z%16);	
    	
    	g.setStroke(new BasicStroke(2));
	    g.setColor(Color.yellow.brighter());
	    g.drawOval(px-6, py-6, 11, 11);
	    g.drawLine(px-8, py, px+8, py);
	    g.drawLine(px, py-8, px, py+8);
    	minimapTexture.update();
    }
    
    /***
     * Main render loop
     * @param timeDelta
     * @return
     */
    private boolean render(float timeDelta) {
    	//GL11.glLoadIdentity();
        GL11.glLoadIdentity();
    	GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);          // Clear The Screen And The Depth Buffer
    	
    	// are we still loading the map? 
    	if (!map_load_started)
    	{
    		map_load_started = true;
       		//drawMapMarkersToMinimap();
       		//minimapTexture.update();
       		setLightMode(true); // basically enable fog etc  		
    	}
        
        // we are viewing a world
    	GL11.glPushMatrix();
    
    	// change the camera to point a the right direction
    	camera.applyCameraTransformation();

        currentCameraPosX = (int) -camera.getPosition().x;
        currentCameraPosZ = (int) -camera.getPosition().z;
        
        // determine if we need to load new map chunks
        if(currentCameraPosX != levelBlockX || currentCameraPosZ != levelBlockZ || needToReloadWorld) {
        	levelBlockX = currentCameraPosX;
        	levelBlockZ = currentCameraPosZ;
        	currentLevelX = level.getChunkX(levelBlockX);
        	currentLevelZ = level.getChunkZ(levelBlockZ);
        }
        
        // draw the visible world
		int chunk_range = visible_chunk_range;
		if (HIGHLIGHT_RANGES[currentHighlightDistance] < chunk_range)
		{
			chunk_range = HIGHLIGHT_RANGES[currentHighlightDistance];
		} 
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(1.0f,1.0f,1.0f); 
        minecraftTexture.bind();
        for(int lx=currentLevelX-visible_chunk_range;lx<currentLevelX+visible_chunk_range;lx++) {
    		for(int lz=currentLevelZ-visible_chunk_range;lz<currentLevelZ+visible_chunk_range;lz++) {
    			Chunk k = level.getChunk(lx, lz);
    	        
    			if(k != null)
    			{
    				k.renderSolid(render_bedrock);
    				k.renderSelected(this.mineralToggle);
    				paintingTexture.bind();
    				k.renderPaintings();
    				minecraftTexture.bind();
    			}
    		}
    	}
    	for(int lx=currentLevelX-visible_chunk_range;lx<currentLevelX+visible_chunk_range;lx++) {
    		for(int lz=currentLevelZ-visible_chunk_range;lz<currentLevelZ+visible_chunk_range;lz++) {
    			Chunk k = level.getChunk(lx, lz);
    	        
    			if(k != null) k.renderTransparency();
    		}
    	}

    	
    	if(highlightOres) {

			GL11.glDisable(GL11.GL_DEPTH_TEST);
			long time = System.currentTimeMillis();
			float alpha = (time % 1000) / 1000.0f;
			if(time % 2000 > 1000) alpha = 1.0f - alpha;
			alpha = 0.1f + (alpha*0.8f);
			GL11.glColor4f(alpha, alpha, alpha, alpha);
			setLightLevel(20);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			for(int lx=currentLevelX-chunk_range;lx<currentLevelX+chunk_range;lx++) {
	    		for(int lz=currentLevelZ-chunk_range;lz<currentLevelZ+chunk_range;lz++) {
	    			Chunk k = level.getChunk(lx, lz);
	    			if(k != null)
	    				k.renderSelected(this.mineralToggle);
	    		}
			}
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			setLightLevel();
    	}
        
        
        GL11.glPopMatrix();
        
        // draw the user interface (fps and map)
        drawUI(); 
        
        return true;
    }
    
    /***
     * Draw the ui
     */
	private void drawUI() {
		framesSinceLastFps++;

		setOrthoOn(); // 2d mode
		
		drawMinimap();
		drawFPSCounter();
		drawMineralToggle();
		drawLevelInfo();
				
		setOrthoOff(); // back to 3d mode
    }
	
	private void updateLevelInfo() {
		int labelX = 5;
		int valueX = 70;
		Graphics2D g = levelInfoTexture.getImage().createGraphics();
		g.setBackground(Color.BLUE);
		g.clearRect(0, 0, 128, 144);
		g.setColor(Color.WHITE);
		g.fillRect(2, 2, 124, 140);
		g.setFont(ARIALFONT);
		int chunkX = level.getChunkX(levelBlockX);
		int chunkZ = level.getChunkZ(levelBlockZ);
		g.setColor(Color.BLACK);
		g.drawString("Chunk X:", labelX, 22);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(chunkX), valueX, 22);

		g.setColor(Color.BLACK);
		g.drawString("Chunk Z:", labelX, 22+16);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(chunkZ), valueX, 22+16);
		
		g.setColor(Color.BLACK);
		g.drawString("World X:", labelX, 22+32);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(levelBlockX), valueX, 22+32);

		g.setColor(Color.BLACK);
		g.drawString("World Z:", labelX, 22+16+32);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(levelBlockZ), valueX, 22+16+32);

		g.setColor(Color.BLACK);
		g.drawString("World Y:", labelX, 22+16+32+16);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString((int)-camera.getPosition().y), valueX, 22+16+32+16);
		
		long heapSize = Runtime.getRuntime().totalMemory(); 
		g.setColor(Color.BLACK);
		g.drawString("Memory Used", labelX, 22+16+32+16+25);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString((int) (heapSize/1024/1024)) + " MB", 20, 22+16+32+16+25+20);
		
		
		levelInfoTexture.update();
	}
	
	/***
	 * 
	 */
	private void drawLevelInfo() {
		if(levelInfoToggle) {
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			SpriteTool.drawSpriteAbsoluteXY(
				levelInfoTexture, 
				0, 
				48
			);
		}
	}
	
	/***
	 * Draw the mineral toggles
	 */
	private void drawMineralToggle() {
		int barWidth = 128+10+32;
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
		float curX = (screenWidth / 2.0f) - (mineralTogglebarLength/2.0f);
		float curY = screenHeight - barHeight;
		if (mineralToggleTextures.length > maxCols)
		{
			curY -= barHeight;
		}
		
		for(int i=0;i<mineralToggleTextures.length;i++) {
			if (i == mineralToggleTextures.length - maxCols)
			{
				mineralTogglebarLength = maxCols * barWidth;
				curY += barHeight;
				curX = (screenWidth / 2.0f) - (mineralTogglebarLength/2.0f);
			}
			if(mineralToggle[i]) {
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				SpriteTool.drawCurrentSprite(
						curX - 2, curY -2, 
						36, 36, 
						MineCraftConstants.precalcSpriteSheetToTextureX[blockDataToSpriteSheet[HIGHLIGHT_ORES[i]]], 
						MineCraftConstants.precalcSpriteSheetToTextureY[blockDataToSpriteSheet[HIGHLIGHT_ORES[i]]],
						MineCraftConstants.precalcSpriteSheetToTextureX[blockDataToSpriteSheet[HIGHLIGHT_ORES[i]]]+TEX16,
						MineCraftConstants.precalcSpriteSheetToTextureY[blockDataToSpriteSheet[HIGHLIGHT_ORES[i]]]+TEX16
				);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
			} else {
				GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
			}
			minecraftTexture.bind();
			SpriteTool.drawCurrentSprite(
					curX, curY, 
					32, 32, 
					MineCraftConstants.precalcSpriteSheetToTextureX[blockDataToSpriteSheet[HIGHLIGHT_ORES[i]]], 
					MineCraftConstants.precalcSpriteSheetToTextureY[blockDataToSpriteSheet[HIGHLIGHT_ORES[i]]],
					MineCraftConstants.precalcSpriteSheetToTextureX[blockDataToSpriteSheet[HIGHLIGHT_ORES[i]]]+TEX16,
					MineCraftConstants.precalcSpriteSheetToTextureY[blockDataToSpriteSheet[HIGHLIGHT_ORES[i]]]+TEX16
			);
			
			SpriteTool.drawSpriteAbsoluteXY(mineralToggleTextures[i], curX + 32 + 10, curY+7);
			curX += barWidth;
		}
	}
	/***
	 * Draws a simple fps counter on the top-left of the screen
	 */
	private void drawFPSCounter() {
		previousTime = time;
		time = System.nanoTime();
		timeDelta = time - previousTime;

		if (time - lastFpsTime > NANOSPERSECOND) {
			fps = framesSinceLastFps;
			framesSinceLastFps = 0;
			lastFpsTime = time;
			updateFPSText = true;
		}
		if (updateFPSText) {
			if(levelInfoToggle)
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
        GL11.glPushMatrix();                   // preserve perspective view
        GL11.glLoadIdentity();                 // clear the perspective matrix
        GL11.glOrtho(                          // turn on 2D mode
        		////viewportX,viewportX+viewportW,    // left, right
        		////viewportY,viewportY+viewportH,    // bottom, top    !!!
        		0,screenWidth,    // left, right
        		screenHeight,0,    // bottom, top
        		-500,500);                        // Zfar, Znear
        // clear the modelview matrix
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();				   // Preserve the Modelview Matrix
        GL11.glLoadIdentity();				   // clear the Modelview Matrix
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
	private void drawMinimap() {	
		if(mapBig) {
			// the big map
			// just draws the texture, but move the texture so the middle of the screen is where we currently are

			minimapTexture.bind();

			float vSizeFactor = .5f;
			
			float vTexX = -(1.0f/minimap_dim_f) * currentCameraPosZ;
			float vTexY = (1.0f/minimap_dim_f) * currentCameraPosX;
			float vTexZ = vSizeFactor;
			
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
			GL11.glPushMatrix();
			GL11.glTranslatef((screenWidth/2.0f), (screenHeight/2.0f), 0.0f);
				GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		
					GL11.glTexCoord2f(vTexX-vTexZ, vTexY-vTexZ);
					GL11.glVertex2f(-minimap_dim_h_f, -minimap_dim_h_f);
		
					GL11.glTexCoord2f(vTexX+vTexZ, vTexY-vTexZ);
					GL11.glVertex2f(+minimap_dim_h_f, -minimap_dim_h_f);
		
					GL11.glTexCoord2f(vTexX-vTexZ, vTexY+vTexZ);
					GL11.glVertex2f(-minimap_dim_h_f, +minimap_dim_h_f);

					GL11.glTexCoord2f(vTexX+vTexZ, vTexY+vTexZ);
					GL11.glVertex2f(+minimap_dim_h_f, +minimap_dim_h_f);
					
				GL11.glEnd();
			GL11.glPopMatrix();
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1f);
			
			SpriteTool.drawSpriteAndRotateAndScale(minimapArrowTexture, screenWidth/2.0f, screenHeight/2.0f, camera.getYaw()+90,0.5f);
		} else {
			// the minimap
			// I set the minimap to 200 wide and tall
			
			// Interestingly, thanks to the fact that we're using GL11.GL_REPEAT on our
			// textures (via glTexParameter), we don't have to worry about checking
			// bounds here, etc.  Or in other words, our map will automatically wrap for
			// us.  Sweet!
			float vSizeFactor = 200.0f/minimap_dim_f;
			
			float vTexX = -(1.0f/minimap_dim_f) * currentCameraPosZ;
			float vTexY = (1.0f/minimap_dim_f) * currentCameraPosX;
			float vTexZ = vSizeFactor;
			
			minimapTexture.bind();
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
			GL11.glPushMatrix();
				GL11.glTranslatef(screenWidth-100, 100, 0.0f);
				GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		
					GL11.glTexCoord2f(vTexX-vTexZ, vTexY-vTexZ);
					GL11.glVertex2f(-100, -100);
		
					GL11.glTexCoord2f(vTexX+vTexZ, vTexY-vTexZ);
					GL11.glVertex2f(+100, -100);
		
					GL11.glTexCoord2f(vTexX-vTexZ, vTexY+vTexZ);
					GL11.glVertex2f(-100, +100);
		
					GL11.glTexCoord2f(vTexX+vTexZ, vTexY+vTexZ);
					GL11.glVertex2f(+100, +100);
							
				GL11.glEnd();
			GL11.glPopMatrix();
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1f);
			
			SpriteTool.drawSpriteAndRotateAndScale(minimapArrowTexture, screenWidth-100, 100, camera.getYaw()+90,0.5f);
		}
	}
	
	/**
	 * Returns the "base" minimap X coordinate, given chunk coordinate Z.
	 * The "base" will be the upper right corner.
	 * 
	 * In Minecraft, Z increases to the West, and decreases to the East,
	 * so our minimap X coordinate will go up as chunkZ goes down.
	 * 
	 * @param chunkZ
	 * @return
	 */
	private int getMinimapBaseX(int chunkZ)
	{
		if (chunkZ < 0)
		{
			return ((Math.abs(chunkZ+1)*16) % minimap_dim) + 15;
		}
		else
		{
			return minimap_dim - (((chunkZ*16)+1) % minimap_dim);
		}
	}

	/**
	 * Returns the "base" minimap Y coordinate, given chunk coordinate X.
	 * The "base" will be the upper right corner.
	 * 
	 * In Minecraft, X increases to the South, and decreases to the North,
	 * so our minimap Y coordinate will go up as chunkX goes up (since the
	 * origin of a texture is in the upper-left).
	 * 
	 * @param chunkX
	 * @return
	 */
	private int getMinimapBaseY(int chunkX)
	{
		if (chunkX < 0)
		{
			return (minimap_dim - ((Math.abs(chunkX)*16) % minimap_dim)) % minimap_dim;
		}
		else
		{
			return (chunkX*16) % minimap_dim;
		}
	}
	
	
	/**
	 * Clears out the area on the minimap belonging to this chunk
	 * 
	 * @param x
	 * @param z
	 */
	public void removeMapChunkFromMap(int x, int z) {
		//minimapGraphics.setColor(new Color(0f, 0f, 0f, 1f));
		//minimapGraphics.setComposite(AlphaComposite.Src);
		minimapGraphics.fillRect(getMinimapBaseX(z)-15, getMinimapBaseY(x), 16, 16);
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
	 * @param x
	 * @param z
	 */
	public void drawChunkToMap(int x, int z) {
		
		Chunk c = level.getChunk(x,z);
		if (c != null)
		{
			c.isOnMinimap = true;
		}
		byte[] chunkData = level.getChunkData(x,z);
		
		int base_x = getMinimapBaseX(z);
		int base_y = getMinimapBaseY(x);
		
		Graphics2D g = minimapGraphics;
		for(int zz = 0; zz<16; zz++) {
			for(int xx =0; xx<16; xx++) {
				// determine the top most visible block
				for (int yy = 127; yy >= 0; yy--)
				{
					int blockOffset = yy + (zz * 128) + (xx * 128 * 16);
					byte blockData = chunkData[blockOffset];
					
					if(MineCraftConstants.blockDataToSpriteSheet[blockData] > -1) {
						if (blockData > -1) {
							Color blockColor = MineCraftConstants.blockColors[blockData];
							if(blockColor != null) {
								// Previously we were using g.drawLine() here, but a minute-or-so's worth of investigating
								// didn't uncover a way to force that to be pixel-precise (the color would often bleed over
								// into adjoining pixels), so we're using g.fillRect() instead, which actually looks like it
								// is probably a faster operation anyway.  I'm sure there'd have been a way to get drawLine
								// to behave, but c'est la vie!
								g.setColor(blockColor);
								g.fillRect(base_x-zz, base_y+xx, 1, 1);
							}
						}
						break;
					}
				}
			}
		}
	}
	
	/***
	 * Draws the minimap sprites (currently just the arrow image) to their textures
	 */
    private void createMinimapSprites() {
    	
    	// First the arrow
    	Graphics2D g = minimapArrowTexture.getImage().createGraphics();
    	g.setColor(Color.red);
    	g.setStroke(new BasicStroke(5));
    	g.drawLine(3,16, 30,24);
    	g.drawLine(30,24,30,8);
    	g.drawLine(30,8, 3,16);
    	minimapArrowTexture.update();
    }
    
    /***
     * cleanup
     */
    private void cleanup() {
        Display.destroy();
    }
    
 
}