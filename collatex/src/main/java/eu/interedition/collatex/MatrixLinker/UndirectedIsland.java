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
		if(island.isEmpty()) {
			result = island.add(coordinate);
		} else 
			if(!partOf(coordinate) && neighbour(coordinate)) {
				result = island.add(coordinate);
			} else {
				return false;
			}
		if(versions.isEmpty()) {
			DirectedIsland version = new DirectedIsland();
			version.add(coordinate);
			versions.add(version);
		} else {
			ArrayList<DirectedIsland> new_versions = new ArrayList<DirectedIsland>();
			boolean addedToExistingVersion = false;
			for(DirectedIsland ver: versions) {
				addedToExistingVersion |= ver.add(coordinate);
			}
			for(Coordinate c: island) {
				if(c.borders(coordinate)) {
					boolean formNewVersion = true;
					for(DirectedIsland di : versions) {
						formNewVersion &= !(di.partOf(coordinate) && di.partOf(c));
					}
					if(formNewVersion) {
  					DirectedIsland version = new DirectedIsland();
  					version.add(coordinate);
  					version.add(c);
  					new_versions.add(version);
					}
				}
			}
			if(!new_versions.isEmpty()) {
  			for(DirectedIsland di : new_versions) {
  				versions.add(di);
  			}
			}
		}
		return result;
  }

	public void merge(Island island2) {
		for(Coordinate c: island2.iterator()) {
			add(c);
		}
  }
	
	public int numOfVersions() {
		return versions.size();
  }
	
	public ArrayList<DirectedIsland> getVersions() {
		return versions;
	}
}