/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	EnergyBars.java - agent and team energy visualizer class
	
*******************************************************************************/


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.util.ArrayList;


/** 
 * This class extends the Canvas class implementing all drawing operations to 
 * display the agent energy bars on the right side of the main frame.
 *
 * @author Peter Eredics
 */
public class EnergyBars extends Canvas{
	/** Reference to the model object of the simulator/player. */
	ModelInterface model;
	
	/** Reference for the environment - enables seeking in player mode */
	EnvironmentInterface environment;
	
	/** The max of the scale for agent energy levels. */
	int agentMaxValue = 10000;
	
	/** The max of the scale for team energy levels. */
    int teamMaxValue = 0;
       
    /** ID for serialization. */
	private static final long serialVersionUID = 1L;
	
	/** List of registered agents in the game */
	public ArrayList<Agent> agentIDs;
	
	/** Indicates if the agents have been already registered into the agentIDs. */
	private boolean agentIDsFilled = false;
	
	/** Percent of difference between teams meaning drawn  */
	private float drawnFactor = 0;

    
	/**
	 * The constructor setting the local model and config variables.
	 * @param model			The model object of the application.
	 * @param environment 	The game environment
	 * @param config		The game configuration
	 */
    public EnergyBars(ModelInterface model, EnvironmentInterface environment, GameConfig config) {
        this.model = model;
        this.environment = environment;
        EnergyBarListener listener = new EnergyBarListener(this,environment,model);
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
        agentIDs = new ArrayList<Agent>();
        
        drawnFactor = 1f-(float)config.getInt("DrawnPercent")/100f;
    }
    
    
    /**
     * Calculate the maximal bar height (both for agents and for teams)
     * @return				The maximal bar height to display.
     */
    public int getMaxBarHeight() {
    	return getSize().height/2-30-40-10-40;
    }
    
    
    /**
     * This method draws all content to the canvas.
     */
    @Override
    public void repaint(){  	
    	// Only update if there are agents in the system 
        if ((model.getAgentsI().size()>0)&&(getSize().width>0))  {
        	// Prepare double buffering
            Image offscreen = createImage(getSize().width, getSize().height);
            Graphics offg = offscreen.getGraphics();
            offg.setColor(Color.BLACK);
            offg.fillRect(0, 0, getSize().width, getSize().height);

            // Calculate agent bar and space values between bars for agents
            float agentWidth = (float)this.getSize().width/(float)model.getAgentsI().size();
            int barWidth = (int)((float)agentWidth * 0.9f);
            int spaceWidth =  (int)((float)agentWidth * 0.1f);
            int barTop = 30;
            int maxBarHeight = getMaxBarHeight();
            
            // Update scale maximum value and calculate scaling ratio 
            while (Agent.maxEnergyAgent.getEnergy()>agentMaxValue) agentMaxValue += 5000;
            float energyFactor = (float)maxBarHeight / (float)agentMaxValue;
            
            // Draw scale
            offg.setColor(new Color(60,60,60));
            //offg.setColor(Color.WHITE);
            for (float line=0f; line<=agentMaxValue; line += 1000f)
            	offg.drawLine(0, barTop+maxBarHeight-(int)(line*energyFactor), (int)(getSize().width), barTop+maxBarHeight-(int)(line*energyFactor));            

            // Process agents team by team: teams, names and bars first
            String lastTeamName = "";
            int maxTeamEnergy = 0;
            Color maxTeamColor = Color.GRAY;
            int barLeft = 5;
            for (int tId = 0; tId<model.getTeamsI().size(); tId++) {
                Team aTeam = model.getTeamsI().get(tId);
                // Save maximal team energy for the scaling of the team bars
                if (aTeam.getTotalEnergy()>maxTeamEnergy) {
                	maxTeamEnergy = aTeam.getTotalEnergy();
                	maxTeamColor  = aTeam.color;
                }

                // Display agent bars one by one
                for (int i=0; i<aTeam.members.size(); i++) {
                    Agent agent =  aTeam.members.get(i);
                    if (!agentIDsFilled) agentIDs.add(agent);
                    offg.setColor(agent.color);
                    offg.fillRect(barLeft, barTop+maxBarHeight-(int)(agent.getEnergy()*energyFactor), barWidth-10, (int)(agent.getEnergy()*energyFactor));
                    offg.setColor(Color.WHITE);
                    offg.setFont(new Font("Arial", Font.BOLD, 12));
                    offg.drawString(agent.label, barLeft, barTop+maxBarHeight+20);
                    if (!lastTeamName.equals(agent.team.name)) {
                    	// Print team name if new team is started
                        offg.drawString(agent.team.name, barLeft, barTop+maxBarHeight+40);
                    }
                    lastTeamName = agent.team.name;
                    barLeft+=barWidth+spaceWidth;
                }
            }
            agentIDsFilled = true;
            
            // Energy level values second    
            barLeft = 5;
            offg.setColor(Color.WHITE);
            for (int tId = 0; tId<model.getTeamsI().size(); tId++) {
                Team aTeam = model.getTeamsI().get(tId);
                for (int i=0; i<aTeam.members.size(); i++) {
                    Agent agent =  aTeam.members.get(i);             
                    offg.setFont(new Font("Arial", Font.PLAIN, 10));
                    offg.drawString(String.valueOf(agent.getEnergy()), barLeft, barTop+maxBarHeight-(int)(agent.getEnergy()*energyFactor)-10);
                    barLeft+=barWidth+spaceWidth;
                }
            }

            // Calculate agent bar and space values between bars for teams
            float teamWidth = (float)this.getSize().width/(float)model.getTeamsI().size();
            barWidth = (int)(teamWidth * 0.9f);
            spaceWidth =  (int)(teamWidth * 0.1f);
            barLeft = 5;
            barTop = getSize().height/2+30;
            
            // Update scale maximum value and calculate scaling ratio 
            while  (maxTeamEnergy>teamMaxValue-1000) teamMaxValue+= 1000;
            energyFactor = (float)maxBarHeight / (float)teamMaxValue;
            
            // Draw drawn line
            offg.setColor(maxTeamColor);
            offg.drawLine(barLeft+barWidth-10, barTop+maxBarHeight-(int)(maxTeamEnergy*drawnFactor*energyFactor), barLeft+barWidth+spaceWidth, barTop+maxBarHeight-(int)(maxTeamEnergy*drawnFactor*energyFactor));
            
            // Process teams one by one
            for (int tId = 0; tId<model.getTeamsI().size(); tId++) {
                Team aTeam = model.getTeamsI().get(tId);
                int totalEnergy = aTeam.getTotalEnergy();
                offg.setColor(aTeam.color);
                offg.fillRect(barLeft, barTop+maxBarHeight-(int)(totalEnergy*energyFactor), barWidth-10, (int)(totalEnergy*energyFactor));
                offg.setColor(Color.WHITE);
                offg.setFont(new Font("Arial", Font.PLAIN, 10));
                offg.drawString(String.valueOf(totalEnergy), barLeft, barTop+maxBarHeight-(int)(totalEnergy*energyFactor)-10);
                offg.setFont(new Font("Arial", Font.BOLD, 12));
                offg.drawString(aTeam.name, barLeft, barTop+maxBarHeight+20);                
                barLeft+=barWidth+spaceWidth;      
            }
                        
            // Print round information and bar if round count is limited
            offg.setColor(new Color(30,30,30));
            offg.fillRect(10, getSize().height-45, (int)(float)(getSize().width-20), 5);
            offg.setColor(Color.WHITE);
            offg.fillRect(10, getSize().height-45, (int)((float)(getSize().width-20)*(float)model.getRound()/(float)model.getRoundLimit()), 5);
            offg.setFont(new Font("Arial", Font.PLAIN, 10));
            offg.drawString("Round: "+model.getRound(), 10, getSize().height-50);
            
            // Print playback and speed controls
            offg.setColor(new Color(30,30,30));
            
            offg.fillRect(10, getSize().height-30, 20, 20);           
            offg.fillRect(35, getSize().height-30, 20, 20);            
            offg.fillRect(70, getSize().height-15, 20, 5);            
            offg.fillRect(95, getSize().height-15, 20, 5);            
            offg.fillRect(120, getSize().height-15, 20, 5);            
            offg.fillRect(145, getSize().height-15, 20, 5);            
            offg.fillRect(170, getSize().height-15, 20, 5);
            
            if (environment.isDone()) offg.setColor(new Color(60,60,60));
            else offg.setColor(Color.WHITE);
            int[] xs = new int[3];
            int [] ys = new int[3];
            
            // Pause / step button
            if (environment.isPaused()) {
            	xs[0] = 16;
            	xs[1] = 21;
            	xs[2] = 16;
            	ys[0] = getSize().height-26;
            	ys[1] = getSize().height-20;
            	ys[2] = getSize().height-15;
            	offg.fillPolygon(new Polygon(xs,ys,3));
            	offg.fillRect(21,getSize().height-26,3,11);
            } else {
            	offg.fillRect(16,getSize().height-26,3,11);
            	offg.fillRect(21,getSize().height-26,3,11);
            }
            
            // Play button
            if (environment.isPaused()&&!environment.isDone()) {
            	offg.setColor(Color.WHITE);
            } else {
            	offg.setColor(new Color(60,60,60));
            }
            xs[0] = 43;
        	xs[1] = 48;
        	xs[2] = 43;
        	ys[0] = getSize().height-26;
        	ys[1] = getSize().height-20;
        	ys[2] = getSize().height-15;
        	offg.fillPolygon(new Polygon(xs,ys,3));
            
        	// Speed bars
        	int speed = environment.getSpeed(); 
        	if (speed>0) {
        		offg.setColor(new Color(25,154,12));
                offg.fillRect(70, getSize().height-14, 20, 4);            
        	}
        	if (speed>1) {
        		offg.setColor(new Color(155,239,51));
        		offg.fillRect(95, getSize().height-18, 20, 8);          
        	}
        	if (speed>2) {
        		offg.setColor(new Color(209,231,51));
        		offg.fillRect(120, getSize().height-22, 20, 12);                
        	}
        	if (speed>3) {
        		offg.setColor(new Color(237,154,42));
        		offg.fillRect(145, getSize().height-26, 20, 16);          
        	}
        	if (speed>4) {
        		offg.setColor(new Color(237,15,22));
        		offg.fillRect(170, getSize().height-30, 20, 20);   
        	}
        	          
            offg.setColor(Color.WHITE);
            offg.setFont(new Font("Arial", Font.PLAIN, 10));
            offg.drawString("Speed:", 70, getSize().height-20);

            // Display the image from the background buffer
            this.getGraphics().drawImage(offscreen, 0, 0, this);
        }
    }
}