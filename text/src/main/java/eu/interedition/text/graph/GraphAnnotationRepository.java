package eu.interedition.text.graph;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.query.Criterion;
import eu.interedition.text.util.AbstractAnnotationRepository;
import eu.interedition.text.util.QNames;
import org.apache.lucene.search.Query;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static com.google.common.collect.Iterables.transform;
import static eu.interedition.text.graph.GraphAnnotation.FROM_NODE;
import static eu.interedition.text.graph.TextRelationshipType.ANNOTATES;
import static eu.interedition.text.graph.TextRelationshipType.NAMES;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphAnnotationRepository extends AbstractAnnotationRepository {
  protected static final Logger LOG = LoggerFactory.getLogger(GraphAnnotationRepository.class);

  private GraphDatabaseService db;
  private QNameRepository nameRepository;

  private final GraphQueryCriteriaTranslator queryCriteriaTranslator = new GraphQueryCriteriaTranslator();

  @Required
  public void setGraphDataSource(GraphDataSource ds) {
    this.db = ds.getGraphDatabaseService();
  }

  @Required
  public void setNameRepository(QNameRepository nameRepository) {
    this.nameRepository = nameRepository;
  }

  @Override
  public Iterable<Annotation> create(Iterable<Annotation> annotations) {
    final List<Annotation> created = Lists.newArrayListWithExpectedSize(Iterables.size(annotations));
    final Index<Node> index = annotationIndex();
    for (Annotation a : annotations) {
      final Range range = a.getRange();

      final Node textNode = ((GraphText) a.getText()).getNode();
      final Node nameNode = ((GraphQName) nameRepository.get(a.getName())).getNode();
      final Node annotationNode = db.createNode();

      annotationNode.setProperty(GraphAnnotation.PROP_RANGE_START, range.getStart());
      annotationNode.setProperty(GraphAnnotation.PROP_RANGE_END, range.getEnd());

      annotationNode.createRelationshipTo(textNode, ANNOTATES);
      nameNode.createRelationshipTo(annotationNode, NAMES);

      index.add(annotationNode, GraphAnnotation.PROP_ID, annotationNode.getId());
      index.add(annotationNode, GraphAnnotation.PROP_TEXT, textNode.getId());
      index.add(annotationNode, GraphAnnotation.PROP_RANGE_START, range.getStart());
      index.add(annotationNode, GraphAnnotation.PROP_RANGE_END, range.getEnd());
      index.add(annotationNode, GraphAnnotation.PROP_RANGE_LENGTH, range.length());

      created.add(new GraphAnnotation(annotationNode));
    }
    return created;
  }

  protected Index<Node> annotationIndex() {
    return db.index().forNodes("annotations");
  }

  @Override
  public Iterable<Annotation> find(Criterion criterion) {
    final Query query = queryCriteriaTranslator.toQuery(criterion);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Query annotations with '{}'", query);
    }
    return transform(annotationIndex().query(query), FROM_NODE);
  }

  @Override
  public void delete(Iterable<Annotation> annotations) {
    final Index<Node> index = annotationIndex();
    for (GraphAnnotation a : Iterables.filter(annotations, GraphAnnotation.class)) {
      final Node node = a.getNode();

      index.remove(node);

      node.getSingleRelationship(ANNOTATES, OUTGOING).delete();
      node.getSingleRelationship(NAMES, INCOMING).delete();
      node.delete();
    }
  }

  @Override
  public void delete(Criterion criterion) {
    delete(find(criterion));
  }

  @Override
  public Map<Annotation, Map<QName, String>> find(Criterion criterion, Set<QName> names) {
    final List<QName> nameList = (names == null ? null : Lists.newArrayList(names));
    final List<String> attrNames = (names == null ? null : Lists.transform(nameList, QNames.TO_STRING));

    final Iterable<GraphAnnotation> annotations = Iterables.filter(find(criterion), GraphAnnotation.class);
    final Map<Annotation, Map<QName, String>> result = Maps.newLinkedHashMap();

    if (names != null && names.isEmpty()) {
      for (GraphAnnotation a : annotations) {
        result.put(a, Maps.<QName, String>newHashMap());
      }
    } else {
      for (GraphAnnotation a : annotations) {
        final Map<QName, String> attributes = Maps.newLinkedHashMap();
        final Node node = a.getNode();
        if (attrNames == null) {
          for (String propertyKey : node.getPropertyKeys()) {
            try {
              attributes.put(QNames.fromString(propertyKey), node.getProperty(propertyKey).toString());
            } catch (IllegalArgumentException e) {
            }
          }
        } else {
          for (int ac = 0; ac < attrNames.size(); ac++) {
            attributes.put(nameList.get(ac), node.getProperty(attrNames.get(ac).toString()).toString());
          }
        }
        result.put(a, attributes);
      }
    }
    return result;
  }

  @Override
  public void set(Map<Annotation, Map<QName, String>> data) {
    for (GraphAnnotation a : Iterables.filter(data.keySet(), GraphAnnotation.class)) {
      final Node node = a.getNode();
      final Map<QName, String> attributes = data.get(a);
      for (QName an : attributes.keySet()) {
        node.setProperty(an.toString(), attributes.get(an));
      }
    }
  }

  @Override
  public void unset(Map<Annotation, Iterable<QName>> data) {
    for (GraphAnnotation a : Iterables.filter(data.keySet(), GraphAnnotation.class)) {
      final Node node = a.getNode();
      for (QName an : data.get(a)) {
        node.removeProperty(an.toString());
      }
    }
  }

  @Override
  protected SortedSet<QName> getNames(Text text) {
    SortedSet<QName> names = Sets.newTreeSet();
    for (Relationship ar : ((GraphText) text).getNode().getRelationships(ANNOTATES, INCOMING)) {
      names.add(new GraphAnnotation(ar.getStartNode()).getName());
    }
    return names;
  }
}
