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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class MainWindow extends JFrame implements ActionListener {
	/*** CONSTANT ***/
	private static final long 		serialVersionUID = 1L;
	private static final String		CACHE_FILE = "cache.dat";

	/*** CLASS DATA MEMBERS ***/
	private LinkedList<File>		files;						// List of selected files
	private LinkedList<String>		fileTitles;					// Titles of selected files
	private LinkedList<FileDetails>	fileDetails;				// Details about selected files
	private AnimeTitles				animeTitles;				// Anime title cache from AniDB download
	private SeriesList				seriesCache;				// Cache of details looked up from AniDB
	private LinkedList<String>		seriesList;					// List of individual series to create NFO files for
	
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
		
		// Initialize our file listing
		files = new LinkedList<File>();
		
		// Initialize file details and series id listings
		seriesList = new LinkedList<String>();
		fileDetails = new LinkedList<FileDetails>();
		
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
		loadCache();
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
	
	private void loadCache() {
		File cacheFile = new File(CACHE_FILE);
		
		if (cacheFile.exists()) {
			try {
				FileInputStream fis = new FileInputStream(cacheFile);
				ObjectInputStream in = new ObjectInputStream(fis);
				
				seriesCache = (SeriesList)in.readObject();
				
				in.close();
			}
			catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Unable to read from the cached data file. The file may be corrupt.\n" +
						"A new cache file will be created.", "Error - Unable to Read Cache", JOptionPane.ERROR_MESSAGE);
				cacheFile.delete();
				seriesCache = new SeriesList();
			}
			catch (ClassNotFoundException e) {
				JOptionPane.showMessageDialog(this, "There was an error when reading from the cache data file. The file may be corrupt.\n" +
						"A new cache file will be created.", "Error - Data Error in Cache File", JOptionPane.ERROR_MESSAGE);
				cacheFile.delete();
				seriesCache = new SeriesList();
			}
		}
		else {
			seriesCache = new SeriesList();
		}
	} // end loadCache()
	
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
		// Initialize new fileTitles listing
		fileTitles = new LinkedList<String>();
		
		// Get file list from File Browser
		JFileChooser ofDialog = new JFileChooser(ConfigMgr.getDefaultPath());
		ofDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		ofDialog.setMultiSelectionEnabled(true);
		int result = ofDialog.showOpenDialog(this);
		
		// Only continue if OK was selected
		if (result == JFileChooser.APPROVE_OPTION) {
			// list for storing selected files and directories to process
			File[] selectedFiles = ofDialog.getSelectedFiles();
			
			for (int i = 0; i < selectedFiles.length; i++) {
				// Check if this is a file or directory
				if (selectedFiles[i].isDirectory()) {
					// Process each file in the directory
					File[] contents = selectedFiles[i].listFiles();
					for (int n = 0; n < contents.length; n++)
						if (contents[n].isFile())
							processFile(contents[n]);
				} // End if directory
				else if (selectedFiles[i].isFile())
					processFile(selectedFiles[i]);
			} // end For
			
			// Get the AniDB titles and id from downloaded title list
			getTitles();
			// Get full details from AniDB site
			getDetails();
		} // end if result
		
		// Refresh the file listing
		infoPanel.refreshFileList();
	} // end loadFiles
	
	private void processFile(File file) {
		// To ensure existing NFO files are ignored
		String filename = file.getName().toLowerCase();
		String ext = filename.substring(filename.lastIndexOf('.'));
		
		// If it is an NFO file, ignore it
		if (ext.compareTo(".nfo") == 0) {
			return;
		}
		
		// Add the file to our file list
		files.add(file);
		// Parse the title
		parseTitle(file);
	} // end processFile
	
	private void parseTitle(File file) {
		FileDetails fDetail;
		String 	filename	= null;
		String 	series		= null;
		int 	epno		= 0;
		int 	season		= 1;
		String temp;
		boolean isMovie		= false;
		
			// Get the filename, without extension, replace . and _ with space
			filename = file.getName();
			
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
				fDetail = new FileDetails(filename, series);
			else
				fDetail = new FileDetails(filename, series, epno);

			if (season != 1) 
				fDetail.setSeason(season);
			
			// Store the path for the file
			fDetail.setPath(file.getParent());
			fileDetails.add(fDetail);
			
			// Add the title to the title list if it doesn't exist already
			if (!fileTitles.contains(series))
				fileTitles.add(series);
	} // end parseTitles
	
	private void getTitles() {
		// Iterate through the list of file series titles found
		// and associate the correct AniDB entry with each file
		for (int i = 0; i < fileTitles.size(); i ++) {
			AnimeTitles titles = animeTitles.searchTitles(fileTitles.get(i));
			
			// If there is more than one title matched provide a list to select
			// from.
			if (titles.size() > 1) {
				TitleSelectorDlg tsWnd = new TitleSelectorDlg(titles);
				AnimeTitle title = tsWnd.showDialog();
				
				if (title != null)
					linkFileDetails(fileTitles.get(i), title);
				else
					JOptionPane.showMessageDialog(this,	"No anime title was selected from the list.", 
							"Error - No Selection Made", JOptionPane.ERROR_MESSAGE);
			} // end if
			// Only 1 match was found, just use it.
			else if (titles.size() != 0) {
				linkFileDetails(fileTitles.get(i), titles.getFirst());
			} // end else
			else {
				JOptionPane.showMessageDialog(this, "There were no matching anime titles found in the database.", 
						"Error - No Match Found", JOptionPane.ERROR_MESSAGE);
			}
		} // end for
	} // end getTitles
	
	private void linkFileDetails(String title, AnimeTitle anime) {
		// Check each file for matching title, update details from the provided anime
		for (int i = 0; i < fileDetails.size(); i++) {
			FileDetails file = fileDetails.get(i);
			if (file.getSeries().compareToIgnoreCase(title) == 0) {
				file.setSeries(anime.getMainTitle());
				file.setAID(anime.getID());
			} // end if
		} // end for
		
		if (!seriesList.contains(String.valueOf(anime.getID())))
			seriesList.add(String.valueOf(anime.getID()));
	} // end linkFileDetails
	
	private void getDetails() {
		// It is possible this will take some time. Set to the wait cursor
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		// Connect to AniDB incase we need to use it
		connect();
		
		// Get series details for the individual series in the seriesList
		for (int i = 0; i < seriesList.size(); i++) {
			int aid = Integer.parseInt(seriesList.get(i));
			SeriesEntry series = seriesCache.getSeries(aid);
			
			if (series == null) {
				// Request series details from AniDB
				AniPacket response = CommMgr.sendAnime(aid);
				
				// Handle packet response
				int code = response.getCode();
				String msg;
				String eTitle;
				String reason;
				String reply = response.getReply();

				// If the response is successful
				if (code == CommMgr.ANIME_FOUND) {
					String details = reply.substring(reply.indexOf('\n')).trim();
					String[] values = details.split("\\|");
					String title = values[14];
					String plot;
					
					if (title.equals("")) {
						AnimeTitle anime = animeTitles.searchTitles(aid);
						title = anime.getMainTitle();
					}
					
					series = new SeriesEntry(title, aid);
					
					int rating = Integer.parseInt(values[4]);
					series.setRating(rating);
					
					String genre[] = values[18].split(",");
					for (int g = 0; g < genre.length; g++)
						series.addGenre(genre[g]);
					
					System.out.println("Requesting plot info");
					plot = getPlot(aid);
					series.setPlot(plot);
					
					int result = seriesCache.addSeries(series);
					
					// Verify it was added to the cache
					if (result != SeriesList.SUCCESS) {
						JOptionPane.showMessageDialog(this, "There was an error when adding the series to the cache.", 
								"Error - Unable to update cache", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				else {
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
					case CommMgr.LOGIN_REQUIRED:
						msg = "The AniNFO client is not currently logged into the AniDB server.\nPlease check your username and password in Preferences.";
						eTitle = "Login Required";
						break;
					case CommMgr.ACCESS_DENIED:
						msg = "Access to the AniDB server has been denied.\nPlease check your username and password in Preferences.";
						eTitle = "Access Denied";
						break;
					case CommMgr.INVALID_SESSION:
						msg = "The session ID is invalid.\nPlease try restarting the AniDB client.";
						eTitle = "Invalid Session";
						break;
					case CommMgr.NO_SUCH_ANIME:
						msg = "The anime series was not found in the AniDB database.\nSeries ID: " + aid;
						eTitle = "Invalid Session";
						break;
					default:
						msg = "An unknown error has occurred.\nCode: " + code + "\nReply: " + reply;
						eTitle = "Unknown Error";
						break;
					} // end switch
					
					JOptionPane.showMessageDialog(this, msg, "Error - " + eTitle, JOptionPane.ERROR_MESSAGE);
					return;		// End execution due to error
				} // end else (error)
			} // end if (series==null)
		} // end for (seriesList)
		
		// Setup empty list of files to get from online database
		LinkedList<FileDetails> checkDB = new LinkedList<FileDetails>();
		
		// process each title in the file list
		for (int i = 0; i < fileDetails.size(); i++) {
			// Get the current fileDetails and check cache for series info
			FileDetails file = fileDetails.get(i);
			EpisodeEntry ep = seriesCache.getEpisode(file.getAID(), file.getEpno());
			
			// If the episode was not already cached then
			// add it to the list to get from the database
			if (ep == null) {
				checkDB.add(file);
			} // end if
		} // end for
		
		// Process unfound episodes
		for (int i = 0; i < checkDB.size(); i++) {
			FileDetails file = checkDB.get(i);
			int aid = file.getAID();
			int epno = file.getEpno();
			
			// Enforce packet delay
			if (i > 0) {
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException e) {
					System.err.println("Thread was interrupted... this shouldn't happen though..");
				}
			}
			
			AniPacket response = CommMgr.sendEpisode(aid, epno);
			int code = response.getCode();
			String reply = response.getReply();
			
			// If the response is successful
			if (code == CommMgr.EPISODE_FOUND) {
				String details = reply.substring(reply.indexOf('\n')).trim();
				String[] values = details.split("\\|");
				
				int eid = Integer.parseInt(values[0]);
				int length = Integer.parseInt(values[2]);
				String eps = values[5];
				String title = values[6];
				int aired = Integer.parseInt(values[9]);
				
				EpisodeEntry episode = new EpisodeEntry(eid);
				episode.setEpno(eps);
				episode.setTitle(title);
				episode.setLength(length);
				episode.setAired((long)aired * 1000);
				
				SeriesEntry series = seriesCache.getSeries(aid);
				series.getEpisodes().addEpisode(episode);
			}
			else {
				String msg;
				String eTitle;
				String reason; 
				
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
				case CommMgr.LOGIN_REQUIRED:
					msg = "The AniNFO client is not currently logged into the AniDB server.\nPlease check your username and password in Preferences.";
					eTitle = "Login Required";
					break;
				case CommMgr.ACCESS_DENIED:
					msg = "Access to the AniDB server has been denied.\nPlease check your username and password in Preferences.";
					eTitle = "Access Denied";
					break;
				case CommMgr.INVALID_SESSION:
					msg = "The session ID is invalid.\nPlease try restarting the AniDB client.";
					eTitle = "Invalid Session";
					break;
				case CommMgr.NO_SUCH_EPISODE:
					msg = "The anime episode was not found in the AniDB database.\nSeries ID: " + aid + " Episode: " + epno;
					eTitle = "Invalid Session";
					break;
				default:
					msg = "An unknown error has occurred.\nCode: " + code + "\nReply: " + reply;
					eTitle = "Unknown Error";
					break;
				} // end switch
				
				// Display error
				JOptionPane.showMessageDialog(this, msg, eTitle, JOptionPane.ERROR_MESSAGE);
				return;		// End execution because there was an error
			} // end else
		} // end for
		
		// Return the cursor to normal
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		
		// Disconnect from AniDB
		CommMgr.sendLogout();
	} // end getDetails
	
	private String getPlot(int aid) {
		String result = "";
		
		AniPacket packet;
		int code;
		String reply;
		int part = 0;
		int count = 0;
		String piece;
		
		
		System.out.println("Part: " + part + "; Count: " + count);
		// Get all parts of the plot
		do {
			System.out.println("Executing loop...");
			// Sleep for 2s to prevent flooding ban
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {
				// do nothing
			}
			
			packet = CommMgr.sendAnimeDesc(aid, part);
			code = packet.getCode();
			reply = packet.getReply();
			
			if (!packet.isError()) {
				part++;			// Increment part counter
				reply = reply.substring(reply.indexOf('\n'));
				String[] values = reply.split("\\|");
				
				count = Integer.parseInt(values[1]);
				piece = values[2];
				
				// If this is not the last part, remove <cut> from end
				// of the string
				if (count > part) {
					result += piece.substring(0, piece.lastIndexOf(' '));
				}
				else {
					result += piece;
				} // end if count
			} // end if !error
			else {
				String msg;
				String eTitle;
				String reason; 
				
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
				case CommMgr.LOGIN_REQUIRED:
					msg = "The AniNFO client is not currently logged into the AniDB server.\nPlease check your username and password in Preferences.";
					eTitle = "Login Required";
					break;
				case CommMgr.ACCESS_DENIED:
					msg = "Access to the AniDB server has been denied.\nPlease check your username and password in Preferences.";
					eTitle = "Access Denied";
					break;
				case CommMgr.INVALID_SESSION:
					msg = "The session ID is invalid.\nPlease try restarting the AniDB client.";
					eTitle = "Invalid Session";
					break;
				case CommMgr.NO_SUCH_ANIME:
					msg = "The anime series was not found in the AniDB database.\nSeries ID: " + aid;
					eTitle = "Invalid Session";
					break;
				case CommMgr.NO_SUCH_DESC:
					msg = "The requested description part could not be found.\nSeries ID: " + aid + " Part: " +  part;
					eTitle = "Invalid Session";
					break;
				default:
					msg = "An unknown error has occurred.\nCode: " + code + "\nReply: " + reply;
					eTitle = "Unknown Error";
					break;
				} // end switch
				
				// Display error
				JOptionPane.showMessageDialog(this, msg, eTitle, JOptionPane.ERROR_MESSAGE);
				return "";		// End execution because there was an error
			} // end else
			
		} while (part < count); // end while
		
		return result;
	} // end getPlot
	
	@Override
	public void dispose() {
		// Write the cache file to disk to be loaded later
		File file = new File(CACHE_FILE);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(seriesCache);
			out.flush();
			out.close();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this,	"Unable to update the cache file.\nYour cache file may be corrupt and is being deleted.",
					"Error - Unable to Update Cache", JOptionPane.ERROR_MESSAGE);
			file.delete();
		}
		
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
			String text;			// The text to display
			// File information
			text = "--FILE DETAILS--\n";
			text += "Filename: " + file.getName() + "\n"; 
			text += "Path: " + file.getPath() + "\n";
			
			// Series Information
			SeriesEntry series = seriesCache.getSeries(file.getAID());
			text += "\n--SERIES DETAILS--\n";
			text += "Series ID: " + series.getAID() + "\n";
			text += "Series Title: " + series.getTitle() + "\n";
			text += "Series Rating: " + series.getRating() + "\n";
			text += "Series Genre: " + series.getGenre() + "\n";
			text += "Series Plot:\n" + series.getPlot() + "\n";
			
			// Episode Information
			EpisodeEntry episode = series.getEpisodes().getEpisode(file.getEpno());
			text += "\n--EPISODE DETAILS--\n";
			text += "Episode ID: " + episode.getEID() +"\n";
			text += "Season " + episode.getSeason() + " Episode " + episode.getEpno() + "\n";
			text += "Episode Title: " + episode.getTitle() + "\n";
			text += "Episode Length: " + episode.getLength() + "m\n";
			
			// Displaying the date requires some formatting
			SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(episode.getAired());
			String aired = sdf.format(cal.getTime());
			text += "Episode Air Date: " + aired + "\n";
			
			seriesText.setText(text);
			seriesText.repaint();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getSource() == fileList) {
				FileDetails file = fileList.getSelectedValue();
				setDetails(file);
			} // end if
		}
		
	} // end class InfoPanel
	
	class TitleSelectorDlg extends JDialog implements ActionListener {
		
		/*** CONSTANTS ***/
		private static final long serialVersionUID = 1L;
		
		/*** CLASS DATA MEMBERS ***/
		// Window elements
		private JButton				bOk;				// OK Button
		private JButton				bCancel;			// Cancel button
		private JList<AnimeTitle> 	listTitles;			// List of tiltes
		
		// Data elements
		AnimeTitles	titleList;			// List of titles
		AnimeTitle	selectedTitle;		// The selected anime title
		
		// Constructor
		public TitleSelectorDlg(AnimeTitles newList) {
			// Set the window title
			this.setTitle("Anime Title Selection");
			
			// Set the title list
			titleList = newList;
			
			// Populate the dialog box
			// Create and populate the list model
			DefaultListModel<AnimeTitle> model = new DefaultListModel<AnimeTitle>();
			for (int i = 0; i < titleList.size(); i++)
				model.add(i, titleList.get(i));
			
			// Start with the list box and a panel to contain it
			JPanel list = new JPanel();
			list.setBorder(BorderFactory.createTitledBorder("Possible Title Matches"));
			listTitles = new JList<AnimeTitle>(model);
			listTitles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listTitles.setLayoutOrientation(JList.VERTICAL);
			listTitles.setVisibleRowCount(10);
			listTitles.setFixedCellWidth(200);
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
			msg += "<bold>Version:</bold> " + ConfigMgr.getAppVersion() + "<br/>";
			msg += "<bold>Author:</bold> Chris Workman<br/>";
			msg += "Copyright (c) 2011<br/>";
			msg += "<br/>";
			msg += "This program is free software: you can redistribute it and/or modify<br/>";
			msg += "it under the terms of the GNU General Public License as published by<br/>";
			msg += "the Free Software Foundation, either version 3 of the License, or<br/>";
			msg += "(at your option) any later version.<br/>";
			msg += "<br/>";
			msg += "This program is distributed in the hope that it will be useful,<br/>";
			msg += "but WITHOUT ANY WARRANTY; without even the implied warranty of<br/>";
			msg += "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br/>";
			msg += "GNU General Public License for more details.<br/>";
			msg += "<br/>";
			msg += "You should have received a copy of the GNU General Public License<br/>";
			msg += "along with this program.  If not, see <a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses</a><br/>";
			JLabel text = new JLabel(msg);
			
			// Add the labels to the panel
			panel.add(text);
			panel.setBorder(BorderFactory.createEtchedBorder());
			
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
			buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
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
		private JTextField		pass;
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
			pass = new JTextField(20);
			pass.setText(ConfigMgr.getPass());
			
			// Default Path
			JLabel defaultLabel = new JLabel("Base Video Folder");
			defaultPath = new JTextField(30);
			bBrowse = new JButton("Browse");
			bBrowse.addActionListener(this);
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
			updatePanel.setBorder(BorderFactory.createTitledBorder("Anime Titles File Updating"));
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
				ConfigMgr.setPass(pass.getText().toString());
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
			if (e.getSource() == bBrowse) {
				JFileChooser fcDialog = new JFileChooser(new File(defaultPath.getText()));
				fcDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retVal = fcDialog.showOpenDialog(this);
				
				if (retVal == JFileChooser.APPROVE_OPTION)
					defaultPath.setText(fcDialog.getSelectedFile().getPath());
			}
		} // end ActionPerformed
		
		public int showDialog()	{
			setVisible(true);
			
			return retValue;
		} // end show Dialog
		
	} // end class ConfigDlg
	
} // end class MainWindow
