import java.io.Serializable;
import java.util.LinkedList;

/***
 * SeriesEntry - Contains data on an individual series entry. The known list of these is stored
 * in the SeriesList.
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
public class SeriesEntry implements Serializable {
	
	/*** CONSTANTS ***/
	private static final long serialVersionUID = -3161937020365620069L;			// Seriali ID
	
	/*** CLASS DATA MEMBERS ***/
	private int						aid;			// AniDB ID
	private String 					title;			// The series title
	private String					plot;			// Series plot
	private LinkedList<String>		genre;			// Series Genre List
	private int						genreCount;		// Count of genre entries
	
	public SeriesEntry(String nTitle, int nAid) {
		aid = nAid;								// Set aid
		title = nTitle;							// Set the title
		plot = null;							// null plot
		genre = new LinkedList<String>();		// empty list of genres
		genreCount = 0;							// Initialize count of genre entries
	} // end SeriesEntry(title)
	
	public int getAID() {
		return aid;
	} // end getAID
	
	public String getTitle() {
		return title;
	} // end getTitle
	
	public String getPlot() {
		return plot;
	} // end getPlot
	
	public String getGenre() {
		String result = null;
		
		// If we do not have an empty list
		// Create a comma separated string of all entries
		if (!genre.isEmpty()) {
			result = genre.getFirst();
			for (int i = 1; i < genreCount; i++)
				result = result + ", " + genre.get(i);
		} // end if
		
		// Return the genre list (null if empty
		return result;
	} // end getGenre
	
	public void setPlot(String nPlot) {
		plot = nPlot;
	} // end setPlot
	
	public void addGenre(String nGenre) {
		// Validate that genre hasn't already been added
		if (genre.indexOf(nGenre) == -1) {
			genre.add(nGenre);
			genreCount++;
		} // end if
	} // end addGenre

} // end class SeriesEntry
