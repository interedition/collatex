package eu.interedition.text.json.map;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextModule extends SimpleModule {

  public TextModule() {
    super(TextModule.class.getPackage().getName(), Version.unknownVersion());
    addSerializer(new TextSerializer());
    addSerializer(new NameSerializer());
    addSerializer(new TextTargetSerializer());
    addSerializer(new AnnotationSerializer());

    //addDeserializer(Name.class, new NameDeserializer());
    //addDeserializer(TextTarget.class, new RangeDeserializer());
  }

}
