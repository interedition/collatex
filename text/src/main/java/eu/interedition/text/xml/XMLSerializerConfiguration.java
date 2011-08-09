package eu.interedition.text.xml;

import eu.interedition.text.QName;
import eu.interedition.text.query.Criterion;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface XMLSerializerConfiguration {

  QName getRootName();

  Map<String, URI> getNamespaceMappings();

  Set<QName> getHierarchy();

  Criterion getQuery();
}
