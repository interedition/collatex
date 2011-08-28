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
package eu.interedition.text.xml;

import eu.interedition.text.Range;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface XMLParserModule {
  void start(XMLParserState state);

  void start(XMLEntity entity, XMLParserState state);

  void startText(XMLParserState state);

  void text(String text, XMLParserState state);

  void endText(XMLParserState state);

  void end(XMLEntity entity, XMLParserState state);

  void insertText(String read, String inserted, XMLParserState state);

  void end(XMLParserState state);

  void offsetMapping(XMLParserState state, Range textRange, Range sourceRange);
}
