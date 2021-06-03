package forest_fire;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

public class FirefighterSquad {
	
	private Grid<Object> grid;
	private int id;
	private Context<Object> context;
	private ArrayList<AgentMessage> mailbox;
	
	private boolean dispersion = false;
	
	// to count how many more iterations the fire fighter need to put out fire 
	private int extinguishCount;
	
	// a squad can detect fire within this radius
	private final int FIRE_DETECT_RADIUS = 6;
	// a squad can extinguish all fire within the radius
	private final int EXTINGUISH_RADIUS = 2;
	// this is used to calculate the final destination of each move of squad.
	// the bigger this value is, the more likely the squad move to the fire;
	// the smaller this value is, the more likely the squad tries to keep distance with other squad
	// this value should always be greater than 0
	private final int FIRE_ATTRACTION_WEIGHT = 3;
	// how many iterations are needed to put out fire
	private final int EXTINGUISH_TIME = 2;
	
	private GridPoint destination;
	
	public FirefighterSquad(Context<Object> context, Grid<Object> grid, int id, GridPoint dest) {
		destination = dest;
		this.grid = grid;
		this.id = id;
		this.context = context;
		this.mailbox = new ArrayList<AgentMessage>();
	}
	
	public int getId() {
		return this.id;
	}
	
	public boolean isExtinguishing() {
		return getExtinguishCount() > 0 ? true : false;
	}
	
	public int getExtinguishCount() {
		return this.extinguishCount;
	}
	
	public void setExtinguishCount(int c) {
		if (c < 0)
			c = 0;
		if (c > EXTINGUISH_TIME)
			c = EXTINGUISH_TIME;
		this.extinguishCount = c;
	}
	
	public boolean isDispersion() {
		return dispersion;
	}

