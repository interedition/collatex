package eu.interedition.text.json;

import com.google.common.collect.BiMap;
import eu.interedition.text.QName;
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

  BiMap<String, URI> getNamespaceMappings();

  Set<QName> getDataSet();

  Criterion getQuery();
}
