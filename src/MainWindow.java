/***
 * MainWindow - This class manages the main window for the AniDB application.
 * 
 * Copyright (C) 2011  Chris Workman
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class MainWindow extends JFrame implements ActionListener {
	/*** CONSTANT ***/
	private static final long serialVersionUID = 1L;

	/*** CLASS DATA MEMBERS ***/
	private File[] 					files 		= null;
	private LinkedList<String>		fileTitles;
	private LinkedList<FileDetails>	fileDetails;
	private AnimeTitles				animeTitles;
	
	/*** WINDOW COMPONENTS ***/
	// Menu Bar Components
	JMenuBar menuBar;
	JMenu fileMenu;					
	JMenu editMenu;
	JMenu helpMenu;
	JMenuItem fileOpen;
	JMenuItem fileExit;
	JMenuItem helpUpdate;
	JMenuItem helpAbout;
	JMenuItem editPreferences;
	// Info Panel
	InfoPanel infoPanel;
	// Buttons
	JButton bCreate;
	JButton bClose;
	
	public MainWindow() {
		// Load config settings
		int result = ConfigMgr.loadConfig();
		
		// Check results of config loading
		// Is this the first load (we have a new config file)
		if (ConfigMgr.isFirstLoad()) {
			JOptionPane.showMessageDialog(this, "It appears this is your first time running the AniDB NFO File Creator.\n " +
						"Thank you for choosing this program. In a moment the Configuration Settings screen will be displayed for you.\n " +
						"You will need to set your AniDB username and password in this screen before you will be able to properly use the program.",
						"Welcome to AniDB NFO Creator", JOptionPane.PLAIN_MESSAGE);
			ConfigDlg config = new ConfigDlg(this);
			config.showDialog();
			
			// Now that it has run once, clear the firstLoad
			ConfigMgr.setFirstLoad(false);
			ConfigMgr.saveConfig();
		} // end firstload
		
		// A problem was encountered reading the file
		else if (result == ConfigMgr.INVALID_CONFIG || result == ConfigMgr.UNREADABLE_FILE) {
			JOptionPane.showMessageDialog(this, "A problem was encountered when reading the configuration settings.\n" + 
					"Please review the configuration settings before we recreate the configuration file for you.", "Error Reading File", JOptionPane.ERROR_MESSAGE);
			ConfigDlg config = new ConfigDlg(this);
			config.showDialog();
			
			// ensure configuration is saved
			ConfigMgr.saveConfig();
		} // end error
		
		// Set values for this window
		this.setTitle("AniNFO - Anime NFO Creator");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		/// Build the Window Components ///
		// Build the menu bar
		buildMenu();
		
		// Add the info panel for file list and file details
		infoPanel = new InfoPanel(this.getContentPane());
		
		// Add the Create Files and Close buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		bCreate = new JButton("Create Files");
		bCreate.addActionListener(this);
		bClose = new JButton("Close");
		bClose.addActionListener(this);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(bCreate);
		buttonPanel.add(Box.createRigidArea(new Dimension(5,0)));
		buttonPanel.add(bClose);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		// Pack window components and display window
		this.pack();
		this.setVisible(true);
		
		// Perform update check on database file
		updateDatabase();		
	} // end MainWindow
	
	private void buildMenu() {
		// Initialize the menu bar
		menuBar = new JMenuBar();
		
		// Build the File menu
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		fileOpen = new JMenuItem("Open", 'O');
		fileOpen.addActionListener(this);
		fileExit = new JMenuItem("Exit", 'x');
		fileMenu.add(fileOpen);
		fileMenu.addSeparator();
		fileMenu.add(fileExit);
		
		fileExit.addActionListener(this);
		
		// Build Edit menu
		editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');
		editPreferences = new JMenuItem("Preferences", 'P');
		editPreferences.addActionListener(this);
		editMenu.add(editPreferences);
		
		// Build help menu
		helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		helpUpdate = new JMenuItem("Update Database", 'U');
		helpUpdate.addActionListener(this);
		helpAbout = new JMenuItem("About...", 'A');
		helpAbout.addActionListener(this);
		helpMenu.add(helpUpdate);
		helpMenu.addSeparator();
		helpMenu.add(helpAbout);		
		
		//Add menus to menu bar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(helpMenu);
		
		// Add menu to window
		//this.getContentPane().add(menuBar, BorderLayout.NORTH);
		this.setJMenuBar(menuBar);
	} // end buildMenu
	
	private void updateDatabase() {
		// Check that we have an upto date database file
		if (!DatabaseMgr.isCurrent()) {
			if (ConfigMgr.isAutoUpdate()) {
				// Get the updated file
				int result = DatabaseMgr.getUpdatedDatabase();
					
				// Check for errors
				if (result == DatabaseMgr.FILE_NOT_FOUND) {
					JOptionPane.showMessageDialog(this, "Unable to find necessary file. This usually occurs if the animetitles.xml.gz did not" +
							" download properly.", "File Not Found", JOptionPane.WARNING_MESSAGE);
				} // end FILE_NOT_FOUND
				else if (result == DatabaseMgr.INVALID_URL) {
					JOptionPane.showMessageDialog(this, "Unable to access AniDB Databse Dump URL.", "Invalid URL", 
							JOptionPane.WARNING_MESSAGE);
				} // end INVALID_URL
				else if (result == DatabaseMgr.UNREADABLE_FILE) {
					JOptionPane.showMessageDialog(this, "Unable to access one of the necessary files due. The file may be in use or you " +
							"may not have permission to access the file.", "I/O Exception", JOptionPane.WARNING_MESSAGE);
				} // end UNREADABLE_FILE
			} // end if autoupdate
		} // end isCurrent
		
		// Read the database XML file into the AnimeTitles structure
		DatabaseMgr.loadTitles();
		animeTitles = DatabaseMgr.getTitles();
	} // end updateDatabase
	
	private void connect() {
		// Authenticate to AniDB server
		AniPacket packet = CommMgr.sendAuth();
		
		// NOTE: Add code to handle NAT checking. 
		// Verify that we have authenticated successfully.
		if (packet.isError()) {
			// Packet data received
			int code = packet.getCode();
			String reply = packet.getReply();

			// Used for generating popup messages
			String msg;
			String eTitle;
			String reason;
			
			// Provide user feedback based on the error received
			switch (code) {
			case CommMgr.CLIENT_ERROR:
				msg = reply;
				eTitle = "Client Error";
				break;
			case CommMgr.ILLEGAL_INPUT_ACCESS_DENIED:	
				msg = "The server did not recognize the data sent.";
				eTitle = "Illegal Input";
				break;
			case CommMgr.INTERNAL_SERVER_ERROR:
				msg = "An internal server error was encountered";
				eTitle = "Internal Server Error";
				break;
			case CommMgr.OUT_OF_SERVICE:
				msg = "The server is currently down for maintenance.\nPlease try again later.";
				eTitle = "Server Out of Service";
				break;
			case CommMgr.SERVER_BUSY:
				msg = "The server is too busy to process your request at this time.\nPlease try again later.";
				eTitle = "Server Busy";
				break;
			case CommMgr.UNKNOWN_COMMAND:
				msg = "The command was not recongnized by the server.";
				eTitle = "Unknown Command";
				break;
			case CommMgr.BANNED:
				reason = reply.substring(reply.indexOf('\n'));
				msg = "Your account has been banned by the AniDB server.\nReason: " + reason;
				eTitle = "Account Banned";
				break;
			case CommMgr.CLIENT_OUTDATED:
				msg = "The client uses an outdated version of the AniDB API.\nPlease update your client.";
				eTitle = "Version Outdated";
				break;
			case CommMgr.CLIENT_BANNED:
				reason = reply.substring(reply.indexOf('-')+2);
				msg = "This client has been banned.\nReason: " + reason;
				eTitle = "Client Banned";
				break;
			case CommMgr.LOGIN_FAILED:
				msg = "Authentication failed.\nPlease check your information and try again.";
				eTitle = "Login Failed";
				break;
			default:
				msg = "An unknown error has occurred.";
				eTitle = "Unknown Error";
				break;
			} // end switch
			
			JOptionPane.showMessageDialog(this, msg, eTitle, JOptionPane.ERROR_MESSAGE);
		} // end if
	} // end connect
	
	private void loadFiles() {
		// Get file list from File Browser
		JFileChooser ofDialog = new JFileChooser();
		ofDialog.setMultiSelectionEnabled(true);
		ofDialog.showOpenDialog(this);
		
		files = ofDialog.getSelectedFiles();
		
		parseTitles();
	} // end loadFiles
	
	private void parseTitles() {
		FileDetails file;
		String 	filename	= null;
		String 	series		= null;
		int 	epno		= 0;
		int 	season		= 1;
		String temp;
		boolean isMovie		= false;
		
		// Initialize filetitles
		fileTitles = new LinkedList<String>();
		
		// If we have files check for titles
		if (files != null) {
			fileDetails = new LinkedList<FileDetails>();
			
			// Process each file for relevant information
			for (int i = 0; i < files.length; i++) {
				// Get the filename, without extension, replace . and _ with space
				filename = files[i].getName();
				temp = filename.substring(0,filename.lastIndexOf('.'));
				temp = filename.replace('.', ' ');
				temp = temp.replace('_', ' ');

				// Try finding match for E##
				String matchstr = filename.replace('.', ' ');
				matchstr = matchstr.replace('_', ' ');
				String regex = "\\s+E\\d{2,}\\s+"; 		// First boxee format requires E followed by two numbers for the episode number
				Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				Matcher match = pattern.matcher(matchstr);
				
				// Check if a match was found
				if (match.find()) {
					temp = match.group().trim();
					series = matchstr.substring(0,match.start()).trim();
					try {
						epno = Integer.parseInt(temp.substring(1));
					}
					catch (NumberFormatException e) {
						isMovie = true;
					}
				} // end match
				// If not found then try second format
				else {
					regex = "\\s+\\d+x\\d{2,}\\s+";		// Second boxee format
					pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
					match = pattern.matcher(matchstr);
					
					// Check for a match
					if (match.find()) {
						temp = match.group().trim();
						String[] words = temp.split("[xX]");
						season = Integer.parseInt(words[0]);
						series = matchstr.substring(0,matchstr.indexOf(temp));
						try {
							epno = Integer.parseInt(words[1]);
						}
						catch (NumberFormatException e) {
							isMovie = true;
						}
					} // end if
					else {
						series = "Did not find a naming convention match for " + filename;
					}
				} // end if matches
				
				if (isMovie)
					file = new FileDetails(filename, series);
				else
					file = new FileDetails(filename, series, epno);

				if (season != 1) 
					file.setSeason(season);
				fileDetails.add(file);
				
				// Add the title to the title list if it doesn't exist already
				if (!fileTitles.contains(series))
					fileTitles.add(series);
			} // end for

			// Iterate through the list of file series titles found
			// and associate the correct AniDB entry with each file
			for (int i = 0; i < fileTitles.size(); i ++) {
				AnimeTitles titles = animeTitles.searchTitles(fileTitles.get(i));
				
				// If there is more than one title matched provide a list to select
				// from.
				if (titles.size() > 1) {
					TitleSelector tsWnd = new TitleSelector(titles);
					AnimeTitle title = tsWnd.showDialog();
					
					if (title != null)
						linkFileDetails(fileTitles.get(i), title);
					else
						JOptionPane.showMessageDialog(this,	"No anime title was selected from the list.", "Error - No Selection Made", JOptionPane.ERROR_MESSAGE);
				} // end if
				// Only 1 match was found, just use it.
				else if (titles.size() != 0) {
					linkFileDetails(fileTitles.get(i), titles.getFirst());
				} // end else
				else {
					JOptionPane.showMessageDialog(this, "There were no matching anime titles found in the database.", "Error - No Match Found", JOptionPane.ERROR_MESSAGE);
				}
			} // end for
			
			// Refresh the file list
			infoPanel.refreshFileList();
		} // end if
	} // end parseTitles
	
	private void linkFileDetails(String title, AnimeTitle anime) {
		// Check each file for matching title, update details from the provided anime
		for (int i = 0; i < fileDetails.size(); i++) {
			FileDetails file = fileDetails.get(i);
			if (file.getSeries().compareToIgnoreCase(title) == 0) {
				file.setSeries(anime.getMainTitle());
				file.setAID(anime.getID());
			} // end if
		} // end for
	} // end linkFileDetails
	
	@Override
	public void dispose() {
		CommMgr.sendLogout();
		super.dispose();
	} // end dispose
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// Handle Open menu item
		if (e.getSource() == fileOpen) {
			loadFiles();
			infoPanel.refreshFileList();
		} // end fileOpen
		
		// Handle Exit menu item
		if (e.getSource() == fileExit) {
			this.dispose();
		} // end fileExit
		
		// Handle Preferences menu item
		if (e.getSource() == editPreferences) {
			ConfigDlg config = new ConfigDlg(this);
			int result = config.showDialog();
			if (result == ConfigDlg.OK)
				ConfigMgr.saveConfig();
		} // end editPreferences
		
		// Handle Update menu item
		if (e.getSource() == helpUpdate) {
			updateDatabase();
		} // end helpUpdate
		
		// Handle About menu item
		if (e.getSource() == helpAbout) {
			AboutDlg about = new AboutDlg(this);
			about.setVisible(true);
		} // end helpAbout
		
		// Button Responses
		// Create Button
		if (e.getSource() == bCreate) {
			//// Create the files - Code needs to be added ////
			// Empty the file list
			fileTitles = new LinkedList<String>();
			fileDetails = new LinkedList<FileDetails>();
			infoPanel.refreshFileList();
		}
		
		// Close Button
		if (e.getSource() == bClose) {
			this.dispose();
		}
		
	} // end actionPerformed
	
	class InfoPanel extends JPanel implements ListSelectionListener {
		/*** CONSTANTS ***/
		private static final long serialVersionUID = 1L;
		
		/*** PANEL COMPONENTS ***/
		private JTextArea seriesText;
		private JList<FileDetails> fileList;
		
		public InfoPanel(Container parent) {
			// Create the file list panel
			JPanel filePanel = new JPanel();
			filePanel.setBorder(BorderFactory.createTitledBorder("File Listing"));
			
			// Create the list box for the panel
			fileList = new JList<FileDetails>();
			fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			fileList.setLayoutOrientation(JList.VERTICAL);
			fileList.setVisibleRowCount(20);
			
			// Set listener
			fileList.addListSelectionListener(this);
			
			// Create the scroll window
			JScrollPane fileScroller = new JScrollPane(fileList);
			
			// Add components to file panel
			filePanel.add(fileScroller);
			
			// Create the Series Info panel
			JPanel seriesPanel = new JPanel();
			seriesPanel.setBorder(BorderFactory.createTitledBorder("Details"));
			
			// Create the series info text field
			seriesText = new JTextArea(20, 25);
			seriesText.setText("Nothing selected.");
			seriesText.setEditable(false);
			
			// Create the scroll window
			JScrollPane seriesScroller = new JScrollPane(seriesText);
			
			// add components to series panel
			seriesPanel.add(seriesScroller);
			
			// Add panels to this component
			this.add(filePanel);
			this.add(seriesPanel);
			
			// Add this panel to the parent container
			parent.add(this, BorderLayout.CENTER);
		} // end InfoPanel
		
		public void refreshFileList() {
			fileList.setListData(fileDetails.toArray(new FileDetails[0]));
			fileList.repaint();
		} // end refreshFileList
		
		public void setDetails(FileDetails file) {
			String text = "Series: " + file.getSeries() + "\n";
			
			// If the AID was found for this anime
			if (file.getAID() > 0) {
				text += "AniDB AId: " + file.getAID() + "\n";
			}
			// Additional details for series episodes
			if (!file.isMovie()) {
				text += "Season: " + file.getSeason() + "\n";
				text += "Episode: " + file.getEpno();
			}
			
			seriesText.setText(text);
			seriesText.repaint();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int index = e.getFirstIndex();
			FileDetails file = fileDetails.get(index);
			setDetails(file);
		}
		
	} // end class InfoPanel
	
	class TitleSelector extends JDialog implements ActionListener {
		
		/*** CONSTANTS ***/
		private static final long serialVersionUID = 1L;
		private final int WIDTH = 25;
		
		/*** CLASS DATA MEMBERS ***/
		// Window elements
		private JButton				bOk;				// OK Button
		private JButton				bCancel;			// Cancel button
		private JList<AnimeTitle> 	listTitles;			// List of tiltes
		
		// Data elements
		AnimeTitles	titleList;			// List of titles
		AnimeTitle	selectedTitle;		// The selected anime title
		
		// Constructor
		public TitleSelector(AnimeTitles newList) {
			// Set the window title
			this.setTitle("Anime Title Selection");
			
			// Set the title list
			titleList = newList;
			
			// Populate the dialog box
			// Create and populate the list model
			DefaultListModel model = new DefaultListModel();
			for (int i = 0; i < titleList.size(); i++)
				model.add(i, titleList.get(i));
			
			// Start with the list box and a panel to contain it
			JPanel list = new JPanel();
			list.setBorder(BorderFactory.createTitledBorder("Possible Title Matches"));
			listTitles = new JList<AnimeTitle>(model);
			listTitles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listTitles.setLayoutOrientation(JList.VERTICAL);
			listTitles.setVisibleRowCount(10);
			JScrollPane scrollList = new JScrollPane(listTitles);
			list.add(scrollList);
			
			// Add buttons to a panel
			JPanel buttons = new JPanel();
			bOk = new JButton("Ok");
			bOk.addActionListener(this);
			bCancel = new JButton("Cancel");
			bCancel.addActionListener(this);
			buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
			buttons.add(Box.createHorizontalGlue());
			buttons.add(bOk);
			buttons.add(Box.createRigidArea(new Dimension(5,0)));
			buttons.add(bCancel);
			
			// Add the panels to the dialog
			this.getContentPane().add(list, BorderLayout.NORTH);
			this.getContentPane().add(buttons, BorderLayout.SOUTH);
			
			// Make this a modal dialog box
			this.setModal(true);
			this.pack();
		} // end constructor

		@Override
		public void actionPerformed(ActionEvent e) {
			// If the OK button was selected
			if (e.getSource() == bOk) {
				selectedTitle = (AnimeTitle)listTitles.getSelectedValue();
			}
			else {
				selectedTitle = null;
			}
			
			// Get rid of the window
			this.setVisible(false);
			this.dispose();
		} // end actionPerformed
		
		public AnimeTitle showDialog() {
			this.setVisible(true);
			return selectedTitle;
		} // end showDialog
		
	} // end class TitleSelector
	
	/***
	 * This class controls the appearance of the About dialog
	 * window displayed when selecting Help > About...
	 * 
	 * @author Chris Workman
	 *
	 */
	class AboutDlg extends JDialog implements ActionListener {
		
		/*** CONSTANTS ***/
		private static final long serialVersionUID = 1L;
		
		/*** CLASS DATA MEMBERS ***/
		JButton	 	bOk;		// Just a standard OK button
		
		public AboutDlg(Container parent) {
			// Create a panel for the text
			JPanel panel = new JPanel();
			
			// Create the labels for the About information
			String msg = "<html><h2>" + ConfigMgr.getAppName() + "</h2><hr/>";
			msg += "<strong>Version:</strong> " + ConfigMgr.getVersion() + "<br/>";
			msg += "<strong>Author:</strong> Chris Workman<br/>";
			msg += "Copyright 2011";
			JLabel text = new JLabel(msg);
			
			// Add the labels to the panel
			panel.add(text);
			
			// Add panel to window
			this.getContentPane().add(panel);
			
			// Create a panel for the buttons
			JPanel buttons = new JPanel();
			buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
			
			// Create the button
			bOk = new JButton("OK");
			bOk.addActionListener(this);
			
			// Add the button to the pane
			buttons.add(Box.createHorizontalGlue());
			buttons.add(bOk);
			
			// Add button panel to window
			this.getContentPane().add(buttons, BorderLayout.SOUTH);
			 
			// Set Dialog properties
			this.setResizable(false);
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.setTitle("About " + ConfigMgr.getAppName());
			this.setModal(true);
			
			// Pack the window and display
			this.setLocationRelativeTo(parent);
			this.pack();
		} // end AboutDlg

		@Override
		public void actionPerformed(ActionEvent e) {
			// If the OK button was pushed Dispose of the window
			if (e.getSource() == bOk) {
				this.setVisible(false);
				this.dispose();
			}
		} // end actionPerformed
		
	} // end class AboutDlg	
	
	/***
	 * Class for creating and managing the Configuration Settings dialog window.
	 * 
	 * @author Chris Workman
	 *
	 */
	class ConfigDlg extends JDialog implements ActionListener {
		/*** CONSTANTS ***/
		private static final long serialVersionUID = 1L;
		
		// Window components
		private JTextField		user;
		private JPasswordField	pass;
		private JTextField		defaultPath;
		private JButton			bBrowse;
		private JCheckBox		autoUpdate;
		private JButton			bOk;
		private JButton			bCancel;
		
		// Handle returning from this dialog
		private int 			retValue		=	ConfigDlg.NO_SELECTION;		// Defaults to nothing selected
		
		// Return values
		public static final int	NO_SELECTION	=	-1;					// nothing was selected
		public static final int	OK				=	0;					// OK button clicked
		public static final int CANCEL			=	1;					// Cancel button clicked
		
		public ConfigDlg(Container parent) {
			// Set window properties
			this.setTitle("Preferences");
			this.setLocationRelativeTo(parent);
			this.setModal(true);
			
			// Constructing dialog from bottom up.
			// Start with the components that contain data
			// and related labels
			// Username Components
			JLabel userLabel = new JLabel("AniDB Username");
			user = new JTextField(20);
			user.setText(ConfigMgr.getUser());
			
			// password components
			JLabel passLabel = new JLabel("AniDB Password");
			pass = new JPasswordField(20);
			pass.setText(ConfigMgr.getPass());
			
			// Default Path
			JLabel defaultLabel = new JLabel("Base Video Folder");
			defaultPath = new JTextField(30);
			bBrowse = new JButton("Browse");
			defaultPath.setText(ConfigMgr.getDefaultPath());
			
			// Auto Update
			autoUpdate = new JCheckBox(("Automatically Update AniDB XML File"), ConfigMgr.isAutoUpdate());
			
			// Add labels and input fields into panels to pair them
			// User name panel
			JPanel userPanel = new JPanel();
			userPanel.add(userLabel);
			userPanel.add(user);
			
			// password panel
			JPanel passPanel = new JPanel();
			passPanel.add(passLabel);
			passPanel.add(pass);
			
			// Default panel
			// Panel for input items
			JPanel defaultInput = new JPanel();
			defaultInput.add(defaultPath);
			defaultInput.add(bBrowse);
			
			// Create the bordered and titled panels for parts of the config window
			// Login information
			JPanel loginPanel = new JPanel();
			loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
			loginPanel.setBorder(BorderFactory.createTitledBorder("AniDB Login Information"));
			loginPanel.add(userPanel);
			loginPanel.add(passPanel);
			
			// Path settings
			JPanel pathPanel = new JPanel();
			pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
			pathPanel.setBorder(BorderFactory.createTitledBorder("Default Path Locations"));
			pathPanel.add(defaultLabel);
			pathPanel.add(defaultInput);
			
			// Auto Update
			JPanel updatePanel = new JPanel();
			updatePanel.setBorder(BorderFactory.createTitledBorder("XML File Updating"));
			updatePanel.add(autoUpdate);
			
			// Add all these components to a config settings panel
			JPanel settingsPanel = new JPanel();
			settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
			settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			settingsPanel.add(loginPanel);
			settingsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			settingsPanel.add(pathPanel);
			settingsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			settingsPanel.add(updatePanel);
			
			// Create the OK and cancel buttons
			bOk = new JButton("OK");
			bOk.addActionListener(this);
			bCancel = new JButton("Cancel");
			bCancel.addActionListener(this);
			
			// Create Panel for OK and Cancel buttons
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			buttonPanel.add(Box.createHorizontalGlue());
			buttonPanel.add(bOk);
			buttonPanel.add(Box.createRigidArea(new Dimension(5,0)));
			buttonPanel.add(bCancel);
			
			// Add panels to the window
			this.getContentPane().add(settingsPanel);
			this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			
			// Pack window to size properly
			this.pack();
			
		} // end ConfigDlg

		@Override
		public void actionPerformed(ActionEvent e) {
			// Check which button was clicked
			if (e.getSource() == bOk) {
				// Update config settings if OK was clicked
				ConfigMgr.setUser(user.getText());
				ConfigMgr.setPass(pass.getPassword().toString());
				ConfigMgr.setAutoUpdate(autoUpdate.isSelected());
				boolean check = ConfigMgr.setDefaultPath(defaultPath.getText());
				if (check) {
					retValue = ConfigDlg.OK;
					dispose();
				} // end valid folder
				else {
					JOptionPane.showMessageDialog(this, "The path entered is invalid.", "Invalid Video Folder", JOptionPane.ERROR_MESSAGE);
				} // end invalid folder
			} // end bOk
			if (e.getSource() == bCancel) {
				retValue = ConfigDlg.CANCEL;
				dispose();
			} // end cancel
		} // end ActionPerformed
		
		public int showDialog()	{
			setVisible(true);
			
			return retValue;
		} // end show Dialog
		
	} // end class ConfigDlg
	
} // end class MainWindow
