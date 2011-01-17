package com.plusminus.craft;

public class PaintingInfo
{
	public float sizex;
	public float sizey;
	public float sizex_tex;
	public float sizey_tex;
	public float offsetx;
	public float offsety;
	
	/**
	 * A new PaintingInfo.  Passed-in units are abstracted from pixels, so
	 * for (for instance) "Wanderer," you'd pass in (1, 2, 0, 4)
	 * 
	 * @param sizex How wide the painting is, in units of 16 pixels
	 * @param sizey How tall the painting is, in units of 16 pixels
	 * @param offsetx Offset in the painting file, in units of 16 pixels
	 * @param offsety Offset in the painting file, in units of 16 pixels
	 */
	public PaintingInfo(int sizex, int sizey, int offsetx, int offsety)
	{
		this.sizex = (float)sizex;
		this.sizey = (float)sizey;
		this.sizex_tex = (float)sizex/16.0f;
		this.sizey_tex = (float)sizey/16.0f;
		this.offsetx = (float)offsetx/16.0f;
		this.offsety = (float)offsety/16.0f;
	}
}