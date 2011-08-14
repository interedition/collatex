package eu.interedition.text.repository.atom;

import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;
import org.springframework.stereotype.Component;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class TextCollectionAdapter extends AbstractCollectionAdapter {
    @Override
    public String getAuthor(RequestContext request) throws ResponseContextException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getId(RequestContext request) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ResponseContext postEntry(RequestContext request) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ResponseContext deleteEntry(RequestContext request) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ResponseContext getEntry(RequestContext request) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ResponseContext putEntry(RequestContext request) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ResponseContext getFeed(RequestContext request) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getTitle(RequestContext request) {
        return "Texts";
    }
}
