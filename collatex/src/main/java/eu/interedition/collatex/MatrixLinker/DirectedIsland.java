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

public class DirectedIsland {

	private int direction;
	private ArrayList<MatchMatrixCell> island;

	public DirectedIsland() {
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
	
	public DirectedIsland removePoints(DirectedIsland di) {
		DirectedIsland result = (DirectedIsland) this.copy();
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

	public void merge(DirectedIsland di) {
		for(MatchMatrixCell c: di.iterator()) {
			add(c);
		}
  }

	public DirectedIsland copy() {
		DirectedIsland result = new DirectedIsland();
		for(MatchMatrixCell c: island)
			result.add(c.copy());
		return result;
	}

	/**
   * Two islands are competitors if there is a horizontal or
   * vertical line which goes through both islands 
   */
  public boolean isCompetitor(DirectedIsland isl) {
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

	public boolean overlap(DirectedIsland isl) {
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

}
