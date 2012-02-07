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

	public ArrayList<Archipelago> getNonConflConf(Island isl_2) {
		ArrayList<Archipelago> archList = new ArrayList<Archipelago>();
		Archipelago arch = new Archipelago();
		arch.add(this);
		if(!isCompetitor(isl_2)) {
			arch.add(isl_2);
			archList.add (arch);
		} else {
			Island[] sharedCs = sharedCoords(isl_2);
			Island isl_1a = clone();
			Island isl_2a = isl_2.clone();
			for(Coordinate c: sharedCs[1].iterator()) {
				isl_2a.remove(c);
			}
		  arch = new Archipelago();
			arch.add(isl_1a.copy());
			arch.add(isl_2a.copy());
			archList.add (arch.copy());
		  int tel = sharedCs[1].size()-1;
			for(int i=sharedCs[0].size()-1; i>-1; i--) {
				Coordinate c = sharedCs[0].get(i);
			  arch = new Archipelago();
			  isl_1a.remove(c);
			  isl_2a.add(sharedCs[1].get(tel--));
				arch.add(isl_1a.copy());
				arch.add(isl_2a.copy());
				archList.add (arch.copy());
			}
		}
	  return archList;
  }

	private Coordinate get(int i) {
	  return island.get(i);
  }

	public Coordinate getCoorOnRow(int row) {
		for(Coordinate c: island) {
			if(c.row==row)
				return c;
		}
	  return null;
  }

	public Coordinate getCoorOnCol(int col) {
		for(Coordinate c: island) {
			if(c.col==col)
				return c;
		}
	  return null;
  }

	public Island clone() {
		Island result;
		if(this.getClass().equals(DirectedIsland.class))
			result = new DirectedIsland();
		else
			result = new UndirectedIsland();
		for(Coordinate c: island)
			result.add(c);
		return result;
	};
		
public Island[] sharedCoords(Island isl) {
	Island result_1 = new UndirectedIsland();
	Island result_2 = new UndirectedIsland();
	Island[] result = {null,null};
	for(Coordinate c : island) {
		Coordinate row = isl.getCoorOnRow(c.row);
		Coordinate col = isl.getCoorOnCol(c.col);
		if(row!=null || col!=null) {
			if(row==null) {
				result_1.add(col);
			} else if(col==null) {
				result_1.add(row);
			} else {
				if(col.equals(row)) {
					result_1.add(col);
				} else {
					result_1.add(col);
					result_1.add(row);
				}
			}
			result_2.add(c);
		}
	}
	result[0] = result_2;
	result[1] = result_1;
	return result;
}

public Island copy() {
	Island result;
	if(this.getClass().equals(DirectedIsland.class))
		result = new DirectedIsland();
	else
		result = new UndirectedIsland();
	for(Coordinate c: island)
		result.add(c.copy());
	return result;
}

}
