import java.util.LinkedList;

/***
 * AnimeTitle - Class to store data on an individual Anime Title from the animetitles.xml file
 * This is various listings of titles, the official title and the AID to make
 * it easier to determine the correct title to look up from the AniDB API
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
public class AnimeTitle {
	/*** CLASS DATA MEMBERS ***/
	private int						aID;			// Anime ID number
	private String 					title;			// Official anime title
	private LinkedList<String>		officialTitle;	// Linked list of Main Titles
	private int						otCount;		// Count of main title entries
	private LinkedList<String>		shortTitle;		// Linked list of short titles
	private int						stCount;		// Count of short title entries
	private boolean					movie;			// Identify if this is a movie or series
	
	public AnimeTitle(int newID) {
		aID = newID;
		title = null;
		officialTitle = new LinkedList<String>();
		shortTitle = new LinkedList<String>();
	} // end AnimeTitle
	
	public void setMainTitle(String newTitle) {
		title = newTitle;
	} // end setMainTitle
	
	public String getMainTitle() {
		return title;
	} // end toString
	
	public void addOfficialTitle(String newTitle) {
		officialTitle.add(newTitle);
		otCount++;
	} // end addMainTitle
	
	public void addShortTitle(String newTitle) {
		shortTitle.add(newTitle);
		stCount++;
	} // end addShortTitle
	
	public int getID() {
		return aID;
	} // end getID
	
	public void setMovie(boolean on) {
		movie = on;
	}
	
	public boolean isMovie() {
		return movie;
	}
	
	public boolean hasTitle(String query) {
		//AbstractStringMetric metric = new QGramsDistance();		// Similarity testing
		String name;											// String for current name being tested
		//float similarity;										// Current similarity results
		
		// Ensure query is lower case
		query = query.toLowerCase();
		
		// check the official title first
		name = title.toLowerCase();
		//similarity = metric.getSimilarity(name, query);
		
		if (name.contains(query))
			return true;
		
		// Test against main titles
		for (int i = 0; i < otCount; i++) {
			name = officialTitle.get(i).toLowerCase();
			//similarity = metric.getSimilarity(name, query);
			if (name.contains(query))
				return true;
		} // end for
		
		// Test against short titles
		for (int i = 0; i < stCount; i++) {
			name = shortTitle.get(i).toLowerCase();
			//similarity = metric.getSimilarity(name, query);
			if (name.contains(query))
				return true;
		} // end for
		
		// title was not found
		return false;
	} // end hasTitle
	
	public int shortTitleCount() {
		return stCount;
	}
	
	public int officialTitleCount() {
		return otCount;
	}
	
	@Override
	public String toString() {
		return title;
	} // end toString
	
} // end class AnimeTitle
