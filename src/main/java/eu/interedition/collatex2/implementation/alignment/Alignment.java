package eu.interedition.collatex2.implementation.alignment;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.modifications.Transposition;
import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.ITransposition;

public class Alignment implements IAlignment {
  private final List<IMatch> matches;
  private final List<IGap> gaps;

  public Alignment(final List<IMatch> matches1, final List<IGap> gaps1) {
    // matches1 should be sorted om phraseA.getBeginPosition() 

    this.matches = matches1;
    this.gaps = gaps1;
  }

  public List<IMatch> getMatches() {
    return matches;
  }

  public List<IGap> getGaps() {
    return gaps;
  }

  final Comparator<IMatch> SORT_MATCHES_ON_POSITION_B = new Comparator<IMatch>() {
    public int compare(final IMatch o1, final IMatch o2) {
      return o1.getPhraseB().getBeginPosition() - o2.getPhraseB().getBeginPosition();
    }
  };

  public List<IMatch> getMatchesSortedForB() {
    final List<IMatch> matchesForB = Lists.newArrayList(matches);
    Collections.sort(matchesForB, SORT_MATCHES_ON_POSITION_B);
    return matchesForB;
  }

  @Override
  public List<ITransposition> getTranspositions() {
    final List<IMatch> matchesA = getMatches();
    final List<IMatch> matchesB = getMatchesSortedForB();
    final List<ITransposition> transpositions = Lists.newArrayList();
    for (int i = 0; i < matchesA.size(); i++) {
      final IMatch matchA = matchesA.get(i);
      final IMatch matchB = matchesB.get(i);
      if (!matchA.equals(matchB)) {
        transpositions.add(new Transposition(matchA, matchB));
      }
    }
    return transpositions;
  }

  private static final Predicate<IGap> ADDITION_PREDICATE = new Predicate<IGap>() {
    @Override
    public boolean apply(final IGap gap) {
      return gap.isAddition();
    }
  };

  private static final Predicate<IGap> REPLACEMENT_PREDICATE = new Predicate<IGap>() {
    @Override
    public boolean apply(final IGap gap) {
      return gap.isReplacement();
    }
  };

  //TODO: remove gap.getModification!
  //Modification should know about Gap, not the other way around!
  private static final Function<IGap, IAddition> GAP_TO_ADDITION = new Function<IGap, IAddition>() {
    @Override
    public IAddition apply(final IGap gap) {
      return (IAddition) gap.getModification();
    }
  };

  //TODO: remove gap.getModification!
  //Modification should know about Gap, not the other way around!
  private static final Function<IGap, IReplacement> GAP_TO_REPLACEMENT = new Function<IGap, IReplacement>() {
    @Override
    public IReplacement apply(final IGap gap) {
      return (IReplacement) gap.getModification();
    }
  };

  @Override
  public List<IAddition> getAdditions() {
    return Lists.newArrayList(transform(filter(getGaps(), ADDITION_PREDICATE), GAP_TO_ADDITION));
  }

  @Override
  public List<IReplacement> getReplacements() {
    return Lists.newArrayList(transform(filter(getGaps(), REPLACEMENT_PREDICATE), GAP_TO_REPLACEMENT));
  }

  //      public static Alignment create(final IWitness a, final IWitness b) {
  //        final WitnessSet set = new WitnessSet(a, b);
  //        return set.align();
  //      }

  //	  public void accept(final ModificationVisitor modificationVisitor) {
  //		    for (final Gap gap : gaps) {
  //		      final Modification modification = gap.getModification();
  //		      modification.accept(modificationVisitor);
  //		    }
  //		  }

}
