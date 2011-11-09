/***
 * AniNFO
 * This program will be written to accept multiple file selection, parse series title
 * and episode number information from the file names and create NFO files for the series
 * and/or each episode from the AniDB.net daily dump file. It will also be able to download 
 * and read the data from the daily dump file if connected to the internet.
 * 
 * Because of how AniDB has organized it's file information long running series such as
 * Bleach, One Piece and Naruto will have all episodes under Season 1. Series with separated
 * seasons such as Shakugan no Shana, Sayonara Zetsubo Sensei and Darker than Black will
 * treat individual seasons as separate shows and will need to be stored into their own folders
 * with their own NFO files.
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
 *
 */
public class AniNFO {
	/***
	 * The main program loop, not much to see here if it's done right! ;)
	 * @param args - command line arguments.
	 */
	public static void main(String[] args) {
		// Create the main application window
		@SuppressWarnings("unused")
		MainWindow wnd = new MainWindow();
	} // end main

} // end class AniNFO
