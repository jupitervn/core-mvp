package com.jupitervn.mvp.common.view;

import com.jupitervn.mvp.common.presenter.BaseViewPresenter;
import com.jupitervn.mvp.common.presenter.PresenterLifecycle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 12/19/15.
 */
public class BaseFragmentView<P extends BaseViewPresenter> extends Fragment {
    @VisibleForTesting
    private PresenterLifecycle<P> presenterLifecycle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenterLifecycle.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenterLifecycle.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenterLifecycle.onPause(getActivity().isFinishing());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        presenterLifecycle.onSavePresenterState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterLifecycle.destroyPresenterIfNeeded(getActivity().isFinishing());
    }

    /**
     * Use to create presenter lifecycle, should be set in onCreate.
     * @param presenterLifecycle
     */
    public void setPresenterLifecycle(PresenterLifecycle<P> presenterLifecycle) {
        this.presenterLifecycle = presenterLifecycle;
    }
}
