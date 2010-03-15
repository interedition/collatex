package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.implementation.indexing.NGram;
import eu.interedition.collatex2.interfaces.IMatch;

public class Match implements IMatch {

	private final NGram a;
	private final NGram b;

	public Match(NGram a, NGram b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public String getNormalized() {
		return a.getNormalized();
	}
}
