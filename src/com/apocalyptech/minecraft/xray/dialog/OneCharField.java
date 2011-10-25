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
			//TODO: Deal with cases where AWT and LWJGL Strings are not the same
			//for example: CTRL vs. LCONTROL
			return KeyEvent.getKeyText(ke.getKeyCode()).toUpperCase();
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