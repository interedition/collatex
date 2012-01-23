package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

/**
 * An Island is a collections of Coordinates. All these 'fields' are connected at the corners.
 * I.e. all the white or all the black fields on a chessboard could be an island.
 * 
 * @author meindert
 *
 */

public class Island extends DirectedIsland {

	private ArrayList<DirectedIsland> versions;

	public Island() {
		island = new ArrayList<Coordinate>();
		versions = new ArrayList<DirectedIsland>();
	}

	public void add(Coordinate coordinate) {
		if(island.isEmpty())
			island.add(coordinate);
		else 
			if(!partOf(coordinate) && neighbour(coordinate))
				island.add(coordinate);
			else
				return;
		if(versions.isEmpty()) {
			DirectedIsland version = new DirectedIsland();
			version.add(coordinate);
			versions.add(version);
		} else {
			
		}
  }

	public void merge(Island island2) {
		for(Coordinate c: island2.iterator()) {
			System.out.println("("+c.col+","+c.row+")");
			add(c);
			System.out.println("size: "+size());
		}
  }
	
	/**
	 * Two islands are competitors if there is a horizontal or
	 * vertical line which goes through both islands 
	 */
	public boolean isCompetitor (Island isl) {
		for(Coordinate c: isl.iterator()) {
			for(Coordinate d: island) {
				if(c.sameCol(d) || c.sameRow(d))
					return true;
			}
		}
		return false;
	}

	public int numOfVersions() {
		return versions.size();
  }
}