package com.plusminus.craft;

/*
 * Copyright (c) 2005-2006, The ParticleReality Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of The ParticleReality Project nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

/***
 * Static popup screen which enables the user to set values such as
 * resolution, refreshrate, antialas modes and fullscreen</br>
 * </br>
 * Will query the system for available resolutions, bit depths and appropriate
 * refresh rates.</br>
 * </br>
 * Allows the programmer to pass a set of preferred values for each property,
 * the ResolutionDialog will attempt to find the nearest match</br>
 * </br>
 * Allows the programmer to attach a second panel where optional
 * advanced properties can be set</br>
 * </br>
 * The static method "presentDialog" will popup the screen and will not
 * return untill the user has either selected "go" or "exit". The return
 * value of the method determines which button the user has clicked</br>
 * </br>
 * </br>
 * </br>
 * <i>(example:)</i><br/>
 * <code>
 * if(ResolutionDialog.presentDialog("Choose Video Options") == ResolutionDialog.DIALOG_BUTTON_EXIT) {
 *			System.exit(0);
 * }
 * </code>
 * <br/>
 * The selected properties can be read this way<br/>
 * <code>
 * ResolutionDialog.selectedDisplayMode
 * ResolutionDialog.selectedAntiAliasMode
 * ResolutionDialog.selectedFullScreenValue
 * </code>
 * @author Vincent Vollers
 * @version 1.0
 */
public class ResolutionDialog extends JFrame {
	private static final int FRAMEWIDTH = 300;
	private static final int FRAMEHEIGHT = 230;
	private static final String PANEL_NAME_BASIC ="Basic";
	private static final String PANEL_NAME_ADVANCED = "Advanced";
	private static final int[][] defaultPreferredResolutions = 
		new int[][] {{1920,1080},{1600,900},{1280,720},{1024, 768}, {800, 600}, {1280,1024}};
	private static final int[] defaultPreferredBitDepths =
		new int[] {32, 16, 8};
	private static final int[] defaultPreferredRefreshRates =
		new int[] {85, 80, 75, 70, 65, 60};
	private static final int[] defaultPreferredAntialiasModes =
		new int[] {0,2,4,8};
	private static final boolean defaultPreferredFullScreenValue = false;
		
	public static final int DIALOG_BUTTON_EXIT = 0;
	public static final int DIALOG_BUTTON_GO = 1;

	private JComboBox resolutionsList;
	private JComboBox bitDepthList;
	private JComboBox refreshRateList;
	private JComboBox antiAliasList;
	private JButton runButton;
	private JButton exitButton;
	private GridBagLayout gridBagLayoutManager;
	private JTabbedPane tabbedPane;
	private JPanel basicPanel;
	private Container advancedPanel;
	private JCheckBox fullScreenCheckBox;
	
	private DefaultComboBoxModel resolutionsModel;
	private DefaultComboBoxModel bitDepthModel;
	private DefaultComboBoxModel refreshRateModel;
	private DefaultComboBoxModel antiAliasModel;
	
	private Map<IntegerPair, List<DisplayMode>> resolutionsMap;
	
	private int[][] preferredResolutions;
	private int[] preferredRefreshRates;
	private int[] preferredBitDepths;
	private int[] preferredAntiAliasModes;
	private boolean preferredFullScreenValue;
	
	private int exitCode = -1;
	
	public static DisplayMode selectedDisplayMode;
	public static int selectedAntiAliasMode;
	public static boolean selectedFullScreenValue;
	
	public static Image iconImage;
	
	/***
	 * Class holding a pair of two integers where the order is determined
	 * first by the first integer and when these are equal, by the second
	 * integer. This is used for holding resolution information
	 * @author Vincent Vollers
	 */
	private class IntegerPair implements Comparable {
		private int valueOne;
		private int valueTwo;
		
		public IntegerPair(int valueOne, int valueTwo) {
			this.valueOne = valueOne;
			this.valueTwo = valueTwo;
		}
		
		public int getValueOne() {
			return this.valueOne;
		}
		
		public int getValueTwo() {
			return this.valueTwo;
		}

		public int compareTo(Object o) {
			if(!(o instanceof IntegerPair)) {
				return -1;
			}
			IntegerPair i = (IntegerPair) o;
			
			if(i.getValueOne()>valueOne)
				return 1;
			
			if(i.getValueOne()<valueOne)
				return -1;
			
			if(i.getValueTwo()>valueTwo)
				return 1;
			
			if(i.getValueTwo()<valueTwo)
				return -1;

			return 0;
		}
	}
	
