package eu.interedition.text.repository.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.text.QName;
import eu.interedition.text.TextConstants;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.query.Criterion;
import eu.interedition.text.xml.XMLSerializerConfiguration;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLSerializerConfigurationImpl implements XMLSerializerConfiguration {
  private QNameImpl rootName = new QNameImpl(new SimpleQName(TextConstants.INTEREDITION_NS_URI, "root"));
  private Map<String, URI> namespaceMappings = Maps.newHashMap();
  private List<QName> hierarchy = Lists.newArrayList();
  private boolean hierarchyOnly = true;
  private Criterion query = Criteria.any();

  public XMLSerializerConfigurationImpl() {
    namespaceMappings.put("tei", TextConstants.TEI_NS);
    namespaceMappings.put("ie", TextConstants.INTEREDITION_NS_URI);
  }

  @JsonProperty("root")
  @Override
  public QName getRootName() {
    return rootName;
  }

  @JsonProperty("root")
  @JsonDeserialize(as = QNameImpl.class)
  public void setRootName(QNameImpl rootName) {
    this.rootName = rootName;
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

  public boolean isHierarchyOnly() {
    return hierarchyOnly;
  }

  public void setHierarchyOnly(boolean hierarchyOnly) {
    this.hierarchyOnly = hierarchyOnly;
  }

  @JsonProperty("hierarchy")
  @Override
  public List<QName> getHierarchy() {
    return hierarchy;
  }

  @JsonProperty("hierarchy")
  @JsonDeserialize(contentAs = QNameImpl.class)
  public void setHierarchy(List<QName> hierarchy) {
    this.hierarchy = hierarchy;
  }

  @JsonIgnore
  @Override
  public Criterion getQuery() {
    return query;
  }

  @JsonIgnore
  public void setQuery(Criterion query) {
    this.query = query;
  }
}
