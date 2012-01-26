package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

public class Archipelago {
	private ArrayList<Island> islands;

	public Archipelago() {
		islands = new ArrayList<Island>();
	}

	public Archipelago(Island isl) {
		islands = new ArrayList<Island>();
		islands.add(isl);
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

	public void mergeIslands() {
		int i=0;
		int j=1;
		int[] rr = new int[size()];
		for(i=0; i<size(); i++) {
			for(j=i+1; j<size(); j++) {
				if(islands.get(i).overlap(islands.get(j))) {
					System.out.println("island "+i+" overlaps island "+j);
					((UndirectedIsland) islands.get(i)).merge(islands.get(j));
					islands.get(j).clear();
					rr[j] = 1;
				}
			}
		}
		for(i=(rr.length-1); i>0; i--) {
			if(rr[i]==1)
			  islands.remove(i);
		}
  }
	
	public Island get(int i) {
		return islands.get(i);
	}
}