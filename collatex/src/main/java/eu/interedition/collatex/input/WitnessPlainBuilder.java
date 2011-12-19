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

package eu.interedition.collatex.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import eu.interedition.collatex.ITokenNormalizer;
import eu.interedition.collatex.IWitness;

public class WitnessPlainBuilder extends WitnessStreamBuilder {

  private final ITokenNormalizer tokenNormalizer;

  public WitnessPlainBuilder(ITokenNormalizer tokenNormalizer) {
    super(tokenNormalizer);
    this.tokenNormalizer = tokenNormalizer;
  }

  @Override
  public IWitness build(InputStream inputStream) throws IOException {
    InputStreamReader reader = new InputStreamReader(inputStream);

    BufferedReader bufferedReader = new BufferedReader(reader);
    StringBuilder stringBuilder = new StringBuilder();
    String line;
    try {
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line);
      }
    } finally {
      bufferedReader.close();
    }
    WitnessBuilder builder = new WitnessBuilder(tokenNormalizer);
    return builder.build(stringBuilder.toString());
  }

}
