package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

public class Archipelago {
	private ArrayList<Island> islands;

	public Archipelago() {
		islands = new ArrayList<Island>();
	}

	// this is not a real iterator implementation but it works...
  public ArrayList<Island> iterator() {
	  return islands;
  }

	public void add(Island island) {
		islands.add(island);
  }

	public int size() {
	  return islands.size();
  }
}