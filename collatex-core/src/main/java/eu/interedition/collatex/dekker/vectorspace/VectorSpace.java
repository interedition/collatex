package eu.interedition.collatex.dekker.vectorspace;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

/*
 * Class: VectorSpace
 * @Author: Ronald Haentjens Dekker
 * 
 * The idea is to build a multiple dimension vector space
 * with one dimension for each witness.
 * vectors represent phrase matches (=sequences of token matches)
 * between witnesses.
 * 
 *  
 *  To test out the idea we first start with a 2d vector space
 */

public class VectorSpace {
	//Note: the direction of the vector is a constant,
	//so there is no need to store it.
	public class Vector {
		public final int[] startCoordinate;
		public int length;
		
		public Vector(int x, int y) {
		  this(x, y, 1);
		}

    public Vector(int x, int y, int l) {
      this.startCoordinate = new int[] {x, y};
      this.length = l;
    }
    
    @Override
    public String toString() {
      String result = String.format("V: Start coordinates: %s, length %d", Arrays.toString(startCoordinate), length);
      return result;
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(startCoordinate, length);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Vector)) {
        return false;
      }
      Vector other = (Vector) obj;
      boolean result = Arrays.equals(other.startCoordinate, this.startCoordinate) && other.length == this.length;
      return result; 
    }

    public void extendLength() {
      this.length++;
    }

    //Note: this only checks whether the end coordinates of this 
    //and start coordinates of other are adjacent
    public boolean isAdjacent(Vector other) {
      int xDistance = Math.abs(startCoordinate[0]+length - other.startCoordinate[0]);
      int yDistance = Math.abs(startCoordinate[1]+length - other.startCoordinate[1]);
      return xDistance == 0 && yDistance == 0;
    }

    /*
     * This method checks whether one or more dimensions of this
     * vector and the other vector conflict with each other.
     */
    public boolean conflictsWith(Vector other) {
      // check x dimension
      Range<Integer> thisXRange = Ranges.closed(this.startCoordinate[0], this.startCoordinate[0]+this.length-1);
      Range<Integer> otherXRange = Ranges.closed(other.startCoordinate[0], other.startCoordinate[0]+other.length-1);
      if (thisXRange.encloses(otherXRange)) {
        return true;
      }
      // check y dimension
      Range<Integer> thisYRange = Ranges.closed(this.startCoordinate[1], this.startCoordinate[1]+this.length-1);
      Range<Integer> otherYRange = Ranges.closed(other.startCoordinate[1], other.startCoordinate[1]+other.length-1);
      if (thisYRange.encloses(otherYRange)) {
        return true;
      }
      return false;
    }
	}
	
	private List<Vector> vectors;
	
	public VectorSpace() {
		this.vectors = Lists.newArrayList();
	}
	
  public List<Vector> getVectors() {
    return vectors;
  }

  // add the matches tokens as a new vector to the vector space
	// if there is an adjacent vector already exists in the space
	// add to it
	// first check whether there is an existing vector to add to
	void addVector(int x, int y) {
    Vector match = new Vector(x, y);
		Vector adjacentVector = findAdjacentVector(match);
		if (adjacentVector!=null) {
		  adjacentVector.extendLength();
		} else {
	    vectors.add(match);
		}
	}

	/*
	 * find an adjacent vector in the vector space to the other vector
	 * returns null if none is found 
	 */
  private Vector findAdjacentVector(Vector other) {
    for (Vector v : vectors) {
		  if (v.isAdjacent(other)) {
		    return v;
		  }
		}
    return null;
  }

  public void remove(Vector v) {
    boolean removed = vectors.remove(v);
    if (!removed) {
      throw new RuntimeException("Vector "+v+" not present in vector space");
    }
  }
}
