package eu.interedition.server;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Map;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class TransactionFilter extends Filter {
  private static final String TX_ATTRIBUTE = "tx";
  private final PlatformTransactionManager transactionManager;

  public TransactionFilter(Context context, Restlet next, PlatformTransactionManager transactionManager) {
    super(context, next);
    this.transactionManager = transactionManager;
  }

  @Override
  protected int beforeHandle(Request request, Response response) {
    final Map<String, Object> responseAttributes = response.getAttributes();
    if (!responseAttributes.containsKey(TX_ATTRIBUTE)) {
      responseAttributes.put(TX_ATTRIBUTE, transactionManager.getTransaction(new DefaultTransactionDefinition()));
    }
    return super.beforeHandle(request, response);
  }

  @Override
  protected void afterHandle(Request request, Response response) {
    TransactionStatus tx = (TransactionStatus) response.getAttributes().remove(TX_ATTRIBUTE);
    if (tx != null) {
      if (response.getStatus().isError()) {
        tx.setRollbackOnly();
      }
      transactionManager.commit(tx);
    }
    super.afterHandle(request, response);
  }
}
