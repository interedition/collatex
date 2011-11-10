package eu.interedition.markup;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DSchemaBuilderImpl;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StopWatch;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextEncodingInitiativeMarkupReader {
    private static final Resource TEI_ALL_RNG = new ClassPathResource("/tei_all_20111109.rng");

    public static void main(String[] args) throws IOException, IllegalSchemaException {
        InputStream teiSchemaStream = null;
        try {
            final StopWatch sw = new StopWatch("tei_all");

            sw.start("parse");
            Locale.setDefault(Locale.US);
            final SAXParseable teiSchema = new SAXParseable(new InputSource(teiSchemaStream = TEI_ALL_RNG.getInputStream()), new DefaultHandler());
            final DSchemaBuilderImpl schemaBuilder = new DSchemaBuilderImpl();
            final RelaxNGSchemaTransformer transformer = new RelaxNGSchemaTransformer((DPattern) teiSchema.parse(schemaBuilder));
            sw.stop();

            sw.start("transform");
            transformer.run();
            sw.stop();

            final Map<QName,AnnotationType> annotationTypes = transformer.getAnnotationTypes();

            for (AnnotationType at : Sets.newTreeSet(annotationTypes.values())) {
                System.out.printf(at.toString()).printf(": ").println(Strings.nullToEmpty(at.getDocumentation()));
            }
            System.out.println(Strings.repeat("-", 80));
            for (AnnotationType at : Sets.newTreeSet(annotationTypes.values())) {
                System.out.printf(at.toString()).printf(": ").println(Iterables.toString(at.getContainers()));
            }
            System.out.println(Strings.repeat("-", 80));
            for (AnnotationType at : Sets.newTreeSet(annotationTypes.values())) {
                System.out.println(at.toString());
                for (AnnotationDataType adt : at.getDataTypes()) {
                    System.out.printf("\t").printf(adt.toString()).printf(": ").println(Strings.nullToEmpty(adt.getDocumentation()));
                }
            }
            System.out.println(Strings.repeat("-", 80));
            System.out.printf("\n").println(sw.prettyPrint());


        } finally {
            Closeables.close(teiSchemaStream, false);
        }
    }
}
