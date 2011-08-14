package eu.interedition.text.xml;

import eu.interedition.text.QName;
import eu.interedition.text.query.Criterion;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface XMLSerializerConfiguration {

  QName getRootName();

  Map<String, URI> getNamespaceMappings();

  List<QName> getHierarchy();

  Criterion getQuery();
}
