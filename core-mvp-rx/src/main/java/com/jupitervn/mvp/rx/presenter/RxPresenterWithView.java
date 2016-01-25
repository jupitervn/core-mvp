package com.jupitervn.mvp.rx.presenter;

import com.jupitervn.mvp.common.presenter.PresenterWithView;
import com.jupitervn.mvp.rx.ObservableFactory;
import com.jupitervn.mvp.rx.ViewResultDelivery;
import com.jupitervn.mvp.rx.delivery.DeliveryLatestTransformer;
import com.jupitervn.mvp.rx.delivery.DeliveryReplayTransformer;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action2;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Simple presenter that make use of rxjava to solve the following problems:<br> - Only return the result of async task
 * if view is attached when task finished. - Return task's result if configuration changes without executing it
 * again.<br> - If process is killed, auto restart tasks that are executing.<br> - If user kill the view before tasks
 * finish, these task will be cancelled.<br> - Support to cancel an executing task.<br>
 *
 * Created by Jupiter (vu.cao.duy@gmail.com) on 12/14/15.
 */
public class RxPresenterWithView<V> extends PresenterWithView<V> {

    @VisibleForTesting
    private CompositeSubscription compositeSubscriptions;

    private BehaviorSubject<V> viewSubject = BehaviorSubject.create();

    @VisibleForTesting
    private Map<Integer, Subscription> runningTasks = new HashMap<>();

    public static final int START_SUCCESSFULLY = 0x1;
    public static final int TASK_ALREADY_RUNNING = 0x2;
    public static final int TASK_START_FAIL = 0x3;

    @IntDef({START_SUCCESSFULLY, TASK_ALREADY_RUNNING, TASK_START_FAIL})
    public @interface START_TASK_RESULT {

    }

    @Override
    public void onTakeView(V v) {
        viewSubject.onNext(v);
    }

    @Override
    public void onDropView() {
        viewSubject.onNext(null);
    }

    @Nullable
    @Override
    public Bundle saveState() {
        return null;
    }

    @Override
    @CallSuper
    public void onDestroy() {
        viewSubject.onCompleted();
        if (compositeSubscriptions != null) {
            compositeSubscriptions.unsubscribe();
        }
    }

    protected Observable<V> getView() {
        return viewSubject;
    }

    /**
     * Add a task to execute and stop when presenter is killed.
     */
    protected void addTask(Subscription subscriptions) {
        if (compositeSubscriptions == null) {
            compositeSubscriptions = new CompositeSubscription();
        }
        this.compositeSubscriptions.add(subscriptions);
    }

    /**
     * Remove the task out of presenter's lifecycle.<br> Un-subscribe the task will be your responsibility.
     */
    protected void removeTask(Subscription subscription) {
        compositeSubscriptions.remove(subscription);
    }

    /**
     * Start a task and only emit last result if view is attached.<br>
     * If view is reattached then the last result will be emitted to the callback.
     *
     * @param taskFactory
     * @param onSuccess
     * @param onError
     * @param <RESULT>
     * @return taskID that is automatically generated.
     */
    protected <RESULT> int startViewTask(ObservableFactory<RESULT> taskFactory, Action2<V, RESULT> onSuccess,
            Action2<V, Throwable> onError) {
        int taskId;
        do {
            taskId = UUID.randomUUID().hashCode();
        } while (runningTasks.containsKey(taskId));
        startViewTask(taskId, taskFactory, onSuccess, onError);
        return taskId;
    }

    /**
     * Start a task with specified id.<br>
     * If task with this id is already running then it will not start a new task.
     *
     * @param taskId
     * @param taskFactory
     * @param onSuccess
     * @param onError
     * @param <RESULT>
     * @return
     */
    protected @START_TASK_RESULT <RESULT> int startViewTask(int taskId, ObservableFactory<RESULT> taskFactory,
            Action2<V, RESULT> onSuccess, Action2<V, Throwable> onError) {
        return startViewTaskWithAction(taskId, taskFactory, new DeliveryLatestTransformer<>(viewSubject), onSuccess, onError);
    }

    /**
     * Start a task and only emit all result if view is attached.<br>
     * If view is reattached then all result will be emitted to the callback.
     *
     * @param taskId
     * @param taskFactory
     * @param onSuccess
     * @param onError
     * @param <RESULT>
     * @return
     */
    protected @START_TASK_RESULT <RESULT> int startViewTaskReplay(int taskId, ObservableFactory<RESULT> taskFactory,
            Action2<V, RESULT> onSuccess, Action2<V, Throwable> onError) {
        return startViewTaskWithAction(taskId, taskFactory, new DeliveryReplayTransformer<>(viewSubject), onSuccess, onError);
    }

    /**
     * Start a task and only emit all result if view is attached.<br>
     * Result will be transformed based on resultTransformer.
     *
     * @param taskId
     * @param taskFactory
     * @param resultTransformer
     * @param onSuccess
     * @param onError
     * @param <RESULT>
     * @return
     */
    protected @START_TASK_RESULT <RESULT> int startViewTaskWithAction(int taskId, ObservableFactory<RESULT> taskFactory,
            Observable.Transformer<RESULT, ViewResultDelivery<V, RESULT>> resultTransformer,
            Action2<V, RESULT> onSuccess, Action2<V, Throwable> onError) {
        @START_TASK_RESULT int startTaskResult = START_SUCCESSFULLY;
        if (!isTaskRunning(taskId)) {
            Subscription subscription = taskFactory.doAction()
                    .compose(resultTransformer)
                    .subscribe(viewResultDelivery -> {
                        viewResultDelivery.passResult(onSuccess, onError);
                    });
            putRunningTask(taskId, subscription);
        } else {
            startTaskResult = TASK_ALREADY_RUNNING;
        }
        return startTaskResult;
    }

    /**
     * Cancel a running task with id.<br> If a task has not emitted result, the callback will not be called.<br> Else,
     * the task will be unsubscribed and removed.
     */
    protected void cancelRunningTask(int taskId) {
        Subscription subscription = runningTasks.get(taskId);
        if (subscription != null && !subscription.isUnsubscribed()) {
            compositeSubscriptions.remove(subscription);
            subscription.unsubscribe();
        }
        runningTasks.remove(taskId);
    }

    /**
     * Check if a task with id is running or not.
     */
    protected boolean isTaskRunning(int taskId) {
        Subscription subscription = runningTasks.get(taskId);
        return subscription != null && !subscription.isUnsubscribed();
    }


    private void putRunningTask(int taskId, Subscription taskSubscription) {
        addTask(taskSubscription);
        this.runningTasks.put(taskId, taskSubscription);
    }
}
