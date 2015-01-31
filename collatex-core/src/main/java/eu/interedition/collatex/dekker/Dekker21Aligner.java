package eu.interedition.collatex.dekker;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;

public class Dekker21Aligner extends CollationAlgorithm.Base {
	
	protected int[] lCP_array;

	public Dekker21Aligner(SimpleWitness[] w) {
		// 1. prepare token array
		// 2. We need to prepare the suffix array 
		// 3. LCP array
		// 4. LCP intervals
		List<Token> token_array = Lists.newArrayList();
		for (SimpleWitness witness : w) {
			for (Token t : witness) {
				token_array.add(t);
			}
			//TODO: add witness separation marker token
		}
		Comparator<Token> comparator = new SimpleTokenNormalizedFormComparator();
		SuffixData suffixData = SuffixArrays.createWithLCP(token_array.toArray(new Token[0]), new SAIS(), comparator);
		lCP_array = suffixData.getLCP();
	}

	@Override
	public void collate(VariantGraph against, Iterable<Token> witness) {
		// TODO Auto-generated method stub
		
	}

}
