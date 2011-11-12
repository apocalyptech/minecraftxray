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

import com.centerkey.utils.BareBonesBrowserLaunch;

import java.awt.Font;
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
public class KeyHelpDialog extends JFrame {
	private static final int FRAMEWIDTH = 540;
	private static final int FRAMEHEIGHT = 620;

	private static String window_title = "X-Ray Keyboard Reference";
	private JButton okButton;
	private JButton actionButton;

	private GridBagLayout gridBagLayoutManager;
	private JPanel basicPanel;

	private static boolean dialog_showing = false;
	private static KeyHelpDialog keyhelp_dialog;
	
	public static Image iconImage;

	public static HashMap<KEY_ACTIONS, Integer> key_mapping;

	private ArrayList<JPanel> displayKeys;
	private ArrayList<JPanel> setKeys;

	private enum STATE
	{
		DISPLAY,
		SET
	}
	private STATE curState = STATE.DISPLAY;

	private class JUrlLabel extends JLabel
	{
		private String url;

		public JUrlLabel(String url)
		{
			super();
			this.url = url;
			setup();
		}

		private void setup()
		{
			setText("<html><span style=\"color: #000099;\"><u>" + this.url + "</u></span></html>");
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e)
				{
					BareBonesBrowserLaunch.openURL(url);
				}
			});
		}
	}
	
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

	private String getKeyEnglish(KEY_ACTIONS key, int bound_key)
	{
		if (Keyboard.getKeyName(bound_key).equals("GRAVE"))
		{
			return "`";
		}
		else
		{
			return Keyboard.getKeyName(bound_key);
		}
	}

	private String getKeyExtraAfter(KEY_ACTIONS key, int bound_key)
	{
		switch (key)
		{
			case SPEED_INCREASE:
				return " / Left Mouse Button (hold)";

			case SPEED_DECREASE:
				return " / Right Mouse Button (hold)";

			default:
				if (Keyboard.getKeyName(bound_key).startsWith("NUMPAD"))
				{
					return " (numlock must be on)";
				}
				else if (Keyboard.getKeyName(bound_key).equals("GRAVE"))
				{
					return " (grave accent)";
				}
				break;
		}
		return null;
	}

	private String getKeyExtraBefore(KEY_ACTIONS key, int bound_key)
	{
		switch (key)
		{
			case QUIT:
				return "CTRL-";
		}
		return null;
	}
	
	/***
	 * Layouts all the controls and labels on the dialog using a gridbaglayout
	 */
	private void layoutControlsOnDialog() {
		
		this.getContentPane().setLayout(gridBagLayoutManager);
		GridBagConstraints c = new GridBagConstraints();
		
		float flabel = 0.1f;
		float flist = 1.9f;

		JLabel titleLabel = new JLabel(window_title);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

		Font sectionFont = new Font("Arial", Font.BOLD, 14);
		Font descFont = new Font("Arial", Font.PLAIN, 12);
		Font keyFont = new Font("Arial", Font.BOLD, 12);
		Font noteFont = new Font("Arial", Font.ITALIC, 10);
		JLabel sectionLabel;
		JLabel descLabel;
		JLabel keyLabel;
		JLabel keyLabelBefore;
		JLabel keyLabelAfter;
		
		Insets standardInsets = new Insets(5, 5, 5, 5);
		Insets categoryInsets = new Insets(20, 5, 5, 5);
		Insets noBottomInsets = new Insets(5, 5, 0, 5);
		Insets noTopInsets = new Insets(0, 5, 5, 5);
		c.insets = standardInsets;
		c.weighty = .1f;

		displayKeys = new ArrayList<JPanel>();
		setKeys = new ArrayList<JPanel>();

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
		for (KEY_ACTIONS key : KEY_ACTIONS.values())
		{
			bound_key = key_mapping.get(key);
			current_grid_y++;
			c.gridy = current_grid_y;

			if (curCat != key.category)
			{
				curCat = key.category;

				c.gridx = 0;
				c.gridwidth = 3;
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

			if (key == KEY_ACTIONS.TOGGLE_SLIME_CHUNKS)
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
			JPanel indPanel = new JPanel();
			indPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

			String before = this.getKeyExtraBefore(key, bound_key);
			if (before == null)
			{
				keyLabelBefore = null;
			}
			else
			{
				keyLabelBefore = new JLabel(before);
				keyLabelBefore.setFont(keyFont);
				indPanel.add(keyLabelBefore);
			}

			keyLabel = new JLabel(this.getKeyEnglish(key, bound_key));
			keyLabel.setFont(keyFont);
			indPanel.add(keyLabel);

			String after = this.getKeyExtraAfter(key, bound_key);
			if (after == null)
			{
				keyLabelAfter = null;
			}
			else
			{
				keyLabelAfter = new JLabel(after);
				keyLabelAfter.setFont(keyFont);
				indPanel.add(keyLabelAfter);
			}

			addComponent(keyPanel, indPanel, c, keyLayout);
			displayKeys.add(indPanel);
			c.gridx = 2;

			this.showDisplay();

			indPanel = new JPanel();
			indPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			if (keyLabelBefore != null)
			{
				indPanel.add(keyLabelBefore);
			}
			KeyField foo = new KeyField(key, this.getKeyEnglish(key, bound_key));
			indPanel.add(foo);
			if (keyLabelAfter != null)
			{
				indPanel.add(keyLabelAfter);
			}
			addComponent(keyPanel, indPanel, c, keyLayout);
			setKeys.add(indPanel);

			// One extra note for slime chunks
			if (key == KEY_ACTIONS.TOGGLE_SLIME_CHUNKS)
			{
				current_grid_y++;
				c.gridy = current_grid_y;
				c.insets = noTopInsets;

				c.gridx = 0;
				c.anchor = GridBagConstraints.EAST;
				descLabel = new JLabel("");
				addComponent(keyPanel, descLabel, c, keyLayout);

				c.gridx = 1;
				c.gridwidth = 2;
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

		// Link
		current_grid_y++;
		c.gridx = 0; c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = noBottomInsets;
		addComponent(this.getContentPane(), new JLabel("For information on setting your own keybindings, see:"), c);
		current_grid_y++;
		c.gridx = 0; c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = noTopInsets;
		addComponent(this.getContentPane(), new JUrlLabel("http://apocalyptech.com/minecraft/xray/config.php"), c);
		c.insets = standardInsets;
		
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
		addComponent(this.getContentPane(), actionButton,c);
		
		current_grid_y++;
		c.weightx = flist; 
		c.weighty = 0f; 
		c.gridx = 0; c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(this.getContentPane(), okButton,c);
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
		
        // The "OK" button
		okButton	= new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogOK();
			}
		});

		// The "Action" button
		actionButton = new JButton("Edit");
		actionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				processAction();
			}
		});

		// Key mapping for the Jump button
		KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterStroke, "ENTER");
		KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "ENTER");
		rootPane.getActionMap().put("ENTER", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dialogOK();
			}
		});
	}

	/**
	 * Actions to perform if the "Jump" button is hit, or otherwise triggered.
	 */
	private void dialogOK()
	{
		setVisible(false);
		dispose();
		KeyHelpDialog.dialog_showing = false;
		synchronized(KeyHelpDialog.this) {
			KeyHelpDialog.this.notify();
		}
	}

	private void processAction()
	{
		switch (this.curState)
		{
			case DISPLAY:
				this.showSet();
				break;

			case SET:
				this.showDisplay();
				break;
		}
	}

	private void showSet()
	{
		for (JPanel label : this.displayKeys)
		{
			label.setVisible(false);
		}
		for (JPanel label : this.setKeys)
		{
			label.setVisible(true);
		}
		this.curState = STATE.SET;
		this.actionButton.setText("Save Key Bindings");
		this.okButton.setEnabled(false);
	}
	private void showDisplay()
	{
		for (JPanel label : this.setKeys)
		{
			label.setVisible(false);
		}
		for (JPanel label : this.displayKeys)
		{
			label.setVisible(true);
		}
		this.okButton.requestFocus();
		this.curState = STATE.DISPLAY;
		this.actionButton.setText("Edit Key Bindings");
		this.okButton.setEnabled(true);
	}
	
	/***
	 * Creates a new KeyHelpDialog
	 * @param windowName the title of the dialog
	 */
	protected KeyHelpDialog()
	{
		super(window_title);
		
		if(KeyHelpDialog.iconImage != null)
			this.setIconImage(KeyHelpDialog.iconImage);
		
		this.setSize(FRAMEWIDTH,FRAMEHEIGHT);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowClosing(WindowEvent e)
			{
				dialogOK();
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
	public static void presentDialog(HashMap<KEY_ACTIONS, Integer> key_mapping)
	{
		if (dialog_showing)
		{
			KeyHelpDialog.keyhelp_dialog.toFront();
			KeyHelpDialog.keyhelp_dialog.requestFocus();
		}
		else
		{
			KeyHelpDialog.key_mapping = key_mapping;
			KeyHelpDialog.dialog_showing = true;
			KeyHelpDialog.keyhelp_dialog = new KeyHelpDialog();
		}
	}

	public static void closeDialog()
	{
		if (KeyHelpDialog.dialog_showing && KeyHelpDialog.keyhelp_dialog != null)
		{
			KeyHelpDialog.keyhelp_dialog.dialogOK();
		}
	}
}
