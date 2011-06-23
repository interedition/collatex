package eu.interedition.collatex.persistence;

import org.lmnl.Text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Witness {
  private int id;
  private Text source;
  private Text text;
  private Collation collation;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Text getSource() {
    return source;
  }

  public void setSource(Text source) {
    this.source = source;
  }

  public Text getText() {
    return text;
  }

  public void setText(Text text) {
    this.text = text;
  }

  public Collation getCollation() {
    return collation;
  }

  public void setCollation(Collation collation) {
    this.collation = collation;
  }

  @Override
  public int hashCode() {
    return (id == 0 ? super.hashCode() : id);
  }

  @Override
  public boolean equals(Object obj) {
    if (id != 0 && obj != null && obj instanceof Witness) {
      return id == ((Witness) obj).id;
    }
    return super.equals(obj);
  }
}
