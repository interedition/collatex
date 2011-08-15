package eu.interedition.text;

import java.nio.charset.Charset;
import java.util.Date;

public interface Text {
  final Charset CHARSET = Charset.forName("UTF-8");

  enum Type {
    TXT, XML
  }

  Type getType();

  long getLength();

  String getDigest();
}
