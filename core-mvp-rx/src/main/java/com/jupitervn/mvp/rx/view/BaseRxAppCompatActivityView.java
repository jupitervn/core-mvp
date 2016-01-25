package com.jupitervn.mvp.rx.view;

import com.jupitervn.mvp.common.presenter.BaseViewPresenter;
import com.jupitervn.mvp.common.presenter.PresenterLifecycle;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 12/6/15.
 */
public class BaseRxAppCompatActivityView<P extends BaseViewPresenter> extends RxAppCompatActivity {

    @VisibleForTesting
    private PresenterLifecycle<P> presenterLifecycle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        assert presenterLifecycle != null;
        presenterLifecycle.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenterLifecycle.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenterLifecycle.onPause(isFinishing());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        presenterLifecycle.onSavePresenterState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenterLifecycle.destroyPresenterIfNeeded(isFinishing());
    }

    protected P getPresenter() {
        return presenterLifecycle.getPresenter();
    }

    /**
     * Set the presenter lifecycle delegate. This method should be called in onCreate of subclass.
     */
    public void setPresenterLifecycle(PresenterLifecycle<P> presenterLifecycle) {
        this.presenterLifecycle = presenterLifecycle;
    }
}
