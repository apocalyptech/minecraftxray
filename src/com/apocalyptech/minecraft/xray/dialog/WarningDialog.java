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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.List;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.JSeparator;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.AbstractAction;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.metal.MetalBorders.TextFieldBorder;

/**
 */
public class WarningDialog extends JFrame {
	private static final long serialVersionUID = 1185056716578355842L;
	private static final int FRAMEWIDTH = 400;
	private static final int FRAMEHEIGHT = 250;

	private JCheckBox showCheckbox;

	private JButton okButton;

	private GridBagLayout gridBagLayoutManager;
	private JPanel basicPanel;
	private JScrollPane mainLabel;

	public static boolean selectedShow;
	
	public static Image iconImage;

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
		this.setAlwaysOnTop(true);
	}
	
	/***
	 * Layouts all the controls and labels on the dialog using a gridbaglayout
	 */
	private void layoutControlsOnDialog(boolean showCheckboxBool) {
		basicPanel = new JPanel();
		
		this.getContentPane().setLayout(gridBagLayoutManager);
		basicPanel.setLayout(gridBagLayoutManager);
		GridBagConstraints c = new GridBagConstraints();
		
		JLabel headerLabel = new JLabel("Warning");
		headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
		
		float flabel = 0.1f;
		float flist = 1.9f;

		int current_grid_y = 0;
		
		c.insets = new Insets(5,5,5,5);
		c.weighty = .1f;

		// Checkbox to see if we'll show this warning in the future
		showCheckbox = new JCheckBox("Show this warning next time");
		showCheckbox.setSelected(true);

		// Now actually add the buttons
		c.insets = new Insets(5, 5, 5, 5);
		c.gridx = 0;
		c.gridwidth = 1;
		c.weightx = 1f;
		c.weighty = 0f;
		c.anchor = GridBagConstraints.CENTER;
		current_grid_y++;
		c.gridy = current_grid_y;
		addComponent(basicPanel, headerLabel, c);
		current_grid_y++;
		c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weighty = 1f;
		c.fill = GridBagConstraints.BOTH;
		addComponent(basicPanel, mainLabel, c);
		if (showCheckboxBool)
		{
			current_grid_y++;
			c.gridy = current_grid_y;
			c.weighty = 0f;
			c.weightx = 1f;
			c.anchor = GridBagConstraints.EAST;
			c.fill = GridBagConstraints.NONE;
			addComponent(basicPanel, showCheckbox, c);
		}
		
		// Add our JPanel to the window
		c.weightx = 1.0f;  
		c.weighty = .1f;
		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		addComponent(this.getContentPane(), basicPanel,c);
		
		// Now add the button
		c.insets = new Insets(5,15,5,15);
		c.gridwidth = 1;
		
		c.weightx = 1f; 
		c.weighty = 0f; 
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.SOUTHEAST;
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
	 * Builds the OK Button and attaches the actions to it
	 */
	private void buildButtons(String warningText) {
        JRootPane rootPane = this.getRootPane();
		
        // The "Jump" button
		okButton	= new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogOK();
			}
		});

		// Key mapping for the OK button
		KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterStroke, "ENTER");
		KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "ENTER");
		rootPane.getActionMap().put("ENTER", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dialogOK();
			}
		});

		// Main label
		JTextArea labelArea  = new JTextArea(warningText);
		labelArea.setLineWrap(true);
		labelArea.setWrapStyleWord(true);
		labelArea.setEditable(false);
		labelArea.setMargin(new Insets(8, 8, 8, 8));
		labelArea.setBorder(new CompoundBorder(new TextFieldBorder(), new EmptyBorder(6, 6, 6, 6)));
		mainLabel = new JScrollPane(labelArea);
		mainLabel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mainLabel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

	}

	/**
	 * Actions to perform if the "OK" button is hit, or otherwise triggered.
	 */
	private void dialogOK()
	{
		setSelectedValues();
		setVisible(false);
		dispose();
		synchronized(WarningDialog.this) {
			WarningDialog.this.notify();
		}
	}
	
	/***
	 * Sets the selected values to the static properties of this resolution dialog
	 */
	private void setSelectedValues() {
		WarningDialog.selectedShow = this.showCheckbox.isSelected();
	}
	
	/***
	 * Creates a new WarningDialog
	 * @param windowName the title of the dialog
	 */
	protected WarningDialog(String windowName, String warningText, boolean showCheckbox, int width, int height)
	{
		super(windowName);
		
		if(WarningDialog.iconImage != null)
			this.setIconImage(WarningDialog.iconImage);
		
		this.setSize(width, height);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(width, height));

		centerDialogOnScreen();
	
		buildButtons(warningText);
		layoutControlsOnDialog(showCheckbox);
		
		validate();
		
		this.setVisible(true);
	}

	/***
	 * Pops up the dialog window
	 * @param windowName the title of the dialog
	 */
	public static void presentDialog(String windowName, String warningText)
	{
		presentDialog(windowName, warningText, true, FRAMEWIDTH, FRAMEHEIGHT);
	}
	
	/***
	 * Pops up the dialog window
	 * @param windowName the title of the dialog
	 */
	public static void presentDialog(String windowName, String warningText, boolean showCheckbox, int width, int height)
	{
		WarningDialog dialog = new WarningDialog(windowName, warningText, showCheckbox, width, height);
		try
		{
			synchronized(dialog)
			{
				dialog.wait();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
