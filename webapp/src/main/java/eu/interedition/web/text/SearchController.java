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
package eu.interedition.web.text;

import org.apache.lucene.queryParser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Controller
@RequestMapping(SearchController.URL_PREFIX)
public class SearchController {
  public static final String URL_PREFIX = "/search";

  @Autowired
  private TextIndex textIndex;

  @RequestMapping(method = RequestMethod.GET)
  public ModelAndView search(@ModelAttribute("query") TextIndexQuery query) throws IOException, ParseException {
    final List<TextIndexQueryResult> searchResults = textIndex.search(query);
    return (searchResults.size() == 1) ?
            new ModelAndView(TextController.redirectTo(searchResults.get(0))) :
            new ModelAndView("search", "results", searchResults);
  }
}
