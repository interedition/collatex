package eu.interedition.collatex.visualization;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.match.views.ModificationVisitor;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.collation.Match;

public class Modifications {
  private final List<Transposition> transpositions;
  private final List<Modification> modifications;
  private final Set<Match> matches;

  public Modifications(List<Modification> _modifications, List<Transposition> _transpositions, Set<Match> _matches) {
    this.modifications = _modifications;
    this.transpositions = _transpositions;
    this.matches = _matches;
  }

  public List<Transposition> getTranspositions() {
    return transpositions;
  }

  public List<Modification> getModifications() {
    List<Modification> addedUp = Lists.newArrayList();
    addedUp.addAll(modifications);
    addedUp.addAll(transpositions);
    return addedUp;
  }

  public int size() {
    return getModifications().size();
  }

  public Modification get(int i) {
    return getModifications().get(i);
  }

  public Match getMatchAtBasePosition(int basePosition) {
    for (Match match : matches) {
      if (match.getBaseWord().position == basePosition) return match;
    }
    return null;
  }

  public Match getMatchAtWitnessPosition(int witnessPosition) {
    for (Match match : matches) {
      if (match.getWitnessWord().position == witnessPosition) return match;
    }
    return null;
  }

  public Set<Match> getMatches() {
    return matches;
  }

  public void accept(ModificationVisitor modificationVisitor) {
    for (Modification modification : modifications) {
      modification.accept(modificationVisitor);
    }

  }

}
