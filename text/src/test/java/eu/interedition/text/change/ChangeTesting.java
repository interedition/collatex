package eu.interedition.text.change;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.io.CharStreams;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Annotation;
import eu.interedition.text.TextConstants;
import eu.interedition.text.TextConsumer;
import eu.interedition.text.event.AnnotationEventAdapter;
import eu.interedition.text.event.AnnotationEventSource;
import eu.interedition.text.mem.SimpleName;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.util.Annotations;
import eu.interedition.text.xml.XML;
import eu.interedition.text.xml.XMLNodePath;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static eu.interedition.text.query.Criteria.annotationName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ChangeTesting extends AbstractTestResourceTest {
  private static final File[] TEST_FILES = new File("/Users/gregor/Desktop/change-collection").listFiles(new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.isFile() && pathname.getName().endsWith(".xml");
    }
  });

  @Autowired
  private AnnotationEventSource annotationEventSource;

  @Test
  public void createTexts() throws IOException {
    for (File testFile : TEST_FILES) {
      final List<ChangeSet> changeSets = Lists.newArrayList();
      textRepository.read(source(testFile.toURI()), new TextConsumer() {
        @Override
        public void read(Reader content, long contentLength) throws IOException {
          XMLStreamReader xml = null;
          try {
            changeSets.addAll(ChangeSet.readDeclarations(xml = xmlInputFactory.createXMLStreamReader(content)));
          } catch (XMLStreamException e) {
            Throwables.propagate(e);
          } finally {
            XML.closeQuietly(xml);
          }
        }
      });
      changeSets.add(0, new ChangeSet(""));

      final Multimap<String, ChangeAdapter> changes = HashMultimap.create();
      annotationEventSource.listen(new AnnotationEventAdapter() {
        private Deque<ChangeAdapter> parents = new ArrayDeque<ChangeAdapter>();

        @Override
        public void start(long offset, Iterable<Annotation> annotations) {
          for (Annotation annotation : CHANGE_HIERARCHY_ORDERING.immutableSortedCopy(annotations)) {
            final ChangeAdapter change = new ChangeAdapter(annotation);

            final String revisionType = change.getRevisionType();
            if (isNullOrEmpty(revisionType)) {
              final ChangeAdapter parent = Iterators.find(parents.descendingIterator(), ChangeAdapter.HAS_REV_TYPE, null);
              if (parent != null) {
                change.setRevisionType(parent.getRevisionType());
              }
            }
            if (isNullOrEmpty(change.getChangeSetRef())) {
              final ChangeAdapter parent = Iterators.find(parents.descendingIterator(), ChangeAdapter.HAS_CHANGE_SET_REF, null);
              if (parent != null) {
                change.setChangeSetRef(parent.getChangeSetRef());                
              }
              changes.put((parent == null ? "" : parent.getChangeSetRef()), change);
            }
            changes.put(firstNonNull(change.getChangeSetRef(), ""), change);

            parents.add(change);
          }
        }

        @Override
        public void end(long offset, Iterable<Annotation> annotations) {
          parents.removeAll(Lists.newArrayList(Iterables.transform(annotations, ChangeAdapter.ADAPT)));
        }
      }, text(testFile.toURI()), Criteria.or(
              annotationName(new SimpleName(TextConstants.TEI_NS, "add")),
              annotationName(new SimpleName(TextConstants.TEI_NS, "del")),
              annotationName(new SimpleName(TextConstants.TEI_NS, "subst")),
              annotationName(new SimpleName(TextConstants.TEI_NS, "restore"))
      ));

      final Multimap<String,ChangeAdapter> revTypeIndex = Multimaps.index(changes.values(), ChangeAdapter.TO_REV_TYPE);

      System.out.printf("%s %s\n", Strings.repeat("=", 100), testFile.getName());
      System.out.printf("%d changes\n", changes.size());
      for (String revType : Ordering.natural().immutableSortedCopy(revTypeIndex.keySet())) {
        System.out.printf("%s %s\n", Strings.repeat("-", 50), revType);
        System.out.println(Joiner.on("\n").join(CHANGE_HIERARCHY_ORDERING.immutableSortedCopy(Iterables.transform(revTypeIndex.get(revType), ChangeAdapter.TO_ANNOTATION))));
      }
      //textRepository.read(testText, PRINTING_TEXT_CONSUMER);
    }
  }

  private static final Ordering<Annotation> CHANGE_HIERARCHY_ORDERING = Annotations.RANGE_ORDERING.compound(XMLNodePath.ANNOTATION_COMPARATOR);

  private static final TextConsumer PRINTING_TEXT_CONSUMER = new TextConsumer() {
    @Override
    public void read(Reader content, long contentLength) throws IOException {
      System.out.println(Strings.repeat("=", 100));
      CharStreams.copy(content, System.out);
      System.out.println(Strings.repeat("=", 100));
    }
  };
}
