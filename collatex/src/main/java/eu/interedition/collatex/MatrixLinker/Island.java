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
	
	public boolean overlap(Island isl) {
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

	public void merge(Island island2) {
		for(Coordinate c: island2.iterator()) {
			System.out.println("("+c.col+","+c.row+")");
			add(c);
			System.out.println("size: "+size());
		}
  }
}