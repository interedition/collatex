package eu.interedition.collatex.http;

import eu.interedition.collatex.io.IOExceptionMapper;
import eu.interedition.collatex.io.SimpleCollationJSONMessageBodyReader;
import eu.interedition.collatex.io.VariantGraphJSONMessageBodyWriter;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ronald on 5/3/15.
 */
public class CollateApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<>();
        s.add(SimpleCollationJSONMessageBodyReader.class);
        s.add(VariantGraphJSONMessageBodyWriter.class);
        s.add(IOExceptionMapper.class);
        return s;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();
        singletons.add(new CollateResource("", 10, 0));
        return singletons;
    }
}


