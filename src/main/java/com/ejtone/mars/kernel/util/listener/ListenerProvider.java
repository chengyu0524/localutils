package com.ejtone.mars.kernel.util.listener;

public interface ListenerProvider<T extends Object> {

  public void registListener(T listener);

  public void removeListener(T listener);
}
