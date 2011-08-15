package eu.interedition.text.rdbms;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.text.AbstractTest;
import eu.interedition.text.QName;
import eu.interedition.text.mem.SimpleQName;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class QNameTest extends AbstractTest {
  final SortedSet<QName> TEST_NAMES = Sets.<QName>newTreeSet(Sets.newHashSet(
          new SimpleQName((URI) null, "noNamespaceName"),
          new SimpleQName(TEST_NS, "namespacedName")
  ));

  @Test
  public void getNames() {
    final Set<QName> resolved = nameRepository.get(TEST_NAMES);
    Assert.assertEquals(2, resolved.size());
    Assert.assertEquals(2, Iterables.size(Iterables.filter(resolved, RelationalQName.class)));
  }
}
