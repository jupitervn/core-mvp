package com.jupitervn.mvp.rx.presenter;

import com.jupitervn.mvp.rx.ObservableFactory;

import rx.Observable;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/21/16.
 */
public class StubObservableFactory implements ObservableFactory {
    boolean isActionCalled = false;
    @Override
    public Observable doAction() {
        isActionCalled = true;
        return Observable.empty();
    }
}
