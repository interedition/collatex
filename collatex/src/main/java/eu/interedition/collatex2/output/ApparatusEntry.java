package eu.interedition.collatex2.output;

import java.util.List;
import java.util.Set;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;

public class ApparatusEntry {

  private final List<String> sigli;
  private final Multimap<String, INormalizedToken> sigilToTokens;

  public ApparatusEntry(final List<String> sigli) {
    this.sigli = sigli;
    this.sigilToTokens = LinkedHashMultimap.create();
  }

  public void addToken(final String sigil, final INormalizedToken token) {
    sigilToTokens.put(sigil, token);
  }

  public boolean containsWitness(final String sigil) {
    return sigilToTokens.containsKey(sigil);
  }

  public IPhrase getPhrase(final String witnessId) {
    return new Phrase(Lists.newArrayList(sigilToTokens.get(witnessId)));
  }

  public List<String> getSigli() {
    return sigli;
  }

  public Set<String> getEmptyCells() {
    final Set<String> emptySigli = Sets.newLinkedHashSet(sigli);
    emptySigli.removeAll(sigilToTokens.keySet());
    return emptySigli;
  }

  public boolean hasEmptyCells() {
    return sigli.size() != sigilToTokens.keySet().size();
  }
}
