/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.web;

import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.output.apparatus.ParallelSegmentationApparatus;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

@Controller
@RequestMapping("/examples/**")
public class ExamplesController implements InitializingBean {
  private CollateXEngine engine = new CollateXEngine();

  @Autowired
  private ApplicationContext applicationContext;

  private List<IWitness[]> usecases;
  private List<IWitness[]> darwin;
  
  @RequestMapping("usecases")
  public ModelMap collateUseCases() {
    List<IAlignmentTable> alignments = Lists.newArrayListWithCapacity(usecases.size());
    for (IWitness[] example : usecases) {
      alignments.add(engine.align(example));
    }
    return new ModelMap("examples", alignments);
  }

  @RequestMapping("darwin")
  public ModelMap collateDarwin() {
    List<ParallelSegmentationApparatus> alignments = Lists.newArrayListWithCapacity(darwin.size());
    
   for (IWitness[] paragraph : darwin) {
      IVariantGraph graph = engine.graph(paragraph);
      alignments.add(engine.createApparatus(graph));
   }
    return new ModelMap("paragraphs", alignments);
  }
  
  @Override
  public void afterPropertiesSet() throws Exception {
    usecases = parseWitnesses(applicationContext.getResource("/examples.xml"));
    darwin = parseWitnesses(applicationContext.getResource("/darwin.xml"));
  }
  
  private List<IWitness[]> parseWitnesses(Resource resource) throws Exception {
    Document examplesDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resource.getInputStream());
    NodeList exampleNodes = examplesDoc.getDocumentElement().getElementsByTagName("example");
    List<IWitness[]> examples = Lists.newArrayListWithCapacity(exampleNodes.getLength());
    for (int ec = 0; ec < exampleNodes.getLength(); ec++) {
      Node exampleNode = exampleNodes.item(ec);
      if (exampleNode.getNodeType() == Node.ELEMENT_NODE) {
        NodeList witnessNodes = ((Element) exampleNode).getElementsByTagName("witness");
        IWitness[] witnesses = new IWitness[witnessNodes.getLength()];
        for (int wc = 0; wc < witnessNodes.getLength(); wc++) {
          Element witnessElement = (Element) witnessNodes.item(wc);
          String id = witnessElement.hasAttribute("id") ? witnessElement.getAttribute("id") : String.valueOf(wc);
          witnesses[wc] = engine.createWitness(id, witnessElement.getTextContent());
        }
        examples.add(witnesses);
      }
    }
    return Collections.unmodifiableList(examples);
  }
}
