package eu.interedition.collatex.implementation.matching;

import eu.interedition.collatex.implementation.alignment.VariantGraphWitnessAdapter;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.interfaces.Token;

import java.util.Comparator;

public class EqualityTokenComparator implements Comparator<Token> {

  @Override
  public int compare(Token base, Token witness) {
    final String baseContent = (base instanceof SimpleToken ? ((SimpleToken) base).getNormalized() : ((VariantGraphWitnessAdapter.VariantGraphVertexTokenAdapter) base).getNormalized());
    final String witnessContent = (witness instanceof SimpleToken ? ((SimpleToken) witness).getNormalized() : ((VariantGraphWitnessAdapter.VariantGraphVertexTokenAdapter) witness).getNormalized());
    return baseContent.compareTo(witnessContent);
  }

}
