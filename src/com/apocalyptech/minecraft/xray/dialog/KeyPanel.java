/**
 * Copyright (c) 2010-2012, Vincent Vollers and Christopher J. Kucera
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
import java.awt.Insets;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;

import org.lwjgl.input.Keyboard;

/**
 * This is a custom JPanel for our key-binding dialog.  It consists of four
 * elements: a JLabel for the key, an "edit" box for the key, and two JLabels for
 * "before" and "after" informational text.  By default, only the labels are
 * shown, but the panel can be switched from display to edit (and vice versa) at
 * will.
 */
public class KeyPanel extends JPanel
{
	private String beforeStr;
	private String keyStr;
	private String afterStr;

	private KeyHelpDialog kh;

	private KEY_ACTION action;
	private int bound_key;

	private JLabel beforeLabel;
	private JLabel afterLabel;
	private JLabel keyLabel;
	private KeyField keyEdit;
	private JLabel unbindSpacer;
	private JButton unbindButton;

	/**
	 * Create a new KeyPanel
	 * @param kh Our master dialog
	 * @param action The action this KeyPanel represents
	 * @param bound_key The currently-bound key
	 */
	public KeyPanel(KeyHelpDialog kh, Font keyFont, Font buttonFont, KEY_ACTION action, int bound_key)
	{
		super();
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		this.kh = kh;
		this.action = action;

		this.beforeLabel = new JLabel();
		this.beforeLabel.setFont(keyFont);
		this.afterLabel = new JLabel();
		this.afterLabel.setFont(keyFont);
		this.keyLabel = new JLabel();
		this.keyLabel.setFont(keyFont);
		this.keyEdit = new KeyField(action, this);
		this.unbindSpacer = new JLabel("  ");
		this.unbindSpacer.setFont(keyFont);
		this.unbindButton = new JButton("Unbind");
		this.unbindButton.setFont(buttonFont);
		this.unbindButton.setMargin(new Insets(0, 5, 0, 5));
		this.unbindButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				notifyUnbindClicked();
			}
		});

		this.add(this.beforeLabel);
		this.add(this.keyLabel);
		this.add(this.keyEdit);
		this.add(this.afterLabel);
		this.add(this.unbindSpacer);
		this.add(this.unbindButton);

		this.setBoundKey(bound_key);
		this.finishEdit();
	}

	/**
	 * Sets a new bound key for this panel.  Will update all the labels, etc.
	 * @param bound_key The new bound key
	 */
	public void setBoundKey(int bound_key)
	{
		this.bound_key = bound_key;
		this.keyStr = getKeyEnglish(this.action, bound_key);
		this.beforeStr = getKeyExtraBefore(this.action, bound_key);
		this.afterStr = getKeyExtraAfter(this.action, bound_key);

		this.beforeLabel.setText(this.beforeStr);
		this.afterLabel.setText(this.afterStr);
		this.keyLabel.setText(this.keyStr);
		this.keyEdit.setText(this.keyStr);
	}

	/**
	 * Get the action this Panel represents
	 * @return The action
	 */
	public KEY_ACTION getAction()
	{
		return this.action;
	}

	/**
	 * Get the currently-bound key for the Panel
	 * @return The LWJGL key ID
	 */
	public int getBoundKey()
	{
		return this.bound_key;
	}

	/**
	 * Called when our "Unbind" button is clicked.  Report back up
	 * to our master dialog to process.
	 */
	public void notifyUnbindClicked()
	{
		this.kh.notifyUnbindClicked(this);
	}

	/**
	 * Called from our inner KeyField, this is how we
	 * receive notification that we've been clicked.  Passes that
	 * information back up to the master dialog
	 */
	public void notifyClicked()
	{
		this.kh.notifyKeyPanelClicked(this);
		this.unbindButton.setVisible(true);
		this.unbindSpacer.setVisible(true);
	}

	/**
	 * Called from the master dialog, a notification that we are no
	 * longer the actively-clicked Panel.
	 */
	public void clickFinish()
	{
		this.unbindButton.setVisible(false);
		this.unbindSpacer.setVisible(false);
		this.keyEdit.clickFinish();
	}

	/**
	 * Called to move the Panel into "edit" mode
	 */
	public void startEdit()
	{
		this.keyLabel.setVisible(false);
		this.keyEdit.setVisible(true);
		this.afterLabel.setVisible(false);
		this.unbindButton.setVisible(false);
		this.unbindSpacer.setVisible(false);
	}

	/**
	 * Called to move the Panel back into "display" mode
	 */
	public void finishEdit()
	{
		this.keyEdit.setVisible(false);
		this.keyLabel.setVisible(true);
		this.afterLabel.setVisible(true);
		this.unbindButton.setVisible(false);
		this.unbindSpacer.setVisible(false);
	}
}
