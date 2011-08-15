package eu.interedition.text.mem;

import com.google.common.base.Preconditions;
import eu.interedition.text.Text;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleText implements Text {
  private final Type type;
  private String content;
  private String digest;

  public SimpleText(Type type, String content) {
    this.type = type;
    setContent(content);
  }

  public SimpleText(Type type) {
    this(type, "");
  }

  public Type getType() {
    return type;
  }

  public String getContent() {
    return content;
  }

  public long getLength() {
    return content.length();
  }

  @Override
  public String getDigest() {
    return digest;
  }


  public void setContent(String content) {
    Preconditions.checkArgument(content != null);
    this.content = content;
    this.digest = DigestUtils.sha512Hex(content);
  }
}
