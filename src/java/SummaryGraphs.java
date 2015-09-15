/*******************************************************************************
	
	AgentGame 13.10.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2013.
	
	SummarGraphs - Show graph summary at the end of the game
	
*******************************************************************************/


import java.io.File;
import java.io.IOException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


/**
 * This class records and displays summary data
 * 
 * @author Peter Eredics
 */
public class SummaryGraphs {
	/** The configuration of the game to read graph related parameters */
	private GameConfig config;
	
	/** Are the graphs enabled? */
	public boolean enabled = false;
	
	/** Are the graphs already displayed? */
	private boolean displayed = false;
	
	/** Series of agents */
	private XYSeries[] agentSeries;
	
	/** Series of teams */
	private XYSeries[] teamSeries;
	
	/** Number of agents in the game */
	private int agentCount;
	
	/** Number of teams in the game */
	private int teamCount;
	
	/** Number of steps data to record after */
	private int logInterval = 1;
	
	/** The log status */
	private int logStep = 9999;
	
	
	/**
	 * Default constructor setting the config reference variable
	 * @param config		The game configuration
	 */
	public SummaryGraphs(GameConfig config) {
		this.config = config;
		logInterval = config.getInt("GraphInterval");
	}
	
	
	/**
	 * Creating the series for the data
	 * @param model			The model to extract agent and team counts from
	 */
	public void initGraphs(GameModel model) {
		// Prepare agents
		agentCount = model.agentsI.size();
		agentSeries = new XYSeries[agentCount];
		for (int i = 0; i<agentCount; i++)
			agentSeries[i] = new XYSeries(model.agentsI.get(i).name);
		
		// Prepare teams
		teamCount = model.teamsI.size();
		teamSeries = new XYSeries[teamCount];
		for (int i = 0; i<teamCount; i++)
			teamSeries[i] = new XYSeries("Team "+model.teamsI.get(i).name);
	}
	
	
	/**
	 * Store data from the actual step of the game 
	 * @param step			The actual step
	 * @param model			The model to extract data from
	 */
	public void record(int step, GameModel model) {
		logStep++;
		if (logStep<logInterval) return;
		logStep = 0;
		
		// Agent data
		for (int i = 0; i<agentCount; i++) 
			agentSeries[i].add(step,model.agentsI.get(i).getEnergy());
		
		// Team data
		for (int i = 0; i<teamCount; i++)
			teamSeries[i].add(step, model.teamsI.get(i).getTotalEnergy());
	}
	
	
	/**
	 * Display the summary graphs
	 * @param showFrame 	If true, display the graph in a frame
	 */
	public void display(boolean showFrame){
		// Display only once
		if (displayed) return;
		else displayed = true;
		
		// Add series to the chart
		XYSeriesCollection dataSet = new XYSeriesCollection();
		
		// Add agents
		for (int i = 0; i<agentCount; i++) dataSet.addSeries(agentSeries[i]);

		// Add teams
		for (int i = 0; i<teamCount; i++) dataSet.addSeries(teamSeries[i]);
		
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart("",
		"Time (step)", "Energy", dataSet, PlotOrientation.VERTICAL, 
		true, false, false);
			
		// Export graph if set in configuration
		if (config.getInt("ExportGraphs", 0)==1) {
			try {
				ChartUtilities.saveChartAsPNG(new File(config.getString("ExportGraphsFile")), chart, 1024, 700);
			} catch (IOException e) {
				System.err.println("Problem occurred creating chart.");
			}
		}
		
		// Display the chart on a frame
		if (showFrame) {
			ChartFrame frame = new ChartFrame("Results", chart);
			frame.setSize(1024, 700);
			frame.setVisible(true);
		}
	}
}
