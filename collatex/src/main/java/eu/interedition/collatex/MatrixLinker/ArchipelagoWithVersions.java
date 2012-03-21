package eu.interedition.collatex.MatrixLinker;

import java.io.PrintWriter;
import java.util.ArrayList;

public class ArchipelagoWithVersions extends Archipelago {

  private static final String newLine = System.getProperty("line.separator");
  private ArrayList<Archipelago> nonConflVersions;
  private Integer[] isl2;

  public ArchipelagoWithVersions() {
    islands = new ArrayList<MatchMatrix.Island>();
    nonConflVersions = new ArrayList<Archipelago>();
  }

  public ArchipelagoWithVersions(MatchMatrix.Island isl) {
    islands = new ArrayList<MatchMatrix.Island>();
    nonConflVersions = new ArrayList<Archipelago>();
    add(isl);
  }

  public void add(MatchMatrix.Island island) {
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
    for (MatchMatrix.Island island : islands) {
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
      if (nonConflVersions.size() == 0) {
        if (debug)
          System.out.println("nonConflVersions.size()==0");
        Archipelago version = new Archipelago();
        version.add(island);
        nonConflVersions.add(version);
      } else {
        boolean found_one = false;
        ArrayList<Archipelago> new_versions = new ArrayList<Archipelago>();
        int tel_loop = 0;
        for (Archipelago version : nonConflVersions) {
          if (debug)
            System.out.println("loop 1: " + tel_loop++);
          if (!version.conflictsWith(island)) {
            if (debug)
              System.out.println("!version.conflictsWith(island)");
            version.add(island);
            found_one = true;
          }
        }
        if (!found_one) {
          if (debug)
            System.out.println("!found_one");
          // try to find a existing version in which the new island fits
          // after removing some points
          tel_loop = 0;
          for (Archipelago version : nonConflVersions) {
            if (debug)
              System.out.println("loop 2: " + tel_loop++);
            MatchMatrix.Island island_copy = new MatchMatrix.Island(island);
            for (MatchMatrix.Island isl : version.iterator()) {
              island_copy = island_copy.removePoints(isl);
            }
            if (island_copy.size() > 0) {
              version.add(island_copy);
              found_one = true;
            }
          }
          // create a new version with the new island and (parts of) existing islands
          tel_loop = 0;
          for (Archipelago version : nonConflVersions) {
            if (debug)
              System.out.println("loop 3: " + tel_loop++);
            Archipelago new_version = new Archipelago();
            new_version.add(island);
            for (MatchMatrix.Island isl : version.iterator()) {
              MatchMatrix.Island di = new MatchMatrix.Island(isl);
              if (debug)
                System.out.println("di: " + di);
              MatchMatrix.Island res = di.removePoints(island);
              if (debug)
                System.out.println("res: " + res);
              if (res.size() > 0) {
                found_one = true;
                new_version.add(res);
              }
            }
            new_versions.add(new_version);
          }
          if (new_versions.size() > 0) {
            tel_loop = 0;
            for (Archipelago arch : new_versions) {
              if (debug)
                System.out.println("loop 4: " + tel_loop++);
              addVersion(arch);
            }
          }
        }
        if (!found_one) {
          if (debug)
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
    for (pos = 0; pos < nonConflVersions.size(); pos++) {
      if (version.equals(nonConflVersions.get(pos)))
        return;
//			System.out.println("loop 5: "+tel_loop++);
      if (version.value() > nonConflVersions.get(pos).value()) {
        nonConflVersions.add(pos, version);
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
    for (MatchMatrix.Island isl : islands) {
      result.add(new MatchMatrix.Island(isl));
    }
    return result;
  }

  public Archipelago getVersion(int i) {
    try {
      if (nonConflVersions.isEmpty())
        createNonConflictingVersions();
      return nonConflVersions.get(i);
    } catch (IndexOutOfBoundsException exc) {
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
    for (MatchMatrix.Island isl : islands) {
      int i = 0;
      int res_size = result.size();
      boolean confl = false;
      for (i = 0; i < res_size; i++) {
        if (result.get(i).isCompetitor(isl)) {
          confl = true;
//					System.out.println("confl: "+isl+" with: "+i+" : "+result.get(i));
          break;
        }
      }
      if (!confl)
        result.add(isl);
      else {
        MatchMatrix.Island island1 = result.get(i);
        if (island1.size() <= isl.size()) {
          double tot_d_1 = 0.0;
          double tot_d_2 = 0.0;
          for (int j = 0; j < i; j++) {
            MatchMatrix.Island island2 = result.get(j);
            tot_d_1 += distance(island2, island1);
            tot_d_2 += distance(island2, isl);
          }
          System.out.println("tot_d_1: " + tot_d_1);
          System.out.println("tot_d_2: " + tot_d_2);
          if (tot_d_2 < tot_d_1) {
            result.remove(i);
            result.add(isl);
          }
        }
      }
    }
    return result;
  }

  private double distance(MatchMatrix.Island isl1, MatchMatrix.Island isl2) {
    double result = 0.0;
    int isl1_L_x = isl1.getLeftEnd().column;
    int isl1_L_y = isl1.getLeftEnd().row;
    int isl1_R_x = isl1.getRightEnd().column;
    int isl1_R_y = isl1.getRightEnd().row;
    int isl2_L_x = isl2.getLeftEnd().column;
    int isl2_L_y = isl2.getLeftEnd().row;
    int isl2_R_x = isl2.getRightEnd().column;
    int isl2_R_y = isl2.getRightEnd().row;
    result = distance(isl1_L_x, isl1_L_y, isl2_L_x, isl2_L_y);
    double d = distance(isl1_L_x, isl1_L_y, isl2_R_x, isl2_R_y);
    if (d < result)
      result = d;
    d = distance(isl1_R_x, isl1_R_y, isl2_L_x, isl2_L_y);
    if (d < result)
      result = d;
    d = distance(isl1_R_x, isl1_R_y, isl2_R_x, isl2_R_y);
    if (d < result)
      result = d;
    return result;
  }

  private double distance(int a_x, int a_y, int b_x, int b_y) {
    double result = 0.0;
    result = Math.sqrt((a_x - b_x) * (a_x - b_x) + (a_y - b_y) * (a_y - b_y));
    return result;
  }

  public String createXML(MatchMatrix mat, PrintWriter output) {
    String result = "";
    ArrayList<String> columnLabels = mat.columnLabels();
    ArrayList<String> rowLabels = mat.rowLabels();
    ArrayList<MatchMatrix.Coordinates> list = new ArrayList<MatchMatrix.Coordinates>();
    int rowNum = rowLabels.size();
    int colNum = columnLabels.size();
    list.add(new MatchMatrix.Coordinates(0, 0));
    list.add(new MatchMatrix.Coordinates(colNum - 1, rowNum - 1));
//  	System.out.println("1");
    Archipelago createFirstVersion = createFirstVersion();
    System.out.println("2");
//  	output.println(mat.toHtml(this));
    output.println(mat.toHtml(createFirstVersion));
//  	System.out.println("3");
    ArrayList<MatchMatrix.Coordinates> gaps = createFirstVersion.findGaps(list);
    System.out.println("4");
    ArrayList<Integer[]> listOrderedByCol = new ArrayList<Integer[]>();
    int teller = 0;
    Integer[] isl = {0, 0, 0, 0, 0};
    for (MatchMatrix.Coordinates c : gaps) {
      teller++;
      if (teller % 2 == 0) {
        isl[0] = teller / 2;
        isl[3] = c.column;
        isl[4] = c.row;
        listOrderedByCol.add(isl.clone());
      } else {
        isl[1] = c.column;
        isl[2] = c.row;
      }
    }
    ArrayList<Integer[]> listOrderedByRow = new ArrayList<Integer[]>();
    for (Integer[] a : listOrderedByCol) {
      System.out.println(a[0] + ": " + a[1] + "-" + a[2]);
      boolean added = false;
      for (int i = 0; i < listOrderedByRow.size(); i++) {
        Integer row_a = a[2];
        Integer row_b = listOrderedByRow.get(i)[2];
        if (row_a < row_b) {
          listOrderedByRow.add(i, a);
          added = true;
          break;
        }
      }
      if (!added) {
        listOrderedByRow.add(a);
      }
    }
    for (Integer[] a : listOrderedByRow) {
      System.out.println(a[0] + ": " + a[1] + "-" + a[2]);
    }
    int rowPos = 0;
    int colPos = 0;
    int gapsPos = 0;
    output.println("<xml>");
    result += "<xml>" + newLine;
    String res = "";
    /* inspecteer inhoud gaps */
    teller = 0;
    MatchMatrix.Coordinates vorige = new MatchMatrix.Coordinates(0, 0);
    for (MatchMatrix.Coordinates c : gaps) {
      teller++;
      if (teller % 2 == 0) {
        System.out.print(" " + c + " ");
        for (int i = vorige.column; i <= c.column; i++)
          System.out.print(" " + columnLabels.get(i));
        System.out.println();
        vorige = c;
      } else {
        // vergelijk c met vorige
        if (c.column < vorige.column || c.row < vorige.row)
          System.out.println("sprong");
        System.out.print(c);
        vorige = c;
      }
    }
//  	while(true) {
//  		/**
//  		 * Hier anders aanpakken?
//  		 * Bijv eerst de stukken tussen de eilanden simpel als varianten opvatten?
//  		 * Probleem (bijvoorbeeld text 4): de eerste woorden in 'lem' matchen met
//  		 * 	woorden veel verderop in de 'rdg' (en zijn geen echte match).
//  		 * Dus proberen kijken of eerst de eilanden nogmaals gewaardeerd worden
//  		 * om zo eilanden die niet in de 'archipel' passen er buiten te laten.
//  		 */
//  		/** */
//  		int islStart = gaps.get(gapsPos).col; 
//  		int islStartRow = gaps.get(gapsPos).row; 
//  		int islEnd = gaps.get(gapsPos+1).col;
//  		if(colPos<islStart || rowPos<gaps.get(gapsPos).row) {
//  			String lem ="";
//  			String rdg = "";
//  			while(colPos<gaps.get(gapsPos).col)
//  				lem += columnLabels.get(colPos++) + " ";
//  			while(rowPos<gaps.get(gapsPos).row)
//  				rdg += rowLabels.get(rowPos++) + " ";
//  			result += printApp(output,lem, rdg);
//  			res = " ";
//  		}
//  		if(colPos==islStart && rowPos>islStartRow) {
//  			String lem ="";
//  			String rdg = "";
//  			while(colPos<gaps.get(gapsPos).col)
//  				lem += columnLabels.get(colPos++) + " ";
//  			result += printApp(output,lem, rdg);
//  			gapsPos += 2;
//  		} else {
//  			while(islStart<=islEnd)
//  				res += columnLabels.get(islStart++)+" ";
//  			output.println(res);
//  			if(!res.trim().isEmpty())
//  			  result += res + newLine;
//  			res = "";
//  			colPos = gaps.get(gapsPos+1).col + 1;
//  			rowPos = gaps.get(gapsPos+1).row + 1;
//  			gapsPos += 2;
//  		}
//  		if(gapsPos>=gaps.size() || colPos==colNum)
//  			break;
//  	}
//		String lem = "";
//		while(colPos<colNum-1){
//			lem += columnLabels.get(colPos) + " ";
//			colPos++;
//		}
//		String rdg = "";
//		while(rowPos<rowNum-1){
//			rdg += rowLabels.get(rowPos) + " ";
//			rowPos++;
//		}
//		if(!(lem.isEmpty() && rdg.isEmpty())) {
//			result += printApp(output,lem,rdg);
//		}
//		output.println("</xml>");
//		result += "</xml>";
    return doeiets(output, listOrderedByCol, listOrderedByRow, columnLabels, rowLabels);
  }

  private String doeiets(PrintWriter output, ArrayList<Integer[]> listOrderedByCol,
                         ArrayList<Integer[]> listOrderedByRow, ArrayList<String> columnLabels,
                         ArrayList<String> rowLabels) {
    String result = new String("<xml>\n");
    int rowCount = 0;
    int colCount = 0;
    int lastCol = -1;
    int lastRow = -1;
    boolean sprong = false;
    boolean finished = false;

    while (!finished) {
      System.out.println("col: " + colCount + " (" + drukAfArray(listOrderedByCol.get(colCount)) + ") - row: " + rowCount + " (" + drukAfArray(listOrderedByRow.get(rowCount)) + ")");
      String lem = "";
      String rdg = "";
      if (colCount > lastCol) {
        int a = -1;
        try {
          a = listOrderedByCol.get(lastCol)[3];
        } catch (ArrayIndexOutOfBoundsException excep) {
        }
        int b = listOrderedByCol.get(colCount)[1];
        if ((b - a) > 1)
          lem = getTekst(columnLabels, (a + 1), (b - 1));
        lastCol = colCount;
      }
      if (rowCount > lastRow) {
        int a = -1;
        try {
          a = listOrderedByRow.get(lastRow)[4];
        } catch (ArrayIndexOutOfBoundsException excep) {
        }
        int b = listOrderedByRow.get(rowCount)[2];
        if ((b - a) > 1)
          rdg = getTekst(rowLabels, (a + 1), (b - 1));
        lastRow = rowCount;
      }

      String app = printApp(output, lem, rdg);
      result += app;
      System.out.println(app);

      int colIslNo = listOrderedByCol.get(colCount)[0];
      int rowIslNo = listOrderedByRow.get(rowCount)[0];
      if (colIslNo == rowIslNo) {
        String tekst = getTekst(columnLabels, listOrderedByCol.get(colCount)[1], listOrderedByCol.get(colCount)[3]);
        result += tekst;
        System.out.println(tekst);
        output.println(tekst);
        rowCount++;
        colCount++;
      } else if (colIslNo > rowIslNo) {
        String message = "<!-- er is iets mis -->";
        output.println(message);
        result += message + "\n";
        System.out.println("!!! colIslNo (" + colIslNo + ") > rowIslNo (" + rowIslNo + ")");
        lem = "";
        rdg = "";
        if (listOrderedByCol.get(colCount + 1)[0] < colIslNo) {
          lem = getTekst(columnLabels, listOrderedByCol.get(colCount)[1], listOrderedByCol.get(colCount)[3]);
          rdg = "[VERPLAATST" + colIslNo + "]";
          colCount++;
        } else {
          lem = "[VERPLAATST" + rowIslNo + "]";
          rdg = getTekst(rowLabels, listOrderedByRow.get(rowCount)[2], listOrderedByRow.get(rowCount)[4]);
          rowCount++;
        }
        app = printApp(output, lem, rdg);
        result += app;
        System.out.println(app);
      } else if (colIslNo < rowIslNo) {
//				System.out.println("colIslNo (" +colIslNo + ") < rowIslNo ("+ rowIslNo + ")");
        lem = "";
        rdg = "";
        if (listOrderedByRow.get(rowCount + 1)[0] < rowIslNo) {
          lem = "[VERPLAATST" + rowIslNo + "]";
          rdg = getTekst(rowLabels, listOrderedByRow.get(rowCount)[2], listOrderedByRow.get(rowCount)[4]);
          rowCount++;
        } else {
          lem = getTekst(columnLabels, listOrderedByCol.get(colCount)[1], listOrderedByCol.get(colCount)[3]);
          rdg = "[VERPLAATST" + colIslNo + "]";
          colCount++;
        }
        app = printApp(output, lem, rdg);
        result += app;
        System.out.println(app);
      }
//  		for(Integer[] a : listOrderedByCol) {
//  			if((a[0]-lastCol)>1)
//  				sprong  = true;
//  			if(!sprong)
//  				while(listOrderedByRow.get(rowCount)[0]<a[0]) {
//  					rowCount++;
//  				}
//  			else {
//  				String lem = "";
//  				for(int i=a[1];i<=a[3];i++)
//  					lem += " " + columnLabels.get(i);
//  				result += printApp(output,lem,"[VERPLAATST?"+a[0]+"]");
//  				sprong = false;
//  			}
//  		}
      if (rowCount >= listOrderedByRow.size() || colCount >= listOrderedByCol.size())
        finished = true;
    }
    output.println("</xml>");
    output.flush();
    return result + "</xml>";
  }

  private String drukAfArray(Integer[] integers) {
    String result = "[";
    for (Integer i : integers) {
      result += "," + i;
    }
    return result.replace("[,", "[") + "]";
  }

  private String getTekst(ArrayList<String> rowLabels, Integer start,
                          Integer einde) {
    String result = "";
    for (int i = start; i <= einde; i++) {
      result += " " + rowLabels.get(i);
    }
    return result.trim();
  }

  private String printApp(PrintWriter output, String lem, String rdg) {
    String result = "";
    if (!(lem.isEmpty() && rdg.isEmpty())) {
      if (lem.isEmpty())
        lem = "[WEGGELATEN]";
      if (rdg.isEmpty())
        rdg = "[WEGGELATEN]";
      result += "  <app>" + newLine;
      result += "    <lem>" + lem.trim() + "</lem>" + newLine;
      result += "    <rdg>" + rdg.trim() + "</rdg>" + newLine;
      result += "  </app>" + newLine;
      output.print(result);
    }
    return result;

  }

}