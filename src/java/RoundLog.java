/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	RoundLog.java - Game recording class
	
*******************************************************************************/


import java.io.*;
import java.text.SimpleDateFormat;


/**
 * Class for logging the states of the game step by step.
 * 
 * @author Peter Eredics
 */
public class RoundLog {
	/** The model of the application to log from */
	private GameModel model;
	
	/** The debug text file object */
	private BufferedWriter logFile;
	
	/** Is the bin logging enabled? */
	public boolean enabled = false;
	
	/** Is the log already prepared? Are headers already written? */
	private boolean prepared = false;
	
	
	/**
	 * Open the log file for writing.
	 * @param model 		The game model to extract state from
	 * @param config		The configuration object holding the logfile name
	 */
    public RoundLog(GameModel model, GameConfig config) {
    	// Init local references
    	this.model = model;
    	
    	// Write logfile header
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	try {
			logFile = new BufferedWriter(new FileWriter(config.getString("StepLogFile")));
			logFile.append("# AgentGame 2.00 replay log");
			logFile.append("\n# Recorded at: "+formatter.format(new java.util.Date()));
			logFile.append("\n# Recorded on: "+java.net.InetAddress.getLocalHost().getHostName());
			
			logFile.append("\n\n# Map width\n");
			logFile.append(String.valueOf(model.getMapWidth())+"\n");
			logFile.append("\n# Map height\n");
			logFile.append(String.valueOf(model.getMapHeight())+"\n");
			logFile.append("\n# Agent viewdistance\n");
			logFile.append(String.valueOf(model.getViewDistance())+"\n");
			logFile.append("\n# Team starting energy\n");
			logFile.append(String.valueOf(model.getTeamStartEnergy())+"\n");
			logFile.append("\n# Maximum food value\n");
			logFile.append(String.valueOf(model.getMaxFoodValue())+"\n");
			logFile.append("\n# Number of water zones\n");
			logFile.append(String.valueOf(model.watersI.size())+"\n");
			
			logFile.append("\n# Water zones\n");
			Water water;
			for (int i=0; i<model.watersI.size();i++) {
				water = model.watersI.get(i);
				logFile.append(water.position.x+" "+water.position.y+" "+water.height+" "+water.width+"\n");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}    	
    }
    
    /**
     * Prepare log file by writing header information about teams, agents and food
     * objects.
     */
    private void prepareLog() {
    	try {
    		// Number of objects
    		logFile.append("\n# Number of teams\n");
    		logFile.append(model.teamsI.size()+"\n");
    		logFile.append("\n# Number of agents\n");
    		logFile.append(model.agentsI.size()+"\n");
    		logFile.append("\n# Number of food\n");
    		logFile.append(model.foodsI.size()+"\n");
    		
    		// Team names
    		logFile.append("\n# List of teams\n");
    		for (int tId=0;tId<model.teamsI.size();tId++) 
	    		logFile.append(model.teamsI.get(tId).name+"\n");
    		
    		// Agent ID + name + label
    		logFile.append("\n# List of team members\n");
    		for (int aId=0;aId<model.agentsI.size();aId++) 
	    		logFile.append(model.agentsI.get(aId).team.id+" "+model.agentsI.get(aId).name+" "+model.agentsI.get(aId).label+"\n");
    		
    		logFile.append("\n# Game history\n");
	    } catch (IOException e) {e.printStackTrace();}
    	prepared = true;
    }
    
    
    /**
     * Log the acutal state of the game into file.
     */
    public void logRound() {
    	// Write headers if necessary
    	if (!prepared) prepareLog();
    	
    	try { 
    		logFile.append("# "+model.round+"\n");
    		// States of agents
	    	for (int aId=0;aId<model.agentsI.size();aId++) {
	    		Agent agent = model.agentsI.get(aId);
	    		logFile.append(agent.position.x+";"+agent.position.y+";"+agent.direction+";"+agent.getEnergy()+"\n");
	    	}
	
	    	// States of food
	    	for (int fId=0;fId<model.foodsI.size();fId++) {
	    		Food food = model.foodsI.get(fId);
	    		logFile.append(food.position.x+";"+food.position.y+";"+food.value+"\n");
	    	}
    	} catch (IOException e) {e.printStackTrace();}
    }
    
    
    /**
     * Close the log file.
     */
    public void closeLog(){
    	try {
    		logFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
