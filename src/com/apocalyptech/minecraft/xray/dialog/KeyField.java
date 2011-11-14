/**
 * Copyright (c) 2010-2011, Christopher J. Kucera, Eleazar Vega-Gonzalez,
 *      and Saxon Parker
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

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextField;

import com.apocalyptech.minecraft.xray.MinecraftConstants.KEY_ACTION;

/***
 * KeyField class is used for the KeyHelpDialog. It is a Field that 
 * will contain the LWJGL string for the keyboard button that is pressed
 * while the field is in focus.
 * 
 * @author Eleazar
 */
public class KeyField extends JTextField{
	private KEY_ACTION keyAction;
	private KeyPanel panel;
	private Color bgColorNormal;
	private Color bgColorActive;

	/**
	 * Constructs a new KeyField given the string to populate it with
	 * and the KEY_ACTION it represents 
	 * @param ka
	 * @param s
	 */
	public KeyField(KEY_ACTION ka, KeyPanel panel) {
		super(10);
		this.keyAction = ka;
		this.panel = panel;
		this.setEditable(false);
		this.bgColorNormal = Color.WHITE;
		this.bgColorActive = new Color(144, 204, 255);
		this.setBackground(this.bgColorNormal);
		this.setFocusable(false);
		this.addMouseListener(new KeyFieldMouseListener(this));
	}

	/**
	 * What to do when we're clicked
	 */
	public void clicked()
	{
		this.panel.notifyClicked();
		this.setBackground(this.bgColorActive);
	}

	/**
	 * What to do when we're no longer in a "clicked" state
	 */
	public void clickFinish()
	{
		this.setBackground(this.bgColorNormal);
	}

	/*Inner classes below*/

	private class KeyFieldMouseListener implements MouseListener
	{
		KeyField kf;
		KeyFieldMouseListener(KeyField kf)
		{
			this.kf = kf;
		}
		public void mouseClicked(MouseEvent e)
		{
			this.kf.clicked();
		}
		public void mouseEntered(MouseEvent e)
		{
		}
		public void mouseExited(MouseEvent e)
		{
		}
		public void mousePressed(MouseEvent e)
		{
		}
		public void mouseReleased(MouseEvent e)
		{
		}
	}
}
