package eu.interedition.text.repository;

import eu.interedition.text.Range;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RangeConverterTest extends AbstractTest{
  @Autowired
  private ConversionService conversionService;

  @Test
  public void convert() {
    Assert.assertEquals(new Range(0, 100), conversionService.convert("100", Range.class));
    Assert.assertEquals(new Range(0, 10), conversionService.convert("0,10", Range.class));
    Assert.assertEquals(new Range(100, 200), conversionService.convert("100,100", Range.class));
  }

}
