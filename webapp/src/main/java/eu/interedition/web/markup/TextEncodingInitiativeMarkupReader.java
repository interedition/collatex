package eu.interedition.web.markup;

import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;

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
