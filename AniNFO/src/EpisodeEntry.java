import java.io.Serializable;

/***
 * EpisodeEntry - Used to store data on individual episodes within a series.
 *   This class is serializable for easy cacheing.
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
public class EpisodeEntry implements Serializable {
	/*** CONSTANTS ***/
	private static final long serialVersionUID = -7480223043249434470L;
	
	/*** DATA MEMBERS ***/
	private int eid = -1;			// Episode ID
	private int epno;				// Episode Number (can include characters S, C, T, O to define type)
	private int season;				// Episode Season, used for special episodes in Boxee
	private int length;				// Length of the episode
	private float rating;			// Episode rating (scale 0 - 10)
	private String title;			// Episode title
	private int aired;				// Date the episode aired
	
	// Constructor
	public EpisodeEntry(int new_eid) {
		eid = new_eid;
		epno = 1;
		season = 1;
		length = 24;
		rating = 0.0f;
		title = null;
		aired = 0;
	} // end constructor
	
	// Accessor/Mutator methods
	public void setEpno(String new_epno) {
		char type = new_epno.charAt(0);
		
		// Set season to 0 for special episodes
		// and 1 for regular episodes
		switch (type) {
		case 'S':
		case 'T':
		case 'C':
		case 'P':
		case 'O':
			season = 0;
			epno = Integer.parseInt(new_epno.substring(1));
			break;
		default:
			season = 1;
			epno = Integer.parseInt(new_epno.trim());
			break;
		} // end switch
	}
	
	// added for manual setting of season
	// incase the user wants to organize episodes
	// into seasons.
	public void setSeason(int new_season) {
		season = new_season;
	}
	
	public void setLength(int new_length) {
		length = new_length;
	}
	
	public void setRating(int new_rating) {
		// Convert AniDB int based rating to
		// correct float based rating
		float temp = (new_rating/100);
		
		// Ensure that rating falls between 0 and 10
		if (temp > 10.0f)
			temp = 10.0f;
		
		if (temp < 0.0f)
			temp = 0.0f;
		
		// Set rating value
		rating = temp;
	}
	
	public void setTitle(String new_title) {
		title = new_title;
	}
	
	public void setAired(int new_date) {
		aired = new_date;
	}
	
	public int getEID() {
		return eid;
	}
	
	public int getEpno() {
		return epno;
	}
	
	public int getSeason() {
		return season;
	}
	
	public float getRating() {
		return rating;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getAired() {
		return aired;
	}
	
} // end class EpisodeEntry 
