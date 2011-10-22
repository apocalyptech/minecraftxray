package com.apocalyptech.minecraft.xray.dialog;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


public class OneCharField extends JTextField{

	public OneCharField() {
		super(1);
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
			char c = str.charAt(0);
			c = Character.toUpperCase(c);
			String s = ""+c;
			
			try{
				super.remove(0, 1);
			} catch (BadLocationException ble) {
				//The Document was empty
			}
			super.insertString(0, s, a);
		}
	}
}