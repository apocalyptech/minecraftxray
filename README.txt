ABOUT
-----

Minecraft X-Ray is a program whose primary purpose is to aid in finding
valuable ores and resources inside a Minecraft world. By default, when you
select a resource type to highlight, X-Ray will cause any blocks of that
type to visibly glow within the range of loaded chunks. The glowing can
sometimes be a bit much, so you can also toggle the glowing on/off, which
will still leave all instances of the selected resource visible on the
screen.

Additionally, X-Ray is somewhat useful for taking a look at natural
underground caves, to find out how extensive they are, or even to help
find your way out if you're lost.

The original author of Minecraft X-Ray was plusminus, who was kind enough
to provide the sourcecode for that excellent application.

Minecraft X-Ray is released under the Modified BSD License.
See COPYING.txt for more information, and Changelog.txt for a complete
list of changes since X-Ray 2.7.

See TODO.txt for a list of known bugs and things that I'd like to
implement, and BUILDING.txt if you wanted some info on building the project
yourself.

The official website for Minecraft X-Ray is currently:
    http://apocalyptech.com/minecraft/xray/
	
The official forum link is currently:
    http://www.minecraftforum.net/viewtopic.php?f=1022&t=119356
	
Once again, many thanks to plusminus for writing X-Ray in the first place,
and providing the sourcecode so that it could be extended and maintained.

RUNNING
-------

There isn't an installer for this currently.  Perhaps one day...

Windows users should be able to run the program by just doubleclicking on
minecraft_xray.exe.

OSX users should be able to doubleclick on minecraft_xray_osx.command or
minecraft_xray.sh.

Linux users can run minecraft_xray.sh (actually minecraft_xray_osx.command
should work as well, come to that).

PROPERTIES FILE
---------------

When X-Ray starts up for the first time, it will write out a properties file
which you can edit if you want to change the keybindings or which resources
are available for highlighting.  This will be installed essentially right
alongside the ".minecraft" directory that Minecraft itself uses.

File locations:

    Windows: %appdata%/.minecraft_xray/xray.properties
    OSX: ~/Library/Application Support/.minecraft_xray/xray.properties
    Linux: ~/.minecraft_xray/xray.properties 

This is just a text file, and the format should be fairly obvious.  For the
keyboard mappings, you should use the key names found at the LWJGL site:

    http://www.lwjgl.org/javadoc/constant-values.html#org.lwjgl.input.Keyboard.KEY_1

But without the "KEY_" prefix.

You can also set which resources you want to be highlightable in the app.
For specifying resource highlights, you should use the following names:

    BED                     ICE                      RED_ROSE                 
    BEDROCK                 IRON_BLOCK               SAND                     
    BOOKSHELF               IRON_DOOR                SANDSTONE                
    BRICK                   IRON_ORE                 SAPLING                  
    BROWN_MUSHROOM          JACK_O_LANTERN           SIGNPOST                 
    BURNING_FURNACE         JUKEBOX                  SLAB                     
    CACTUS                  LADDER                   SNOW                     
    CAKE                    LAPIS_LAZULI_BLOCK       SNOW_BLOCK               
    CHEST                   LAPIS_LAZULI_ORE         SOUL_SAND                
    CLAY                    LAVA                     SPONGE                   
    COAL_ORE                LEAVES                   STATIONARY_LAVA          
    COBBLESTONE             LEVER                    STATIONARY_WATER         
    COBBLESTONE_STAIRS      MINECART_TRACKS          STONE                    
    CROPS                   MOB_SPAWNER              STONE_BUTTON             
    DEAD_SHRUB              MOSSY_COBBLESTONE        STONE_PRESSURE_PLATE     
    DETECTOR_RAIL           NETHERRACK               SUGARCANE                
    DIAMOND_BLOCK           NOTE_BLOCK               TALL_GRASS               
    DIAMOND_ORE             OBSIDIAN                 TNT                      
    DIRT                    PISTON_BODY              TORCH                    
    DISPENSER               PISTON_HEAD              TRAPDOOR                 
    DOUBLE_SLAB             PISTON_STICKY_BODY       WALL_SIGN                
    FARMLAND                PLANK                    WATER                    
    FENCE                   PORTAL                   WEB                      
    FIRE                    POWERED_RAIL             WOOD                     
    FURNACE                 PUMPKIN                  WOODEN_DOOR              
    GLASS                   REDSTONE_ORE             WOODEN_PRESSURE_PLATE    
    GLOWING_REDSTONE_ORE    REDSTONE_REPEATER_OFF    WOODEN_STAIRS            
    GLOWSTONE               REDSTONE_REPEATER_ON     WOOL                     
    GOLD_BLOCK              REDSTONE_TORCH_OFF       WORKBENCH                
    GOLD_ORE                REDSTONE_TORCH_ON        YELLOW_FLOWER            
    GRASS                   REDSTONE_WIRE                                     
    GRAVEL                  RED_MUSHROOM                                      

