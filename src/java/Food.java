/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	Food.java - food class
	
*******************************************************************************/


/**
 * This class stores all values about food object in the game.
 *
 * @author Peter Eredics
 */
public class Food{
	/** The numerical id of the food object. */
	public int id;
	
	/** The actual position of the food object. */
	public Position position;
	
	/** The acutal value of the food object. */
	public int value;
	
	
	/**
	 * Default constructor setting all internal variables.
	 * @param id			The numerical id of the new food object.
	 * @param position		The actual position of the new food object.
	 * @param value			The acutal value of the new food object.
	 */
	public Food(int id, Position position, int value) {
		this.id = id;
		this.position = position;
		this.value = value;
	}
}
