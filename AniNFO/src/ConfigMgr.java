import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/***
 * ConfigMgr - This class contains configuration parameters, program wide defines
 * and general setting information and controls.
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
public class ConfigMgr {

	/*** CONSTANTS ***/
	private static final String		APP_NAME		= 		"Anime NFO Creator";	// Application name
	private static final String		APP_VERSION		=		"0.01";					// Application version for string representations
	private static final String		CLIENT_NAME		=		"aninfo";				// Client name code for AniDB API
	private static final int		VERSION			=		1;						// The client version as an int for AniDB API
	private static final String		VERSION_TYPE	=		"BETA";					// Client type (Beta, Stable, Final, etc)
	
	/// Return Codes
	public static final int			SUCCESS			=		0;				// Operation was successful
	public static final int			UNREADABLE_FILE	=		-1;				// IOException occurred
	public static final int			INVALID_CONFIG	=		-2;				// Config file is either corrupt or out of date		

	/*** CLASS DATA MEMBERS ***/
	private static boolean			autoUpdate		=		true;			// Is autoupdating enabled? Defaults to true
	private static int				clientPort		=		2116;			// Client port for API communications
	private static String			user			=		"";				// AniDB Username
	private static String			pass			=		"";				// AniDB Password
	private static boolean			firstLoad		=		true;			// First time loading the application
	private static String			defaultPath		=		"";				// The default path to open file dialogs to
	
	/***
	 * Returns formatted string for the application version.
	 * Such as: 0.01 BETA
	 * @return Formatted string describing application version
	 */
	public static String getVersion() {
		String ver = APP_VERSION + " " + VERSION_TYPE;
		
		return ver;
	} // end getVersion
	
	/***
	 * Returns the App Name of the application
	 * @return Application name
	 */
	public static String getAppName() {
		return APP_NAME;
	} // end getAppName
	
	/***
	 * Returns the integer client version for authentication with
	 * the AniDB API
	 * @return Integer value for the application version number
	 */
	public static String getClientVersion() {
		return String.valueOf(VERSION);
	} // end getClientVersion
	
	/***
	 * Enable and Disable the Autoupdate feature for the
	 * AniDB XML animetitles file.
	 * @param on - If autoupdate is enabled
	 */
	public static void setAutoUpdate(boolean on) {
		autoUpdate = on;
	} // end setAutoUpdate
	
	/***
	 * Check the state of the autoupdate feature
	 * @return Boolean value representing if the feature is enabled or not
	 */
	public static boolean isAutoUpdate() {
		return autoUpdate;
	} // end isAutoUpdate
	
	/***
	 * Get the client name as used by the AniDB API.
	 * This client name must be in all lowercase and match
	 * the client name registered with AniDB
	 * @return String containing the client name
	 */
	public static String getClientName() {
		return CLIENT_NAME;
	} // end getClientName
	
	/***
	 * Get the client port number for API communications.
	 * @return Integer value for the selected client port.
	 */
	public static int getPort() {
		return clientPort;
	} // end getPort
	
	/***
	 * Set the port number used for API communications.
	 * Port numbers less than or equal to 1024 are ignored.
	 * @param port - The new port number to set
	 */
	public static void setPort(int port) {
		// Ensure the value is > 1024
		if (port > 1024)
			clientPort = port;
	} // end setPort
	
	
	public static String getUser() {
		return user;
	}
	
	public static void setUser(String nUser) {
		user = nUser;
	}
	
	public static String getPass() {
		return pass;
	}
	
	public static void setPass(String nPass) {
		pass = nPass;
	}
	
	public static void setFirstLoad(boolean on) {
		firstLoad = on;
	}
	
	public static boolean isFirstLoad() {
		return firstLoad;
	}
	
	public static boolean setDefaultPath(String path) {
		File file = new File(path);
		
		if (file.canRead() || defaultPath == "") {
			defaultPath = path;
			return true;
		}
		else {
			return false;
		}
	}
	
	public static String getDefaultPath() {
		return defaultPath;
	}
	
	public static int loadConfig() {
		int result = SUCCESS;
		
		// Check if file exists before attempting to read from it
		File file = new File("config.dat");
		
		if (file.exists()) {
			try {
				// Create file reading stream
				FileInputStream fis = new FileInputStream(file);
				ObjectInputStream in = new ObjectInputStream(fis);
				
				// Read values from the file
				autoUpdate = in.readBoolean();
				clientPort = in.readInt();
				user = in.readUTF();
				pass = in.readUTF();
				firstLoad = in.readBoolean();
				defaultPath = in.readUTF();
				
				// Close the file
				in.close();
				fis.close();
			}
			catch (FileNotFoundException e) {
				// set defaults
				autoUpdate = true;
				clientPort = 2116;
				user = "";
				pass = "";
				firstLoad = true;
				defaultPath = "";
				
				// Create a config.dat file
				saveConfig();
			} // end filenotfound
			catch (EOFException e) {
				result = INVALID_CONFIG;
			}
			catch (IOException e) {
				result = UNREADABLE_FILE;
			} // end IOException
		} // end exists
		else {
			// Set defaults
			autoUpdate = true;
			clientPort = 2116;
			user = "";
			pass = "";
			firstLoad = true;
			defaultPath = "";
			
			// Create config.dat file
			saveConfig();
		} // end else
		
		return result;
	} // end loadConfig
	
	public static int saveConfig() {
		int result = SUCCESS;
		
		// Try reading from file
		try {
			// Create the file reading stream
			File file = new File("config.dat");
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			
			// Read config values from file
			out.writeBoolean(autoUpdate);
			out.writeInt(clientPort);
			out.writeUTF(user);
			out.writeUTF(pass);
			out.writeBoolean(firstLoad);
			out.writeUTF(defaultPath);
			
			// Close the files
			out.flush();
			out.close();
			fos.close();
		} // end try
		catch (IOException e) {
			result = UNREADABLE_FILE;
		} // end IOException
		
		return result;
	} // end saveConfig
	
} // end class ConfigMgr
