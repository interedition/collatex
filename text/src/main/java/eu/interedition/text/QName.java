package eu.interedition.text;

import java.net.URI;

public interface QName extends Comparable<QName> {

  URI getNamespaceURI();

  String getLocalName();
}
