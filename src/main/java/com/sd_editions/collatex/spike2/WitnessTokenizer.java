package com.sd_editions.collatex.spike2;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

public class WitnessTokenizer {
  private final StreamTokenizer st;

  public WitnessTokenizer(String witness) {
    StringReader reader = new StringReader(witness);
    st = new StreamTokenizer(reader);
    st.eolIsSignificant(true);
  }

  public boolean hasNextToken() {
    try {
      boolean next = (st.nextToken() != StreamTokenizer.TT_EOF);
      st.pushBack();
      return next;
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
  }

  public String nextToken() {
    try {
      st.nextToken();
      String value = st.sval;
      return value;
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
  }
}
