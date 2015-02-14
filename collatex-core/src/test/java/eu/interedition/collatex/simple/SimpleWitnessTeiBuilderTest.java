/*
 * Copyright (c) 2013 The Interedition Development Group.
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

import eu.interedition.collatex.Token;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;

public class SimpleWitnessTeiBuilderTest {

    @Ignore
    @Test
    public void testTei() throws IOException, XMLStreamException {
        InputStream resourceAsStream = getClass().getResourceAsStream("/matenadaran_1731.xml");
        //System.out.println(resourceAsStream.available());
        SimpleWitnessTeiBuilder builder = new SimpleWitnessTeiBuilder();
        SimpleWitness w = builder.read(resourceAsStream);
        for (Token t : w) {
            System.out.print(((SimpleToken) t).getContent() + " ");
        }
        System.out.println();
        InputStream stream1767 = getClass().getResourceAsStream("/matenadaran_1767.xml");
        //System.out.println(resourceAsStream.available());
        builder = new SimpleWitnessTeiBuilder();
        SimpleWitness w2 = builder.read(stream1767);
        for (Token t : w2) {
            System.out.print(((SimpleToken) t).getContent() + " ");
        }
    }
}
