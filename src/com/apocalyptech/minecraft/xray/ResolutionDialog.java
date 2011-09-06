/**
 * Copyright (c) 2005-2006, The ParticleReality Project
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
import java.awt.Font;
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

import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.JSeparator;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.AbstractAction;
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
 * ResolutionDialog.selectedFullScreenValue
 * ResolutionDialog.selectedWorld
 * </code>
 * @author Vincent Vollers
 * @version 1.0
 */
public class ResolutionDialog extends JFrame {
	private static final long serialVersionUID = -1496486770452508286L;
	private static final int FRAMEWIDTH = 400;
	private static final int FRAMEHEIGHT = 400;
	private static final int[][] defaultPreferredResolutions = 
		new int[][] {{1920,1080},{1600,900},{1280,720},{1024, 768}, {800, 600}, {666, 666}, {1280,1024}};
	// fallbackResolutions defines resolutions that we'll offer in the dropdown regardless of whether or not
	// they show up in the list of detected resolutions
	private static final int[][] fallbackResolutions =
		new int[][] {{1280,1024},{1024,768},{800,600}};
	private static final int[] defaultPreferredBitDepths =
		new int[] {32, 16, 8};
	private static final int[] defaultPreferredRefreshRates =
		new int[] {85, 80, 75, 70, 65, 60};
	private static final boolean defaultPreferredFullScreenValue = false;
	private static final boolean defaultPreferredInvertMouseValue = false;
		
	public static final int DIALOG_BUTTON_EXIT = 0;
	public static final int DIALOG_BUTTON_GO = 1;

	private JComboBox resolutionsList;
	private JComboBox bitDepthList;
	private JComboBox refreshRateList;
	private JButton runButton;
	private JButton exitButton;
	private GridBagLayout gridBagLayoutManager;
	private JPanel basicPanel;
	private JCheckBox fullScreenCheckBox;
	private JCheckBox invertMouseCheckBox;
	private ButtonGroup worldButtonGroup;
	JRadioButton[] worldButtons;
	
	private DefaultComboBoxModel resolutionsModel;
	private DefaultComboBoxModel bitDepthModel;
	private DefaultComboBoxModel refreshRateModel;
	
	private Map<IntegerPair, List<DisplayMode>> resolutionsMap;
		
	private int[][] preferredResolutions;
	private int[] preferredRefreshRates;
	private int[] preferredBitDepths;
	private boolean preferredFullScreenValue;
	private boolean preferredInvertMouseValue;
	private String preferredWorld;
	
	private int exitCode = -1;
	
	private XRayProperties xray_properties;
	
	public static DisplayMode selectedDisplayMode;
	public static int selectedRefreshRate;
	public static int selectedBitDepth;
	public static boolean selectedFullScreenValue;
	public static boolean selectedInvertMouseValue;
	public static int selectedWorld;
	
	public static Image iconImage;
	
	/***
	 * Class holding a pair of two integers where the order is determined
	 * first by the first integer and when these are equal, by the second
	 * integer. This is used for holding resolution information
	 * @author Vincent Vollers
	 */
	@SuppressWarnings("rawtypes")
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

