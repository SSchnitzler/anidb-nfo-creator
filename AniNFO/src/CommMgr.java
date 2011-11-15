import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/***
 * CommMgr - Manager for communications between client and AniDB server.
 * Since communications are in UDP form this will be handled
 * as a static class, allowing quick and easy calls to AniDB
 * data without constantly instantiating a new instance.
 * 
 * This manager will also work with the DatabaseMgr in order
 * to handle caching of data. Any series and episode information
 * that is requested will be added to the database for caching.
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
public class CommMgr {
	/*** CONSTANTS ***/
	private static final String		SERVER			=	"api.anidb.net";
	private static final int		SERVER_PORT 	=	9000;
	private static final String		PROTOVER		=	"3";
	private static final int		BUF_SIZE		=	1400;
	
	/*** PACKET CODE DEFINITIONS ***/
	// Can occur in any commmand
	public static final int			ILLEGAL_INPUT_ACCESS_DENIED	=	505;
	public static final int			BANNED						=	555;
	public static final int			UNKNOWN_COMMAND				=	598;
	public static final int			INTERNAL_SERVER_ERROR		=	600;
	public static final int			OUT_OF_SERVICE				=	601;
	public static final int			SERVER_BUSY					=	602;
	
	// Can occur in any command requiring authentication
	public static final int			LOGIN_REQUIRED				= 	501;
	public static final int			ACCESS_DENIED				= 	502;
	public static final int			INVALID_SESSION				=	506;
	
	// AUTH codes
	public static final int			LOGIN_ACCEPTED				=	200;
	public static final int			LOGIN_ACCEPTED_NV			=	201;
	public static final int			LOGIN_FAILED				= 	500;
	public static final int			CLIENT_OUTDATED				=	503;
	public static final int			CLIENT_BANNED				= 	504;
	
	// LOGOUT codes
	public static final int			LOGGED_OUT					=	203;
	public static final int			NOT_LOGGED_IN				= 	403;
	
	// ANIME codes
	public static final int			ANIME_FOUND					= 	230;
	public static final int			NO_SUCH_ANIME				=	330;
	
	// EPISODE codes
	public static final int			EPISODE_FOUND				=	240;
	public static final int			NO_SUCH_EPISODE				=	340;
	
	// ANIMEDESC Codes
	public static final int			DESC_FOUND					=	233;
	public static final int			NO_SUCH_DESC				= 	333;
	
	// Client generated codes
	public static final int			CLIENT_ERROR				= 	000;
	
	/*** CLASS DATA MEMBERS ***/
	private static String			sessionID	= null;		// The session ID assigned for this session.
	private static boolean			connected	= false;	// Are we connected/authenticated?

	public static AniPacket sendPacket(String type, String msg) {
		AniPacket response;				// Response packet
		InetAddress server;
		DatagramSocket socket;
		DatagramPacket packet;
		
		// Build data buffer for packet
		String pstr = type + " " + msg + "\n";
		byte[] buf = new byte[BUF_SIZE];
		buf = pstr.getBytes();
		
		// Attempt connection to API server	
		try {
			// Get the InetAddress for the API server, create a socket bound to defined local port
			server = InetAddress.getByName(SERVER);
			socket = new DatagramSocket(ConfigMgr.getPort());
			
			System.out.println("Sending packet: " + pstr);
			// Build the packet and send it to the API server
			packet = new DatagramPacket(buf, buf.length, server, SERVER_PORT);
			socket.send(packet);
			
			// Clear the buffer
			buf = new byte[BUF_SIZE];
			
			// Receive response from the API server
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			
			// Close the socket
			socket.close();
			
			// Build AniPacket from response message
			String mess = new String(buf).trim();
			System.out.println("Received Packet: " + mess);
			int code = Integer.parseInt(mess.substring(0,mess.indexOf(" ")));
			String reply = mess.substring(mess.indexOf(" "));
			response = new AniPacket(code, reply);
		}
		catch (UnknownHostException e) {
			response = new AniPacket(000, e.getMessage());
		}
		catch (SocketException e) {
			response = new AniPacket(000, e.getMessage());
		}
		catch (IOException e) {
			response = new AniPacket(000, e.getMessage());
		}
		
		return response;
	} // end sendPacket
	
	public static AniPacket sendAuth() {
		AniPacket response;
		
		// Verify necessary data is set in config
		if (ConfigMgr.getUser() == null || ConfigMgr.getPass() == null) {
			response = new AniPacket(000, "USERNAME OR PASSWORD IS NOT SET IN PREFERENCES");
			return response;
		} // end if
		
		// Build data for AUTH packet
		String user = "user=" + ConfigMgr.getUser();
		String pass = "&pass=" + ConfigMgr.getPass();
		String prot = "&protover=" + PROTOVER;
		String client = "&client=" + ConfigMgr.getClientName();
		String clientver = "&clientver=" + ConfigMgr.getVersion();
		String msg = user+pass+prot+client+clientver;
		
		// Send AUTH packet
		response = sendPacket("AUTH", msg);
		
		// If this is not an error packet, grab the sessionID and
		// Toggle connection flag
		if (!response.isError()) {
			String[] words = response.getReply().split(" ");
			sessionID = words[1];
			connected = true;
		}
		
		// Return response packet
		return response;
	} // end sendAuth
	
	public static boolean isConnected() {
		return connected;
	}
	
	public static AniPacket sendLogout() {
		AniPacket response;
		
		// Verify we are connected and have a valid session ID
		if (!connected || sessionID == null) {
			response = new AniPacket(000, "NOT CONNECTED OR INVALID SESSION ID");
			return response;
		} // end if
		
		// Build data for LOGOUT packet
		String msg = "s=" + sessionID;
		
		// Send LOGOUT packet
		response = sendPacket("LOGOUT", msg);
		
		// Regardless of response, set as logged out
		connected = false;
		sessionID = null;
		
		// Return packet
		return response;
	} // end sendLogout
	
	public static AniPacket sendAnime(int aid) {
		AniPacket 	response;		// The response packet
		String		msg;			// The packet to send
		
		msg = "aid=" + String.valueOf(aid);
		msg += "&s=" + sessionID;
		
		response = sendPacket("ANIME", msg);
		
		return response;
	} // end sendAnime
	
	public static AniPacket sendEpisode(int aid, int epno) {
		AniPacket response;			// The response packet
		String msg;					// The packet info to send
		
		msg = "aid=" + String.valueOf(aid);
		msg += "&epno=" + String.valueOf(epno);
		msg += "&s=" + sessionID;
		
		response = sendPacket("EPISODE", msg);
		
		return response;
	} // end sendEpisode
	
	public static AniPacket sendAnimeDesc(int aid, int part) {
		AniPacket response;
		String msg;
		
		msg = "aid=" + String.valueOf(aid);
		msg += "&part=" + String.valueOf(part);
		msg += "&s=" + sessionID;
		
		response = sendPacket("ANIMEDESC", msg);
		
		return response;
	} // end sendAnimeDesc
} // end class CommMgr
