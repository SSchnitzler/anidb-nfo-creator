import java.io.Serializable;
import java.util.LinkedList;

/***
 * SeriesList - Used for storing and managing a list of known series information.
 * This list will be serializable to be stored in a data cache file to save time doing lookups
 * when adding new episodes, files or if it is necessary to rebuild the NFO files.
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
public class SeriesList implements Serializable {

	

	/*** CONSTANTS ***/
	private static final long 	serialVersionUID = -8396836568300617265L;	// Serial ID
	public static final int		SUCCESS = 0;								// Successful return code	
	public static final int		NO_AID = -1;								// Attempted to add series with -1 AID
	public static final int		SERIES_NOT_FOUND = -2;						// Return this error if the series was not found
	public static final int  	EPISODE_NOT_FOUND = -3;						// REturn this error if the episode was not found

	/*** CLASS DATA MEMBERS ***/
	private int								seriesCount;	// Count of series entries
	private LinkedList<SeriesEntry>			series;			// List of series
	
	public SeriesList() {
		series = new LinkedList<SeriesEntry>();
		seriesCount = 0;
	}
	
	public SeriesEntry getSeries(int sAid) {
		SeriesEntry result = null;
		
		// Check each series for aid
		for (int i = 0; i < seriesCount; i++) {
			if (series.get(i).getAID() == sAid)
				result = series.get(i);
		} // end for
		
		return result;
	}
	
	public int addSeries(SeriesEntry nSeries) {
		int aid = nSeries.getAID();

		// Make sure series has a valid aid
		if (aid == -1) {
			return NO_AID;
		}
		
		// Only add if the series hasn't already been added
		if (getSeries(aid) == null) {
			series.add(nSeries);
			seriesCount++;
		} // end if
		
		return SUCCESS;
	}

	public EpisodeEntry getEpisode(int sAid, int epno) {
		SeriesEntry temp = getSeries(sAid);
		if (temp == null)
			return null;
		
		EpisodeList epList = temp.getEpisodes();
		if (epList == null)
			return null;
		
		EpisodeEntry episode = epList.getEpisode(epno);
		if (episode == null)
			return null;
		
		return episode;
	}
} // end class SeriesList