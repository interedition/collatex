package eu.interedition.text.rdbms;

import com.google.common.base.Objects;
import eu.interedition.text.Text;

public class RelationalText implements Text {
  private long id;
  private Type type;
  private long length;
  private String digest;

  public RelationalText() {
  }

  public RelationalText(RelationalText other) {
    this.id = other.id;
    this.type = other.type;
    this.length = other.length;
    this.digest = other.digest;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }

  public String getDigest() {
    return digest;
  }

  public void setDigest(String digest) {
    this.digest = digest;
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
