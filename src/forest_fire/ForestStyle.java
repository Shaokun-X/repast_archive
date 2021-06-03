package forest_fire;


import java.awt.Color;
import java.awt.Font;

import repast.simphony.visualizationOGL2D.StyleOGL2D;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;

public class ForestStyle implements StyleOGL2D<Object> {

	protected ShapeFactory2D shapeFactory;
	private final int size = 15;
	
	@Override
	public float getRotation(Object object) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void init(ShapeFactory2D factory) {
		this.shapeFactory = factory;
	}

	@Override
	public VSpatial getVSpatial(Object object, VSpatial spatial) {
		if (spatial == null) {
			spatial = shapeFactory.createRectangle(this.size, this.size, true);
		}
		return spatial;
	}

	@Override
	public Color getColor(Object object) {
		Forest forest = (Forest) object;
		forest.getFireSize();
		
		float health = forest.getLife() / (float) forest.getFullLife();
		
		if (forest.getFireSize() > 0) {
			float red = (float) (1.0 - Math.exp(-8f / FireLevel.BIG * forest.getFireSize()));
//			System.out.println(red);
			return new Color(red * health, (1-red) * health, 0f);
		} else {
			return new Color(0, health, 0);
		}
	}

	@Override
	public int getBorderSize(Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Color getBorderColor(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getScale(Object object) {
		return 1;
	}

	@Override
	public String getLabel(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Font getLabelFont(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getLabelXOffset(Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getLabelYOffset(Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Position getLabelPosition(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getLabelColor(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
