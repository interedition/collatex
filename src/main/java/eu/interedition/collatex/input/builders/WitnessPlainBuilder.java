package eu.interedition.collatex.input.builders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import eu.interedition.collatex.input.Segment;

public class WitnessPlainBuilder extends WitnessStreamBuilder {

  @Override
  public Segment build(InputStream inputStream) throws IOException {
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
    WitnessBuilder builder = new WitnessBuilder();
    return builder.build(stringBuilder.toString());
  }

}
