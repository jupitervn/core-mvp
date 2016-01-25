package com.jupitervn.mvp.common.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * An simple abstract presenter with view that implements some of the method of {@link BaseViewPresenter}
 * Created by Jupiter (vu.cao.duy@gmail.com) on 12/14/15.
 */
public abstract class PresenterWithView<V> implements BaseViewPresenter<V> {
    protected V view;

    @Override
    public void setView(V view) {
        this.view = view;
        if (view != null) {
            onTakeView(view);
        } else {
            onDropView();
        }
    }

    public abstract void onTakeView(V v);
    public abstract void onDropView();

    @Override
    public void create(Bundle bundle) {

    }

    @Nullable
    @Override
    public Bundle saveState() {
        return null;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause(boolean isFinishing) {

    }
}
