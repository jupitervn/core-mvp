package com.jupitervn.mvp.rx.delivery;

import com.jupitervn.mvp.rx.ViewResultDelivery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Notification;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.BehaviorSubject;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/21/16.
 */
public class DeliveryReplayTransformerTest {

    private DeliveryReplayTransformer deliveryReplayTransformer;
    private BehaviorSubject views;

    @Before
    public void setUp() throws Exception {
        views = BehaviorSubject.create();
        deliveryReplayTransformer = new DeliveryReplayTransformer(views);
    }

    @After
    public void tearDown() throws Exception {
        views.onCompleted();
    }

    @Test
    public void testReplayShouldEmitAllItemsWhenViewAttach() throws Exception {
        Observable resultObservable = deliveryReplayTransformer.call(Observable.just(1, 2, 3));
        TestSubscriber testSubscriber = new TestSubscriber();
        resultObservable.subscribe(testSubscriber);
        Object view = new Object();
        views.onNext(view);

        testSubscriber.assertValueCount(4);
        testSubscriber.assertValues(new ViewResultDelivery<>(view, Notification.createOnNext(1)),
                new ViewResultDelivery<>(view, Notification.createOnNext(2)),
                new ViewResultDelivery<>(view, Notification.createOnNext(3)),
                new ViewResultDelivery<>(view, Notification.createOnCompleted()));
        testSubscriber.assertNotCompleted();
    }

    @Test
    public void testReplayShouldEmitAllItemsWhenViewReAttach() throws Exception {
        Observable resultObservable = deliveryReplayTransformer.call(Observable.just(1, 2, 3));
        TestSubscriber testSubscriber = new TestSubscriber();
        resultObservable.subscribe(testSubscriber);
        views.onNext(new Object());
        testSubscriber.assertValueCount(4);
        views.onNext(null);
        testSubscriber.assertValueCount(4); //No more items are emitted
        views.onNext(new Object());
        testSubscriber.assertValueCount(8); //New items are emitted
    }

    @Test
    public void testReplayShouldEmitErrorWhenViewAttach() throws Exception {
        Exception exception = new IllegalArgumentException();
        Observable resultObservable = deliveryReplayTransformer.call(Observable.error(exception));
        TestSubscriber testSubscriber = new TestSubscriber();
        resultObservable.subscribe(testSubscriber);
        testSubscriber.assertNoValues();
        Object view = new Object();
        views.onNext(view);
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValue(new ViewResultDelivery<>(view, Notification.createOnError(exception)));
    }

    @Test
    public void testReplayShouldEmitPastItemsHotObservable() throws Exception {
        TestScheduler testScheduler = new TestScheduler();
        Observable<Long> interval = Observable.interval(1, TimeUnit.SECONDS, testScheduler);
        ConnectableObservable<Long> publish = interval.publish();
        publish.connect();
        //Replay only starts after transformer has been called.
        Observable resultObservable = deliveryReplayTransformer.call(publish);
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS);
        TestSubscriber testSubscriber = new TestSubscriber();
        resultObservable.subscribe(testSubscriber);
        testSubscriber.assertValueCount(0);
        views.onNext(new Object());
        testSubscriber.assertValueCount(3);
        views.onNext(null);
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS);
        testSubscriber.assertValueCount(6); //more items are emitted.
        views.onNext(new Object());
        testSubscriber.assertValueCount(12); //New items are emitted
        resultObservable.unsubscribeOn(testScheduler);
    }
}