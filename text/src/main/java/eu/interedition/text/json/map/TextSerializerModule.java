package eu.interedition.text.json.map;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextSerializerModule extends SimpleModule {

  public TextSerializerModule() {
    super(TextSerializerModule.class.getPackage().getName(), new Version(1, 0, 0, ""));
    addSerializer(new TextSerializer());
    addSerializer(new QNameSerializer());
    addSerializer(new RangeSerializer());
    addSerializer(new AnnotationSerializer());
  }

}
