package com.jupitervn.mvp.rx.delivery;

import com.jupitervn.mvp.rx.ViewResultDelivery;

import rx.Observable;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/25/16.
 */
public class DeliveryLatestTransformer<RESULT, V> implements Observable.Transformer<RESULT, ViewResultDelivery<V, RESULT>> {

    private Observable<V> viewObservable;

    public DeliveryLatestTransformer(Observable<V> viewObservable) {
        this.viewObservable = viewObservable;
    }

    @Override
    public Observable<ViewResultDelivery<V, RESULT>> call(Observable<RESULT> resultObservable) {
        return Observable.combineLatest(resultObservable
                        .materialize()
                        .filter(resultNotification -> !resultNotification.isOnCompleted()),
                viewObservable, (notification, v) -> v != null ? new ViewResultDelivery<>(v, notification) : null)
                .filter(result1 -> result1 != null);
    }
}
