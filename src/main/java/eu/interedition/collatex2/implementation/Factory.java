package eu.interedition.collatex2.implementation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.alignment.Alignment;
import eu.interedition.collatex2.implementation.indexing.NGram;
import eu.interedition.collatex2.implementation.matching.Match;
import eu.interedition.collatex2.implementation.matching.RealMatcher;
import eu.interedition.collatex2.implementation.matching.worddistance.NormalizedLevenshtein;
import eu.interedition.collatex2.implementation.matching.worddistance.WordDistance;
import eu.interedition.collatex2.implementation.tokenization.NormalizedWitnessBuilder;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class Factory {

	public IWitness createWitness(String sigil, String words) {
		return NormalizedWitnessBuilder.create(sigil, words);
	}

	public IAlignment createAlignment(IWitness a, IWitness b) {
		WordDistance distanceMeasure = new NormalizedLevenshtein();
		Set<IMatch> matches = RealMatcher.findMatches(a, b, distanceMeasure);
		List<IMatch> matchesAsList = Lists.newArrayList(matches);
		List<IGap> gaps = Lists.newArrayList();
		return new Alignment(matchesAsList, gaps);
	}

	public static IMatch createMatch(INormalizedToken baseWord,
			INormalizedToken witnessWord) {
		NGram a = NGram.create(baseWord);
		NGram b = NGram.create(baseWord);
		return new Match(a, b);
	}

	public static IMatch createMatch(INormalizedToken baseWord,
			INormalizedToken witnessWord, float editDistance) {
	throw new RuntimeException("Near matches are not yet supported!");
	}

}
