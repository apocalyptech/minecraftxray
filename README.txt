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
list of changes since X-Ray 2.7.  X-Ray uses various third-party libraries
for other tasks.  See COPYING.txt for details on their licensing, and
COPYING-*.txt for copies of the licenses themselves.

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
minecraft_xray.exe or minecraft_xray.bat.

Linux and OSX users should be able to doubleclick on either minecraft_xray.sh
or minecraft_xray_osx.command (the files are actually identical).

PROPERTIES FILE
---------------

When X-Ray starts up for the first time, it will write out a properties file
which you can edit if you want to change the keybindings or which resources
are available for highlighting.  This will be installed essentially right
alongside the ".minecraft" directory that Minecraft itself uses.

File locations:

    Windows: %appdata%\.minecraft_xray\xray.properties
    OSX: ~/Library/Application Support/.minecraft_xray/xray.properties
    Linux: ~/.minecraft_xray/xray.properties 

This is just a text file, and the format should be fairly obvious.  For the
keyboard mappings, you should use the key names found at the LWJGL site:

    http://www.lwjgl.org/javadoc/constant-values.html#org.lwjgl.input.Keyboard.KEY_1

But without the "KEY_" prefix.

You can also set which resources you want to be highlightable in the app.
For specifying resource highlights, you should use the following names:

    BED                     IRON_BARS                REDSTONE_TORCH_ON
    BEDROCK                 IRON_BLOCK               REDSTONE_WIRE
    BOOKSHELF               IRON_DOOR                RED_MUSHROOM
    BRICK                   IRON_ORE                 RED_ROSE
    BRICK_STAIRS            JACK_O_LANTERN           SAND
    BROWN_MUSHROOM          JUKEBOX                  SANDSTONE
    BURNING_FURNACE         LADDER                   SAPLING
    CACTUS                  LAPIS_LAZULI_BLOCK       SIGNPOST
    CAKE                    LAPIS_LAZULI_ORE         SILVERFISH
    CHEST                   LAVA                     SLAB
    CLAY                    LEAVES                   SNOW
    COAL_ORE                LEVER                    SNOW_BLOCK
    COBBLESTONE             LILY_PAD                 SOUL_SAND
    COBBLESTONE_STAIRS      MELON                    SPONGE
    CROPS                   MELON_STEM               STATIONARY_LAVA
    DEAD_SHRUB              MINECART_TRACKS          STATIONARY_WATER
    DETECTOR_RAIL           MOB_SPAWNER              STONE
    DIAMOND_BLOCK           MOSSY_COBBLESTONE        STONE_BRICK
    DIAMOND_ORE             MYCELIUM                 STONE_BRICK_STAIRS
    DIRT                    NETHER_BRICK             STONE_BUTTON
    DISPENSER               NETHER_FENCE             STONE_PRESSURE_PLATE
    DOUBLE_SLAB             NETHER_STAIRS            SUGARCANE
    FARMLAND                NETHER_WART              TALL_GRASS
    FENCE                   NETHERRACK               TNT
    FENCE_GATE              NOTE_BLOCK               TORCH
    FIRE                    OBSIDIAN                 TRAPDOOR
    FURNACE                 PISTON_BODY              VINE
    GLASS                   PISTON_HEAD              WALL_SIGN
    GLASS_PANE              PISTON_STICKY_BODY       WATER
    GLOWING_REDSTONE_ORE    PLANK                    WEB
    GLOWSTONE               PORTAL                   WOOD
    GOLD_BLOCK              POWERED_RAIL             WOODEN_DOOR
    GOLD_ORE                PUMPKIN                  WOODEN_PRESSURE_PLATE
    GRASS                   PUMPKIN_STEM             WOODEN_STAIRS
    GRAVEL                  REDSTONE_ORE             WOOL
    HUGE_BROWN_MUSHROOM     REDSTONE_REPEATER_OFF    WORKBENCH
    HUGE_RED_MUSHROOM       REDSTONE_REPEATER_ON     YELLOW_FLOWER
    ICE                     REDSTONE_TORCH_OFF

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
        Toggle Beta 1.9 Fences:      C
        Toggle Silverfish highlight: V
        Toggle chunk borders:        U
        Toggle slime chunks:         M

    Other
        Toggle Fullscreen:       BACKSPACE
        Toggle Level Info:       ` (grave accent)
        Toggle Rendering Info:   R (on by default)
        Reload Map from Disk:    =
        Show large map:          TAB
        Release Mouse:           ESC
        Show Keyboard Reference: Y
        Quit:                    CTRL-Q

EXTRA BLOCK DEFINITIONS
-----------------------

As of version 3.3.0, X-Ray includes a mechanism to allow the user to
define custom block types.  X-Ray will read any block definition file
found inside the "blockdefs" directory inside .minecraft_xray.  This
is located at:

    Windows: %appdata%\.minecraft_xray\blockdefs\
    OSX: ~/Library/Application Support/.minecraft_xray/blockdefs/
    Linux: ~/.minecraft_xray/blockdefs/

Each file must have a ".yaml" extension, and X-Ray won't read any file
named "minecraft.yaml".  It would be best practice to name the file
after the mod you're intending to support, such as "aether.yaml".  The
file format is in YAML 1.1.  There should be some very detailed docs
contained inside the global "minecraft.yaml" file (you can find this
in X-Ray's own "blockdefs" directory, where you unpacked it).  Examples
(and a copy of the global minecraft.yaml file) can be found here:

    http://apocalyptech.com/minecraft/xray/modsupport.php

As mentioned above, X-Ray will automatically attempt to load any YAML
file it finds in the blockdefs directory, and it will display which ones
it was able to load on the opening dialog.  If your file doesn't show up
in the list, there's probably an error in it - you should be able to
find that error in the file minecraft_xray_output_log.txt in the root
X-Ray directory, unless you launched X-Ray from the EXE version.  To
get the error report on Windows, launch X-Ray using the .BAT instead.
Linux and OSX users will see the errors on the console X-Ray was launched
from, as well.


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

Minecraft Beta 1.9 changed the way fences work slightly, so that they will
"connect" up to adjacent solid blocks.  X-Ray will now, by default, render
fences that way, but you can toggle it with the "C" key, in case you're
viewing pre-1.9 maps.

By default, X-Ray will highlight Silverfish blocks with a red tint.  You can
toggle this on and off with the "V" key.

The "U" key can be used to toggle the rendering of chunk borders.  This will
draw a transparent box around the chunk the camera is currently in, so it's
easy to see what's inside your current chunk and what isn't.

The "Slime Chunk" option will turn on the highlighting of chunks which
should be able to spawn Slimes.  The highlighting will actually only occur
on the bottom part of the map, where the slimes themselves are actually
capable of spawning.  The equation used to calculate this was taken from
http://www.minecraftwiki.net/wiki/Slime#Spawning in early October, 2011,
when Beta 1.9-pre2 was out.  The equation may or may not be valid for
earlier or later versions of Minecraft, but should be at least valid
for Minecraft Beta 1.6 through Beta 1.9.  The default key to toggle this
highlighting is "M".

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

OVERRIDING TEXTURES
-------------------

In general, X-Ray will attempt to use the same texture pack that Minecraft is
using, but there may be some circumstances where you want X-Ray to use a
particular texture.

X-Ray will look in three locations for the texture information to load, in this
order:

  1) Inside the following directory, as an override:

        Windows: %appdata%\.minecraft_xray\textures\
        OSX: ~/Library/Application Support/.minecraft_xray/textures/
        Linux: ~/.minecraft_xray/textures/

  2) From the texture pack that Minecraft itself is set to use

  3) Finally, from the builtin texture that Minecraft itself uses. This might
     be a custom texture pack if you've patched the Minecraft JAR file directly
     with a texture pack, with xau's mcpatcher or the like.

The override texture directory mirrors the internal structure of the
texturepacks, but should not be a zipfile. Right now there's really only two
files that X-Ray will end up reading from this directory: terrain.png and
misc/water.png. So, rather than packing those inside a zipfile, just put them
inside the "textures" directory and restart X-Ray, if you wanted to manually
override a texture.

Note that this *will* work for files specified in custom block definition
files (as described above).  For instance, if you're using Aethermod and want
to override the "Icestone.png" file, you'd put your own Icestone.png file into
.minecraft_xray/textures/aether/blocks/Icestone.png.
