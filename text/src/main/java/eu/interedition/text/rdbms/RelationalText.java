package eu.interedition.text.rdbms;

import com.google.common.base.Objects;
import eu.interedition.text.Text;

import java.util.Date;

public class RelationalText implements Text {
  private long id;
  private Date created;
  private Type type;
  private int length;

  public long getId() {
    return id;
  }

  public void setId(long id) {
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

  public int length() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", Long.toString(id)).toString();
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
    return (id == 0 ? super.hashCode() : Objects.hashCode(id));
  }

}