		private static final long serialVersionUID = 8272355980006119103L;

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
	private void layoutControlsOnDialog(ArrayList<WorldInfo> availableWorlds) {
		basicPanel = new JPanel();
		
		this.getContentPane().setLayout(gridBagLayoutManager);
		basicPanel.setLayout(gridBagLayoutManager);
		GridBagConstraints c = new GridBagConstraints();
		
		JLabel resolutionsLabel = new JLabel("Resolution: ");
		JLabel bitDepthLabel 	= new JLabel("Bit Depth: ");
		JLabel refreshRateLabel = new JLabel("Refresh Rate: ");
		JLabel fullScreenLabel  = new JLabel("Full Screen: ");
		JLabel invertMouseLabel  = new JLabel("Invert Mouselook: ");
		JLabel xrayTitleLabel  = new JLabel(XRay.windowTitle);
		xrayTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		
		float flabel = 0f;
		float flist = 1.9f;

		int current_grid_y = 0;

		Insets labelInsets = new Insets(0, 5, 0, 0);
		Insets inputInsets = new Insets(5, 5, 5, 5);
		Insets checkboxInsets = new Insets(5, 0, 5, 0);
		
		c.insets = new Insets(5,5,5,5);
		c.weighty = 0f;

		// Title
		c.gridx = 0; c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 2;
		addComponent(basicPanel, xrayTitleLabel, c);
		c.gridwidth = 1;
		
		// Add the resolution label
		current_grid_y++;
		c.weightx = flabel; 
		c.gridx = 0; c.gridy = current_grid_y;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.insets = labelInsets;
		addComponent(basicPanel, resolutionsLabel,c);
		
		// Add the resolution list
		c.weightx = flist; 
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1; c.gridy = current_grid_y;
		c.insets = inputInsets;
		addComponent(basicPanel, resolutionsList,c);
		
		// Add the bit depth label
		current_grid_y++;
		c.weightx = flabel; 
		c.gridx = 0; c.gridy = current_grid_y;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.insets = labelInsets;
		addComponent(basicPanel, bitDepthLabel,c);
		
		// Add the bit depth list
		c.weightx = flist;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1; c.gridy = current_grid_y;
		c.insets = inputInsets;
		addComponent(basicPanel, bitDepthList,c);
		
		// Add the refresh rate label
		current_grid_y++;
		c.weightx = flabel;
		c.gridx = 0; c.gridy = current_grid_y;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.insets = labelInsets;
		addComponent(basicPanel, refreshRateLabel,c);
		
		// Add the refresh rate list
		c.weightx = flist;  
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1; c.gridy = current_grid_y;
		c.insets = inputInsets;
		addComponent(basicPanel, refreshRateList,c);
		
		// Add the fullscreen label
		current_grid_y++;
		c.weightx = flabel; 
		c.gridx = 0; c.gridy = current_grid_y;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.insets = labelInsets;
		addComponent(basicPanel, fullScreenLabel,c);

		// Set up the fullscreen checkbox
		fullScreenCheckBox = new JCheckBox();
		fullScreenCheckBox.setSelected(this.preferredFullScreenValue);
		
		// Add the fullscreen checkbox
		c.weightx = flist;  
		c.gridx = 1; c.gridy = current_grid_y;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = checkboxInsets;
		addComponent(basicPanel, fullScreenCheckBox,c);
		
		// Add the invert mouse label
		current_grid_y++;
		c.weightx = flabel; 
		c.gridx = 0; c.gridy = current_grid_y;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.insets = labelInsets;
		addComponent(basicPanel, invertMouseLabel,c);

		// Set up the invert mouse checkbox
		invertMouseCheckBox = new JCheckBox();
		invertMouseCheckBox.setSelected(this.preferredInvertMouseValue);
		
		// Add the invert mouse checkbox
		c.weightx = flist;  
		c.gridx = 1; c.gridy = current_grid_y;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = checkboxInsets;
		addComponent(basicPanel, invertMouseCheckBox,c);
		
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
		
		// World Label
		current_grid_y++;
		c.gridx = 0; c.gridy = current_grid_y;
		c.gridwidth = 2;
		addComponent(basicPanel, new JLabel("Choose a World to Open:"), c);
		
		// Create a buttongroup and radio buttons
		worldButtonGroup = new ButtonGroup();
		worldButtons = new JRadioButton[availableWorlds.size()];
		int curidx = 0;
		int selectedWorld = 0;
		boolean matched_world = false;
		for (WorldInfo world : availableWorlds)
		{
			JRadioButton button;
			if (world.isCustom())
			{
				button = new JRadioButton("Other...");				
			}
			else if (!world.isOverworld())
			{
				if (world.isKnownDimension())
				{
					button = new JRadioButton(world.getDirName() + ": " + world.getDimensionDesc() + " Dimension");
				}
				else
				{
					button = new JRadioButton(world.getDirName() + ": " + world.getDimensionDesc());
				}
			}
			else
			{
				if (world.getLevelName() != null && !world.getDirName().equals(world.getLevelName()))
				{
					button = new JRadioButton(world.getDirName() + " (" + world.getLevelName() + ")");
				}
				else
				{
					button = new JRadioButton(world.getDirName());
				}
			}
			worldButtonGroup.add(button);
			worldButtons[curidx] = button;
			if (!matched_world && this.preferredWorld != null)
			{
				if (world.isCustom())
				{
					selectedWorld = curidx;
					matched_world = true;
				}
				else if (world.getBasePath().equalsIgnoreCase(this.preferredWorld))
				{
					selectedWorld = curidx;
					matched_world = true;
				}
			}
			curidx += 1;
		}
		worldButtons[selectedWorld].setSelected(true);

		// World scrollpane
		JPanel worldPanel = new JPanel();
		GridBagLayout worldLayout = new GridBagLayout();
		worldPanel.setLayout(worldLayout);
		GridBagConstraints wc = new GridBagConstraints();
		JScrollPane worldPane = new JScrollPane(worldPanel);
		worldPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		worldPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		worldPane.setPreferredSize(new Dimension(200, 300));
		worldPane.setBorder(null);
		current_grid_y++;
		c.insets = new Insets(5, 15, 5, 5);
		c.gridx = 0;
		c.gridy = current_grid_y;
		c.gridwidth = 2;
		c.weightx = 1f;
		c.weighty = 1f;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		addComponent(basicPanel, worldPane, c);
		
		// Now insert the world radio buttons
		int current_wc_grid_y = 0;
		wc.insets = new Insets(0, 0, 0, 0);
		wc.gridx = 0;
		wc.gridwidth = 1;
		wc.anchor = GridBagConstraints.NORTHWEST;
		wc.weightx = 1f;
		wc.weighty = 0f;
		for (JRadioButton button : worldButtons)
		{
			wc.gridy = current_wc_grid_y;
			if (current_wc_grid_y == worldButtons.length-1)
			{
				wc.weighty = 1f;
			}
			addComponent(worldPanel, button, wc, worldLayout);
			current_wc_grid_y++;
		}
		
		// Add our JPanel to the window
		c.weightx = 1.0f;  
		c.weighty = 1f;
		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		addComponent(this.getContentPane(), basicPanel,c);
		
		// Now add the buttons
		c.insets = new Insets(5,15,5,15);
		c.gridwidth = 1;
		
		c.weightx = flabel; 
		c.weighty = 0f; 
		c.gridx = 0; c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(this.getContentPane(), exitButton,c);
		
		c.weightx = flist; 
		c.weighty = 0f; 
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
		addComponent(root, comp, constraints, gridBagLayoutManager);
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

        // The OK Button
		runButton 	= new JButton("Go!");
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                dialogGo();
			}
		});

        // Key mapping for the OK button
        KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterStroke, "ENTER");
        rootPane.getActionMap().put("ENTER", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
                dialogGo();
			}
        });
		
        // The Exit button
		exitButton 	= new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                dialogExit();
			}
		});

        // Key mapping for the Exit button
        KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "ESCAPE");
        rootPane.getActionMap().put("ESCAPE", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
                dialogExit();
			}
        });
	}

    /**
     * Actions to perform if the "Go" button is hit, or otherwise triggered.
     */
    private void dialogGo()
    {
        exitCode = ResolutionDialog.DIALOG_BUTTON_GO;
        setSelectedValues();
        setVisible(false);
        dispose();
        synchronized(ResolutionDialog.this) {
            ResolutionDialog.this.notify();
        }
    }

    /**
     * Actions to perform if the "Exit" button is hit, or otherwise triggered.
     */
    private void dialogExit()
    {
        exitCode = ResolutionDialog.DIALOG_BUTTON_EXIT;
        setVisible(false);
        dispose();
        synchronized(ResolutionDialog.this) {
            ResolutionDialog.this.notify();
        }
    }
	
	/***
	 * Builds the different lists and fills them with their respective information
	 * (Available Resolutions, Available Bit Depths, Available Refresh Rates etc)
	 */
	private void buildLists() {
		resolutionsModel 	= new DefaultComboBoxModel();
		bitDepthModel 		= new DefaultComboBoxModel();
		refreshRateModel 	= new DefaultComboBoxModel();
		
		resolutionsMap 	= new TreeMap<IntegerPair, List<DisplayMode>>();
		
		bitDepthList 	= new JComboBox();
		bitDepthList.setModel(bitDepthModel);
		
		refreshRateList	= new JComboBox();
		refreshRateList.setModel(refreshRateModel);
		
		// Create a map of our fallback resolutions
		HashMap<Integer, Boolean> fallbackMap = new HashMap<Integer, Boolean>();
		for (int[] res : fallbackResolutions)
		{
			fallbackMap.put(res[0]*10000 + res[1], false);
		}
		
		try {
			DisplayMode[] modes = Display.getAvailableDisplayModes();
		
			for(DisplayMode mode : modes) {
				IntegerPair modePair = new IntegerPair(mode.getWidth(), mode.getHeight());
				
				// Mark that we've seen our fallback resolution if it exists
				int hash = mode.getWidth()*10000 + mode.getHeight();
				if (fallbackMap.containsKey(hash))
					fallbackMap.put(hash, true);
				
				// Now add the mode into the full list
				if(!resolutionsMap.containsKey(modePair))
					resolutionsMap.put(modePair, new ArrayList<DisplayMode>());
				
				resolutionsMap.get(modePair).add(mode);	
			}
			
			// Add in our resolutions that we want to display regardless of what was auto-detected
			DisplayMode curMode = Display.getDisplayMode();
			for(int[] res: fallbackResolutions)
			{
				int hash = res[0]*10000+res[1];
				if (fallbackMap.containsKey(hash))
				{
					//System.out.println(res[0] + "x" + res[1] + ": " + preferredMap.get(hash));
					if (!fallbackMap.get(hash))
					{
						if (res[0] <= curMode.getWidth() && res[1] <= curMode.getHeight())
						{
							DisplayMode mode = new DisplayMode(res[0], res[1]);
							ArrayList<DisplayMode> modelist = new ArrayList<DisplayMode>();
							modelist.add(mode);
							resolutionsMap.put(new IntegerPair(res[0], res[1]), modelist);
						}
					}
				}
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
		
		for(DisplayMode mode : resolutionsMap.get(resolution)) {
			if(mode.getBitsPerPixel() == bitDepth &&
				mode.getFrequency() == refreshRate) {
				ResolutionDialog.selectedDisplayMode = mode;
				break;
			}
		}
		
		ResolutionDialog.selectedRefreshRate = refreshRate;
		ResolutionDialog.selectedBitDepth = bitDepth;
		
		ResolutionDialog.selectedFullScreenValue = this.fullScreenCheckBox.isSelected();
		ResolutionDialog.selectedInvertMouseValue = this.invertMouseCheckBox.isSelected();
		
		for (int i=0; i<worldButtons.length; i++)
		{
			if (worldButtons[i].isSelected())
			{
				ResolutionDialog.selectedWorld = i;
				break;
			}
		}
		
		// Also set the info in our properties object
		this.xray_properties.setIntProperty("LAST_RESOLUTION_X", ResolutionDialog.selectedDisplayMode.getWidth());
		this.xray_properties.setIntProperty("LAST_RESOLUTION_Y", ResolutionDialog.selectedDisplayMode.getHeight());
		this.xray_properties.setIntProperty("LAST_BPP", ResolutionDialog.selectedBitDepth);
		this.xray_properties.setIntProperty("LAST_REFRESH_RATE", ResolutionDialog.selectedRefreshRate);
		this.xray_properties.setBooleanProperty("LAST_FULLSCREEN", ResolutionDialog.selectedFullScreenValue);
		this.xray_properties.setBooleanProperty("LAST_INVERT_MOUSE", ResolutionDialog.selectedInvertMouseValue);
		// World directory preference is set out in XRay.java, because we might have loaded a world from an arbitrary dir
	}
	
	/**
	 * Given an array of ints, prepend it with a value from our properties file, if it
	 * exists.
	 * 
	 * @param prop_name
	 * @param dest_var
	 * @param existing_var
	 */
	private int[] prepend_int_array(String prop_name, int[] dest_var, int[] existing_var)
	{
		String val = this.xray_properties.getProperty(prop_name);
		if (val != null)
		{
			dest_var = new int[existing_var.length+1];
			dest_var[0] = Integer.valueOf(val);
			for (int i=0; i<existing_var.length; i++)
			{
				dest_var[i+1] = existing_var[i];
			}
		}
		return dest_var;
	}
	
	/***
	 * Creates a new Resolutions Dialog
	 * @param windowName the title of the dialog
	 * @param advancedPanel an optional advanced panel
	 * @param preferredResolutions a list of resolutions, in order of preference, which will be looked for
	 * @param preferredBitDepths a list of color depths, in order of preference, which will be looked for
	 * @param preferredRefreshRates a list of refresh rates, in order of preference, which will be looked for
	 * @param preferredFullScreenValue the initial value of the full-screen checkbox
	 */
	protected ResolutionDialog(String windowName, Container advancedPanel,
			int[][] preferredResolutions, int[] preferredBitDepths, int[] preferredRefreshRates,
			boolean preferredFullScreenValue, boolean preferredInvertMouseValue,
			ArrayList<WorldInfo> availableWorlds, XRayProperties xray_properties) {
		super(windowName);
		
		this.xray_properties		= xray_properties;
		this.preferredResolutions	= preferredResolutions;
		this.preferredBitDepths 	= preferredBitDepths;
		this.preferredRefreshRates 	= preferredRefreshRates;
		this.preferredFullScreenValue = preferredFullScreenValue;
		this.preferredInvertMouseValue = preferredInvertMouseValue;
		
		// Load last-used resolution/display information from our properties file
		String val_1 = this.xray_properties.getProperty("LAST_RESOLUTION_X");
		String val_2 = this.xray_properties.getProperty("LAST_RESOLUTION_Y");
		if (val_1 != null && val_2 != null)
		{
			this.preferredResolutions = new int[preferredResolutions.length+1][];
			this.preferredResolutions[0] = new int[2];
			this.preferredResolutions[0][0] = Integer.valueOf(val_1);
			this.preferredResolutions[0][1] = Integer.valueOf(val_2);
			for (int i=0; i<preferredResolutions.length; i++)
			{
				this.preferredResolutions[i+1] = preferredResolutions[i];
			}
		}
		this.preferredBitDepths = this.prepend_int_array("LAST_BPP", this.preferredBitDepths, preferredBitDepths);
		this.preferredRefreshRates = this.prepend_int_array("LAST_REFRESH_RATE", this.preferredRefreshRates, preferredRefreshRates);
		this.preferredWorld = this.xray_properties.getProperty("LAST_WORLD");
		
		this.preferredFullScreenValue = this.xray_properties.getBooleanProperty("LAST_FULLSCREEN", preferredFullScreenValue);
		this.preferredInvertMouseValue = this.xray_properties.getBooleanProperty("LAST_INVERT_MOUSE", preferredInvertMouseValue);
		
		if(ResolutionDialog.iconImage != null)
			this.setIconImage(ResolutionDialog.iconImage);
		
		this.setSize(FRAMEWIDTH,FRAMEHEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(FRAMEWIDTH, FRAMEHEIGHT));

		centerDialogOnScreen();
	
		buildLists();
		buildButtons();
		layoutControlsOnDialog(availableWorlds);
		
		validate();
		
		this.setVisible(true);
		
		// Adjust to the appropriate height, in case our list of worlds is too long.
		// This should correctly deal with differences in WM decoration size.
		Dimension preferred = this.getContentPane().getPreferredSize();
		int framediff = FRAMEHEIGHT - this.getContentPane().getHeight();
		if (preferred.height > FRAMEHEIGHT-framediff)
		{
			this.setSize(FRAMEWIDTH, preferred.height+framediff);
		}
	}
	
	/***
	 * Pops up the dialog window
	 * @param windowName the title of the dialog
	 * @param advancedPanel an optional advanced panel
	 * @param preferredResolutions a list of resolutions, in order of preference, which will be looked for
	 * @param preferredBitDepths a list of color depths, in order of preference, which will be looked for
	 * @param preferredRefreshRates a list of refresh rates, in order of preference, which will be looked for
	 * @param preferredFullScreenValue the initial value of the full-screen checkbox
	 * @param preferredInvertMouseValue the initial value of the invert mouse checkbox
	 * @return an integer value which represents which button was clicked (DIALOG_BUTTON_EXIT or DIALOG_BUTTON_GO)
	 */
	public static int presentDialog(String windowName, Container advancedPanel,
			int[][] preferredResolutions, int[] preferredBitDepths, int[] preferredRefreshRates,
			boolean preferredFullScreenValue, boolean preferredInvertMouseValue,
			ArrayList<WorldInfo> availableWorlds, XRayProperties xray_properties) {
		ResolutionDialog dialog = new ResolutionDialog(
				windowName,
				advancedPanel,
				preferredResolutions,
				preferredBitDepths,
				preferredRefreshRates,
				preferredFullScreenValue,
				preferredInvertMouseValue,
				availableWorlds,
				xray_properties
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
	public static int presentDialog(String windowName, ArrayList<WorldInfo> availableWorlds, XRayProperties xray_properties) {
		return presentDialog(windowName, null, availableWorlds, xray_properties);
	}
	
	/***
	 * Pops up the dialog window using the default preffered values and an
	 * advanced panel
	 * @return an integer value which represents which button was clicked (DIALOG_BUTTON_EXIT or DIALOG_BUTTON_GO)
	 */
	public static int presentDialog(String windowName, Container advancedPanel,
			ArrayList<WorldInfo> availableWorlds, XRayProperties xray_properties) {
		return presentDialog(windowName,advancedPanel,
				defaultPreferredResolutions,
				defaultPreferredBitDepths,
				defaultPreferredRefreshRates,
				defaultPreferredFullScreenValue,
				defaultPreferredInvertMouseValue,
				availableWorlds,
				xray_properties
		);
	}
	
	/***
	 * Pops up the dialog window
	 * @param windowName the title of the dialog
	 * @param preferredResolutions a list of resolutions, in order of preference, which will be looked for
	 * @param preferredBitDepths a list of color depths, in order of preference, which will be looked for
	 * @param preferredRefreshRates a list of refresh rates, in order of preference, which will be looked for
	 * @param preferredFullScreenValue the initial value of the full-screen checkbox
	 * @return an integer value which represents which button was clicked (DIALOG_BUTTON_EXIT or DIALOG_BUTTON_GO)
	 */
	public static int presentDialog(String windowName,
			int[][] preferredResolutions, int[] preferredBitDepths, int[] preferredRefreshRates,
			boolean preferredFullScreenValue, boolean preferredInvertMouseValue,
			ArrayList<WorldInfo> availableWorlds, XRayProperties xray_properties) {
		return presentDialog(windowName, null,
				preferredResolutions, preferredBitDepths, preferredRefreshRates,
				preferredFullScreenValue, preferredInvertMouseValue,
				availableWorlds, xray_properties);
	}
}
