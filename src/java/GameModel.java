/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	GameModel.java - model class
	
*******************************************************************************/


import java.util.*;


/**
 * Model of the world in the game. 
 * 
 * @author Peter Eredics
 */
public class GameModel implements ModelInterface{
    
	/** The map object to handle position related tasks */
    public GameMap map;
    
    /** The configuration object handling setting from the config file */
    public GameConfig config;
    
    /** HashMap to access agents by their numerical ID */
	public HashMap<Integer,Agent>  agentsI;
	
	/** HashMap to access agent by their name set by JASON */ 
	public HashMap<String, Agent>  agentsS;
	
	/** HashMap to access food object by their numerical ID */
	public HashMap<Integer,Food>   foodsI;
	
	/** HashMap to access teams by their name */
    public HashMap<String,Team>    teamsS;
    
    /** HashMap to access teams by their numerical ID */
    public HashMap<Integer,Team>   teamsI;
    
    /** HashMap to access water objects by their numerical ID */
    public HashMap<Integer,Water> watersI;
        
    /** The current round of the game */
    public int round = 0;
    
    /** The distance the agents are able to see - default set from the config file */
    public int viewDistance = 0;
    
    /** The randomizing X factor of deterministic food positioning */
    private float newFoodXScaler;
    
    /** The randomizing Y factor of deterministic food positioning */
    private float newFoodYScaler;
    
    /** The maximal food value allowed by the configuration */
    private int maxFoodValue;
    
    /** The default food value loss in every round */
	int foodDefaultLost;
	
	/** The value lost by a food when an agent is on its position */
	int foodDefaultLostAgent;
	
	/** Maximum execution time allowed for a team in the whole game*/
	int maxTeamExecutionTime;
	
	
	
