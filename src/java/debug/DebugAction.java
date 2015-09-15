/*******************************************************************************
	
	AgentGame 11.00.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2011.
	
	DebugAction.java - sample internal action for debugging
	
*******************************************************************************/

package debug;

import jason.asSemantics.*;
import jason.asSyntax.*;


/**
 * This class is a sample internal action also useful for debugging.
 */
public class DebugAction extends DefaultInternalAction{
	/** Serial necessary for serialization. */
	private static final long serialVersionUID = 4L;
	
	/** The GUI Frame of the debugger. */
	DebugFrame debugFrame;
	
	
	/**
	 * The default constructor initializes the GUI.
	 */
	public DebugAction() {
		debugFrame = new DebugFrame();
	}

	
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
		// Update the GUI with the agent's current perception
		debugFrame.add(ts);
		
		// This action finishes always with success - no failure handling needed in the .asl code.
		return true;
	}
}