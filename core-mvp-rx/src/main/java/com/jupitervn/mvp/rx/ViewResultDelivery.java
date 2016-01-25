package com.jupitervn.mvp.rx;

import android.support.annotation.Nullable;

import rx.Notification;
import rx.functions.Action2;

/**
 * A Simple class contains view and data from observable.
 * Created by Jupiter (vu.cao.duy@gmail.com) on 12/15/15.
 */
public class ViewResultDelivery<V, T> {
    protected V view;
    protected Notification<T> notification;

    public ViewResultDelivery(V view, Notification<T> notification) {
        this.view = view;
        this.notification = notification;
    }

    public void passResult(Action2<V, T> onNext, @Nullable Action2<V, Throwable> onError) {
        if (notification.isOnError()) {
            if (onError != null) {
                onError.call(view, notification.getThrowable());
            }
        } else if (notification.isOnNext()) {
            onNext.call(view, notification.getValue());
        }
    }

    @Override
    public String toString() {
        return "ViewResultDelivery{" +
                "view=" + view +
                ", notification=" + notification +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ViewResultDelivery<?, ?> that = (ViewResultDelivery<?, ?>) o;

        if (!view.equals(that.view)) {
            return false;
        }
        return notification.equals(that.notification);

    }

    @Override
    public int hashCode() {
        int result = view.hashCode();
        result = 31 * result + notification.hashCode();
        return result;
    }
}
