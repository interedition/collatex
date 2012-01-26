package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

public abstract class Island {

	protected ArrayList<Coordinate> island;

	public ArrayList<Coordinate> iterator() {
    return island;
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

	/**
   * Two islands are competitors if there is a horizontal or
   * vertical line which goes through both islands 
   */
  public boolean isCompetitor(UndirectedIsland isl) {
  	for(Coordinate c: isl.iterator()) {
  		for(Coordinate d: island) {
  			if(c.sameCol(d) || c.sameRow(d))
  				return true;
  		}
  	}
  	return false;
  }

	public abstract boolean add(Coordinate c) ;
	
	public boolean remove(Coordinate c) {
		boolean result = true;
		if(partOf(c))
			island.remove(c);
		else
			result = false;
		return result;
	}

	protected boolean removeSameColOrRow(Coordinate c) {
    ArrayList<Coordinate> remove = new ArrayList<Coordinate>();
		for(Coordinate coor: island) {
			if(coor.sameCol(c) || coor.sameRow(c)) {
				remove.add(coor);
			}
		}
		if(remove.isEmpty())
			return false;
    for(Coordinate coor : remove) {
    	island.remove(coor);
    }
	  return true;
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

	public String toString() {
		String result = "";
		for(Coordinate coor : island) {
		  if(result.isEmpty())
		  	result = "{ " + coor;
		  else
		  	result += ", "+coor;
		}
		result += " }";
		return result;
	}
	
	public boolean sharedCol(Island isl) {
		boolean result = false;
		for(Coordinate col : isl.iterator()) {
			for(Coordinate c: island) {
		    result = c.sameCol(col);
		    if(result)
		    	break;
			}
		}
		return result;
	}

	public boolean sharedRow(Island isl) {
		boolean result = false;
		for(Coordinate col : isl.iterator()) {
			for(Coordinate c: island) {
		    result = c.sameRow(col);
		    if(result)
		    	break;
			}
		}
		return result;
	}

}
