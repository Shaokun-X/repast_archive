package forest_fire;

import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;

public class ForestFireBuilder implements ContextBuilder<Object>{

	@Override
	public Context<Object> build(Context<Object> context) {
		/*
		 * Initialize grid and network
		 */
		context.setId("forest_fire");	
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("water spray", context, true);
		netBuilder.buildNetwork();		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(
				new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, 50, 50));
		
		/*
		 * Initialize forest and fire fighters
		 */
		
		// parameters
		int forestLife = 300;
		int squadNumber = 1;
		GridPoint fireSpot = new GridPoint(15, 15);
		
		// Add forest
		for (int i = 10; i < 38; i++) {
			for (int j = 7; j < 25; j++) {
				Forest newForest;
				newForest = new Forest(grid, forestLife);
				if (i==15&&j==15)
					newForest.setOnFire();
				context.add(newForest);
				grid.moveTo(newForest, i, j);
			}
		}
		for (int i = 6; i < 24; i++) {
			for (int j = 25; j < 40; j++) {
				Forest newForest;
				newForest = new Forest(grid, forestLife);	
				context.add(newForest);
				grid.moveTo(newForest, i, j);
			}
		}
		for (int i = 24; i < 42; i++) {
			for (int j = 32; j < 45; j++) {
				Forest newForest;
				newForest = new Forest(grid, forestLife);	
				context.add(newForest);
				grid.moveTo(newForest, i, j);
			}
		}
		
		// Set on initial fire
		for (Object obj : grid.getObjectsAt(fireSpot.getX(), fireSpot.getY())) {
			if (obj instanceof Forest)
				((Forest) obj).setOnFire();
		}
		
		// Add fire fighters
		for (int i = 0; i < squadNumber; i++) {
			FirefighterSquad squad = new FirefighterSquad(context, grid, i, fireSpot);
			// uncomment the line below to test new strategy
			// squad.setDispersion(true);
			context.add(squad);
			grid.moveTo(squad, 49, 49 - i);			
		}
		
		return context;
	}
}
