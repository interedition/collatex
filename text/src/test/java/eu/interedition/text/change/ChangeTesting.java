package eu.interedition.text.change;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextConstants;
import eu.interedition.text.TextConsumer;
import eu.interedition.text.event.AnnotationEventAdapter;
import eu.interedition.text.event.AnnotationEventSource;
import eu.interedition.text.mem.SimpleName;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.util.Annotations;
import eu.interedition.text.util.Ranges;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

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

      final Text testText = text(testFile.toURI());

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
      }, testText, Criteria.or(
              annotationName(new SimpleName(TextConstants.TEI_NS, "add")),
              annotationName(new SimpleName(TextConstants.TEI_NS, "del")),
              annotationName(new SimpleName(TextConstants.TEI_NS, "subst")),
              annotationName(new SimpleName(TextConstants.TEI_NS, "restore"))
      ));

      final Multimap<String,ChangeAdapter> revTypeIndex = Multimaps.index(changes.values(), ChangeAdapter.TO_REV_TYPE);

      final HashSet<String> chosenRevisionTypes = Sets.newHashSet("soon-or-late", "late");

      Set<ChangeAdapter> before = Sets.newHashSet();
      Set<ChangeAdapter> after = Sets.newHashSet();
      for (String revisionType : revTypeIndex.keySet()) {
        (chosenRevisionTypes.contains(revisionType) ? after : before).addAll(revTypeIndex.get(revisionType));
      }

      final List<Set<ChangeAdapter>> versions = Lists.newArrayList(before, after);
      final Iterable<SortedSet<Range>> removedRanges = new Iterable<SortedSet<Range>>() {

        @Override
        public Iterator<SortedSet<Range>> iterator() {
          return new AbstractIterator<SortedSet<Range>>() {
            private int version = 0;

            @Override
            protected SortedSet<Range> computeNext() {
              if (version > versions.size()) {
                return endOfData();
              }

              final SortedSet<Range> rangesToRemove = Sets.newTreeSet();
              String toRemove = "del";
              for (int version = 0; version < versions.size(); version++) {
                if (this.version <= version) {
                  toRemove = "add";
                }

                for (ChangeAdapter changeAdapter : versions.get(version)) {
                  final Name changeName = changeAdapter.getAnnotation().getName();
                  if (toRemove.equals(changeName.getLocalName())) {
                    rangesToRemove.add(changeAdapter.getAnnotation().getRange());
                  }
                }
              }

              this.version++;
              return Ranges.compress(rangesToRemove);
            }
          };
        }
      };

      System.out.printf("%s %s\n", Strings.repeat("=", 100), testFile.getName());
      final SortedSet<Range> rangesToRemove = Iterables.getLast(removedRanges);
      System.out.println(Iterables.toString(rangesToRemove));
      annotationEventSource.listen(new AnnotationEventAdapter() {
        @Override
        public void text(Range r, String text) {
          int removed = 0;
          StringBuffer buf = new StringBuffer(text);
          for (Iterator<Range> it = rangesToRemove.iterator(); it.hasNext(); ) {
            final Range rangeToRemove = it.next();
            if (rangeToRemove.precedes(r)) {
              it.remove();
              continue;
            } else if (rangeToRemove.follows(r)) {
              break;
            }

            final Range overlap = rangeToRemove.overlap(r);
            final Range shifted = overlap.shift(- (r.getStart() + removed));
            buf.replace((int) shifted.getStart(), (int) shifted.getEnd(), "");
            removed += overlap.length();
          }
          System.out.print(buf.toString());
        }
      }, testText, Criteria.none());
      System.out.println();
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
