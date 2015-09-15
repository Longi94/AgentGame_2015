/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	GameMap.java - grid map class
	
*******************************************************************************/

/**
 * The map class handling position based tasks
 * 
 * @author Peter Eredics
 */
public class GameMap {
	/** Width of the map */
	public int width;
	
	/** Height of the map */
	public int height;
	
	/** The model class of the application */
	GameModel model;
	
	/** Water cells of the map */
	private Boolean waters[][];
	
	/**  The water's cost multiplier factor*/
	private int waterFactor; 
	
	
	/**
	 * Constructor of the map object.
	 * @param width			Width of the map
	 * @param height		Height of the map
	 * @param model			The model class of the application
	 * @param config		The config object of the application
	 */
	public  GameMap(int width, int height, GameModel model, GameConfig config) {
        // Init private variables
        this.width = width;
        this.height = height;
        this.model = model;

        // Create food object at random position with random values
        for (int foodId=0; foodId<config.getInt("FoodCount");foodId++) {
            Food newFood = new Food(foodId,getRandomFreePosition(),model.getRandomFoodValue());
            model.foodsI.put(foodId, newFood);
        }
        
        // Create water register
        waters = new Boolean[width][height];
        for (int x=0; x<width; x++)
        	for (int y=0; y<height; y++)
        		waters[x][y] = false;
        waterFactor = config.getInt("WaterFactor");
	}

	
	/**
	 * Choose a position not occupied by an agent or food object
	 * @return				A free position on the map
	 */
	public Position getRandomFreePosition() {
		Position result = new Position(0,0);
		do {
			// Select random x and y coordinates 
			result.x = (int)Math.round(Math.random()*(width-1));
			result.y = (int)Math.round(Math.random()*(height-1));
		} while (!isFree(result));
		return result;
	}
	
	
	/**
	 * Check a position weather it is not occupied by an agent or food object
	 * @param position		The position to check
	 * @return				True if the position is free
	 */
	public boolean isFree(Position position) {
		// Is it inside the map?
		if ((position.x<0)||(position.x>width-1)||(position.y<0)||(position.y>height-1)) return false;		
			
		// Is there some food?
		for (int foodId=0; foodId<model.foodsI.size(); foodId++)
			if (model.foodsI.get(foodId).position.equals(position)) return false;
		
		// Is there an other agent?
		for (int aId=0; aId<model.agentsI.size(); aId++)
			if (model.agentsI.get(aId).position.equals(position)) return false;
		
		// If neither above, then it is free
		return true;
	}
	
	
	/**
	 * Set agent position if the position is free
	 * @param agent			The agent to move to the position
	 * @param position		The position to move to
	 * @return				True if the agent is moved to the position
	 */
	public boolean setAgentPosition(Agent agent, Position position) {
		if (!isFree(position)) return false;
		agent.position  = position;
		return true;
	}
	
	
	/**
	 * Mark cell as water
	 * 
	 * @param x				X coordinate of the cell
	 * @param y				Y coordinate of the cell
	 */
	public void registerWaterCell(int x, int y) {
		waters[x][y] = true;
	}
	
	
	/**
	 * Return the water factor of a given cell
	 * 
	 * @param position		Position of the cell
	 * @return				The cost multiplying factor of the cell
	 */
	public int getCellWaterFactor(Position position) {
		if (waters[position.x][position.y]) return waterFactor;
		else return 1;
	}
	
	
	/**
	 * Check is there is water on the given position
	 * 
	 * @param position		The position to check
	 * @return				True if there is water
	 */
	public boolean isWater(Position position) {
		return waters[position.x][position.y];
	}
}