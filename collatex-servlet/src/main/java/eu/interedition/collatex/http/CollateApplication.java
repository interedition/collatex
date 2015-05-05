package eu.interedition.collatex.http;

import eu.interedition.collatex.io.VariantGraphJSONMessageBodyWriter;
import org.glassfish.jersey.jackson1.Jackson1Feature;

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
        s.add(VariantGraphJSONMessageBodyWriter.class);
        //TODO: remove depdendency on Jackson 1
        //TODO: To remove this.. use JSON read code from JSONProcessor class.
        s.add(ObjectMapperProvider.class);
        s.add(Jackson1Feature.class);
        return s;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();
        singletons.add(new CollateResource("", 10, 0));
        return singletons;
    }
}


