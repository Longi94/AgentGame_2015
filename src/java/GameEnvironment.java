/*******************************************************************************
	
	AgentGame 13.20
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	GameEnvironment.java - game environment (controller) class
	
*******************************************************************************/


import jason.asSyntax.*;
import jason.environment.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.Timer;


/**
 * The main class of the AgentGame, extending the built in 
 * TimeSteppedEnvironment.
 * 
 * @author Peter Eredics
 */
public class GameEnvironment extends Environment implements EnvironmentInterface {
	/** The model of the game world */
	private GameModel model;
	
	/** The CSV logger recording energy levels of the agents */
    private CSVLog log;
    
    /** The step logger */
    private RoundLog roundLog; 
    
    /** The configuration read from the file specified in the mas2j file */
    public GameConfig config;
    
    /** The GUI of the game */
    private GameView view;
    
    /** The time in milliseconds to sleep after each round */
    public int sleepTime = 110;
    
    /** The SummaryGraphs object for collecting data and displaying graphs. */
    private SummaryGraphs graphs;
       
    /** The id of the agent last joining the game */ 
    private int lastUsedAgentId = -1;
    
    /** Number of rounds in a game - negative value means no limit */
    private int roundLimit = -1; 
    
    /** The minimum sleep time of animation */
    public final int minSleepTime = 0;
    
    /** The agent selected for execution */
    public int activeAgentID = 0;
    
    /** Version value for serialization compatibility */
    public static String version = "15.00";
    
    /** Indicates that this version is the latest available */
    public static boolean upToDate = true;
    
	/**	Indicates if the game is paused */
	private boolean paused = false;
	
	/** Indicates if the simulator should display debug messages*/
	private static boolean debugging = false; 
	
	/** Speed level of the simulation */
	private int speed;
	
	/** Indicates if the simulation ended */
	private boolean done = false;
	
	/** The time stamp when the active agent started execution */
	private long activeAgentActivated;
	
	/** The time stamp when the simulation started */
	private long simulationStarted = 0;
	
	/** The time stamp when the simulation finished */
	private long simulationFinished;
	
	/** Indicates if an agent code is being executed */
	private boolean agentActive = false;
	
