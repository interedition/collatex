package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

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

public class MatchMatrixIsland {

	private int direction;
	private ArrayList<MatchMatrixCell> island;

	public MatchMatrixIsland() {
		island = new ArrayList<MatchMatrixCell>();
		direction = 0;
	}

	public boolean add(MatchMatrixCell cell) {
		boolean result = false;
		if(island.isEmpty()) {
			result = island.add(cell);
		} else  
			if(!partOf(cell) && neighbour(cell)) {
				if(direction==0) {
					MatchMatrixCell existing = island.get(0);
					direction = (existing.row - cell.row) / (existing.col - cell.col);
					result = island.add(cell);
				}	else {
					MatchMatrixCell existing = island.get(0);
					if(existing.col!= cell.col) {
  					int new_direction = (existing.row - cell.row) / (existing.col - cell.col);
  					if(new_direction==direction)
  						result = island.add(cell);
					}
				}
			}
		return result; 
  }

	public int direction() {
		return direction;
	}
	
	public MatchMatrixIsland removePoints(MatchMatrixIsland di) {
		MatchMatrixIsland result = (MatchMatrixIsland) this.copy();
    for(MatchMatrixCell c : di.iterator()) {
    	result.removeSameColOrRow(c);
    }
		return result;		
	}

	public MatchMatrixCell getCoorOnRow(int row) {
		for(MatchMatrixCell coor : island) {
			if(coor.getRow()==row)
				return coor;
		}
		return null;
	}
		
	public MatchMatrixCell getCoorOnCol(int col) {
		for(MatchMatrixCell coor : island) {
			if(coor.getCol()==col)
				return coor;
		}
		return null;
	}

	public void merge(MatchMatrixIsland di) {
		for(MatchMatrixCell c: di.iterator()) {
			add(c);
		}
  }

	public MatchMatrixIsland copy() {
		MatchMatrixIsland result = new MatchMatrixIsland();
		for(MatchMatrixCell c: island)
			result.add(c.copy());
		return result;
	}

	/**
   * Two islands are competitors if there is a horizontal or
   * vertical line which goes through both islands 
   */
  public boolean isCompetitor(MatchMatrixIsland isl) {
  	for(MatchMatrixCell c: isl.iterator()) {
  		for(MatchMatrixCell d: island) {
  			if(c.sameCol(d) || c.sameRow(d))
  				return true;
  		}
  	}
  	return false;
  }

	public boolean partOf(MatchMatrixCell c) {
  	return island.contains(c);
  }

	public boolean neighbour(MatchMatrixCell c) {
  	if(partOf(c))
  		return false;
  	for(MatchMatrixCell islC : island) {
  		if(c.borders(islC)) {
  			return true;
  		}
  	}
  	return false;
  }

	public ArrayList<MatchMatrixCell> iterator() {
    return island;
  }

	protected boolean removeSameColOrRow(MatchMatrixCell c) {
    ArrayList<MatchMatrixCell> remove = new ArrayList<MatchMatrixCell>();
		for(MatchMatrixCell coor: island) {
			if(coor.sameCol(c) || coor.sameRow(c)) {
				remove.add(coor);
			}
		}
		if(remove.isEmpty())
			return false;
    for(MatchMatrixCell coor : remove) {
    	island.remove(coor);
    }
	  return true;
  }

	public boolean overlap(MatchMatrixIsland isl) {
		for(MatchMatrixCell c: isl.iterator()) {
    	if(partOf(c) || neighbour(c))
    		return true;
    }
    return false;
	}

  public int size() {
  	return island.size();
  }

  public void clear() {
  	island.clear();
  }
  
  public String toString() {
  	if(size()==0)
  		return "{ }";
  	String result = "";
  	for(MatchMatrixCell c : island) {
  		if(result.isEmpty())
  			result = "{ " + c;
  		else
  			result += ", " + c;
  	}
		return result + " }";
  }

	public int value() {
		if(size()<2) {
			return size();
		}
	  return direction + size()*size();
  }

	@Override
	public boolean equals(Object obj) {
		if(!obj.getClass().equals(MatchMatrixIsland.class))
			return false;
		MatchMatrixIsland isl = (MatchMatrixIsland) obj;
		boolean result = true;
		for(MatchMatrixCell c : isl.iterator()) {
			result &= this.partOf(c);
		}
		return result;
	}

	public MatchMatrixCell getLeftEnd() {
		MatchMatrixCell coor = island.get(0);
		for(MatchMatrixCell c: island) {
			if(c.col<coor.col)
				coor = c;
		}
	  return coor;
  }

	public MatchMatrixCell getRightEnd() {
		MatchMatrixCell coor = island.get(0);
		for(MatchMatrixCell c: island) {
			if(c.col>coor.col)
				coor = c;
		}
	  return coor;
  }

}
