/*******************************************************************************
	
	AgentGame 15.00.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2015.
	
	GameView.java - GUI class
	
*******************************************************************************/


import java.awt.*;
import java.awt.event.*;
import com.jogamp.opengl.util.*;
import javax.media.opengl.*;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;


/**
 * The GUI object of the application displaying the OpenGL powered world view
 * 
 * @author Peter Eredics
 */
public class GameView extends Frame implements GLEventListener
{
	/** The environment of the simulation */
	private EnvironmentInterface environment;
	
	/** The model of the game world */
    private ModelInterface model;
    
    /** The logger object of the application - the log has to be closed when exiting */
    private CSVLog csvLog;
    
    /** The step logger opject - the log has to be closed when exiting */
    private RoundLog stepLog;
    
    /** The OpenGL canvas object */
    public GLCanvas canvas;
    
    /** The thread responsible for drawing - it has to sleep to slow down animation */ 
    private Thread drawThread;
       
    /** The EnergyBars object displaying inforamtion on the right side */ 
    private EnergyBars energyBars;
    
	/** ID for object serialization */
	private static final long serialVersionUID = 1L;
    
	/** The OpenGL animator thread */
	private Animator animator;
	
	
    /**
     * The constructor setting all references to important game object   
     * @param environment 	The controller class of the application
     * @param model			The model to draw the world from
     * @param config		The configuration object holding GUI configuration
     * @param playBack 		Is the GUI in playback mode?
     * @param titleTag 		Additional string tag to diplay in the header of the frame
     */
    public GameView(EnvironmentInterface environment, ModelInterface model, GameConfig config, boolean playBack, String titleTag)
    {
        // Create and init the window
        super("AgentGame "+GameEnvironment.version+titleTag);
        setLayout(new BorderLayout());
        setSize(config.getInt("WindowWidth"), config.getInt("WindowHeight"));
        this.setLocation(1, 1);
        if (config.getInt("WindowMaximized")==1) this.setExtendedState(MAXIMIZED_BOTH);
        setVisible(true);
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent we){
            	// Halt application when the window is closed
            	halt();
            }
        });

        // Init data structure references and variables
        this.environment = environment;
        this.model   = model;

        // Init OpenGL
        GLProfile profile = GLProfile.get(GLProfile.GL2);
    	GLCapabilities capabilities = new GLCapabilities(profile);
    	capabilities.setDoubleBuffered(true);
    	capabilities.setHardwareAccelerated(true);
        canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);

        // Init window layout
        Panel rightPanel = new Panel();
        rightPanel.setPreferredSize(new Dimension(200, 100));
        rightPanel.setLayout(new BorderLayout());
        canvas.setMinimumSize(new Dimension(700, 700));
        rightPanel.setMinimumSize(new Dimension(100, 700));
        add(rightPanel, BorderLayout.LINE_END);
        add(canvas, BorderLayout.CENTER);

        // Init OpenGL animator thread
        animator = new Animator(canvas);
        animator.start();
        
        // Charts pane on the right side: top and bottom labels and buttons
        energyBars = new EnergyBars(model, environment, config);
              
        Panel rightTop = new Panel();        
        rightTop.setLayout(new BorderLayout());
        Label label1 = new Label("AgentGame "+GameEnvironment.version);
        Label label2 = new Label("© Peter Eredics / BUTE-DMIS 2010-2015");
        label1.setBackground(Color.BLACK);        
        label1.setForeground(Color.WHITE);
        label1.setFont(new Font("Arial",Font.BOLD,12));
        label1.setAlignment(1);
        label2.setBackground(Color.BLACK);
        label2.setForeground(Color.WHITE);
        label2.setFont(new Font("Arial",Font.BOLD,10));
        rightTop.add(label1, BorderLayout.PAGE_START);
        rightTop.add(label2, BorderLayout.PAGE_END);
        rightTop.setBackground(Color.BLACK);      
        
        rightPanel.add(rightTop, BorderLayout.PAGE_START);
        rightPanel.add(energyBars, BorderLayout.CENTER);
        //rightPanel.add(rightBottom, BorderLayout.PAGE_END);
    }
    
    
    /**
     * Called by the drawable immediately after the OpenGL context is
     * initialized; the GLContext has already been made current when
     * this method is called.
     * 
     * @param GLDrawable	The display context to render to 
     */
    public void init(GLAutoDrawable GLDrawable)
    {
        GL2 gl = GLDrawable.getGL().getGL2();

        // Set the projection matrix
        gl.glClearColor(0, 0, 0, 0);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, 1, 0, 1, -1, 1);

        // Enable blending
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    }

    
    /**
     * Called by the drawable when the surface resizes itself. Used to
     * reset the viewport dimensions.
     *
     * @param drawable The display context to render to
     * @param x 
     * @param y 
     * @param width 
     * @param height 
     * 
     */
    public void reshape(GLDrawable drawable,
                        int x,
                        int y,
                        int width,
                        int height)
    {
    	// Nothing to do here
    }

    
    /**
     * Called by the drawable when the display mode or the display device
     * associated with the GLDrawable has changed
     * @param drawable 
     * @param modeChanged 
     * @param deviceChanged 
     */
    public void displayChanged(GLDrawable drawable,
                               boolean modeChanged,
                               boolean deviceChanged)
    {
    	// Nothing to do here
    }

    
    /**
     * Called by the drawable to perform rendering by the client.
     * 
     * @param GLDrawable 	The display context to render to
     */
    public void display(GLAutoDrawable GLDrawable)
    {
    	// Init
    	GL2 gl = GLDrawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        // Draw background
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3f(1, 1, 1);
        gl.glVertex3f(0, 0, 0);
        gl.glVertex3f(0, 1, 0);
        gl.glVertex3f(1, 1, 0);
        gl.glVertex3f(1, 0, 0);
        gl.glEnd();
        
        // Draw waters
        gl.glColor4f(0.5f, 0.75f, 1f, 1f);
        
        for (int waterId=0; waterId<model.getWatersI().size();waterId++) {
        	Water aWater = model.getWatersI().get(waterId);
	        gl.glBegin(GL2.GL_QUADS);
	        gl.glVertex3f(getGX(aWater.position,0), getGY(aWater.position,0), 1);
	        gl.glVertex3f(getGX(aWater.position,aWater.width), getGY(aWater.position,0), 1);
	        gl.glVertex3f(getGX(aWater.position,aWater.width), getGY(aWater.position,aWater.height), 1);
	        gl.glVertex3f(getGX(aWater.position,0), getGY(aWater.position,aWater.height), 1);
	        gl.glEnd();
        }
        
        // Draw grid
        gl.glBegin(GL.GL_LINES);
        gl.glColor3f(0, 0, 0);
        for (int x = 0; x<model.getMapWidth(); x++) {
            gl.glVertex3f((float)x/(float)model.getMapWidth(), 0, 0);
            gl.glVertex3f((float)x/(float)model.getMapWidth(), 1, 0);
        }
        for (int y = 0; y<model.getMapHeight(); y++) {
            gl.glVertex3f(0,(float)y/(float)model.getMapHeight(), 0);
            gl.glVertex3f(1,(float)y/(float)model.getMapHeight(), 0);
        }
        gl.glEnd();

        
        if (environment.isReady()) {	
	        // Draw food
	        float maxFoodValue = model.getMaxFoodValue();
	        for (int foodId=0; foodId<model.getFoodI().size(); foodId++) {
	            Food aFood = model.getFoodI().get(foodId);
	            gl.glColor4f(1, 0, 1, aFood.value/maxFoodValue);
	            gl.glBegin(GL2.GL_QUADS);
	            gl.glVertex3f(getGX(aFood.position,0), getGY(aFood.position,0), 1);
	            gl.glVertex3f(getGX(aFood.position,1), getGY(aFood.position,0), 1);
	            gl.glVertex3f(getGX(aFood.position,1), getGY(aFood.position,1), 1);
	            gl.glVertex3f(getGX(aFood.position,0), getGY(aFood.position,1), 1);
	            gl.glEnd();
	            
	            gl.glColor3f(1, 0, 1);
	            gl.glBegin(GL.GL_LINE_LOOP);
	            gl.glVertex3f(getGX(aFood.position,0f), getGY(aFood.position,0f), 1);
	            gl.glVertex3f(getGX(aFood.position,1f), getGY(aFood.position,0f), 1);
	            gl.glVertex3f(getGX(aFood.position,1f), getGY(aFood.position,1f), 1);
	            gl.glVertex3f(getGX(aFood.position,0f), getGY(aFood.position,1f), 1);
	            gl.glEnd();
	        }
	        
	        // Draw agents
	        for (int aId=0; aId<model.getAgentsI().size(); aId++) {
	            Agent agent = model.getAgentsI().get(aId);
	
	            // Agent body
	            gl.glBegin(GL2.GL_QUADS);
	            gl.glColor4f((float)agent.color.getRed()/(float)255, (float)agent.color.getGreen()/(float)255, (float)agent.color.getBlue()/(float)255,1);
	            gl.glVertex3f(getGX(agent.position,0), getGY(agent.position,0), 1);
	            gl.glVertex3f(getGX(agent.position,1), getGY(agent.position,0), 1);
	            gl.glVertex3f(getGX(agent.position,1), getGY(agent.position,1), 1);
	            gl.glVertex3f(getGX(agent.position,0), getGY(agent.position,1), 1);
	            gl.glEnd();
	
	            // Agent field of view
	            gl.glBegin(GL.GL_TRIANGLES);            
	            gl.glColor4f((float)agent.color.getRed()/255f,(float)agent.color.getGreen()/255f,(float)agent.color.getBlue()/255f,0.2f);
	            switch (agent.direction) {
	                case 0:
	                	// Up
	                    gl.glVertex3f(getGX(agent.position,0.5f),                  getGY(agent.position,0.5f), 0);
	                    gl.glVertex3f(getGX(agent.position,model.getViewDistance()+1),  getGY(agent.position,-1*model.getViewDistance()), 0);
	                    gl.glVertex3f(getGX(agent.position,-1*model.getViewDistance()), getGY(agent.position,-1*model.getViewDistance()), 0);
	                    break;
	                case 1:
	                	// Right
	                    gl.glVertex3f(getGX(agent.position,0.5f),                  getGY(agent.position,0.5f), 0);
	                    gl.glVertex3f(getGX(agent.position,model.getViewDistance()+1),  getGY(agent.position,model.getViewDistance()+1), 0);
	                    gl.glVertex3f(getGX(agent.position,model.getViewDistance()+1),  getGY(agent.position,-1*model.getViewDistance()), 0);
	                    break;
	                case 2:
	                	// Down
	                    gl.glVertex3f(getGX(agent.position,0.5f),                  getGY(agent.position,0.5f), 0);
	                    gl.glVertex3f(getGX(agent.position,model.getViewDistance()+1),  getGY(agent.position,model.getViewDistance()+1), 0);
	                    gl.glVertex3f(getGX(agent.position,-1*model.getViewDistance()), getGY(agent.position,model.getViewDistance()+1), 0);
	                    break;
	                case 3:
	                	// Left
	                    gl.glVertex3f(getGX(agent.position,0.5f),                  getGY(agent.position,0.5f), 0);
	                    gl.glVertex3f(getGX(agent.position,-1*model.getViewDistance()), getGY(agent.position,model.getViewDistance()+1), 0);
	                    gl.glVertex3f(getGX(agent.position,-1*model.getViewDistance()), getGY(agent.position,-1*model.getViewDistance()), 0);
	                    break;
	            }
	            gl.glEnd();
	        }
        }
        gl.glFlush();       
        
        // Update the energy bars on the right
        if (energyBars!=null) energyBars.repaint();

        // Sleep the drawing thread to avoid high CPU load
        drawThread = Thread.currentThread();
        try{
        	Thread.sleep(10);
        } catch(Exception ie){}
    }

    
    /**
     * Force screen update by waking up the drawing thread when JASON
     * finished a round.
     */
    public void updateScreen() {
        if (drawThread!=null) drawThread.interrupt();
    }
    
    
    /**
     * Get the X coordinate (optionally with offset) in the OpenGL 
     * coordinate plane.
     * 
     * @param position	The position to transform X value from
     * @param offset	Optional offset value.
     * @return			The X coordinate on the OpenGL plane
     */
    public float getGX(Position position, float offset) {
        return ((float)position.x+offset)/(float)model.getMapWidth();
    }

    
    /**
     * Get the Y coordinate (optionally with offset) in the OpenGL 
     * coordinate plane.
     * 
     * @param position	The position to transform Y value from
     * @param offset	Optional offset value.
     * @return			The Y coordinate on the OpenGL plane
     */
    public float getGY(Position position, float offset) {
        return 1.0f-(((float)position.y+offset)/(float)model.getMapHeight());
    }
    
    
    /**
     * Called when the form is being closed: the log is closed and the
     * application shuts down.
     */
    public void halt() {
    	animator.stop();
    	if (csvLog !=null) csvLog.closeLogs();
    	if (stepLog!=null) stepLog.closeLog();
    	System.exit(0);
    }

    
    /**
     * Dispose the display context 
     * 
     * @param arg0 		The display context
     */
	public void dispose(GLAutoDrawable arg0) {
		// Nothing to do here
		
	}

	
    /**
     * Reshape the display context
     * 
     * @param arg0 		The display context
     * @param arg1  
     * @param arg2 
     * @param arg3 
     * @param arg4 
     */
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
		// Nothing to do here
		
	}
}

