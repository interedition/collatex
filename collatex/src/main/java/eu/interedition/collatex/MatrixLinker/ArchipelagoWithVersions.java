package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

public class ArchipelagoWithVersions extends Archipelago {

	private ArrayList<Archipelago> nonConflVersions;

	public ArchipelagoWithVersions() {
		islands = new ArrayList<Island>();
		nonConflVersions = new ArrayList<Archipelago>();
	}

	public ArchipelagoWithVersions(Island isl) {
		islands = new ArrayList<Island>();
		nonConflVersions = new ArrayList<Archipelago>();
		add(isl);
  }

	public void add(Island island) {
    super.add(island);
		addIslandToNonConflictingVersions(island);
  }

	private void addIslandToNonConflictingVersions(Island island) {
		if(nonConflVersions.size()==0) {
			Archipelago version = new Archipelago();
			version.add(island.copy());
			nonConflVersions.add(version);
		}
  }

	public int numOfNonConflConstell() {
	  return nonConflVersions.size();
  }

	public ArchipelagoWithVersions copy() {
		ArchipelagoWithVersions result = new ArchipelagoWithVersions();
		for(Island isl: islands) {
			result.add(isl.copy());
		}
	  return result;
  }

	// is to be a list of non-conflicting versions
	public ArchipelagoWithVersions orderedBySizeNonConfl() {
		ArchipelagoWithVersions result = this.copy();
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