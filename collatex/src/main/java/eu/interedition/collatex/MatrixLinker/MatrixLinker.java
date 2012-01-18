package eu.interedition.collatex.MatrixLinker;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.TokenLinker;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.matching.Matches;

import com.google.common.collect.ArrayTable;

public class MatrixLinker implements TokenLinker {

	@Override
	public Map<Token, VariantGraphVertex> link(VariantGraph base,
	    Iterable<Token> witness, Comparator<Token> comparator) {
		// TODO Auto-generated method stub
		
		buildMatrix(base,witness,comparator);
		return null;
	}

	protected ArrayTable<VariantGraphVertex,Token,Boolean> buildMatrix(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
		base.rank();
		Matches matches = Matches.between(base.vertices(), witness, comparator);
		ArrayTable<VariantGraphVertex, Token, Boolean> arrayTable = ArrayTable.create(base.vertices(), witness);
    Set<Token> unique = matches.getUnique();
    Set<Token> ambiguous = matches.getAmbiguous();
    int column = 0;
    for(Token t: witness) {
    	if(unique.contains(t)) {
    		arrayTable.set(matches.getAll().get(t).get(0).getRank()-1,column,true);
    	} else {
    		if(ambiguous.contains(t)) {
    			for(VariantGraphVertex vgv : matches.getAll().get(t)) {
    				arrayTable.set(vgv.getRank()-1,column,true);
    			}
    		}
    	}
    	column++;
    }
    return arrayTable;
  }

}
