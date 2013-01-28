package eu.interedition.collatex.simple;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import javax.annotation.Nullable;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleTokenNormalizers {

  public static final Function<String, String> LOWER_CASE = new Function<String, String>() {
    @Override
    public String apply(@Nullable String input) {
      return input.toLowerCase();
    }
  };

  public static final Function<String, String> TRIM_WS = new Function<String, String>() {
    @Override
    public String apply(@Nullable String input) {
      return input.trim();
    }
  };

  public static final Function<String, String> TRIM_WS_PUNCT = new Function<String, String>() {

    @Override
    public String apply(@Nullable String input) {
      int start = 0;
      int end = input.length() - 1;
      while (start <= end && isWhitespaceOrPunctuation(input.charAt(start))) {
        start++;
      }
      while (end >= start && isWhitespaceOrPunctuation(input.charAt(end))) {
        end--;
      }
      return input.substring(start, end + 1);
    }

    boolean isWhitespaceOrPunctuation(char c) {
      if (Character.isWhitespace(c)) {
        return true;
      }
      final int type = Character.getType(c);
      return (Character.START_PUNCTUATION == type || Character.END_PUNCTUATION == type || Character.OTHER_PUNCTUATION == type);
    }
  };

  public static final Function<String, String> LC_TRIM_WS_PUNCT = Functions.compose(LOWER_CASE, TRIM_WS_PUNCT);
}
