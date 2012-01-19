package eu.interedition.collatex.MatrixLinker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ArrayTable;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.input.SimpleWitness;
import eu.interedition.collatex.matching.EqualityTokenComparator;

public class MatrixLinkerTest extends AbstractTest {

  @Ignore
	@Test
  public void test1() {
  	SimpleWitness[] sw = createWitnesses("A B C A B","A B C A B");
    int baseWitness = 0;
		int posBaseWitness = 0;
  	int secondWitness = 1;
  	int posSecondWitness = 0;
  	
		VariantGraph vg = collate(sw[posBaseWitness]);
  	MatrixLinker linker = new MatrixLinker();
  	Map<Token, VariantGraphVertex> link = linker.link(vg,sw[1],new EqualityTokenComparator());

		compareWitnesses(sw, baseWitness, posBaseWitness, secondWitness,
        posSecondWitness, link);
  }
  
  @Test
  public void testBuildMatrix() {
  	SimpleWitness[] sw = createWitnesses("A B C A B","A B C A B");
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
  	assertTrue(buildMatrix.at(0, 0));
  	assertTrue(buildMatrix.at(1, 1));
  	assertTrue(buildMatrix.at(1, 4));
  	assertTrue(buildMatrix.at(2, 2));
  	assertTrue(buildMatrix.at(0, 3));
  	assertTrue(buildMatrix.at(3, 0));
  	assertTrue(buildMatrix.at(3, 3));
  	assertTrue(buildMatrix.at(4, 1));
  	assertTrue(buildMatrix.at(4, 4));
  	System.out.println(buildMatrix.toHtml());
  }
  
  @Test
  public void testAsymmatricMatrix() {
  	SimpleWitness[] sw = createWitnesses("A B A B C","A B C A B");
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
  	assertTrue(buildMatrix.at(4, 2));
  }
  
  @Test
  public void testHermansText1() {
  	String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer, de lucht was helder blauw, het water rimpelend satijn.";
  	String textD9 = "Over de Atlantische Oceaan voer een grote stomer. De lucht was helder blauw, het water rimpelend satijn.<p/>";
  	SimpleWitness[] sw = createWitnesses(textD1,textD9);
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
//  	System.out.println(buildMatrix.toHtml());
  }
  
  @Ignore
  @Test
  public void testHermansText2() {
  	String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer. Onder de velen aan boojrd bevond zich een bruine, korte dikke man. <i>JSg</i> werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels.<b> De</b> pantalon werd naar boven toe breed, ontzaggelijk breed; hij omsloot den buik van den kleinen man als een soort balcon.";
  	String textD9 = "Op de Atlantische Oceaan voer een ontzaggelijk zeekasteel. Onder de vele passagiers aan boord, bevond zich een bruine, korte dikke man. Hij werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. De pantalon werd naar boven toe breed, ongelofelijk breed: hij omsloot de buik van de kleine man als een soort balkon.";
  	SimpleWitness[] sw = createWitnesses(textD1,textD9);
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
//  	System.out.println(buildMatrix.toHtml());
  }
  
  @Test
  public void testRowLabels() {
  	String textD1 = "de het een";
  	String textD9 = "het een de";
  	SimpleWitness[] sw = createWitnesses(textD1,textD9);
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
    ArrayList<String> labels = buildMatrix.rowLabels();
    assertEquals("de",labels.get(0));
    assertEquals("het",labels.get(1));
    assertEquals("een",labels.get(2));
  }
  
  @Test
  public void testColumnLabels() {
  	String textD1 = "de het een";
  	String textD9 = "het een de";
  	SimpleWitness[] sw = createWitnesses(textD1,textD9);
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
    ArrayList<String> labels = buildMatrix.columnLabels();
    assertEquals("het",labels.get(0));
    assertEquals("een",labels.get(1));
    assertEquals("de",labels.get(2));
  }
  
  @Test
  public void testAllTrues() {
  	SimpleWitness[] sw = createWitnesses("A B A B C","A B C A B");
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
  	ArrayList<Coordinate> allTrue = buildMatrix.allTrues();
  	assertEquals(9,allTrue.size());
  	assertTrue(allTrue.contains(new Coordinate(0,0)));
  	assertFalse(allTrue.contains(new Coordinate(1,0)));
  }
  
  @Test
  public void testCoordinates() {
  	Coordinate a = new Coordinate(0,0);
  	Coordinate b = new Coordinate(0,0);
  	Coordinate c = new Coordinate(1,1);
  	assertEquals(new Coordinate(0,0),a);
  	assertEquals(b, a);
  	assertFalse(a.equals(c));
  }
  
  @Test
  public void testBorders() {
  	Coordinate a = new Coordinate(0,0);
  	Coordinate b = new Coordinate(1,1);
  	Coordinate c = new Coordinate(2,1);
  	assertTrue(a.borders(b));
  	assertFalse(a.borders(c));
  	assertFalse(b.borders(c));
  }
  
  @Test
  public void testIslands() {
  	SimpleWitness[] sw = createWitnesses("A B C A B","A B C A B");
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
  	ArrayList<ArrayList<Coordinate>> islands = buildMatrix.getIslands();
  	System.out.println("size: "+islands.size());
  	assertEquals(3, islands.size());
  }

	private void compareWitnesses(SimpleWitness[] sw, int baseWitness,
      int posBaseWitness, int secondWitness, int posSecondWitness,
      Map<Token, VariantGraphVertex> link) {
	  List<Token> lt = sw[secondWitness].getTokens();
    VariantGraphVertex variantGraphVertex = link.get(lt.get(posSecondWitness));
    
    Set<Token> tokens = variantGraphVertex.tokens();
    Token next = tokens.iterator().next();
		assertEquals(sw[baseWitness].getTokens().get(posBaseWitness),next);
  }
  
 
}
