/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	Agent.java - agent class
	
*******************************************************************************/


import java.awt.Color;


/** 
 * The Agent class hold all information about agents in the system and is 
 * responsible for the coloring and energy management of the agents.  
 *
 * @author Peter Eredics
 */
public class Agent{
	/** The numerical agent identifier. */
	public int id;
	
	/** The string agent identifier used by Jason. */
	public String name;
	
	/** The string agent identifier set by the agent itself. */
	public String label;
	
	/** The actual position of the agent. */
	public Position position;
	
	/** The actual orientation of the agent. */
	public int direction;
	
	/** The actual energy level of the agent. */
	private int energy;
	
	/** The team of the agent. */
	public Team team;
	   
    /** The calculated color of the agent based on the inTeamId value. */
    public Color color;

    /** Indicate if the agent manually changed it's color */
    public boolean colorOverriden = false;    
    
    /** The model of the world */
    private ModelInterface model;
    
    /** The perception of the agent who attacked the agent last */
    public String lastAttackedBy = "[]";
    
	/** The agent having the highest energy level */
    static Agent maxEnergyAgent = null;
    
    /** The original color of the agent (is former when the agent is highlighted) */
    private Color formerColor;
    
    /** Whether the agent is highlighted on the GUI */
    private boolean highlighted;
    
    /** Total execution time of the agent */
    public long executionTime = 0;
    
    /** ID of the agent in the team*/
    public int inTeamId = 0;
    
    /** Last external action executed by the agent */
    public String lastAction = "N/A"; 
    
    
	/**
	 * Constructor of the Agent object, setting all variables. 
	 * @param id				The numerical agent identifier.
	 * @param name				The string agent identifier used by Jason.
	 * @param position			The actual position of the agent.
	 * @param direction			The actual orientation of the agent.
	 * @param energy			The actual energy level of the agent.
	 * @param team				The team of the agent.
	 * @param model				The model of the game world.
	 */
	public Agent(int id, String name, Position position, int direction, int energy, Team team, ModelInterface model) {
		// Set local variables
		this.id = id;
		this.name = name;
		this.position = position;
		this.direction = direction;
		this.energy = energy;
		this.team = team;
		this.label = String.valueOf(id);
		this.model = model;

		// Join team (and automatically calculate color)
        team.join(this);
        refreshColor();

        // Update maxEnergyAgent if this is the strongest agent reference if necessary
        if ((Agent.maxEnergyAgent==null)||(energy>Agent.maxEnergyAgent.energy)) Agent.maxEnergyAgent = this;
	}

	
	/**
	 * Select color manually for the agent and protect it from future
	 * automatic updates. 
	 * @param r			Red channel value.
	 * @param g			Green channel value.
	 * @param b			Blue channel value.
	 */
	public void forceColor(int r, int g, int b) {
		// Set selected color
		color = new Color(r,g,b);
		// Disable automatic coloring
		colorOverriden = true;
	}

	
	/**
	 * Update agent color based on its idInTeam value and the number of 
	 * other agents in the team.
	 */
    public void refreshColor() {
    	// No change if the color was selected manually
    	if (colorOverriden) return;
    	
    	// Linearly split the color scale    	
        float factor = (float)inTeamId/((float)team.members.size());
        color = new Color(
                (int)((float)team.color.getRed()*factor),
                (int)((float)team.color.getGreen()*factor),
                (int)((float)team.color.getBlue()*factor));
    }

    
    /**
     * Check if the agent has enough energy for an action.
     * @param actionEnergy		The energy needed for the action
     * @return					Return true if the agent has enough energy
     */
    public boolean hasEnergy(int actionEnergy) {
        return (energy>=actionEnergy);
    }

    
    /**
     * Decrease the agent's energy based on the parameter and update the 
     * strongest agent reference if necessary.
     * @param actionEnergy		The energy needed for the action
     */
    public void burnEnergy(int actionEnergy) {
    	// Decrease energy level if there is enough for the action
        if (!hasEnergy(actionEnergy)) return;
        energy -= actionEnergy;

        // If the strongest agent looses energy search others for a stronger one
        if (this == Agent.maxEnergyAgent) 
        	model.updateStrongestAgent();
    }

    
    /**
     * Increase agent energy level and update the strongest agent if necessary.
     * @param newEnergy		The amount of energy to increase with.
     */
    public void gainEnergy(int newEnergy) {
        energy += newEnergy;
        if (energy>maxEnergyAgent.energy) maxEnergyAgent = this;
    }

    
    /**
     * Get the energy level of the agent.
     * @return				The energy level of the agent.
     */
    public int getEnergy() {
        return energy;
    }
    
    
    /**
     * Set the energy level of the agent - used when new agents join the game or are under attack
     * @param newEnergy		The energy level of the agent.
     */
    public void setEnergy(int newEnergy) {
    	int oldEnergy = energy;
    	energy = newEnergy;
    	
        // If the strongest agent looses energy search others for a stronger one
        if ((newEnergy<oldEnergy)&&(this == Agent.maxEnergyAgent)) 
            model.updateStrongestAgent();
        
        // If a weaker agent gains energy check it to be the strongest
        if ((newEnergy>oldEnergy)&&(newEnergy>Agent.maxEnergyAgent.getEnergy())) Agent.maxEnergyAgent = this; 
    }
    
    
    /**
     * Turn on/off agent highlighting on the GUI
     */
    public void toggleHighlighting() {
    	if (highlighted) {
    		// Turn OFF highlighting and restore original color
    		color = formerColor;
    		highlighted = false;
    	}else{
    		// Remember original color and turn ON highlighting
    		formerColor = color;
    		color = new Color(0,0,(int)(255*((float)inTeamId/((float)team.members.size()))));
    		highlighted = true;
    	}
    }
    
    
    /**
     * Increase the total execution time of the agent and it's team
     * @param executionTime		actual execution time
     */
    public void addExecutionTime(long executionTime) {
    	this.executionTime += executionTime;
    	team.addExecutionTime(executionTime);
    }
    
    
    /**
     * Return the total execution time of the agent
     * @return					the total execution time
     */
    public long getExecutionTime() { return executionTime; };
    
    
    /**
     * Select the id of the agent in the team
     * @param inTeamId			the id of the agent
     */
    public void setInTeamId(int inTeamId) {
    	this.inTeamId = inTeamId;
    }
    
    /**
     * Save the last action of the agent for debug purposes
     * @param action			the last action's description in text format
     */
    public void storeLastAction(String action) {
    	lastAction = action;
    }
    
    
    /**
     * Return the last successfull action of the agent
     * @return					the description of the last action
     */
    public String getLastAction() {
    	return lastAction;
    }
}