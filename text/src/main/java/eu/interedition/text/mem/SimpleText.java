package eu.interedition.text.mem;

import eu.interedition.text.Annotation;
import eu.interedition.text.Text;

import java.util.Date;
import java.util.HashSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleText extends HashSet<Annotation> implements Text {
  private final Date created;
  private final Type type;
  private String content;

  public SimpleText(Type type, String content) {
    this.created = new Date();
    this.type = type;
    this.content = content;
  }

  public SimpleText(Type type) {
    this(type, "");
  }

  public Date getCreated() {
    return created;
  }

  public Type getType() {
    return type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
