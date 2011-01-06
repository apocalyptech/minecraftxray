package com.plusminus.craft;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JWindow;

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
	// TODO: revert this
	//private int visible_chunk_range = 5;
	private int visible_chunk_range = 8;
	private int mapchange_redraw_range = 3;
	
	private static final int[] CHUNK_RANGES_KEYS = new int[] {
		Keyboard.KEY_NUMPAD1,
		Keyboard.KEY_NUMPAD2,
		Keyboard.KEY_NUMPAD3,
		Keyboard.KEY_NUMPAD4,
		Keyboard.KEY_NUMPAD5,
		Keyboard.KEY_NUMPAD6
	};
	private static final int[] CHUNK_RANGES = new int[] {3,4,5,6,7,8};
	// TODO: revert this
	//private int currentChunkRange = 4;
	private int currentChunkRange = 5;
	
	// highlight distance
	private static final int[] HIGHLIGHT_RANGES_KEYS = new int[] {
		Keyboard.KEY_1,
		Keyboard.KEY_2,
		Keyboard.KEY_3,
		Keyboard.KEY_4,
		Keyboard.KEY_5,
		Keyboard.KEY_6,
		Keyboard.KEY_7
	};
	private static final int[] HIGHLIGHT_RANGES = new int[] {2, 3, 4, 5, 6, 7, 8};
	private int currentHighlightDistance = 1;
	
	// By default we'll keep 20x20 chunks in our cache, which should hopefully let
	// us stay ahead of the camera
	// TODO: keep this at 8, or back up to 10?
	private final int loadChunkRange = 8;
	
	// set to true when the program is finished 
	private boolean done 				= false; 
	// are we full screen
    private boolean fullscreen 			= false; 
    // window title
    private final String app_version    = "2.7 Maintenance Branch 5";
    private final String app_name       = "Minecraft X-Ray";
    private final String windowTitle 	= app_name + " " + app_version; 

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
    public Texture portalTexture;
    
    // the textures used by the minimap
    private Texture minimapTexture;
    private Texture minimapArrowTexture;
    
    // Whether or not we're showing bedrock
    private boolean render_bedrock = false;
    
    // the minecraft level we are exploring
    private MinecraftLevel level;
    
    // the current block (universal coordinate) where the camera is hovering on
    private int levelBlockX, levelBlockZ;


	
	// the current and previous chunk coordinates where the camera is hovering on
	private int currentLevelX, currentLevelZ;
    
	// wheter we are loading a level
	private boolean loading = false;
	private long loadingStart = 0;
	private float loadingProgress  = 0;
	
	// synchronization object, a bit redundant now ... but potentially the loader could cause a conflict with the drawing operation
	private Object lock = new Object();
	
	// we render to a display list and use that later for quick drawing, this is the index
	private int worldDisplayListNum;
	private int visibleOresListNum;
	
	// wheter we need to recreate the display list using the updated level data
	private boolean needToRedrawWorld = false;
	
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
	private boolean mapLoaded = false;
	private boolean map_load_started = false;
	
	// the available world numbers
	private ArrayList<WorldInfo> availableWorlds;
	private int selectedWorld;
	
	// the world chunks we still need to load
	private LinkedList<Block> mapChunksToLoad;
	
	// the total chunks in this world
	private int totalMapChunks = 0;
	
	// the current (selected) world number
	private WorldInfo world = null;
	
	// the currently pressed key
	private int keyPressed = -1;
	
	// the index to the available worlds array
	private int worldSelectIndex = 0;
	
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
	
	// How long are we allowed to spend loading chunks before we update?
	private long max_chunkload_time = Sys.getTimerResolution() / 10;  // a tenth of a second
	
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
     * Loads any pending chunks, but won't exceed max_chunkload_time timer ticks.
     */
    public void loadPendingChunks()
    {
    	Block b;
    	long time = Sys.getTime();
		while (!mapChunksToLoad.isEmpty())
		{
			// Load and draw the chunk
			b = (Block) mapChunksToLoad.pop();
			//System.out.println("Loading chunk " + b.x + "," + b.z);
			drawMapChunkToMap(b.x, b.z);
			
			// Make sure we update the minimap
			minimap_needs_updating = true;
			
			// If we've taken too long, break out so the GUI can update
			if (initial_load_done && Sys.getTime() - time  > max_chunkload_time)
			{
				break;
			}
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
        	minimapTexture 			= TextureTool.allocateTexture(2048,2048);
			minimapArrowTexture 	= TextureTool.allocateTexture(32,32);
			fpsTexture				= TextureTool.allocateTexture(128, 32);
			levelInfoTexture		= TextureTool.allocateTexture(128,128);
			
			drawMinimapArrowImage();
			
			// minecraft textures
			BufferedImage minecraftTextureImage 	= MineCraftEnvironment.getMinecraftTexture();
			minecraftTexture 						= TextureTool.allocateTexture(minecraftTextureImage, GL11.GL_NEAREST);
			minecraftTexture.update();
			
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
    	this.mapchange_redraw_range = this.visible_chunk_range-2;
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
     * @param worldNum
     */
    private void setMinecraftWorld(WorldInfo world) {
    	this.world = world;
    	this.level =  new MinecraftLevel(world, minecraftTexture, portalTexture);
    	
    	// determine which chunks are available in this world
    	mapChunksToLoad = new LinkedList<Block>();
		totalMapChunks = 0;
		
		moveCameraToPlayerPos();
    }

    private void moveCameraToSpawnPoint() {
    	Block spawnPoint = level.getSpawnPoint();
		this.camera.getPosition().set(spawnPoint.x, spawnPoint.y-1, spawnPoint.z);
		this.camera.setYawAndPitch(0,0);
		initial_load_queued = false;
		initial_load_done = false;
		this.triggerChunkLoads();
    }
    
    private void moveCameraToPlayerPos() {
    	Block playerPos = level.getPlayerPosition();
    	this.camera.getPosition().set(playerPos.x, playerPos.y, playerPos.z);
		this.camera.setYawAndPitch(180+level.getPlayerYaw(),level.getPlayerPitch());
		initial_load_queued = false;
		initial_load_done = false;
		this.triggerChunkLoads();
    }
    
    private void triggerChunkLoads()
    {
    	int chunkX = level.getChunkX((int) -camera.getPosition().x);
    	int chunkZ = level.getChunkZ((int) -camera.getPosition().z);
 
    	if (initial_load_queued)
    	{
    		// TODO: expire items from the cache
    		int dx = chunkX - cur_chunk_x;
    		int dz = chunkZ - cur_chunk_z;
    		
    		// X
    		if (dx < 0)
    		{
    			System.out.println("Loading in chunks from the X range " + (cur_chunk_x-1-loadChunkRange) + " to " + (chunkX-loadChunkRange) + " (going down)");
    			for (int lx=cur_chunk_x-1-loadChunkRange; lx >= chunkX-loadChunkRange; lx--)
    			{
    				for (int lz=chunkZ-loadChunkRange; lz<=chunkZ+loadChunkRange; lz++)
    				{
    					level.clearChunk(lx, lz);
    					mapChunksToLoad.add(new Block(lx, 0, lz));
    				}
    			}
    		}
    		else if (dx > 0)
    		{
    			System.out.println("Loading in chunks from the X range " + (cur_chunk_x+1+loadChunkRange) + " to " + (chunkX+loadChunkRange) + " (going up)");
    			for (int lx=cur_chunk_x+1+loadChunkRange; lx <= chunkX+loadChunkRange; lx++)
    			{
    				for (int lz=chunkZ-loadChunkRange; lz<=chunkZ+loadChunkRange; lz++)
    				{
    					level.clearChunk(lx, lz);
    					mapChunksToLoad.add(new Block(lx, 0, lz));
    				}
    			}
    		}
    		
    		// Z
    		if (dz < 0)
    		{
    			System.out.println("Loading in chunks from the Z range " + (cur_chunk_z-1-loadChunkRange) + " to " + (chunkZ-loadChunkRange) + " (going down)");
    			for (int lx=chunkX-loadChunkRange; lx<=chunkX+loadChunkRange; lx++)
    			{
    				for (int lz=cur_chunk_z-1-loadChunkRange; lz >= chunkZ-loadChunkRange; lz--)
    				{
    					level.clearChunk(lx, lz);
    					mapChunksToLoad.add(new Block(lx, 0, lz));
    				}
    			}
    		}
    		else if (dz > 0)
    		{
    			System.out.println("Loading in chunks from the Z range " + (cur_chunk_z+1+loadChunkRange) + " to " + (chunkZ+loadChunkRange) + " (going up)");
    			for (int lx=chunkX-loadChunkRange; lx<=chunkX+loadChunkRange; lx++)
    			{
    				for (int lz=cur_chunk_z+1+loadChunkRange; lz <= chunkZ+loadChunkRange; lz++)
    				{
    					level.clearChunk(lx, lz);
    					mapChunksToLoad.add(new Block(lx, 0, lz));
    				}
    			}
    		}
    	}
    	else
    	{
    		System.out.println("Loading world from X: " + (chunkX-loadChunkRange) + " - " + (chunkX+loadChunkRange) + ", Z: " + (chunkZ-loadChunkRange) + " - " + (chunkZ+loadChunkRange));
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
       
    	if (Mouse.isButtonDown(0) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
        	MOVEMENT_SPEED = 30.0f;
        } else if (Mouse.isButtonDown(1) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
        	MOVEMENT_SPEED = 3.0f;
        } else {
        	MOVEMENT_SPEED = 10.0f;
        }
        // check for various keys
        if (Keyboard.isKeyDown(Keyboard.KEY_W))//move forward
        {
            camera.walkForward(MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S))//move backwards
        {
            camera.walkBackwards(MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A))//strafe left
        {
            camera.strafeLeft(MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D))//strafe right
        {
            camera.strafeRight(MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))//strafe right
        {
            camera.moveUp(MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))//strafe right
        {
            camera.moveUp(-MOVEMENT_SPEED*timeDelta);
            triggerChunkLoads();
        }
       
        if(Keyboard.isKeyDown(Keyboard.KEY_TAB) && keyPressed != Keyboard.KEY_TAB) {
        	mapBig = !mapBig;
        	keyPressed = Keyboard.KEY_TAB;  
        }

        if(!loading) {
        	needToReloadWorld = false;
	        for(int i=0;i<mineralToggle.length;i++) {
	        	if(Keyboard.isKeyDown(Keyboard.KEY_F1 + i) && keyPressed != Keyboard.KEY_F1 + i) {
	        		keyPressed = Keyboard.KEY_F1 + i;
	        		mineralToggle[i] = !mineralToggle[i];
	        		needToReloadWorld = true;
	        	}
	        }
	        if(needToReloadWorld) {
	        	invalidateSelectedChunks();
	        }
        }
        
        if(Keyboard.isKeyDown(Keyboard.KEY_F10) && keyPressed != Keyboard.KEY_F10) {    // Is F10 Being Pressed?
        	keyPressed = Keyboard.KEY_F10;                                     
            switchFullScreenMode();                                   // Toggle Fullscreen / Windowed Mode
        }
        
        if(Keyboard.isKeyDown(Keyboard.KEY_F) && keyPressed != Keyboard.KEY_F) {    // Is F9 Being Pressed?
        	keyPressed = Keyboard.KEY_F;                                     
          
            setLightMode(!lightMode); // toggle torch
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_H) && keyPressed != Keyboard.KEY_H) {    // Is F8 Being Pressed?
        	keyPressed = Keyboard.KEY_H;                                     
            highlightOres = !highlightOres;                                  					// Toggle torch
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_HOME) && keyPressed != Keyboard.KEY_HOME) {    // Is HOME Being Pressed?
        	keyPressed = Keyboard.KEY_HOME;                                     
        	moveCameraToSpawnPoint();                              					//Move to spawnpoint
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_END) && keyPressed != Keyboard.KEY_END) {    // Is HOME Being Pressed?
        	keyPressed = Keyboard.KEY_END;                                     
        	moveCameraToPlayerPos();                              					//Move to spawnpoint
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_ADD) && keyPressed != Keyboard.KEY_ADD) {    // Is PLUS Being Pressed?
        	keyPressed = Keyboard.KEY_ADD;                                     
        	incLightLevel();                             					// Increase Light Level
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_EQUALS) && keyPressed != Keyboard.KEY_EQUALS) {    // Is PLUS Being Pressed?
         	keyPressed = Keyboard.KEY_EQUALS;                                     
        	incLightLevel();                             					// Increase Light Level
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_MINUS) && keyPressed != Keyboard.KEY_MINUS) {    // Is PLUS Being Pressed?
        	keyPressed = Keyboard.KEY_MINUS;                                     
        	decLightLevel();                             					// Increase Light Level
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_SUBTRACT) && keyPressed != Keyboard.KEY_SUBTRACT) {    // Is PLUS Being Pressed?
        	keyPressed = Keyboard.KEY_SUBTRACT;                                     
        	decLightLevel();                             					// Increase Light Level
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_GRAVE) && keyPressed != Keyboard.KEY_GRAVE) {    // toggle level info
        	keyPressed = Keyboard.KEY_GRAVE;                                     
        	levelInfoToggle = !levelInfoToggle;                             					// toggle level info
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_B) && keyPressed != Keyboard.KEY_B) { // Toggle bedrock rendering
        	keyPressed = Keyboard.KEY_B;
        	render_bedrock = !render_bedrock;
    		invalidateSelectedChunks(true);
        }

        // handle chunk ranges
        for(int i = 0; i<CHUNK_RANGES.length;i++) {
	        if(Keyboard.isKeyDown(CHUNK_RANGES_KEYS[i]) && keyPressed != CHUNK_RANGES_KEYS[i]) {    // Is PLUS Being Pressed?
	        	keyPressed = CHUNK_RANGES_KEYS[i];                                     
	        	setChunkRange(i);                             					// Increase Light Level
	        }
        }

        // handle highlight distances
        for(int i = 0; i<HIGHLIGHT_RANGES.length;i++) {
	        if(Keyboard.isKeyDown(HIGHLIGHT_RANGES_KEYS[i]) && keyPressed != HIGHLIGHT_RANGES_KEYS[i]) {
	        	keyPressed = HIGHLIGHT_RANGES_KEYS[i];                                     
	        	setHighlightRange(i);
	        }
        }        
        	
        	
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {       // Release the mouse on Escape
            Mouse.setGrabbed(false);
        }
        if (Mouse.isButtonDown(0)) {
        	Mouse.setGrabbed(true);							// Grab the mouse if we're clicked
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_Q) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {			// Exit if "Ctrl-Q" is pressed
        	done = true;
        }
        if(Display.isCloseRequested()) {                     // Exit if window is closed
            done = true;
        }
        if(keyPressed != -1) {
	        if(!Keyboard.isKeyDown(keyPressed)) {
	        	keyPressed = -1;
	        }
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
     * Draw the current and spawn position to the minimap
     */
    private void drawMapMarkersToMinimap() {
    	Graphics2D g = minimapTexture.getImage().createGraphics();
    	
    	Block spawn = level.getSpawnPoint();
    	Block player = level.getPlayerPosition();
    	
    	int px = 1024-player.x;
		int py = 1024-player.z;
		int sx = 1024-spawn.x;
		int sy = 1024-spawn.z;
    	
    	
    	g.setColor(Color.red.brighter());
    	g.setStroke(new BasicStroke(2));
    	g.drawOval(sx-6, sy-6, 11, 11);
    	g.drawLine(sx-8, sy, sx+8, sy);
    	g.drawLine(sx, sy-8, sx, sy+8);
    	
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
    		mapLoaded = true;
       		drawMapMarkersToMinimap();
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
        if(!loading && (currentCameraPosX != levelBlockX || currentCameraPosZ != levelBlockZ || needToReloadWorld)) {
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
		drawLoadingBar();
		drawLevelInfo();
				
		setOrthoOff(); // back to 3d mode
    }
	
	private void updateLevelInfo() {
		Graphics2D g = levelInfoTexture.getImage().createGraphics();
		g.setBackground(Color.BLUE);
		g.clearRect(0, 0, 128, 128);
		g.setColor(Color.WHITE);
		g.fillRect(2, 2, 124, 124);
		g.setFont(ARIALFONT);
		int chunkX = level.getChunkX(levelBlockX);
		int chunkZ = level.getChunkZ(levelBlockZ);
		g.setColor(Color.BLACK);
		g.drawString("Chunk X:", 5, 22);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(chunkX), 90, 22);

		g.setColor(Color.BLACK);
		g.drawString("Chunk Z:", 5, 22+16);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(chunkZ), 90, 22+16);
		
		g.setColor(Color.BLACK);
		g.drawString("World X:", 5, 22+32);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(levelBlockX), 90, 22+32);

		g.setColor(Color.BLACK);
		g.drawString("World Z:", 5, 22+16+32);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString(levelBlockZ), 90, 22+16+32);
		
		long heapSize = Runtime.getRuntime().totalMemory(); 
		g.setColor(Color.BLACK);
		g.drawString("Memory Used", 5, 22+16+32+25);
		g.setColor(Color.RED.darker());
		g.drawString(Integer.toString((int) (heapSize/1024/1024)) + " MB", 20, 22+16+32+25+20);
		
		
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
	 * draw the loading bar 
	 */
	private void drawLoadingBar() {
		if(loading) {
		/*	long time = System.currentTimeMillis() - loadingStart;
			float alpha = (time % 1000) / 1000.0f;
			if(time % 2000 > 1000) alpha = 1.0f - alpha;*/
			
	    	float bx = screenWidth/2.0f - 200.0f;
	    	float ex = bx+400.0f;
	    	float by = 20;
	    	float ey = 50;
	    	
	    	float px = ((ex-bx)*loadingProgress) + bx;
	    	
	    	float alpha = 1.0f;
	    	if(loadingProgress < 0.1f) {
	    		alpha = loadingProgress * 10.0f;
	    	}
	    	if(loadingProgress > 0.9f) {
	    		alpha = 1.0f - ((loadingProgress-0.9f) * 10.0f);
	    	}
	    	GL11.glDisable(GL11.GL_TEXTURE_2D);
	    	GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
	    	GL11.glLineWidth(2);
	    	
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
	    	
	    	GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}
	
	/***
	 * Draw the mineral toggles
	 */
	private void drawMineralToggle() {
		int barWidth = 128+10+32;
		int barHeight = 42;
		int maxCols = 5;
		float mineralTogglebarLength = (mineralToggleTextures.length % maxCols) * barWidth;
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
			float halfMapWidth = 2048/2.0f; // 840

			minimapTexture.bind();
			
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
			GL11.glPushMatrix();
				GL11.glTranslatef((screenWidth/2.0f)-currentCameraPosX, (screenHeight/2.0f)-currentCameraPosZ, 0.0f);
				GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		
					GL11.glTexCoord2f(0, 0);
					GL11.glVertex2f(-halfMapWidth, -halfMapWidth);
		
					GL11.glTexCoord2f(1, 0);
					GL11.glVertex2f(+halfMapWidth, -halfMapWidth);
		
					GL11.glTexCoord2f(0, 1);
					GL11.glVertex2f(-halfMapWidth, +halfMapWidth);
		
					GL11.glTexCoord2f(1, 1);
					GL11.glVertex2f(+halfMapWidth, +halfMapWidth);
		
				GL11.glEnd();
			GL11.glPopMatrix();
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1f);
			
			SpriteTool.drawSpriteAndRotateAndScale(minimapArrowTexture, screenWidth/2.0f, screenHeight/2.0f, camera.getYaw(),0.5f);
		} else {
			// the minimap
			// a bit more interesting
			// I set the minimap to 200 wide and tall
			float vSizeFactor = 200.0f/2048.0f;
			
			float vTexX = 0.5f + (1.0f/2048.0f) * currentCameraPosX;
			float vTexY = 0.5f + (1.0f/2048.0f) * currentCameraPosZ;
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
			
			SpriteTool.drawSpriteAndRotateAndScale(minimapArrowTexture, screenWidth-100, 100, camera.getYaw(),0.5f);
		}
	}
	
	/***
	 * draws a chunk to the (mini) map
	 * @param x
	 * @param z
	 */
	public void drawMapChunkToMap(int x, int z) {
		level.loadChunk(x, z);
		 
		byte[] chunkData = level.getChunkData(x,z);
		 
		 Graphics2D g = minimapTexture.getImage().createGraphics();
		  for(int zz = 0; zz<16; zz++) {
			for(int xx =0; xx<16; xx++) {
				int highestY = 0;
				// determine the top most visible block
				for(int yy =1; yy<128; yy++) {
					int blockOffset = yy + (zz * 128) + (xx * 128 * 16);
					byte blockData = chunkData[blockOffset];
					
					if(MineCraftConstants.blockDataToSpriteSheet[blockData] > -1) {
						if(yy > highestY) {
							highestY = yy;
						}
					}
				}
				// draw it if we can do something with it
				int blockOffset = highestY + (zz * 128) + (xx * 128 * 16);
				byte blockData = chunkData[blockOffset];
				if (blockData > -1) {
					Color blockColor = MineCraftConstants.blockColors[blockData];
					if(blockColor != null) {
						g.setColor(blockColor);
						int px = 1024+(x*16)+xx;
						int py = 1024+(z*16)+zz;
						g.drawLine(px, py, px, py); // yes, this can be optimized (draw to texture instead of image), but meh...
					}
				}
			}
		}
	}
	
	/***
	 * Draws the minimap arrow image (to the texture)
	 */
    private void drawMinimapArrowImage() {
    	Graphics2D g = minimapArrowTexture.getImage().createGraphics();
    	//g.clearRect(0, 0, 32, 32);
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
/*
	// top
GL11.glVertex3f(wx, 	128, 	wz);
GL11.glVertex3f(wx+16, 	128, 	wz);

GL11.glVertex3f(wx+16, 	128, 	wz);
GL11.glVertex3f(wx+16, 	128, 	wz+16);

GL11.glVertex3f(wx+16, 	128, 	wz+16);
GL11.glVertex3f(wx+16, 	128, 	wz+16);

GL11.glVertex3f(wx, 	128, 	wz+16);
GL11.glVertex3f(wx, 	128, 	wz+16);


// sides
GL11.glVertex3f(wx, 	128, 	wz);
GL11.glVertex3f(wx, 	0, 		wz);

GL11.glVertex3f(wx+16, 	128, 	wz);
GL11.glVertex3f(wx+16, 	0, 		wz);

GL11.glVertex3f(wx+16, 	128, 	wz+16);
GL11.glVertex3f(wx+16, 	0, 		wz+16);

GL11.glVertex3f(wx, 	128, 	wz+16);
GL11.glVertex3f(wx, 	0, 		wz+16);

// bottom
GL11.glVertex3f(wx, 	0, 	wz);
GL11.glVertex3f(wx+16, 	0, 	wz);

GL11.glVertex3f(wx+16, 	0, 	wz);
GL11.glVertex3f(wx+16, 	0, 	wz+16);

GL11.glVertex3f(wx+16, 	0, 	wz+16);
GL11.glVertex3f(wx+16, 	0, 	wz+16);

GL11.glVertex3f(wx, 	0, 	wz+16);
GL11.glVertex3f(wx, 	0, 	wz+16);
*/
