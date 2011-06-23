package eu.interedition.collatex.persistence;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Collation {
  private int id;
  private Set<Witness> witnesses;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Set<Witness> getWitnesses() {
    return witnesses;
  }

  public void setWitnesses(Set<Witness> witnesses) {
    this.witnesses = witnesses;
  }

  @Override
  public int hashCode() {
    return (id == 0 ? super.hashCode() : id);
  }

  @Override
  public boolean equals(Object obj) {
    if (id != 0 && obj != null && obj instanceof Collation) {
      return id == ((Collation) obj).id;
    }
    return super.equals(obj);
  }
}
