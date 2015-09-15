/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	Player.java - replay controller for logged games
	
*******************************************************************************/


/**
 * The controller class of the player application  
 * 
 * @author Peter Eredics
 */
public class Player implements EnvironmentInterface {
	/** The model to read game states from */
	private ReplayModel model;
	
	/** The GUI */
	private GameView view;
	
	/** The player's configuration */
	private GameConfig config;
	
	/** Is the playback paused? */
	private boolean paused;
	
	/** The animating thread */
	private StepThread thread = new StepThread(this);
	
	/** The minimal sleep time of the animating thread in player mode */
	public final int minSleepTime = 5;
	
	/** Speed level of the playback*/
	private int speed;
	
	/** Is the player in debug mode? */
	private static boolean debugging = true;
	
	
	/**
	 * Initilize the player application
	 * @param args		Command line arguments (filename to read replay log from)
	 */
	public static void main(String[] args) {
		System.out.println("AgentGame player initializing...");
		if (args.length != 1) {
			System.out.println("No replay log file specified on command line. Exiting.");
			try {Thread.sleep(6000);} catch (InterruptedException e) {e.printStackTrace();}
			System.exit(1);
		}
		
		System.out.println("Creating controller class...");
		Player player = new Player();
		
		System.out.println("Loading configuration from Player.conf...");
		player.config = new GameConfig("Player.conf");
		player.speed = player.config.getInt("PlayerSpeed");
		player.paused = (player.config.getInt("AutoPlay")!=1);
		
		System.out.println("Load model from "+args[0]+"...");
		player.model  = new ReplayModel(args[0]);
		
		System.out.println("Creating GUI...");
		player.view   = new GameView(player,player.model,player.config,true," - Player");
		player.view.updateScreen();
		player.view.setSize(player.view.getWidth()+1, player.view.getHeight());

		System.out.println("Creating animator thread...");
		player.thread.start();
		
		System.out.println("AgentGame player ready.");
	}
	
	
	/**
	 * Step the animation with a single step to the given direction
	 */
	public void step() {
		// Make step
		model.selectRound(model.getRound()+1);
		
		// Pause at the end of the game
		if (((model.getRound()+1)>model.roundMax+1)) paused = true;
	}
	
	
	/**
	 *	Animation thread class. 
	 */
	class StepThread extends Thread {
		/** The controller class of the animation */
		Player player;
		
		
		/**
		 * Constructor setting internal references
		 * @param player	The controller class of the animation
		 */
		public StepThread(Player player) {
			this.player = player;
		}
		
		
		/**
		 * The main thread process runs the animation (if it is not paused) 
		 * and sleeps 
		 */
		 public void run() {
			 while (true) {
	             if (!player.paused)player.step();
	             try {
	            	 Thread.sleep(Math.round(Math.pow(5-speed,3)*10));
				} catch (InterruptedException e) {}
			 }
         }
	}

	
	/**
	 * @see EnvironmentInterface#seekToPercent(float)
	 */
	public void seekToPercent(float percent) {
		model.selectRound((int)((float)model.roundMax*percent/100f));		
	}


	/**
	 * @see EnvironmentInterface#pause()
	 */
	public void pause() {
		if (!paused)
			paused = true;
		else 
			step();
		
	}


	/**
	 * @see EnvironmentInterface#play()
	 */
	public void play() {
		paused = false;
	}

	/**
	 * @see EnvironmentInterface#isPaused()
	 */
	public boolean isPaused() { return paused;}
	
	
	/**
	 * @see EnvironmentInterface#isDone()
	 */
	public boolean isDone() { return false;}

	
	/**
	 * @see EnvironmentInterface#getSpeed()
	 */
	public int getSpeed() { return speed; }


	/**
	 * Set the speed of the playback.
	 * @param speed			New speed level (1-5) of the playback.
	 */
	public void setSpeed(int speed) {
		if (speed>5) this.speed = 5;
		else if (speed<1) this.speed = 1;
		else this.speed = speed;
	}
	
	
	/**
	 * @see EnvironmentInterface#isReady()
	 */
	public boolean isReady() { return true; }
	
	
	/**
	 * Write message to the standard output if debugging is enabled
	 * @param message			The message to display in the standard ouput.
	 */
	public static void debug(String message) {
		if (debugging) System.out.println(message);
	}	
}