Perhaps someday there'll be an actual GUI for specifying all this.

KEYS
----

Note that currently the mouse buttons cannot be specified in the properties
file, so those functions are hardcoded.  All keyboard commands can be
overridden, though.  The default keybindings are as follows:

    Movement
        Movement:       WASD
        Fly Upward:     SPACE
        Fly Downward:   LEFT CONTROL
        Move Faster:    Left Shift / Left Mouse Button (hold)
        Move Slower:    Right Shift / Right Mouse Button (hold)

    Camera
        Warp to Spawnpoint:         HOME
        Warp to Player Position:    END
        Cycle Up through Presets:   INS
        Cycle Down through Presets: DEL
        Jump to Arbitrary Position: J
        Jump to next dimension:     N
        Jump to previous dimension: P
        Lock to Vertical Axis:      L

    Rendering
        Highlight Ores:              F1 - F10
        Toggle Highlight Glow:       H
        Set Highlight distance:      1 - 7
        Toggle Fullbright:           F
        Toggle Bedrock:              B
        Toggle Water:                T
        Increase Lighting Range:     +
        Decrease Lighting Range:     -
        Set visibility range:        NUMPAD1 - NUMPAD6 (remember numlock)
        Toggle "explored" areas:     E
        Toggle accurate grass sides: G

    Other
        Toggle Fullscreen:      BACKSPACE
        Toggle Level Info:      ` (grave accent)
        Toggle Rendering Info:  R (on by default)
        Reload Map from Disk:   =
        Show large map:         TAB
        Release Mouse:          ESC
        Quit:                   CTRL-Q
		
RENDERING DETAILS
-----------------

There are three main "sliders" available to control how things are
rendered: Visibility range, Highlighting range, and Lighting.

Visibility range specifies how many chunks away from the camera the
app will render at any one time.  The minimum is 3, the maximum is 8.

Highlighting range specifies how many chunks away from the camera the
app will highlight/glow the selected resources that you're looking for.
Often (with more common resources) you'll want to keep this value very
low.  Otherwise it becomes quite difficult to tell where you're actually
going.  For less common resources (like pumpkins or clay), you'll want
to have it set as high as possible, though.  Note that this will never
be able to highlight ores outside the set visibility range.  This
option does nothing if you've toggled ore highlighting off (which is
useful to do sometimes, because even without the glow, X-Ray will
render all instances of the resources you've selected).

Lighting just determines the OpenGL "fog" value.  This is useful to have
a better sense of scale while moving around.  You can toggle into
"fullbright" mode with F, which will disable the fog entirely.

In addition to the sliders, there are a few toggles which let you set
whether to always draw water and bedrock.  Water is on by default, and
bedrock is off by default (though it will of course show up if necessary,
regardless of this setting).

The "explored" area toggle, basically just tints any blocks around torches with
a green color.  This makes it very easy to see where you've explored in
underground caves (and is fairly useless above ground).  It does this in a
7x7x7 cube centered around the torches, so the highlighting can easily "bleed
over" into adjacent tunnels where you might not have actually explored, but
it's usually very easy to tell when that's happened.

The toggle for grass sides will let you toggle the accurate grass sides
on or off.  Until version 3.2.0, Minecraft X-Ray drew grass as a solid block
of green, which I found occasionally handy while hollowing out mountains
and the like, to know where I could still dig out and where I was right up
against the edge.  X-Ray will now default to the more-accurate rendering,
but you can toggle back and forth with the "G" key.

The rendering information popup can be toggled with "R" and is on by
default.  This will let you know what these various settings are set to.

CAMERA OPTIONS
--------------

For singleplayer worlds, there will be two camera presets: the spawnpoint,
and the location of the player.  In this case, INS/DEL isn't really any
different than using HOME/END to jump directly to those presets.  If you
use X-Ray to load a multiplayer world, though, there will also be a camera
preset for each multiplayer user discovered in the world folder, which you
can then cycle through using INS/DEL.  If you've imported a multiplayer
map into singleplayer, the app should create presets for the singleplayer
character AND any multiplayer users still found in the "players" directory.

If your world contains a Nether subdirectory, you can warp back and forth
between them with the "N" key.  The app will attempt to automatically
translate your position based on where you'd go if you had just used a
portal, though this should only be considered a rough estimate.  Note that
especially when in the Nether, it's possible to warp back to the Overworld
at a location where there isn't actually any map data.  Eventually I'll
try to check for this and make sure that you don't warp outside of the
map, but for now just use the camera presets to get back into known
territory if that happens to you.

By default, if you move forward, X-Ray will move directly towards the point
you're looking at, including up/down.  If you want to "lock" the camera to
the vertical axis, you can do so with "L," at which time moving forward/back
will only move the camera horizontally.  You can still move the camera up
and down manually, of course.
