package com.jupitervn.mvp.rx;

import rx.Observable;

/**
 * Provides a subscription. The main purpose is delay observable until a task is started.
 * Created by Jupiter (vu.cao.duy@gmail.com) on 12/15/15.
 */
public interface ObservableFactory<T> {
  Observable<T> doAction();
}
