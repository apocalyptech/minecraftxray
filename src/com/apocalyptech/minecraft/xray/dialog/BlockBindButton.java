/**
 * Copyright (c) 2010-2012, Christopher J. Kucera
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
package com.apocalyptech.minecraft.xray.dialog;

import com.apocalyptech.minecraft.xray.BlockType;

import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.ImageIcon;

/**
 * A single button describing a block
 */
public class BlockBindButton
	extends JButton 
{
	private BlockType block;
	private HashMap<Short, ImageIcon> icon_map;

	/**
	 * Constructor, takes in our currently-assigned block, and a map of icons.
	 *
	 * @param block The currently assigned block
	 * @param icon_map A map of block IDs to icons
	 */
	public BlockBindButton(BlockType block, HashMap<Short, ImageIcon> icon_map)
	{
		super();
		this.icon_map = icon_map;
		this.setBlock(block);
	}

	/**
	 * Sets a new BlockType for this button to represent.  Will update its
	 * text and icon.
	 *
	 * @param block The new BlockType
	 */
	public void setBlock(BlockType block)
	{
		this.block = block;

		if (block == null)
		{
			this.setText("(none)");
		}
		else
		{
			if (block.aka != null && !block.name.equalsIgnoreCase(block.aka))
			{
				this.setText(block.name + " (" + block.aka + ")");
			}
			else
			{
				this.setText(block.name);
			}
			if (icon_map.containsKey(block.getId()))
			{
				this.setIcon(icon_map.get(block.getId()));
			}
		}
	}

	/**
	 * Gets the currently-assigned block
	 */
	public BlockType getBlock()
	{
		return this.block;
	}
}
