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
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextConstants;
import eu.interedition.text.event.TextAdapter;
import eu.interedition.text.mem.SimpleName;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.util.Annotations;
import eu.interedition.text.util.Ranges;
import eu.interedition.text.xml.XML;
import eu.interedition.text.xml.XMLNodePath;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedWriter;
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
  private static final File TEST_FILE_BASE = new File("/Users/gregor/Desktop/change-collection");
  private static final File OUTPUT_FILE = new File(TEST_FILE_BASE, "versions");

  @Test
  public void createTexts() throws IOException, XMLStreamException {
    if (!TEST_FILE_BASE.isDirectory()) {
      return;
    }

    if (OUTPUT_FILE.isDirectory()) {
      Files.deleteRecursively(OUTPUT_FILE);
    }
    OUTPUT_FILE.mkdir();

    for (File testFile : TEST_FILE_BASE.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isFile() && pathname.getName().endsWith(".xml");
      }
    })) {
      final List<ChangeSet> changeSets = Lists.newArrayList();

      Reader xmlStream = null;
      XMLStreamReader xml = null;
      try {
        xmlStream = textRepository.read(source(testFile.toURI())).getInput();
        changeSets.addAll(ChangeSet.readDeclarations(xml = xmlInputFactory.createXMLStreamReader(xmlStream)));
      } finally {
        XML.closeQuietly(xml);
        Closeables.close(xmlStream, false);
      }

      changeSets.add(0, new ChangeSet(""));

      final Text testText = text(testFile.toURI());

      final Multimap<String, ChangeAdapter> changes = HashMultimap.create();
      textRepository.read(testText, Criteria.or(
              annotationName(new SimpleName(TextConstants.TEI_NS, "add")),
              annotationName(new SimpleName(TextConstants.TEI_NS, "del")),
              annotationName(new SimpleName(TextConstants.TEI_NS, "subst")),
              annotationName(new SimpleName(TextConstants.TEI_NS, "restore"))
      ), new TextAdapter() {
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
      });

      final Multimap<String,ChangeAdapter> revTypeIndex = Multimaps.index(changes.values(), ChangeAdapter.TO_REV_TYPE);

      final HashSet<String> chosenRevisionTypes = Sets.newHashSet("soon-or-late", "late");

      Set<ChangeAdapter> before = Sets.newHashSet();
      Set<ChangeAdapter> after = Sets.newHashSet();
      for (String revisionType : revTypeIndex.keySet()) {
        (chosenRevisionTypes.contains(revisionType) ? after : before).addAll(revTypeIndex.get(revisionType));
      }

      final List<Set<ChangeAdapter>> versions = Lists.newArrayList();
      versions.add(before);
      versions.add(after);

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

      int version = 0;
      for (final SortedSet<Range> rangesToRemove : removedRanges) {
        final BufferedWriter out = Files.newWriter(new File(OUTPUT_FILE, testFile.getName().replaceAll("\\.xml$", "-" + (version++) + ".txt")), Text.CHARSET);
        try {
        textRepository.read(testText, Criteria.none(), new TextAdapter() {
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
              final Range shifted = overlap.shift(-(r.getStart() + removed));
              buf.replace((int) shifted.getStart(), (int) shifted.getEnd(), "");
              removed += overlap.length();
            }
            try {
              out.write(buf.toString());
            } catch (IOException e) {
              throw Throwables.propagate(e);
            }
          }
        });
        } finally {
          Closeables.close(out, false);
        }
      }
    }
  }

  private static final Ordering<Annotation> CHANGE_HIERARCHY_ORDERING = Annotations.RANGE_ORDERING.compound(XMLNodePath.ANNOTATION_COMPARATOR);

  private static final TextAdapter PRINTING_TEXT_LISTENER = new TextAdapter() {

    @Override
    public void start(long contentLength) {
      System.out.println(Strings.repeat("=", 100));
    }

    @Override
    public void end() {
      System.out.println();
      System.out.println(Strings.repeat("=", 100));
    }

    @Override
    public void text(Range r, String text) {
      System.out.print(text);
    }
  };
}
