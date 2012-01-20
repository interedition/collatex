package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

public class Island {

	private ArrayList<Coordinate> island;

	public Island() {
		island = new ArrayList<Coordinate>();
	}

	// this is not a real iterator implementation but it works...
  public ArrayList<Coordinate> iterator() {
	  return island;
  }

	public void add(Coordinate coordinate) {
		island.add(coordinate);
  }

	public int size() {
	  return island.size();
  }
}