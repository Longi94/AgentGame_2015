/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	ModelInterface.java - common interface for game and replay models
	
*******************************************************************************/


import java.util.HashMap;


/**
 * Interface the common access to simulation and replay models
 */
public interface ModelInterface {	
	/**
	 * Get the width of the map
	 * @return		The width of the map
	 */
	public int getMapWidth();
	
	
	/**
	 * Get the height of the map
	 * @return		The height of the map
	 */
	public int getMapHeight();
	
	
	/** 
	 * Get the agent register
	 * @return		The numerical id based agent hashmap
	 */
	public HashMap<Integer, Agent> getAgentsI();
	
	
	/** 
	 * Get the food register
	 * @return		The numerical id based food hashmap
	 */
	public HashMap<Integer, Food> getFoodI();
	
	
	/** 
	 * Get the team register
	 * @return		The numerical id based team hashmap
	 */
	public HashMap<Integer, Team> getTeamsI();
	

	/**
	 * Get the agent's view distance
	 * @return		The agent's view distance
	 */
	public int getViewDistance();
	
	
	/**
	 * Get the actual round of the game 
	 * @return		The actual round of the game
	 */
	public int getRound();
	
	
	/**
	 * Get the starting energy of a team
	 * @return		Starting total energy of a team
	 */
	public int getTeamStartEnergy();
	
	
	/**
	 * Update the strongest agent reference if necessary - only used 
	 * in the game model
	 */
	public void updateStrongestAgent();
	
	
	/**
	 * Get the round limit of the game
	 * @return		The round limit of the game
	 */
	public int getRoundLimit();
	
	
	/** 
	 * Get the maximal food value possible
	 * 
	 * @return		The maximal food value 
	 */
	public int getMaxFoodValue();
	
	
	/**
	 * Return the registered water object's hashmap
	 * 
	 * @return		Water objects on the map
	 */
	public HashMap<Integer, Water> getWatersI();
}