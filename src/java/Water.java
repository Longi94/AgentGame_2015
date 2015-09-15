/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	GameView.java - GUI class
	
*******************************************************************************/


/**
 * The class representing water objects on the map making agent movement 
 * more costly than usual.
 */
public class Water {
	/** The top left corner position of the water object */
	public Position position;
	
	/** Height of the water object */
	public int height;
	
	/** Width of the water object*/
	public int width;
	
	
	/**
	 * Constructor of the water object
	 * 
	 * @param position	The top left corner position of the water object
	 * @param height	Height of the water object
	 * @param width		Width of the water object
	 */
	public Water(Position position, int height, int width) {
		this.position = position;
		this.height   = height;
		this.width    = width;
	}
	
	
	/** 
	 * Static water generating method creating the necessary number of
	 * random water object in the model based on the given configuration
	 * 
	 * @param model		The model object to create maps in
	 * @param config	The config object containing necessary water count
	 */
	public static void generateWaters(GameModel model, GameConfig config) {	
		// Load configuration
		System.out.println("Generating waters...");
		int waterCount = config.getInt("WaterCount");
		if (waterCount==0) return;
		
		int waterCells = (model.map.width*model.map.height)*config.getInt("WaterCoveragePercent")/100;
		int waterSideAspect = config.getInt("WaterSideAspect");
		int avgWaterSize = waterCells/waterCount;		
		float avgSideWidth = Math.round(Math.sqrt(avgWaterSize));
		
		// Generate water objects one by one
		int x = 0;
		int y = 0;
		int side1 = 0;
		int side2 = 0;
		Water water;
		for (int i=0; i<waterCount; i++) {
			side1 = (int)Math.ceil(avgSideWidth / (1+Math.random()*waterSideAspect));
			side2 = avgWaterSize/side1;
			
			if (Math.random()>0.5) {
				// portrait mode
				x = (int)Math.round(Math.random()*(model.map.width-side1));
				y = (int)Math.round(Math.random()*(model.map.height-side2));
				water = new Water(new Position(x,y),side2, side1);
				model.watersI.put(model.watersI.size(), water);
				System.out.println("   - water "+i+" is at "+x+"x"+y+" with size "+side2+"x"+side1);
			} else {
				// landscape mode
				x = (int)Math.round(Math.random()*(model.map.width-side2));
				y = (int)Math.round(Math.random()*(model.map.height-side1));
				water = new Water(new Position(x,y),side1, side2);
				model.watersI.put(model.watersI.size(), water);
				System.out.println("   - water "+i+" is at "+x+"x"+y+" with size "+side1+"x"+side2);
			}
		}
		
		// Register water cells on the map
		for (int i=0; i<model.watersI.size();i++) {
			water = model.watersI.get(i);
			for (x=water.position.x;x<water.position.x+water.width;x++)
				for (y=water.position.y;y<water.position.y+water.height;y++)
					model.map.registerWaterCell(x,y);
		}
	}
}