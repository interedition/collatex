/*
 * #%L
 * Text Repository: Datastore for texts based on Interedition's model.
 * %%
 * Copyright (C) 2010 - 2011 The Interedition Development Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.interedition.text.repository.atom;

import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.AbstractWorkspaceProvider;
import org.apache.abdera.protocol.server.impl.RouteManager;
import org.apache.abdera.protocol.server.impl.SimpleWorkspaceInfo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service("org.apache.abdera.protocol.server.Provider")
public class RepositoryWorkspaceProvider extends AbstractWorkspaceProvider implements InitializingBean {

    public static final String BASE_PATH = "/atom/";
    @Autowired
    private TextCollectionAdapter textCollectionAdapter;

    @Override
    public CollectionAdapter getCollectionAdapter(RequestContext request) {
        return textCollectionAdapter;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final RouteManager routeManager = new RouteManager();
        routeManager.addRoute("service", BASE_PATH, TargetType.TYPE_SERVICE);
        //routeManager.addRoute("search", BASE_PATH + "search", OpenSearchFilter.TYPE_OPENSEARCH_DESCRIPTION);
        routeManager.addRoute("feed", BASE_PATH + ":collection", TargetType.TYPE_COLLECTION);
        routeManager.addRoute("entry", BASE_PATH + ":collection/:entry", TargetType.TYPE_ENTRY);
        routeManager.addRoute("categories", BASE_PATH + ":collection/:entry;categories", TargetType.TYPE_CATEGORIES);

        setTargetBuilder(routeManager);
        setTargetResolver(routeManager);

        final SimpleWorkspaceInfo textWorkspace = new SimpleWorkspaceInfo("Texts");
        textWorkspace.addCollection(textCollectionAdapter);

        addWorkspace(textWorkspace);
    }
}
