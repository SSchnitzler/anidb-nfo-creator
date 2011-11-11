/***
 * AniPacket - Simple packet structure for managing data received from the 
 * AniDB API server. This can allow for better processing of
 * the data and error feedback.
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
public class AniPacket {
	/*** CLASS DATA MEMBERS ***/
	private boolean			error;
	private int				code;
	private String			reply;
	
	public AniPacket(int nCode, String nReply) {
		code = nCode;
		reply = nReply;
		
		switch (code) {
			case 000:
			case 330:
			case 333:
			case 340:
			case 403:
			case 500:
			case 501:
			case 502:
			case 503:
			case 504:
			case 505:
			case 506:
			case 555:
			case 598:
			case 600:
			case 601:
			case 602:
				error = true;
				break;
			default:
				error = false;
				break;
		} // end switch
	} // end AniPacket
	
	public boolean isError() {
		return error;
	} // end isError
	
	public int getCode() {
		return code;
	}
	
	public String getReply() {
		return reply;
	}
	
} // end class AniPacket
