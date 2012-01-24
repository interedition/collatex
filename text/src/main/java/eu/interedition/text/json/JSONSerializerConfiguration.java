package eu.interedition.text.json;

import eu.interedition.text.Name;
import eu.interedition.text.Range;
import eu.interedition.text.query.Criterion;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface JSONSerializerConfiguration {

  Range getRange();

  Map<String, URI> getNamespaceMappings();

  Set<Name> getDataSet();

  Criterion getQuery();
}
