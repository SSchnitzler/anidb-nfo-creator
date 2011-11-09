import java.util.LinkedList;

/***
 * AnimeTitles - This class stores the list of Anime Titles from the animetitles.xml file
 * It contains methods for easy searching of a title to obtain possible matches
 * and their AID numbers.
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
public class AnimeTitles {
	/*** CLASS DATA MEMBERS ***/
	private LinkedList<AnimeTitle>	titleList;			// List of AnimeTitle entries
	private int						titleCount;			// counter of titles added
	
	public AnimeTitles() {
		titleList = new LinkedList<AnimeTitle>();
	} // end AnimeTitles
	
	public void add(AnimeTitle newTitle) {
		titleList.add(newTitle);
		titleCount++;
	} // end add
	
	public AnimeTitles searchTitles(String title) {
		AnimeTitles results = new AnimeTitles();	// Results found
		AnimeTitle temp;							// temp title element for loop
		
		// Loop through the list of titles, if title matches string
		// add to the result list
		for (int i = 0; i < titleCount; i++) {
			temp = titleList.get(i);
			if (temp.hasTitle(title)) {
				results.add(temp);
			} // end if
		} // end for
		
		// Return the results
		return results;
	} // end searchTitles

	public int size() {
		return titleCount;
	} // end length
	
	// Get the i'th entry in the titleList
	public AnimeTitle get(int i) {
		return titleList.get(i);
	}
	
	// Get the first entry
	public AnimeTitle getFirst() {
		return titleList.getFirst();
	}
	
	// The title list as an array
	public AnimeTitle[] toArray() {
		return titleList.toArray(new AnimeTitle[0]);
	}
} // end class AnimeTitles
