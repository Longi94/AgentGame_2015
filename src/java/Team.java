/*******************************************************************************

	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.

	Team.java - team class

*******************************************************************************/


import java.util.ArrayList;
import java.awt.Color;


/**
 * The team class represents the team of agents belonging to the same player. The
 * team membership is decided in the GameEnvironment class when agents join the 
 * game based on their name's first 6 characters.
 * 
 * @author Peter Eredics
 */
public class Team {
    /** String identifier of the team */
    public String name;
    
    /** Numerical identifier of the team */
    public int id;
    
    /** Selected color for the team */
    public Color color;
    
    /** Members of the team */
    public ArrayList<Agent> members;
    
	/** The default colors of the teams. */
	public static Color[] teamColors = {Color.RED, Color.GREEN, Color.BLUE, Color.BLACK};
	
    /** The model of the game world */
    private ModelInterface model;
    
    /** The starting energy of team members calculated from the total team energy and member count. */
    public int memberEnergy = 0;
    
    /** The total execution time of a team*/
    private long executionTime;
    
    /** Is the team already disqualified? */
    private boolean disqualified = false;

    
    /**
     * Constructor of the team object setting all important internal variable
     * @param name			String identifier of the team
     * @param id			Numerical identifier of the team
     * @param color			Selected color for the team
     * @param model			The model of the game world
     */
    public Team(String name, int id, Color color, ModelInterface model) {
        this.name  = name;
        this.id    = id;
        this.color = color;
        this.model = model;
        this.members =  new ArrayList<Agent>();
    }

    
    /**
     * Join agent to the team: only allowed at the beginning of the game
     * @param agent			The agent joining the team
     */
    public void join(Agent agent) {
    	// Join the team
        members.add(agent);
        
        // Recalculate member energies to keep the team total starting energy at the
        // preset value read from the config file
        memberEnergy = model.getTeamStartEnergy() / members.size();
        agent.setEnergy(memberEnergy);
        for (int i=0; i<members.size()-1;i++) {
            members.get(i).refreshColor();
            members.get(i).setEnergy(memberEnergy);
        }
        agent.setInTeamId(members.size());        
    }

    
    /**
     * Calculate the total energy of team members
     * @return				The total energy of the team
     */
    public int getTotalEnergy() {  	    	
        int total = 0;
        if (disqualified) return total;
        
        for (int i=0; i<members.size(); i++) {
        	total += members.get(i).getEnergy();
        }
        return total;
    }
    
    
    /**
     * Calculate the position of the team based on the numerical identifier of the team
     * @return				The staring position of the team
     */
    public Position getTeamStart() {
    	switch (id) {
    		case 0:
    			// top left corner
    			return new Position(0,0);
    		case 1:
    			// right bottom corner
    			return new Position(model.getMapWidth()-1,model.getMapHeight()-1);
    		case 2:
    			// left bottom corner
    			return new Position(0,model.getMapHeight()-1);
    		case 3:
    			// right top corner
    			return new Position(model.getMapWidth()-1,0);
  			default:
  				// more than 3 teams are not allowed
  				return null;
    	}
    }
    
    
    /**
     * Increase the total execution time of the team when a member finishes
     * its execution cycle
     *  
     * @param executionTime		The execution time to increase total with
     */
    public void addExecutionTime(long executionTime) {
    	this.executionTime += executionTime;
    }
    
    
    /** 
     * Return the total team execution time
     * 
     * @return					The total team execution time
     */
    public long getExecutionTime() { return executionTime; };
    
    
    /**
     * Disqualify team
     */
    public void disqualify() {
    	System.out.println("Team "+name+" is disqualified.");
    	disqualified = true;
    } 
}