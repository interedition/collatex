package eu.interedition.text.repository.io;

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
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLSerializerConfigurationBean implements XMLSerializerConfiguration {
  private QNameBean rootName = new QNameBean(new SimpleQName(TextConstants.INTEREDITION_NS_URI, "root"));
  private Map<String, URI> namespaceMappings = Maps.newHashMap();
  private List<QName> hierarchy = Lists.newArrayList();
  private boolean hierarchyOnly = true;
  private Criterion query = Criteria.any();

  public XMLSerializerConfigurationBean() {
    namespaceMappings.put("tei", TextConstants.TEI_NS);
    namespaceMappings.put("ie", TextConstants.INTEREDITION_NS_URI);
  }

  @JsonProperty("root")
  @Override
  public QName getRootName() {
    return rootName;
  }

  @JsonProperty("root")
  @JsonDeserialize(as = QNameBean.class)
  public void setRootName(QNameBean rootName) {
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
  @JsonDeserialize(contentAs = QNameBean.class)
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
