package eu.interedition.text;

import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface QNameSet {
  QName getName();

  SortedSet<QName> getMembers();
}
