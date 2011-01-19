package com.plusminus.craft;

/**
 * Simple data container to hold information about a player's location.  Used so
 * that we can switch between users when loading multiplayer maps, though it's
 * also intended to hold the singleplayer location, if available.
 */
class CameraPreset
{
	public int idx;
	public String name;
	public Block block;
	public float yaw;
	public float pitch;
	
	CameraPreset(int idx, String name, Block block, float yaw, float pitch)
	{
		this.idx = idx;
		this.name = name;
		this.block = block;
		this.yaw = yaw;
		this.pitch = pitch;
	}
}