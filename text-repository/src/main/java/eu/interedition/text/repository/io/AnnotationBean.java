package eu.interedition.text.repository.io;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import eu.interedition.text.Annotation;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.util.Annotations;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationBean implements Annotation {
  private QNameBean name;
  private Range range;

  public AnnotationBean() {
  }

  public AnnotationBean(QNameBean name, Range range) {
    this.name = name;
    this.range = range;
  }


  @JsonIgnore
  @Override
  public Text getText() {
    return null;
  }

  @JsonProperty("n")
  public QNameBean getName() {
    return name;
  }

  @JsonProperty("n")
  public void setName(QNameBean name) {
    this.name = name;
  }

  @JsonProperty("r")
  @JsonSerialize(using = RangeSerializer.class)
  public Range getRange() {
    return range;
  }

  @JsonProperty("r")
  @JsonDeserialize(using = RangeDeserializer.class)
  public void setRange(Range range) {
    this.range = range;
  }

  public static final Function<Annotation, AnnotationBean> TO_BEAN = new Function<Annotation, AnnotationBean>() {
    @Override
    public AnnotationBean apply(Annotation input) {
      return new AnnotationBean(QNameBean.TO_BEAN.apply(input.getName()), input.getRange());
    }
  };

  @Override
  public int compareTo(Annotation o) {
    return Annotations.compare(this, o).compare(this, o, Ordering.arbitrary()).result();
  }
}
