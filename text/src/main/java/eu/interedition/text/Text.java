package eu.interedition.text;

import java.util.Date;

public interface Text {

  enum Type {
    PLAIN, XML
  }

  Date getCreated();

  Type getType();
}
