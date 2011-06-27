package eu.interedition.collatex.persistence;

import eu.interedition.text.Annotation;
import eu.interedition.text.Text;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface TokenizerSettings {
  boolean startingAnnotationsAreBoundary(Text text, int offset, Set<Annotation> annotations);

  boolean emptyAnnotationsAreBoundary(Text text, int offset, Set<Annotation> annotations);

  boolean endingAnnotationsAreBoundary(Text text, int offset, Set<Annotation> annotations);

  boolean isBoundary(Text text, int offset, char c);
}
