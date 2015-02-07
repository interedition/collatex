package eu.interedition.collatex.dekker;

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
  
  @Override
  public String toString() {
    return ("LCP interval start at: "+start+" , length: "+this.length+" depth:" + depth());
  }
  
}
