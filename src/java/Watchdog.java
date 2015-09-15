/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	GameView.java - GUI class
	
*******************************************************************************/


import java.awt.event.*;


/**
 * The class looking for frozen simulation if no GUI is enabled
 *  
 * @author Peter Eredics
 */
public class Watchdog implements ActionListener{	
	/** The game model to operate on*/
	private GameModel model;
	
	/** The game environment*/
	private GameEnvironment environment;
	
	/** The execution time limit for a single agent */
	private int maxAgentExecutionTime;
	
	/** The total execution time limit for a team*/
	private int maxTeamExecutionTime;
	
	
	/**
	 * Default constructor of the watchdog
	 * 
	 * @param environment	The game environment
	 * @param model			The geme model
	 */
	public Watchdog(GameEnvironment environment, GameModel model) {
		this.model = model;
		this.environment = environment;
		maxAgentExecutionTime = environment.config.getInt("MaxAgentTime")*1000;
		maxTeamExecutionTime  = environment.config.getInt("MaxTeamTime")*1000;
	}
	
	
	/**
	 * Checking for frozen simulation when the timer ticks: disqualify team and end
	 * simulation if necessary
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		// Do nothing if no agent is active, the simulation is paused or the game is over
		if (!environment.isAgentActive() || environment.isPaused() || environment.isDone()) return;
		
		// Check active agent timeout
		if (System.currentTimeMillis()-environment.getActiveAgentActivated()>maxAgentExecutionTime) {
			System.out.println("Active agent frozen: "+model.getAgentsI().get(environment.activeAgentID).name);
			Agent agent = model.agentsI.get(environment.activeAgentID); 
			agent.team.disqualify();
			environment.closeSimulation("agent "+agent.name+" is not responding for "+maxAgentExecutionTime+" ms.", true);
			return;
		}
		
		// Check team timeout
		for (int i=0; i<model.teamsI.size();i++) {
			Team team = model.teamsI.get(i);
			if (team.getExecutionTime()>maxTeamExecutionTime) {
				// Team out of time
				team.disqualify();
				environment.closeSimulation("team "+team.name+" is out of time ("+maxTeamExecutionTime+" ms).", true);
				return;
			}
		}
	}
}
