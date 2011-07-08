package eu.interedition.text.xml;

import eu.interedition.text.Annotation;

import java.util.List;
import java.util.Stack;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface NodePathHandler {

  void newNodePath(Annotation annotation, List<Integer> nodePath);
}
