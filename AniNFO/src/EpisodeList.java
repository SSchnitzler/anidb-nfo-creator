import java.io.Serializable;
import java.util.LinkedList;

/***
 * EpisodeList - Maintains a list of episodes for a given series.
 *   This class is serializable to be easily cached.
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
public class EpisodeList implements Serializable {

	/*** CONSTANTS ***/
	private static final long serialVersionUID = 4821124393941487674L;			// Serial ID
	public static final int			SUCCESS = 0;								// Successful return code	
	public static final int			NO_EID = -1;								// Attempted to add series with -1 AID

	/*** CLASS DATA MEMBERS ***/
	private int								episodeCount;	// Count of series entries
	private LinkedList<EpisodeEntry>		episode;			// List of series
	
	public EpisodeList() {
		episode = new LinkedList<EpisodeEntry>();
		episodeCount = 0;
	}
	
	public EpisodeEntry getEpisode(int sEpno) {
		// Check each episode for eid
		for (int i = 0; i < episodeCount; i++) {
			EpisodeEntry temp = episode.get(i);
			if (temp.getEpno() == sEpno)
				return temp;
		} // end for
		
		return null;
	}
	
	public int addEpisode(EpisodeEntry nEpisode) {
		int eid = nEpisode.getEID();

		// Make sure series has a valid aid
		if (eid == -1) {
			return NO_EID;
		}
		
		// Only add if the series hasn't already been added
		if (getEpisode(eid) == null) {
			episode.add(nEpisode);
			episodeCount++;
		} // end if
		
		return SUCCESS;
	}

} // end class SeriesList