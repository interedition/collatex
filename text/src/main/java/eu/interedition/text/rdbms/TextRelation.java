package eu.interedition.text.rdbms;

import com.google.common.base.Objects;
import eu.interedition.text.Annotation;
import eu.interedition.text.Text;

import java.sql.Clob;
import java.util.Set;

public class TextRelation implements Text {
  private int id;
  private Clob content;
  private Set<Annotation> annotations;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Deprecated
  public Clob getContent() {
    return content;
  }

  @Deprecated
  public void setContent(Clob content) {
    this.content = content;
  }

  public Set<Annotation> getAnnotations() {
    return annotations;
  }

  public void setAnnotations(Set<Annotation> annotations) {
    this.annotations = annotations;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", Integer.toString(id)).toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (id != 0 && obj != null && obj instanceof TextRelation) {
      return id == ((TextRelation) obj).id;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return (id == 0 ? super.hashCode() : id);
  }

}
