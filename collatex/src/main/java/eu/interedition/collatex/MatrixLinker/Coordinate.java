package eu.interedition.collatex.MatrixLinker;

public class Coordinate {
	int row;
	int col;
	
	Coordinate(int col, int row) {
	  this.col = col;
	  this.row = row;
	}
	
	public int getRow() {
		return row;
	}
	
	public int getCol() {
		return col;
	}
	
	public boolean sameCol(Coordinate c) {
		return c.col==col;
	}

	public boolean sameRow(Coordinate c) {
		return c.row==row;
	}

	public String toString() {
		return "("+col+","+row+")";
	}
	
	public boolean equals(Object o) {
		if(!o.getClass().equals(Coordinate.class))
			return false;
		Coordinate c = (Coordinate) o;
		return this.row==c.getRow() && this.col==c.getCol();
	}

	public boolean borders(Coordinate c) {
    boolean res = Math.abs(this.row - c.getRow())==1;
    res &= Math.abs(this.col - c.getCol())==1;
		return res;
  }

	public Coordinate copy() {
	  return new Coordinate(col, row);
  }

	public boolean partOf(Archipelago arch) {
		boolean result = false;
		for(Island isl : arch.iterator()) {
			result |= isl.partOf(this);
		}
	  return result;
  }
}
