package eu.interedition.text.edit;

import com.google.common.collect.Iterables;
import org.junit.Test;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ChangeListTest {

  @Test
  public void construction() {
    final ChangeList list = new ChangeList(10)
            .insert("hello\n\"world\"")
            .retain(5, 10)
            .insert("!")
            .insert(" Test");
    System.out.println(list + ": " + Iterables.toString(list.getRemovals()));
  }
}
