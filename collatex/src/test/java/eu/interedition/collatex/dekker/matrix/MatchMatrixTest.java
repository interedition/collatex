package eu.interedition.collatex.dekker.matrix;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

public class MatchMatrixTest extends AbstractTest {

  String newLine = System.getProperty("line.separator");

  @Test
  public void testBuildMatrix() {
    SimpleWitness[] sw = createWitnesses("A B C A B", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    assertEquals(5, buildMatrix.colNum());
    assertEquals(5, buildMatrix.rowNum());
    assertTrue(buildMatrix.at(0, 0));
    assertTrue(buildMatrix.at(1, 1));
    assertTrue(buildMatrix.at(1, 4));
    assertTrue(buildMatrix.at(2, 2));
    assertTrue(buildMatrix.at(0, 3));
    assertTrue(buildMatrix.at(3, 0));
    assertTrue(buildMatrix.at(3, 3));
    assertTrue(buildMatrix.at(4, 1));
    assertTrue(buildMatrix.at(4, 4));
    //  	System.out.println(buildMatrix.toHtml());
  }

  @Test
  public void testAsymmatricMatrix() {
    SimpleWitness[] sw = createWitnesses("A B A B C", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    assertTrue(buildMatrix.at(4, 2));
  }

  @Test
  public void testRowLabels() {
    String textD1 = "de het een";
    String textD9 = "het een de";
    SimpleWitness[] sw = createWitnesses(textD1, textD9);
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    ArrayList<String> labels = buildMatrix.rowLabels();
    assertEquals("de", labels.get(0));
    assertEquals("het", labels.get(1));
    assertEquals("een", labels.get(2));
  }

  @Test
  public void testColumnLabels() {
    String textD1 = "de het een";
    String textD9 = "het een de";
    SimpleWitness[] sw = createWitnesses(textD1, textD9);
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    ArrayList<String> labels = buildMatrix.columnLabels();
    assertEquals("het", labels.get(0));
    assertEquals("een", labels.get(1));
    assertEquals("de", labels.get(2));
  }

  @Test
  public void testAllTrues() {
    SimpleWitness[] sw = createWitnesses("A B A B C", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    ArrayList<MatchMatrix.Coordinate> allTrue = buildMatrix.allMatches();
    assertEquals(9, allTrue.size());
    assertTrue(allTrue.contains(new MatchMatrix.Coordinate(0, 0)));
    assertFalse(allTrue.contains(new MatchMatrix.Coordinate(0, 1)));
  }

  @Test
  public void testCoordinates() {
    MatchMatrix.Coordinate a = new MatchMatrix.Coordinate(0, 0);
    MatchMatrix.Coordinate b = new MatchMatrix.Coordinate(0, 0);
    MatchMatrix.Coordinate c = new MatchMatrix.Coordinate(1, 1);
    assertEquals(new MatchMatrix.Coordinate(0, 0), a);
    assertEquals(b, a);
    assertFalse(a.equals(c));
  }

  @Test
  public void testBorders() {
    MatchMatrix.Coordinate a = new MatchMatrix.Coordinate(0, 0);
    MatchMatrix.Coordinate b = new MatchMatrix.Coordinate(1, 1);
    MatchMatrix.Coordinate c = new MatchMatrix.Coordinate(1, 2);
    assertTrue(a.bordersOn(b));
    assertFalse(a.bordersOn(c));
    assertFalse(b.bordersOn(c));
  }

  @Test
  public void testUndirectedIsland() {
    MatchMatrix.Island isl = new MatchMatrix.Island();
    isl.add(new MatchMatrix.Coordinate(0, 0));
    assertEquals(1, isl.size());
    isl.add(new MatchMatrix.Coordinate(0, 0));
    assertEquals(1, isl.size());
    isl.add(new MatchMatrix.Coordinate(1, 0));
    assertEquals(1, isl.size());
    isl.add(new MatchMatrix.Coordinate(2, 2));
    assertEquals(1, isl.size());
    assertTrue(isl.neighbour(new MatchMatrix.Coordinate(1, 1)));
    isl.add(new MatchMatrix.Coordinate(1, 1));
    assertEquals(2, isl.size());
  }

  @Test
  public void testDirectedIsland() {
    MatchMatrix.Island isl = new MatchMatrix.Island();
    isl.add(new MatchMatrix.Coordinate(0, 0));
    assertEquals(1, isl.size());
    assertEquals(0, isl.direction());
    isl.add(new MatchMatrix.Coordinate(1, 1));
    assertEquals(2, isl.size());
    assertEquals(1, isl.direction());
    isl.add(new MatchMatrix.Coordinate(2, 2));
    assertEquals(3, isl.size());
    assertEquals(1, isl.direction());
  }

  @Test
  public void testArchipelago() {
    Archipelago arch = new Archipelago();
    MatchMatrix.Island isl_1 = new MatchMatrix.Island();
    isl_1.add(new MatchMatrix.Coordinate(0, 0));
    isl_1.add(new MatchMatrix.Coordinate(1, 1));
    arch.add(isl_1);
    MatchMatrix.Island isl_2 = new MatchMatrix.Island();
    isl_2.add(new MatchMatrix.Coordinate(2, 2));
    isl_2.add(new MatchMatrix.Coordinate(3, 3));
    arch.add(isl_2);
    assertEquals(2, arch.size());
    assertTrue(isl_1.overlap(isl_2));
    arch.mergeIslands();
    assertEquals(1, arch.size());
  }

  @Test
  public void testArchipelagoRivalIslands() {
    SimpleWitness[] sw = createWitnesses("A B C A B", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    Archipelago archipelago = new Archipelago();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    assertEquals(3, archipelago.size());
    assertEquals(2, archipelago.numOfConflicts());
  }

  @Test
  public void testIslands() {
    SimpleWitness[] sw = createWitnesses("A B C A B", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    ArchipelagoWithVersions islands = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      islands.add(isl);
    }
    //  	System.out.println("islands: "+islands);
    assertEquals(3, islands.size());
    assertEquals(2, islands.numOfConflicts());
    islands.createNonConflictingVersions();
    //		System.out.println("version(0): "+islands.getVersion(0));
    //  	System.out.println("version(1): "+islands.getVersion(1));
    //  	System.out.println("version(2): "+islands.getVersion(2));
    int res = islands.numOfNonConflConstell();
    assertEquals(2, res);
    assertEquals(1, islands.getVersion(0).size());
    //		System.out.println("version(0): "+islands.getVersion(0));
    //  	System.out.println("version(1): "+islands.getVersion(1));
    assertEquals(3, islands.getVersion(1).size());
    assertEquals(null, islands.getVersion(2));
  }

  @Test
  public void testRemovePoints() {
    MatchMatrix.Island di_1 = new MatchMatrix.Island();
    di_1.add(new MatchMatrix.Coordinate(1, 1));
    di_1.add(new MatchMatrix.Coordinate(2, 2));
    MatchMatrix.Island di_2 = new MatchMatrix.Island();
    di_2.add(new MatchMatrix.Coordinate(2, 2));
    MatchMatrix.Island di_3 = di_1.removePoints(di_2);
    assertEquals(2, di_1.size());
    assertEquals(1, di_3.size());
  }

  @Test
  public void testFindCoorOnRowOrCol() {
    MatchMatrix.Island isl_1 = new MatchMatrix.Island();
    isl_1.add(new MatchMatrix.Coordinate(0, 0));
    isl_1.add(new MatchMatrix.Coordinate(1, 1));
    assertEquals(new MatchMatrix.Coordinate(0, 0), isl_1.getCoorOnRow(0));
    assertEquals(new MatchMatrix.Coordinate(1, 1), isl_1.getCoorOnCol(1));
    assertEquals(null, isl_1.getCoorOnCol(4));
  }

  @Test
  public void testOrderedIslands() {
    Archipelago arch = new Archipelago();
    MatchMatrix.Island isl_1 = new MatchMatrix.Island();
    isl_1.add(new MatchMatrix.Coordinate(1, 1));
    isl_1.add(new MatchMatrix.Coordinate(2, 2));
    isl_1.add(new MatchMatrix.Coordinate(3, 3));
    arch.add(isl_1);
    MatchMatrix.Island isl_2 = new MatchMatrix.Island();
    isl_2.add(new MatchMatrix.Coordinate(5, 5));
    isl_2.add(new MatchMatrix.Coordinate(6, 6));
    arch.add(isl_2);
    MatchMatrix.Island isl_3 = new MatchMatrix.Island();
    isl_3.add(new MatchMatrix.Coordinate(8, 8));
    isl_3.add(new MatchMatrix.Coordinate(9, 9));
    isl_3.add(new MatchMatrix.Coordinate(10, 10));
    isl_3.add(new MatchMatrix.Coordinate(11, 11));
    arch.add(isl_3);
    assertEquals(4, arch.get(0).size());
    assertEquals(3, arch.get(1).size());
    assertEquals(2, arch.get(2).size());
  }

  @Test
  public void testOrderedIslands2() {
    ArchipelagoWithVersions arch = new ArchipelagoWithVersions();
    MatchMatrix.Island isl_1 = new MatchMatrix.Island();
    isl_1.add(new MatchMatrix.Coordinate(1, 1));
    isl_1.add(new MatchMatrix.Coordinate(2, 2));
    isl_1.add(new MatchMatrix.Coordinate(3, 3));
    arch.add(isl_1);
    MatchMatrix.Island isl_2 = new MatchMatrix.Island();
    isl_2.add(new MatchMatrix.Coordinate(5, 3));
    isl_2.add(new MatchMatrix.Coordinate(6, 4));
    arch.add(isl_2);
    arch.createNonConflictingVersions();
    //  	System.out.println("versions: ");
    //  	for(Archipelago arch_v : arch.getNonConflVersions()) {
    //  		System.out.println(arch_v);
    //  	}
    assertEquals(2, arch.getNonConflVersions().size());
    MatchMatrix.Island isl_3 = new MatchMatrix.Island();
    isl_3.add(new MatchMatrix.Coordinate(8, 6));
    isl_3.add(new MatchMatrix.Coordinate(9, 7));
    isl_3.add(new MatchMatrix.Coordinate(10, 8));
    isl_3.add(new MatchMatrix.Coordinate(11, 9));
    arch.add(isl_3);
    arch.createNonConflictingVersions();
    assertEquals(28, arch.getVersion(0).value());
    assertEquals(27, arch.getVersion(1).value());
  }

  @Test
  public void testNonConflictingVersions() {
    ArchipelagoWithVersions arch = new ArchipelagoWithVersions();
    MatchMatrix.Island isl_1 = new MatchMatrix.Island();
    isl_1.add(new MatchMatrix.Coordinate(1, 1));
    isl_1.add(new MatchMatrix.Coordinate(2, 2));
    isl_1.add(new MatchMatrix.Coordinate(3, 3));
    arch.add(isl_1);
    arch.createNonConflictingVersions();
    assertEquals(1, arch.numOfNonConflConstell());
  }

  @Test
  public void testIslandValue() {
    MatchMatrix.Island isl_1 = new MatchMatrix.Island();
    isl_1.add(new MatchMatrix.Coordinate(1, 1));
    assertEquals(1, isl_1.value());
    isl_1.add(new MatchMatrix.Coordinate(2, 2));
    assertEquals(5, isl_1.value());
    isl_1.add(new MatchMatrix.Coordinate(3, 3));
    assertEquals(10, isl_1.value());
    MatchMatrix.Island isl_2 = new MatchMatrix.Island();
    isl_2.add(new MatchMatrix.Coordinate(2, 2));
    isl_2.add(new MatchMatrix.Coordinate(1, 3));
    assertEquals(3, isl_2.value());
    Archipelago arch = new Archipelago();
    arch.add(isl_1);
    arch.add(isl_2);
    assertEquals(13, arch.value());
  }

  @Test
  public void testFindGaps() {
    MatchMatrix.Island isl_1 = new MatchMatrix.Island();
    isl_1.add(new MatchMatrix.Coordinate(1, 1));
    isl_1.add(new MatchMatrix.Coordinate(2, 2));
    MatchMatrix.Island isl_2 = new MatchMatrix.Island();
    isl_2.add(new MatchMatrix.Coordinate(4, 4));
    isl_2.add(new MatchMatrix.Coordinate(5, 5));
    ArchipelagoWithVersions arch = new ArchipelagoWithVersions();
    arch.add(isl_1);
    arch.add(isl_2);
    ArrayList<MatchMatrix.Coordinate> list = new ArrayList<MatchMatrix.Coordinate>();
    list.add(new MatchMatrix.Coordinate(0, 0));
    list.add(new MatchMatrix.Coordinate(7, 7));
    ArrayList<MatchMatrix.Coordinate> gaps = arch.findGaps(list);
    assertEquals(4, gaps.size());
    assertEquals(new MatchMatrix.Coordinate(1, 1), gaps.get(0));
    assertEquals(new MatchMatrix.Coordinate(5, 5), gaps.get(3));
  }

  @Test
  @Ignore
  public void testArchipelagoGaps() {
    SimpleWitness[] sw = createWitnesses("A B E F C D G H", "A B C D E F G H");
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    assertEquals(4, archipelago.size());
    String result = "";
    try {
      PrintWriter pw = new PrintWriter(new File("exampleOutput.txt"));
      result = archipelago.createXML(buildMatrix, pw);
      pw.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    String expected = "<xml>" + newLine + "a b " + newLine + "  <app>" + newLine + "    <lem>[WEGGELATEN]</lem>" + newLine + "    <rdg>e f</rdg>" + newLine + "  </app>" + newLine + " c d " + newLine + "  <app>" + newLine + "    <lem>e f</lem>" + newLine + "    <rdg>[WEGGELATEN]</rdg>" + newLine + "  </app>" + newLine + " g h " + newLine + "</xml>";
    assertEquals(expected.length(), result.length());
    ArrayList<MatchMatrix.Coordinate> list = new ArrayList<MatchMatrix.Coordinate>();
    ArrayList<MatchMatrix.Coordinate> gaps = archipelago.createNonConflictingVersion().findGaps(list);
    //  	System.out.println(buildMatrix.toHtml(archipelago.createFirstVersion()));
    assertEquals(6, gaps.size());

    sw = createWitnesses("A J K D E F L M I", "A B C D E F G H I");
    vg = collate(sw[0]);
    buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    archipelago = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    assertEquals(3, archipelago.size());
    //  	System.out.println(buildMatrix.toHtml(archipelago.createFirstVersion()));
    result = "";
    try {
      PrintWriter pw = new PrintWriter(new File("exampleOutput.txt"));
      result = archipelago.createXML(buildMatrix, pw);
      pw.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    expected = "<xml>" + newLine + "a " + newLine + "  <app>" + newLine + "    <lem>b c</lem>" + newLine + "    <rdg>j k</rdg>" + newLine + "  </app>" + newLine + " d e f " + newLine + "  <app>" + newLine + "    <lem>g h</lem>" + newLine + "    <rdg>l m</rdg>" + newLine + "  </app>" + newLine + " i " + newLine + "</xml>";
    assertEquals(expected, result);
  }

  @Test
  @Ignore
  public void testArchipelagoGapsRealText() {
    SimpleWitness[] sw = createWitnesses("Op den Atlantischen Oceaan voer een groote stoomer. Onder de", "Op de Atlantische Oceaan voer een ontzaggelijk zeekasteel. Onder de");
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    assertEquals(4, archipelago.size());
    String result = "";
    try {
      PrintWriter pw = new PrintWriter(new File("exampleOutput.txt"));
      result = archipelago.createXML(buildMatrix, pw);
      pw.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    String expected = "<xml>" + newLine + "op " + newLine + "  <app>" + newLine + "    <lem>de atlantische</lem>" + newLine + "    <rdg>den atlantischen</rdg>" + newLine + "  </app>" + newLine + " oceaan voer een " + newLine + "  <app>" + newLine + "    <lem>ontzaggelijk zeekasteel</lem>" + newLine + "    <rdg>groote stoomer</rdg>" + newLine + "  </app>" + newLine + " onder de " + newLine + "</xml>";
    assertEquals(expected.substring(0, 10), result.substring(0, 10));
    assertEquals(expected, result);
    ArrayList<MatchMatrix.Coordinate> list = new ArrayList<MatchMatrix.Coordinate>();
    ArrayList<MatchMatrix.Coordinate> gaps = archipelago.createNonConflictingVersion().findGaps(list);
    assertEquals(4, gaps.size());
  }

  @Test
  @Ignore
  public void testArchipelagoGapsRealText2() {
    SimpleWitness[] sw = createWitnesses("Op den Atlantischen Oceaan voer een groote stoomer. Onder de velen aan boojrd bevond zich een bruine, korte dikke man. <i>JSg</i> werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels", "op	de	atlantische	oceaan	voer	een	ontzaggelijk	zeekasteel	onder	de	vele	passagiers	aan	boord	bevond	zich	een	bruine	korte	dikke	man	hij	werd	nooit	zonder	sigaar	gezien	zijn	pantalon	had	lijnrechte	vouwen	in	de	pijpen	maar	zat	toch	altijd	vol	rimpels");
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    assertEquals(12, archipelago.size());
    String result = "";
    try {
      PrintWriter pw = new PrintWriter(new File("exampleOutput.txt"));
      result = archipelago.createXML(buildMatrix, pw);
      pw.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    String expected = "<xml>" + newLine + "op " + newLine + "  <app>" + newLine + "    <lem>de atlantische</lem>" + newLine + "    <rdg>den atlantischen</rdg>" + newLine + "  </app>" + newLine + " oceaan voer een " + newLine + "  <app>" + newLine + "    <lem>ontzaggelijk zeekasteel</lem>" + newLine + "    <rdg>groote stoomer</rdg>" + newLine + "  </app>" + newLine + " onder de " + newLine + "  <app>" + newLine + "    <lem>vele passagiers</lem>" + newLine + "    <rdg>velen</rdg>" + newLine + "  </app>" + newLine + " aan " + newLine + "  <app>" + newLine + "    <lem>boord</lem>" + newLine + "    <rdg>boojrd</rdg>" + newLine + "  </app>" + newLine + " bevond zich een bruine korte dikke man " + newLine + "  <app>" + newLine + "    <lem>hij</lem>" + newLine + "    <rdg>ijsgi</rdg>" + newLine + "  </app>" + newLine + " werd nooit zonder sigaar gezien zijn pantalon had lijnrechte vouwen in de pijpen maar zat toch altijd vol rimpels " + newLine + "</xml>";
    assertEquals(expected, result);
    ArrayList<MatchMatrix.Coordinate> gaps = archipelago.createNonConflictingVersion().findGaps();
    assertEquals(10, gaps.size());
  }

  @Test
  public void testArchipelagoGapsRealText3() {
    //Tekst D9
    String tekstD9 = "Het werd avond en de kleine man ging dineren. Hij zat naast een demi&KOP+mondaine, haar naam was Vera, zij maakte hem het hof. Aan het dessert presenteerde zij hem een haltatgeschilde banaan. Maar de kleine man bedankte zo hof&WEG+ felijk als hij kon. Hij had liever een appel.<p/>" + "&GED+ Ik kan geen bananen zien, bekende hij, behalve aan trossen, groen, op weg naar de koelwagentrein.<p/>" + "De wenkbrauwen van de demi&KOP+mondaine gingen rechtopstaan en leken op vraagtekens. Aanleiding tot een interessant gedeelte in hun gesprek.<p/>" + "&GED+ Ik ben planter, vertelde de korte bruine man, bananenplanter in Nicaragua. &APO+<p/>" + "Uit zijn zak haalde hij een rolletje te voorschijn dat hij opzettelijk bij zich gestoken had. Het was een toto van een halve meter lang: zijn indiaanse vrouwen en kinderen.<p/>";
    //Tekst D1
    String tekstD1 = "<b>Het</b> werd avond en de kleine bruine man ging dineeren. Hij zat naast een demi&KOP+mondaine die Vera heette en hem het hof maakte. Aan het dessert bood zij hem een half afgeschilde banaan. Maar de kleine man bedankte zoo hoffelijk als hij kon. Hij had liever een appel. @Ik kan geen bananen zien,# bekende hij, @behalve aan trossen, groen, op weg naar den koelwagentrein.# Vera&APO+s wenkbrauwen kwamen in vorm nog meer die van vraagteekens nabij. Het werd aanleiding tot een interessant gedeelte van hun gesprek: @Ik ben planter#, vertelde de korte bruine man, @bananenplanter in Nicaragua#. Uit zijn zak haalde hij een rolletje te voorschijn (dat hij opzettelijk bij zich gestoken had). Het was een foto, een halve meter lang:<p/>" + "zijn Indiaansche vrouwen en kinderen.<p/>";
    SimpleWitness[] sw = createWitnesses(tekstD1, tekstD9);
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    assertEquals(146, archipelago.size());
    String result = "";
    try {
      PrintWriter pw = new PrintWriter(new File("exampleOutput.txt"));
      result = archipelago.createXML(buildMatrix, pw);
      pw.println(buildMatrix.toHtml(archipelago.createNonConflictingVersion()));
      pw.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    System.out.println(result);
  }

  @Test
  @Ignore
  public void testArchipelagoGapsRealText4() {
    String tekstD1 = "";
    try {
      FileInputStream fstream = new FileInputStream("C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\conserve\\Conserve_D1_fragment.txt");
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String invoer;
      while ((invoer = br.readLine()) != null) {
        tekstD1 += invoer;
      }
      in.close();
    } catch (Exception e) {}
    String tekstD9 = "";
    try {
      FileInputStream fstream = new FileInputStream("C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\conserve\\Conserve_D9_fragment.txt");
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String invoer;
      while ((invoer = br.readLine()) != null) {
        tekstD9 += invoer;
      }
      in.close();
    } catch (Exception e) {}
    SimpleWitness[] sw = createWitnesses(tekstD1, tekstD9);
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    //    assertEquals(146,archipelago.size());
    String result = "";
    try {
      PrintWriter pw = new PrintWriter(new File("exampleOutput_test4.txt"));
      System.out.println("A");
      result = archipelago.createXML(buildMatrix, pw);
      assertEquals("", result);
      pw.println(buildMatrix.toHtml(archipelago.createNonConflictingVersion()));
      pw.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    System.out.println(result);
  }

  @Test
  @Ignore
  public void testArchipelagoGapsRealText5() {
    String tekstD1 = "Op den Atlantischen Oceaan voer een groote stoomer ... Zij die over de railing hingen en recht naar beneden zagen, konden vaststellen dat het schip vorderde";
    String tekstD9 = "Over de Atlantische Oceaan voer een grote stomer ... Wie over de reling hing en recht naar beneden keek, kon vaststellen dat het schip vorderde";
    SimpleWitness[] sw = createWitnesses(tekstD1, tekstD9);
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    String result = "";
    try {
      PrintWriter pw = new PrintWriter(new File("exampleOutput_test5.txt"));
      result = archipelago.createXML(buildMatrix, pw);
      assertEquals("", result);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

}
