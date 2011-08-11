package eu.interedition.text.repository;

import eu.interedition.text.Annotation;
import eu.interedition.text.Text;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface TokenizerSettings {
  boolean startingAnnotationsAreBoundary(Text text, long offset, Set<Annotation> annotations);

  boolean emptyAnnotationsAreBoundary(Text text, long offset, Set<Annotation> annotations);

  boolean endingAnnotationsAreBoundary(Text text, long offset, Set<Annotation> annotations);

  boolean isBoundary(Text text, long offset, char c);
}
