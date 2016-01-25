package com.jupitervn.mvp.rx.delivery;

import com.jupitervn.mvp.rx.ViewResultDelivery;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.subjects.ReplaySubject;

/**
 *
 * Re-emit all the items when view is attached.
 *
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/21/16.
 */
public class DeliveryReplayTransformer<V, RESULT> implements Observable.Transformer<RESULT, ViewResultDelivery<V, RESULT>> {
    private Observable<V> viewObservable;

    public DeliveryReplayTransformer(Observable<V> viewObservable) {
        this.viewObservable = viewObservable;
    }

    @Override
    public Observable<ViewResultDelivery<V, RESULT>> call(Observable<RESULT> resultObservable) {
        ReplaySubject<Notification> replaySubject = ReplaySubject.create();
        Subscription subscription = resultObservable.materialize().subscribe(replaySubject);
        return viewObservable.flatMap(new Func1<V, Observable<ViewResultDelivery<V, RESULT>>>() {
            @Override
            public Observable<ViewResultDelivery<V, RESULT>> call(V v) {
                if (v != null) {
                    return replaySubject.map(new Func1<Notification, ViewResultDelivery<V, RESULT>>() {
                        @Override
                        public ViewResultDelivery<V, RESULT> call(Notification notification) {
                            return new ViewResultDelivery<V, RESULT>(v, notification);
                        }
                    }).doOnUnsubscribe(() -> {
                        subscription.unsubscribe();
                    });
                } else {
                    return Observable.never();
                }
            }
        });
    }
}
