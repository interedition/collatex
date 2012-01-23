package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

import org.apache.lucene.index.CorruptIndexException;

/**
 * An DirectedIsland is a collections of Coordinates all on the same
 * diagonal. The direction of this diagonal can be -1, 0, or 1.
 * The zero is for a DirectedIsland of only one Coordinate.
 * Directions 1 and -1 examples
 * Coordinates (0,0) (1,1) have Direction 1
 * Coordinates (1,1) (2,1) have Direction -1
 * I.e. if the row-cordinate gets larger and the col-coordinate also, the
 * direction is 1 (positive) else it is -1 (negative)
 * 
 * 
 */

public class DirectedIsland {

	protected ArrayList<Coordinate> island;
	
	private int direction;

	public DirectedIsland() {
		island = new ArrayList<Coordinate>();
		direction = 0;
	}

	// this is not a real iterator implementation but it works...
  public ArrayList<Coordinate> iterator() {
	  return island;
  }

	public void add(Coordinate coordinate) {
		if(island.isEmpty())
			island.add(coordinate);
		else 
			if(!partOf(coordinate) && neighbour(coordinate))
				if(direction==0) {
					Coordinate curr_coordinate = island.get(0);
					System.out.println("curr_coor: "+curr_coordinate);
					direction = (curr_coordinate.row - coordinate.row) / (curr_coordinate.col - coordinate.col);
					System.out.println("new coordinate: "+ coordinate);
					island.add(coordinate);
					System.out.println("direction: "+direction);
				}	else {
					Coordinate curr_coordinate = island.get(0);
					System.out.println("curr_coor: "+curr_coordinate);
					int new_direction = (curr_coordinate.row - coordinate.row) / (curr_coordinate.col - coordinate.col);
					System.out.println("new coordinate: "+ coordinate);
					System.out.println("direction: "+new_direction);
					if(new_direction==direction)
						island.add(coordinate);
				}
  }

	public boolean partOf(Coordinate c) {
		return island.contains(c);
	}
	
	public boolean neighbour(Coordinate c) {
		if(partOf(c))
			return false;
		for(Coordinate islC : island) {
			if(c.borders(islC)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean overlap(DirectedIsland isl) {
		for(Coordinate c: isl.iterator()) {
			if(partOf(c) || neighbour(c))
				return true;
		}
		return false;
	}
	
	public boolean isEmpty() {
		return island.isEmpty();
	}
	
	public void clear() {
		island.clear();
	}
	
	public int size() {
	  return island.size();
  }
	
	public int direction() {
		return direction;
	}

	/**
	 * Two islands are competitors if there is a horizontal or
	 * vertical line which goes through both islands 
	 */
	public boolean isCompetitor (DirectedIsland isl) {
		for(Coordinate c: isl.iterator()) {
			for(Coordinate d: island) {
				if(c.sameCol(d) || c.sameRow(d))
					return true;
			}
		}
		return false;
	}

}