	/** The watchdog object monitoring timeout events */
	private Timer watchdogTimer;
	
	
	/**
	 * Default constructor of the environment creating a single background
	 * thread inherited from its parent Environment class.
	 */
    public GameEnvironment() {
    	super(1);
    }
    
    
    /**
     * Initialize the application.
     * @param args			The string arguments specified in the mas2j file
     */
    @Override
    public void init(String[] args) {
        System.out.println("AgentGame "+GameEnvironment.version+" simulator initializing...");
        
        // Check if the config file was specified as parameter in the mas2j file
        if (args.length != 1) {
        	System.out.println("\nConfiguration file argument required as parameter in the mas2j file!");
        	System.out.println("Example: \n       environment: GameEnvironment(\"AgentGame.conf\")\n");
        	try {Thread.sleep(6000);} catch (InterruptedException e) {e.printStackTrace();}
        	System.exit(1);
        }
        
        // Load configuration
        System.out.println("Loading game configuration from "+args[0]+"...");
        config = new GameConfig(args[0]);
        roundLimit = config.getInt("RoundLimit");
        speed = config.getInt("SimulatorSpeed");
        
        // Check for updates if enabled
        if (config.getInt("AutoUpdate")==1)
        	new Updater();
        
        // Create GameModel
        System.out.println("Initializing game model...");
        model = new GameModel(config);
        
        // Create CSVLog if enabled
        log = new CSVLog();
        if (config.getInt("CSVLogging")==1) {
        	System.out.println("Preparing CSV logging to "+config.getString("CSVLogFile")+"...");
        	log.initLog(config);
        }        
        
        // Create StepLog if enabled
        if (config.getInt("StepLogging",0)==1) {
        	System.out.println("Preparing step log "+config.getString("StepLogFile")+"...");
        	roundLog = new RoundLog(model, config);
        }
        
        // Create GUI if not disabled
        if (config.getInt("NoGUI",0)==0) {
        	System.out.println("Creating GUI...");
        	view  = new GameView(this, model, config, false," - Simulator");
        	view.setVisible(true);
        }

    	// Set up watchdog timer
        if (config.getInt("Watchdog",0)==1) {
	      	System.out.println("Initializing watchdog timer...");
	      	watchdogTimer = new Timer(5000, new Watchdog(this, model));
	      	watchdogTimer.start();
        }
        
        // Initialize graphs if enabled
        if (config.getInt("SummaryGraphs",0)==1) {
        	System.out.println("Initializing summary graphs...");
        	graphs = new SummaryGraphs(config);
        }
        
        // Start Jason
        System.out.println("Starting Jason...");
        String[] timeSteppedEnvironmentParameters = new String[] {"0"};
        super.init(timeSteppedEnvironmentParameters);
        
        System.out.println("AgentGame simulator ready.");        
        
        System.out.println("Waiting for agents...");
        new Thread() {
            // Wait for agents to come
            public void run() {
            		int tries = 10;
            		while (tries>0) {
            			try {
            				Thread.sleep(1000);            			
            			} catch (InterruptedException e) {}
            			System.out.println(" ...");
            			tries--;
            			if ((model.teamsI.size()>=2)&&(tries>1)) tries = 1;
            		}
            		if (model.teamsI.size()<2) {
            			System.out.println("Less than 2 teams registered for the game: terminating simulation.");
            			System.exit(-1);
            		} else startSimulation();
            	}
            }.start();
            
    }
    
    
    /**
     * Start the simulation by initializing timer and activating the first
     * round and the first agent.
     */
    private void startSimulation() {
    	System.out.println("Starting simulation...");
    	simulationStarted = System.currentTimeMillis();
        newRound();
        activateNextAgent();
    }

    
    /** 
     * Handle agent perception request when agent is not yet in the hashmaps 
     */
    @Override
    public List<Literal> getPercepts(String aName) {
       	// If the agent is not in the map, add it and update its perception
    	if (model.agentsS.get(aName) == null) {
            addAgent(aName);
            
            clearPercepts(aName);
            System.out.println(" + "+aName);
            
            return super.getPercepts(aName);
    	}
    	return super.getPercepts(aName);    	
    }
    
    
	/**
	 * Add new agent to the game    
	 * @param aName			The name of the agent used by JASON
	 * @return				The new agent object created
	 */
    private synchronized Agent addAgent(String aName) {
	    // Register new team if necessary
	    String teamName = aName.substring(0,aName.indexOf('_'));
	    int teamId;
	    Team newTeam = model.teamsS.get(teamName);
	    if (newTeam==null){
	        teamId = model.teamsS.size();
	        newTeam = new Team(teamName, teamId, Team.teamColors[teamId],model);
	        model.teamsS.put(teamName,newTeam);
	        model.teamsI.put(teamId, newTeam);
	    }
	
	    // Create new agent
	    lastUsedAgentId++;
	    int aId = lastUsedAgentId;
	    Agent newAgent = new Agent(aId, aName, newTeam.getTeamStart(),(int)Math.floor(Math.random()*4), config.getInt("StartingEnergy"), newTeam, model);
	    model.agentsI.put(aId,newAgent);
	    model.agentsS.put(aName,newAgent);
	
	    // Return new agent
	    return newAgent;
    }

 
    /**
     * Start a new round if the simulation is still active. Do all maintenece
     * task related to rounds.
     */
	protected void newRound() {
		if (done) waitEndless();
		
		// Sleep to slow down animation
		try {
			if (view != null) Thread.sleep(Math.round(Math.pow(5-speed,3)*10));
		} catch (InterruptedException e) {}
    	
    	// If there is no round limit or it is not yet reached: count rounds
		if ((roundLimit<=0) || (model.round<roundLimit)) {
    		model.round++;
    		log.logRound++;
		}
    	
    	// If logging is enabled and it is time, write to the log.
    	if (log.enabled) {
    		if (model.round == 1) {
    			log.writeHeader(model);
    			log.logState(model);
    			log.logRound = 0;
    		} else if ((model.round>1)&& (log.logRound >= config.getInt("CSVLogInterval"))) {
    			log.logState(model);
    			log.logRound = 0;
    		}
    	}
    	
    	// If summary graphs are enabled
    	if (graphs != null) {
    		if (model.round == 1) {
    			graphs.initGraphs(model);
    		} 
    		if (model.round>0) {
    			graphs.record(model.round,model);
    		}
    	}
    	
    	// If step logs are enabled
    	if ((roundLog != null)&&(model.round>0)) roundLog.logRound();
    	 	
    	// If there is no GUI print some progress information to the console
    	if ((view == null) && (model.round % 500 == 0)) System.out.println(" - - - - round "+model.round+" of "+roundLimit+" finished - - - - ");
    	
    	activeAgentID = 0;
    }
	
	
	/**
	 * Practically stop execution by sleeping in endless loop. Useful
	 * to keep windows open until the user closes them to finish the
	 * simulation manually.
	 */
	private void waitEndless() {
		while (true) try { Thread.sleep(5000000); } catch (InterruptedException e) {}
	}
	
	
	/**
	 * End the simulation and close all logs and outputs.
	 * @param reason				The name of the reason to end.
	 * @param sourceWatchdog		Indicates if simulation is interrupted by the watchdog timer.
	 */
	public void closeSimulation(String reason, boolean sourceWatchdog) {
		done = true;
    	simulationFinished = System.currentTimeMillis();
    	
    	if (watchdogTimer!=null)	watchdogTimer.stop();
    	System.out.println("Simulation stopped: "+reason);
    	System.out.println("Total simulation time: "+((simulationFinished - simulationStarted)/1000)+" sec");
    	   	
    	// Close CSV logs
    	log.closeLogs();
    	   	
    	// Write results
    	writeGameResultsToFile(reason);
    	
    	// Close binary logging if it was enabled
    	if (roundLog!=null) roundLog.closeLog();
    	
    	// Display graph if enabled
    	try {
	    	if (graphs != null) {
	    		System.out.print("Constructing summary graph...");
	    		if (view != null) {
	    				graphs.display(true);
	    				System.out.println(" done.");
	    				while (true) try { Thread.sleep(5000000); } catch (InterruptedException e) {}
	    		} else graphs.display(false);
	    		System.out.println(" done.");
	     	}
    	} finally {
        	// Wait for the user to exit
        	if ((!sourceWatchdog)&&(view != null)) {
        		// Simply wait
        		waitEndless();
        	} 
        	
        	// No GUI and no graphs -> exit
        	if (view == null) System.exit(0);    		
    	}
	}
    

