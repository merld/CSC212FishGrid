package edu.smith.cs.csc212.fishgrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * @author jfoley
 *
 */
public class FishGame {
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	/**
	 * These are the missing fish!
	 */
	List<Fish> missing;
	
	/**
	 * These are fish we've found!
	 */
	List<Fish> found;
	
	//These are the fish returned to home!
	List<Fish> atHome;
	
	//This is a snail.
	Snail snail;
	
	/**
	 * Number of steps!
	 */
	int stepsTaken;
	
	//Number of rocks
	public static final int ROCKNUM=7;
	
	/**
	 * Score!
	 */
	int score;
	
	/**
	 * Create a FishGame of a particular size.
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	public FishGame(int w, int h) {
		world = new World(w, h);
		
		//Declaring lists to keep track of fish
		missing = new ArrayList<Fish>();
		found = new ArrayList<Fish>();
		atHome = new ArrayList<Fish>();
		
		// Add a home!
		home = world.insertFishHome();
		
		//Add rocks!
		for (int i=0; i<ROCKNUM; i++) {
			world.insertRockRandomly();
		}
		
		//Add a falling rock and a snail!
		world.insertFallingRockRandomly();
		world.insertSnailRandomly();
		
		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);
		
		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
		}
	}
	
	
	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}
	
	/**
	 * This method is how the Main app tells whether we're done.
	 * @return true if the player has won (or maybe lost?).
	 */
	public boolean gameOver() {
		//Makes sure all fish are home before the win
		if(missing.isEmpty()&&atHome.size()==Fish.COLORS.length-1)
			return true;
		return false;
	}

	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;
			
		//Updates other fish steps
		for(Fish fish: found) {
			fish.addStep();
		}
		
		//checks to see if fish need to leave found
		for(int i =found.size()-1;i>0;i--) {
			if(found.get(i).getSteps()>=20&&(int)(Math.random()*10)<8) {
				found.get(i).resetSteps();
				missing.add(found.get(i));
				found.remove(i);
			}
		}
		
		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();
		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		// If we find a fish, remove it from missing.
		for (WorldObject wo : overlap) {
			// It is missing if it's in our missing list.
			if (missing.contains(wo)) {
				// Remove this fish from the missing list.
				missing.remove(wo);
				
				// Adds to found
				found.add((Fish) wo);
				
				//faster fish is 25 points and all others are 10 points
				if(wo.getColorIndex()==5)
					score+=25;
				else
					score += 10;
			}
			//Puts all found fish in atHome and removes from world
			else if(wo instanceof FishHome) {
				for(Fish f: found)
					atHome.add(f);
				for(int i=found.size()-1;i>=0;i--) {
					world.remove(found.get(i));
					found.remove(i);
				}
				
			}
		}
				
		// Make sure missing fish *do* something.
		wanderMissingFish();
		// When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		// Step any world-objects that run themselves.
		world.stepAll();
	}
	
	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		Random rand = ThreadLocalRandom.current();
		for (Fish lost : missing) {
			// 80% of the time, fastScared fish move randomly
			if(lost.getColorIndex()==5&&rand.nextDouble()<0.8)
				lost.moveRandomly();
			// 30% of the time, lost normal fish move randomly.
			else if (rand.nextDouble() < 0.3) 
				lost.moveRandomly();	
		}
		
		//If lost fish accidentally goes home, they are counted as atHome
		for(int i =missing.size()-1; i>=0;i--) {
			List<WorldObject> overlap2 = missing.get(i).findSameCell();
			overlap2.remove(missing.get(i));
			for(WorldObject thing: overlap2)
				if(thing instanceof FishHome) {
					atHome.add(missing.get(i));
					world.remove(missing.get(i));
					missing.remove(i);
				}
			
		}
	}

	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the game.
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 */
	public void click(int x, int y) {
		System.out.println("Clicked on: "+x+","+y+ " world.canSwim(player,...)="+world.canSwim(player, x, y));
		List<WorldObject> atPoint = world.find(x, y);
		for(WorldObject it: atPoint) {
			if(it instanceof Rock)
				world.remove(it);
		}

	}
	
}
