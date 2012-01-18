package eu.interedition.collatex.MatrixLinker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
  }
  
  @Test
  public void testAsymmatricMatrix() {
  	SimpleWitness[] sw = createWitnesses("A B A B C","A B C A B");
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
  	assertTrue(buildMatrix.at(4, 2));
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
