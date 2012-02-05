package eu.interedition.text.change;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import eu.interedition.text.Annotation;
import eu.interedition.text.mem.SimpleName;
import org.codehaus.jackson.JsonNode;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ChangeAdapter {
  private static final String REVISION_TYPE = new SimpleName("http://www.faustedition.net/ns", "revType").toString();
  private static final String CHANGE_SET_REF = new SimpleName("http://www.tei-c.org/ns/geneticEditions", "stage").toString();

  private final Annotation annotation;
  private String revisionType;
  private String changeSetRef;

  public ChangeAdapter(Annotation annotation) {
    this.annotation = annotation;
  }

  public Annotation getAnnotation() {
    return annotation;
  }

  public String getChangeSetRef() {
    if (changeSetRef != null) {
      return changeSetRef;
    }

    final JsonNode data = annotation.getData();
    if (data.isObject() && data.has(CHANGE_SET_REF)) {
      changeSetRef = data.get(CHANGE_SET_REF).getTextValue().replaceAll("^#", "");
    }

    return changeSetRef;
  }

  public void setChangeSetRef(String changeSetRef) {
    this.changeSetRef = changeSetRef;
  }

  public String getRevisionType() {
    if (revisionType != null) {
      return revisionType;
    }
    final JsonNode data = annotation.getData();
    if (data.isObject() && data.has(REVISION_TYPE)) {
      revisionType = data.get(REVISION_TYPE).getTextValue();
    }

    return revisionType;
  }

  public void setRevisionType(String revisionType) {
    this.revisionType = revisionType;
  }

  @Override
  public int hashCode() {
    return annotation.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof ChangeAdapter) {
      return annotation.equals(((ChangeAdapter)obj).annotation);
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return annotation.toString();
  }

  static final Function<Annotation, ChangeAdapter> ADAPT = new Function<Annotation, ChangeAdapter>() {
    @Override
    public ChangeAdapter apply(Annotation input) {
      return new ChangeAdapter(input);
    }
  };

  static final Function<ChangeAdapter, Annotation> TO_ANNOTATION = new Function<ChangeAdapter, Annotation>() {
    @Override
    public Annotation apply(ChangeAdapter input) {
      return input.getAnnotation();
    }
  };

  static final Function<ChangeAdapter, String> TO_REV_TYPE = new Function<ChangeAdapter, String>() {
    @Override
    public String apply(ChangeAdapter input) {
      return Objects.firstNonNull(input.getRevisionType(), "");
    }
  };

  static final Predicate<ChangeAdapter> HAS_CHANGE_SET_REF = new Predicate<ChangeAdapter>() {
    @Override
    public boolean apply(ChangeAdapter input) {
      return !Strings.isNullOrEmpty(input.getChangeSetRef());
    }
  };

  static final Predicate<ChangeAdapter> HAS_REV_TYPE = new Predicate<ChangeAdapter>() {
    @Override
    public boolean apply(ChangeAdapter input) {
      return !Strings.isNullOrEmpty(input.getRevisionType());
    }
  };
}
