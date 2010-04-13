package eu.interedition.collatex2.rest.output;

import java.util.List;

import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.ITransposition;

public class StringCallback implements ICallback {
	private static final String BR = "<br/>";
	private final StringBuffer buffer;

	public StringCallback() {
		this.buffer = new StringBuffer();
	}

	@Override
	public void alignment(final IAlignment alignment) {
		displayMatches(alignment);
		final List<IGap> gaps = alignment.getGaps();
		buffer.append(BR + BR).//
				append("gaps: ").//
				append(BR);
		for (final IGap gap : gaps) {
			buffer.append(" ").append(gap.toString()).append(BR);
		}
		buffer.append(BR + BR);
		displayTranspositions(alignment);
		buffer.append(BR + BR);
	}

	private void displayTranspositions(final IAlignment alignment) {
		final List<ITransposition> transpositions = alignment
				.getTranspositions();
				buffer.append("transpositions: " + BR);
		if (transpositions.isEmpty()) {
			buffer.append("none" + BR);
		}
		for (final ITransposition transposition : transpositions) {
			buffer.append(" ").append(transposition.toString()).append(BR);
		}
	}

	private void displayMatches(final IAlignment alignment) {
		buffer.append("matches: " + BR + " - ");
		String splitter = "";
		final List<IMatch> matches = alignment.getMatches();
		for (final IMatch match : matches) {
			buffer.append(splitter).append("\"").append(match.getNormalized())
					.append("\"");
			splitter = BR + " - ";
		}
	}

	public String getResult() {
		return buffer.toString();
	}
}