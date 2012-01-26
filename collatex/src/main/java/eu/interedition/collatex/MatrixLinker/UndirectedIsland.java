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
	
	public ArrayList<DirectedIsland> conflictingVersions(DirectedIsland isl) {
		ArrayList<DirectedIsland> rivals = new ArrayList<DirectedIsland>();
		for(DirectedIsland di : versions) {
			if(!isl.equals(di)) {
				if(isl.sharedCol(di) || isl.sharedRow(di)) {
					rivals.add(di);
				}
			}
		}
		return rivals;
	}
	
	public ArrayList<DirectedIsland> nonRivalVersions() {
		ArrayList<DirectedIsland> conflictFree = new ArrayList<DirectedIsland>();
		if(versions.size()<2) {
			conflictFree.addAll(versions);
			return conflictFree;
		}
		int numOfVersions = versions.size();
		conflictFree.add(versions.get(0));
		for(int i=0; i<numOfVersions; i++) {
			for(int j=i+1; j<numOfVersions; j++) {
				versions.get(i);
				versions.get(j);
			}
		}
		return conflictFree;
	}
	
	public ArrayList<Archipelago> allNonRivalVersionGroups() {
		ArrayList<Archipelago> archList = new ArrayList<Archipelago>();
		int numOfVersions = versions.size();
		for(int i=0; i<numOfVersions; i++) {
			DirectedIsland v_i = versions.get(i);
			Archipelago arch = new Archipelago(v_i);
			for(int j=i+1; j<numOfVersions; j++) {
				DirectedIsland v_j = versions.get(j);
				v_j = v_j.removePoints(v_i);
				if(!v_j.isEmpty())
					arch.add(v_j);
			}
			archList.add(arch);
		}
		return archList;
	}
	
}