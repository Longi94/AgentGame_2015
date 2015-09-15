/*******************************************************************************
	
	AgentGame 11.00.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2011.
	
	AlwaysTrue.java - sample internal action returning true all the time
	
*******************************************************************************/

package adamant;

import jason.asSemantics.*;
import jason.asSyntax.*;

/**
 * AlwaysTrue is a sample internal action returning true all the time
 *
 */
public class AlwaysTrue extends DefaultInternalAction{
	/** Serial necessary for serialization. */
	private static final long serialVersionUID = 5L;
	
	/**
	 * This is the method called by Jason if an agent executes our
	 * new internal action. All parameters are directly from Jason.
	 * 
	 * @param ts 			The TransitionSystem object.
	 * @param un 			The Unifier of the call.
	 * @param arg 			Terms of the call.
	 * @return 				Return true / false based on success or failure of the action.
	 * @throws Exception 	Exceptions thrown here are caught by Jason.
	 */
	public Object execute(TransitionSystem ts, final Unifier un, final Term[] arg) throws Exception {	
		// This action finishes always with success
		return true;
	}

}
