package eu.interedition.text.mem;

import eu.interedition.text.QName;
import eu.interedition.text.QNameSet;

import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleQNameSet implements QNameSet {

  private final QName name;
  private final SortedSet<QName> members;

  public SimpleQNameSet(QName name, SortedSet<QName> members) {
    this.name = name;
    this.members = members;
  }

  public QName getName() {
    return name;
  }

  public SortedSet<QName> getMembers() {
    return members;
  }
}
