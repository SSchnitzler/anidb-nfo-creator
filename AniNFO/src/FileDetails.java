/***
 * FileDetails - Class containing details on loaded files such as parsed anime title and episode number
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
public class FileDetails {
	/*** CLASS DATA MEMBERS ***/
	private String		filename;				// The filename without the extension
	private int			aID;					// The anime series ID from AniDB linked to this file
	private String		series;					// Anime series name linked to this file
	private int			epno;					// The episode number linked to this file
	private int			season		= 1;		// The season number, defaults to 1
	private boolean		movie		= false;	// Is this a movie? Defaults to no

	public FileDetails(String name, String title, int num) {
		filename = name;
		series = title;
		epno = num;
	} // end FileDetails
	
	public FileDetails(String name, String title) {
		filename = name;
		series = title;
		movie = true;
	}
	
	public void setFilename(String name) {
		filename = name;
	} // end setFilename
	
	public String getFilename() {
		return filename;
	} // end getFilename
	
	public void setAID(int ID) {
		aID = ID;
	} // end setAID
	
	public int getAID() {
		return aID;
	} // end getAID
	
	public void setSeries(String title) {
		series = title;
	} // end setSeries
	
	public String getSeries() {
		return series;
	} // end getSeries
	
	public void setEpno(int num) {
		epno = num;
	} // end setEpno
	
	public int getEpno() {
		return epno;
	} // end getEpno
	
	public void setSeason(int num) {
		season = num;
	} // end setSeason
	
	public int getSeason() {
		return season;
	} // end getSeason
	
	public void setMovie(boolean on) {
		movie = on;
	}
	
	public boolean isMovie() {
		return movie;
	}
	
	public String toString() {
		return filename;
	}
}
