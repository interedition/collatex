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

public class DirectedIsland extends Island{

	private int direction;

	public DirectedIsland() {
		island = new ArrayList<Coordinate>();
		direction = 0;
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

	public int direction() {
		return direction;
	}

}
