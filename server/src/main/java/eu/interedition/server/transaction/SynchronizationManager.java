package eu.interedition.server.transaction;

/**
 * @author mh
 * @since 15.02.11
 */
public interface SynchronizationManager {
  void initSynchronization();

  boolean isSynchronizationActive();

  void clearSynchronization();
}
