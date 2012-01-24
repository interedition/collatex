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
 */

public class DirectedIsland extends Island{

	private int direction;

	public DirectedIsland() {
		island = new ArrayList<Coordinate>();
		direction = 0;
	}

	public boolean add(Coordinate coordinate) {
		System.out.println("Add to DirectedIsland: "+coordinate);
		boolean result = false;
		if(island.isEmpty()) {
			System.out.println("island is empty");
			result = island.add(coordinate);
		} else  
			if(!partOf(coordinate) && neighbour(coordinate)) {
				System.out.println("!partOf(coordinate) && neighbour(coordinate)");
				if(direction==0) {
					System.out.println("direction==0");
					Coordinate existing_coordinate = island.get(0);
					System.out.println("existing_coor: "+existing_coordinate);
					direction = (existing_coordinate.row - coordinate.row) / (existing_coordinate.col - coordinate.col);
					result = island.add(coordinate);
					System.out.println("direction: "+direction);
				}	else {
					System.out.println("direction = "+direction);
					Coordinate existing_coordinate = island.get(0);
					System.out.println("existing_coor: "+existing_coordinate);
					if(existing_coordinate.col!=coordinate.col) {
  					int new_direction = (existing_coordinate.row - coordinate.row) / (existing_coordinate.col - coordinate.col);
  					System.out.println("new direction: "+new_direction);
  					if(new_direction==direction)
  						result = island.add(coordinate);
					}
				}
			}
		System.out.println("result: "+result);
		return result; 
  }

	public int direction() {
		return direction;
	}

}