	/**
	 * Generate random food scaler value, but exclude values close to 0, 30, 60 and 90.
	 * @return		Random food scaler value.
	 */
	private float generateFoodScaler() {
		float value = 0;
		while ((value<2)||((value>28)&&(value<32))||((value>58)&&(value<62))||(value>88))
			value = (float)(Math.random()*0.8+0.1)*100;
		return value;
	}
	
	
	/**
	 * Calculate the total energy of all teams in the game. 
	 * @return		Total energy of all teams.
	 */
	public double getTotalTeamsEnergy(){
		double result = 0;
		for(int i=0; i<teamsI.size(); i++)
			result += teamsI.get(i).getTotalEnergy();
		return result;
	}
	
	
    /**
     * Constructor initializing the model.
     * @param config			The configuration object of the application
     */
    public GameModel(GameConfig config) {
    	// Set local variables and references
    	this.config = config;
    	viewDistance = config.getInt("ViewDistance");
    	
    	// Create hashmaps 
        agentsI 	= new HashMap<Integer,Agent>  ();
        agentsS 	= new HashMap<String, Agent>  ();
        foodsI   	= new HashMap<Integer,Food>   ();
        teamsI  	= new HashMap<Integer,Team>   ();
        teamsS  	= new HashMap<String,Team>    ();
        watersI		= new HashMap<Integer,Water>  ();

        // Create the map object with waters
        map = new GameMap(config.getInt("MapWidth"),config.getInt("MapHeight"),this, config);
        Water.generateWaters(this, config);
        
        // Init deterministic food positioning parameters
        newFoodXScaler = generateFoodScaler();
        newFoodYScaler = generateFoodScaler();
        if(config.getInt("DetFoodPositioning")==1) {
        	System.out.println("Initializing food generator: ");
        	System.out.println("   - x scaling factor = "+newFoodXScaler);
        	System.out.println("   - y scaling factor = "+newFoodYScaler);
        }
        
        // Load food behavior from the config file
        maxFoodValue = config.getInt("MaxFoodValue");        
		foodDefaultLost = config.getInt("FoodValueLostAlone");
		foodDefaultLostAgent = config.getInt("FoodValueLostAgents");
		
		maxTeamExecutionTime = config.getInt("MaxTeamTime");
    }
	
    
    /**
     * Handle the turn action of an agent
     * @param agent				The agent executing the action
     * @param head				The new direction
     * @return					True if the agent was able to turn
     */
	public boolean turn(Agent agent, int head) {
		// If the agent has enough energy...
        int cost = config.getInt("TurnCost")*map.getCellWaterFactor(agent.position);
		if (!agent.hasEnergy(cost)) return false;
		
		// ... it can successfully turn.
		int newHead = head;
		while (newHead<0) newHead+=4;
		agent.direction = newHead % 4;

		agent.burnEnergy(cost);
		return true;
	}
	
	
	/**
	 * Handle the turn action of an agent
	 * @param agent				The agent executing the action
	 * @param direction			The direction to move
	 * @return					True if the agent was able to move
	 */
	public boolean step(Agent agent, int direction) {
		// If the agent has enough energy...
        int cost = config.getInt("StepCost")*map.getCellWaterFactor(agent.position);
        if (!agent.hasEnergy(cost))	return false;
	
        // ... then calculate the new position
        Position newPosition = new Position(agent.position);
        switch (direction) {
                case 0 : newPosition.y--; break;
                case 1 : newPosition.x++; break;
                case 2 : newPosition.y++; break;
                case 3 : newPosition.x--; break;
                default: return false;
        }

        // Only move to the new position if it is steppable for the agent
        if (isSteppableForAgent(agent, newPosition)) {
        	// Consume energy and set new position
        	agent.burnEnergy(cost);
            agent.position = newPosition;
            return true;
        } else return false;
    }

	
	/**
	 * Handle the eat action of an agent
	 * @param agent				The agent executing the action
	 * @return					True if the action succeeded
	 */
    public boolean eat(Agent agent) {
    	// If there is food on the agent's position...
        for (int foodId=0; foodId<foodsI.size(); foodId++)
            if (foodsI.get(foodId).position.equals(agent.position)) {
            	// Load the energy of the food to the agent
                Food aFood = foodsI.get(foodId);
                if (aFood.value>config.getInt("MaxFoodEaten")) {
                	GameEnvironment.debug("["+agent.name+"] Food eaten: "+agent.position.x+"x"+agent.position.y+" value "+config.getInt("MaxFoodEaten"));
                	// Only the maximum amount allowed can be eaten
                	agent.gainEnergy(config.getInt("MaxFoodEaten"));
                	aFood.value -= config.getInt("MaxFoodEaten");
                } else {
                	GameEnvironment.debug("["+agent.name+"] Food eaten: "+agent.position.x+"x"+agent.position.y+" value "+aFood.value);
                	agent.gainEnergy(aFood.value);
                	// Reset food position and value randomly
                	resetFood(foodId);
                	
                }
                return true;
            };
        return false;
    }
    
    
    /**
     * Handle the energy transfer action between two agents
     * @param sender			The agent willing to send energy
     * @param target			The agent willing to receive energy
     * @param amount			The amount of energy to be sent
     * @return					True if the energy was transfered
     */
    public boolean transfer(Agent sender, Agent target, int amount) {
    	// If the request is valid...
    	if (amount<0) return false;
    	// ... and  the sender has enough energy...
    	if (!sender.hasEnergy(amount)) return false;
    	// ... and the receiver is on the same position as the sender...
    	if (!sender.position.equals(target.position)) return false;
    	// ... then the amount of energy is consumed...
    	sender.burnEnergy(amount);
    	// ... and the target receives a subset of the energy sent.
    	target.gainEnergy((int)Math.round((float)amount*(100f-(float)config.getInt("TransferEnergyLoss"))/100f));
    	return true;
    }
    
    
    /**
     * Handle the attack of an agent
     * @param attacker			The agent attacking
     * @param attackedId		The agent being attacked
     * @return					True if the attack succeeded
     */
    public boolean attack(Agent attacker, int attackedId) {
    	Agent attacked = agentsI.get(attackedId);
    	int cost = config.getInt("AttackCost");
    	
    	// Is the attacked visible for the attacker?
    	if (!isVisible(attacker.position, attacker.direction, attacked.position)) {
    		return false;
    	}
    	
    	// Is the attacker different from the agent being attacked?
    	if (attacker.id==attackedId) {
    		return false;
    	}
    	
    	// Has the attacker enough energy to attack?
    	if (!attacker.hasEnergy(cost)) { 
    		return false;
    	}

    	// Consume energy from the attacker
    	attacker.burnEnergy(cost);
    	
    	// If the attacked is stronger then the attacker in the beginning of the round, 
    	// nothing happens to the attacked
    	if (attacked.getEnergy()>=attacker.getEnergy()+cost) {
    		return false;
    	}
    	

    	// If the attacker is stronger, the attacked is pushed away and...
    	float dx = attacked.position.x-attacker.position.x;
    	float dy = attacked.position.y-attacker.position.y;
    	float l = (float)Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));
    	dx = (dx / l)*config.getInt("AttackPushEffect");
    	dy = (dy / l)*config.getInt("AttackPushEffect");
    	
    	// When attacking an opponent on the same cell (being pushed to the same cell
    	// by a previous attack) the default direction is the attacert orientation
    	if ((dx == 0)&&(dy == 0)) {
    		switch (attacker.direction) {
    			case 0:
    				dy = config.getInt("AttackPushEffect");
    				break;
    			case 1:
    				dx = config.getInt("AttackPushEffect");
    				break;
    			case 2:
    				dy = -1* config.getInt("AttackPushEffect");
    				break;
    			case 3:
    				dx = -1* config.getInt("AttackPushEffect");
    				break;
    		}
    	}
    	
    	// Handle attack at vertical walls
    	if (attacked.position.x+dx<0) attacked.position.x = 0;
    	else if (attacked.position.x+dx>map.width-1) attacked.position.x = map.width-1;
    	else attacked.position.x = (int)Math.ceil((float)attacked.position.x + dx);
    	
    	// Handle attack at vertical walls
    	if (attacked.position.y+dy<0) attacked.position.y = 0;
    	else if (attacked.position.y+dy>map.height-1) attacked.position.y = map.height-1;
    	else attacked.position.y = (int)Math.ceil((float)attacked.position.y + dy);

    	// ... the attacked looses energy too
    	if (attacked.hasEnergy(config.getInt("AttackedEnergyLoss")))
    		attacked.burnEnergy(config.getInt("AttackedEnergyLoss"));
    	else
    		attacked.setEnergy(0);
    	
    	// Set the attacked agent's LastAttackeBy perception
    	attacked.lastAttackedBy = "["+attacker.id +","+attacker.position.x+","+attacker.position.y+"]";

    	// The attack was successful
    	return true;
    }

    
    /**
     * Calculate random value between the configured limits for a new food object
     * @return					The random value for the food object
     */
	public int getRandomFoodValue() {
        return (int)(Math.random()*(config.getInt("MaxFoodValue")-config.getInt("MinFoodValue"))+config.getInt("MinFoodValue"));
	}
	
	
	/**
	 * Check if a position is suitable for an agent to step to
	 * @param agent				The agent willing to step
	 * @param position			The position to check
	 * @return					True if the agent is able to step to the position
	 */
	public boolean isSteppableForAgent(Agent agent, Position position) {
        // Return false, if the position is outside the map
        if ((position.x<0)||(position.x>=map.width)||(position.y<0)||(position.y>=map.height)) return false;

        // Return false if an agent from a different team is on the position
        for (int aId=0; aId<agentsI.size(); aId++) {
            Agent otherAgent = agentsI.get(aId);
            if (otherAgent.position.equals(position) && (otherAgent.team!=agent.team)) return false; // enemy agent
        }
                
        // Otherwise the position is OK
        return true;
	}

	
	/**
	 * Check if a position is visible from an other position in the given direction
	 * @param from				The position looking from
	 * @param direction			The direction looking at
	 * @param target			The position looking at
	 * @return					True if the target position is visible
	 */
    public boolean isVisible(Position from, int direction, Position target) {
        int dx = target.x - from.x;
        int dy = target.y - from.y;            

        // Return true, if the target is in the triangular view field 
        switch (direction) {
            case (0):
                if ((dy<=0)&&(dy*-1<=viewDistance)&&(Math.abs(dx))<=dy*-1) return true;
                return false;
            case (1):
                if ((dx>=0)&&(dx   <=viewDistance)&&(Math.abs(dy))<=dx) return true;
                return false;
            case (2):
                if ((dy>=0)&&(dy   <=viewDistance)&&(Math.abs(dx))<=dy) return true;
                return false;
            case (3):
                if ((dx<=0)&&(dx*-1<=viewDistance)&&(Math.abs(dy))<=dx*-1) return true;
                return false;
        }
        
        // Otherwise return false
        return false;
    }
    
    
    /**
     * Update the strongest agent reference
     */
    public void updateStrongestAgent() {
    	// Check all agents to be the strongest
    	for (int aId=0; aId<agentsI.size(); aId++)
            if (agentsI.get(aId).getEnergy()>Agent.maxEnergyAgent.getEnergy())
                Agent.maxEnergyAgent = agentsI.get(aId);
    }


	/**
	 * @see ModelInterface#getAgentsI()
	 */
	public HashMap<Integer, Agent> getAgentsI() { return agentsI; }

	
	/**
	 * @see ModelInterface#getFoodI()
	 */
	public HashMap<Integer, Food> getFoodI() { return foodsI; }

	
	/**
	 * @see ModelInterface#getTeamsI()
	 */
	public HashMap<Integer, Team> getTeamsI() {return teamsI; }

	
	/**
	 * @see ModelInterface#getMapWidth()
	 */
	public int getMapWidth() { return map.width; }


	/**
	 * @see ModelInterface#getMapHeight()
	 */
	public int getMapHeight() {	return map.height; }
	
	/**
	 * @see ModelInterface#getViewDistance()
	 */
	public int getViewDistance() { return viewDistance; }
	
	/**
	 * @see ModelInterface#getRound()
	 */
	public int getRound() { return round; }


	/**
	 * @see ModelInterface#getTeamStartEnergy()
	 */
	public int getTeamStartEnergy() { return config.getInt("StartingEnergy"); }


	/**
	 * @see ModelInterface#getRoundLimit()
	 */
	public int getRoundLimit() { return config.getInt("RoundLimit"); }
	
	
	/**
	 * Decrease food values in every round as a factor of their state 
	 */
	public void decreaseFoods() {
		int lost = 0;
		
		// Process all food objects
		for (int i=0; i<foodsI.size();i++) {
			Food food = foodsI.get(i);
			lost = foodDefaultLost;
			
			// Check if an agent is standing on the food
			for (int j=0; j<agentsI.size();j++) 
				if (agentsI.get(j).position.equals(food.position)) { 
					lost = foodDefaultLostAgent;
					break;
				}
									
			if (food.value>lost) {
				// Decrease food value if possible or...
				food.value -= lost;
			} else {
				// ...reset food otherwise.
				resetFood(i);
			}
		}
	}
	
	
	/**
	 * Reset food by repositioning it and initializing it with a random new value.
	 * 
	 * @param foodId			The food object id to reset
	 */
	public void resetFood(int foodId) {
		Food food = foodsI.get(foodId);
		
		// If deterministic positioning is enabled
		if (config.getInt("DetFoodPositioning")==1) {				
			int newX = 0;
			int newY = 0;
			Food otherFood;
			for (int i=0;i<foodsI.size();i++){	
				if (i==foodId) continue;
				otherFood = foodsI.get(i);
				newX += otherFood.position.x;
				newY += otherFood.position.y;
			}
			// Calculate new X and Y based on the position of other food objects
			newX = (int)Math.round(newX*newFoodXScaler+Math.random()*10-5) % config.getInt("MapWidth");
			newY = (int)Math.round(newY*newFoodYScaler+Math.random()*10-5)  % config.getInt("MapHeight");
			while(newX<0) newX+=config.getInt("MapWidth");
			while(newY<0) newY+=config.getInt("MapHeight");
			while(newX>config.getInt("MapWidth")-1)  newX-=config.getInt("MapWidth");
			while(newY>config.getInt("MapHeight")-1) newY-=config.getInt("MapHeight");
			
			food.position.x = newX;
			food.position.y = newY;
		} else {
			// If deterministic positioning is disabled, select a completely random position
			food.position = map.getRandomFreePosition();
		}
		
		// Set random food value
    	food.value = getRandomFoodValue();	
	}
	
	
	/**
	 * Return the maximal allowed food value
	 * 
	 * @return 				Maximal food value
	 */
	public int getMaxFoodValue() { return maxFoodValue; }
	
	
	/**
	 * Return the HashMap storing water objects on the map
	 * 
	 * @return				The water object register hashmap 
	 */
	public HashMap<Integer, Water> getWatersI() { return watersI; }
}