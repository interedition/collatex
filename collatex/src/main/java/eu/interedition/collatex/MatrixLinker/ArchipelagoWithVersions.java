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
  }

	public void createNonConflictingVersions() {
		nonConflVersions = new ArrayList<Archipelago>();
		int tel = 0;
		for(Island island : islands) {
//  		System.out.println("createNonConflictingVersions["+tel+"]("+island+")");
  		tel++;
  		if(nonConflVersions.size()==0) {
//  			System.out.println("nonConflVersions.size()==0");
  			Archipelago version = new Archipelago();
  			version.add(island);
  			nonConflVersions.add(version);
  		} else {
  			boolean found_one = false;
				ArrayList<Archipelago> new_versions = new ArrayList<Archipelago>();
  			for(Archipelago version : nonConflVersions)	{
//  				System.out.println("version: "+version);
  				if(!version.conflictsWith(island)) {
//  					System.out.println("!version.conflictsWith(island)");
  					version.add(island);
  					found_one = true;
  				}
  			}
  			if(!found_one) {
//  				System.out.println("!found_one");
  				// try to find a existing version in which the new island fits
  				// after removing some points
    			for(Archipelago version : nonConflVersions)	{
						Island island_copy = island.copy();
  					for(Island isl : version.iterator()) {
  						island_copy = island_copy.removePoints(isl);
  					}
						if(island_copy.size()>0) {
							version.add(island_copy);
							found_one = true;
						}
    			}
    			// create a new version with the new island and (parts of) existing islands
    			for(Archipelago version : nonConflVersions)	{
		  			Archipelago new_version = new Archipelago();
		  			new_version.add(island);
  					for(Island isl : version.iterator()) {
  						Island di = isl.copy();
//  						System.out.println("di: "+di);
  						Island res = di.removePoints(island);
//  						System.out.println("res: "+res);
  						if(res.size()>0) {
  							found_one = true;
  			  			new_version.add(res);
  						}
  					}
		  			new_versions.add(new_version);
    			}
					if(new_versions.size()>0) {
						for(Archipelago arch : new_versions) {
							addVersion(arch);
						}
					}
  			}
  			if(!found_one) {
//  				System.out.println("!found_one");
  				Archipelago version = new Archipelago();
  				version.add(island);
  				addVersion(version);
  			}
  		}
		}
  }
	
	void addVersion(Archipelago version) {
		int pos = 0;
		for(pos = 0; pos<nonConflVersions.size(); pos++) {
			if(version.value()>nonConflVersions.get(pos).value()) {
				nonConflVersions.add(pos,version);
				break;
			}
		}
		nonConflVersions.add(version);
	}

	public int numOfNonConflConstell() {
	  return nonConflVersions.size();
  }

	public ArchipelagoWithVersions copy() {
		ArchipelagoWithVersions result = new ArchipelagoWithVersions();
		for(Island isl: islands) {
			result.add((Island) isl.copy());
		}
	  return result;
  }

	public Archipelago getVersion(int i) {
		try {
			if(nonConflVersions.isEmpty())
				createNonConflictingVersions();
			return nonConflVersions.get(i);
		} catch(IndexOutOfBoundsException exc) {
			return null;
		}
  }

	public ArrayList<Archipelago> getNonConflVersions() {
	  return nonConflVersions;
  }

}