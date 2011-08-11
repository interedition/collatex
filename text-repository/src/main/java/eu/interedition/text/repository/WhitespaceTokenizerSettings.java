package eu.interedition.text.repository;

import eu.interedition.text.Annotation;
import eu.interedition.text.Text;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class WhitespaceTokenizerSettings implements TokenizerSettings {

  private final boolean breakOnAnnotationBoundary;

  public WhitespaceTokenizerSettings(boolean breakOnAnnotationBoundary) {
    this.breakOnAnnotationBoundary = breakOnAnnotationBoundary;
  }

  public WhitespaceTokenizerSettings() {
    this(false);
  }

  @Override
  public boolean startingAnnotationsAreBoundary(Text text, long offset, Set<Annotation> annotations) {
    return breakOnAnnotationBoundary;
  }

  @Override
  public boolean emptyAnnotationsAreBoundary(Text text, long offset, Set<Annotation> annotations) {
    return breakOnAnnotationBoundary;
  }

  @Override
  public boolean endingAnnotationsAreBoundary(Text text, long offset, Set<Annotation> annotations) {
    return breakOnAnnotationBoundary;
  }

  @Override
  public boolean isBoundary(Text text, long offset, char c) {
    return Character.isWhitespace(c);
  }
}
