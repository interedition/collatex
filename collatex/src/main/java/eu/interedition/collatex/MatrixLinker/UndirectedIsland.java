package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

/**
 * An Island is a collections of Coordinates. All these 'fields' are connected at the corners.
 * I.e. all the white or all the black fields on a chessboard could be an island.
 * 
 * @author meindert
 *
 */

public class UndirectedIsland extends Island {

	private ArrayList<DirectedIsland> versions;

	public UndirectedIsland() {
		island = new ArrayList<Coordinate>();
		versions = new ArrayList<DirectedIsland>();
	}

	public boolean add(Coordinate coordinate) {
		boolean result = false;
		if(island.isEmpty())
			result = island.add(coordinate);
		else 
			if(!partOf(coordinate) && neighbour(coordinate))
				result = island.add(coordinate);
			else
				return false;
		if(versions.isEmpty()) {
			DirectedIsland version = new DirectedIsland();
			version.add(coordinate);
			versions.add(version);
		} else {
			boolean res = false;
			for(DirectedIsland ver: versions) {
				res = ver.add(coordinate);
			}
			if(!res) {
  			DirectedIsland version_1 = new DirectedIsland();
  			version_1.add(coordinate);
  			DirectedIsland version_2 = new DirectedIsland();
  			version_2.add(coordinate);
  			for(DirectedIsland ver: versions) {
  				if(ver.neighbour(coordinate)) {
  					for(Coordinate c: ver.iterator()) {
  						if(!version_1.add(c)) {
  							version_2.add(c);
  						}
  					}
  				}
  			}
  			versions.add(version_1);
  			if(version_2.direction()!=0)
  				versions.add(version_2);
  			// 
			}
		}
		return result;
  }

	public void merge(Island island2) {
		for(Coordinate c: island2.iterator()) {
			System.out.println("("+c.col+","+c.row+")");
			add(c);
			System.out.println("size: "+size());
		}
  }
	
	public int numOfVersions() {
		return versions.size();
  }
}