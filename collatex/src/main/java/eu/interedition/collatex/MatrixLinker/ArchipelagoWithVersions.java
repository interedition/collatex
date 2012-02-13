package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

public class ArchipelagoWithVersions extends Archipelago {

	private ArrayList<Archipelago> nonConflVersions;

	public ArchipelagoWithVersions() {
		islands = new ArrayList<DirectedIsland>();
		nonConflVersions = new ArrayList<Archipelago>();
	}

	public ArchipelagoWithVersions(DirectedIsland isl) {
		islands = new ArrayList<DirectedIsland>();
		nonConflVersions = new ArrayList<Archipelago>();
		add(isl);
  }

	public void add(DirectedIsland island) {
    super.add(island);
//		addIslandToNonConflictingVersions(island);
  }

//	private void addIslandToNonConflictingVersions(DirectedIsland island) {
//		System.out.println("addIslandToNonConflictingVersions("+island+")");
//
//		if(nonConflVersions.size()==0) {
//			System.out.println("nonConflVersions.size()==0");
//			Archipelago version = new Archipelago();
//			version.add(island);
//			nonConflVersions.add(version);
//		} else {
//			boolean found_one = false;
//			for(Archipelago version : nonConflVersions)	{
//				System.out.println("version: "+version);
//				if(!version.conflictsWith(island)) {
//					System.out.println("!version.conflictsWith(island)");
//					version.add(island);
//					found_one = true;
//				} else {
//					for(Island isl : version.iterator()) {
//						DirectedIsland di = (DirectedIsland)isl;
//						DirectedIsland res = di.removePoints((DirectedIsland) island);
//						System.out.println("di: "+di);
//						System.out.println("res: "+res);
//					}
//					/* misschien een deel van deze versie bruikbaar om samen met
//					 * het nieuwe island een nieuwe versie te maken? 
//					 */
//				}
//			}
//			if(!found_one) {
//				System.out.println("!found_one");
//				// new version
//				// geheel nieuw of deels uit een andere versie opgebouwd.
//				Archipelago version = new Archipelago();
//				version.add(island);
//				nonConflVersions.add(version);
//			}
//		}
//  }

	public void createNonConflictingVersions() {
		for(DirectedIsland island : islands) {
  		System.out.println("createNonConflictingVersions("+island+")");
  		if(nonConflVersions.size()==0) {
  			System.out.println("nonConflVersions.size()==0");
  			Archipelago version = new Archipelago();
  			version.add(island);
  			nonConflVersions.add(version);
  		} else {
  			boolean found_one = false;
				ArrayList<Archipelago> new_versions = new ArrayList<Archipelago>();
  			for(Archipelago version : nonConflVersions)	{
  				System.out.println("version: "+version);
  				if(!version.conflictsWith(island)) {
  					System.out.println("!version.conflictsWith(island)");
  					version.add(island);
  					found_one = true;
  				}
  			}
  			if(!found_one) {
  				System.out.println("!found_one");
    			for(Archipelago version : nonConflVersions)	{
  					for(DirectedIsland isl : version.iterator()) {
  						DirectedIsland di = (DirectedIsland)isl;
  						DirectedIsland res = di.removePoints((DirectedIsland) island);
  						System.out.println("di: "+di);
  						System.out.println("res: "+res);
  						if(res.size()>0) {
  							found_one = true;
  			  			Archipelago new_version = new Archipelago();
  			  			new_version.add(island);
  			  			new_version.add(res);
  			  			new_versions.add(new_version);
  						}
  					}
    			}
					if(new_versions.size()>0) {
						for(Archipelago arch : new_versions) {
							nonConflVersions.add(arch);
						}
					}
  			}
  			if(!found_one) {
  				System.out.println("!found_one");
  				Archipelago version = new Archipelago();
  				version.add(island);
  				nonConflVersions.add(version);
  			}
  		}
		}
  }

	public int numOfNonConflConstell() {
	  return nonConflVersions.size();
  }

	public ArchipelagoWithVersions copy() {
		ArchipelagoWithVersions result = new ArchipelagoWithVersions();
		for(DirectedIsland isl: islands) {
			result.add((DirectedIsland) isl.copy());
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

	public Archipelago getVersion(int i) {
		try {
			if(nonConflVersions.isEmpty())
				createNonConflictingVersions();
			return nonConflVersions.get(i);
		} catch(IndexOutOfBoundsException exc) {
			return null;
		}
  }

//	public Archipelago nextNonConflConf() {
//		return new Archipelago();
//	}
}