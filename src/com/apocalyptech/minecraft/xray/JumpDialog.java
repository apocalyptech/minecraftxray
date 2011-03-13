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
package com.apocalyptech.minecraft.xray;

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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.JSeparator;
import javax.swing.JComponent;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.AbstractAction;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 */
public class JumpDialog extends JFrame {
	private static final long serialVersionUID = -670931768263974900L;
	private static final int FRAMEWIDTH = 300;
	private static final int FRAMEHEIGHT = 200;

	private JSpinner xSpinner;
	private JSpinner zSpinner;
	private SpinnerNumberModel xSpinnerModel;
	private SpinnerNumberModel zSpinnerModel;
	private ButtonGroup positionSelectGroup;
	private JRadioButton positionButton;
	private JRadioButton chunkButton;

	private JButton runButton;
	private JButton exitButton;

	private GridBagLayout gridBagLayoutManager;
	private JPanel basicPanel;

	private static XRay xray_obj;
	private static boolean dialog_showing = false;
	private static JumpDialog jump_dialog;

	public static boolean selectedChunk;
	public static int selectedX;
	public static int selectedZ;
	
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
	private void layoutControlsOnDialog() {
		basicPanel = new JPanel();
		
		this.getContentPane().setLayout(gridBagLayoutManager);
		basicPanel.setLayout(gridBagLayoutManager);
		GridBagConstraints c = new GridBagConstraints();
		
		JLabel xLabel = new JLabel("X Position: ");
		JLabel zLabel  = new JLabel("Z Position: ");
		
		float flabel = 0.1f;
		float flist = 1.9f;

		int current_grid_y = 0;
		
		c.insets = new Insets(5,5,5,5);
		c.weighty = .1f;

		// Radio buttons to determine whether we're zooming to a position or a chunk
		positionSelectGroup = new ButtonGroup();
		positionButton = new JRadioButton("Jump to position");
		positionButton.setSelected(true);
		chunkButton = new JRadioButton("Jump to chunk");
		positionSelectGroup.add(positionButton);
		positionSelectGroup.add(chunkButton);

		// Now actually add the buttons
		c.insets = new Insets(5, 15, 5, 5);
		c.gridx = 0;
		c.gridwidth = 2;
		current_grid_y++;
		c.gridy = current_grid_y;
		addComponent(basicPanel, positionButton, c);
		current_grid_y++;
		c.gridy = current_grid_y;
		addComponent(basicPanel, chunkButton, c);
		
		// Separator
		current_grid_y++;
		c.insets = new Insets(5,5,5,5);
		c.weightx = 1.0f;
		c.gridx = 0; c.gridy = current_grid_y;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(basicPanel, Box.createVerticalStrut(5), c);
		addComponent(basicPanel, new JSeparator(SwingConstants.HORIZONTAL), c);
		addComponent(basicPanel, Box.createVerticalStrut(5), c);

		// Add the X label
		current_grid_y++;
		c.gridwidth = 1;
		c.weightx = flabel; 
		c.gridx = 0; c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		addComponent(basicPanel, xLabel,c);

		// Add the X spinner
		// TODO: Minecaft coordinates are technically Longs, so we should be using that.  However,
		// X-Ray only supports up to Integer values, and Minecraft itself bugs way out long before
		// then anyway.  So we should be okay regardless.
		xSpinnerModel = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
		xSpinner = new JSpinner(xSpinnerModel);
		c.weightx = flist; 
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1; c.gridy = current_grid_y;
		addComponent(basicPanel, xSpinner, c);

		// Add the Z label
		current_grid_y++;
		c.weightx = flabel; 
		c.gridx = 0; c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		addComponent(basicPanel, zLabel,c);

		// Add the Z spinner
		zSpinnerModel = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
		zSpinner = new JSpinner(zSpinnerModel);
		c.weightx = flist; 
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1; c.gridy = current_grid_y;
		addComponent(basicPanel, zSpinner, c);
		
		// Add our JPanel to the window
		c.weightx = 1.0f;  
		c.weighty = .1f;
		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		addComponent(this.getContentPane(), basicPanel,c);
		
		// Now add the buttons
		c.insets = new Insets(5,15,5,15);
		c.gridwidth = 1;
		
		c.weightx = flabel; 
		c.weighty = 1.0f; 
		c.gridx = 0; c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(this.getContentPane(), exitButton,c);
		
		c.weightx = flist; 
		c.weighty = 1.0f; 
		c.gridx = 1; c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(this.getContentPane(), runButton,c);
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
	 * Builds the Go and Exit Buttons and attaches the actions to them
	 */
	private void buildButtons() {
        JRootPane rootPane = this.getRootPane();
		
        // The "Jump" button
		runButton	= new JButton("Jump");
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogOK();
			}
		});

		// Key mapping for the Jump button
		KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterStroke, "ENTER");
		rootPane.getActionMap().put("ENTER", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dialogOK();
			}
		});
		
        // The Cancel button
		exitButton	= new JButton("Cancel");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogCancel();
			}
		});

		// Key mapping for the Cancel button
		KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dialogCancel();
			}
		});
	}

	/**
	 * Actions to perform if the "Jump" button is hit, or otherwise triggered.
	 */
	private void dialogOK()
	{
		setSelectedValues();
		setVisible(false);
		dispose();
		JumpDialog.xray_obj.jump_dialog_trigger = true;
		JumpDialog.dialog_showing = false;
		synchronized(JumpDialog.this) {
			JumpDialog.this.notify();
		}
	}


	/**
	 * Actions to perform if the "Cancel" button is hit, or otherwise triggered.
	 */
	private void dialogCancel()
	{
		setVisible(false);
		dispose();
		JumpDialog.dialog_showing = false;
		synchronized(JumpDialog.this) {
			JumpDialog.this.notify();
		}
	}
	
	/***
	 * Sets the selected values to the static properties of this resolution dialog
	 */
	private void setSelectedValues() {
		JumpDialog.selectedX = this.xSpinnerModel.getNumber().intValue();
		JumpDialog.selectedZ = this.zSpinnerModel.getNumber().intValue();
		JumpDialog.selectedChunk = this.chunkButton.isSelected();
	}
	
	/***
	 * Creates a new JumpDialog
	 * @param windowName the title of the dialog
	 */
	protected JumpDialog(String windowName)
	{
		super(windowName);
		
		if(JumpDialog.iconImage != null)
			this.setIconImage(JumpDialog.iconImage);
		
		this.setSize(FRAMEWIDTH,FRAMEHEIGHT);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
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
	public static void presentDialog(String windowName, XRay xray_obj)
	{
		if (!dialog_showing)
		{
			JumpDialog.xray_obj = xray_obj;
			JumpDialog.dialog_showing = true;
			JumpDialog.jump_dialog = new JumpDialog(windowName);
		}
	}

	public static void closeDialog()
	{
		if (JumpDialog.dialog_showing && JumpDialog.jump_dialog != null)
		{
			JumpDialog.jump_dialog.dialogCancel();
		}
	}
}