	public void setDispersion(boolean dispersion) {
		this.dispersion = dispersion;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void update() {
		send(new AgentMessage(id, grid.getLocation(this), assessFire()));
		if (hasFireEngaged() || isExtinguishing()) {
			putDownFire();
		} else {
			updateDestination();
			moveToDestination();
		}
	}
	
	private boolean testFire(int radius) {
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<Forest> nghCreator = new GridCellNgh<Forest>(grid, pt, Forest.class, radius, radius);
		List<GridCell<Forest>> gridCells = nghCreator.getNeighborhood(true);
		for (GridCell<Forest> cell : gridCells) {
			// Get neighbor Forest
			// Every cell there should exist only one Forest instance
			Forest neighbor = null;
			for(Object obj : cell.items()) {
				if (obj instanceof Forest) {
					neighbor = (Forest) obj;
					if (neighbor.isOnFire())
						return true;
				}
			}
		}
		return false;
	}
	
	private boolean hasFireEngaged() {
		return testFire(EXTINGUISH_RADIUS);
	}
	
	private boolean hasFireInSight() {
		return testFire(FIRE_DETECT_RADIUS);
	}
	
	private int assessFire() {
		GridPoint pt = grid.getLocation(this);
		
		GridCellNgh<Forest> nghCreator = new GridCellNgh<Forest>(grid, pt, Forest.class, FIRE_DETECT_RADIUS, FIRE_DETECT_RADIUS);
		List<GridCell<Forest>> gridCells = nghCreator.getNeighborhood(true);
		int fireSizeSum = 0;
		for (GridCell<Forest> cell : gridCells) {
			// Get neighbor Forest
			// Every cell there should exist only one Forest instance
			Forest neighbor = null;
			for(Object obj : cell.items()) {
				if (obj instanceof Forest) {
					neighbor = (Forest) obj;
					if (neighbor.isOnFire())
						fireSizeSum += neighbor.getFireSize();
				}
			}
		}

		// consider other squads in range, the more there are, the lower fire assessment is
//		GridCellNgh<FirefighterSquad> nghCreator_ = new GridCellNgh<FirefighterSquad>(grid, pt, FirefighterSquad.class, FIRE_DETECT_RADIUS, FIRE_DETECT_RADIUS);
//		List<GridCell<FirefighterSquad>> gridCells_ = nghCreator_.getNeighborhood(true);
//		int squadSum = 0;
//		for (GridCell<FirefighterSquad> cell : gridCells_) {
//			squadSum += cell.size();
//		}
		
		// quantification of fire assessment
//		int fireAssessment = fireSizeSum - squadSum * EXTINGUISH_RADIUS * FireLevel.SMALL;
		int fireAssessment = fireSizeSum;
		if (fireAssessment < 0 )
			fireAssessment = 0;
		
//		System.out.printf("id:%d fire assessment:%d\n", this.id, fireAssessment);
		
		return fireAssessment;
	}
	
	private void putDownFire() {
		
		Network<Object> net = (Network<Object>) context.getProjection("water spray");
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<Forest> nghCreator = new GridCellNgh<Forest>(grid, pt, Forest.class, EXTINGUISH_RADIUS, EXTINGUISH_RADIUS);
		List<GridCell<Forest>> gridCells = nghCreator.getNeighborhood(true);
		
		if (isExtinguishing()) {
			setExtinguishCount(getExtinguishCount() - 1);
			
			if (getExtinguishCount() == 0) {
				for (GridCell<Forest> cell : gridCells) {
					// Get neighbor Forest
					// Every cell there should exist only one Forest instance
					Forest neighbor = null;
					for(Object obj : cell.items()) {
						if (obj instanceof Forest) {
							neighbor = (Forest) obj;
							if (neighbor.isOnFire()) {
								neighbor.putOutFire();
							}
						}
					}
				}
				List<RepastEdge<Object>> edgesToBeDeleted = new ArrayList<RepastEdge<Object>>();
				for (RepastEdge<Object> edge : net.getOutEdges(this)) {
					edgesToBeDeleted.add(edge);
				}
				for (RepastEdge<Object> edge : edgesToBeDeleted) {
					net.removeEdge(edge);
				}				
			}
			
		} else {
			for (GridCell<Forest> cell : gridCells) {
				// Get neighbor Forest
				// Every cell there should exist only one Forest instance
				Forest neighbor = null;
				for(Object obj : cell.items()) {
					if (obj instanceof Forest) {
						neighbor = (Forest) obj;
						if (neighbor.isOnFire()) {
							net.addEdge(this, neighbor);
						}
					}
				}
			}
			setExtinguishCount(EXTINGUISH_TIME);
		}
	}
	
	private void updateDestination() {
		// use vector to decide direction
		// move to the fires while avoiding other squads
		if (hasFireInSight()) {
			
			// get a fire randomly among those bigger than middle size
			GridCellNgh<Forest> nghCreator = new GridCellNgh<Forest>(grid, grid.getLocation(this), Forest.class, FIRE_DETECT_RADIUS, FIRE_DETECT_RADIUS);
			List<GridCell<Forest>> forestCells = nghCreator.getNeighborhood(true);
			SimUtilities.shuffle(forestCells, RandomHelper.getUniform());
			
			Forest towardsFire = null;
			for (GridCell<Forest> cell : forestCells) {
				for(Object obj : cell.items()) {
					if (obj instanceof Forest) {
						Forest fire = (Forest) obj;
						if (fire.isOnFire()) {
							if (fire.getFireSize() >= FireLevel.MIDDLE) {
								towardsFire = fire;
							}
						}
					}
				}
			}
			// if no fire bigger than middle size, randomly choose one
			if (towardsFire == null) {
				for (GridCell<Forest> cell : forestCells) {
					for(Object obj : cell.items()) {
						if (obj instanceof Forest) {
							Forest fire = (Forest) obj;
							if (fire.isOnFire()) {							
								towardsFire = fire;
							}
						}
					}
				}
			}
			
			// get other squads
			List<FirefighterSquad> squads = new ArrayList<FirefighterSquad>();
			GridCellNgh<FirefighterSquad> nghCreator_ = new GridCellNgh<FirefighterSquad>(grid, grid.getLocation(this), FirefighterSquad.class, FIRE_DETECT_RADIUS, FIRE_DETECT_RADIUS);
			List<GridCell<FirefighterSquad>> squadCells = nghCreator_.getNeighborhood(true);
			for (GridCell<FirefighterSquad> cell : squadCells) {
				// Get neighbor Forest
				for(Object obj : cell.items()) {
					if (obj instanceof FirefighterSquad) {
						FirefighterSquad squad = (FirefighterSquad) obj;
						squads.add(squad);
					}
				}
			}
			
			int vectorX = 0;
			int vectorY = 0;
			GridPoint currentPt = grid.getLocation(this);
			GridPoint firePt = grid.getLocation(towardsFire);
			
			// avoid other squad
			if (isDispersion()) {
				for (FirefighterSquad squad : squads) {
					GridPoint pt = grid.getLocation(squad);
					vectorX -= pt.getX();
					vectorY -= pt.getY();
				}
				// calculate destination
				vectorX += currentPt.getX() * squads.size();
				vectorY += currentPt.getY() * squads.size();
			}

			
			// towards biggest fire
			vectorX += firePt.getX() * FIRE_ATTRACTION_WEIGHT;
			vectorY += firePt.getY() * FIRE_ATTRACTION_WEIGHT;
			vectorX -= (FIRE_ATTRACTION_WEIGHT - 1) * currentPt.getX();
			vectorY -= (FIRE_ATTRACTION_WEIGHT - 1) * currentPt.getY();
			
			// here vectorX and vectorY are already the destination coordinates
			// rather than displacement vectors
			destination = new GridPoint(vectorX, vectorY);
			
		} else {
			// go help others if no fire in detect range
			AgentMessage msg = randomRead();
			if (msg != null && msg.getFireAssessment() > 0) {
				destination = msg.getSenderPosition();
			}
			// old messages are useless
			flushMailbox();
		}
//		System.out.println(destination);
	}
	
	private void moveToDestination() {
		
		if (destination == null)
			return;
		
		GridPoint currentPt = grid.getLocation(this);

		int deltaX = destination.getX() - currentPt.getX();
		int deltaY = destination.getY() - currentPt.getY();
		
		if (Math.abs(deltaX) > Math.abs(deltaY)) {
			grid.moveByDisplacement(this, signum(deltaX));
		} else {
			grid.moveByDisplacement(this, 0, signum(deltaY));
		}
	}
	
	private int signum(int n) {
		if (n > 0) 
			return 1;
		if (n < 0)
			return -1;
		return 0;
	}
	
	public void send(AgentMessage m) {
		for (Object obj : context)
			if (obj instanceof FirefighterSquad && ((FirefighterSquad) obj).id != this.id)
				((FirefighterSquad) obj).receive(m);
	}

	public void receive(AgentMessage m) {
		mailbox.add(m);
	}

	private AgentMessage read() {
		if (mailbox.size() > 0)
			return mailbox.remove(0);
		return null;
	}
	
	private AgentMessage randomRead() {
		if (mailbox.size() > 0) {
			Random r = new Random();
			int index = r.nextInt(mailbox.size());
			mailbox.get(index);
			return mailbox.remove(index);
		}
		return null;
	}
	
	private void flushMailbox() {
		mailbox.clear();
	}
}
