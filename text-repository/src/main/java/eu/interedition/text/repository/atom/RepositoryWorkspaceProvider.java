package eu.interedition.text.repository.atom;

import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.impl.AbstractWorkspaceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service("org.apache.abdera.protocol.server.Provider")
public class RepositoryWorkspaceProvider extends AbstractWorkspaceProvider {

    @Autowired
    private TextCollectionAdapter textCollectionAdapter;

    @Override
    public CollectionAdapter getCollectionAdapter(RequestContext request) {
        return textCollectionAdapter;
    }
}