    /** Handle agent actions and end of the game situation
     *  @param aName			Name of the agent
     *  @param action			The action requested
     */    
    @Override
    public boolean executeAction(String aName, Structure action) {
    	// Check if the game was stopped by the round limit
        if ((roundLimit>0) && (model.round>=roundLimit)) {
        	closeSimulation("round limit ("+roundLimit+") reached.", false);
        }
        
        // Process the action
        Agent agent = model.agentsS.get(aName);
    	String actionName = action.getFunctor();
    	Boolean result = false;
        try {            
            if (actionName.equals("wait")) {
            	// No action - always succeeds 
                return true;
            } else if (actionName.equals("turn")) {
            	// Turn - 1 argument: new direction
            	result = model.turn(agent,(int)(((NumberTerm)(action.getTerm(0))).solve()));
            } else if (actionName.equals("step")) {
            	// Step - 1 argument: direction to move 
            	result = model.step(agent,(int)(((NumberTerm)(action.getTerm(0))).solve()));
            } else if (actionName.equals("eat")) {
            	// Eat - no argument
            	result = model.eat(agent);
            } else if (actionName.equals("transfer")) {
            	// Transfer - 2 arguments: target and amount of energy to transfer
            	int targetAgent = (int)(((NumberTerm)(action.getTerm(0))).solve());
                int amount      = (int)(((NumberTerm)(action.getTerm(1))).solve());
                result = model.transfer(agent,model.agentsI.get(targetAgent),amount);
            } else if (actionName.equals("attack")) {
            	// Attack - 1 argument: the agent to attack 
            	result = model.attack(agent,(int)(((NumberTerm)(action.getTerm(0))).solve()));
            } else if (actionName.equals("setlabel")) {
            	// SetLabel - 1 argument: the new label
            	agent.label = action.getTerm(0).toString().replaceAll("[\'\"]", "");
            	return true;
            } else if (actionName.equals("setcolor")) {
            	// Set agent color - arguments are the color channel values
            	agent.forceColor((int)(((NumberTerm)(action.getTerm(0))).solve()), (int)(((NumberTerm)(action.getTerm(1))).solve()), (int)(((NumberTerm)(action.getTerm(2))).solve()));
            	return true;
            } else {
            	// Handle unknown action
                System.out.println("Unknown action in AgentGame: "+action);
                return false;
            }
        } catch (Exception e) {
        	// Handle any exception
        	System.out.println("Exception in AgentGame while executing action "+action+" for "+aName+": - "+e.getMessage());
        	return false;
        }
        return result;
    }

    
    /**
     * Update the perceptions of a single agent
     * @param agent				The agent to be updated
     */
    void updateAgentPercept(Agent agent) { 	
        clearPercepts(agent.name); 

        // Standard perceptions of the agent
    	Literal l = ASSyntax.createLiteral("myname",ASSyntax.createAtom(agent.name));
        addPercept(agent.name, l);
        
    	l = ASSyntax.createLiteral("myid",ASSyntax.createNumber(agent.id));
        addPercept(agent.name, l);
              
    	l = ASSyntax.createLiteral("myteamtimeleft",ASSyntax.createNumber(model.maxTeamExecutionTime-(int)Math.round((float)agent.team.getExecutionTime()/1000f)));
        addPercept(agent.name, l);
        
    	l = ASSyntax.createLiteral("myteam",ASSyntax.createNumber(agent.team.id));
        addPercept(agent.name, l);
    	
        l = ASSyntax.createLiteral("mypos",
                ASSyntax.createNumber(agent.position.x),
                ASSyntax.createNumber(agent.position.y));
        addPercept(agent.name, l);
        
        if (model.map.isWater(agent.position)) addPercept(agent.name, ASSyntax.createAtom("inwater"));
		
        l = ASSyntax.createLiteral("mydir",ASSyntax.createNumber(agent.direction));
        addPercept(agent.name, l);
        
        l = ASSyntax.createLiteral("myenergy",ASSyntax.createNumber(agent.getEnergy()));
        addPercept(agent.name, l);
        
        l = ASSyntax.createLiteral("myteamratio",ASSyntax.createNumber(((double)agent.team.getTotalEnergy())/model.getTotalTeamsEnergy()));
        addPercept(agent.name, l);
    
    	ListTerm list = ASSyntax.createList();
    	Agent other;
    	for (int i=0; i<agent.team.members.size(); i++) {
    		other = agent.team.members.get(i); 
    		if (other.id!=agent.id) list.add(ASSyntax.createAtom(other.name));
    	}   	
        l = ASSyntax.createLiteral("teammates",list);
        addPercept(agent.name, l);
        
        for (int i=0; i<agent.team.members.size(); i++) {
    		other = agent.team.members.get(i); 
    		if (other.id == agent.id) continue;
            l = ASSyntax.createLiteral("teammate",
                    ASSyntax.createNumber(other.id),
                    ASSyntax.createAtom(other.name));
            addPercept(agent.name, l);
    	}   	       

        try {
            l = ASSyntax.createLiteral("lastattacker",ASSyntax.parseList(agent.lastAttackedBy));
        } catch (Exception e) { System.out.println("Parser excpetion preparing the lastAttacker perception list: "+agent.lastAttackedBy);}
        addPercept(agent.name, l);
        
        // Visual perceptions - food
        String foodTerm = "";
        for (int fId=0; fId<model.foodsI.size(); fId++) {
            Food food = model.foodsI.get(fId);
            if (model.isVisible(agent.position,agent.direction,food.position)) {
                int distance = Math.abs(agent.position.x-food.position.x)+Math.abs(agent.position.y-food.position.y);
                if (foodTerm == "")
                    foodTerm = "["+distance+","+food.value+","+food.position.x+","+food.position.y+"]";
                else
                    foodTerm += ",["+distance+","+food.value+","+food.position.x+","+food.position.y+"]";
            }
        }
        try {
            l = ASSyntax.createLiteral("food",ASSyntax.parseList("["+foodTerm+"]"));
        } catch (Exception e) { System.out.println("Parser excpetion preparing the food perception list.");}
        addPercept(agent.name, l);
        
        // Visual perceptions - other agents
        String agentTerm = "";
        for (int aId=0; aId<model.agentsI.size(); aId++) {
            Agent aAgent = model.agentsI.get(aId);
            if (aAgent == agent) continue;
            if (model.isVisible(agent.position,agent.direction,aAgent.position)) {
                int distance = Math.abs(agent.position.x-aAgent.position.x)+Math.abs(agent.position.y-aAgent.position.y);
                if (agentTerm == "")
                    agentTerm = "["+distance+","+aAgent.id+","+aAgent.team.id+","+aAgent.getEnergy()+","+aAgent.position.x+","+aAgent.position.y+","+aAgent.direction+"]";
                else
                    agentTerm += ",["+distance+","+aAgent.id+","+aAgent.team.id+","+aAgent.getEnergy()+","+aAgent.position.x+","+aAgent.position.y+","+aAgent.direction+"]";
            }
        }
        try {
            l = ASSyntax.createLiteral("agent",ASSyntax.parseList("["+agentTerm+"]"));
        } catch (Exception e) { System.out.println("Parser excpetion preparing the food perception list.");}
        addPercept(agent.name, l); 

		l = ASSyntax.createLiteral("time",ASSyntax.createNumber(model.round));
    	addPercept(agent.name, l);
        
        debug("Percepts updated for "+agent.name+".");
        this.getEnvironmentInfraTier().informAgsEnvironmentChanged(agent.name);
        activeAgentActivated = System.currentTimeMillis();
        agentActive = true;
    }

    
    /**
     * Write the game results (team names and final total energy levels) to file
     * for automatic processing of the results
     */
    private void writeGameResultsToFile(String reason) {
    	// Skip if no file is given
    	if (config.getString("ResultFile").equals("")) return;
    	   	
    	try {
    		String hostName;
    		try {
    			hostName = java.net.InetAddress.getLocalHost().getHostName();
    		} catch (Exception e) {
    			hostName = "unknown";
			}
    		
    		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			BufferedWriter resultsFile = new BufferedWriter(new FileWriter(config.getString("ResultFile")));
			resultsFile.append("# AgentGame "+GameEnvironment.version+" simulation results");
			resultsFile.append("\n# Recorded at: "+formatter.format(new java.util.Date()));
			resultsFile.append("\n# Recorded on: "+hostName+"\n\n");
			
			// Write team results
			for (int tId = 0; tId<model.teamsI.size(); tId++) {
				resultsFile.append(model.teamsI.get(tId).name+"\n");
				resultsFile.append(model.teamsI.get(tId).getTotalEnergy()+"\n");
			}
			
			// Write reason
			resultsFile.append("\nSimulation finished because "+reason);
			
        	// Write agent execution times
			resultsFile.append("\nTotal simulation time: "+((simulationFinished - simulationStarted)/1000)+" sec\n");
			resultsFile.append("Execution times:\n");
        	for (int i=0; i<model.teamsI.size(); i++) {
        		Team team = model.teamsI.get(i);
        		resultsFile.append("   "+team.name+": "+team.getExecutionTime()+" ms\n");
        		for (int j=0; j<team.members.size(); j++) {
        			Agent agent = team.members.get(j);
        			resultsFile.append("      "+agent.name+": "+agent.executionTime+" ms\n");
        		}       			        	
        	}
        	
			resultsFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    

	/**
	 * Returns the sleep time of the animation after every round.
	 * @return			The sleep time.
	 */
	public int getSleepTime() {
		return sleepTime;
	}


	/**
	 * Set the animation sleep time after every round.
	 * @param value		The new sleep time.
	 */
	public void setSleepTime(int value) {
		sleepTime = value;
	}


	/**
	 * Set the roundLimit when game automatically ends
	 * @param roundLimit		The end of the game in rounds
	 */
	public void setRoundLimit(int roundLimit) {
		this.roundLimit = roundLimit;
	}


	/**
	 * Get the limit of rounds when the game ends
	 * @return 					The round limit
	 */
	public int getRoundLimit() {
		return roundLimit;
	}


	/**
	 * @see EnvironmentInterface#pause()
	 */
	public void pause() {
		if (done) return;
		if (!paused) 
			paused = true;
		else 
			activateNextAgent();
	}


	/**
	 * @see EnvironmentInterface#play()
	 */
	public void play() {
		if (done) return;
		if (paused) {
			paused = false;
			activateNextAgent();
		}
	}


	/**
	 * Seek the simulation to the given position - not implemented in 
	 * the simulator.
	 * @param percent		The percent to seek to.
	 */
	public void seekToPercent(float percent) {
		// The GameEnvironment does not support seeking
		return;
	}
	
	
	/**
	 * Schedule an action by the agent - JASON calls this inherited function
	 * when an agent executes an external action.
	 * @param agName		Name of the agent executing the action
	 * @param action		The action to be executed
	 * @param infraData		The JASON infrastructure
	 */
	public void scheduleAction(final String agName, final Structure action, final Object infraData) {
		if ((model.round>1)&&(!model.agentsI.get(activeAgentID).name.equals(agName))) {
			// Check if the agent is allowed to execute actions
			// System.out.println("Invalid action execution attempt in round "+model.round+": "+action.toString()+" for "+agName+" when "+ model.agentsI.get(activeAgentID).name+" was executed. Last valid action of "+agName+" was "+model.agentsS.get(agName).getLastAction()+".");
			return;
		}
		
		// Update total execution time
		agentActive = false;
		if ((activeAgentActivated>0)&&(model.agentsI.get(activeAgentID)!=null)) 
			model.agentsI.get(activeAgentID).addExecutionTime(System.currentTimeMillis() - activeAgentActivated);
		else 
			activeAgentActivated = System.currentTimeMillis();
		
		// Try to execute the action and inform JASON about the result
		debug("Executing action "+action.toString()+" for "+agName+".");
		boolean success = executeAction(agName, action);		
		super.getEnvironmentInfraTier().actionExecuted(agName, action, success, infraData);
		
		// Save the agent's last valid action
		model.agentsS.get(agName).storeLastAction(action.toString()+" @ "+model.round);

		// Start new round when all agents are done
		if (activeAgentID>=model.agentsI.size()-1) {
			debug("-------- round #"+model.round+" finished -------- -------------- -------------- -------------- ------------");
			model.decreaseFoods();
			newRound();
		} else {
			activeAgentID++;
		}
		
		// Activate next agent if the simulation is in auto mode
		if (!paused) activateNextAgent();
    }
	
	
	/**
	 * Select the next agent for execution.
	 */
	public void activateNextAgent(){
		// Suspend execution if simulation is already done.
		if (done) waitEndless();
		
		// Test if the next agent exists
		if (model.agentsI.get(activeAgentID)==null) {
			debug("Failed to activate agent "+activeAgentID+".");
			return;			
		}
		
		// Update percepts to trigger agent execution
		debug("Activating agent: "+activeAgentID+" who is "+model.agentsI.get(activeAgentID).name);
		updateAgentPercept(model.agentsI.get(activeAgentID));
	}
	
	
	/**
	 * Print debug messages on the standard output if debug mode is activated.
	 * @param message			The message to display.
	 */
	public static void debug(String message) {
		if (debugging) System.out.println("["+Thread.currentThread().getId()+"@"+System.currentTimeMillis()+"] "+message);
		
	}

	
	/**
	 * Returns true if the simulation is paused by the user.
	 * @return					True if the simulation is paused. 
	 */
	public boolean isPaused() { return paused; }
	
	
	/**
	 * Returns true if the simulation is finished.
	 * @return 					True if the simulation is done.
	 */
	public boolean isDone() { return done; }
	
	
	/**
	 * Indicates if agent code is running.
	 * @return					True if an agent is being executed in the background.
	 */
	public boolean isAgentActive() { return agentActive; }
	
	
	/**
	 * Returns the speed of the simulation.
	 * @return 					Speed level (1-5) of the simulation
	 */
	public int getSpeed() { return speed; }
	
	
	/**
	 * Returns the timestamp when the last agent execution was started. 
	 * @return					Timestamp when the active agent's execution was started.
	 */
	public long getActiveAgentActivated() { return activeAgentActivated; }
	
	
	/**
	 * Set the speed of the simulation.	 
	 * @param speed 			Speed of the simulation from the 1-5 range.
	 */
	public void setSpeed(int speed) {
		if (speed>5) this.speed = 5;
		else if (speed<1) this.speed = 1;
		else this.speed = speed;
	}
	
	
	/**
	 * Returns true if the simulator is running.  
	 * @return 					True if the simulator is already started.
	 */
	public boolean isReady() {return simulationStarted!=0; }
}
