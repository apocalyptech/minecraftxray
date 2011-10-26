package com.apocalyptech.minecraft.xray.dialog;

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


public class OneCharField extends JTextField{
	private KEY_ACTIONS keyAction;

	public OneCharField(KEY_ACTIONS ka, String s) {
		super(10);
		keyAction = ka;
		this.setEditable(false);
		this.setText(s);
		this.addKeyListener(new KeyAdapterK(this));
	}
	
	private class KeyAdapterK extends KeyAdapter{
		OneCharField ocf;
		
		public KeyAdapterK(OneCharField ocf) {
			this.ocf = ocf;
		}
		
		public void keyPressed(KeyEvent ke){
			ocf.setText(getKeyText(ke));
		}
		
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
				if(Keyboard.getKeyIndex(KeyEvent.getKeyText(ke.getKeyCode()).toUpperCase()) <= 0)
					System.out.println("KE " +ke.getKeyCode()+ " " + KeyEvent.getKeyText(ke.getKeyCode()) + " " + ke.getKeyLocation());
				return KeyEvent.getKeyText(ke.getKeyCode()).toUpperCase();
			}
		}
	}

	public KEY_ACTIONS getKeyAction() {
		return keyAction;
	}

	public Integer getKeyAsInt() {
		String text = this.getText();
		char ch = text.charAt(0);
		switch(ch) {
		case ';':  return Keyboard.KEY_SEMICOLON;
		case '\'': return Keyboard.KEY_APOSTROPHE;
		case '@':  return Keyboard.KEY_AT;
		case '\\': return Keyboard.KEY_BACKSLASH;
		case ':':  return Keyboard.KEY_COLON;
		case ',':  return Keyboard.KEY_COMMA;
		case '.':  return Keyboard.KEY_PERIOD;
		case '=':  return Keyboard.KEY_EQUALS;
		case '[':  return Keyboard.KEY_LBRACKET;
		case ']':  return Keyboard.KEY_RBRACKET;
		case '-':  return Keyboard.KEY_MINUS;
		case '+':  return Keyboard.KEY_ADD;
		default:
			return Keyboard.getKeyIndex(text);
		}
	}

	protected Document createDefaultModel() {
		return new OneCharDocument();
	}

	static class OneCharDocument extends PlainDocument {

		public void insertString(int offs, String str, AttributeSet a) 
		throws BadLocationException {

			if (str == null) {
				return;
			}
			
			try{
				super.remove(0, 1);
			} catch (BadLocationException ble) {
				//The Document was empty
			}
			super.insertString(0, str, a);
		}
	}
}