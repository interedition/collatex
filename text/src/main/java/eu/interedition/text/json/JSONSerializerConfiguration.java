package eu.interedition.text.json;

import eu.interedition.text.TextTarget;
import eu.interedition.text.query.QueryCriterion;

import java.net.URI;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface JSONSerializerConfiguration {

  TextTarget getRange();

  Map<String, URI> getNamespaceMappings();

  QueryCriterion getQuery();
}
