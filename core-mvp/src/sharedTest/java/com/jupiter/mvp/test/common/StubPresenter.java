package com.jupiter.mvp.test.common;

import com.jupitervn.mvp.common.presenter.BaseViewPresenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/13/16.
 */
public class StubPresenter implements BaseViewPresenter {

    @Override
    public void setView(Object view) {

    }

    @Override
    public void create(Bundle bundle) {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause(boolean isFinishing) {

    }

    @Nullable
    @Override
    public Bundle saveState() {
        return null;
    }

    @Override
    public void onDestroy() {

    }
}
