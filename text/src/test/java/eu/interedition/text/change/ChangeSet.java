package eu.interedition.text.change;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import eu.interedition.text.Name;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ChangeSet extends HashSet<ChangeAdapter> {
  private static final Name CHANGE_DECL_SET = new Name(URI.create("http://www.tei-c.org/ns/geneticEditions"), "stageNotes");
  private static final Name CHANGE_DECL = new Name(URI.create("http://www.tei-c.org/ns/geneticEditions"), "stageNote");

  public static final Function<ChangeSet,String> TO_ID = new Function<ChangeSet, String>() {
    @Override
    public String apply(ChangeSet input) {
      return input.getId();
    }
  };

  private String id;
  private String type;

  public ChangeSet() {
    super();
  }

  public ChangeSet(String id) {
    super();
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).addValue(id).add("type", type).toString();
  }

  public static List<ChangeSet> readDeclarations(XMLStreamReader xml) throws XMLStreamException {
    List<ChangeSet> declarations = Lists.newArrayList();
    while (xml.hasNext()) {
      switch (xml.next()) {
        case XMLStreamReader.START_ELEMENT:
          if (CHANGE_DECL.equals(new Name(xml.getName()))) {
            final ChangeSet changeSet = new ChangeSet();
            for (int ac = 0; ac < xml.getAttributeCount(); ac++) {
              if ("id".equals(xml.getAttributeLocalName(ac))) {
                changeSet.setId(xml.getAttributeValue(ac).replaceAll("^#", ""));
              } else if ("type".equals(xml.getAttributeLocalName(ac))) {
                changeSet.setType(xml.getAttributeValue(ac));
              }
            }
            if (!Strings.isNullOrEmpty(changeSet.getId())) {
              declarations.add(changeSet);
            }
          }
          break;
        case XMLStreamReader.END_ELEMENT:
          if (CHANGE_DECL_SET.equals(new Name(xml.getName()))) {
            return declarations;
          }
          break;
      }
    }

    return declarations;
  }
}
