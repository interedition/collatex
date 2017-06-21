/*
 * Copyright (c) 2015 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.simple;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// we are going to use stax
// previous version was build on DOM, which is not particularly well suited to parsing
// large TEI files, with segments and expansions
public class SimpleWitnessTeiBuilder {

    private static QName w = new QName("http://www.tei-c.org/ns/1.0", "w");
    private static QName seg = new QName("http://www.tei-c.org/ns/1.0", "seg");
    private static QName p = new QName("http://www.tei-c.org/ns/1.0", "p");

    public SimpleWitness read(InputStream input) throws XMLStreamException {
        SimpleWitness witness = new SimpleWitness("id");
        List<String> tokenContents = new ArrayList<>();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader reader = factory.createXMLEventReader(input);
        XMLEvent event = null;
        int numberOfParagraphs = 0;
        while ((event = reader.peek()) != null && numberOfParagraphs < 10) {
            // System.out.println(event.toString());
            if (event.isStartElement() && event.asStartElement().getName().equals(w)) {
                tokenContents.add(handleWElement(reader));
            } else if (event.isStartElement() && event.asStartElement().getName().equals(seg)) {
                tokenContents.add(handleSegElement(reader));
            } else if (event.isStartElement() && event.asStartElement().getName().equals(p)) {
                reader.next();
                numberOfParagraphs++;
            } else {
                reader.next();
            }
        }
        witness.setTokenContents(tokenContents.stream(), SimpleTokenNormalizers.LC_TRIM_WS_PUNCT);
        return witness;
    }

    private static String handleWElement(XMLEventReader reader) throws XMLStreamException {
        XMLEvent event = reader.nextEvent();
        // Do what you need to do with the start element, e.g. initialize
        // data structures
        // System.out.println("W tag is triggered here!");
        StringBuilder textBuffer = new StringBuilder();
        while ((event = reader.peek()) != null) {
            if (event.isEndElement() && event.asEndElement().getName().equals(w)) {
                // Do what you need to do at the end, e.g. add data
                // collected from sub elements, etc.
                event = reader.nextEvent();
                break;
            }

            // Do what you need to do for start or child elements, e.g.
            // dispatch to another handler function
            event = reader.nextEvent();
            textBuffer.append(event.toString());
            // System.out.println("Text :"+event.toString());
        }

        return textBuffer.toString();
    }

    private static String handleSegElement(XMLEventReader reader) throws XMLStreamException {
        XMLEvent event = reader.nextEvent();
        // Do what you need to do with the start element, e.g. initialize
        // data structures
        // System.out.println("Seg tag is triggered here!");
        StringBuilder textBuffer = new StringBuilder();
        while ((event = reader.peek()) != null) {
            if (event.isEndElement() && event.asEndElement().getName().equals(seg)) {
                // Do what you need to do at the end, e.g. add data
                // collected from sub elements, etc.
                event = reader.nextEvent();
                break;
            }

            // Do what you need to do for start or child elements, e.g.
            // dispatch to another handler function
            event = reader.nextEvent();
            if (event.getEventType() == XMLEvent.CHARACTERS) {
                textBuffer.append(event.toString().trim());
            }

        }
        return textBuffer.toString();
    }
}
