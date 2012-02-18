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

import java.io.PrintWriter;
import java.io.StringWriter;
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
public class ExceptionDialog extends JFrame {
	static final long serialVersionUID = -6144075797607274601L;
	private static final int FRAMEWIDTH = 800;
	private static final int FRAMEHEIGHT = 400;

	private JButton okButton;

	private GridBagLayout gridBagLayoutManager;
	private JPanel basicPanel;
	private JTextArea mainLabel;
	private JScrollPane mainPane;
	
	public static Image iconImage;

	private static String extraStatus1;
	private static String extraStatus2;

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
	private void layoutControlsOnDialog(String windowName) {
		basicPanel = new JPanel();
		
		this.getContentPane().setLayout(gridBagLayoutManager);
		basicPanel.setLayout(gridBagLayoutManager);
		GridBagConstraints c = new GridBagConstraints();
		
		JLabel headerLabel = new JLabel(windowName);
		headerLabel.setFont(new Font("Arial", Font.BOLD, 18));

		JLabel reportLabel = new JLabel("If you report this error, please be sure to include the full text of this exception.");
		reportLabel.setFont(new Font("Arial", Font.PLAIN, 12));

		JLabel extraStatusLabel1 = null;
		if (extraStatus1 != null)
		{
			extraStatusLabel1 = new JLabel(extraStatus1);
			extraStatusLabel1.setFont(new Font("Arial", Font.PLAIN, 12));
		}

		JLabel extraStatusLabel2 = null;
		if (extraStatus2 != null)
		{
			extraStatusLabel2 = new JLabel(extraStatus2);
			extraStatusLabel2.setFont(new Font("Arial", Font.PLAIN, 12));
		}

		int current_grid_y = 0;
		
		c.insets = new Insets(5,5,5,5);
		c.weighty = .1f;

		// Now actually add the buttons
		c.insets = new Insets(5, 5, 5, 5);
		c.gridx = 0;
		c.gridwidth = 1;
		c.weightx = 1f;
		c.weighty = 0f;
		c.anchor = GridBagConstraints.CENTER;

		// Header
		current_grid_y++;
		c.gridy = current_grid_y;
		addComponent(basicPanel, headerLabel, c);

		// Extra Status Label 1
		if (extraStatusLabel1 != null)
		{
			current_grid_y++;
			c.gridy = current_grid_y;
			addComponent(basicPanel, extraStatusLabel1, c);
		}

		// Extra Status Label 2
		if (extraStatusLabel2 != null)
		{
			current_grid_y++;
			c.gridy = current_grid_y;
			addComponent(basicPanel, extraStatusLabel2, c);
		}

		// the main exception pane
		current_grid_y++;
		c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weighty = 1f;
		c.fill = GridBagConstraints.BOTH;
		addComponent(basicPanel, mainPane, c);

		// Also add a bit about reporting errors
		current_grid_y++;
		c.gridy = current_grid_y;
		c.weightx = 1f;
		c.weighty = 0f;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		addComponent(basicPanel, reportLabel, c);
		
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
		rootPane.getActionMap().put("ENTER", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dialogOK();
			}
		});

		// Main label
		mainLabel  = new JTextArea(warningText);
		mainLabel.setLineWrap(false);
		mainLabel.setEditable(false);
		mainLabel.setMargin(new Insets(8, 8, 8, 8));
		mainLabel.setBorder(new CompoundBorder(new TextFieldBorder(), new EmptyBorder(6, 6, 6, 6)));

		// Scrollpane
		mainPane = new JScrollPane(mainLabel);

	}

	/**
	 * Actions to perform if the "OK" button is hit, or otherwise triggered.
	 */
	private void dialogOK()
	{
		setVisible(false);
		dispose();
		synchronized(ExceptionDialog.this) {
			ExceptionDialog.this.notify();
		}
	}
	
	/**
	 * We have a couple of extra strings to keep track of status information.
	 * this method clears them out.
	 */
	public static void clearExtraStatus()
	{
		clearExtraStatus1();
		clearExtraStatus2();
	}

	/**
	 * Sets our first extra-status string.
	 */
	public static void setExtraStatus1(String newStatus)
	{
		extraStatus1 = newStatus;
	}

	/**
	 * Gets our first extra-status string.
	 */
	public static String getExtraStatus1()
	{
		return extraStatus1;
	}

	/**
	 * Clears out our first extra-status string.
	 */
	public static void clearExtraStatus1()
	{
		extraStatus1 = null;
	}

	/**
	 * Sets our second extra-status string.
	 */
	public static void setExtraStatus2(String newStatus)
	{
		extraStatus2 = newStatus;
	}

	/**
	 * Gets our second extra-status string.
	 */
	public static String getExtraStatus2()
	{
		return extraStatus2;
	}

	/**
	 * Clears out our second extra-status string.
	 */
	public static void clearExtraStatus2()
	{
		extraStatus2 = null;
	}

	/***
	 * Creates a new ExceptionDialog
	 * @param windowName the title of the dialog
	 */
	protected ExceptionDialog(String windowName, String warningText)
	{
		super(windowName);
		
		if(ExceptionDialog.iconImage != null)
			this.setIconImage(ExceptionDialog.iconImage);
		
		this.setSize(FRAMEWIDTH,FRAMEHEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(FRAMEWIDTH, FRAMEHEIGHT));

		centerDialogOnScreen();
	
		buildButtons(warningText);
		layoutControlsOnDialog(windowName);
		
		validate();
		
		this.setVisible(true);
	}
	
	/***
	 * Pops up the dialog window
	 * @param windowName the title of the dialog
	 */
	public static void presentDialog(String windowName, String warningText)
	{
		ExceptionDialog dialog = new ExceptionDialog(windowName, warningText);
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
	
	/***
	 * Pops up the dialog window, given an exception
	 * @param windowName the title of the dialog
	 */
	public static void presentDialog(String windowName, Exception e)
	{
		// Put our stack trace into a string...
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		// ... and report.
		if (getExtraStatus1() != null)
		{
			XRay.logger.error(getExtraStatus1());
		}
		if (getExtraStatus2() != null)
		{
			XRay.logger.error(getExtraStatus2());
		}
		XRay.logger.error(sw.toString());
		presentDialog(windowName, sw.toString());
	}
}
