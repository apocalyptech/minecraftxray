/**
 * Copyright (c) 2010-2011, Vincent Vollers, Christopher J. Kucera,
 * 		Eleazar Vega-Gonzalez, and Saxon Parker
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

import com.centerkey.utils.BareBonesBrowserLaunch;

import java.awt.Font;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.List;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.AbstractAction;

import org.lwjgl.input.Keyboard;

/**
 */
public class KeyMapDialog extends JFrame {
	private static final int FRAMEWIDTH = 500;
	private static final int FRAMEHEIGHT = 620;

	private static String window_title = "X-Ray Keyboard Binding Editor";
	private JButton saveButton;
	private JButton cancelButton;

	private GridBagLayout gridBagLayoutManager;
	private JPanel basicPanel;

	private static boolean dialog_showing = false;
	private static KeyMapDialog keymap_dialog;
	
	public static Image iconImage;
	
	private List<KeyField> keyBoxes;

	public static HashMap<KEY_ACTION, Integer> key_mapping;
	private HashMap<KEY_ACTION, Integer> newMap;

	
	/***
	 * Centers this dialog on the screen
	 */
	private void centerDialogOnScreen() {
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension screenSize = t.getScreenSize();
			
		int x = (screenSize.width / 2) - (this.getWidth()/ 2);
		int y = (screenSize.height/ 2) - (this.getHeight()/ 2);

		gridBagLayoutManager = new GridBagLayout();
		
		this.setLocation(x,y);
	}
	
	/***
	 * Layouts all the controls and labels on the dialog using a gridbaglayout
	 */
	private void layoutControlsOnDialog() {
		
		this.getContentPane().setLayout(gridBagLayoutManager);
		GridBagConstraints c = new GridBagConstraints();
		
		float flabel = 0.1f;
		float flist = 1.9f;
		
		keyBoxes = new ArrayList<KeyField>();

		JLabel titleLabel = new JLabel(window_title);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

		Font sectionFont = new Font("Arial", Font.BOLD, 14);
		Font descFont = new Font("Arial", Font.PLAIN, 12);
		Font keyFont = new Font("Arial", Font.BOLD, 12);
		Font noteFont = new Font("Arial", Font.ITALIC, 10);
		JLabel sectionLabel;
		JLabel descLabel;
		JLabel keyLabel;
		KeyField keyBox;
		
		Insets standardInsets = new Insets(5, 5, 5, 5);
		Insets categoryInsets = new Insets(20, 5, 5, 5);
		Insets noBottomInsets = new Insets(5, 5, 0, 5);
		Insets noTopInsets = new Insets(0, 5, 5, 5);
		c.insets = standardInsets;
		c.weighty = .1f;

		// Scrollpane to put stuff into
		JPanel keyPanel = new JPanel();
		GridBagLayout keyLayout = new GridBagLayout();
		keyPanel.setLayout(keyLayout);
		JScrollPane keyScroll = new JScrollPane(keyPanel);
		keyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		keyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		keyScroll.setBorder(null);
		int current_grid_y = 0;
		int bound_key;
		ACTION_CAT curCat = null;
		for (KEY_ACTION key : KEY_ACTION.values())
		{
			bound_key = key_mapping.get(key);
			current_grid_y++;
			c.gridy = current_grid_y;

			if (curCat != key.category)
			{
				curCat = key.category;

				c.gridx = 0;
				c.gridwidth = 2;
				c.anchor = GridBagConstraints.WEST;
				c.insets = categoryInsets;
				sectionLabel = new JLabel(curCat.title);
				sectionLabel.setFont(sectionFont);
				addComponent(keyPanel, sectionLabel, c, keyLayout);

				current_grid_y++;
				c.gridy = current_grid_y;
				c.gridwidth = 1;
				c.insets = standardInsets;
			}

			if (key == KEY_ACTION.TOGGLE_SLIME_CHUNKS)
			{
				c.insets = noBottomInsets;
			}

			c.gridx = 0;
			c.anchor = GridBagConstraints.EAST;
			descLabel = new JLabel(key.desc + ":");
			descLabel.setFont(descFont);
			addComponent(keyPanel, descLabel, c, keyLayout);

			c.gridx = 1;
			c.anchor = GridBagConstraints.WEST;
			switch (key)
			{
	/*			case SPEED_INCREASE:
					keyLabel = new JLabel(Keyboard.getKeyName(bound_key) + " / Left Mouse Button (hold)");
					break;

				case SPEED_DECREASE:
					keyLabel = new JLabel(Keyboard.getKeyName(bound_key) + " / Right Mouse Button (hold)");
					break;

				case QUIT:
					keyLabel = new JLabel("CTRL-" + Keyboard.getKeyName(bound_key));
					break;
*/
				default:
					if (Keyboard.getKeyName(bound_key).startsWith("NUMPAD"))
					{
						keyBox = new KeyField(key, Keyboard.getKeyName(bound_key) + " (numlock must be on)");
					}
					else if (Keyboard.getKeyName(bound_key).equals("GRAVE"))
					{
						keyBox = new KeyField(key, "` (grave accent)");
					}
					else
					{
						keyBox = new KeyField(key , Keyboard.getKeyName(bound_key));
					}
					break;
			}
			keyBox.setFont(keyFont);
			addComponent(keyPanel, keyBox, c, keyLayout);
			keyBoxes.add(keyBox);
			
			// One extra note for slime chunks
			if (key == KEY_ACTION.TOGGLE_SLIME_CHUNKS)
			{
				current_grid_y++;
				c.gridy = current_grid_y;
				c.insets = noTopInsets;

				c.gridx = 0;
				c.anchor = GridBagConstraints.EAST;
				descLabel = new JLabel("");
				addComponent(keyPanel, descLabel, c, keyLayout);

				c.gridx = 1;
				c.anchor = GridBagConstraints.WEST;
				keyLabel = new JLabel("May not be accurate for all Minecraft Versions");
				keyLabel.setFont(noteFont);
				addComponent(keyPanel, keyLabel, c, keyLayout);

				c.insets = standardInsets;
			}
		}

		current_grid_y = 0;

		// A title
		current_grid_y++;
		c.weightx = 1f;
		c.weighty = 0f;
		c.gridx = 0; c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.CENTER;
		addComponent(this.getContentPane(), titleLabel, c);
		
		// Add our scrollpane to the window
		current_grid_y++;
		c.weightx = 1f;  
		c.weighty = 1f;
		c.gridx = 0; c.gridy = current_grid_y;
		c.fill = GridBagConstraints.BOTH;
		addComponent(this.getContentPane(), keyScroll, c);
		
		// Now add the buttons
		c.insets = new Insets(5,15,5,15);
		
		current_grid_y++;
		c.weightx = flist; 
		c.weighty = 0f; 
		c.gridx = 0; c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(this.getContentPane(), saveButton,c);
		
		current_grid_y++;
		c.weightx = flist; 
		c.weighty = 0f; 
		c.gridx = 0; c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(this.getContentPane(), cancelButton,c);
	}
	
