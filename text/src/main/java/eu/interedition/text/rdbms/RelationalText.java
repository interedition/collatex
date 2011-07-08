package eu.interedition.text.rdbms;

import com.google.common.base.Objects;
import eu.interedition.text.Annotation;
import eu.interedition.text.Text;

import java.sql.Clob;
import java.util.Date;
import java.util.Set;

public class RelationalText implements Text {
  private int id;
  private Date created;
  private Type type;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", Integer.toString(id)).toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (id != 0 && obj != null && obj instanceof RelationalText) {
      return id == ((RelationalText) obj).id;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return (id == 0 ? super.hashCode() : id);
  }

}
