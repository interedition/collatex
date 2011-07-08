package eu.interedition.text.rdbms;

import com.google.common.base.Joiner;
import eu.interedition.text.*;
import eu.interedition.text.util.QNameImpl;
import eu.interedition.text.xml.XMLParser;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class RelationalXMLParser extends XMLParser {
  private static final Joiner PATH_JOINER = Joiner.on('.');

  protected DataSource dataSource;
  protected SimpleJdbcTemplate jt;

  protected QNameRepository nameRepository;
  protected AnnotationRepository annotationRepository;
  protected AnnotationDataRepository annotationDataRepository;

  protected ThreadLocal<Map<Annotation, Map<String, Object>>> annotations = new ThreadLocal<Map<Annotation, Map<String, Object>>>();

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    this.jt = (dataSource == null ? null : new SimpleJdbcTemplate(dataSource));
  }

  public void setNameRepository(QNameRepository nameRepository) {
    this.nameRepository = nameRepository;
  }

  public void setAnnotationRepository(AnnotationRepository annotationRepository) {
    this.annotationRepository = annotationRepository;
  }

  public void setAnnotationDataRepository(AnnotationDataRepository annotationDataRepository) {
    this.annotationDataRepository = annotationDataRepository;
  }

  protected Annotation startAnnotation(Session session, QName name, Map<QName, String> attrs, int start) {

    final Map<String, Object> annotation = new HashMap<String, Object>();
    annotation.put("text", session.target);
    annotation.put("name", name);
    annotation.put("start", start);
    annotation.put("attributes", attrs);

    Map<Annotation, Map<String, Object>> annotations = this.annotations.get();
    if (annotations == null) {
      this.annotations.set(annotations = new HashMap<Annotation, Map<String, Object>>());
    }

    final AnnotationHandle annotationHandle = new AnnotationHandle(name, new Range(start, start));
    annotations.put(annotationHandle, annotation);
    return annotationHandle;
  }

  protected Annotation endAnnotation(Annotation annotation, int offset) {
    final Map<String, Object> annotationData = annotations.get().remove(annotation);

    final Annotation created =//
            annotationRepository.create((RelationalText) annotationData.get("text"), (QName) annotationData.get("name"),//
                    new Range((Integer) annotationData.get("start"), offset));

    annotationDataRepository.set(created, (Map<QName, String>) annotationData.get("attributes"));
    return created;
  }

  private static class AnnotationHandle implements Annotation {

    private final QName name;
    private final Range range;

    private AnnotationHandle(QName name, Range range) {
      this.name = name;
      this.range = range;
    }

    public QName getName() {
      return name;
    }

    public Range getRange() {
      return range;
    }

    public int compareTo(Annotation o) {
      throw new UnsupportedOperationException();
    }
  }
}
