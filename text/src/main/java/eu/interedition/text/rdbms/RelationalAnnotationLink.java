package eu.interedition.text.rdbms;

import com.google.common.base.Objects;
import eu.interedition.text.AnnotationLink;
import eu.interedition.text.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RelationalAnnotationLink implements AnnotationLink {
  private long id;
  private QName name;

  public RelationalAnnotationLink(long id, QName name) {
    this.id = id;
    this.name = name;
  }

  public long getId() {
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
    if (obj != null && obj instanceof RelationalAnnotationLink) {
      return id == ((RelationalAnnotationLink)obj).id;
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).addValue(id).addValue(name).toString();
  }
}
