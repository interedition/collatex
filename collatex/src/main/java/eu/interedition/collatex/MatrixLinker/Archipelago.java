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
		for(Island i : islands) {
			if(island.size()>i.size()) {
				islands.add(islands.indexOf(i), island);
				return;
			} else
				try {
					DirectedIsland disl = (DirectedIsland) island;
					DirectedIsland di   = (DirectedIsland) i;
				if(island.size()>i.size() && disl.direction()>di.direction()) {
					islands.add(islands.indexOf(i), island);
					return;
				}
				} catch (Exception e) {
					// No directed island(s) so no way to determine priority
				}
		}
		islands.add(island);
  }

	private void remove(int i) {
		islands.remove(i);
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

	private void set(int i, Island island) {
		islands.set(i, island);
  }

	public Object numOfConflicts() {
		int result = 0;
		int num = islands.size();
		for(int i=0; i<num; i++)
			for(int j=i+1; j<num; j++) {
//				System.out.println("compare "+islands.get(j)+" with "+islands.get(i));				
				if(islands.get(j).isCompetitor(islands.get(i)))
					result++;
			}
	  return result;
  }

	public int numOfNonConflConstell() {
		int num = size();
		for(int i=0 ; i<num ; i++)
			for(int j=i+1; j<num; j++) {
				
			}
	  // TODO Auto-generated method stub
	  return 0;
  }

	public Archipelago copy() {
		Archipelago result = new Archipelago();
		for(Island isl: islands) {
			result.add(isl.copy());
		}
	  return result;
  }

	public Archipelago orderedBySizeNonConfl() {
		Archipelago result = this.copy();
		int num = result.size();
		ArrayList<Integer> remove = new ArrayList<Integer>();
		for(int i=0; i<num; i++)
			for(int j=i+1; j<num; j++)
				if(!remove.contains(i)&& !remove.contains(j)) {
					if(result.get(i).isCompetitor(result.get(j))) {
						int pos = 0;
						for(Integer p : remove)
							if(remove.get(p)<j)
								break;
							else
								pos++;
						remove.add(pos,j);
					}
				}
	  for(Integer i : remove)
	  	result.remove(i.intValue());
	  return result;
  }

//	public Archipelago nextNonConflConf() {
//		return new Archipelago();
//	}
}