	/***
	 * Adds a component to the container and updates the constraints for that component
	 * @param root The contiainer to add the component to
	 * @param comp The component to add to the container
	 * @param constraints The constraints which affect the component
	 */
	private void addComponent(Container root, Component comp, GridBagConstraints constraints) {
		gridBagLayoutManager.setConstraints(comp,constraints);
		root.add(comp);
	}

	/***
	 * Adds a component to the container and updates the constraints for that component
	 * @param root The contiainer to add the component to
	 * @param comp The component to add to the container
	 * @param constraints The constraints which affect the component
	 * @param manager The GridBagLayout to operate on
	 */
	private void addComponent(Container root, Component comp, GridBagConstraints constraints, GridBagLayout manager) {
		manager.setConstraints(comp,constraints);
		root.add(comp);
	}

	/***
	 * Builds the Go and Exit Buttons and attaches the actions to them
	 */
	private void buildButtons() {
        JRootPane rootPane = this.getRootPane();
		
        // The "Save" button
		saveButton	= new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogSave();
			}
		});

		// Key mapping for the Save button. Enter is Save
		KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterStroke, "ENTER");
		rootPane.getActionMap().put("ENTER", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dialogSave();
			}
		});
		
        // The "Cancel" button
		cancelButton	= new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogExit();
			}
		});

		// Key mapping for the Save button. Enter is Save
		KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dialogExit();
			}
		});
		
	}
	


	/**
	 * Actions to perform if the "Jump" button is hit, or otherwise triggered.
	 */
	private void dialogSave()
	{
		newMap = buildHashMap();
		setVisible(false);
		dispose();
		KeyMapDialog.dialog_showing = false;
		synchronized(KeyMapDialog.this) {
			KeyMapDialog.this.notify();
		}
	}
	
	private HashMap<KEY_ACTION, Integer> buildHashMap() {
		HashMap<KEY_ACTION, Integer> map = new HashMap<KEY_ACTION, Integer> ();
		for(KeyField kf :  keyBoxes) {
			map.put(kf.getKeyAction(), kf.getKeyAsInt());
		}
		return map;
	}
	
	private void dialogExit()
	{
		newMap = null;
		setVisible(false);
		dispose();
		KeyMapDialog.dialog_showing = false;
		synchronized(KeyMapDialog.this) {
			KeyMapDialog.this.notify();
		}
	}
	
	
	/***
	 * Creates a new KeyMapDialog
	 * @param windowName the title of the dialog
	 */
	protected KeyMapDialog()
	{
		super(window_title);
		
		if(KeyMapDialog.iconImage != null)
			this.setIconImage(KeyMapDialog.iconImage);
		
		this.setSize(FRAMEWIDTH,FRAMEHEIGHT);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowClosing(WindowEvent e)
			{
				dialogExit();
			}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
		});
		this.setMinimumSize(new Dimension(FRAMEWIDTH, FRAMEHEIGHT));

		centerDialogOnScreen();
	
		buildButtons();
		layoutControlsOnDialog();
		
		validate();
		
		this.setVisible(true);
	}
	
	/***
	 * Pops up the dialog window
	 * @param windowName the title of the dialog
	 */
	public static HashMap<KEY_ACTION, Integer> presentDialog(HashMap<KEY_ACTION, Integer> key_mapping)
	{
		if (dialog_showing)
		{
			KeyMapDialog.keymap_dialog.toFront();
			KeyMapDialog.keymap_dialog.requestFocus();
		}
		else
		{
			KeyMapDialog.key_mapping = key_mapping;
			KeyMapDialog.dialog_showing = true;
			KeyMapDialog.keymap_dialog = new KeyMapDialog();
		}
		
		try {
			synchronized(keymap_dialog) {
				keymap_dialog.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return keymap_dialog.newMap;
	}

	public static void closeDialog()
	{
		if (KeyMapDialog.dialog_showing && KeyMapDialog.keymap_dialog != null)
		{
			KeyMapDialog.keymap_dialog.dialogExit();
		}
	}
}