	/***
	 * Renders IntegerPairs ("[a] x [b]", so "1024 x 768" for example) 
	 * @author Vincent Vollers
	 */
	private class DisplayModesRenderer extends BasicComboBoxRenderer {
		public DisplayModesRenderer() {
			super();
		}
		
		public Component getListCellRendererComponent(
                 JList list, 
                 Object value,
                 int index, 
                 boolean isSelected, 
                 boolean cellHasFocus)
		 {
			IntegerPair pair = (IntegerPair) value;
			String newValue = "" + pair.getValueOne() + " x " + pair.getValueTwo();
			
			return super.getListCellRendererComponent(list, newValue, index, isSelected, cellHasFocus); 
		 }
	}
	
	/***
	 * Renders Integers and adds an 'x' to the end, so '2x' for example.
	 * @author Vincent Vollers
	 */
	private class AntiAliasModesRenderer extends BasicComboBoxRenderer {
		public AntiAliasModesRenderer() {
			super();
		}
		
		public Component getListCellRendererComponent(
                 JList list, 
                 Object value,
                 int index, 
                 boolean isSelected, 
                 boolean cellHasFocus)
		 {
			Integer val = (Integer) value;
			String newValue = "" + val + "x";
			if(val == 0) {
				newValue = "none";
			}
			
			return super.getListCellRendererComponent(list, newValue, index, isSelected, cellHasFocus); 
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
		this.tabbedPane = new JTabbedPane();
		basicPanel = new JPanel();
		this.tabbedPane.add(PANEL_NAME_BASIC, basicPanel);
		
		if(advancedPanel != null) {
			this.tabbedPane.add(PANEL_NAME_ADVANCED, advancedPanel);
			this.tabbedPane.setEnabledAt(1, true);
		} else {
			this.tabbedPane.add(PANEL_NAME_ADVANCED, new JPanel());
			this.tabbedPane.setEnabledAt(1, false);
		}
		
		this.getContentPane().setLayout(gridBagLayoutManager);
		basicPanel.setLayout(gridBagLayoutManager);
		GridBagConstraints c = new GridBagConstraints();
		
		JLabel resolutionsLabel = new JLabel("Resolution: ");
		JLabel bitDepthLabel 	= new JLabel("Bit Depth: ");
		JLabel refreshRateLabel = new JLabel("Refresh Rate: ");
		JLabel antiAliasLabel 	= new JLabel("Anti Aliasing: ");
		JLabel fullScreenLabel  = new JLabel("Full Screen: ");
		
		float flabel = 0.1f;
		float flist = 1.9f;
		
		c.insets = new Insets(5,5,5,5);
		
		c.weightx = flabel; 
		c.weighty = 1.0f;
		c.gridx = 0; c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		addComponent(basicPanel, resolutionsLabel,c);
		
		c.weightx = flist; 
		c.weighty = 1.0f; 
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1; c.gridy = 0;
		addComponent(basicPanel, resolutionsList,c);
		
		c.weightx = flabel; 
		c.weighty = 1.0f; 
		c.gridx = 0; c.gridy = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		addComponent(basicPanel, bitDepthLabel,c);
		
		c.weightx = flist;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 1.0f; 
		c.gridx = 1; c.gridy = 1;
		addComponent(basicPanel, bitDepthList,c);
		
		c.weightx = flabel;
		c.weighty = 1.0f; 
		c.gridx = 0; c.gridy = 2;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		addComponent(basicPanel, refreshRateLabel,c);
		
		c.weightx = flist;  
		c.weighty = 1.0f; 
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1; c.gridy = 2;
		addComponent(basicPanel, refreshRateList,c);
		
		c.weightx = flabel; 
		c.weighty = 1.0f; 
		c.gridx = 0; c.gridy = 3;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		addComponent(basicPanel, antiAliasLabel,c);
		
		c.weightx = flist;  
		c.weighty = 1.0f; 
		c.gridx = 1; c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(basicPanel, antiAliasList,c);
		
		fullScreenCheckBox = new JCheckBox();
		
		c.weightx = flabel; 
		c.weighty = 1.0f; 
		c.gridx = 0; c.gridy = 4;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		addComponent(basicPanel, fullScreenLabel,c);
		
		c.insets = new Insets(5,0,5,0);
		fullScreenCheckBox.setSelected(this.preferredFullScreenValue);
		
		c.weightx = flist;  
		c.weighty = 1.0f; 
		c.gridx = 1; c.gridy = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(basicPanel, fullScreenCheckBox,c);
		
		c.weightx = 1.0f;  
		c.weighty = 15.0f;
		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		addComponent(this.getContentPane(), tabbedPane,c);
		
		
		
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
		runButton 	= new JButton("Go!");
		
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitCode = ResolutionDialog.DIALOG_BUTTON_GO;
				setSelectedValues();
				setVisible(false);
				dispose();
				synchronized(ResolutionDialog.this) {
					ResolutionDialog.this.notify();
				}
			}
		});
		
		exitButton 	= new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitCode = ResolutionDialog.DIALOG_BUTTON_EXIT;
				setVisible(false);
				dispose();
				synchronized(ResolutionDialog.this) {
					ResolutionDialog.this.notify();
				}
			}
		});
	}
	
	/***
	 * Builds the different lists and fills them with their respective information
	 * (Available Resolutions, Available Bit Depths, Available Refresh Rates etc)
	 */
	private void buildLists() {
		resolutionsModel 	= new DefaultComboBoxModel();
		bitDepthModel 		= new DefaultComboBoxModel();
		antiAliasModel 		= new DefaultComboBoxModel();
		refreshRateModel 	= new DefaultComboBoxModel();
		
		resolutionsMap 	= new TreeMap<IntegerPair, List<DisplayMode>>();
		
		bitDepthList 	= new JComboBox();
		bitDepthList.setModel(bitDepthModel);
		
		antiAliasList	= new JComboBox();
		antiAliasList.setModel(antiAliasModel);
		antiAliasList.setRenderer(new AntiAliasModesRenderer());
		
		refreshRateList	= new JComboBox();
		refreshRateList.setModel(refreshRateModel);
		
		try {
			DisplayMode[] modes = Display.getAvailableDisplayModes();
		
			for(DisplayMode mode : modes) {
				IntegerPair modePair = new IntegerPair(mode.getWidth(), mode.getHeight());

				if(!resolutionsMap.containsKey(modePair))
					resolutionsMap.put(modePair, new ArrayList<DisplayMode>());
				
				resolutionsMap.get(modePair).add(mode);	
			}
			
			IntegerPair firstMode = null;
			
			for(IntegerPair mode : resolutionsMap.keySet()) {
				resolutionsModel.addElement(mode);
				if(firstMode == null)
					firstMode = mode;
			}
			
			fillBppAndRefreshForMode(firstMode);
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		antiAliasModel.addElement(0);
		antiAliasModel.addElement(2);
		antiAliasModel.addElement(4);
		antiAliasModel.addElement(8);
		antiAliasModel.addElement(16);
		

		resolutionsList = new JComboBox();
		resolutionsList.setModel(resolutionsModel);
		resolutionsList.setRenderer(new DisplayModesRenderer());
		resolutionsList.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				IntegerPair pair = (IntegerPair) resolutionsList.getSelectedItem();
				
				if(pair != null)
					fillBppAndRefreshForMode(pair);
			}
		});
		
		findSelectionIntegerPair(resolutionsList, preferredResolutions);
		findSelectionInteger(antiAliasList, preferredAntiAliasModes);
	}
	
	/***
	 * Reconstructs the Bpp (bits per pixel, or color depth) and refresh rates lists
	 * for a given resolution. Will then attempt to select the most compatible color depth
	 * and refresh rate given the preferred values for each
	 * @param mode The mode for which to look up the color depth and refresh rates
	 */
	private void fillBppAndRefreshForMode(IntegerPair mode) {
		List<DisplayMode> modes = resolutionsMap.get(mode);
		
		bitDepthList.setSelectedIndex(-1);
		refreshRateList.setSelectedItem(-1);
		
		bitDepthModel.removeAllElements();
		refreshRateModel.removeAllElements();
		
		TreeSet<Integer> bppSet 		= new TreeSet<Integer>();
		TreeSet<Integer> refreshRateSet = new TreeSet<Integer>();
		for(DisplayMode m : modes) {
			bppSet.add(m.getBitsPerPixel());
			refreshRateSet.add(m.getFrequency());
		}
		
		for(Integer bpp : bppSet) {
			bitDepthModel.addElement(bpp);
		}
		
		for(Integer refreshRate : refreshRateSet) {
			refreshRateModel.addElement(refreshRate);
		}

		findSelectionInteger(bitDepthList, preferredBitDepths);
		findSelectionInteger(refreshRateList, preferredRefreshRates);
		
		bitDepthList.validate();
		refreshRateList.validate();
	}
	
	/***
	 * Will attempt to locate and select a preferred integer-pair value in a given combobox
	 * @param box the combobox to look in
	 * @param preferred the list of integer pairs, in order of preference, to look for
	 */
	private void findSelectionIntegerPair(JComboBox box, int[][] preferred) {
		ComboBoxModel model = box.getModel();
		
		int arrayOff = Integer.MAX_VALUE;
		int modelOff = -1;
		for(int i=0; i<model.getSize(); i++) {
			IntegerPair pair = (IntegerPair) model.getElementAt(i);
		
			int foundOff = -1;
			for(int j=0; j<preferred.length; j++) {
				if( pair.getValueOne() == preferred[j][0] &&
					pair.getValueTwo() == preferred[j][1]) {
					foundOff = j;
					break;
				}
			}
			
			if(foundOff != -1 && foundOff < arrayOff) {
				arrayOff = foundOff;
				modelOff = i;
			}
		}
		
		if(modelOff != -1) {
			box.setSelectedIndex(modelOff);
		}
		
		box.validate();
	}
	
	/***
	 * Will attempt to locate and select a preferred integer value in a given combobox
	 * @param box the combobox to look in
	 * @param preferred the list of integers, in order of preference, to look for
	 */
	private void findSelectionInteger(JComboBox box, int[] preferred) {
		ComboBoxModel model = box.getModel();
		
		int arrayOff = Integer.MAX_VALUE;
		int modelOff = -1;
		for(int i=0; i<model.getSize(); i++) {
			Integer intVal = (Integer) model.getElementAt(i);
		
			int foundOff = -1;
			for(int j=0; j<preferred.length; j++) {
				if( intVal == preferred[j]) {
					foundOff = j;
					break;
				}
			}
			
			if(foundOff != -1 && foundOff < arrayOff) {
				arrayOff = foundOff;
				modelOff = i;
			}
		}
		
		if(modelOff != -1) {
			box.setSelectedIndex(modelOff);
		}
		
		box.validate();
	}
	
	/***
	 * Sets the selected values to the static properties of this resolution dialog
	 */
	private void setSelectedValues() {
		IntegerPair resolution;
		if(resolutionsList.getSelectedIndex() == -1) {
			resolution = (IntegerPair) resolutionsModel.getElementAt(0);
		} else {
			resolution = (IntegerPair) resolutionsModel.getElementAt(resolutionsList.getSelectedIndex());
		}
		
		Integer bitDepth;
		if(bitDepthList.getSelectedIndex() == -1) {
			bitDepth = (Integer) bitDepthModel.getElementAt(0);
		} else {
			bitDepth = (Integer) bitDepthModel.getElementAt(bitDepthList.getSelectedIndex());
		}
		
		Integer refreshRate;
		if(refreshRateList.getSelectedIndex() == -1) {
			refreshRate = (Integer) refreshRateModel.getElementAt(0);
		} else {
			refreshRate = (Integer) refreshRateModel.getElementAt(refreshRateList.getSelectedIndex());
		}
		
		Integer antiAliasMode;
		if(antiAliasList.getSelectedIndex() == -1) {
			antiAliasMode = (Integer) antiAliasModel.getElementAt(0);
		} else {
			antiAliasMode = (Integer) antiAliasModel.getElementAt(antiAliasList.getSelectedIndex());
		}
		
		for(DisplayMode mode : resolutionsMap.get(resolution)) {
			if(mode.getBitsPerPixel() == bitDepth &&
				mode.getFrequency() == refreshRate) {
				ResolutionDialog.selectedDisplayMode = mode;
				break;
			}
		}
		
		ResolutionDialog.selectedAntiAliasMode = antiAliasMode;
		
		ResolutionDialog.selectedFullScreenValue = this.fullScreenCheckBox.isSelected();
	}
	
	/***
	 * Creates a new Resolutions Dialog
	 * @param windowName the title of the dialog
	 * @param advancedPanel an optional advanced panel
	 * @param preferredResolutions a list of resolutions, in order of preference, which will be looked for
	 * @param preferredBitDepths a list of color depths, in order of preference, which will be looked for
	 * @param preferredRefreshRates a list of refresh rates, in order of preference, which will be looked for
	 * @param preferredAntialiasModes a list of antia-alias values, in order of preference, which will be looked for
	 * @param preferredFullScreenValue the initial value of the full-screen checkbox
	 */
	protected ResolutionDialog(String windowName, Container advancedPanel, int[][] preferredResolutions, int[] preferredBitDepths, int[] preferredRefreshRates, int[] preferredAntialiasModes, boolean preferredFullScreenValue) {
		super(windowName);
		
		this.preferredResolutions	= preferredResolutions;
		this.preferredBitDepths 	= preferredBitDepths;
		this.preferredRefreshRates 	= preferredRefreshRates;
		this.preferredAntiAliasModes = preferredAntialiasModes;
		this.advancedPanel			= advancedPanel;
		this.preferredFullScreenValue = preferredFullScreenValue;
		
		if(ResolutionDialog.iconImage != null)
			this.setIconImage(ResolutionDialog.iconImage);
		
		this.setSize(FRAMEWIDTH,FRAMEHEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		centerDialogOnScreen();
	
		buildLists();
		buildButtons();
		layoutControlsOnDialog();
		
		validate();
		
		this.setVisible(true);
	}
	
	/***
	 * Pops up the dialog window
	 * @param windowName the title of the dialog
	 * @param advancedPanel an optional advanced panel
	 * @param preferredResolutions a list of resolutions, in order of preference, which will be looked for
	 * @param preferredBitDepths a list of color depths, in order of preference, which will be looked for
	 * @param preferredRefreshRates a list of refresh rates, in order of preference, which will be looked for
	 * @param preferredAntialiasModes a list of antia-alias values, in order of preference, which will be looked for
	 * @param preferredFullScreenValue the initial value of the full-screen checkbox
	 * @return an integer value which represents which button was clicked (DIALOG_BUTTON_EXIT or DIALOG_BUTTON_GO)
	 */
	public static int presentDialog(String windowName, Container advancedPanel, int[][] preferredResolutions, int[] preferredBitDepths, int[] preferredRefreshRates, int[] preferredAntialiasModes, boolean preferredFullScreenValue) {
		ResolutionDialog dialog = new ResolutionDialog(
				windowName,
				advancedPanel,
				preferredResolutions,
				preferredBitDepths,
				preferredRefreshRates,
				preferredAntialiasModes,
				preferredFullScreenValue
		);
		try {
			synchronized(dialog) {
				dialog.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dialog.exitCode;
	}
	
	/***
	 * Pops up the dialog window using the default preffered values
	 * @return an integer value which represents which button was clicked (DIALOG_BUTTON_EXIT or DIALOG_BUTTON_GO)
	 */
	public static int presentDialog(String windowName) {
		return presentDialog(windowName, null);
	}
	
	/***
	 * Pops up the dialog window using the default preffered values and an
	 * advanced panel
	 * @return an integer value which represents which button was clicked (DIALOG_BUTTON_EXIT or DIALOG_BUTTON_GO)
	 */
	public static int presentDialog(String windowName, Container advancedPanel) {
		return presentDialog(windowName,advancedPanel,
				defaultPreferredResolutions,
				defaultPreferredBitDepths,
				defaultPreferredRefreshRates,
				defaultPreferredAntialiasModes,
				defaultPreferredFullScreenValue
		);
	}
	
	/***
	 * Pops up the dialog window
	 * @param windowName the title of the dialog
	 * @param preferredResolutions a list of resolutions, in order of preference, which will be looked for
	 * @param preferredBitDepths a list of color depths, in order of preference, which will be looked for
	 * @param preferredRefreshRates a list of refresh rates, in order of preference, which will be looked for
	 * @param preferredAntialiasModes a list of antia-alias values, in order of preference, which will be looked for
	 * @param preferredFullScreenValue the initial value of the full-screen checkbox
	 * @return an integer value which represents which button was clicked (DIALOG_BUTTON_EXIT or DIALOG_BUTTON_GO)
	 */
	public static int presentDialog(String windowName, int[][] preferredResolutions, int[] preferredBitDepths, int[] preferredRefreshRates, int[] preferredAntialiasModes, boolean preferredFullScreenValue) {
		return presentDialog(windowName, null, preferredResolutions, preferredBitDepths, preferredRefreshRates, preferredAntialiasModes, preferredFullScreenValue);
	}
	
	/***
	 * A little test
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("return code: " + ResolutionDialog.presentDialog("Choose Video Options"));
		System.out.println("selected resolution: " + ResolutionDialog.selectedDisplayMode);
		System.out.println("selected anti alias settings: " + ResolutionDialog.selectedAntiAliasMode);
	}
}
