package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

public class Archipelago {

	protected ArrayList<MatchMatrixIsland> islands;

	public Archipelago() {
		islands = new ArrayList<MatchMatrixIsland>();
	}

	public Archipelago(MatchMatrixIsland isl) {
		islands = new ArrayList<MatchMatrixIsland>();
		islands.add(isl);
  }
	
	public void add(MatchMatrixIsland island) {
		for(MatchMatrixIsland i : islands) {
			if(island.size()>i.size()) {
				islands.add(islands.indexOf(i), island);
				return;
			} else
				try {
					MatchMatrixIsland disl = (MatchMatrixIsland) island;
					MatchMatrixIsland di   = (MatchMatrixIsland) i;
				if(island.size()>i.size() && disl.direction()>di.direction()) {
					islands.add(islands.indexOf(i), island);
					return;
				}
				} catch (Exception e) {
				}
		}
		islands.add(island);
  }

	// this is not a real iterator implementation but it works...
  public ArrayList<MatchMatrixIsland> iterator() {
	  return islands;
  }


	protected void remove(int i) {
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
					(islands.get(i)).merge(islands.get(j));
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
	
	public MatchMatrixIsland get(int i) {
		return islands.get(i);
	}

	public Archipelago copy() {
		Archipelago result = new Archipelago();
		for(MatchMatrixIsland isl: islands) {
			result.add((MatchMatrixIsland) isl.copy());
		}
	  return result;
  }

	public boolean conflictsWith(MatchMatrixIsland island) {
		for(MatchMatrixIsland isl : islands) {
			if(isl.isCompetitor(island))
				return true;
		}
	  return false;
  }

	public String toString() {
		String result = "";
		for(MatchMatrixIsland island : islands) {
		  if(result.isEmpty())
		  	result = "[ " + island;
		  else
		  	result += ", " + island;
		}
		result += " ]";
		return result;
	}

	public int value() {
	  int result= 0;
	  for(MatchMatrixIsland isl: islands) {
	  	result += isl.value();
	  }
	  return result;
  }

	@Override
	public boolean equals(Object object) {
		if(object.getClass()!=this.getClass())
				return false;
		if(((Archipelago)object).size()!=this.size())
		  return false;
		for(int i=0; i<size(); i++) {
			if(!((Archipelago)object).get(i).equals(get(i)))
				return false;
		}
		return true;		
	}

	public ArrayList<MatchMatrixCell> findGaps() {
		ArrayList<MatchMatrixCell> list = new ArrayList<MatchMatrixCell>();
		return findGaps(list);
  }

	public ArrayList<MatchMatrixCell> findGaps(MatchMatrixCell begin, MatchMatrixCell end) {
		ArrayList<MatchMatrixCell> list = new ArrayList<MatchMatrixCell>();
		list.add(begin);
		list.add(end);
		return findGaps(list);
  }

	public ArrayList<MatchMatrixCell> findGaps(ArrayList<MatchMatrixCell> list) {
		ArrayList<MatchMatrixCell> result = new ArrayList<MatchMatrixCell>(list);
		for(MatchMatrixIsland isl : islands) {
			MatchMatrixCell left = isl.getLeftEnd();
			MatchMatrixCell right = isl.getRightEnd();
			boolean found = false;
			for(int i=0; i<result.size(); i++) {
				if(left.col<result.get(i).col || (left.col==result.get(i).col && left.row<result.get(i).row)) {
					result.add(i, right);
					result.add(i, left);
					found = true;
					break;
				}
			}
			if(!found) {
				result.add(left);
				result.add(right);
			}
		}
		result.remove(result.size()-1);
		result.remove(0);
	  return result;
  }

}
