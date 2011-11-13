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
import java.awt.AWTKeyStroke;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

/**
 */
public class KeyHelpDialog
	extends JFrame 
	implements KeyListener
{
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

	public static HashMap<KEY_ACTION, Integer> key_mapping;

	private ArrayList<KeyPanel> keyPanels;
	private KeyPanel curKeyPanel;

	private enum STATE
	{
		DISPLAY,
		EDITING,
		KEYSET
	}
	private STATE curState = STATE.DISPLAY;

	/* Key mapping code taken from LWJGL, specifically
       org.lwjgl.opengl.KeyboardEventQueue
    */
    private static final int[] KEY_MAP = new int[0xffff];
    static {
        KEY_MAP[KeyEvent.VK_0] = Keyboard.KEY_0;
        KEY_MAP[KeyEvent.VK_1] = Keyboard.KEY_1;
        KEY_MAP[KeyEvent.VK_2] = Keyboard.KEY_2;
        KEY_MAP[KeyEvent.VK_3] = Keyboard.KEY_3;
        KEY_MAP[KeyEvent.VK_4] = Keyboard.KEY_4;
        KEY_MAP[KeyEvent.VK_5] = Keyboard.KEY_5;
        KEY_MAP[KeyEvent.VK_6] = Keyboard.KEY_6;
        KEY_MAP[KeyEvent.VK_7] = Keyboard.KEY_7;
        KEY_MAP[KeyEvent.VK_8] = Keyboard.KEY_8;
        KEY_MAP[KeyEvent.VK_9] = Keyboard.KEY_9;
        KEY_MAP[KeyEvent.VK_A] = Keyboard.KEY_A;
        KEY_MAP[KeyEvent.VK_ADD] = Keyboard.KEY_ADD;
        KEY_MAP[KeyEvent.VK_ALT_GRAPH] = Keyboard.KEY_RMENU;
        KEY_MAP[KeyEvent.VK_AT] = Keyboard.KEY_AT;
        KEY_MAP[KeyEvent.VK_B] = Keyboard.KEY_B;
        KEY_MAP[KeyEvent.VK_BACK_SLASH] = Keyboard.KEY_BACKSLASH;
        KEY_MAP[KeyEvent.VK_BACK_SPACE] = Keyboard.KEY_BACK;
        KEY_MAP[KeyEvent.VK_C] = Keyboard.KEY_C;
        KEY_MAP[KeyEvent.VK_CAPS_LOCK] = Keyboard.KEY_CAPITAL;
        KEY_MAP[KeyEvent.VK_CIRCUMFLEX] = Keyboard.KEY_CIRCUMFLEX;
        KEY_MAP[KeyEvent.VK_CLOSE_BRACKET] = Keyboard.KEY_RBRACKET;
        KEY_MAP[KeyEvent.VK_COLON] = Keyboard.KEY_COLON;
        KEY_MAP[KeyEvent.VK_COMMA] = Keyboard.KEY_COMMA;
        KEY_MAP[KeyEvent.VK_CONVERT] = Keyboard.KEY_CONVERT;
        KEY_MAP[KeyEvent.VK_D] = Keyboard.KEY_D;
        KEY_MAP[KeyEvent.VK_DECIMAL] = Keyboard.KEY_DECIMAL;
        KEY_MAP[KeyEvent.VK_DELETE] = Keyboard.KEY_DELETE;
        KEY_MAP[KeyEvent.VK_DIVIDE] = Keyboard.KEY_DIVIDE;
        KEY_MAP[KeyEvent.VK_DOWN] = Keyboard.KEY_DOWN;
        KEY_MAP[KeyEvent.VK_E] = Keyboard.KEY_E;
        KEY_MAP[KeyEvent.VK_END] = Keyboard.KEY_END;
        KEY_MAP[KeyEvent.VK_ENTER] = Keyboard.KEY_RETURN;
        KEY_MAP[KeyEvent.VK_EQUALS] = Keyboard.KEY_EQUALS;
        KEY_MAP[KeyEvent.VK_ESCAPE] = Keyboard.KEY_ESCAPE;
        KEY_MAP[KeyEvent.VK_F] = Keyboard.KEY_F;
        KEY_MAP[KeyEvent.VK_F1] = Keyboard.KEY_F1;
        KEY_MAP[KeyEvent.VK_F10] = Keyboard.KEY_F10;
        KEY_MAP[KeyEvent.VK_F11] = Keyboard.KEY_F11;
        KEY_MAP[KeyEvent.VK_F12] = Keyboard.KEY_F12;
        KEY_MAP[KeyEvent.VK_F13] = Keyboard.KEY_F13;
        KEY_MAP[KeyEvent.VK_F14] = Keyboard.KEY_F14;
        KEY_MAP[KeyEvent.VK_F15] = Keyboard.KEY_F15;
        KEY_MAP[KeyEvent.VK_F2] = Keyboard.KEY_F2;
        KEY_MAP[KeyEvent.VK_F3] = Keyboard.KEY_F3;
        KEY_MAP[KeyEvent.VK_F4] = Keyboard.KEY_F4;
        KEY_MAP[KeyEvent.VK_F5] = Keyboard.KEY_F5;
        KEY_MAP[KeyEvent.VK_F6] = Keyboard.KEY_F6;
        KEY_MAP[KeyEvent.VK_F7] = Keyboard.KEY_F7;
        KEY_MAP[KeyEvent.VK_F8] = Keyboard.KEY_F8;
        KEY_MAP[KeyEvent.VK_F9] = Keyboard.KEY_F9;
        KEY_MAP[KeyEvent.VK_G] = Keyboard.KEY_G;
        KEY_MAP[KeyEvent.VK_H] = Keyboard.KEY_H;
        KEY_MAP[KeyEvent.VK_HOME] = Keyboard.KEY_HOME;
        KEY_MAP[KeyEvent.VK_I] = Keyboard.KEY_I;
        KEY_MAP[KeyEvent.VK_INSERT] = Keyboard.KEY_INSERT;
        KEY_MAP[KeyEvent.VK_J] = Keyboard.KEY_J;
        KEY_MAP[KeyEvent.VK_K] = Keyboard.KEY_K;
        KEY_MAP[KeyEvent.VK_KANA] = Keyboard.KEY_KANA;
        KEY_MAP[KeyEvent.VK_KANJI] = Keyboard.KEY_KANJI;
        KEY_MAP[KeyEvent.VK_L] = Keyboard.KEY_L;
        KEY_MAP[KeyEvent.VK_LEFT] = Keyboard.KEY_LEFT;
        KEY_MAP[KeyEvent.VK_M] = Keyboard.KEY_M;
        KEY_MAP[KeyEvent.VK_MINUS] = Keyboard.KEY_MINUS;
        KEY_MAP[KeyEvent.VK_MULTIPLY] = Keyboard.KEY_MULTIPLY;
        KEY_MAP[KeyEvent.VK_N] = Keyboard.KEY_N;
        KEY_MAP[KeyEvent.VK_NUM_LOCK] = Keyboard.KEY_NUMLOCK;
        KEY_MAP[KeyEvent.VK_NUMPAD0] = Keyboard.KEY_NUMPAD0;
        KEY_MAP[KeyEvent.VK_NUMPAD1] = Keyboard.KEY_NUMPAD1;
        KEY_MAP[KeyEvent.VK_NUMPAD2] = Keyboard.KEY_NUMPAD2;
        KEY_MAP[KeyEvent.VK_NUMPAD3] = Keyboard.KEY_NUMPAD3;
        KEY_MAP[KeyEvent.VK_NUMPAD4] = Keyboard.KEY_NUMPAD4;
        KEY_MAP[KeyEvent.VK_NUMPAD5] = Keyboard.KEY_NUMPAD5;
        KEY_MAP[KeyEvent.VK_NUMPAD6] = Keyboard.KEY_NUMPAD6;
        KEY_MAP[KeyEvent.VK_NUMPAD7] = Keyboard.KEY_NUMPAD7;
        KEY_MAP[KeyEvent.VK_NUMPAD8] = Keyboard.KEY_NUMPAD8;
        KEY_MAP[KeyEvent.VK_NUMPAD9] = Keyboard.KEY_NUMPAD9;
        KEY_MAP[KeyEvent.VK_O] = Keyboard.KEY_O;
        KEY_MAP[KeyEvent.VK_OPEN_BRACKET] = Keyboard.KEY_LBRACKET;
        KEY_MAP[KeyEvent.VK_P] = Keyboard.KEY_P;
        KEY_MAP[KeyEvent.VK_PAGE_DOWN] = Keyboard.KEY_NEXT;
        KEY_MAP[KeyEvent.VK_PAGE_UP] = Keyboard.KEY_PRIOR;
        KEY_MAP[KeyEvent.VK_PAUSE] = Keyboard.KEY_PAUSE;
        KEY_MAP[KeyEvent.VK_PERIOD] = Keyboard.KEY_PERIOD;
        KEY_MAP[KeyEvent.VK_Q] = Keyboard.KEY_Q;
        KEY_MAP[KeyEvent.VK_R] = Keyboard.KEY_R;
        KEY_MAP[KeyEvent.VK_RIGHT] = Keyboard.KEY_RIGHT;
        KEY_MAP[KeyEvent.VK_S] = Keyboard.KEY_S;
        KEY_MAP[KeyEvent.VK_SCROLL_LOCK] = Keyboard.KEY_SCROLL;
        KEY_MAP[KeyEvent.VK_SEMICOLON] = Keyboard.KEY_SEMICOLON;
        KEY_MAP[KeyEvent.VK_SEPARATOR] = Keyboard.KEY_DECIMAL;
        KEY_MAP[KeyEvent.VK_SLASH] = Keyboard.KEY_SLASH;
        KEY_MAP[KeyEvent.VK_SPACE] = Keyboard.KEY_SPACE;
        KEY_MAP[KeyEvent.VK_STOP] = Keyboard.KEY_STOP;
        KEY_MAP[KeyEvent.VK_SUBTRACT] = Keyboard.KEY_SUBTRACT;
        KEY_MAP[KeyEvent.VK_T] = Keyboard.KEY_T;
        KEY_MAP[KeyEvent.VK_TAB] = Keyboard.KEY_TAB;
        KEY_MAP[KeyEvent.VK_U] = Keyboard.KEY_U;
        KEY_MAP[KeyEvent.VK_UP] = Keyboard.KEY_UP;
        KEY_MAP[KeyEvent.VK_V] = Keyboard.KEY_V;
        KEY_MAP[KeyEvent.VK_W] = Keyboard.KEY_W;
        KEY_MAP[KeyEvent.VK_X] = Keyboard.KEY_X;
        KEY_MAP[KeyEvent.VK_Y] = Keyboard.KEY_Y;
        KEY_MAP[KeyEvent.VK_Z] = Keyboard.KEY_Z;
	}

	public static int awtKeyToLWJGL(KeyEvent e)
	{
		return awtKeyToLWJGL(e.getKeyCode(), e.getKeyLocation());
	}

	public static int awtKeyToLWJGL(int key_code, int position)
	{
		/* Code taken from LWJGL, specifically org.lwjgl.opengl.KeyboardEventQueue */
		switch (key_code)
		{
			case KeyEvent.VK_ALT: // fall through
				if (position == KeyEvent.KEY_LOCATION_RIGHT)
					return Keyboard.KEY_RMENU;
				else
					return Keyboard.KEY_LMENU;
			case KeyEvent.VK_META:
				if (position == KeyEvent.KEY_LOCATION_RIGHT)
					return Keyboard.KEY_RMETA;
				else
					return Keyboard.KEY_LMETA;
			case KeyEvent.VK_SHIFT:
				if (position == KeyEvent.KEY_LOCATION_RIGHT)
					return Keyboard.KEY_RSHIFT;
				else
					return Keyboard.KEY_LSHIFT;
			case KeyEvent.VK_CONTROL:
				if (position == KeyEvent.KEY_LOCATION_RIGHT)
					return Keyboard.KEY_RCONTROL;
				else
					return Keyboard.KEY_LCONTROL;
			default:
				return KEY_MAP[key_code];
		}
	}

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

		keyPanels = new ArrayList<KeyPanel>();

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
			KeyPanel indPanel = new KeyPanel(this, keyFont, key, bound_key);
			addComponent(keyPanel, indPanel, c, keyLayout);
			keyPanels.add(indPanel);

			this.showDisplay();

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
		/*
		KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterStroke, "ENTER");
		KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "ENTER");
		rootPane.getActionMap().put("ENTER", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dialogOK();
			}
		});
		*/
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

			case EDITING:
				this.showDisplay();
				break;

			case KEYSET:
				this.stopKeySet();
				break;
		}
	}

	private void showSet()
	{
		for (KeyPanel panel : this.keyPanels)
		{
			panel.startEdit();
		}
		this.curState = STATE.EDITING;
		this.actionButton.setText("Save Key Bindings");
		this.okButton.setEnabled(false);
	}
	private void showDisplay()
	{
		for (KeyPanel panel : this.keyPanels)
		{
			panel.finishEdit();
		}
		this.okButton.requestFocus();
		this.curState = STATE.DISPLAY;
		this.actionButton.setText("Edit Key Bindings");
		this.okButton.setEnabled(true);
	}
	private void stopKeySet()
	{
		this.showSet();
		if (this.curKeyPanel != null)
		{
			this.curKeyPanel.clickFinish();
			this.curKeyPanel = null;
		}
		this.setFocusTraversalKeysEnabled(true);
		this.removeKeyListener(this);
	}

	public void notifyKeyPanelClicked(KeyPanel keypanel)
	{
		if (this.curKeyPanel == null)
		{
			this.addKeyListener(this);
		}
		else
		{
			this.curKeyPanel.clickFinish();
		}
		this.curKeyPanel = keypanel;
		this.curState = STATE.KEYSET;
		this.actionButton.setText("Cancel Edit");
		this.okButton.setEnabled(false);
		this.requestFocus();
		this.setFocusTraversalKeysEnabled(false);
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
	public static void presentDialog(HashMap<KEY_ACTION, Integer> key_mapping)
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

	/***
	 *** KeyListener Methods
	 ***/

	public void keyPressed(KeyEvent e)
	{
		XRay.logger.debug("Key pressed: " + e.getKeyText(e.getKeyCode()) + ", char: " + e.getKeyChar() + " (int: " + (int)e.getKeyChar() + ")");
		// We override the numpad because the Typed event they get is
		// indistinguishable from the non-numpad equivalents
		if (this.curKeyPanel != null)
		{
			int key_code = e.getKeyCode();
			switch (key_code)
			{
				case KeyEvent.VK_NUMPAD0:
					this.curKeyPanel.setBoundKey(Keyboard.KEY_NUMPAD0);
					this.stopKeySet();
					break;
				case KeyEvent.VK_NUMPAD1:
					this.curKeyPanel.setBoundKey(Keyboard.KEY_NUMPAD1);
					this.stopKeySet();
					break;
				case KeyEvent.VK_NUMPAD2:
					this.curKeyPanel.setBoundKey(Keyboard.KEY_NUMPAD2);
					this.stopKeySet();
					break;
				case KeyEvent.VK_NUMPAD3:
					this.curKeyPanel.setBoundKey(Keyboard.KEY_NUMPAD3);
					this.stopKeySet();
					break;
				case KeyEvent.VK_NUMPAD4:
					this.curKeyPanel.setBoundKey(Keyboard.KEY_NUMPAD4);
					this.stopKeySet();
					break;
				case KeyEvent.VK_NUMPAD5:
					this.curKeyPanel.setBoundKey(Keyboard.KEY_NUMPAD5);
					this.stopKeySet();
					break;
				case KeyEvent.VK_NUMPAD6:
					this.curKeyPanel.setBoundKey(Keyboard.KEY_NUMPAD6);
					this.stopKeySet();
					break;
				case KeyEvent.VK_NUMPAD7:
					this.curKeyPanel.setBoundKey(Keyboard.KEY_NUMPAD7);
					this.stopKeySet();
					break;
				case KeyEvent.VK_NUMPAD8:
					this.curKeyPanel.setBoundKey(Keyboard.KEY_NUMPAD8);
					this.stopKeySet();
					break;
				case KeyEvent.VK_NUMPAD9:
					this.curKeyPanel.setBoundKey(Keyboard.KEY_NUMPAD9);
					this.stopKeySet();
					break;
			}
		}
	}
	public void keyReleased(KeyEvent e)
	{
		XRay.logger.debug("Key released: " + e.getKeyText(e.getKeyCode()) + ", char: " + e.getKeyChar() + " (int: " + (int)e.getKeyChar() + ")");
		if (this.curState == STATE.KEYSET)
		{
			if (this.curKeyPanel != null)
			{
				int key = awtKeyToLWJGL(e);
				this.curKeyPanel.setBoundKey(key);
			}
			this.stopKeySet();
		}
	}
	public void keyTyped(KeyEvent e)
	{
		XRay.logger.debug("Key typed: " + e.getKeyText(e.getKeyCode()) + ", char: " + e.getKeyChar() + " (int: " + (int)e.getKeyChar() + ")");
		if (this.curState == STATE.KEYSET)
		{
			/*
			if (this.curKeyPanel != null)
			{
				int key_code = AWTKeyStroke.getAWTKeyStroke(e.getKeyChar()).getKeyCode();
				XRay.logger.debug("key_code: " + key_code);
				int key = awtKeyToLWJGL(AWTKeyStroke.getAWTKeyStroke(e.getKeyChar()).getKeyCode(), KeyEvent.KEY_LOCATION_STANDARD);
				this.curKeyPanel.setBoundKey(key);
			}
			*/
			this.stopKeySet();
		}
	}
}
