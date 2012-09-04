package eu.interedition.text.edit;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import eu.interedition.text.TextRange;

import javax.annotation.Nullable;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Change {

  public enum Type {
    INSERT, DELETE, RETAIN
  }

  private final String content;
  private final TextRange range;
  private final Type type;

  public Change(String content, TextRange range, Type type) {
    this.content = content;
    this.range = range;
    this.type = type;
  }


  public String getContent() {
    return content;
  }

  public TextRange getRange() {
    return range;
  }

  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    final Objects.ToStringHelper toStringHelper = Objects.toStringHelper(this).addValue(type);
    if (range != null) {
      toStringHelper.addValue(range);
    }

    if (content != null) {
      toStringHelper.addValue("\"" + content
              .replaceAll("\n", "\\\\n")
              .replaceAll("\r", "\\\\r")
              .replaceAll("\t", "\\\\t")
              .replaceAll("\"", "\\\\\"")
              + "\"");
    }
    return toStringHelper.toString();
  }

  public long length() {
    return (range == null ? content.length() : range.length());
  }

  public static final Predicate<Change> RETAIN_PREDICATE = new Predicate<Change>() {
    @Override
    public boolean apply(@Nullable Change input) {
      return (input.getType() == Type.RETAIN);
    }
  };
}
