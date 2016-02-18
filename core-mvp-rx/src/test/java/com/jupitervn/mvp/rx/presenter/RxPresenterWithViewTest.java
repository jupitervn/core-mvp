package com.jupitervn.mvp.rx.presenter;

import com.jupitervn.mvp.rx.ObservableFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.functions.Action2;
import rx.observables.ConnectableObservable;
import rx.observers.TestSubscriber;
import rx.plugins.RxJavaPlugins;
import rx.plugins.RxJavaSchedulersHook;
import rx.plugins.RxJavaTestPlugins;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/19/16.
 */
public class RxPresenterWithViewTest {

    private RxPresenterWithView rxPresenterWithView;
    private Action2 mockSuccessCallback;
    private Action2 mockErrorCallback;
    private Object view;

    @BeforeClass
    public static void setUpClass() throws Exception {
        RxJavaTestPlugins.resetPlugins();
        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
        RxJavaPlugins.getInstance().registerSchedulersHook(new RxJavaSchedulersHook() {
            @Override
            public Scheduler getIOScheduler() {
                return Schedulers.immediate();
            }

        });
    }


    @Before
    public void setUp() throws Exception {
        rxPresenterWithView = spy(new RxPresenterWithView());
        view = new Object();

    }

    @After
    public void tearDown() throws Exception {

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        RxJavaTestPlugins.resetPlugins();
        RxAndroidPlugins.getInstance().reset();
    }

    @Test
    public void testOnTakeViewShouldSignalViewSubject() throws Exception {
        Observable viewObservable = rxPresenterWithView.getView();
        TestSubscriber testSubscriber = TestSubscriber.create();
        viewObservable.subscribe(testSubscriber);
        Object mockView = mock(Object.class);
        rxPresenterWithView.onTakeView(mockView);
        testSubscriber.assertNoErrors();
        testSubscriber.assertValue(mockView);
    }

    @Test
    public void testOnDropViewShouldSignalViewSubject() throws Exception {
        Observable viewObservable = rxPresenterWithView.getView();
        TestSubscriber testSubscriber = TestSubscriber.create();
        viewObservable.subscribe(testSubscriber);
        rxPresenterWithView.onDropView();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValue(null);
    }

    @Test
    public void testStartViewTaskShouldNotInvokeCallbackIfNoView() throws Exception {
        setupTask();
        verifyNoMoreInteractions(mockSuccessCallback);
        verifyNoMoreInteractions(mockErrorCallback);
    }

    @Test
    public void testStartViewTaskShouldInvokeCallbackIfViewAttached() throws Exception {
        setupTask();
        Object mockView = mock(Object.class);
        rxPresenterWithView.onTakeView(mockView);
        verify(mockSuccessCallback).call(eq(mockView), eq(3));
    }

    @Test
    public void testStartViewTaskShouldNotInvokeCallbackIfViewDetached() throws Exception {
        setupTask();
        rxPresenterWithView.onTakeView(view);
        verify(mockSuccessCallback).call(eq(view), eq(3));
        rxPresenterWithView.onDropView();
        verifyNoMoreInteractions(mockSuccessCallback);
        verifyNoMoreInteractions(mockErrorCallback);
    }

    @Test
    public void testStartViewTaskShouldReEmitAgainIfViewReAttach() throws Exception {
        setupTask();
        rxPresenterWithView.onTakeView(view);
        verify(mockSuccessCallback).call(eq(view), eq(3));
        rxPresenterWithView.onDropView();
        verifyNoMoreInteractions(mockSuccessCallback);
        verifyNoMoreInteractions(mockErrorCallback);
        Mockito.reset(mockSuccessCallback);
        Mockito.reset(mockErrorCallback);
        Object newView = new Object();
        rxPresenterWithView.onTakeView(newView);
        verify(mockSuccessCallback).call(eq(newView), eq(3));
    }

