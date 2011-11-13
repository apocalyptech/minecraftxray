/**
 * Copyright (c) 2010-2011, Vincent Vollers and Christopher J. Kucera
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

import com.apocalyptech.minecraft.xray.XRay;
import static com.apocalyptech.minecraft.xray.MinecraftConstants.*;

import java.awt.Font;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.List;
import java.util.ArrayList;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.util.Map;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.JSeparator;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.AbstractAction;

import org.lwjgl.input.Keyboard;

public class KeyPanel extends JPanel
{
	private String beforeStr;
	private String keyStr;
	private String afterStr;

	private KeyHelpDialog kh;

	private KEY_ACTION key;
	private int bound_key;

	private JLabel beforeLabel;
	private JLabel afterLabel;
	private JLabel keyLabel;
	private KeyField keyEdit;

	public KeyPanel(KeyHelpDialog kh, Font keyFont, KEY_ACTION key, int bound_key)
	{
		super();
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		this.kh = kh;
		this.key = key;

		this.beforeLabel = new JLabel();
		this.beforeLabel.setFont(keyFont);
		this.afterLabel = new JLabel();
		this.afterLabel.setFont(keyFont);
		this.keyLabel = new JLabel();
		this.keyLabel.setFont(keyFont);
		this.keyEdit = new KeyField(key, this);

		this.add(this.beforeLabel);
		this.add(this.keyLabel);
		this.add(this.keyEdit);
		this.add(this.afterLabel);

		this.setBoundKey(bound_key);
		this.finishEdit();
	}

	public void setBoundKey(int bound_key)
	{
		this.bound_key = bound_key;
		this.keyStr = this.getKeyEnglish();
		this.beforeStr = this.getKeyExtraBefore();
		this.afterStr = this.getKeyExtraAfter();

		this.beforeLabel.setText(this.beforeStr);
		this.afterLabel.setText(this.afterStr);
		this.keyLabel.setText(this.keyStr);
		this.keyEdit.setText(this.keyStr);
	}

	public void notifyClicked()
	{
		this.kh.notifyKeyPanelClicked(this);
	}

	public void clickFinish()
	{
		this.keyEdit.clickFinish();
	}

	private String getKeyEnglish()
	{
		if (Keyboard.getKeyName(this.bound_key).equals("GRAVE"))
		{
			return "`";
		}
		else
		{
			return Keyboard.getKeyName(this.bound_key);
		}
	}

	private String getKeyExtraAfter()
	{
		switch (this.key)
		{
			case SPEED_INCREASE:
				return " / Left Mouse Button (hold)";

			case SPEED_DECREASE:
				return " / Right Mouse Button (hold)";

			default:
				if (Keyboard.getKeyName(this.bound_key).startsWith("NUMPAD"))
				{
					return " (numlock must be on)";
				}
				else if (Keyboard.getKeyName(this.bound_key).equals("GRAVE"))
				{
					return " (grave accent)";
				}
				break;
		}
		return "";
	}

	private String getKeyExtraBefore()
	{
		switch (this.key)
		{
			case QUIT:
				return "CTRL-";
		}
		return "";
	}

	public void startEdit()
	{
		this.keyLabel.setVisible(false);
		this.keyEdit.setVisible(true);
	}

	public void finishEdit()
	{
		this.keyEdit.setVisible(false);
		this.keyLabel.setVisible(true);
	}
}
