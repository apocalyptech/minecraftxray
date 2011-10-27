package com.apocalyptech.minecraft.xray.dialog;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.lwjgl.input.Keyboard;

import com.apocalyptech.minecraft.xray.MinecraftConstants.KEY_ACTIONS;

/***
 * KeyField class is used for the KeyMapDialog. It is a Field that 
 * will contain the LWJGL string for the keyboard button that is pressed
 * while the field is in focus.
 * 
 * @author Eleazar
 */
public class KeyField extends JTextField{
	private KEY_ACTIONS keyAction;

	/**
	 * Constructs a new KeyField given the string to populate it with
	 * and the KEY_ACTION it represents 
	 * @param ka
	 * @param s
	 */
	public KeyField(KEY_ACTIONS ka, String s) {
		super(10);
		keyAction = ka;
		this.setEditable(false);
		this.setText(s);
		this.setBackground(Color.WHITE);
		this.addKeyListener(new OneKeyAdapter(this));
	}
	
	/**
	 * Gets the KEY_ACTION
	 * @return The KEY_ACTION this matches with
	 */
	public KEY_ACTIONS getKeyAction() {
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