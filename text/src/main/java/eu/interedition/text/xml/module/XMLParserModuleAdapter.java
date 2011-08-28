/*
 * #%L
 * Text: A text model with range-based markup via standoff annotations.
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
package eu.interedition.text.xml.module;

import eu.interedition.text.Range;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserModule;
import eu.interedition.text.xml.XMLParserState;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLParserModuleAdapter implements XMLParserModule {
  public void start(XMLParserState state) {
  }

  public void start(XMLEntity entity, XMLParserState state) {
  }

  public void startText(XMLParserState state) {
  }

  public void end(XMLEntity entity, XMLParserState state) {
  }

  public void text(String text, XMLParserState state) {
  }

  public void insertText(String read, String inserted, XMLParserState state) {
  }

  public void end(XMLParserState state) {
  }

  public void offsetMapping(XMLParserState state, Range textRange, Range sourceRange) {
  }

  public void endText(XMLParserState state) {
  }
}
