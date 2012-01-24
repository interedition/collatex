package eu.interedition.web.text;

import com.google.common.collect.Maps;
import eu.interedition.text.Name;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.json.JSONSerializerConfiguration;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.query.Criterion;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JSONSerialization implements JSONSerializerConfiguration {
  private Text text;
  private Range range;
  private Map<String, URI> namespaceMappings = Maps.newHashMap();
  private Set<Name> dataSet;
  private Criterion query = Criteria.any();

  @JsonIgnore
  public Text getText() {
    return text;
  }

  @JsonIgnore
  public void setText(Text text) {
    this.text = text;
  }

  @Override
  public Range getRange() {
    return range;
  }

  public void setRange(Range range) {
    this.range = range;
  }

  @JsonProperty("nsMap")
  @Override
  public Map<String, URI> getNamespaceMappings() {
    return namespaceMappings;
  }

  @JsonProperty("nsMap")
  public void setNamespaceMappings(Map<String, URI> namespaceMappings) {
    this.namespaceMappings = namespaceMappings;
  }

  @Override
  public Set<Name> getDataSet() {
    return dataSet;
  }

  @JsonDeserialize(contentAs = Name.class)
  public void setDataSet(Set<Name> dataSet) {
    this.dataSet = dataSet;
  }

  @JsonIgnore
  @Override
  public Criterion getQuery() {
    return query;
  }
}
