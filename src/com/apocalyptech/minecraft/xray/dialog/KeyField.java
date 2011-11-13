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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.lwjgl.input.Keyboard;

import com.apocalyptech.minecraft.xray.MinecraftConstants.KEY_ACTION;

/***
 * KeyField class is used for the KeyMapDialog. It is a Field that 
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
		//this.addKeyListener(new OneKeyAdapter(this));
		//this.addFocusListener(new KeyFieldFocusListener(this, new Color(144, 204, 255), Color.WHITE));
		this.addMouseListener(new KeyFieldMouseListener(this));
	}

	public void clicked()
	{
		this.panel.notifyClicked();
		this.setBackground(this.bgColorActive);
	}

	public void clickFinish()
	{
		this.setBackground(this.bgColorNormal);
	}
	
	/**
	 * Gets the KEY_ACTION
	 * @return The KEY_ACTION this matches with
	 */
	public KEY_ACTION getKeyAction() {
		return keyAction;
	}

	/**
	 * Returns the LWJGL index for the key this represents
	 * @return The LWJGL index for the key this represent
	 */
	public Integer getKeyAsInt() {
		String text = this.getText();
		return Keyboard.getKeyIndex(text);
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

	/**
	 * KeyFieldFocusListener is a focus listener for our KeyField
	 */
	private class KeyFieldFocusListener implements FocusListener
	{
		KeyField kf;
		Color focusedColor;
		Color defocusedColor;

		/**
		 * Constructor
		 * @param kf Our KeyField
		 */
		public KeyFieldFocusListener(KeyField kf, Color focusedColor, Color defocusedColor)
		{
			this.kf = kf;
			this.focusedColor = focusedColor;
			this.defocusedColor = defocusedColor;
		}

		/**
		 * Updates our background color when we have focus
		 * @param e our FocusEvent
		 */
		public void focusGained(FocusEvent e)
		{
			this.kf.setBackground(this.focusedColor);
		}

		/**
		 * Updates our background color when we lose focus
		 * @param e our FocusEvent
		 */
		public void focusLost(FocusEvent e)
		{
			this.kf.setBackground(this.defocusedColor);
		}
	}
	
	/**
	 * OneKeyAdapter is a custom KeyAdapter for the KeyField
	 */
	private class OneKeyAdapter extends KeyAdapter{
		KeyField kf;
		
		/**
		 * Constructor
		 * @param kf
		 */
		public OneKeyAdapter(KeyField kf) {
			this.kf = kf;
		}
		
		/**
		 * Updates the text with the relevant string based on ke
		 * @param ke 
		 */
		public void keyPressed(KeyEvent ke){
			String text = getKeyText(ke); 
			if(text != null)	kf.setText(text);
		}
		
		/**
		 * Get the appropriate LWJGL String for the KeyEvent.
		 * @param ke
		 * @return Relevant string. Null if n/a.
		 */
		private String getKeyText(KeyEvent ke) {
			int keyCode = ke.getKeyCode();
			int keyLocation = ke.getKeyLocation();
			switch(keyCode) {
			case KeyEvent.VK_NUMPAD0:
				return Keyboard.getKeyName(Keyboard.KEY_NUMPAD0);
			case KeyEvent.VK_NUMPAD1:
				return Keyboard.getKeyName(Keyboard.KEY_NUMPAD1);
			case KeyEvent.VK_NUMPAD2:
				return Keyboard.getKeyName(Keyboard.KEY_NUMPAD2);
			case KeyEvent.VK_NUMPAD3:
				return Keyboard.getKeyName(Keyboard.KEY_NUMPAD3);
			case KeyEvent.VK_NUMPAD4:
				return Keyboard.getKeyName(Keyboard.KEY_NUMPAD4);
			case KeyEvent.VK_NUMPAD5:
				return Keyboard.getKeyName(Keyboard.KEY_NUMPAD5);
			case KeyEvent.VK_NUMPAD6:
				return Keyboard.getKeyName(Keyboard.KEY_NUMPAD6);
			case KeyEvent.VK_NUMPAD7:
				return Keyboard.getKeyName(Keyboard.KEY_NUMPAD7);
			case KeyEvent.VK_NUMPAD8:
				return Keyboard.getKeyName(Keyboard.KEY_NUMPAD8);
			case KeyEvent.VK_NUMPAD9:
				return Keyboard.getKeyName(Keyboard.KEY_NUMPAD9);
			case KeyEvent.VK_NUM_LOCK:
				return Keyboard.getKeyName(Keyboard.KEY_NUMLOCK);
			case KeyEvent.VK_QUOTE:
				return Keyboard.getKeyName(Keyboard.KEY_APOSTROPHE);
			case KeyEvent.VK_OPEN_BRACKET:
				return Keyboard.getKeyName(Keyboard.KEY_LBRACKET);
			case KeyEvent.VK_CLOSE_BRACKET:
				return Keyboard.getKeyName(Keyboard.KEY_RBRACKET);
			case KeyEvent.VK_BACK_QUOTE:
				return Keyboard.getKeyName(Keyboard.KEY_GRAVE);
			case KeyEvent.VK_BACK_SLASH:
				return Keyboard.getKeyName(Keyboard.KEY_BACKSLASH);
			case KeyEvent.VK_CONTROL:
				return Keyboard.getKeyName( keyLocation==2 ? 
						Keyboard.KEY_LCONTROL : Keyboard.KEY_RCONTROL);  
			case KeyEvent.VK_SHIFT:
				return Keyboard.getKeyName( keyLocation==2 ? 
						Keyboard.KEY_LSHIFT : Keyboard.KEY_RSHIFT);
			case KeyEvent.VK_ALT:
				return Keyboard.getKeyName( keyLocation==2 ? 
						Keyboard.KEY_LMETA: Keyboard.KEY_RMETA);
			default:
				if(Keyboard.getKeyIndex(KeyEvent.getKeyText(ke.getKeyCode()).toUpperCase()) != 0)
					return KeyEvent.getKeyText(ke.getKeyCode()).toUpperCase();
				return null;
			}
		}
	}
}
