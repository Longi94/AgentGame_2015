/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	CSVLog.java - CSV logger class
	
*******************************************************************************/


import java.io.*;
import java.util.Iterator;


/**
 * Class for logging the agent energy levels into Excel readable CSV file.
 * 
 * @author Peter Eredics
 */
public class CSVLog {
	/** The CSV log output file object */
	private BufferedWriter logFile;
	
	/** The number of rounds since the last logged round */
	public int logRound = 0; 
	
	/** Is the CSV logging enabled? */
	public boolean enabled = false;
	
	
	/**
	 * Open the log file for writing.
	 * @param config		The configuration object holding the logfile name
	 */
    public void initLog(GameConfig config) {
    	try {
    		enabled = true;
			logFile = new BufferedWriter(new FileWriter(config.getString("CSVLogFile")));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
       
    /**
     * Write header to the top of the CSV log 
     * @param model			The model to extract header data from
     */
    public void writeHeader(GameModel model) {
    	// At the first time write the headers 
		String header1 = ";";
		String header2 = "time;";
		
		// Process teams: extract team name and agents
        String lastTeamName = "";
        for (int tId = 0; tId<model.teamsI.size(); tId++) {
            Team aTeam = model.teamsI.get(tId);

            // Process agents, extract label
            Iterator<Agent> member=aTeam.members.iterator();
            while (member.hasNext()) {
                Agent agent = (Agent)member.next();
                if (!lastTeamName.equals(agent.team.name)) {
                	header1 += agent.team.name +";";
                	lastTeamName = agent.team.name;
                } else header1 +=";";
                header2 += agent.label +";";
            }
        }
        
        // Write column headers to the team total values columns on the right
        for (int tId = 0; tId<model.teamsI.size(); tId++) 
        	header2 += model.teamsI.get(tId).name +";";
		
        // Write to the file
		try {
			logFile.write(header1+"\n");
			logFile.write(header2+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    /**
     * Write the current state of the game to the logfile
     * @param model			The model to extract game state from
     */
    public void logState(GameModel model) {
    	// Process agent team by team
    	String line = String.valueOf(model.round-1)+";";
        for (int tId = 0; tId<model.teamsI.size(); tId++) {
            Team aTeam = model.teamsI.get(tId);
            Iterator<Agent> member=aTeam.members.iterator();
            while (member.hasNext()) {
                Agent agent = (Agent)member.next();
                line += String.valueOf(agent.getEnergy()) +";";
            }
        }
        
        // Calculate team total values
        for (int tId = 0; tId<model.teamsI.size(); tId++) 
        	line += String.valueOf(model.teamsI.get(tId).getTotalEnergy())+";";
        
        // Write all data to a line in the file
		try {
			logFile.write(line+"\n");
			logFile.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    /**
     * Close the log file when terminating the application.
     */
    public void closeLogs() {   	
    	// Close th log if it was opened
    	try {
    		if (logFile != null) logFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
