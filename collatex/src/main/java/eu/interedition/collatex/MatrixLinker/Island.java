package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

/**
 * An Island is a collections of Coordinates. All these 'fields' are connected at the corners.
 * I.e. all the white or all the black fields on a chessboard could be an island.
 * 
 * @author meindert
 *
 */

public class Island {

	private ArrayList<Coordinate> island;

	public Island() {
		island = new ArrayList<Coordinate>();
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
				island.add(coordinate);
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
	
	public int size() {
	  return island.size();
  }
}