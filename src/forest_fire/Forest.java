package forest_fire;

import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

import java.util.List;
import java.lang.Math;   

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;

public class Forest {
	
	/*
	 * The fire development function:
	 * 
	 * f -- fire size
	 * x -- lost life in [0, 1]
	 * t -- turning point in (0,1)
	 * -----------------------------------
	 *         1,                x == 0,
	 *         a * x,            0 < x < t
	 * f(x) =  Firelevel.MAX,    x == t
	 *         b * (1 - x),      x > t
	 * -----------------------------------
	 * 
	 * f(x) is continuous, a and b are greater than 0,
	 * their values can be determined by Firelevel.MAX
	 * 
	 */
	
	private final float TURNING_POINT = 4f / 5; 
	private final float CONST_A = (FireLevel.MAX - 1) / TURNING_POINT;
	private final float CONST_B = FireLevel.MAX / (1 - TURNING_POINT);
	
	// 1 means no penalty, should be greater than 1
	private final float PROPAGATION_PENALTY = 2;
	
//	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	private int life;
	private int fullLife;
	private int fireSize;
	/*
	 * Constructors
	 */
	
	public Forest(Grid<Object> grid, int fullLife, int fireSize) {
		setLife(fullLife);
		setFullLife(fullLife);
		setFireSize(fireSize);
//		this.space = space;
		this.grid = grid;
	}
	
	public Forest(Grid<Object> grid, int fullLife) {
		setFullLife(fullLife);
		setLife(fullLife);
		setFireSize(0);
//		this.space = space;
		this.grid = grid;
	}
	
	public Forest(Grid<Object> grid, int life, int fullLife, int fireSize) {
		setFullLife(fullLife);
		setLife(life);
		setFireSize(fireSize);
//		this.space = space;
		this.grid = grid;
	}
	
	/*
	 * Setters and Getters
	 */
	
	public int getLife() {
		return life;
	}
	public void setLife(int life) {
		if (life < 0)
			life = 0;
		if (life > getFullLife())
			life = getFullLife();
		this.life = life;
	}
	public boolean isOnFire() {
		if (fireSize > 0)
			return true;
		return false;
	}
	public int getFireSize() {
		return fireSize;
	}
	public void setFireSize(int fireSize) {
		if (fireSize < 0)
			fireSize = 0;
		if (fireSize > FireLevel.MAX)
			fireSize = FireLevel.MAX;
		this.fireSize = fireSize;
	}
	public int getFullLife() {
		return fullLife;
	}
	private void setFullLife(int fullLife) {
		if (fullLife < 0)
			fullLife = 0;
		this.fullLife = fullLife;
	}	

	public void setOnFire() {
		setFireSize(1);
	}
	
	public void putOutFire() {
		setFireSize(0);
	}
	
	
	/*
	 * Update
	 */
	
	@ScheduledMethod(start = 1, interval = 1)
	public void update() {
		updateFire();
		updateLife();
		propagateFire();
	}
	
	/*
	 * f -- fire size
	 * x -- lost life in [0, 1]
	 * t -- turning point in (0,1)
	 * -----------------------------------
	 *         1,                x == 0,
	 *         a * x,            0 < x < t
	 * f(x) =  Firelevel.MAX,    x == t
	 *         b * (1 - x),      x > t
	 * -----------------------------------
	 */
	public void updateFire() {
		if (!isOnFire()) {
			return;
		}
		
		float lostLife = 1 - getLife() / (float) getFullLife();
		float newFireSize = getFireSize();
		
		if (lostLife == 0) {
			newFireSize = 1;
		} else if (lostLife < TURNING_POINT) {
			newFireSize = (float) Math.ceil(CONST_A * lostLife);	
		} else if (lostLife == TURNING_POINT) {
			newFireSize = FireLevel.MAX;
		} else if (lostLife > TURNING_POINT) {
			newFireSize = CONST_B * (1 - lostLife);
		}
		
		setFireSize((int) newFireSize);
	}
	
	public void updateLife() {
		setLife(getLife() - getFireSize());
	}
	
	
	
	public void propagateFire() {
		if (!isOnFire())
			return;
		// get gird location of current tree
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<Forest> nghCreator = new GridCellNgh<Forest>(grid, pt, Forest.class, 1, 1);
		List<GridCell<Forest>> gridCells = nghCreator.getNeighborhood(false);
		
		for (GridCell<Forest> cell : gridCells) {
			// Get neighbor Forest
			// Every cell there should exist only one Forest instance
			Forest neighbor = null;
			for(Object obj : cell.items()) {
				if (obj instanceof Forest) {
					neighbor = (Forest) obj;
					if (neighbor.isOnFire())
						continue;
					if (fireSize >= FireLevel.SMALL && fireSize < FireLevel.MIDDLE) {
						if (Math.random() * FireLevel.MAX * PROPAGATION_PENALTY < FireLevel.SMALL)
							neighbor.setOnFire();
					} else if (fireSize >= FireLevel.MIDDLE && fireSize < FireLevel.BIG) {
						if (Math.random() * FireLevel.MAX * PROPAGATION_PENALTY < FireLevel.MIDDLE)
							neighbor.setOnFire();
					} else if (fireSize >= FireLevel.BIG) {
						if (Math.random() * FireLevel.MAX * PROPAGATION_PENALTY < FireLevel.BIG)
							neighbor.setOnFire();
					}
					break;
				}
			}

		}
	}
	
}
