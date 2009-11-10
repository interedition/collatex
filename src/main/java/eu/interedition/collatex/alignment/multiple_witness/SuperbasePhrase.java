package eu.interedition.collatex.alignment.multiple_witness;

import com.sd_editions.collatex.match.Subsegment;

import eu.interedition.collatex.input.Phrase;

public class SuperbasePhrase extends Phrase {

  public SuperbasePhrase(final int position, final Subsegment subsegment) {
    super(position, position, subsegment);
  }

  @Override
  public String toString() {
    return "SB: " + getSubsegment().getTitle().toString() + " " + getBeginPosition();
  }

}
