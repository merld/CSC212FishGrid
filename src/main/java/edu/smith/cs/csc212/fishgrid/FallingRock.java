package edu.smith.cs.csc212.fishgrid;

import java.awt.Color;

public class FallingRock extends Rock{
	
	private static Color[] ROCK_COLORS = new Color[] {
			new Color(144,144,192),
			new Color(145,163,176),
			new Color(112,128,144),
			new Color(94,113,106),
			new Color(76,88,102),
			new Color(170,152,169),
			new Color(152,129,123),
			new Color(138,129,141),
			new Color(72,60,50)
	};
	
	public FallingRock(World world) {
		super(world);
		
		color=(int)(Math.random()*ROCK_COLORS.length);
		
	}
	
	public void step() {
		this.moveDown();
	}
}
