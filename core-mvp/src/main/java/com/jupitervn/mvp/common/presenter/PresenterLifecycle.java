package com.jupitervn.mvp.common.presenter;

import android.os.Bundle;

/**
 * Presenter lifecycle delegate to integrate into activity/fragment of your choice.
 *
 * Created by Jupiter (vu.cao.duy@gmail.com) on 12/14/15.
 */
public class PresenterLifecycle<P extends BaseViewPresenter> {

    public static final String PRESENTER_ID_KEY = "presenter-id-key";
    public static final String PRESENTER_DATA_KEY = "presenter-data-key";
    private PresenterStorage presenterStorage;
    private PresenterFactory<P> presenterFactory;
    private P presenter;

    public PresenterLifecycle(PresenterStorage presenterStorage, PresenterFactory<P> presenterFactory) {
        assert presenterStorage != null;
        assert presenterFactory != null;
        this.presenterStorage = presenterStorage;
        this.presenterFactory = presenterFactory;
    }

    public P onCreate(Bundle savedInstanceState) {
        String presenterKey = null;
        Bundle presenterSavedState = null;
        if (savedInstanceState != null) {
            presenterKey = savedInstanceState.getString(PRESENTER_ID_KEY);
            presenterSavedState = savedInstanceState.getBundle(PRESENTER_DATA_KEY);
        }
        if (presenter == null && savedInstanceState != null) {
            presenter = (P) presenterStorage.getPresenterWithId(presenterKey);
        }
        if (presenter == null) {
            presenter = presenterFactory.createPresenter();
            if (presenter != null) {
                presenterStorage.putPresenter(presenter);
                presenter.create(presenterSavedState);
            } else {
                throw new IllegalArgumentException("Please provide an instance of presenter");
            }
        }
        return presenter;
    }

    public void onSavePresenterState(Bundle outState) {
        if (presenter != null) {
            if (outState == null) {
                outState = new Bundle();
            }
            Bundle presenterBundle = presenter.saveState();
            outState.putString(PRESENTER_ID_KEY, presenterStorage.getIdOfPresenter(presenter));
            outState.putBundle(PRESENTER_DATA_KEY, presenterBundle);
        }
    }

    public void onResume(Object view) {
        if (presenter != null) {
            presenter.setView(view);
            presenter.onResume();
        }
    }

    public void onPause(boolean shouldDestroy) {
        if (presenter != null) {
            presenter.setView(null);
            presenter.onPause(shouldDestroy);
            if (shouldDestroy) {
                destroyPresenterIfNeeded(shouldDestroy);
            }
        }
    }

    public P getPresenter() {
        return presenter;
    }

    public void destroyPresenterIfNeeded(boolean shouldDestroy) {
        if (shouldDestroy && presenter != null) {
            presenter.onDestroy();
            presenterStorage.destroyPresenter(presenter);
            presenter = null;
        }
    }
}
