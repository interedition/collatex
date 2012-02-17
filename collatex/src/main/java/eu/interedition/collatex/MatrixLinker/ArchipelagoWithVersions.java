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
		boolean debug = false;
//		PrintWriter logging = null;
//		File logFile = new File(File.separator + "c:\\logfile.txt");
//		try {
//	    logging = new PrintWriter(new FileOutputStream(logFile));
//    } catch (FileNotFoundException e) {
//	    e.printStackTrace();
//    }

    nonConflVersions = new ArrayList<Archipelago>();
		int tel = 0;
		for(Island island : islands) {
  		System.out.println("createNonConflictingVersions["+tel+"]("+island+")");
  		tel++;
//  		if(tel>22)
  			debug = false;
//  			System.out.println("nonConflVersions.size(): "+nonConflVersions.size());
//  			int tel_version = 0;
//  			for(Archipelago arch : nonConflVersions) {
//  				System.out.println("arch version ("+(tel_version++)+"): " + arch);
//  			}
// TODO
//  		if(tel>22) {
//  			int tel_version = 0;
//  			for(Archipelago arch : nonConflVersions) {
//  				System.out.println("arch version ("+(tel_version++)+"): " + arch);
//  			}
//  			System.exit(1);
//  		}
  		if(nonConflVersions.size()==0) {
  			if(debug)
  				System.out.println("nonConflVersions.size()==0");
  			Archipelago version = new Archipelago();
  			version.add(island);
  			nonConflVersions.add(version);
  		} else {
  			boolean found_one = false;
				ArrayList<Archipelago> new_versions = new ArrayList<Archipelago>();
				int tel_loop = 0;
  			for(Archipelago version : nonConflVersions)	{
    			if(debug)
    				System.out.println("loop 1: "+tel_loop++);
  				if(!version.conflictsWith(island)) {
  	  			if(debug)
  	  				System.out.println("!version.conflictsWith(island)");
  					version.add(island);
  					found_one = true;
  				}
  			}
  			if(!found_one) {
    			if(debug)
    				System.out.println("!found_one");
  				// try to find a existing version in which the new island fits
  				// after removing some points
    			tel_loop = 0;
    			for(Archipelago version : nonConflVersions)	{
    				if(debug)
    				  System.out.println("loop 2: "+tel_loop++);
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
    			tel_loop = 0;
    			for(Archipelago version : nonConflVersions)	{
    				if(debug)
    					System.out.println("loop 3: "+tel_loop++);
		  			Archipelago new_version = new Archipelago();
		  			new_version.add(island);
  					for(Island isl : version.iterator()) {
  						Island di = isl.copy();
  		  			if(debug)
  		  				System.out.println("di: "+di);
  						Island res = di.removePoints(island);
  		  			if(debug)
  		  				System.out.println("res: "+res);
  						if(res.size()>0) {
  							found_one = true;
  			  			new_version.add(res);
  						}
  					}
		  			new_versions.add(new_version);
    			}
					if(new_versions.size()>0) {
						tel_loop = 0;
						for(Archipelago arch : new_versions) {
							if(debug)
								System.out.println("loop 4: "+tel_loop++);
							addVersion(arch);
						}
					}
  			}
  			if(!found_one) {
    			if(debug)
    				System.out.println("!found_one");
  				Archipelago version = new Archipelago();
  				version.add(island);
  				addVersion(version);
  			}
  		}
		}
//		int tel_version = 0;
//		for(Archipelago arch : nonConflVersions) {
//			logging.println("arch version ("+(tel_version++)+"): " + arch);
//		}
//		tel_version = 0;
//		for(Archipelago arch : nonConflVersions) {
//			logging.println("version "+(tel_version++)+": " + arch.value());
//		}
//		logging.close();
  }

	private void addVersion(Archipelago version) {
		int pos = 0;
//		int tel_loop = 0;
//		System.out.println("addVersion - num of versions: "+nonConflVersions.size());
		for(pos = 0; pos<nonConflVersions.size(); pos++) {
			if(version.equals(nonConflVersions.get(pos)))
					return;
//			System.out.println("loop 5: "+tel_loop++);
			if(version.value()>nonConflVersions.get(pos).value()) {
				nonConflVersions.add(pos,version);
				return;
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

	/*
	 * Create a non-conflicting version by simply taken all the islands
	 * that to not conflict with each other, largest first. This presuming
	 * that Archipelago will have a high value if it contains the largest
	 * possible islands
	 */
	public Archipelago createFirstVersion() {
		Archipelago result = new Archipelago();
		for(Island isl: islands) {
			boolean confl = false;
			for(Island i: result.iterator()) {
				if(i.isCompetitor(isl)) {
					confl = true;
//					System.out.println(isl);
					break;
				}
			}
			if(!confl)
				result.add(isl);
		}
	  // TODO Auto-generated method stub
	  return result;
  }

}