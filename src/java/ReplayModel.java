/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	Player.java - replay controller for logged games
	
*******************************************************************************/


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * The model of the player  
 * 
 * @author Peter Eredics
 */
public class ReplayModel implements ModelInterface {
	/** The width of the map */
	private int mapWidth;
	
	/** The height of the map */
	private int mapHeight;
	
	/** The viewdistance of the agents */
	private int viewDistance;
	
	/** The staring energy of the teams */
	private int teamStartEnergy;
	
	/** The number of teams */
	private int teamCount;
	
	/** The number of agents */
	private int agentCount;
	
	/** The number of food */
	private int foodCount;
	
	/** The agent register */
	private HashMap<Integer, Agent> agentsI;
	
	/** The food register */
	private HashMap<Integer, Food> foodI;
	
	/** The team register */
	private HashMap<Integer, Team> teamsI;
	
	/** Log entries read from the file */
	private ArrayList<String> gameHistory;
	
	/** The selected round of the playback */
	private int roundSelected;
	
	/** The round limit */
	public int roundMax;
	
	/** The maximal food value allowed by the game configuration */
	public int maxFoodValue;
	
	/** Water register */
	private HashMap<Integer, Water> watersI; 
	
	/** Number of water objects on the map */
	public int waterCount = 0;
	
	
	/**
	 * Constructor setting local references and reading parameters and game
	 * data from the file
	 * @param logFile			The logfile to read game from
	 */
	public ReplayModel(String logFile) {
		// Create registers
		agentsI = new HashMap<Integer,Agent>  ();
        foodI   = new HashMap<Integer,Food>   ();
        teamsI  = new HashMap<Integer,Team>   ();
        gameHistory = new ArrayList<String>   ();
        watersI = new HashMap<Integer,Water>  ();
		
        // Read file line by line
		int lineNumber = 0;
		int headerLength = 9;
		try {
		    BufferedReader in = new BufferedReader(new FileReader(logFile));
		    String line;
		    while ((line = in.readLine()) != null) {    	
		    	// Read line
		    	if ((line.length()==0)||(line.startsWith("#"))) continue;
		    	lineNumber++;
		    	
		    	if (lineNumber<=headerLength+waterCount) {
			    	// Process header data
			    	switch (lineNumber) {
			    		case 1: 
			    			mapWidth        = Integer.valueOf(line); 
			    			Player.debug("mapWidth = "+mapWidth);
			    			break;
			    		case 2: 
			    			mapHeight       = Integer.valueOf(line);
			    			Player.debug("mapHeight = "+mapHeight);
			    			break;
			    		case 3: 
			    			viewDistance    = Integer.valueOf(line); 
			    			Player.debug("viewDistance = "+viewDistance);
			    			break;
			    		case 4: 
			    			teamStartEnergy = Integer.valueOf(line);
			    			Player.debug("teamStartEnergy = "+teamStartEnergy);
			    			break;
			    		case 5: 
			    			maxFoodValue    = Integer.valueOf(line);
			    			Player.debug("maxFoodValue = "+maxFoodValue);
			    			break;	    		
			    		case 6: 
			    			waterCount = Integer.valueOf(line);
			    			Player.debug("waterCount = "+waterCount);
			    			break;
			    	}
			    	if ((lineNumber>6)&&(lineNumber<=6+waterCount)) {
			    		String[] parts = line.split(" ");
			    		Player.debug("Water "+(lineNumber-6)+": "+line);
			    		watersI.put(watersI.size(), new Water(new Position(Integer.valueOf(parts[0]),Integer.valueOf(parts[1])),Integer.valueOf(parts[2]),Integer.valueOf(parts[3])));
			    		
		    		} else if (lineNumber==6+waterCount+1) {
			    		teamCount = Integer.valueOf(line);
		    			Player.debug("teamCount = "+teamCount);
			    	} else if (lineNumber==6+waterCount+2) { 
			    		agentCount = Integer.valueOf(line);		
		    			Player.debug("agentCount = "+agentCount);
		    		} else if (lineNumber==6+waterCount+3) { 
			    		foodCount = Integer.valueOf(line); 
		    			Player.debug("foodCount = "+foodCount);
			    		for (int fId=0; fId<foodCount; fId++) 
			    			foodI.put(fId, new Food(fId,new Position(0,0),0));
			    	}	
		    	} else if (lineNumber<=headerLength+waterCount+teamCount) {
		    		// Process teams
		    		int newTeamId = teamsI.size();
		    		Team newTeam = new Team(line, newTeamId, Team.teamColors[newTeamId],this);
		    		Player.debug("Team "+teamsI.size()+": "+line);
		    		teamsI.put(newTeamId, newTeam);
		    	} else if (lineNumber<=headerLength+waterCount+teamCount+agentCount) {
		    		// Process agents
		    		int newAgentId = agentsI.size();
		    		
		    		String[] parts = line.split(" ");
		    		int teamId = Integer.valueOf(parts[0]);
		    		Agent newAgent = new Agent(newAgentId, parts[1], teamsI.get(teamId).getTeamStart(),0, teamStartEnergy, teamsI.get(teamId), this);
		    		newAgent.label = parts[2];
		    		agentsI.put(newAgentId, newAgent);
		    		Player.debug("Agent "+agentsI.size()+" in team "+teamId+" is "+parts[1]);
		    	} else {
		    		// Read gameplay data
		    		gameHistory.add(line);
		    	}
		    }
		    in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		roundMax = gameHistory.size()/(agentCount+foodCount);
	}
	
	
	/**
	 * Select round of the game to display
	 * 
	 * @param newRound			The round to select
	 * @return					True if newRound is a valid round number
	 */
	public boolean selectRound(int newRound){
		// Select the new setting if it's valid
		if (newRound>roundMax) return false;
		if (newRound<1) return false;
		roundSelected = newRound;
		
		// Read and set agent states
		int logStart = (agentCount+foodCount)*(roundSelected-1);
		for (int aId=0; aId<agentCount; aId++) {
			String[] parts = gameHistory.get(logStart+aId).split(";");
			Agent agent = agentsI.get(aId);
			agent.position  = new Position(Integer.valueOf(parts[0]),Integer.valueOf(parts[1]));
			agent.direction = Integer.valueOf(parts[2]);
			agent.setEnergy(Integer.valueOf(parts[3]));
		}
		
		// Read and set food states
		for (int fId=0; fId<foodCount; fId++) {
			String[] parts = gameHistory.get(logStart+agentCount+fId).split(";");
			Food food = foodI.get(fId);
			food.position = new Position(Integer.valueOf(parts[0]),Integer.valueOf(parts[1]));
			food.value    = Integer.valueOf(parts[2]);
		}	
		return true;
	}
	
	
	/**
	 * @see ModelInterface#getMapWidth()
	 */
	public int getMapWidth() {
		return mapWidth;
	}

	
	/**
	 * @see ModelInterface#getMapHeight()
	 */
	public int getMapHeight() {
		return mapHeight;
	}

	
	/**
	 * @see ModelInterface#getAgentsI()
	 */
	public HashMap<Integer, Agent> getAgentsI() {
		return agentsI;
	}

	
	/**
	 * @see ModelInterface#getFoodI()
	 */
	public HashMap<Integer, Food> getFoodI() {
		return foodI;
	}

	
	/**
	 * @see ModelInterface#getTeamsI()
	 */
	public HashMap<Integer, Team> getTeamsI() {
		return teamsI;
	}

	
	/**
	 * @see ModelInterface#getViewDistance()
	 */
	public int getViewDistance() {
		return viewDistance;
	}

	
	/**
	 * @see ModelInterface#getRound()
	 */
	public int getRound() {
		return roundSelected;
	}

	
	/**
	 * @see ModelInterface#getTeamStartEnergy()
	 */
	public int getTeamStartEnergy() {
		return teamStartEnergy;
	}

	
	/**
	 * @see ModelInterface#updateStrongestAgent()
	 */
	public void updateStrongestAgent() {
		// Not used in replay mode
		return;
	}

	
	/**
	 * @see ModelInterface#getRoundLimit()
	 */
	public int getRoundLimit() {
		return roundMax;
	}
	
	
	/**
	 * @see ModelInterface#getMaxFoodValue()
	 */
	public int getMaxFoodValue() {
		return maxFoodValue;
	}
	
	
	/**
	 * @see ModelInterface#getWatersI()
	 */	
	public HashMap<Integer, Water> getWatersI() {
		return watersI;
	}
}