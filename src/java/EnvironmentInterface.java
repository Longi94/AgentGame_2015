/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	EnvironmentInterface - common interface for the control classes
	
*******************************************************************************/


/** 
 * This interface unifies the control classes ("environments") of the simulator
 * and the player making it possible to use the same View object in both 
 * applications with only minor modifications.
 *
 * @author Peter Eredics
 */
public interface EnvironmentInterface {			
	/**	
	 * Pause or step playback
	 */
	public void pause();
	
	
	/**
	 * Start forward playback
	 */
	public void play();
	
	
	/**
	 * Seek the playback to a given position in percents - used only in Player
	 * @param percent	The relative position to seek to
	 */
	public void seekToPercent(float percent);

	
	/**
	 * Indicates if the game or the playback is paused
	 * @return			True if the simulaiton is paused
	 */
	public boolean isPaused();
	
	
	/**
	 * Returns the speed of the simulation
	 * @return			Actual speed of the simulation
	 */
	public int getSpeed();
	
	
	/**
	 * Set the speed of the simulation
	 * @param speed		New speed of the simulation
	 */
	public void setSpeed(int speed);
	
	
	/**
	 * Indicates if the simulation is done.
	 * @return			True if the simulation is done.
	 */
	public boolean isDone();
	
	
	/**
	 * Indicates if the simulation is started.
	 * @return			True if the simulation is started.
	 */
	public boolean isReady();
}