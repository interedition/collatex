package eu.interedition.collatex.dekker;

import java.util.stream.IntStream;

public class LCP_Interval {
  // length = number of tokens in this block of text
  int length;
  // start = start position in suffix array
  int start;
  // end = end position in suffix array
  int end;

  public LCP_Interval(int suffix_start_position, int length) {
    this.start = suffix_start_position;
    this.length = length;
  }

  public LCP_Interval(int start, int end, int length) {
    this.start = start;
    this.end = end;
    this.length = length;
  }

  // depth = number of times this block of text occurrences in the text
  public int depth() {
    if (end == 0) {
      throw new IllegalStateException("LCP interval is unclosed!");
    }
    return this.end - this.start +1;
  }

  // transform lcp interval into int stream range
  public IntStream getAllOccurrencesAsRanges(TokenIndex index) {
      IntStream result = IntStream.empty();
      // with/or without end
      for (int i=start; i < end; i++) {
          // every i is one occurrence
          int token_position = index.suffix_array[i];
          IntStream range = IntStream.range(token_position, token_position + length);
          result = IntStream.concat(result, range);
      }
      return result;
  }

  @Override
  public String toString() {
    return ("LCP interval start at: "+start+" , length: "+this.length+" depth:" + depth());
  }

}
