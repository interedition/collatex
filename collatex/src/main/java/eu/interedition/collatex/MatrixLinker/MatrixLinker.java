package eu.interedition.collatex.MatrixLinker;

import java.util.Comparator;
import java.util.Map;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.TokenLinker;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import com.google.common.collect.ArrayTable;

public class MatrixLinker implements TokenLinker {

	@Override
	public Map<Token, VariantGraphVertex> link(VariantGraph base,
	    Iterable<Token> witness, Comparator<Token> comparator) {
		// TODO Auto-generated method stub
		
		buildMatrix(base,witness);
		return null;
	}

	protected ArrayTable<VariantGraphVertex,Token,Boolean> buildMatrix(VariantGraph base, Iterable<Token> witness) {
		ArrayTable<VariantGraphVertex, Token, Boolean> arrayTable = ArrayTable.create(base.vertices(), witness);
	  // TODO Auto-generated method stub
		arrayTable.set(0, 0, true);
		return arrayTable;
  }

}
