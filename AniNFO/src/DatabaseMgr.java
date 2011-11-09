import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/***
 * DatabaseMgr - This static class is used to manage the XML database file from
 * AniDB. Use it to check if the database is upto date, download
 * the current daily dump file and ungzip the dump to the
 * xml file.
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
public class DatabaseMgr {
	/*** CONSTANT ***/
	public final static int	SUCCESS 			= 	0;			// Successful return code
	public final static int INVALID_URL			=	-1;			// URL was not valid or not found
	public final static int UNREADABLE_FILE		= 	-2;			// File IO error
	public final static int FILE_NOT_FOUND		= 	-3;			// File was not found
	public final static int	PARSER_ERROR		=	-4;			// Parser Configuration Error
	public final static int	BAD_XML				= 	-5;			// Error in the XML format
	
	public final static int	BUFFER				= 	1024;		// Buffer size for file IO
	
	private static final String ANIDB = "http://anidb.net/api/animetitles.xml.gz";
	private static final String OUTDB = "animetitles.xml";

	/*** CLASS DATA MEMBERS ***/
	private static AnimeTitles	animeTitles;
	
	public static boolean isCurrent() {
		File file = new File(OUTDB);
		
		return ((System.currentTimeMillis() - file.lastModified()) < 86400000);
	}
	
	public static int getUpdatedDatabase() {
		// Try downloading the compressed database from ANIDB url 
		// then uncompress it to the XML file
		try {
			// Open an input stream from the URL provided
			URL siteURL = new URL(ANIDB);
			InputStream site = siteURL.openStream();
			BufferedInputStream in = new BufferedInputStream(site);
			
			// Open an output stream to the provided filename
			String gzfile = ANIDB.substring(ANIDB.lastIndexOf('/')+1,ANIDB.length());
			FileOutputStream fos = new FileOutputStream(gzfile);
			BufferedOutputStream out = new BufferedOutputStream(fos,BUFFER);
			
			byte[] data = new byte[BUFFER];		// Data read from the file
			int bits = 0;						// Number of bits read
			
			// Read from the file until reaching the end
			// Write the read data to the output file
			while ((bits = in.read(data,0,BUFFER)) >= 0) {
				out.write(data,0,bits);
			} // end while
			
			// Close the files
			out.close();
			in.close();
			
			// Open the compressed database file
			FileInputStream fis = new FileInputStream(gzfile);
			GZIPInputStream zin = new GZIPInputStream(fis);
								
			// Read the entries from the archive
			bits = 0;
								
			// Open file to write uncompressed database to
			FileOutputStream xml = new FileOutputStream(OUTDB);
								
			while ((bits = zin.read(data)) != -1) {
				// While there is data in the buffer, write it to the output file
				xml.write(data,0,bits);
			}

			// Close the files
			xml.close();
			zin.close();
										
			// Clean up compressed file
			File f = new File(gzfile);
			f.delete();
		} // end try
		catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			return FILE_NOT_FOUND;
		}
		catch (MalformedURLException e) {
			System.err.println(e.getMessage());
			return INVALID_URL;
		} // end catch URL
		catch (IOException e) {
			System.err.println(e.getMessage());
			return UNREADABLE_FILE;
		} // end IOE
						
		return SUCCESS;
	} // end getUpdatedDatabse
	
	public static int loadTitles() {
		animeTitles = new AnimeTitles();
		
		// Open the xml file
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			// Create the document builder and parse the file.
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document xmlfile = db.parse(OUTDB);
			
			// Get root node
			xmlfile.getDocumentElement().normalize();
			
			// Get list of aID nodes
			NodeList nodes = xmlfile.getElementsByTagName("anime");
			
			// Process each anime node
			for (int i = 0; i < nodes.getLength(); i++) {
				Element anime = (Element)nodes.item(i);

				// Get the aID and create the AnimeTitle object from it
				String aid = anime.getAttribute("aid");
				AnimeTitle animeTitle = new AnimeTitle(Integer.parseInt(aid));
				
				// Get list of titles for this anime
				NodeList titleNodes = anime.getElementsByTagName("title");
				
				for (int n = 0; n < titleNodes.getLength(); n++) {
					Element node = (Element)titleNodes.item(n);
					String type = node.getAttribute("type");
					String temp = node.getTextContent();
					
					if (type.compareToIgnoreCase("main") == 0)
						animeTitle.setMainTitle(temp);
					else if (type.compareToIgnoreCase("official") == 0)
						animeTitle.addOfficialTitle(temp);
					else if (type.compareToIgnoreCase("short") == 0)
						animeTitle.addShortTitle(temp);
				} // end for title
				
				// Add the title object to the list
				animeTitles.add(animeTitle);
			} // end for anime
		} // end try
		catch (ParserConfigurationException e) {
			return PARSER_ERROR;
		} // end ParserConfEx
		catch (IOException e) {
			return UNREADABLE_FILE;
		} // end IOException
		catch (SAXException e) {
			return BAD_XML;
		} // end SAXException
		
		return SUCCESS;
	} // end loadTitles
	
	public static AnimeTitles getTitles() {
		return animeTitles;
	} // end getTitles
} // end class DatabaseMgr
