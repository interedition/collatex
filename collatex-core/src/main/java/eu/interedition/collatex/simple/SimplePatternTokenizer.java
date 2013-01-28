package eu.interedition.collatex.simple;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimplePatternTokenizer implements Function<String, Iterable<String>> {

  private final Pattern pattern;

  public SimplePatternTokenizer(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Iterable<String> apply(@Nullable String input) {
    final Matcher matcher = pattern.matcher(input);
    final List<String> tokens = Lists.newLinkedList();
    while (matcher.find()) {
      tokens.add(input.substring(matcher.start(), matcher.end()));
    }
    return tokens;
  }

  public static final SimplePatternTokenizer BY_WHITESPACE = new SimplePatternTokenizer(
          Pattern.compile("\\s*?\\S+\\s*]")
  );

  static final String PUNCT = Pattern.quote(".?!,;:");

  public static final SimplePatternTokenizer BY_WS_AND_PUNCT = new SimplePatternTokenizer(
          Pattern.compile("[\\s" + PUNCT + "]*?[^\\s" + PUNCT + "]+[\\s" + PUNCT + "]*")
  );
}
