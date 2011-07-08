package eu.interedition.text.rdbms;

import com.google.common.base.Objects;
import eu.interedition.text.AnnotationSet;
import eu.interedition.text.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RelationalAnnotationSet implements AnnotationSet {
  private int id;
  private QName name;

  public RelationalAnnotationSet(int id, QName name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public QName getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof RelationalAnnotationSet) {
      return id == ((RelationalAnnotationSet)obj).id;
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).addValue(id).addValue(name).toString();
  }
}
