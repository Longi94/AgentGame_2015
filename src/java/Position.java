/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	Position.java - position class
	
*******************************************************************************/


/**
 * Class representing positions in the game.
 *
 * @author Peter Eredics
 */
public class Position {
	/** X coordinate on the grid.*/
	public int x;
	
	/** Y coordinate on the grid.*/
	public int y;
	
	
	/**
	 * Constructor setting booth coordinates 
	 * @param x		X coordinate on the grid
	 * @param y		Y coordinate on the grid
	 */
	public Position(int x, int y)  {
		this.x = x;
		this.y = y;
	}
	
	
	/**
	 * Copy constructor
	 * @param p		Position to be copied.
	 */
	public Position(Position p)  {
		this.x = p.x;
		this.y = p.y;
	}

	
	/**
	 * Check equality of the positions.
	 * @param p		The other position to check against.
	 * @return		Returns true if positions are the same.
	 */
    public boolean equals(Position p) {
        return ((p.x==x)&&(p.y==y));
    }
}
