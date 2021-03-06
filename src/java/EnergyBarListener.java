/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	EnergyBarListener - Mouse listener class handling clicks on the energy bar
	
*******************************************************************************/


import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;


/** 
 * This class handles mouse events from the energy bar part of the GUI
 *
 * @author Peter Eredics
 */

class EnergyBarListener extends MouseInputAdapter {
	/**	The EnergyBars object the click occurred on */
	private EnergyBars canvas;
	
	/** The environment to call back for seeking */
	private EnvironmentInterface environment;
	
	private ModelInterface model;
	
	/**
	 * Constructor setting internal reference variables
	 * @param canvas			The EnergyBars object the click occurred on
	 * @param environment		The environment to call back for seeking
	 * @param model				The game model
	 */
	public EnergyBarListener(EnergyBars canvas,EnvironmentInterface environment, ModelInterface model) {
		this.canvas = canvas;
		this.environment = environment;
		this.model = model;
	}
	
	
	/**
	 * Inherited method to handle mouse release event - the only
	 * one used here, because all mouse click operation ends with this
	 * @param event 			The event to handle
	 */
	public void mouseReleased(MouseEvent event) {
		// If the mouse is in the progress bar region...
		if ((event.getX()>=10) && 
			(event.getY()>=canvas.getSize().height-45) && 
			(event.getY()<=canvas.getSize().height-35) && 
			(event.getX()<=canvas.getSize().width-10))
			// ... calculate its relative position in percents and send seek command for the environment
			environment.seekToPercent(100f*  (float)(event.getX()-10) / (float)(canvas.getSize().width-20)   );
		
		// If the mouse is over the pause button
		if ((event.getX()>=10) && 
			(event.getY()>=canvas.getSize().height-30) && 
			(event.getY()<=canvas.getSize().height-10) && 
			(event.getX()<=30))
			environment.pause();
		
		// If the mouse is over the play button
		if ((event.getX()>=35) && 
			(event.getY()>=canvas.getSize().height-30) && 
			(event.getY()<=canvas.getSize().height-10) && 
			(event.getX()<=55))
			environment.play();
		
		// If the mouse is over the speed region
		if ((event.getY()>=canvas.getSize().height-30) && 
			(event.getY()<=canvas.getSize().height-10) &&
			(event.getX()>=70) && 
			(event.getX()<=190)) {
			
			if (event.getX()<92) environment.setSpeed(1);
			else if (event.getX()<117) environment.setSpeed(2);
			else if (event.getX()<142) environment.setSpeed(3);
			else if (event.getX()<167) environment.setSpeed(4);
			else environment.setSpeed(5);
		}
		
 		if ((event.getX()>=5) && 
			(event.getY()>=35+canvas.getMaxBarHeight()) && 
			(event.getY()<=55+canvas.getMaxBarHeight()) && 
			(event.getX()<=canvas.getSize().width-5)) {
 				int position = event.getX()-5;
 				float step = (float)(canvas.getSize().width-10)/(float)model.getAgentsI().size();
 				int i;
 				for (i=1; i<=model.getAgentsI().size();i++)
 					if (position<step*i) break;
				canvas.agentIDs.get(i-1).toggleHighlighting();
 		}
	}
	
	
	/**
	 * Display hand cursor image over clickable regions. 
	 * @param event 			The event generated by the GUI.
	 */
	public void mouseMoved(MouseEvent event) {
		int height = canvas.getSize().height;
		int width = canvas.getSize().width;
		
		// Look for clickable areas.
		if ( 
		((event.getY()>=height-30) && 
		 (event.getY()<=height-10) &&
		 (event.getX()>=70) && 
		 (event.getX()<=190)) || 

		((event.getX()>=35) && 
		 (event.getY()>=height-30) && 
		 (event.getY()<=height-10) && 
		 (event.getX()<=55)) ||
		
		((event.getX()>=10) && 
		 (event.getY()>=height-30) && 
		 (event.getY()<=height-10) && 
		 (event.getX()<=30)) ||
		 
		((event.getX()>=10) && 
		 (event.getY()>=height-45) && 
		 (event.getY()<=height-35) && 
		 (event.getX()<=width-10)) ||
		 
		((event.getX()>=5) && 
		 (event.getY()>=35+canvas.getMaxBarHeight()) && 
		 (event.getY()<=55+canvas.getMaxBarHeight()) && 
		 (event.getX()<=width-5))
		
		) {
			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else { 
			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}