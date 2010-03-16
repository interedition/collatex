package eu.interedition.collatex2.implementation.alignment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;

public class Alignment implements IAlignment {

  private final List<IMatch> matches;
  private final List<IGap> gaps;

  public Alignment(final List<IMatch> matches, final List<IGap> gaps) {
    this.matches = matches;
    this.gaps = gaps;
  }

  public List<IMatch> getMatches() {
    return matches;
  }

  public List<IGap> getGaps() {
    return gaps;
  }

  public List<IMatch> getMatchesSortedForB() {
    final Comparator<IMatch> comparator = new Comparator<IMatch>() {
      public int compare(final IMatch o1, final IMatch o2) {
        return o1.getPhraseB().getBeginPosition() - o2.getPhraseB().getBeginPosition();
      }
    };
    final List<IMatch> matchesForB = Lists.newArrayList(matches);
    Collections.sort(matchesForB, comparator);
    return matchesForB;
  }

  //		  public static Alignment create(final IWitness a, final IWitness b) {
  //		    final WitnessSet set = new WitnessSet(a, b);
  //		    return set.align();
  //		  }

  //	  public void accept(final ModificationVisitor modificationVisitor) {
  //		    for (final Gap gap : gaps) {
  //		      final Modification modification = gap.getModification();
  //		      modification.accept(modificationVisitor);
  //		    }
  //		  }

}