    @Test
    public void testStartViewTaskShouldReturnLastItemWhenViewAttached() throws Exception {
        TestScheduler testScheduler = new TestScheduler();
        Observable<Long> longObservable = Observable.interval(1, TimeUnit.SECONDS, testScheduler);
        setupTask(longObservable);
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS);
        rxPresenterWithView.onTakeView(view);
        verify(mockSuccessCallback).call(eq(view), eq(2L));
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS);
        verify(mockSuccessCallback).call(eq(view), eq(3L));
        verify(mockSuccessCallback).call(eq(view), eq(4L));
        verify(mockSuccessCallback).call(eq(view), eq(5L));
        longObservable.unsubscribeOn(testScheduler);
    }

    @Test
    public void testStartViewTaskShouldReturnLastItemWhenViewAttachedHotObservable() throws Exception {
        TestScheduler testScheduler = new TestScheduler();
        ConnectableObservable<Long> longObservable = Observable.interval(1, TimeUnit.SECONDS, testScheduler).publish();
        longObservable.connect();
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS);
        rxPresenterWithView.onTakeView(view);
        setupTask(longObservable);
        verify(mockSuccessCallback, times(0)).call(eq(view), anyInt());
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        verify(mockSuccessCallback).call(eq(view), eq(3L));
        longObservable.unsubscribeOn(testScheduler);
    }

    @Test
    public void testStartViewTaskShouldReturnItemIfViewAttachedBeforeTask() throws Exception {
        rxPresenterWithView.onTakeView(view);
        setupTask();
        verify(mockSuccessCallback).call(eq(view), eq(3));
    }

    @Test
    public void testStartViewTaskShouldReturnErrorIfViewAttached() throws Exception {
        IllegalArgumentException exception = new IllegalArgumentException();
        setupTask(Observable.error(exception));
        rxPresenterWithView.onTakeView(view);
        verifyNoMoreInteractions(mockSuccessCallback);
        verify(mockErrorCallback).call(eq(view), eq(exception));
    }

    @Test
    public void testStartViewTaskShouldReturnErrorIfViewAttachedBeforeTask() throws Exception {
        IllegalArgumentException exception = new IllegalArgumentException();
        rxPresenterWithView.onTakeView(view);
        setupTask(Observable.error(exception));
        verifyNoMoreInteractions(mockSuccessCallback);
        verify(mockErrorCallback).call(eq(view), eq(exception));
    }

    @Test
    public void testStartViewTaskShouldReturnErrorIfViewReAttached() throws Exception {
        IllegalArgumentException exception = new IllegalArgumentException();
        rxPresenterWithView.onTakeView(view);
        setupTask(Observable.error(exception));
        verifyNoMoreInteractions(mockSuccessCallback);
        verify(mockErrorCallback).call(eq(view), eq(exception));
        Mockito.reset(mockSuccessCallback, mockErrorCallback);
        rxPresenterWithView.onDropView();
        verifyNoMoreInteractions(mockSuccessCallback);
        verifyNoMoreInteractions(mockErrorCallback);
        Mockito.reset(mockSuccessCallback, mockErrorCallback);
        Object newView = new Object();
        rxPresenterWithView.onTakeView(newView);
        verifyNoMoreInteractions(mockSuccessCallback);
        verify(mockErrorCallback).call(eq(newView), eq(exception));
    }

    @Test
    public void testStartViewTaskShouldNotReturnAnyThingIfNoItemEmitted() throws Exception {
        setupTask(Observable.never());
        rxPresenterWithView.onTakeView(view);
        verifyNoMoreInteractions(mockErrorCallback);
        verifyNoMoreInteractions(mockSuccessCallback);
    }

    @Test
    public void testCancelRunningTaskPreventCallbacksToBeCalled() throws Exception {
        TestScheduler scheduler = new TestScheduler();
        int taskId = setupTask(Observable.interval(1, TimeUnit.SECONDS, scheduler));
        rxPresenterWithView.onTakeView(view);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        verify(mockSuccessCallback).call(eq(view), eq(0L));
        verifyNoMoreInteractions(mockErrorCallback);
        rxPresenterWithView.cancelRunningTask(taskId);
        scheduler.advanceTimeBy(3, TimeUnit.SECONDS);
        verifyNoMoreInteractions(mockSuccessCallback, mockErrorCallback);
    }

    @Test
    public void testIsRunningTaskShouldReturnTrueIfTaskIsNotCancelled() throws Exception {
        TestScheduler scheduler = new TestScheduler();
        int taskId = setupTask(Observable.interval(1, TimeUnit.SECONDS, scheduler));
        assertThat(rxPresenterWithView.isTaskRunning(taskId)).isTrue();
    }

    @Test
    public void testIsRunningTaskShouldReturnFalseIfTaskIsCancelled() throws Exception {
        TestScheduler scheduler = new TestScheduler();
        int taskId = setupTask(Observable.interval(1, TimeUnit.SECONDS, scheduler));
        rxPresenterWithView.cancelRunningTask(taskId);
        assertThat(rxPresenterWithView.isTaskRunning(taskId)).isFalse();
    }

    @Test
    public void testIsRunningTaskShouldReturnFalseIfTaskIdDoesNotExist() throws Exception {
        assertThat(rxPresenterWithView.isTaskRunning((new Random()).nextInt())).isFalse();
    }

    @Test
    public void testOnDestroyShouldCompleteViewObservable() throws Exception {
        TestSubscriber testSubscriber = new TestSubscriber();
        rxPresenterWithView.getView().subscribe(testSubscriber);
        rxPresenterWithView.onDestroy();
        testSubscriber.assertCompleted();
    }

    @Test
    public void testOnDestroyShouldUnsubscribeAllRunningTasks() throws Exception {
        List<Integer> taskIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            taskIds.add(setupTask());
        }
        rxPresenterWithView.onDestroy();
        for(int taskId : taskIds) {
            assertThat(rxPresenterWithView.isTaskRunning(taskId)).isFalse();
        }
    }

    @Test
    public void testStartViewTaskShouldExecuteTaskIfTaskNotStartedBefore() throws Exception {
        int taskId = -2;
        mockSuccessCallback = mock(Action2.class);
        mockErrorCallback = mock(Action2.class);
        StubObservableFactory observableFactory = new StubObservableFactory();
        int taskResult = rxPresenterWithView.startViewTask(taskId, observableFactory, mockSuccessCallback, mockErrorCallback);
        assertThat(observableFactory.isActionCalled).isTrue();
        assertThat(taskResult).isEqualTo(RxPresenterWithView.START_SUCCESSFULLY);
    }

    @Test
    public void testStartViewTaskShouldNotExecuteTaskIfTaskStartedBefore() throws Exception {
        int taskId = setupTask();
        ObservableFactory observableFactory = mock(ObservableFactory.class);
        int taskResult = rxPresenterWithView.startViewTask(taskId, observableFactory, mockSuccessCallback, mockErrorCallback);
        verifyNoMoreInteractions(observableFactory);
        assertThat(taskResult).isEqualTo(RxPresenterWithView.TASK_ALREADY_RUNNING);
    }



    private int setupTask() {
        Observable<Integer> rangeObservable = Observable.just(1, 2, 3);
        return setupTask(rangeObservable);
    }

    private int setupTask(Observable observable) {
        mockSuccessCallback = mock(Action2.class);
        mockErrorCallback = mock(Action2.class);
        return rxPresenterWithView.startViewTask(() -> observable, mockSuccessCallback, mockErrorCallback);
    }
}