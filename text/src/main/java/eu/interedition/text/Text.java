package eu.interedition.text;

import java.nio.charset.Charset;
import java.util.Date;

public interface Text {
  final Charset CHARSET = Charset.forName("UTF-8");

  enum Type {
    PLAIN, XML
  }

  Date getCreated();

  Type getType();

  long getLength();

  String getDigest();
}
