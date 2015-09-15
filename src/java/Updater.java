/*******************************************************************************

	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.

	Updater.java - search for updates for the AgentGame framework

*******************************************************************************/

import java.net.*;
import java.io.*;

import javax.swing.JOptionPane;

/**
 * Class responsible for searching updated versions of the framework on the official page of the competition.
 * 
 * @author Peter Eredics
 */
public class Updater {
	/**
	 * 	Default constructor checking for a new version displaying a message dialog if the running version is not the latest available.
	 */
	public Updater() {
		try {
			 URL url = new URL("http://agentgame.mit.bme.hu/agentgame_latest.txt");
		     URLConnection conn = url.openConnection();
		     conn.connect();
		     BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		     
		     String inputLine;
		     if ((inputLine = in.readLine()) != null) { 
		            if (inputLine.compareTo(GameEnvironment.version)>0) {
		            	GameEnvironment.upToDate = false;
		            	JOptionPane.showMessageDialog(null, "A new version ("+inputLine+") of the framework is available for download.");
		            }
		     }
		     
		     in.close();
		} catch (Exception e) {
			// System.out.println(e.toString());
		}
	}
}
