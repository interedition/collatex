package eu.interedition.markup;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import eu.interedition.markup.schema.RelaxNGSchemaTransformation;
import eu.interedition.markup.schema.Schema;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DSchemaBuilderImpl;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextEncodingInitiativeMarkupReader {
    private static final Resource TEI_ALL_RNG = new ClassPathResource("/tei_all_20111109.rng");

    public static void main(String[] args) throws IOException, IllegalSchemaException {
        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("service-context.xml", TextEncodingInitiativeMarkupReader.class);
        ctx.registerShutdownHook();

        final GraphDatabaseService db = ctx.getBean(GraphDatabaseService.class);
        final TransactionTemplate tt = new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class));
        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
            }
        });
    }

    private static final RelationshipType TEST = new RelationshipType() {
        @Override
        public String name() {
            return "TEST";
        }
    };
}
