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

public class Island {

	private int direction;
	private ArrayList<Coordinate> island;

	public Island() {
		island = new ArrayList<Coordinate>();
		direction = 0;
	}

	public boolean add(Coordinate coordinate) {
		boolean result = false;
		if(island.isEmpty()) {
			result = island.add(coordinate);
		} else  
			if(!partOf(coordinate) && neighbour(coordinate)) {
				if(direction==0) {
					Coordinate existing_coordinate = island.get(0);
					direction = (existing_coordinate.row - coordinate.row) / (existing_coordinate.col - coordinate.col);
					result = island.add(coordinate);
				}	else {
					Coordinate existing_coordinate = island.get(0);
					if(existing_coordinate.col!=coordinate.col) {
  					int new_direction = (existing_coordinate.row - coordinate.row) / (existing_coordinate.col - coordinate.col);
  					if(new_direction==direction)
  						result = island.add(coordinate);
					}
				}
			}
		return result; 
  }

	public int direction() {
		return direction;
	}
	
	public Island removePoints(Island di) {
		Island result = (Island) this.copy();
    for(Coordinate c : di.iterator()) {
    	result.removeSameColOrRow(c);
    }
		return result;		
	}

	public Coordinate getCoorOnRow(int row) {
		for(Coordinate coor : island) {
			if(coor.getRow()==row)
				return coor;
		}
		return null;
	}
		
	public Coordinate getCoorOnCol(int col) {
		for(Coordinate coor : island) {
			if(coor.getCol()==col)
				return coor;
		}
		return null;
	}

	public void merge(Island di) {
		for(Coordinate c: di.iterator()) {
			add(c);
		}
  }

	public Island copy() {
		Island result = new Island();
		for(Coordinate c: island)
			result.add(c.copy());
		return result;
	}

	/**
   * Two islands are competitors if there is a horizontal or
   * vertical line which goes through both islands 
   */
  public boolean isCompetitor(Island isl) {
  	for(Coordinate c: isl.iterator()) {
  		for(Coordinate d: island) {
  			if(c.sameCol(d) || c.sameRow(d))
  				return true;
  		}
  	}
  	return false;
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

	public ArrayList<Coordinate> iterator() {
    return island;
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

	public boolean overlap(Island isl) {
		for(Coordinate c: isl.iterator()) {
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
  	for(Coordinate c : island) {
  		if(result.isEmpty())
  			result = "{ " + c;
  		else
  			result += ", " + c;
  	}
		return result + " }";
  }

}
