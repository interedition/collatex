package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

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
 */

public class DirectedIsland extends Island{

	private int direction;

	public DirectedIsland() {
		island = new ArrayList<Coordinate>();
		direction = 0;
	}

	public boolean add(Coordinate coordinate) {
		boolean result = false;
		if(island.isEmpty()) {
			result = island.add(coordinate);
		} else  
			if(!partOf(coordinate) && neighbour(coordinate)) {
				if(direction==0) {
					Coordinate existing_coordinate = island.get(0);
					direction = (existing_coordinate.row - coordinate.row) / (existing_coordinate.col - coordinate.col);
					result = island.add(coordinate);
				}	else {
					Coordinate existing_coordinate = island.get(0);
					if(existing_coordinate.col!=coordinate.col) {
  					int new_direction = (existing_coordinate.row - coordinate.row) / (existing_coordinate.col - coordinate.col);
  					if(new_direction==direction)
  						result = island.add(coordinate);
					}
				}
			}
		return result; 
  }

	public int direction() {
		return direction;
	}
	
	public DirectedIsland removePoints(DirectedIsland di) {
		DirectedIsland result = (DirectedIsland) this.copy();
    for(Coordinate c : di.iterator()) {
    	result.removeSameColOrRow(c);
    }
		return result;		
	}

	@Override
	public Coordinate getCoorOnRow(int row) {
		for(Coordinate coor : island) {
			if(coor.getRow()==row)
				return coor;
		}
		return null;
	}
		
	@Override
	public Coordinate getCoorOnCol(int col) {
		for(Coordinate coor : island) {
			if(coor.getCol()==col)
				return coor;
		}
		return null;
	}

}
